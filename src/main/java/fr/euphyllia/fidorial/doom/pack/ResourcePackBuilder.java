package fr.euphyllia.fidorial.doom.pack;

import fr.euphyllia.fidorial.doom.audio.DmxSound;
import fr.euphyllia.fidorial.doom.audio.MusicTrack;
import fr.euphyllia.fidorial.doom.audio.VorbisEncoder;
import fr.euphyllia.fidorial.doom.display.DoomPalette;
import fr.euphyllia.fidorial.doom.display.ShaderSource;
import fr.euphyllia.fidorial.doom.engine.Sfx;
import fr.euphyllia.fidorial.doom.engine.Wad;
import fr.euphyllia.fidorial.doom.engine.render.Patch;
import fr.euphyllia.fidorial.doom.engine.render.RenderData;
import fr.euphyllia.fidorial.doom.engine.render.Video;
import org.jspecify.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Builds the resource pack a client needs to see and hear Doom: the {@code text} core shader, an
 * invisible item model, transparent HUD sprites and every sound of the WAD encoded to Ogg Vorbis.
 *
 * <p>The zip is deterministic, so the same WAD always gives the same SHA-1.</p>
 */
public final class ResourcePackBuilder {

    /**
     * The namespace of the sounds and the invisible item.
     */
    public static final String NAMESPACE = "doom";
    /**
     * The item model that draws nothing.
     */
    public static final String BLANK_MODEL = NAMESPACE + ":blank";

    private static final int MIN_FORMAT_MAJOR = 100;
    private static final int MIN_FORMAT_MINOR = 0;
    /**
     * Any later format: the pack only replaces the text shader and adds sounds.
     */
    private static final int MAX_FORMAT_MAJOR = 1000;

    /**
     * HUD sprites made transparent while playing.
     */
    private static final String[] HUD_SPRITES = {
            "hotbar", "hotbar_selection", "hotbar_offhand_left", "hotbar_offhand_right",
            "hotbar_attack_indicator_background", "hotbar_attack_indicator_progress",
            "crosshair", "crosshair_attack_indicator_background", "crosshair_attack_indicator_full",
            "crosshair_attack_indicator_progress", "experience_bar_background", "experience_bar_progress",
            "food_empty", "food_empty_hunger", "food_full", "food_full_hunger", "food_half", "food_half_hunger",
            "armor_empty", "armor_full", "armor_half", "air", "air_bursting", "air_empty",
            "jump_bar_background", "jump_bar_cooldown", "jump_bar_progress", "locator_bar_background",
    };
    private static final String[] HEART_KINDS = {"container", "full", "half", "hardcore_full", "hardcore_half",
            "poisoned_full", "poisoned_half", "poisoned_hardcore_full", "poisoned_hardcore_half", "withered_full",
            "withered_half", "withered_hardcore_full", "withered_hardcore_half", "absorbing_full", "absorbing_half",
            "absorbing_hardcore_full", "absorbing_hardcore_half", "frozen_full", "frozen_half", "frozen_hardcore_full",
            "frozen_hardcore_half", "container_hardcore", "vehicle_container", "vehicle_full", "vehicle_half"};

    private ResourcePackBuilder() {
    }

    /**
     * Builds the pack.
     *
     * @param wad      the loaded WADs
     * @param res      their render data, for the pack icon
     * @param palette  the palette mapping shared with the encoder
     * @param hideHud  whether to make the HUD transparent
     * @param music    music tracks already rendered, keyed by sound event ({@code music.e1m1}), or empty
     * @param progress receives a line per step
     * @return the pack
     */
    public static Pack build(final Wad wad, final @Nullable RenderData res, final DoomPalette palette,
                             final boolean hideHud, final Map<String, MusicTrack> music,
                             final Consumer<String> progress) {
        final Map<String, byte[]> files = new LinkedHashMap<>();
        files.put("pack.mcmeta", ("""
                {
                  "pack": {
                    "description": "Doom on Fidorial",
                    "min_format": [%d, %d],
                    "max_format": %d
                  }
                }
                """.formatted(MIN_FORMAT_MAJOR, MIN_FORMAT_MINOR, MAX_FORMAT_MAJOR))
                .getBytes(StandardCharsets.UTF_8));
        final byte[] icon = icon(wad, res);
        if (icon != null) {
            files.put("pack.png", icon);
        }
        final ShaderSource shader = new ShaderSource(palette);
        files.put("assets/minecraft/shaders/core/text.vsh", shader.vertexShader().getBytes(StandardCharsets.UTF_8));
        files.put("assets/minecraft/shaders/core/text.fsh", shader.fragmentShader().getBytes(StandardCharsets.UTF_8));
        files.put("assets/" + NAMESPACE + "/items/blank.json",
                "{\"model\":{\"type\":\"minecraft:empty\"}}\n".getBytes(StandardCharsets.UTF_8));
        if (hideHud) {
            final byte[] transparent = transparentPng();
            for (final String sprite : HUD_SPRITES) {
                files.put("assets/minecraft/textures/gui/sprites/hud/" + sprite + ".png", transparent);
            }
            for (final String heart : HEART_KINDS) {
                files.put("assets/minecraft/textures/gui/sprites/hud/heart/" + heart + ".png", transparent);
                files.put("assets/minecraft/textures/gui/sprites/hud/heart/" + heart + "_blinking.png", transparent);
            }
        }
        final StringBuilder sounds = new StringBuilder("{\n");
        final Map<String, Float> lengths = new LinkedHashMap<>();
        int encoded = 0;
        for (int sfx = 1; sfx < Sfx.NAME.length; sfx++) {
            final String name = Sfx.NAME[sfx];
            final byte[] lump = wad.readLumpOrNull("DS" + name.toUpperCase(Locale.ROOT));
            if (lump == null) {
                continue;
            }
            final DmxSound sound = DmxSound.read(lump);
            if (sound == null) {
                continue;
            }
            files.put("assets/" + NAMESPACE + "/sounds/sfx/" + name + ".ogg",
                    VorbisEncoder.encode(sound.samples(), sound.sampleRate()));
            lengths.put("sfx." + name, sound.samples().length / (float) sound.sampleRate());
            appendSound(sounds, "sfx." + name, "sfx/" + name, false);
            encoded++;
        }
        progress.accept("encoded " + encoded + " sounds");
        for (final Map.Entry<String, MusicTrack> track : music.entrySet()) {
            final String path = track.getKey().replace('.', '/');
            files.put("assets/" + NAMESPACE + "/sounds/" + path + ".ogg", track.getValue().ogg());
            lengths.put(track.getKey(), track.getValue().seconds());
            appendSound(sounds, track.getKey(), path, true);
        }
        if (sounds.charAt(sounds.length() - 2) == ',') {
            sounds.setLength(sounds.length() - 2);
            sounds.append('\n');
        }
        sounds.append("}\n");
        files.put("assets/" + NAMESPACE + "/sounds.json", sounds.toString().getBytes(StandardCharsets.UTF_8));
        final byte[] zip = zip(files);
        return new Pack(zip, sha1(zip), lengths);
    }

    private static void appendSound(final StringBuilder json, final String event, final String path,
                                    final boolean stream) {
        json.append("  \"").append(event).append("\": {\"sounds\": [{\"name\": \"").append(NAMESPACE).append(':')
                .append(path).append('"');
        if (stream) {
            json.append(", \"stream\": true");
        }
        json.append("}]},\n");
    }

    private static byte @Nullable [] icon(final Wad wad, final @Nullable RenderData res) {
        if (res == null) {
            return null;
        }
        try {
            final byte[] screen = new byte[320 * 200];
            final byte[] titlepic = wad.readLumpOrNull("TITLEPIC");
            if (titlepic == null) {
                return null;
            }
            new Video().drawPatch(screen, 0, 0, Patch.decode(titlepic));
            final byte[] pal = res.palettes[0];
            final BufferedImage image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < 128; y++) {
                for (int x = 0; x < 128; x++) {
                    // centre crop of the 4:3 picture
                    final int sx = 40 + x * 240 / 128;
                    final int sy = y * 200 / 128;
                    final int c = screen[sy * 320 + sx] & 0xff;
                    image.setRGB(x, y, (pal[c * 3] & 0xff) << 16 | (pal[c * 3 + 1] & 0xff) << 8 | (pal[c * 3 + 2] & 0xff));
                }
            }
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "png", out);
            return out.toByteArray();
        } catch (final IOException | RuntimeException e) {
            return null;
        }
    }

    private static byte[] transparentPng() {
        try {
            final BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "png", out);
            return out.toByteArray();
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static byte[] zip(final Map<String, byte[]> files) {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (final ZipOutputStream zip = new ZipOutputStream(bytes)) {
            for (final Map.Entry<String, byte[]> file : files.entrySet()) {
                final ZipEntry entry = new ZipEntry(file.getKey());
                // fixed time, so the hash only depends on the content
                entry.setTime(315532800000L);
                final boolean stored = file.getKey().endsWith(".ogg") || file.getKey().endsWith(".png");
                if (stored) {
                    final CRC32 crc = new CRC32();
                    crc.update(file.getValue());
                    entry.setMethod(ZipEntry.STORED);
                    entry.setSize(file.getValue().length);
                    entry.setCompressedSize(file.getValue().length);
                    entry.setCrc(crc.getValue());
                }
                zip.putNextEntry(entry);
                zip.write(file.getValue());
                zip.closeEntry();
            }
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
        return bytes.toByteArray();
    }

    private static String sha1(final byte[] data) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1").digest(data));
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * The built pack.
     */
    public record Pack(byte[] zip, String sha1, Map<String, Float> soundLengths) {
    }
}
