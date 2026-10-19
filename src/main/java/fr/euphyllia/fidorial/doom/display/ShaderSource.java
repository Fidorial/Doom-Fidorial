package fr.euphyllia.fidorial.doom.display;

import java.util.Locale;
import java.util.Random;

/**
 * Generates the replacement of the vanilla {@code text} core shader, which draws map canvases.
 *
 * <p>The vertex shader looks for the {@link ScreenEncoder} signature in the canvas of every quad it
 * draws. A Doom canvas has its quad stretched over the part of the screen its strip covers, in front
 * of everything else in the world; any other text or map is drawn as vanilla does. The fragment shader
 * reads the map color of each Doom pixel, turns it back into a symbol through a hash table, and paints
 * it with the exact Doom palette color.</p>
 */
public final class ShaderSource {

    private static final int HASH_SIZE = 1024;

    private final DoomPalette palette;
    private int k1 = 244;
    private int k2 = 801;
    private int k3 = 114;
    private int maxProbe;
    private final int[] table = new int[HASH_SIZE];

    public ShaderSource(final DoomPalette palette) {
        this.palette = palette;
        if (!buildTable()) {
            final Random random = new Random(1993);
            do {
                k1 = 1 + random.nextInt(HASH_SIZE - 1);
                k2 = 1 + random.nextInt(HASH_SIZE - 1);
                k3 = 1 + random.nextInt(HASH_SIZE - 1);
            } while (!buildTable());
        }
    }

    private int hash(final int rgb) {
        final int r = (rgb >> 16) & 0xff;
        final int g = (rgb >> 8) & 0xff;
        final int b = rgb & 0xff;
        return (r * k1 + g * k2 + b * k3) % HASH_SIZE;
    }

    /**
     * Fills the open addressing table; fails when a probe sequence gets longer than 3.
     */
    private boolean buildTable() {
        java.util.Arrays.fill(table, 0);
        maxProbe = 0;
        for (int symbol = 0; symbol < MapColors.COUNT; symbol++) {
            final int rgb = MapColors.rgb(MapColors.FIRST_ID + symbol);
            int h = hash(rgb);
            int probe = 0;
            while (table[h] != 0) {
                h = (h + 1) % HASH_SIZE;
                if (++probe > 3) {
                    return false;
                }
            }
            table[h] = rgb << 8 | symbol;
            maxProbe = Math.max(maxProbe, probe);
        }
        return true;
    }

    private String decoder() {
        final StringBuilder sb = new StringBuilder();
        sb.append("const int DOOM_HASH_SIZE = ").append(HASH_SIZE).append(";\n");
        sb.append("const int DOOM_MAX_PROBE = ").append(maxProbe).append(";\n");
        sb.append("const uint DOOM_TABLE[").append(HASH_SIZE).append("] = uint[](");
        for (int i = 0; i < HASH_SIZE; i++) {
            if (i % 16 == 0) {
                sb.append("\n    ");
            }
            sb.append(Integer.toUnsignedString(table[i])).append('u');
            if (i + 1 < HASH_SIZE) {
                sb.append(", ");
            }
        }
        sb.append("\n);\n\n");
        sb.append(String.format(Locale.ROOT, """
                // Symbol of a map pixel, or -1 when its color is not a map color.
                int doomDecode(vec4 texel) {
                    ivec3 c = ivec3(round(texel.rgb * 255.0));
                    uint key = uint(c.r << 16 | c.g << 8 | c.b);
                    if (key == 0u) {
                        return -1;
                    }
                    int h = (c.r * %d + c.g * %d + c.b * %d) %% DOOM_HASH_SIZE;
                    for (int i = 0; i <= DOOM_MAX_PROBE; i++) {
                        uint entry = DOOM_TABLE[h];
                        if ((entry >> 8u) == key) {
                            return int(entry & 255u);
                        }
                        h = (h + 1) %% DOOM_HASH_SIZE;
                    }
                    return -1;
                }
                """, k1, k2, k3));
        return sb.toString();
    }

    private String paletteTable() {
        final StringBuilder sb = new StringBuilder();
        final int n = DoomPalette.PALETTES * MapColors.COUNT;
        sb.append("const int DOOM_PALETTE[").append(n).append("] = int[](");
        for (int p = 0; p < DoomPalette.PALETTES; p++) {
            for (int s = 0; s < MapColors.COUNT; s++) {
                final int i = p * MapColors.COUNT + s;
                if (i % 16 == 0) {
                    sb.append("\n    ");
                }
                sb.append(s < palette.symbols() ? palette.symbolColor(p, s) : 0);
                if (i + 1 < n) {
                    sb.append(", ");
                }
            }
        }
        sb.append("\n);\n");
        return sb.toString();
    }

    private static String signatureCheck() {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ScreenEncoder.SIGNATURE.length; i++) {
            if (i > 0) {
                sb.append("\n        && ");
            }
            sb.append("doomDecode(texelFetch(Sampler0, origin + ivec2(").append(i).append(", 0), 0)) == ")
                    .append(ScreenEncoder.SIGNATURE[i]);
        }
        return sb.toString();
    }

    /**
     * {@return the shared Doom part: the hash table and decoder}
     */
    public String commonGlsl() {
        return decoder();
    }

    /**
     * {@return the Doom detection and quad placement, used by {@link #vertexShader()} and tests}
     */
    public String vertexGlsl() {
        return """
                // Corner of a map quad in vertex order, as the client builds it: (0,1) (1,1) (1,0) (0,0).
                vec2 doomCorner(int vertex) {
                    int i = vertex %% 4;
                    return vec2(i == 1 || i == 2 ? 1.0 : 0.0, i < 2 ? 1.0 : 0.0);
                }

                // Places a Doom canvas quad. Returns false when the texture is not a Doom canvas.
                bool doomPlace(vec2 uv, int vertex, mat4 proj, out vec4 position, out ivec4 info, out ivec2 originOut,
                               out vec2 coord) {
                    ivec2 size = textureSize(Sampler0, 0);
                    vec2 corner;
                    ivec2 origin;
                    if (size == ivec2(128)) {
                        corner = vec2(uv.x > 0.5 ? 1.0 : 0.0, uv.y > 0.5 ? 1.0 : 0.0);
                        origin = ivec2(0);
                    } else {
                        // the canvas may live in an atlas: find its top left texel from the corner
                        corner = doomCorner(vertex);
                        origin = ivec2(round(uv * vec2(size) - corner * 128.0));
                        if (any(lessThan(origin, ivec2(0))) || any(greaterThan(origin + ivec2(128), size))) {
                            return false;
                        }
                    }
                    if (!(%s)) {
                        return false;
                    }
                    int tile = doomDecode(texelFetch(Sampler0, origin + ivec2(8, 0), 0));
                    int tiles = doomDecode(texelFetch(Sampler0, origin + ivec2(9, 0), 0));
                    int pal = doomDecode(texelFetch(Sampler0, origin + ivec2(10, 0), 0));
                    int flags = doomDecode(texelFetch(Sampler0, origin + ivec2(11, 0), 0));
                    if (tiles < 1 || tile < 0 || tile >= tiles || pal < 0 || pal >= %d) {
                        return false;
                    }
                    float width = %d.0 / float(tiles);
                    float aspect = abs(proj[1][1] / proj[0][0]);
                    vec2 scale = vec2(1.0);
                    if ((flags & %d) != 0) {
                        float target = 4.0 / 3.0;
                        if (aspect > target) {
                            scale.x = target / aspect;
                        } else {
                            scale.y = aspect / target;
                        }
                    }
                    float x0 = float(tile) * width;
                    float left = tile == 0 ? -1.0 : (x0 / %d.0 * 2.0 - 1.0) * scale.x;
                    float right = tile == tiles - 1 ? 1.0 : ((x0 + width) / %d.0 * 2.0 - 1.0) * scale.x;
                    vec2 ndc = vec2(corner.x > 0.5 ? right : left, corner.y > 0.5 ? -1.0 : 1.0);
                    coord = vec2((ndc.x / scale.x + 1.0) * 0.5 * %d.0, (1.0 - ndc.y / scale.y) * 0.5 * %d.0);
                    // just past the near plane, whatever the depth convention
                    vec4 near = proj * vec4(0.0, 0.0, -0.06, 1.0);
                    float flip = proj[1][1] < 0.0 ? -1.0 : 1.0;
                    position = vec4(ndc.x, ndc.y * flip, near.z / near.w, 1.0);
                    info = ivec4(tile, tiles, pal, flags);
                    originOut = origin;
                    return true;
                }
                """.formatted(signatureCheck(), DoomPalette.PALETTES, ScreenEncoder.WIDTH, ScreenEncoder.FLAG_LETTERBOX,
                ScreenEncoder.WIDTH, ScreenEncoder.WIDTH, ScreenEncoder.WIDTH, ScreenEncoder.HEIGHT);
    }

    /**
     * {@return the Doom pixel lookup, used by {@link #fragmentShader()} and tests}
     */
    public String fragmentGlsl() {
        return paletteTable() + """

                // Color of the Doom pixel at the given screen coordinate.
                vec4 doomColor(ivec4 info, ivec2 origin, vec2 coord) {
                    ivec2 d = ivec2(floor(coord));
                    if (d.x < 0 || d.x >= %d || d.y < 0 || d.y >= %d) {
                        return vec4(0.0, 0.0, 0.0, 1.0);
                    }
                    int width = %d / info.y;
                    int x0 = info.x * width;
                    d.x = clamp(d.x, x0, x0 + width - 1);
                    int local = d.y * width + d.x - x0 + %d;
                    int symbol = doomDecode(texelFetch(Sampler0, origin + ivec2(local %% 128, local / 128), 0));
                    if (symbol < 0) {
                        return vec4(0.0, 0.0, 0.0, 1.0);
                    }
                    int rgb = DOOM_PALETTE[info.z * %d + symbol];
                    return vec4(float((rgb >> 16) & 255), float((rgb >> 8) & 255), float(rgb & 255), 255.0) / 255.0;
                }
                """.formatted(ScreenEncoder.WIDTH, ScreenEncoder.HEIGHT, ScreenEncoder.WIDTH, ScreenEncoder.HEADER,
                MapColors.COUNT);
    }

    /**
     * {@return {@code assets/minecraft/shaders/core/text.vsh}}
     */
    public String vertexShader() {
        return """
                #version 330
                #extension GL_ARB_separate_shader_objects : require

                #if !defined(IS_GUI) && !defined(IS_SEE_THROUGH)
                #include <minecraft:fog.glsl>
                #include <minecraft:sample_lightmap.glsl>
                #endif

                #include <minecraft:dynamictransforms.glsl>
                #include <minecraft:projection.glsl>

                #if !defined(IS_GUI) && !defined(IS_SEE_THROUGH) && !defined(IS_GRAYSCALE)
                #define DOOM_ENABLED
                #endif

                layout(location = 0) in vec3 Position;
                layout(location = 1) in vec4 Color;
                layout(location = 2) in vec2 UV0;
                #if !defined(IS_GUI) && !defined(IS_SEE_THROUGH)
                layout(location = 3) in ivec2 UV2;
                #endif

                #if !defined(IS_GUI) && !defined(IS_SEE_THROUGH)
                uniform sampler2D Sampler2;
                layout(location = 0) out float sphericalVertexDistance;
                layout(location = 1) out float cylindricalVertexDistance;
                #endif

                layout(location = 2) out vec4 vertexColor;
                layout(location = 3) out vec2 texCoord0;

                #ifdef DOOM_ENABLED
                uniform sampler2D Sampler0;
                layout(location = 4) flat out ivec4 doomInfo;
                layout(location = 5) flat out ivec2 doomOrigin;
                layout(location = 6) out vec2 doomCoord;

                %s
                %s
                #endif

                void main() {
                    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

                #if !defined(IS_GUI) && !defined(IS_SEE_THROUGH)
                    sphericalVertexDistance = fog_spherical_distance(Position);
                    cylindricalVertexDistance = fog_cylindrical_distance(Position);
                    vertexColor = Color * sample_lightmap(Sampler2, UV2);
                #else
                    vertexColor = Color;
                #endif
                    texCoord0 = UV0;

                #ifdef DOOM_ENABLED
                    doomInfo = ivec4(-1);
                    doomOrigin = ivec2(0);
                    doomCoord = vec2(0.0);
                    vec4 doomPosition;
                    ivec4 info;
                    ivec2 origin;
                    vec2 coord;
                    if (doomPlace(UV0, gl_VertexIndex, ProjMat, doomPosition, info, origin, coord)) {
                        gl_Position = doomPosition;
                        doomInfo = info;
                        doomOrigin = origin;
                        doomCoord = coord;
                    }
                #endif
                }
                """.formatted(commonGlsl(), vertexGlsl());
    }

    /**
     * {@return {@code assets/minecraft/shaders/core/text.fsh}}
     */
    public String fragmentShader() {
        return """
                #version 330
                #extension GL_ARB_separate_shader_objects : require

                #if !defined(IS_GUI) && !defined(IS_SEE_THROUGH)
                #include <minecraft:fog.glsl>
                #endif

                #include <minecraft:dynamictransforms.glsl>
                #include <minecraft:oit.glsl>

                #if !defined(IS_GUI) && !defined(IS_SEE_THROUGH) && !defined(IS_GRAYSCALE)
                #define DOOM_ENABLED
                #endif

                uniform sampler2D Sampler0;

                #if !defined(IS_GUI) && !defined(IS_SEE_THROUGH)
                layout(location = 0) in float sphericalVertexDistance;
                layout(location = 1) in float cylindricalVertexDistance;
                #endif

                layout(location = 2) in vec4 vertexColor;
                layout(location = 3) in vec2 texCoord0;

                #ifdef DOOM_ENABLED
                layout(location = 4) flat in ivec4 doomInfo;
                layout(location = 5) flat in ivec2 doomOrigin;
                layout(location = 6) in vec2 doomCoord;

                %s
                %s
                #endif

                #ifndef OIT_ALPHA_ONLY
                layout(location = 0) out vec4 fragColor;
                #endif

                vec4 calculateFinalColor(vec4 color) {
                    #ifdef OIT_ACCUMULATE
                    color = sampleColorForAccumulation(color);
                    #endif

                    #if !defined(IS_SEE_THROUGH) && !defined(IS_GUI)

                    color = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);

                    #ifdef OIT_ACCUMULATE
                    color = sampleColorForAccumulation(color);
                    #endif

                    #endif

                    return color;
                }

                void main() {
                #ifdef DOOM_ENABLED
                    if (doomInfo.x >= 0) {
                        vec4 doom = doomColor(doomInfo, doomOrigin, doomCoord);
                    #ifdef OIT_ALPHA_ONLY
                        executeAlphaOnlyPhase(gl_FragCoord.z, 1.0);
                    #else
                    #ifdef OIT_ACCUMULATE
                        doom = sampleColorForAccumulation(doom);
                    #endif
                        fragColor = doom;
                    #endif
                        return;
                    }
                #endif

                    #ifdef IS_GRAYSCALE
                    vec4 texColor = texture(Sampler0, texCoord0).rrrr;
                    #else
                    vec4 texColor = texture(Sampler0, texCoord0);
                    #endif

                    vec4 color = texColor * vertexColor * ColorModulator;

                    if (color.a < 0.1) {
                        discard;
                    }

                    #ifdef OIT_ALPHA_ONLY
                    executeAlphaOnlyPhase(gl_FragCoord.z, color.a);
                    #else
                    fragColor = calculateFinalColor(color);
                    #endif
                }
                """.formatted(commonGlsl(), fragmentGlsl());
    }
}
