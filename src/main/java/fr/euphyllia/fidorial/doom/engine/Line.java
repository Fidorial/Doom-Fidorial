// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine;

import org.jspecify.annotations.Nullable;

/**
 * A linedef ({@code line_t}).
 */
public final class Line {

    public static final int ST_HORIZONTAL = 0;
    public static final int ST_VERTICAL = 1;
    public static final int ST_POSITIVE = 2;
    public static final int ST_NEGATIVE = 3;

    public final int id;
    public final short[] sidenum = new short[2];
    public final int[] bbox = new int[4];
    public Vertex v1;
    public Vertex v2;
    public int dx;
    public int dy;
    public short flags;
    public short special;
    public short tag;
    public int slopetype;
    public @Nullable Sector frontsector;
    public @Nullable Sector backsector;
    public int validcount;
    public @Nullable Thinker specialdata;

    public Line(final int id) {
        this.id = id;
    }
}
