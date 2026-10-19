// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine.render;

import fr.euphyllia.fidorial.doom.engine.Wad;
import fr.euphyllia.fidorial.doom.engine.info.SpriteNum;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Textures, flats, sprites, colormaps and palettes of a WAD ({@code r_data.c}, sprite part of {@code r_things.c}).
 *
 * <p>Built once per WAD and immutable afterwards, so every game and every viewer shares it.</p>
 */
public final class RenderData {

    public static final int NUMCOLORMAPS = 32;
    public final byte[][] palettes;
    public final byte[] colormaps;
    public final Texture[] textures;
    public final byte[][] flats;
    public final Patch[] spritePatches;
    public final int[] spritewidth;
    public final int[] spriteoffset;
    public final int[] spritetopoffset;
    public final SpriteDef[] sprites;
    /**
     * Player color remaps, 3 tables of 256 ({@code translationtables}).
     */
    public final byte[][] translations = new byte[3][256];
    private final Wad wad;
    private final Map<String, Integer> textureByName = new HashMap<>();
    private final Map<String, Integer> flatByName = new HashMap<>();
    private final Map<String, Patch> patchCache = new ConcurrentHashMap<>();
    public RenderData(final Wad wad) {
        this.wad = wad;
        this.palettes = loadPalettes(wad);
        this.colormaps = loadColormaps(wad);
        this.textures = loadTextures(wad);
        for (int i = textures.length - 1; i >= 0; i--) {
            textureByName.put(textures[i].name, i);
        }
        final List<Integer> flatLumps = markerRange(wad, "F_START", "F_END", "FF_START", "FF_END");
        this.flats = new byte[flatLumps.size()][];
        for (int i = 0; i < flatLumps.size(); i++) {
            final int lump = flatLumps.get(i);
            final byte[] raw = wad.readLump(lump);
            final byte[] flat = new byte[4096];
            System.arraycopy(raw, 0, flat, 0, Math.min(4096, raw.length));
            flats[i] = flat;
            flatByName.put(wad.lumpName(lump), i);
        }
        final List<Integer> spriteLumps = markerRange(wad, "S_START", "S_END", "SS_START", "SS_END");
        final int n = spriteLumps.size();
        this.spritePatches = new Patch[n];
        this.spritewidth = new int[n];
        this.spriteoffset = new int[n];
        this.spritetopoffset = new int[n];
        final Map<String, Integer> spriteIndexByName = new HashMap<>();
        for (int i = 0; i < n; i++) {
            final int lump = spriteLumps.get(i);
            final Patch p = Patch.decode(wad.readLump(lump));
            spritePatches[i] = p;
            spritewidth[i] = p.width << 16;
            spriteoffset[i] = p.leftoffset << 16;
            spritetopoffset[i] = p.topoffset << 16;
            spriteIndexByName.put(wad.lumpName(lump), i);
        }
        this.sprites = initSpriteDefs(spriteLumps, spriteIndexByName);
        for (int i = 0; i < 256; i++) {
            if (i >= 0x70 && i <= 0x7f) {
                translations[0][i] = (byte) (0x60 + (i & 0xf));
                translations[1][i] = (byte) (0x40 + (i & 0xf));
                translations[2][i] = (byte) (0x20 + (i & 0xf));
            } else {
                translations[0][i] = translations[1][i] = translations[2][i] = (byte) i;
            }
        }
    }

    private static byte[][] loadPalettes(final Wad wad) {
        final byte[] raw = wad.readLump("PLAYPAL");
        final int count = raw.length / 768;
        final byte[][] pals = new byte[Math.max(14, count)][];
        for (int i = 0; i < pals.length; i++) {
            pals[i] = new byte[768];
            System.arraycopy(raw, (i < count ? i : 0) * 768, pals[i], 0, 768);
        }
        return pals;
    }

    private static byte[] loadColormaps(final Wad wad) {
        final byte[] raw = wad.readLump("COLORMAP");
        final byte[] maps = new byte[34 * 256];
        System.arraycopy(raw, 0, maps, 0, Math.min(raw.length, maps.length));
        return maps;
    }

    private static List<Integer> markerRange(final Wad wad, final String start, final String end,
                                             final String altStart, final String altEnd) {
        final List<Integer> lumps = new ArrayList<>();
        final Map<String, Integer> seen = new HashMap<>();
        boolean inside = false;
        for (int i = 0; i < wad.numLumps(); i++) {
            final String name = wad.lumpName(i);
            if (name.equals(start) || name.equals(altStart)) {
                inside = true;
                continue;
            }
            if (name.equals(end) || name.equals(altEnd)) {
                inside = false;
                continue;
            }
            if (!inside || wad.lumpLength(i) == 0) {
                continue;
            }
            final Integer previous = seen.get(name);
            if (previous != null) {
                lumps.set(previous, i);
            } else {
                seen.put(name, lumps.size());
                lumps.add(i);
            }
        }
        return lumps;
    }

    private static Texture[] loadTextures(final Wad wad) {
        final byte[] pnames = wad.readLump("PNAMES");
        final ByteBuffer pn = ByteBuffer.wrap(pnames).order(ByteOrder.LITTLE_ENDIAN);
        final int nummappatches = pn.getInt(0);
        final Patch[] patchLookup = new Patch[nummappatches];
        final Map<Integer, Patch> decoded = new HashMap<>();
        for (int i = 0; i < nummappatches; i++) {
            final String name = Wad.readName(pnames, 4 + i * 8);
            final int lump = wad.checkNumForName(name);
            if (lump >= 0) {
                patchLookup[i] = decoded.computeIfAbsent(lump, l -> Patch.decode(wad.readLump(l)));
            }
        }
        final List<Texture> out = new ArrayList<>();
        for (final String lumpName : new String[]{"TEXTURE1", "TEXTURE2"}) {
            final byte[] maptex = wad.readLumpOrNull(lumpName);
            if (maptex == null) {
                continue;
            }
            final ByteBuffer mt = ByteBuffer.wrap(maptex).order(ByteOrder.LITTLE_ENDIAN);
            final int numtextures = mt.getInt(0);
            for (int i = 0; i < numtextures; i++) {
                final int offset = mt.getInt(4 + i * 4);
                final String name = Wad.readName(maptex, offset);
                final int width = mt.getShort(offset + 12);
                final int height = mt.getShort(offset + 14);
                final int patchcount = mt.getShort(offset + 20);
                final byte[][] composite = new byte[Math.max(1, width)][Math.max(1, height)];
                final boolean[][] covered = new boolean[Math.max(1, width)][Math.max(1, height)];
                for (int p = 0; p < patchcount; p++) {
                    final int base = offset + 22 + p * 10;
                    final int originx = mt.getShort(base);
                    final int originy = mt.getShort(base + 2);
                    final int patchIndex = mt.getShort(base + 4);
                    if (patchIndex < 0 || patchIndex >= patchLookup.length || patchLookup[patchIndex] == null) {
                        continue;
                    }
                    final Patch patch = patchLookup[patchIndex];
                    final int x1 = originx;
                    final int x2 = Math.min(x1 + patch.width, width);
                    for (int x = Math.max(0, x1); x < x2; x++) {
                        final Patch.Column col = patch.columns[x - x1];
                        for (int post = 0; post < col.tops().length; post++) {
                            final byte[] px = col.posts()[post];
                            int position = originy + col.tops()[post];
                            int count = px.length;
                            int src = 0;
                            if (position < 0) {
                                src = -position;
                                count += position;
                                position = 0;
                            }
                            if (position + count > height) {
                                count = height - position;
                            }
                            for (int k = 0; k < count; k++) {
                                composite[x][position + k] = px[src + k];
                                covered[x][position + k] = true;
                            }
                        }
                    }
                }
                final Patch.Column[] masked = new Patch.Column[Math.max(1, width)];
                for (int x = 0; x < masked.length; x++) {
                    masked[x] = toPosts(composite[x], covered[x]);
                }
                out.add(new Texture(name, width, height, composite, masked));
            }
        }
        if (out.isEmpty()) {
            out.add(new Texture("-", 1, 1, new byte[1][1], new Patch.Column[]{new Patch.Column(new int[0], new byte[0][])}));
        }
        return out.toArray(Texture[]::new);
    }

    private static Patch.Column toPosts(final byte[] column, final boolean[] covered) {
        final List<Integer> tops = new ArrayList<>();
        final List<byte[]> posts = new ArrayList<>();
        int y = 0;
        while (y < column.length) {
            if (!covered[y]) {
                y++;
                continue;
            }
            final int start = y;
            while (y < column.length && covered[y]) {
                y++;
            }
            final byte[] run = new byte[y - start];
            System.arraycopy(column, start, run, 0, run.length);
            tops.add(start);
            posts.add(run);
        }
        final int[] t = new int[tops.size()];
        for (int i = 0; i < t.length; i++) {
            t[i] = tops.get(i);
        }
        return new Patch.Column(t, posts.toArray(byte[][]::new));
    }

    private static int firstValid(final int[] l) {
        for (final int v : l) {
            if (v != -1) {
                return v;
            }
        }
        return 0;
    }

    private static int install(final int[][] lumps, final boolean[][] flips, final int[] rotate, final int lump,
                               final int frame, int rotation, final boolean flipped) {
        if (frame < 0 || frame >= 29 || rotation < 0 || rotation > 8) {
            return -1;
        }
        if (rotation == 0) {
            rotate[frame] = 0;
            for (int r = 0; r < 8; r++) {
                lumps[frame][r] = lump;
                flips[frame][r] = flipped;
            }
            return frame;
        }
        rotate[frame] = 1;
        rotation--;
        lumps[frame][rotation] = lump;
        flips[frame][rotation] = flipped;
        return frame;
    }

    public Wad wad() {
        return wad;
    }

    private SpriteDef[] initSpriteDefs(final List<Integer> spriteLumps, final Map<String, Integer> indexByName) {
        final SpriteDef[] defs = new SpriteDef[SpriteNum.NAMES.length];
        for (int s = 0; s < defs.length; s++) {
            final String prefix = SpriteNum.NAMES[s];
            final int[][] lumps = new int[29][8];
            final boolean[][] flips = new boolean[29][8];
            final int[] rotate = new int[29];
            for (int f = 0; f < 29; f++) {
                java.util.Arrays.fill(lumps[f], -1);
                rotate[f] = -1;
            }
            int maxframe = -1;
            for (int i = 0; i < spriteLumps.size(); i++) {
                final String name = wad.lumpName(spriteLumps.get(i));
                if (name.length() < 6 || !name.startsWith(prefix)) {
                    continue;
                }
                maxframe = Math.max(maxframe, install(lumps, flips, rotate, i, name.charAt(4) - 'A', name.charAt(5) - '0', false));
                if (name.length() >= 8) {
                    maxframe = Math.max(maxframe, install(lumps, flips, rotate, i, name.charAt(6) - 'A', name.charAt(7) - '0', true));
                }
            }
            if (maxframe == -1) {
                defs[s] = new SpriteDef(new SpriteFrame[0]);
                continue;
            }
            final SpriteFrame[] frames = new SpriteFrame[maxframe + 1];
            for (int f = 0; f <= maxframe; f++) {
                final int[] l = lumps[f];
                if (rotate[f] == 1) {
                    for (int r = 0; r < 8; r++) {
                        if (l[r] == -1) {
                            l[r] = firstValid(l);
                        }
                    }
                } else if (rotate[f] == -1) {
                    java.util.Arrays.fill(l, 0);
                }
                frames[f] = new SpriteFrame(rotate[f] == 1, l, flips[f]);
            }
            defs[s] = new SpriteDef(frames);
        }
        return defs;
    }

    public int checkTextureNumForName(final String name) {
        if (name.isEmpty() || name.charAt(0) == '-') {
            return 0;
        }
        final Integer i = textureByName.get(name.toUpperCase(Locale.ROOT));
        return i == null ? -1 : i;
    }

    /**
     * {@code R_TextureNumForName}, falling back to the null texture when the name is unknown.
     */
    public int textureNumForName(final String name) {
        final int i = checkTextureNumForName(name);
        return i == -1 ? 0 : i;
    }

    public int checkFlatNumForName(final String name) {
        final Integer i = flatByName.get(name.toUpperCase(Locale.ROOT));
        return i == null ? -1 : i;
    }

    /**
     * {@code R_FlatNumForName}, falling back to the first flat when the name is unknown.
     */
    public int flatNumForName(final String name) {
        final int i = checkFlatNumForName(name);
        return i == -1 ? 0 : i;
    }

    /**
     * {@return a decoded graphic lump such as {@code STBAR}, cached}
     *
     * @param name the lump name
     */
    public Patch patch(final String name) {
        return patchCache.computeIfAbsent(name.toUpperCase(Locale.ROOT), n -> {
            final byte[] data = wad.readLumpOrNull(n);
            return data == null ? Patch.empty() : Patch.decode(data);
        });
    }

    public boolean hasLump(final String name) {
        return wad.checkNumForName(name) >= 0;
    }

    /**
     * A sprite frame: one lump per rotation ({@code spriteframe_t}).
     */
    public record SpriteFrame(boolean rotate, int[] lump, boolean[] flip) {
    }

    /**
     * All the frames of a sprite ({@code spritedef_t}).
     */
    public record SpriteDef(SpriteFrame[] frames) {
    }
}
