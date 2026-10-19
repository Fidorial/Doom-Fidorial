package fr.euphyllia.fidorial.doom;

import fr.euphyllia.fidorial.doom.engine.Wad;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Finds the WADs to play in the data folder, downloading Freedoom when there is none.
 */
final class WadLocator {

    private static final String FREEDOOM_URL =
            "https://github.com/freedoom/freedoom/releases/download/v0.13.0/freedoom-0.13.0.zip";

    private WadLocator() {
    }

    static Wad locate(final DoomConfig config, final Path folder, final ComponentLogger logger)
            throws IOException, InterruptedException {
        Path iwad = config.iwad().isEmpty() ? findIwad(folder) : folder.resolve(config.iwad());
        if (iwad == null || !Files.exists(iwad)) {
            if (!config.downloadFreedoom()) {
                throw new IOException("No IWAD in " + folder + "; put doom.wad, doom2.wad or freedoom1.wad there");
            }
            logger.info("[Doom] No IWAD found, downloading Freedoom from {}", FREEDOOM_URL);
            downloadFreedoom(folder);
            iwad = folder.resolve("freedoom1.wad");
        }
        final List<Path> paths = new ArrayList<>();
        paths.add(iwad);
        for (final String pwad : config.pwads()) {
            paths.add(folder.resolve(pwad));
        }
        logger.info("[Doom] Loading {}", paths);
        return Wad.load(paths);
    }

    private static Path findIwad(final Path folder) throws IOException {
        final List<Path> candidates = new ArrayList<>();
        try (final DirectoryStream<Path> files = Files.newDirectoryStream(folder, "*.{wad,WAD}")) {
            for (final Path file : files) {
                candidates.add(file);
            }
        }
        candidates.sort(null);
        for (final Path file : candidates) {
            try (final InputStream in = Files.newInputStream(file)) {
                final byte[] magic = in.readNBytes(4);
                if (new String(magic, java.nio.charset.StandardCharsets.US_ASCII).equals("IWAD")) {
                    return file;
                }
            }
        }
        return null;
    }

    private static void downloadFreedoom(final Path folder) throws IOException, InterruptedException {
        final HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofSeconds(20))
                .build();
        final Path zip = folder.resolve("freedoom.zip.part");
        final HttpResponse<Path> response = client.send(HttpRequest.newBuilder(URI.create(FREEDOOM_URL)).build(),
                HttpResponse.BodyHandlers.ofFile(zip));
        if (response.statusCode() != 200) {
            Files.deleteIfExists(zip);
            throw new IOException("Freedoom download failed: HTTP " + response.statusCode());
        }
        try (final ZipInputStream in = new ZipInputStream(Files.newInputStream(zip))) {
            ZipEntry entry;
            while ((entry = in.getNextEntry()) != null) {
                final String name = Path.of(entry.getName()).getFileName().toString().toLowerCase(Locale.ROOT);
                if (name.equals("freedoom1.wad") || name.equals("freedoom2.wad") || name.equals("copying.txt")) {
                    Files.copy(in, folder.resolve(name.equals("copying.txt") ? "FREEDOOM-COPYING.txt" : name),
                            StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } finally {
            Files.deleteIfExists(zip);
        }
    }
}
