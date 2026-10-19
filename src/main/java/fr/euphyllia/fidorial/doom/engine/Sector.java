// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine;

import org.jspecify.annotations.Nullable;

/**
 * A sector ({@code sector_t}).
 */
public final class Sector {

    public final int id;
    public final int[] blockbox = new int[4];
    public final DegenMobj soundorg = new DegenMobj();
    public int floorheight;
    public int ceilingheight;
    public short floorpic;
    public short ceilingpic;
    public short lightlevel;
    public short special;
    public short tag;
    public int soundtraversed;
    public @Nullable Mobj soundtarget;
    public int validcount;
    public @Nullable Mobj thinglist;
    public @Nullable Thinker specialdata;
    public int linecount;
    public Line[] lines = new Line[0];

    public Sector(final int id) {
        this.id = id;
    }
}
