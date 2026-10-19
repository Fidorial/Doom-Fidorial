// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine;

/**
 * A BSP node ({@code node_t}).
 */
public final class Node {

    public final int[][] bbox = new int[2][4];
    public final int[] children = new int[2];
    public int x;
    public int y;
    public int dx;
    public int dy;
}
