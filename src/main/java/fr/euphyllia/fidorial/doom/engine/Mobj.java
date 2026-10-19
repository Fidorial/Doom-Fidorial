// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine;

import fr.euphyllia.fidorial.doom.engine.info.MobjInfo;
import fr.euphyllia.fidorial.doom.engine.info.State;
import org.jspecify.annotations.Nullable;

/**
 * A map object: monster, item, projectile, decoration or player ({@code mobj_t}).
 */
public final class Mobj extends DegenMobj {

    public final MapThing spawnpoint = new MapThing();
    public @Nullable Mobj snext;
    public @Nullable Mobj sprev;
    public int angle;
    public int sprite;
    public int frame;
    public @Nullable Mobj bnext;
    public @Nullable Mobj bprev;
    public Subsector subsector;
    public int floorz;
    public int ceilingz;
    public int radius;
    public int height;
    public int momx;
    public int momy;
    public int momz;
    public int validcount;
    public int type;
    public MobjInfo info;
    public int tics;
    /**
     * Null once the thing reached {@code S_NULL}.
     */
    public @Nullable State state;
    public int flags;
    public int health;
    public int movedir;
    public int movecount;
    public @Nullable Mobj target;
    public int reactiontime;
    public int threshold;
    public @Nullable Player player;
    public int lastlook;
    public @Nullable Mobj tracer;

    /**
     * Index of the current state, as {@code state - states} in C.
     */
    public int stateIndex() {
        return state == null ? 0 : state.index();
    }

    @Override
    public void think(final Game g) {
        g.mobjs.P_MobjThinker(this);
    }
}
