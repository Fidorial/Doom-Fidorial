package fr.euphyllia.fidorial.doom;

import fr.euphyllia.fidorial.doom.audio.MusicRenderer;
import fr.euphyllia.fidorial.doom.audio.MusicTrack;
import fr.euphyllia.fidorial.doom.display.DoomPalette;
import fr.euphyllia.fidorial.doom.engine.Wad;
import fr.euphyllia.fidorial.doom.engine.render.RenderData;
import fr.euphyllia.fidorial.doom.pack.PackHttpServer;
import fr.euphyllia.fidorial.doom.pack.ResourcePackBuilder;
import fr.euphyllia.fidorial.doom.session.SessionManager;
import fr.fidorial.Server;
import fr.fidorial.event.EventBus;
import fr.fidorial.event.EventPriority;
import fr.fidorial.event.Subscription;
import fr.fidorial.event.player.PlayerChatEvent;
import fr.fidorial.event.player.PlayerDeathEvent;
import fr.fidorial.event.player.PlayerDropItemEvent;
import fr.fidorial.event.player.PlayerInputEvent;
import fr.fidorial.event.player.PlayerItemHeldEvent;
import fr.fidorial.event.player.PlayerMoveEvent;
import fr.fidorial.event.player.PlayerQuitEvent;
import fr.fidorial.event.player.PlayerSignedChatEvent;
import fr.fidorial.event.player.PlayerSwapHandItemsEvent;
import fr.fidorial.event.player.PlayerSwingEvent;
import fr.fidorial.event.player.PlayerUseItemEvent;
import fr.fidorial.plugin.Plugin;
import fr.fidorial.plugin.PluginContext;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

/**
 * Doom, running on the server and drawn on unmodified clients through maps and a core shader.
 */
public final class DoomPlugin implements Plugin {

    /**
     * Bump when the pack content changes for the same WAD, to rebuild cached packs.
     */
    private static final int PACK_REVISION = 2;
    private static final UUID PACK_ID = UUID.fromString("d0e5a1b2-1993-4c0d-9e11-000000000666");
    private final List<Subscription> subscriptions = new ArrayList<>();
    private @Nullable PluginContext context;
    private @Nullable ComponentLogger logger;
    private @Nullable Server server;
    private @Nullable DoomConfig config;
    private volatile @Nullable SessionManager sessions;
    private volatile String status = "Doom is still loading, try again in a moment.";
    private @Nullable PackHttpServer http;
    private @Nullable Thread loader;

    private static ResourcePackBuilder.Pack pack(final DoomConfig cfg, final Path folder, final Wad wad,
                                                 final RenderData res, final DoomPalette palette,
                                                 final ComponentLogger log) throws IOException {
        final String key = cacheKey(cfg, wad);
        final Path cache = folder.resolve("cache");
        final Path zip = cache.resolve("pack-" + key + ".zip");
        final Path sounds = cache.resolve("pack-" + key + ".sounds");
        if (Files.exists(zip) && Files.exists(sounds)) {
            final Properties lengths = new Properties();
            try (final Reader r = Files.newBufferedReader(sounds, StandardCharsets.UTF_8)) {
                lengths.load(r);
            }
            final Map<String, Float> map = new LinkedHashMap<>();
            for (final String name : lengths.stringPropertyNames()) {
                map.put(name, Float.parseFloat(lengths.getProperty(name)));
            }
            final byte[] bytes = Files.readAllBytes(zip);
            return new ResourcePackBuilder.Pack(bytes, sha1(bytes), map);
        }
        log.info("[Doom] Building the resource pack (sounds{})...", cfg.music() ? " and music" : "");
        final Map<String, MusicTrack> music = cfg.music()
                ? MusicRenderer.renderAll(wad, message -> log.info("[Doom] {}", message))
                : Collections.emptyMap();
        final ResourcePackBuilder.Pack pack = ResourcePackBuilder.build(wad, res, palette, cfg.hideHud(), music,
                message -> log.info("[Doom] {}", message));
        Files.createDirectories(cache);
        Files.write(zip, pack.zip());
        final Properties lengths = new Properties();
        pack.soundLengths().forEach((name, seconds) -> lengths.setProperty(name, Float.toString(seconds)));
        try (final Writer w = Files.newBufferedWriter(sounds, StandardCharsets.UTF_8)) {
            lengths.store(w, "sound lengths in seconds");
        }
        return pack;
    }

    private static String cacheKey(final DoomConfig cfg, final Wad wad) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(("rev" + PACK_REVISION + cfg.hideHud() + cfg.music()).getBytes(StandardCharsets.UTF_8));
            for (int lump = 0; lump < wad.numLumps(); lump++) {
                digest.update(wad.lumpName(lump).getBytes(StandardCharsets.US_ASCII));
                digest.update(wad.readLump(lump));
            }
            return HexFormat.of().formatHex(digest.digest()).substring(0, 16);
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String sha1(final byte[] data) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1").digest(data));
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * The first non loopback IPv4 address of this machine, good enough on a LAN.
     */
    private static String guessAddress() {
        try {
            for (final NetworkInterface nif : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!nif.isUp() || nif.isLoopback()) {
                    continue;
                }
                for (final InetAddress address : Collections.list(nif.getInetAddresses())) {
                    if (address instanceof Inet4Address && !address.isLoopbackAddress()) {
                        return address.getHostAddress();
                    }
                }
            }
        } catch (final IOException ignored) {
            // fall back to localhost
        }
        return "127.0.0.1";
    }

    @Override
    public void onLoad(final PluginContext context) {
        this.context = context;
        this.logger = context.logger();
        this.server = context.server();
    }

    Server server() {
        return java.util.Objects.requireNonNull(server);
    }

    @Nullable SessionManager sessions() {
        return sessions;
    }

    String status() {
        return status;
    }

    @Override
    public void onEnable() {
        final PluginContext ctx = java.util.Objects.requireNonNull(context);
        final ComponentLogger log = java.util.Objects.requireNonNull(logger);
        try {
            config = DoomConfig.load(ctx.dataFolder());
        } catch (final IOException e) {
            log.error("[Doom] Cannot read doom.properties", e);
            status = "Doom failed to start: " + e.getMessage();
            return;
        }
        ctx.server().commands().register(ctx.meta(), new DoomCommand(this).create());
        registerEvents(ctx.events());
        final Thread thread = new Thread(this::load, "doom-loader");
        thread.setDaemon(true);
        loader = thread;
        thread.start();
    }

    @Override
    public void onDisable() {
        final SessionManager manager = sessions;
        if (manager != null) {
            manager.shutdown();
        }
        sessions = null;
        for (final Subscription s : subscriptions) {
            s.unsubscribe();
        }
        subscriptions.clear();
        final PackHttpServer h = http;
        if (h != null) {
            h.close();
        }
        final Thread t = loader;
        if (t != null) {
            t.interrupt();
        }
        if (context != null && server != null) {
            server.commands().unregisterNamespace(context.meta());
        }
    }

    private void load() {
        final PluginContext ctx = java.util.Objects.requireNonNull(context);
        final ComponentLogger log = java.util.Objects.requireNonNull(logger);
        final DoomConfig cfg = java.util.Objects.requireNonNull(config);
        try {
            final Path folder = ctx.dataFolder();
            final Wad wad = WadLocator.locate(cfg, folder, log);
            final RenderData res = new RenderData(wad);
            final DoomPalette palette = new DoomPalette(wad.readLump("PLAYPAL"));
            final ResourcePackBuilder.Pack pack = pack(cfg, folder, wad, res, palette, log);
            log.info("[Doom] Resource pack ready: {} KiB, sha1 {}", pack.zip().length / 1024, pack.sha1());

            ResourcePackRequest request = null;
            if (cfg.packPort() > 0) {
                final PackHttpServer server = new PackHttpServer(new InetSocketAddress(cfg.packBind(), cfg.packPort()));
                http = server;
                final String path = server.serve(pack);
                final String base = cfg.packPublicUrl().isEmpty()
                        ? "http://" + guessAddress() + ":" + cfg.packPort()
                        : cfg.packPublicUrl().replaceAll("/+$", "");
                final URI uri = URI.create(base + path);
                log.info("[Doom] Serving the resource pack at {}", uri);
                request = ResourcePackRequest.resourcePackRequest()
                        .packs(ResourcePackInfo.resourcePackInfo(PACK_ID, uri, pack.sha1()))
                        .required(cfg.packRequired())
                        .replace(false)
                        .prompt(Component.text("Doom needs this pack to draw the game and play its sounds."))
                        .build();
            } else {
                Files.write(folder.resolve("doom-pack.zip"), pack.zip());
                log.warn("[Doom] pack-port is 0: the pack was written to doom-pack.zip, players must install it themselves");
            }
            sessions = new SessionManager(server(), log, cfg, wad, res, palette, pack.soundLengths(), request, PACK_ID);
            status = "Doom is ready.";
            log.info("[Doom] Ready. Type /doom in game.");
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (final Exception e) {
            log.error("[Doom] Failed to start", e);
            status = "Doom failed to start: " + e.getMessage();
        }
    }

    private void registerEvents(final EventBus events) {
        subscriptions.add(events.subscribe(PlayerInputEvent.class, e -> {
            final SessionManager m = sessions;
            if (m != null) {
                m.keys(e.player().uuid(), e.input());
            }
        }));
        subscriptions.add(events.subscribe(PlayerMoveEvent.class, e -> {
            final SessionManager m = sessions;
            if (m != null) {
                double delta = e.to().yaw() - e.from().yaw();
                delta = ((delta % 360.0) + 540.0) % 360.0 - 180.0;
                m.turn(e.player().uuid(), delta);
            }
        }));
        subscriptions.add(events.subscribe(PlayerUseItemEvent.class, e -> {
            final SessionManager m = sessions;
            if (m != null && m.use(e.player().uuid())) {
                e.setCancelled(true);
            }
        }));
        subscriptions.add(events.subscribe(PlayerSwingEvent.class, e -> {
            final SessionManager m = sessions;
            if (m != null && m.punch(e.player().uuid())) {
                e.setCancelled(true);
            }
        }));
        subscriptions.add(events.subscribe(PlayerItemHeldEvent.class, e -> {
            final SessionManager m = sessions;
            if (m != null && m.hotbar(e.player().uuid(), e.newSlot())) {
                e.setCancelled(true);
            }
        }));
        subscriptions.add(events.subscribe(PlayerSwapHandItemsEvent.class, e -> {
            final SessionManager m = sessions;
            if (m != null && m.toggleAutoMap(e.player().uuid())) {
                e.setCancelled(true);
            }
        }));
        subscriptions.add(events.subscribe(PlayerDropItemEvent.class, e -> {
            final SessionManager m = sessions;
            if (m != null) {
                m.toggleRun(e.player().uuid());
            }
        }));
        subscriptions.add(events.subscribe(PlayerChatEvent.class, EventPriority.HIGH, this::chat));
        subscriptions.add(events.subscribe(PlayerSignedChatEvent.class, EventPriority.HIGH, this::chat));
        subscriptions.add(events.subscribe(PlayerQuitEvent.class, e -> {
            final SessionManager m = sessions;
            if (m != null) {
                m.disconnected(e.player());
            }
        }));
        subscriptions.add(events.subscribe(PlayerDeathEvent.class, e -> {
            final SessionManager m = sessions;
            if (m != null) {
                m.leave(e.player().uuid());
            }
        }));
    }

    private void chat(final PlayerChatEvent e) {
        final SessionManager m = sessions;
        if (m == null || e.isCancelled()) {
            return;
        }
        final String text = PlainTextComponentSerializer.plainText().serialize(e.message());
        if (m.cheat(e.player().uuid(), text)) {
            e.setCancelled(true);
        }
    }
}
