// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine;

import org.jspecify.annotations.Nullable;

import static fr.euphyllia.fidorial.doom.engine.Defs.NF_SUBSECTOR;
import static fr.euphyllia.fidorial.doom.engine.Fixed.FRACBITS;
import static fr.euphyllia.fidorial.doom.engine.Fixed.mul;

/**
 * The geometry of the current level ({@code p_setup.c} globals).
 */
public final class Level {

    /**
     * Offset of the block offsets in {@link #blockmaplump}.
     */
    public static final int BLOCKMAP_OFFSET = 4;
    public Vertex[] vertexes = new Vertex[0];
    public Seg[] segs = new Seg[0];
    public Sector[] sectors = new Sector[0];
    public Subsector[] subsectors = new Subsector[0];
    public Node[] nodes = new Node[0];
    public Line[] lines = new Line[0];
    public Side[] sides = new Side[0];
    public int bmapwidth;
    public int bmapheight;
    /**
     * The whole BLOCKMAP lump as unsigned 16-bit values.
     */
    public int[] blockmaplump = new int[0];
    public int bmaporgx;
    public int bmaporgy;
    public @Nullable Mobj[] blocklinks = new Mobj[0];
    public byte[] rejectmatrix = new byte[0];

    /**
     * {@code R_PointOnSide} against a BSP node.
     */
    public static int pointOnSide(final int x, final int y, final Node node) {
        if (node.dx == 0) {
            if (x <= node.x) {
                return node.dy > 0 ? 1 : 0;
            }
            return node.dy < 0 ? 1 : 0;
        }
        if (node.dy == 0) {
            if (y <= node.y) {
                return node.dx < 0 ? 1 : 0;
            }
            return node.dx > 0 ? 1 : 0;
        }
        final int dx = x - node.x;
        final int dy = y - node.y;
        if (((node.dy ^ node.dx ^ dx ^ dy) & 0x80000000) != 0) {
            if (((node.dy ^ dx) & 0x80000000) != 0) {
                return 1;
            }
            return 0;
        }
        final int left = mul(node.dy >> FRACBITS, dx);
        final int right = mul(dy, node.dx >> FRACBITS);
        if (right < left) {
            return 0;
        }
        return 1;
    }

    /**
     * {@code R_PointInSubsector}.
     */
    public Subsector pointInSubsector(final int x, final int y) {
        if (nodes.length == 0) {
            return subsectors[0];
        }
        int nodenum = nodes.length - 1;
        while ((nodenum & NF_SUBSECTOR) == 0) {
            final Node node = nodes[nodenum];
            final int side = pointOnSide(x, y, node);
            nodenum = node.children[side];
        }
        return subsectors[nodenum & ~NF_SUBSECTOR];
    }
}
