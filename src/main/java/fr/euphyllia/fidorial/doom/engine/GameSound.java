// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine;

import org.jspecify.annotations.Nullable;

import static fr.euphyllia.fidorial.doom.engine.Defs.commercial;
import static fr.euphyllia.fidorial.doom.engine.Music.NUMMUSIC;
import static fr.euphyllia.fidorial.doom.engine.Music.mus_None;
import static fr.euphyllia.fidorial.doom.engine.Music.mus_e1m1;
import static fr.euphyllia.fidorial.doom.engine.Music.mus_e1m5;
import static fr.euphyllia.fidorial.doom.engine.Music.mus_e1m9;
import static fr.euphyllia.fidorial.doom.engine.Music.mus_e2m4;
import static fr.euphyllia.fidorial.doom.engine.Music.mus_e2m5;
import static fr.euphyllia.fidorial.doom.engine.Music.mus_e2m6;
import static fr.euphyllia.fidorial.doom.engine.Music.mus_e2m7;
import static fr.euphyllia.fidorial.doom.engine.Music.mus_e3m2;
import static fr.euphyllia.fidorial.doom.engine.Music.mus_e3m3;
import static fr.euphyllia.fidorial.doom.engine.Music.mus_e3m4;
import static fr.euphyllia.fidorial.doom.engine.Music.mus_runnin;

/**
 * The simulation side of {@code s_sound.c}: it decides which sound starts where, and leaves volume,
 * panning and playback to each listener.
 */
public final class GameSound {

    private static final int[] SPMUS = {
            mus_e3m4, mus_e3m2, mus_e3m3, mus_e1m5, mus_e2m7, mus_e2m4, mus_e2m6, mus_e2m5, mus_e1m9
    };

    private final Game g;
    private int musicnum;

    GameSound(final Game g) {
        this.g = g;
    }

    /**
     * Starts a sound for everyone ({@code S_StartSound}).
     *
     * @param origin where it comes from, or null for a sound heard at full volume
     * @param sfx    the sound number
     */
    public void S_StartSound(final @Nullable DegenMobj origin, final int sfx) {
        if (sfx < 1 || sfx >= Sfx.NAME.length) {
            return;
        }
        g.listener().startSound(origin, sfx, null);
    }

    /**
     * Starts a sound only the given player hears, as vanilla does with
     * {@code if (player == &players[consoleplayer]) S_StartSound(NULL, ...)}.
     *
     * @param player the listener
     * @param sfx    the sound number
     */
    public void S_StartLocalSound(final Player player, final int sfx) {
        if (sfx < 1 || sfx >= Sfx.NAME.length) {
            return;
        }
        g.listener().startSound(null, sfx, player);
    }

    public void S_StopSound(final DegenMobj origin) {
        g.listener().stopSound(origin);
    }

    /**
     * {@return the current music number}
     */
    public int music() {
        return musicnum;
    }

    /**
     * Picks the level music ({@code S_Start}).
     */
    public void S_Start() {
        final int mnum;
        if (g.gamemode == commercial) {
            mnum = mus_runnin + g.gamemap - 1;
        } else if (g.gameepisode < 4) {
            mnum = mus_e1m1 + (g.gameepisode - 1) * 9 + g.gamemap - 1;
        } else {
            mnum = SPMUS[Math.clamp(g.gamemap - 1, 0, SPMUS.length - 1)];
        }
        S_ChangeMusic(mnum, true);
    }

    public void S_ChangeMusic(final int musicnum, final boolean looping) {
        if (musicnum <= mus_None || musicnum >= NUMMUSIC) {
            return;
        }
        this.musicnum = musicnum;
        g.listener().changeMusic(musicnum, looping);
    }
}
