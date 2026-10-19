// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine;

/**
 * A node of the thinker list ({@code thinker_t}).
 */
public abstract class Thinker {

    /**
     * The thinker has no function and is skipped (a plat or ceiling in stasis).
     */
    public static final int FN_NONE = 0;
    /**
     * The thinker runs every tic.
     */
    public static final int FN_ACTIVE = 1;
    /**
     * The thinker is unlinked on the next pass ({@code (actionf_v)(-1)}).
     */
    public static final int FN_REMOVED = -1;

    public Thinker prev;
    public Thinker next;
    public int function = FN_ACTIVE;

    /**
     * Runs one tic of this thinker.
     *
     * @param g the game owning it
     */
    public void think(final Game g) {
    }

    public final boolean isRemoved() {
        return function == FN_REMOVED;
    }
}
