// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine;

import static fr.euphyllia.fidorial.doom.engine.Defs.MAXPLAYERS;

/**
 * What the intermission screen shows ({@code wbstartstruct_t}).
 */
public final class WbStart {

    public final WbPlayer[] plyr = new WbPlayer[MAXPLAYERS];
    public int epsd;
    public boolean didsecret;
    public int last;
    public int next;
    public int maxkills;
    public int maxitems;
    public int maxsecret;
    public int maxfrags;
    public int partime;
    public int pnum;
    public WbStart() {
        for (int i = 0; i < MAXPLAYERS; i++) {
            plyr[i] = new WbPlayer();
        }
    }

    /**
     * Per player statistics ({@code wbplayerstruct_t}).
     */
    public static final class WbPlayer {
        public final int[] frags = new int[4];
        public boolean in;
        public int skills;
        public int sitems;
        public int ssecret;
        public int stime;
        public int score;
    }
}
