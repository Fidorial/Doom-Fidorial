// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine.play;

import fr.euphyllia.fidorial.doom.engine.Game;
import fr.euphyllia.fidorial.doom.engine.Level;
import fr.euphyllia.fidorial.doom.engine.Line;
import fr.euphyllia.fidorial.doom.engine.Mobj;
import fr.euphyllia.fidorial.doom.engine.Sector;
import fr.euphyllia.fidorial.doom.engine.Subsector;

import java.util.function.Predicate;

import static fr.euphyllia.fidorial.doom.engine.BBox.BOXBOTTOM;
import static fr.euphyllia.fidorial.doom.engine.BBox.BOXLEFT;
import static fr.euphyllia.fidorial.doom.engine.BBox.BOXRIGHT;
import static fr.euphyllia.fidorial.doom.engine.BBox.BOXTOP;
import static fr.euphyllia.fidorial.doom.engine.Defs.MAPBLOCKSHIFT;
import static fr.euphyllia.fidorial.doom.engine.Defs.MAPBLOCKSIZE;
import static fr.euphyllia.fidorial.doom.engine.Defs.MAPBTOFRAC;
import static fr.euphyllia.fidorial.doom.engine.Fixed.FRACBITS;
import static fr.euphyllia.fidorial.doom.engine.Fixed.FRACUNIT;
import static fr.euphyllia.fidorial.doom.engine.Fixed.MAXINT;
import static fr.euphyllia.fidorial.doom.engine.Fixed.div;
import static fr.euphyllia.fidorial.doom.engine.Fixed.mul;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_NOBLOCKMAP;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_NOSECTOR;

/**
 * Movement and collision utility functions ({@code p_maputl.c}).
 */
public final class MapUtil {

    public static final int PT_ADDLINES = 1;
    public static final int PT_ADDTHINGS = 2;
    public static final int PT_EARLYOUT = 4;
    public final DivLine trace = new DivLine();
    private final Game g;
    private final DivLine scratchDl = new DivLine();
    private final Predicate<Line> addLineIntercepts = this::PIT_AddLineIntercepts;
    private final Predicate<Mobj> addThingIntercepts = this::PIT_AddThingIntercepts;
    public int opentop;
    public int openbottom;
    public int openrange;
    public int lowfloor;
    private Intercept[] intercepts = new Intercept[128];
    private int interceptCount;
    private boolean earlyout;

    public MapUtil(final Game g) {
        this.g = g;
        for (int i = 0; i < intercepts.length; i++) {
            intercepts[i] = new Intercept();
        }
    }

    public static int P_AproxDistance(int dx, int dy) {
        dx = Math.abs(dx);
        dy = Math.abs(dy);
        if (dx < dy) {
            return dx + dy - (dx >> 1);
        }
        return dx + dy - (dy >> 1);
    }

    public static int P_PointOnLineSide(final int x, final int y, final Line line) {
        if (line.dx == 0) {
            if (x <= line.v1.x) {
                return line.dy > 0 ? 1 : 0;
            }
            return line.dy < 0 ? 1 : 0;
        }
        if (line.dy == 0) {
            if (y <= line.v1.y) {
                return line.dx < 0 ? 1 : 0;
            }
            return line.dx > 0 ? 1 : 0;
        }
        final int dx = x - line.v1.x;
        final int dy = y - line.v1.y;
        final int left = mul(line.dy >> FRACBITS, dx);
        final int right = mul(dy, line.dx >> FRACBITS);
        if (right < left) {
            return 0;
        }
        return 1;
    }

    public static int P_BoxOnLineSide(final int[] tmbox, final Line ld) {
        int p1;
        int p2;
        switch (ld.slopetype) {
            case Line.ST_HORIZONTAL -> {
                p1 = tmbox[BOXTOP] > ld.v1.y ? 1 : 0;
                p2 = tmbox[BOXBOTTOM] > ld.v1.y ? 1 : 0;
                if (ld.dx < 0) {
                    p1 ^= 1;
                    p2 ^= 1;
                }
            }
            case Line.ST_VERTICAL -> {
                p1 = tmbox[BOXRIGHT] < ld.v1.x ? 1 : 0;
                p2 = tmbox[BOXLEFT] < ld.v1.x ? 1 : 0;
                if (ld.dy < 0) {
                    p1 ^= 1;
                    p2 ^= 1;
                }
            }
            case Line.ST_POSITIVE -> {
                p1 = P_PointOnLineSide(tmbox[BOXLEFT], tmbox[BOXTOP], ld);
                p2 = P_PointOnLineSide(tmbox[BOXRIGHT], tmbox[BOXBOTTOM], ld);
            }
            default -> {
                p1 = P_PointOnLineSide(tmbox[BOXRIGHT], tmbox[BOXTOP], ld);
                p2 = P_PointOnLineSide(tmbox[BOXLEFT], tmbox[BOXBOTTOM], ld);
            }
        }
        if (p1 == p2) {
            return p1;
        }
        return -1;
    }

    public static int P_PointOnDivlineSide(final int x, final int y, final DivLine line) {
        if (line.dx == 0) {
            if (x <= line.x) {
                return line.dy > 0 ? 1 : 0;
            }
            return line.dy < 0 ? 1 : 0;
        }
        if (line.dy == 0) {
            if (y <= line.y) {
                return line.dx < 0 ? 1 : 0;
            }
            return line.dx > 0 ? 1 : 0;
        }
        final int dx = x - line.x;
        final int dy = y - line.y;
        if (((line.dy ^ line.dx ^ dx ^ dy) & 0x80000000) != 0) {
            if (((line.dy ^ dx) & 0x80000000) != 0) {
                return 1;
            }
            return 0;
        }
        final int left = mul(line.dy >> 8, dx >> 8);
        final int right = mul(dy >> 8, line.dx >> 8);
        if (right < left) {
            return 0;
        }
        return 1;
    }

    public static void P_MakeDivline(final Line li, final DivLine dl) {
        dl.x = li.v1.x;
        dl.y = li.v1.y;
        dl.dx = li.dx;
        dl.dy = li.dy;
    }

    public static int P_InterceptVector(final DivLine v2, final DivLine v1) {
        final int den = mul(v1.dy >> 8, v2.dx) - mul(v1.dx >> 8, v2.dy);
        if (den == 0) {
            return 0;
        }
        final int num = mul((v1.x - v2.x) >> 8, v1.dy) + mul((v2.y - v1.y) >> 8, v1.dx);
        return div(num, den);
    }

    public void P_LineOpening(final Line linedef) {
        if (linedef.sidenum[1] == -1) {
            openrange = 0;
            return;
        }
        final Sector front = linedef.frontsector;
        final Sector back = linedef.backsector;
        if (front.ceilingheight < back.ceilingheight) {
            opentop = front.ceilingheight;
        } else {
            opentop = back.ceilingheight;
        }
        if (front.floorheight > back.floorheight) {
            openbottom = front.floorheight;
            lowfloor = back.floorheight;
        } else {
            openbottom = back.floorheight;
            lowfloor = front.floorheight;
        }
        openrange = opentop - openbottom;
    }

    public void P_UnsetThingPosition(final Mobj thing) {
        final Level level = g.level;
        if ((thing.flags & MF_NOSECTOR) == 0) {
            if (thing.snext != null) {
                thing.snext.sprev = thing.sprev;
            }
            if (thing.sprev != null) {
                thing.sprev.snext = thing.snext;
            } else {
                thing.subsector.sector.thinglist = thing.snext;
            }
        }
        if ((thing.flags & MF_NOBLOCKMAP) == 0) {
            if (thing.bnext != null) {
                thing.bnext.bprev = thing.bprev;
            }
            if (thing.bprev != null) {
                thing.bprev.bnext = thing.bnext;
            } else {
                final int blockx = (thing.x - level.bmaporgx) >> MAPBLOCKSHIFT;
                final int blocky = (thing.y - level.bmaporgy) >> MAPBLOCKSHIFT;
                if (blockx >= 0 && blockx < level.bmapwidth && blocky >= 0 && blocky < level.bmapheight) {
                    level.blocklinks[blocky * level.bmapwidth + blockx] = thing.bnext;
                }
            }
        }
    }

    public void P_SetThingPosition(final Mobj thing) {
        final Level level = g.level;
        final Subsector ss = level.pointInSubsector(thing.x, thing.y);
        thing.subsector = ss;
        if ((thing.flags & MF_NOSECTOR) == 0) {
            final Sector sec = ss.sector;
            thing.sprev = null;
            thing.snext = sec.thinglist;
            if (sec.thinglist != null) {
                sec.thinglist.sprev = thing;
            }
            sec.thinglist = thing;
        }
        if ((thing.flags & MF_NOBLOCKMAP) == 0) {
            final int blockx = (thing.x - level.bmaporgx) >> MAPBLOCKSHIFT;
            final int blocky = (thing.y - level.bmaporgy) >> MAPBLOCKSHIFT;
            if (blockx >= 0 && blockx < level.bmapwidth && blocky >= 0 && blocky < level.bmapheight) {
                final int link = blocky * level.bmapwidth + blockx;
                thing.bprev = null;
                thing.bnext = level.blocklinks[link];
                if (level.blocklinks[link] != null) {
                    level.blocklinks[link].bprev = thing;
                }
                level.blocklinks[link] = thing;
            } else {
                thing.bnext = thing.bprev = null;
            }
        }
    }

    public boolean P_BlockLinesIterator(final int x, final int y, final Predicate<Line> func) {
        final Level level = g.level;
        if (x < 0 || y < 0 || x >= level.bmapwidth || y >= level.bmapheight) {
            return true;
        }
        final int[] lump = level.blockmaplump;
        int list = lump[Level.BLOCKMAP_OFFSET + y * level.bmapwidth + x];
        for (; list < lump.length && lump[list] != 0xFFFF; list++) {
            final int lineIndex = lump[list];
            if (lineIndex >= level.lines.length) {
                continue;
            }
            final Line ld = level.lines[lineIndex];
            if (ld.validcount == g.validcount) {
                continue;
            }
            ld.validcount = g.validcount;
            if (!func.test(ld)) {
                return false;
            }
        }
        return true;
    }

    public boolean P_BlockThingsIterator(final int x, final int y, final Predicate<Mobj> func) {
        final Level level = g.level;
        if (x < 0 || y < 0 || x >= level.bmapwidth || y >= level.bmapheight) {
            return true;
        }
        for (Mobj mobj = level.blocklinks[y * level.bmapwidth + x]; mobj != null; mobj = mobj.bnext) {
            if (!func.test(mobj)) {
                return false;
            }
        }
        return true;
    }

    private Intercept nextIntercept() {
        if (interceptCount == intercepts.length) {
            final Intercept[] grown = new Intercept[intercepts.length * 2];
            System.arraycopy(intercepts, 0, grown, 0, intercepts.length);
            for (int i = intercepts.length; i < grown.length; i++) {
                grown[i] = new Intercept();
            }
            intercepts = grown;
        }
        return intercepts[interceptCount++];
    }

    private boolean PIT_AddLineIntercepts(final Line ld) {
        final int s1;
        final int s2;
        if (trace.dx > FRACUNIT * 16 || trace.dy > FRACUNIT * 16 || trace.dx < -FRACUNIT * 16 || trace.dy < -FRACUNIT * 16) {
            s1 = P_PointOnDivlineSide(ld.v1.x, ld.v1.y, trace);
            s2 = P_PointOnDivlineSide(ld.v2.x, ld.v2.y, trace);
        } else {
            s1 = P_PointOnLineSide(trace.x, trace.y, ld);
            s2 = P_PointOnLineSide(trace.x + trace.dx, trace.y + trace.dy, ld);
        }
        if (s1 == s2) {
            return true;
        }
        P_MakeDivline(ld, scratchDl);
        final int frac = P_InterceptVector(trace, scratchDl);
        if (frac < 0) {
            return true;
        }
        if (earlyout && frac < FRACUNIT && ld.backsector == null) {
            return false;
        }
        final Intercept in = nextIntercept();
        in.frac = frac;
        in.isaline = true;
        in.line = ld;
        in.thing = null;
        return true;
    }

    private boolean PIT_AddThingIntercepts(final Mobj thing) {
        final int x1;
        final int y1;
        final int x2;
        final int y2;
        final boolean tracepositive = (trace.dx ^ trace.dy) > 0;
        if (tracepositive) {
            x1 = thing.x - thing.radius;
            y1 = thing.y + thing.radius;
            x2 = thing.x + thing.radius;
            y2 = thing.y - thing.radius;
        } else {
            x1 = thing.x - thing.radius;
            y1 = thing.y - thing.radius;
            x2 = thing.x + thing.radius;
            y2 = thing.y + thing.radius;
        }
        final int s1 = P_PointOnDivlineSide(x1, y1, trace);
        final int s2 = P_PointOnDivlineSide(x2, y2, trace);
        if (s1 == s2) {
            return true;
        }
        final DivLine dl = scratchDl;
        dl.x = x1;
        dl.y = y1;
        dl.dx = x2 - x1;
        dl.dy = y2 - y1;
        final int frac = P_InterceptVector(trace, dl);
        if (frac < 0) {
            return true;
        }
        final Intercept in = nextIntercept();
        in.frac = frac;
        in.isaline = false;
        in.thing = thing;
        in.line = null;
        return true;
    }

    private boolean P_TraverseIntercepts(final Predicate<Intercept> func, final int maxfrac) {
        int count = interceptCount;
        Intercept in = null;
        while (count-- > 0) {
            int dist = MAXINT;
            for (int i = 0; i < interceptCount; i++) {
                final Intercept scan = intercepts[i];
                if (scan.frac < dist) {
                    dist = scan.frac;
                    in = scan;
                }
            }
            if (dist > maxfrac) {
                return true;
            }
            if (!func.test(in)) {
                return false;
            }
            in.frac = MAXINT;
        }
        return true;
    }

    public boolean P_PathTraverse(int x1, int y1, int x2, int y2, final int flags, final Predicate<Intercept> trav) {
        final Level level = g.level;
        earlyout = (flags & PT_EARLYOUT) != 0;
        g.validcount++;
        interceptCount = 0;
        if (((x1 - level.bmaporgx) & (MAPBLOCKSIZE - 1)) == 0) {
            x1 += FRACUNIT;
        }
        if (((y1 - level.bmaporgy) & (MAPBLOCKSIZE - 1)) == 0) {
            y1 += FRACUNIT;
        }
        trace.x = x1;
        trace.y = y1;
        trace.dx = x2 - x1;
        trace.dy = y2 - y1;
        x1 -= level.bmaporgx;
        y1 -= level.bmaporgy;
        final int xt1 = x1 >> MAPBLOCKSHIFT;
        final int yt1 = y1 >> MAPBLOCKSHIFT;
        x2 -= level.bmaporgx;
        y2 -= level.bmaporgy;
        final int xt2 = x2 >> MAPBLOCKSHIFT;
        final int yt2 = y2 >> MAPBLOCKSHIFT;
        final int mapxstep;
        final int mapystep;
        int partial;
        final int xstep;
        final int ystep;
        if (xt2 > xt1) {
            mapxstep = 1;
            partial = FRACUNIT - ((x1 >> MAPBTOFRAC) & (FRACUNIT - 1));
            ystep = div(y2 - y1, Math.abs(x2 - x1));
        } else if (xt2 < xt1) {
            mapxstep = -1;
            partial = (x1 >> MAPBTOFRAC) & (FRACUNIT - 1);
            ystep = div(y2 - y1, Math.abs(x2 - x1));
        } else {
            mapxstep = 0;
            partial = FRACUNIT;
            ystep = 256 * FRACUNIT;
        }
        int yintercept = (y1 >> MAPBTOFRAC) + mul(partial, ystep);
        if (yt2 > yt1) {
            mapystep = 1;
            partial = FRACUNIT - ((y1 >> MAPBTOFRAC) & (FRACUNIT - 1));
            xstep = div(x2 - x1, Math.abs(y2 - y1));
        } else if (yt2 < yt1) {
            mapystep = -1;
            partial = (y1 >> MAPBTOFRAC) & (FRACUNIT - 1);
            xstep = div(x2 - x1, Math.abs(y2 - y1));
        } else {
            mapystep = 0;
            partial = FRACUNIT;
            xstep = 256 * FRACUNIT;
        }
        int xintercept = (x1 >> MAPBTOFRAC) + mul(partial, xstep);
        int mapx = xt1;
        int mapy = yt1;
        for (int count = 0; count < 64; count++) {
            if ((flags & PT_ADDLINES) != 0) {
                if (!P_BlockLinesIterator(mapx, mapy, addLineIntercepts)) {
                    return false;
                }
            }
            if ((flags & PT_ADDTHINGS) != 0) {
                if (!P_BlockThingsIterator(mapx, mapy, addThingIntercepts)) {
                    return false;
                }
            }
            if (mapx == xt2 && mapy == yt2) {
                break;
            }
            if ((yintercept >> FRACBITS) == mapy) {
                yintercept += ystep;
                mapx += mapxstep;
            } else if ((xintercept >> FRACBITS) == mapx) {
                xintercept += xstep;
                mapy += mapystep;
            }
        }
        return P_TraverseIntercepts(trav, FRACUNIT);
    }
}
