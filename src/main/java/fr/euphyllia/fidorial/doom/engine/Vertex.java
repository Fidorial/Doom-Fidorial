// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine;

/**
 * A map vertex in fixed point ({@code vertex_t}).
 */
public final class Vertex {

    public int x;
    public int y;

    public Vertex(final int x, final int y) {
        this.x = x;
        this.y = y;
    }
}
