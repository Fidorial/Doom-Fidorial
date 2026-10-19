// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine;

import fr.euphyllia.fidorial.doom.engine.info.MobjInfo;
import fr.euphyllia.fidorial.doom.engine.info.MobjInfos;
import fr.euphyllia.fidorial.doom.engine.info.State;
import fr.euphyllia.fidorial.doom.engine.info.States;
import fr.euphyllia.fidorial.doom.engine.play.Actions;
import fr.euphyllia.fidorial.doom.engine.play.Ceilings;
import fr.euphyllia.fidorial.doom.engine.play.Doors;
import fr.euphyllia.fidorial.doom.engine.play.Enemy;
import fr.euphyllia.fidorial.doom.engine.play.Floors;
import fr.euphyllia.fidorial.doom.engine.play.Inter;
import fr.euphyllia.fidorial.doom.engine.play.Lights;
import fr.euphyllia.fidorial.doom.engine.play.MapMove;
import fr.euphyllia.fidorial.doom.engine.play.MapUtil;
import fr.euphyllia.fidorial.doom.engine.play.MobjSys;
import fr.euphyllia.fidorial.doom.engine.play.Plats;
import fr.euphyllia.fidorial.doom.engine.play.Pspr;
import fr.euphyllia.fidorial.doom.engine.play.Setup;
import fr.euphyllia.fidorial.doom.engine.play.Sight;
import fr.euphyllia.fidorial.doom.engine.play.Spec;
import fr.euphyllia.fidorial.doom.engine.play.User;
import fr.euphyllia.fidorial.doom.engine.render.RenderData;
import fr.euphyllia.fidorial.doom.engine.ui.Finale;
import fr.euphyllia.fidorial.doom.engine.ui.Intermission;
import org.jspecify.annotations.Nullable;

import static fr.euphyllia.fidorial.doom.engine.Defs.BTS_PAUSE;
import static fr.euphyllia.fidorial.doom.engine.Defs.BT_SPECIAL;
import static fr.euphyllia.fidorial.doom.engine.Defs.BT_SPECIALMASK;
import static fr.euphyllia.fidorial.doom.engine.Defs.GS_FINALE;
import static fr.euphyllia.fidorial.doom.engine.Defs.GS_INTERMISSION;
import static fr.euphyllia.fidorial.doom.engine.Defs.GS_LEVEL;
import static fr.euphyllia.fidorial.doom.engine.Defs.MAXHEALTH;
import static fr.euphyllia.fidorial.doom.engine.Defs.MAXPLAYERS;
import static fr.euphyllia.fidorial.doom.engine.Defs.NUMAMMO;
import static fr.euphyllia.fidorial.doom.engine.Defs.PST_DEAD;
import static fr.euphyllia.fidorial.doom.engine.Defs.PST_LIVE;
import static fr.euphyllia.fidorial.doom.engine.Defs.PST_REBORN;
import static fr.euphyllia.fidorial.doom.engine.Defs.am_clip;
import static fr.euphyllia.fidorial.doom.engine.Defs.commercial;
import static fr.euphyllia.fidorial.doom.engine.Defs.doom;
import static fr.euphyllia.fidorial.doom.engine.Defs.doom2;
import static fr.euphyllia.fidorial.doom.engine.Defs.ga_completed;
import static fr.euphyllia.fidorial.doom.engine.Defs.ga_loadlevel;
import static fr.euphyllia.fidorial.doom.engine.Defs.ga_nothing;
import static fr.euphyllia.fidorial.doom.engine.Defs.ga_victory;
import static fr.euphyllia.fidorial.doom.engine.Defs.ga_worlddone;
import static fr.euphyllia.fidorial.doom.engine.Defs.pack_plut;
import static fr.euphyllia.fidorial.doom.engine.Defs.pack_tnt;
import static fr.euphyllia.fidorial.doom.engine.Defs.registered;
import static fr.euphyllia.fidorial.doom.engine.Defs.retail;
import static fr.euphyllia.fidorial.doom.engine.Defs.shareware;
import static fr.euphyllia.fidorial.doom.engine.Defs.sk_baby;
import static fr.euphyllia.fidorial.doom.engine.Defs.sk_medium;
import static fr.euphyllia.fidorial.doom.engine.Defs.sk_nightmare;
import static fr.euphyllia.fidorial.doom.engine.Defs.wp_fist;
import static fr.euphyllia.fidorial.doom.engine.Defs.wp_pistol;
import static fr.euphyllia.fidorial.doom.engine.Fixed.FRACBITS;
import static fr.euphyllia.fidorial.doom.engine.Fixed.FRACUNIT;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_SHADOW;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_telept;
import static fr.euphyllia.fidorial.doom.engine.Tables.ANG45;
import static fr.euphyllia.fidorial.doom.engine.Tables.ANGLETOFINESHIFT;
import static fr.euphyllia.fidorial.doom.engine.Tables.finecosine;
import static fr.euphyllia.fidorial.doom.engine.Tables.finesine;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_BRUISERSHOT;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_HEADSHOT;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_TFOG;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_TROOPSHOT;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SARG_PAIN2;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SARG_RUN1;

/**
 * One running game: the simulation state that vanilla keeps in globals ({@code doomstat.c},
 * {@code g_game.c}, {@code p_tick.c}).
 *
 * <p>Several games can run side by side. A game is not thread safe: tick it and render its views
 * from one thread.</p>
 */
public final class Game {

    public static final int BODYQUESIZE = 32;
    private static final int[][] PARS = {
            {0},
            {0, 30, 75, 120, 90, 165, 180, 180, 30, 165},
            {0, 90, 90, 90, 120, 90, 360, 240, 30, 170},
            {0, 90, 45, 90, 150, 90, 90, 165, 30, 135}
    };
    private static final int[] CPARS = {
            30, 90, 120, 120, 90, 150, 120, 120, 270, 90,
            210, 150, 150, 150, 210, 150, 420, 150, 210, 150,
            240, 150, 180, 150, 150, 300, 330, 420, 300, 180,
            120, 30
    };
    private static final int DEMOMARKER = 0x80;

    public final Wad wad;
    public final RenderData res;
    public final int gamemode;
    public final int gamemission;
    /**
     * Whether lost souls bounce off floors, as in Ultimate Doom and Final Doom.
     */
    public final boolean correctLostSoulBounce;

    /**
     * This game's copy of the state table, which nightmare and {@code -fast} modify.
     */
    public final State[] states = States.STATES.clone();
    /**
     * The state table before fast monsters, as changed by DEHACKED.
     */
    public final State[] baseStates = States.STATES.clone();
    /**
     * This game's copy of the thing table, which nightmare and {@code -fast} modify.
     */
    public final MobjInfo[] mobjinfo = MobjInfos.MOBJINFO.clone();
    /**
     * The thing table before fast monsters, as changed by DEHACKED.
     */
    public final MobjInfo[] baseMobjinfo = MobjInfos.MOBJINFO.clone();

    public final MRandom random = new MRandom();
    public final GameSound sound = new GameSound(this);
    public final MapUtil mapUtil = new MapUtil(this);
    public final MapMove map = new MapMove(this);
    public final MobjSys mobjs = new MobjSys(this);
    public final Enemy enemy = new Enemy(this);
    public final Inter inter = new Inter(this);
    public final Pspr pspr = new Pspr(this);
    public final User user = new User(this);
    public final Spec spec = new Spec(this);
    public final Doors doors = new Doors(this);
    public final Floors floors = new Floors(this);
    public final Ceilings ceilings = new Ceilings(this);
    public final Plats plats = new Plats(this);
    public final Lights lights = new Lights(this);
    public final Sight sight = new Sight(this);
    public final Actions actions = new Actions(this);
    public final Setup setup = new Setup(this);
    public final Intermission wi;
    public final Finale finale;
    public final Thinker thinkercap = new Thinker() {
    };
    public final Player[] players = new Player[MAXPLAYERS];
    public final boolean[] playeringame = new boolean[MAXPLAYERS];
    public final TicCmd[] netcmds = new TicCmd[MAXPLAYERS];
    public final WbStart wminfo = new WbStart();
    public final int skytexturemid = 100 * FRACUNIT;
    public final MapThing[] deathmatchstarts = new MapThing[10];
    public final MapThing[] playerstarts = new MapThing[MAXPLAYERS];
    public final boolean[] playerstartSet = new boolean[MAXPLAYERS];
    public final Mobj[] bodyque = new Mobj[BODYQUESIZE];
    private final java.util.Map<String, String> textOverrides = new java.util.HashMap<>();
    private final java.util.Map<Integer, Integer> parOverrides = new java.util.HashMap<>();
    public Level level = new Level();
    public int gameaction = ga_nothing;
    public int gamestate = GS_LEVEL;
    public int gameskill = sk_medium;
    public boolean respawnmonsters;
    public int gameepisode = 1;
    public int gamemap = 1;
    public boolean paused;
    public int deathmatch;
    public boolean netgame;
    public boolean nomonsters;
    public boolean respawnparm;
    public boolean fastparm;
    /**
     * Deathmatch time limit in minutes ({@code -timer}), 0 for none.
     */
    public int timelimitMinutes;
    public int gametic;
    public int leveltime;
    public int levelstarttic;
    /**
     * Changes each time a level is loaded.
     */
    public int levelSerial;
    public int totalkills;
    public int totalitems;
    public int totalsecret;
    public boolean secretexit;
    public int validcount = 1;
    public int skyflatnum;
    public int skytexture;
    public int[] texturetranslation;
    public int[] flattranslation;
    public int deathmatchCount;
    public int bodyqueslot;
    public boolean demoplayback;
    private byte @Nullable [] demobuffer;
    private int demo_p;
    private GameListener listener = GameListener.NONE;

    /**
     * Creates a game on a loaded WAD.
     *
     * @param wad the IWAD and PWADs
     * @param res the render data of that WAD, which may be shared
     */
    public Game(final Wad wad, final RenderData res) {
        this.wad = wad;
        this.res = res;
        this.gamemode = identifyMode(wad);
        this.gamemission = identifyMission(wad, gamemode);
        this.correctLostSoulBounce = gamemode == retail || gamemission == pack_tnt || gamemission == pack_plut;
        for (int i = 0; i < MAXPLAYERS; i++) {
            players[i] = new Player(i);
            netcmds[i] = new TicCmd();
            playerstarts[i] = new MapThing();
        }
        for (int i = 0; i < deathmatchstarts.length; i++) {
            deathmatchstarts[i] = new MapThing();
        }
        thinkercap.prev = thinkercap.next = thinkercap;
        texturetranslation = new int[res.textures.length + 1];
        for (int i = 0; i < texturetranslation.length; i++) {
            texturetranslation[i] = Math.min(i, res.textures.length - 1);
        }
        flattranslation = new int[res.flats.length + 1];
        for (int i = 0; i < flattranslation.length; i++) {
            flattranslation[i] = Math.min(i, Math.max(0, res.flats.length - 1));
        }
        spec.P_InitSwitchList();
        spec.P_InitPicAnims();
        this.wi = new Intermission(this);
        this.finale = new Finale(this);
    }

    /**
     * {@code IdentifyVersion}, from the lumps instead of the file name.
     */
    public static int identifyMode(final Wad wad) {
        if (wad.checkNumForName("MAP01") >= 0) {
            return commercial;
        }
        if (wad.checkNumForName("E4M1") >= 0) {
            return retail;
        }
        if (wad.checkNumForName("E2M1") >= 0) {
            return registered;
        }
        return shareware;
    }

    private static int identifyMission(final Wad wad, final int mode) {
        if (mode != commercial) {
            return doom;
        }
        for (final String file : wad.files()) {
            final String lower = file.toLowerCase(java.util.Locale.ROOT);
            if (lower.contains("tnt")) {
                return pack_tnt;
            }
            if (lower.contains("plutonia")) {
                return pack_plut;
            }
        }
        return doom2;
    }

    private static MobjInfo withSpeed(final MobjInfo m, final int speed) {
        return new MobjInfo(m.doomednum(), m.spawnstate(), m.spawnhealth(), m.seestate(), m.seesound(), m.reactiontime(),
                m.attacksound(), m.painstate(), m.painchance(), m.painsound(), m.meleestate(), m.missilestate(),
                m.deathstate(), m.xdeathstate(), m.deathsound(), speed, m.radius(), m.height(), m.mass(), m.damage(),
                m.activesound(), m.flags(), m.raisestate());
    }

    public GameListener listener() {
        return listener;
    }

    public void setListener(final GameListener listener) {
        this.listener = listener == null ? GameListener.NONE : listener;
    }

    public void warn(final String message) {
        listener.warn(message);
    }

    /**
     * Replaces a text everywhere it is shown, such as from a DEHACKED lump.
     *
     * @param original the original English text, one of the {@link Strings} constants
     * @param value    the replacement
     */
    public void setTextOverride(final String original, final String value) {
        textOverrides.put(original, value);
    }

    /**
     * Applies every DEHACKED lump of the loaded WADs, in load order. Call before starting a game.
     */
    public void applyDehackedLumps() {
        for (int lump = wad.findForward("DEHACKED", 0); lump >= 0; lump = wad.findForward("DEHACKED", lump + 1)) {
            Dehacked.apply(this, new String(wad.readLump(lump), java.nio.charset.StandardCharsets.ISO_8859_1));
        }
    }

    /**
     * {@return the text to show in place of an original text}
     */
    public String text(final String original) {
        return textOverrides.getOrDefault(original, original);
    }

    /**
     * Changes the par time of a level.
     *
     * @param episode 1 based, ignored for Doom II
     * @param map     1 based
     * @param seconds the par time
     */
    public void setParTime(final int episode, final int map, final int seconds) {
        parOverrides.put((gamemode == commercial ? 0 : episode) * 1000 + map, seconds);
    }

    /**
     * {@return the height of a wall texture in fixed point ({@code textureheight[]})}
     */
    public int textureheight(final int texnum) {
        if (texnum < 0 || texnum >= res.textures.length) {
            return 0;
        }
        return res.textures[texnum].heightFixed;
    }

    /**
     * {@return whether a map exists in this WAD}
     */
    public boolean mapExists(final int episode, final int map) {
        return wad.checkNumForName(Setup.mapLumpName(gamemode == commercial, episode, map)) >= 0;
    }

    public void P_InitThinkers() {
        thinkercap.prev = thinkercap.next = thinkercap;
    }

    public void P_AddThinker(final Thinker thinker) {
        thinkercap.prev.next = thinker;
        thinker.next = thinkercap;
        thinker.prev = thinkercap.prev;
        thinkercap.prev = thinker;
    }

    public void P_RemoveThinker(final Thinker thinker) {
        thinker.function = Thinker.FN_REMOVED;
    }

    private void P_RunThinkers() {
        Thinker currentthinker = thinkercap.next;
        while (currentthinker != thinkercap) {
            if (currentthinker.function == Thinker.FN_REMOVED) {
                currentthinker.next.prev = currentthinker.prev;
                currentthinker.prev.next = currentthinker.next;
            } else if (currentthinker.function == Thinker.FN_ACTIVE) {
                currentthinker.think(this);
            }
            currentthinker = currentthinker.next;
        }
    }

    private void P_Ticker() {
        if (paused) {
            return;
        }
        for (int i = 0; i < MAXPLAYERS; i++) {
            if (playeringame[i] && players[i].mo != null) {
                user.P_PlayerThink(players[i]);
            }
        }
        P_RunThinkers();
        spec.P_UpdateSpecials();
        mobjs.P_RespawnSpecials();
        leveltime++;
    }

    /**
     * Starts a new game, as the menu does ({@code G_DeferedInitNew} then {@code G_DoNewGame}).
     *
     * @param skill   the skill, {@link Defs#sk_baby} to {@link Defs#sk_nightmare}
     * @param episode the episode, 1 based
     * @param map     the map, 1 based
     */
    public void G_InitNew(int skill, int episode, int map) {
        paused = false;
        if (skill > sk_nightmare) {
            skill = sk_nightmare;
        }
        if (skill < sk_baby) {
            skill = sk_baby;
        }
        if (episode < 1) {
            episode = 1;
        }
        if (gamemode == retail) {
            if (episode > 4) {
                episode = 4;
            }
        } else if (gamemode == shareware) {
            if (episode > 1) {
                episode = 1;
            }
        } else {
            if (episode > 3) {
                episode = 3;
            }
        }
        if (map < 1) {
            map = 1;
        }
        if (map > 9 && gamemode != commercial) {
            map = 9;
        }
        random.M_ClearRandom();
        respawnmonsters = skill == sk_nightmare || respawnparm;
        final boolean fast = fastparm || skill == sk_nightmare;
        applyFastMonsters(fast);
        for (int i = 0; i < MAXPLAYERS; i++) {
            players[i].playerstate = PST_REBORN;
        }
        gameepisode = episode;
        gamemap = map;
        gameskill = skill;
        if (gamemode == commercial) {
            skytexture = res.textureNumForName("SKY3");
            if (gamemap < 12) {
                skytexture = res.textureNumForName("SKY1");
            } else if (gamemap < 21) {
                skytexture = res.textureNumForName("SKY2");
            }
        } else {
            skytexture = switch (episode) {
                case 2 -> res.textureNumForName("SKY2");
                case 3 -> res.textureNumForName("SKY3");
                case 4 -> res.textureNumForName("SKY4");
                default -> res.textureNumForName("SKY1");
            };
        }
        G_DoLoadLevel();
    }

    private void applyFastMonsters(final boolean fast) {
        for (int i = S_SARG_RUN1; i <= S_SARG_PAIN2; i++) {
            final State base = baseStates[i];
            states[i] = fast
                    ? new State(base.index(), base.sprite(), base.frame(), base.tics() >> 1, base.action(), base.nextstate(),
                    base.misc1(), base.misc2())
                    : base;
        }
        for (final int type : new int[]{MT_BRUISERSHOT, MT_HEADSHOT, MT_TROOPSHOT}) {
            mobjinfo[type] = fast ? withSpeed(baseMobjinfo[type], 20 * FRACUNIT) : baseMobjinfo[type];
        }
    }

    public void G_DoLoadLevel() {
        skyflatnum = res.flatNumForName("F_SKY1");
        if (gamemode == commercial) {
            skytexture = res.textureNumForName("SKY3");
            if (gamemap < 12) {
                skytexture = res.textureNumForName("SKY1");
            } else if (gamemap < 21) {
                skytexture = res.textureNumForName("SKY2");
            }
        }
        levelstarttic = gametic;
        gamestate = GS_LEVEL;
        for (int i = 0; i < MAXPLAYERS; i++) {
            if (playeringame[i] && players[i].playerstate == PST_DEAD) {
                players[i].playerstate = PST_REBORN;
            }
            java.util.Arrays.fill(players[i].frags, 0);
        }
        for (int i = 0; i < texturetranslation.length; i++) {
            texturetranslation[i] = Math.min(i, res.textures.length - 1);
        }
        for (int i = 0; i < flattranslation.length; i++) {
            flattranslation[i] = Math.min(i, Math.max(0, res.flats.length - 1));
        }
        setup.P_SetupLevel(gameepisode, gamemap, gameskill);
        levelSerial++;
        gameaction = ga_nothing;
        paused = false;
        listener.gameStateChanged(gamestate);
        listener.levelLoaded();
    }

    /**
     * Runs one game tic ({@code G_Ticker}). Fill {@link #netcmds} beforehand.
     */
    public void G_Ticker() {
        for (int i = 0; i < MAXPLAYERS; i++) {
            if (playeringame[i] && players[i].playerstate == PST_REBORN) {
                G_DoReborn(i);
            }
        }
        while (gameaction != ga_nothing) {
            switch (gameaction) {
                case ga_loadlevel -> G_DoLoadLevel();
                case ga_completed -> G_DoCompleted();
                case ga_victory -> {
                    gameaction = ga_nothing;
                    finale.F_StartFinale();
                    listener.gameStateChanged(gamestate);
                }
                case ga_worlddone -> G_DoWorldDone();
                default -> gameaction = ga_nothing;
            }
        }
        for (int i = 0; i < MAXPLAYERS; i++) {
            if (playeringame[i]) {
                final TicCmd cmd = players[i].cmd;
                cmd.copyFrom(netcmds[i]);
                if (demoplayback) {
                    G_ReadDemoTiccmd(cmd);
                }
            }
        }
        for (int i = 0; i < MAXPLAYERS; i++) {
            if (playeringame[i] && (players[i].cmd.buttons() & BT_SPECIAL) != 0) {
                if ((players[i].cmd.buttons() & BT_SPECIALMASK) == BTS_PAUSE) {
                    paused = !paused;
                }
            }
        }
        switch (gamestate) {
            case GS_LEVEL -> P_Ticker();
            case GS_INTERMISSION -> wi.WI_Ticker();
            case GS_FINALE -> finale.F_Ticker();
            default -> {
            }
        }
        gametic++;
    }

    public void G_PlayerFinishLevel(final int player) {
        final Player p = players[player];
        java.util.Arrays.fill(p.powers, 0);
        java.util.Arrays.fill(p.cards, false);
        if (p.mo != null) {
            p.mo.flags &= ~MF_SHADOW;
        }
        p.extralight = 0;
        p.fixedcolormap = 0;
        p.damagecount = 0;
        p.bonuscount = 0;
    }

    public void G_PlayerReborn(final int player) {
        final Player p = players[player];
        final int[] frags = p.frags.clone();
        final int killcount = p.killcount;
        final int itemcount = p.itemcount;
        final int secretcount = p.secretcount;
        p.clear();
        System.arraycopy(frags, 0, p.frags, 0, frags.length);
        p.killcount = killcount;
        p.itemcount = itemcount;
        p.secretcount = secretcount;
        p.usedown = p.attackdown = 1;
        p.playerstate = PST_LIVE;
        p.health = MAXHEALTH;
        p.readyweapon = p.pendingweapon = wp_pistol;
        p.weaponowned[wp_fist] = true;
        p.weaponowned[wp_pistol] = true;
        p.ammo[am_clip] = 50;
        System.arraycopy(fr.euphyllia.fidorial.doom.engine.play.Inter.MAXAMMO, 0, p.maxammo, 0, NUMAMMO);
    }

    private boolean G_CheckSpot(final int playernum, final MapThing mthing) {
        if (players[playernum].mo == null) {
            for (int i = 0; i < playernum; i++) {
                if (players[i].mo != null && players[i].mo.x == mthing.x << FRACBITS && players[i].mo.y == mthing.y << FRACBITS) {
                    return false;
                }
            }
            return true;
        }
        final int x = mthing.x << FRACBITS;
        final int y = mthing.y << FRACBITS;
        if (!map.P_CheckPosition(players[playernum].mo, x, y)) {
            return false;
        }
        if (bodyqueslot >= BODYQUESIZE) {
            final Mobj old = bodyque[bodyqueslot % BODYQUESIZE];
            if (old != null && old.function != Thinker.FN_REMOVED) {
                mobjs.P_RemoveMobj(old);
            }
        }
        bodyque[bodyqueslot % BODYQUESIZE] = players[playernum].mo;
        bodyqueslot++;
        final Subsector ss = level.pointInSubsector(x, y);
        final int an = (ANG45 * (mthing.angle / 45)) >>> ANGLETOFINESHIFT;
        final Mobj mo = mobjs.P_SpawnMobj(x + 20 * finecosine(an), y + 20 * finesine[an], ss.sector.floorheight, MT_TFOG);
        sound.S_StartSound(mo, sfx_telept);
        return true;
    }

    public void G_DeathMatchSpawnPlayer(final int playernum) {
        final int selections = deathmatchCount;
        if (selections >= 1) {
            for (int j = 0; j < 20; j++) {
                final int i = random.P_Random() % selections;
                if (G_CheckSpot(playernum, deathmatchstarts[i])) {
                    deathmatchstarts[i].type = (short) (playernum + 1);
                    mobjs.P_SpawnPlayer(deathmatchstarts[i]);
                    return;
                }
            }
        }
        spawnAtStart(playernum);
    }

    private void spawnAtStart(final int playernum) {
        final MapThing start = playerstartSet[playernum] ? playerstarts[playernum] : playerstarts[0];
        final MapThing copy = new MapThing(start);
        copy.type = (short) (playernum + 1);
        mobjs.P_SpawnPlayer(copy);
    }

    private void G_DoReborn(final int playernum) {
        if (!netgame) {
            gameaction = ga_loadlevel;
            return;
        }
        if (players[playernum].mo != null) {
            players[playernum].mo.player = null;
        }
        if (deathmatch != 0) {
            G_DeathMatchSpawnPlayer(playernum);
            return;
        }
        if (playerstartSet[playernum] && G_CheckSpot(playernum, playerstarts[playernum])) {
            mobjs.P_SpawnPlayer(playerstarts[playernum]);
            return;
        }
        for (int i = 0; i < MAXPLAYERS; i++) {
            if (playerstartSet[i] && G_CheckSpot(playernum, playerstarts[i])) {
                playerstarts[i].type = (short) (playernum + 1);
                mobjs.P_SpawnPlayer(playerstarts[i]);
                playerstarts[i].type = (short) (i + 1);
                return;
            }
        }
        spawnAtStart(playernum);
    }

    public void G_ExitLevel() {
        secretexit = false;
        gameaction = ga_completed;
    }

    public void G_SecretExitLevel() {
        secretexit = !(gamemode == commercial && wad.checkNumForName("MAP31") < 0);
        gameaction = ga_completed;
    }

    private void G_DoCompleted() {
        gameaction = ga_nothing;
        for (int i = 0; i < MAXPLAYERS; i++) {
            if (playeringame[i]) {
                G_PlayerFinishLevel(i);
            }
        }
        if (gamemode != commercial) {
            switch (gamemap) {
                case 8 -> {
                    gameaction = ga_victory;
                    return;
                }
                case 9 -> {
                    for (int i = 0; i < MAXPLAYERS; i++) {
                        players[i].didsecret = true;
                    }
                }
                default -> {
                }
            }
        }
        wminfo.didsecret = players[firstPlayer()].didsecret;
        wminfo.epsd = gameepisode - 1;
        wminfo.last = gamemap - 1;
        if (gamemode == commercial) {
            if (secretexit) {
                switch (gamemap) {
                    case 15 -> wminfo.next = 30;
                    case 31 -> wminfo.next = 31;
                    default -> {
                    }
                }
            } else {
                switch (gamemap) {
                    case 31, 32 -> wminfo.next = 15;
                    default -> wminfo.next = gamemap;
                }
            }
        } else {
            if (secretexit) {
                wminfo.next = 8;
            } else if (gamemap == 9) {
                wminfo.next = switch (gameepisode) {
                    case 1 -> 3;
                    case 2 -> 5;
                    case 3 -> 6;
                    default -> 2;
                };
            } else {
                wminfo.next = gamemap;
            }
        }
        wminfo.maxkills = totalkills;
        wminfo.maxitems = totalitems;
        wminfo.maxsecret = totalsecret;
        wminfo.maxfrags = 0;
        final Integer parOverride = parOverrides.get((gamemode == commercial ? 0 : gameepisode) * 1000 + gamemap);
        if (parOverride != null) {
            wminfo.partime = 35 * parOverride;
        } else if (gamemode == commercial) {
            wminfo.partime = 35 * CPARS[Math.clamp(gamemap - 1, 0, CPARS.length - 1)];
        } else {
            final int ep = Math.clamp(gameepisode, 0, PARS.length - 1);
            wminfo.partime = gamemap < PARS[ep].length ? 35 * PARS[ep][gamemap] : 0;
        }
        wminfo.pnum = firstPlayer();
        for (int i = 0; i < MAXPLAYERS; i++) {
            wminfo.plyr[i].in = playeringame[i];
            wminfo.plyr[i].skills = players[i].killcount;
            wminfo.plyr[i].sitems = players[i].itemcount;
            wminfo.plyr[i].ssecret = players[i].secretcount;
            wminfo.plyr[i].stime = leveltime;
            System.arraycopy(players[i].frags, 0, wminfo.plyr[i].frags, 0, 4);
        }
        gamestate = GS_INTERMISSION;
        wi.WI_Start(wminfo);
        listener.gameStateChanged(gamestate);
    }

    /**
     * {@return the lowest player slot in game, the stand-in for {@code consoleplayer}}
     */
    public int firstPlayer() {
        for (int i = 0; i < MAXPLAYERS; i++) {
            if (playeringame[i]) {
                return i;
            }
        }
        return 0;
    }

    public void G_WorldDone() {
        gameaction = ga_worlddone;
        if (secretexit) {
            for (int i = 0; i < MAXPLAYERS; i++) {
                players[i].didsecret = true;
            }
        }
        if (gamemode == commercial) {
            boolean start = false;
            switch (gamemap) {
                case 15, 31 -> start = secretexit;
                case 6, 11, 20, 30 -> start = true;
                default -> {
                }
            }
            if (start) {
                finale.F_StartFinale();
                listener.gameStateChanged(gamestate);
            }
        }
    }

    private void G_DoWorldDone() {
        gamestate = GS_LEVEL;
        gamemap = wminfo.next + 1;
        G_DoLoadLevel();
        gameaction = ga_nothing;
    }

    /**
     * Starts playing back a demo lump ({@code G_DoPlayDemo}).
     *
     * @param demo the demo bytes
     * @return false if the demo is from an unsupported version
     */
    public boolean playDemo(final byte[] demo) {
        int p = 0;
        final int version = demo[p++] & 0xff;
        if (version != 109 && version != 110) {
            return false;
        }
        final int skill = demo[p++];
        final int episode = demo[p++];
        final int mapnum = demo[p++];
        deathmatch = demo[p++];
        respawnparm = demo[p++] != 0;
        fastparm = demo[p++] != 0;
        nomonsters = demo[p++] != 0;
        p++; // consoleplayer
        for (int i = 0; i < MAXPLAYERS; i++) {
            playeringame[i] = demo[p++] != 0;
        }
        netgame = playeringame[1] || playeringame[2] || playeringame[3];
        demobuffer = demo;
        demo_p = p;
        G_InitNew(skill, episode, mapnum);
        demoplayback = true;
        return true;
    }

    /**
     * {@return whether a demo is still playing}
     */
    public boolean demoPlaying() {
        return demoplayback;
    }

    private void G_ReadDemoTiccmd(final TicCmd cmd) {
        final byte[] buf = demobuffer;
        if (buf == null || demo_p >= buf.length || (buf[demo_p] & 0xff) == DEMOMARKER) {
            demoplayback = false;
            return;
        }
        cmd.forwardmove = buf[demo_p++];
        cmd.sidemove = buf[demo_p++];
        cmd.angleturn = (short) ((buf[demo_p++] & 0xff) << 8);
        cmd.buttons = buf[demo_p++];
    }

    /**
     * {@return a checksum of the simulation state, to compare runs}
     */
    public long stateHash() {
        long h = 1125899906842597L;
        h = 31 * h + random.prndindex;
        h = 31 * h + leveltime;
        for (Thinker th = thinkercap.next; th != thinkercap; th = th.next) {
            if (th instanceof final Mobj mo && th.function != Thinker.FN_REMOVED) {
                h = 31 * h + mo.x;
                h = 31 * h + mo.y;
                h = 31 * h + mo.z;
                h = 31 * h + mo.angle;
                h = 31 * h + mo.health;
                h = 31 * h + mo.stateIndex();
            }
        }
        return h;
    }
}
