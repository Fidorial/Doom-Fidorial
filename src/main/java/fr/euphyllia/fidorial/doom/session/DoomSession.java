package fr.euphyllia.fidorial.doom.session;

import fr.euphyllia.fidorial.doom.display.ScreenEncoder;
import fr.euphyllia.fidorial.doom.engine.Angles;
import fr.euphyllia.fidorial.doom.engine.Defs;
import fr.euphyllia.fidorial.doom.engine.DegenMobj;
import fr.euphyllia.fidorial.doom.engine.Game;
import fr.euphyllia.fidorial.doom.engine.GameListener;
import fr.euphyllia.fidorial.doom.engine.Mobj;
import fr.euphyllia.fidorial.doom.engine.Music;
import fr.euphyllia.fidorial.doom.engine.Sfx;
import fr.euphyllia.fidorial.doom.engine.play.MapUtil;
import fr.euphyllia.fidorial.doom.engine.ui.Cheats;
import fr.euphyllia.fidorial.doom.engine.ui.ViewScreen;
import fr.euphyllia.fidorial.doom.pack.ResourcePackBuilder;
import fr.fidorial.entity.player.Player;
import fr.fidorial.math.Location;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.SoundStop;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static fr.euphyllia.fidorial.doom.engine.Defs.MAXPLAYERS;
import static fr.euphyllia.fidorial.doom.engine.Defs.PST_REBORN;
import static fr.euphyllia.fidorial.doom.engine.Fixed.FRACUNIT;

/**
 * One running Doom game, ticked at 35 Hz on its own thread, with up to four Minecraft players.
 */
public final class DoomSession implements GameListener {

    private static final long TIC_NANOS = 1_000_000_000L / Defs.TICRATE;
    private static final int S_CLIPPING_DIST = 1200 * FRACUNIT;
    private static final int S_CLOSE_DIST = 160 * FRACUNIT;
    private final SessionManager manager;
    private final int id;
    private final Mode mode;
    private final Game game;
    private final Cheats cheats;
    private final DoomPlayer[] players = new DoomPlayer[MAXPLAYERS];
    private final Queue<Runnable> tasks = new ConcurrentLinkedQueue<>();
    private final ScheduledExecutorService thread;
    private volatile boolean running = true;
    private int frame;
    private @Nullable Key music;
    private long musicEnd;
    private boolean musicLooping;
    DoomSession(final SessionManager manager, final int id, final Mode mode, final int skill, final int episode,
                final int map) {
        this.manager = manager;
        this.id = id;
        this.mode = mode;
        this.game = manager.newGame();
        this.cheats = new Cheats(game);
        game.setListener(this);
        game.netgame = mode != Mode.SOLO;
        game.deathmatch = mode == Mode.DEATHMATCH ? 1 : 0;
        // players join afterwards, each spawning on their first tic
        game.G_InitNew(skill, episode, map);
        thread = Executors.newSingleThreadScheduledExecutor(r -> {
            final Thread t = new Thread(r, "doom-session-" + id);
            t.setDaemon(true);
            return t;
        });
        thread.scheduleAtFixedRate(this::tick, TIC_NANOS, TIC_NANOS, TimeUnit.NANOSECONDS);
    }

    /**
     * Stops the music on a client.
     */
    static void stopMusic(final Player player) {
        player.stopSound(SoundStop.source(Sound.Source.RECORD));
    }

    public int id() {
        return id;
    }

    public Mode mode() {
        return mode;
    }

    SessionManager manager() {
        return manager;
    }

    /**
     * Runs something on the session thread, between two tics.
     */
    void submit(final Runnable task) {
        tasks.add(task);
    }

    /**
     * {@return the players in the session}
     */
    public synchronized List<DoomPlayer> players() {
        final List<DoomPlayer> list = new ArrayList<>();
        for (final DoomPlayer p : players) {
            if (p != null) {
                list.add(p);
            }
        }
        return list;
    }

    /**
     * Adds a player to a free slot; they spawn on the next tic.
     *
     * @return the new Doom player, or null when the session is full
     */
    synchronized @Nullable DoomPlayer add(final Player player) {
        final int max = mode == Mode.SOLO ? 1 : MAXPLAYERS;
        for (int slot = 0; slot < max; slot++) {
            if (players[slot] == null) {
                final ViewScreen view = new ViewScreen(game, game.players[slot], manager.font());
                view.setViewSize(manager.config().viewSize(), 0);
                final DoomPlayer doomPlayer = new DoomPlayer(player, slot, this, view,
                        new ScreenEncoder(manager.palette()));
                players[slot] = doomPlayer;
                final int s = slot;
                submit(() -> {
                    game.playeringame[s] = true;
                    game.players[s].playerstate = PST_REBORN;
                    if (music != null) {
                        playMusic(doomPlayer, music);
                    }
                });
                return doomPlayer;
            }
        }
        return null;
    }

    /**
     * Removes a player; the session ends with its last player.
     */
    synchronized void remove(final DoomPlayer player) {
        if (players[player.slot()] != player) {
            return;
        }
        players[player.slot()] = null;
        final int s = player.slot();
        submit(() -> {
            game.playeringame[s] = false;
            final Mobj mo = game.players[s].mo;
            if (mo != null) {
                game.mobjs.P_RemoveMobj(mo);
                game.players[s].mo = null;
            }
        });
        if (players().isEmpty()) {
            stop();
        }
    }

    void stop() {
        running = false;
        thread.shutdown();
        manager.forget(this);
    }

    /**
     * Applies a cheat code for a player.
     *
     * @return false when the code is not a cheat
     */
    boolean cheat(final DoomPlayer player, final String code) {
        final String c = code.trim().toLowerCase(java.util.Locale.ROOT);
        if (!c.startsWith("id")) {
            return false;
        }
        submit(() -> {
            if (!cheats.apply(game.players[player.slot()], c, player.view().autoMap(), manager.config().cheatsInMultiplayer())) {
                player.view().headsUp().post("Unknown cheat: " + c);
            }
        });
        return true;
    }

    private void tick() {
        if (!running) {
            return;
        }
        try {
            Runnable task;
            while ((task = tasks.poll()) != null) {
                task.run();
            }
            final DoomPlayer[] snapshot;
            synchronized (this) {
                snapshot = players.clone();
            }
            final boolean commercial = game.gamemode == Defs.commercial;
            for (int slot = 0; slot < MAXPLAYERS; slot++) {
                final DoomPlayer p = snapshot[slot];
                if (p != null && game.playeringame[slot]) {
                    p.input().build(game.netcmds[slot], game.players[slot], manager.config().mouseSensitivity(), commercial);
                } else {
                    game.netcmds[slot].clear();
                }
            }
            if (anyPlaying()) {
                game.G_Ticker();
            }
            for (final DoomPlayer p : snapshot) {
                if (p != null) {
                    p.view().ticker();
                }
            }
            frame++;
            if (frame % manager.config().frameInterval() == 0) {
                for (final DoomPlayer p : snapshot) {
                    if (p != null) {
                        p.sendFrame();
                    }
                }
            }
            loopMusic(snapshot);
        } catch (final Throwable t) {
            manager.logger().error("[Doom] session {} crashed", id, t);
            running = false;
            manager.crashed(this);
        }
    }

    private boolean anyPlaying() {
        for (int i = 0; i < MAXPLAYERS; i++) {
            if (game.playeringame[i]) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void startSound(final @Nullable DegenMobj origin, final int sfx, final fr.euphyllia.fidorial.doom.engine.@Nullable Player onlyFor) {
        int id = sfx;
        float pitch = 1f;
        float volume = 1f;
        if (Sfx.LINK[sfx] >= 0) {
            id = Sfx.LINK[sfx];
            pitch = Sfx.PITCH[sfx] > 0 ? Sfx.PITCH[sfx] / 128f : 1f;
            volume = Sfx.VOLUME[sfx] >= 0 ? (Sfx.VOLUME[sfx] + 128) / 255f : 1f;
        }
        final String event = "sfx." + Sfx.NAME[id];
        if (!manager.hasSound(event)) {
            return;
        }
        final Key key = Key.key(ResourcePackBuilder.NAMESPACE, event);
        for (final DoomPlayer p : players()) {
            final fr.euphyllia.fidorial.doom.engine.Player listener = game.players[p.slot()];
            if (onlyFor != null && onlyFor != listener) {
                continue;
            }
            final Player mc = p.online();
            if (mc == null) {
                continue;
            }
            final Location eye = mc.location().with(mc.location().x(), mc.location().y() + 1.62, mc.location().z());
            final Mobj mo = listener.mo;
            if (origin == null || mo == null || origin == mo) {
                mc.playSound(Sound.sound(key, Sound.Source.MASTER, volume, pitch), eye.x(), eye.y(), eye.z());
                continue;
            }
            final int dist = MapUtil.P_AproxDistance(mo.x - origin.x, mo.y - origin.y);
            if (dist > S_CLIPPING_DIST) {
                continue;
            }
            final float attenuation = dist < S_CLOSE_DIST ? 1f
                    : (float) (S_CLIPPING_DIST - dist) / (S_CLIPPING_DIST - S_CLOSE_DIST);
            // angle of the sound relative to where the Doom player looks, counter clockwise
            final int relative = Angles.pointToAngle2(mo.x, mo.y, origin.x, origin.y) - mo.angle;
            final double relativeDegrees = (relative & 0xffffffffL) * 360.0 / 4294967296.0;
            // the Minecraft camera looks where the Doom player looks; its yaw grows clockwise
            final double yaw = Math.toRadians(mc.location().yaw() - relativeDegrees);
            final double x = eye.x() - Math.sin(yaw) * 2.0;
            final double z = eye.z() + Math.cos(yaw) * 2.0;
            mc.playSound(Sound.sound(key, Sound.Source.MASTER, volume * attenuation, pitch), x, eye.y(), z);
        }
    }

    @Override
    public void changeMusic(final int musicnum, final boolean looping) {
        final String event = "music." + Music.NAME[musicnum];
        if (!manager.hasSound(event)) {
            return;
        }
        final Key key = Key.key(ResourcePackBuilder.NAMESPACE, event);
        if (key.equals(music)) {
            return;
        }
        music = key;
        musicLooping = looping;
        for (final DoomPlayer p : players()) {
            playMusic(p, key);
        }
        musicEnd = System.nanoTime() + (long) (manager.soundLength(event) * 1e9);
    }

    private void playMusic(final DoomPlayer p, final Key key) {
        final Player mc = p.online();
        if (mc == null) {
            return;
        }
        mc.stopSound(SoundStop.source(Sound.Source.RECORD));
        mc.playSound(Sound.sound(key, Sound.Source.RECORD, manager.config().musicVolume(), 1f), Sound.Emitter.self());
    }

    private void loopMusic(final DoomPlayer[] snapshot) {
        final Key key = music;
        if (key == null || !musicLooping || System.nanoTime() < musicEnd) {
            return;
        }
        for (final DoomPlayer p : snapshot) {
            if (p != null) {
                playMusic(p, key);
            }
        }
        musicEnd = System.nanoTime() + (long) (manager.soundLength(key.value()) * 1e9);
    }

    @Override
    public void warn(final String message) {
        manager.logger().warn("[Doom] {}", message);
    }

    Game game() {
        return game;
    }

    /**
     * How the game is played.
     */
    public enum Mode {
        SOLO, COOP, DEATHMATCH
    }
}
