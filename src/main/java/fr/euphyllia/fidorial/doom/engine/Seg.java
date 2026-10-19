// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine;

import org.jspecify.annotations.Nullable;

/**
 * A BSP line segment ({@code seg_t}).
 */
public final class Seg {

    public Vertex v1;
    public Vertex v2;
    public int offset;
    public int angle;
    public Side sidedef;
    public Line linedef;
    public Sector frontsector;
    public @Nullable Sector backsector;
}
