package fr.euphyllia.fidorial.doom.session;

import fr.euphyllia.fidorial.doom.DoomConfig;
import fr.euphyllia.fidorial.doom.display.DoomPalette;
import fr.euphyllia.fidorial.doom.engine.Game;
import fr.euphyllia.fidorial.doom.engine.Wad;
import fr.euphyllia.fidorial.doom.engine.render.RenderData;
import fr.euphyllia.fidorial.doom.engine.ui.HuFont;
import fr.fidorial.Server;
import fr.fidorial.entity.player.Player;
import fr.fidorial.entity.player.PlayerInput;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Every Doom session of the server, and which player is in which.
 */
public final class SessionManager {

    private final Server server;
    private final ComponentLogger logger;
    private final DoomConfig config;
    private final Wad wad;
    private final RenderData res;
    private final DoomPalette palette;
    private final HuFont font;
    private final Map<String, Float> soundLengths;
    private final @Nullable ResourcePackRequest pack;
    private final UUID packId;
    private final Map<UUID, DoomPlayer> players = new ConcurrentHashMap<>();
    private final Map<Integer, DoomSession> sessions = new ConcurrentHashMap<>();
    private final AtomicInteger nextId = new AtomicInteger(1);

    public SessionManager(final Server server, final ComponentLogger logger, final DoomConfig config, final Wad wad,
                          final RenderData res, final DoomPalette palette, final Map<String, Float> soundLengths,
                          final @Nullable ResourcePackRequest pack, final UUID packId) {
        this.server = server;
        this.logger = logger;
        this.config = config;
        this.wad = wad;
        this.res = res;
        this.palette = palette;
        this.font = new HuFont(res);
        this.soundLengths = Map.copyOf(soundLengths);
        this.pack = pack;
        this.packId = packId;
    }

    Server server() {
        return server;
    }

    ComponentLogger logger() {
        return logger;
    }

    DoomConfig config() {
        return config;
    }

    DoomPalette palette() {
        return palette;
    }

    HuFont font() {
        return font;
    }

    boolean hasSound(final String event) {
        return soundLengths.containsKey(event);
    }

    float soundLength(final String event) {
        return soundLengths.getOrDefault(event, 1f);
    }

    Game newGame() {
        final Game game = new Game(wad, res);
        game.applyDehackedLumps();
        return game;
    }

    /**
     * {@return the game mode of the loaded WAD, see {@link fr.euphyllia.fidorial.doom.engine.Defs}}
     */
    public int gameMode() {
        return Game.identifyMode(wad);
    }

    public @Nullable DoomPlayer player(final UUID uuid) {
        return players.get(uuid);
    }

    public List<DoomSession> sessions() {
        return new ArrayList<>(sessions.values());
    }

    public @Nullable DoomSession session(final int id) {
        return sessions.get(id);
    }

    /**
     * Starts a new session and puts the player in it.
     *
     * @return the session
     */
    public DoomSession create(final Player player, final DoomSession.Mode mode, final int skill, final int episode,
                              final int map) {
        leave(player.uuid());
        final DoomSession session = new DoomSession(this, nextId.getAndIncrement(), mode, skill, episode, map);
        sessions.put(session.id(), session);
        join(player, session);
        return session;
    }

    /**
     * Puts a player in an existing session.
     *
     * @return false when the session is full
     */
    public boolean join(final Player player, final DoomSession session) {
        leave(player.uuid());
        final DoomPlayer doomPlayer = session.add(player);
        if (doomPlayer == null) {
            return false;
        }
        players.put(player.uuid(), doomPlayer);
        if (pack != null) {
            player.sendResourcePacks(pack);
        }
        player.execute(() -> doomPlayer.enter(player));
        return true;
    }

    /**
     * Takes a player out of their session and gives their things back.
     *
     * @return false when the player was not playing
     */
    public boolean leave(final UUID uuid) {
        final DoomPlayer doomPlayer = players.remove(uuid);
        if (doomPlayer == null) {
            return false;
        }
        doomPlayer.session().remove(doomPlayer);
        final Player player = server.players().get(uuid).orElse(null);
        if (player != null) {
            DoomSession.stopMusic(player);
            if (pack != null && config.removePackOnQuit()) {
                player.removeResourcePacks(packId);
            }
            if (player.isOwnedByCurrentThread()) {
                doomPlayer.leave(player);
            } else if (!player.execute(() -> doomPlayer.leave(player))) {
                doomPlayer.leave(player);
            }
        }
        return true;
    }

    /**
     * Gives a disconnecting player their things back right away, before their data is saved.
     */
    public void disconnected(final Player player) {
        final DoomPlayer doomPlayer = players.remove(player.uuid());
        if (doomPlayer == null) {
            return;
        }
        doomPlayer.session().remove(doomPlayer);
        doomPlayer.leave(player);
    }

    void forget(final DoomSession session) {
        sessions.remove(session.id());
    }

    void crashed(final DoomSession session) {
        for (final DoomPlayer p : session.players()) {
            leave(p.uuid());
        }
        session.stop();
    }

    /**
     * Ends every session.
     */
    public void shutdown() {
        for (final UUID uuid : List.copyOf(players.keySet())) {
            leave(uuid);
        }
        for (final DoomSession session : sessions()) {
            session.stop();
        }
    }

    public void keys(final UUID uuid, final PlayerInput input) {
        final DoomPlayer p = players.get(uuid);
        if (p != null) {
            p.input().keys(input);
        }
    }

    public void turn(final UUID uuid, final double degrees) {
        final DoomPlayer p = players.get(uuid);
        if (p != null) {
            p.input().turn(degrees);
        }
    }

    public boolean use(final UUID uuid) {
        final DoomPlayer p = players.get(uuid);
        if (p != null) {
            p.input().use();
        }
        return p != null;
    }

    public boolean punch(final UUID uuid) {
        final DoomPlayer p = players.get(uuid);
        if (p != null) {
            p.input().punch();
        }
        return p != null;
    }

    /**
     * Handles a hotbar selection: keys 1 to 7 pick a weapon, 9 the next one.
     *
     * @return true when the player is playing, so the selection should be undone
     */
    public boolean hotbar(final UUID uuid, final int slot) {
        final DoomPlayer p = players.get(uuid);
        if (p == null) {
            return false;
        }
        if (slot <= 6) {
            p.input().weapon(slot + 1);
        } else if (slot == 8) {
            p.input().weapon(InputState.NEXT_WEAPON);
        }
        return true;
    }

    /**
     * Toggles the automap. @return true when the player is playing
     */
    public boolean toggleAutoMap(final UUID uuid) {
        final DoomPlayer p = players.get(uuid);
        if (p == null) {
            return false;
        }
        p.session().submit(() -> p.view().toggleAutoMap());
        return true;
    }

    /**
     * Toggles always run. @return true when the player is playing
     */
    public boolean toggleRun(final UUID uuid) {
        final DoomPlayer p = players.get(uuid);
        if (p == null) {
            return false;
        }
        final boolean on = p.input().toggleAlwaysRun();
        p.session().submit(() -> p.view().headsUp().post(on ? "ALWAYS RUN ON" : "ALWAYS RUN OFF"));
        return true;
    }

    /**
     * Applies a cheat typed in the chat. @return true when the message was a cheat code
     */
    public boolean cheat(final UUID uuid, final String code) {
        final DoomPlayer p = players.get(uuid);
        return p != null && config.cheats() && p.session().cheat(p, code);
    }

    /**
     * Changes the view size of a player, 3 to 11.
     */
    public boolean viewSize(final UUID uuid, final int size) {
        final DoomPlayer p = players.get(uuid);
        if (p == null) {
            return false;
        }
        p.session().submit(() -> p.view().setViewSize(size, 0));
        return true;
    }
}
