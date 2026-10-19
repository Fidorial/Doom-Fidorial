// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine.play;

import fr.euphyllia.fidorial.doom.engine.Game;
import fr.euphyllia.fidorial.doom.engine.Level;
import fr.euphyllia.fidorial.doom.engine.Line;
import fr.euphyllia.fidorial.doom.engine.Mobj;
import fr.euphyllia.fidorial.doom.engine.Node;
import fr.euphyllia.fidorial.doom.engine.Sector;
import fr.euphyllia.fidorial.doom.engine.Seg;
import fr.euphyllia.fidorial.doom.engine.Subsector;
import fr.euphyllia.fidorial.doom.engine.Vertex;

import static fr.euphyllia.fidorial.doom.engine.Defs.ML_TWOSIDED;
import static fr.euphyllia.fidorial.doom.engine.Defs.NF_SUBSECTOR;
import static fr.euphyllia.fidorial.doom.engine.Fixed.FRACBITS;
import static fr.euphyllia.fidorial.doom.engine.Fixed.div;
import static fr.euphyllia.fidorial.doom.engine.Fixed.mul;

/**
 * Line of sight checking ({@code p_sight.c}).
 */
public final class Sight {

    private final Game g;
    private final DivLine strace = new DivLine();
    private final DivLine divl = new DivLine();
    private int sightzstart;
    private int topslope;
    private int bottomslope;
    private int t2x;
    private int t2y;

    public Sight(final Game g) {
        this.g = g;
    }

    private static int P_DivlineSide(final int x, final int y, final int nx, final int ny, final int ndx, final int ndy) {
        if (ndx == 0) {
            if (x == nx) {
                return 2;
            }
            if (x <= nx) {
                return ndy > 0 ? 1 : 0;
            }
            return ndy < 0 ? 1 : 0;
        }
        if (ndy == 0) {
            // vanilla compares x against node y here
            if (x == ny) {
                return 2;
            }
            if (y <= ny) {
                return ndx < 0 ? 1 : 0;
            }
            return ndx > 0 ? 1 : 0;
        }
        final int dx = x - nx;
        final int dy = y - ny;
        final int left = (ndy >> FRACBITS) * (dx >> FRACBITS);
        final int right = (dy >> FRACBITS) * (ndx >> FRACBITS);
        if (right < left) {
            return 0;
        }
        if (left == right) {
            return 2;
        }
        return 1;
    }

    private static int P_DivlineSide(final int x, final int y, final DivLine node) {
        return P_DivlineSide(x, y, node.x, node.y, node.dx, node.dy);
    }

    private static int P_DivlineSide(final int x, final int y, final Node node) {
        return P_DivlineSide(x, y, node.x, node.y, node.dx, node.dy);
    }

    private static int P_InterceptVector2(final DivLine v2, final DivLine v1) {
        final int den = mul(v1.dy >> 8, v2.dx) - mul(v1.dx >> 8, v2.dy);
        if (den == 0) {
            return 0;
        }
        final int num = mul((v1.x - v2.x) >> 8, v1.dy) + mul((v2.y - v1.y) >> 8, v1.dx);
        return div(num, den);
    }

    private boolean P_CrossSubsector(final int num) {
        final Level level = g.level;
        if (num >= level.subsectors.length) {
            throw new IllegalStateException("P_CrossSubsector: ss " + num + " with numss = " + level.subsectors.length);
        }
        final Subsector sub = level.subsectors[num];
        int count = sub.numlines;
        int segIndex = sub.firstline;
        for (; count > 0; segIndex++, count--) {
            final Seg seg = level.segs[segIndex];
            final Line line = seg.linedef;
            if (line.validcount == g.validcount) {
                continue;
            }
            line.validcount = g.validcount;
            final Vertex v1 = line.v1;
            final Vertex v2 = line.v2;
            int s1 = P_DivlineSide(v1.x, v1.y, strace);
            int s2 = P_DivlineSide(v2.x, v2.y, strace);
            if (s1 == s2) {
                continue;
            }
            divl.x = v1.x;
            divl.y = v1.y;
            divl.dx = v2.x - v1.x;
            divl.dy = v2.y - v1.y;
            s1 = P_DivlineSide(strace.x, strace.y, divl);
            s2 = P_DivlineSide(t2x, t2y, divl);
            if (s1 == s2) {
                continue;
            }
            if ((line.flags & ML_TWOSIDED) == 0) {
                return false;
            }
            final Sector front = seg.frontsector;
            final Sector back = seg.backsector;
            if (back == null) {
                return false;
            }
            if (front.floorheight == back.floorheight && front.ceilingheight == back.ceilingheight) {
                continue;
            }
            final int opentop = Math.min(front.ceilingheight, back.ceilingheight);
            final int openbottom = Math.max(front.floorheight, back.floorheight);
            if (openbottom >= opentop) {
                return false;
            }
            final int frac = P_InterceptVector2(strace, divl);
            if (front.floorheight != back.floorheight) {
                final int slope = div(openbottom - sightzstart, frac);
                if (slope > bottomslope) {
                    bottomslope = slope;
                }
            }
            if (front.ceilingheight != back.ceilingheight) {
                final int slope = div(opentop - sightzstart, frac);
                if (slope < topslope) {
                    topslope = slope;
                }
            }
            if (topslope <= bottomslope) {
                return false;
            }
        }
        return true;
    }

    private boolean P_CrossBSPNode(final int bspnum) {
        if ((bspnum & NF_SUBSECTOR) != 0) {
            if (bspnum == -1) {
                return P_CrossSubsector(0);
            }
            return P_CrossSubsector(bspnum & ~NF_SUBSECTOR);
        }
        final Node bsp = g.level.nodes[bspnum];
        int side = P_DivlineSide(strace.x, strace.y, bsp);
        if (side == 2) {
            side = 0;
        }
        if (!P_CrossBSPNode(bsp.children[side])) {
            return false;
        }
        if (side == P_DivlineSide(t2x, t2y, bsp)) {
            return true;
        }
        return P_CrossBSPNode(bsp.children[side ^ 1]);
    }

    public boolean P_CheckSight(final Mobj t1, final Mobj t2) {
        final Level level = g.level;
        final int s1 = t1.subsector.sector.id;
        final int s2 = t2.subsector.sector.id;
        final int pnum = s1 * level.sectors.length + s2;
        final int bytenum = pnum >> 3;
        final int bitnum = 1 << (pnum & 7);
        if (bytenum < level.rejectmatrix.length && (level.rejectmatrix[bytenum] & bitnum) != 0) {
            return false;
        }
        g.validcount++;
        sightzstart = t1.z + t1.height - (t1.height >> 2);
        topslope = (t2.z + t2.height) - sightzstart;
        bottomslope = t2.z - sightzstart;
        strace.x = t1.x;
        strace.y = t1.y;
        t2x = t2.x;
        t2y = t2.y;
        strace.dx = t2.x - t1.x;
        strace.dy = t2.y - t1.y;
        return P_CrossBSPNode(level.nodes.length - 1);
    }
}
