package fr.euphyllia.fidorial.doom;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/**
 * The settings of the plugin, read from {@code doom.properties} in its data folder.
 *
 * @param iwad                the IWAD file name in the data folder, empty to use the first one found
 * @param pwads               extra WAD files loaded after the IWAD
 * @param downloadFreedoom    whether to download Freedoom when no IWAD is found
 * @param packBind            the address the resource pack server listens on
 * @param packPort            the port of the resource pack server, {@code 0} to disable it
 * @param packPublicUrl       the base URL clients download the pack from; empty for
 *                            {@code http://<server address>:<port>}
 * @param packRequired        whether clients must accept the pack
 * @param removePackOnQuit    whether the pack is removed when a player stops playing
 * @param hideHud             whether the pack hides the Minecraft HUD
 * @param music               whether to synthesize the music into the pack
 * @param musicVolume         the music volume, {@code 0} to {@code 1}
 * @param frameInterval       send a frame every that many tics: 1 for 35 fps, 2 for 17.5 fps
 * @param viewSize            the default view size, 3 to 11 (11 hides the status bar)
 * @param mouseSensitivity    how fast the mouse turns, 1 for one Minecraft degree per Doom degree
 * @param cheats              whether cheat codes typed in the chat work
 * @param cheatsInMultiplayer whether cheats other than {@code idclev} also work in multiplayer games
 */
public record DoomConfig(
        String iwad,
        List<String> pwads,
        boolean downloadFreedoom,
        String packBind,
        int packPort,
        String packPublicUrl,
        boolean packRequired,
        boolean removePackOnQuit,
        boolean hideHud,
        boolean music,
        float musicVolume,
        int frameInterval,
        int viewSize,
        double mouseSensitivity,
        boolean cheats,
        boolean cheatsInMultiplayer) {

    private static final String DEFAULTS = """
            # Doom on Fidorial
            #
            # IWAD to play, in this folder (doom.wad, doom2.wad, freedoom1.wad...). Empty: the first .wad found.
            iwad=
            # Extra WADs, comma separated, loaded after the IWAD.
            pwads=
            # Download Freedoom (BSD licence) when no IWAD is found.
            download-freedoom=true

            # The resource pack (shader, sounds) is served over HTTP by the plugin itself.
            pack-bind=0.0.0.0
            pack-port=8166
            # Address clients download the pack from, e.g. http://play.example.com:8166
            # Empty: http://<first LAN address of this machine>:<pack-port>, fine on a LAN only.
            pack-public-url=
            # true kicks players who decline the pack.
            pack-required=false
            remove-pack-on-quit=true
            hide-hud=true
            # Synthesize the music with an OPL2 emulation (makes the pack bigger and takes a moment at start).
            music=true
            music-volume=0.6

            # 1 = 35 frames per second, 2 = 17.5, 3 = 11.7: less bandwidth.
            frame-interval=1
            # 3 to 11; 10 shows the status bar, 11 hides it.
            view-size=10
            mouse-sensitivity=1.0
            cheats=true
            cheats-in-multiplayer=false
            """;

    /**
     * Reads the settings, writing the defaults first if the file does not exist.
     *
     * @param folder the data folder
     * @return the settings
     * @throws IOException if the file cannot be read or written
     */
    public static DoomConfig load(final Path folder) throws IOException {
        Files.createDirectories(folder);
        final Path file = folder.resolve("doom.properties");
        if (!Files.exists(file)) {
            try (final Writer w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                w.write(DEFAULTS);
            }
        }
        final Properties defaults = new Properties();
        defaults.load(new java.io.StringReader(DEFAULTS));
        final Properties p = new Properties(defaults);
        try (final Reader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            p.load(r);
        }
        final List<String> pwads = new ArrayList<>();
        for (final String s : p.getProperty("pwads", "").split(",")) {
            if (!s.isBlank()) {
                pwads.add(s.trim());
            }
        }
        return new DoomConfig(
                p.getProperty("iwad", "").trim(),
                List.copyOf(pwads),
                bool(p, "download-freedoom"),
                p.getProperty("pack-bind", "0.0.0.0").trim(),
                integer(p, "pack-port", 0, 65535),
                p.getProperty("pack-public-url", "").trim(),
                bool(p, "pack-required"),
                bool(p, "remove-pack-on-quit"),
                bool(p, "hide-hud"),
                bool(p, "music"),
                (float) Math.clamp(dbl(p, "music-volume"), 0.0, 1.0),
                integer(p, "frame-interval", 1, 35),
                integer(p, "view-size", 3, 11),
                Math.clamp(dbl(p, "mouse-sensitivity"), 0.05, 20.0),
                bool(p, "cheats"),
                bool(p, "cheats-in-multiplayer"));
    }

    private static boolean bool(final Properties p, final String key) {
        return p.getProperty(key, "false").trim().toLowerCase(Locale.ROOT).equals("true");
    }

    private static int integer(final Properties p, final String key, final int min, final int max) {
        try {
            return Math.clamp(Integer.parseInt(p.getProperty(key).trim()), min, max);
        } catch (final NumberFormatException e) {
            return min;
        }
    }

    private static double dbl(final Properties p, final String key) {
        try {
            return Double.parseDouble(p.getProperty(key).trim());
        } catch (final NumberFormatException e) {
            return 1.0;
        }
    }
}
