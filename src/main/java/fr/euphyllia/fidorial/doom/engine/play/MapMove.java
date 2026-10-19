// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine.play;

import fr.euphyllia.fidorial.doom.engine.Angles;
import fr.euphyllia.fidorial.doom.engine.Game;
import fr.euphyllia.fidorial.doom.engine.Level;
import fr.euphyllia.fidorial.doom.engine.Line;
import fr.euphyllia.fidorial.doom.engine.Mobj;
import fr.euphyllia.fidorial.doom.engine.Player;
import fr.euphyllia.fidorial.doom.engine.Sector;
import fr.euphyllia.fidorial.doom.engine.Subsector;
import org.jspecify.annotations.Nullable;

import java.util.function.Predicate;

import static fr.euphyllia.fidorial.doom.engine.BBox.BOXBOTTOM;
import static fr.euphyllia.fidorial.doom.engine.BBox.BOXLEFT;
import static fr.euphyllia.fidorial.doom.engine.BBox.BOXRIGHT;
import static fr.euphyllia.fidorial.doom.engine.BBox.BOXTOP;
import static fr.euphyllia.fidorial.doom.engine.Defs.MAPBLOCKSHIFT;
import static fr.euphyllia.fidorial.doom.engine.Defs.MAXRADIUS;
import static fr.euphyllia.fidorial.doom.engine.Defs.ML_BLOCKING;
import static fr.euphyllia.fidorial.doom.engine.Defs.ML_BLOCKMONSTERS;
import static fr.euphyllia.fidorial.doom.engine.Defs.ML_TWOSIDED;
import static fr.euphyllia.fidorial.doom.engine.Defs.USERANGE;
import static fr.euphyllia.fidorial.doom.engine.Fixed.FRACBITS;
import static fr.euphyllia.fidorial.doom.engine.Fixed.FRACUNIT;
import static fr.euphyllia.fidorial.doom.engine.Fixed.div;
import static fr.euphyllia.fidorial.doom.engine.Fixed.mul;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_DROPOFF;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_DROPPED;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_FLOAT;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_MISSILE;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_NOBLOOD;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_NOCLIP;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_PICKUP;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_SHOOTABLE;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_SKULLFLY;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_SOLID;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_SPECIAL;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_TELEPORT;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_noway;
import static fr.euphyllia.fidorial.doom.engine.Tables.ANG180;
import static fr.euphyllia.fidorial.doom.engine.Tables.ANGLETOFINESHIFT;
import static fr.euphyllia.fidorial.doom.engine.Tables.finecosine;
import static fr.euphyllia.fidorial.doom.engine.Tables.finesine;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_BLOOD;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_BRUISER;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_CYBORG;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_KNIGHT;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_PLAYER;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_SPIDER;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_GIBS;
import static fr.euphyllia.fidorial.doom.engine.play.MapUtil.PT_ADDLINES;
import static fr.euphyllia.fidorial.doom.engine.play.MapUtil.PT_ADDTHINGS;
import static fr.euphyllia.fidorial.doom.engine.play.MapUtil.P_AproxDistance;
import static fr.euphyllia.fidorial.doom.engine.play.MapUtil.P_BoxOnLineSide;
import static fr.euphyllia.fidorial.doom.engine.play.MapUtil.P_PointOnLineSide;

/**
 * Movement, collision handling, shooting and aiming ({@code p_map.c}).
 */
public final class MapMove {

    public final int[] tmbbox = new int[4];
    private final Game g;
    private final Predicate<Mobj> stompThing = this::PIT_StompThing;
    private final Predicate<Line> checkLine = this::PIT_CheckLine;
    private final Predicate<Mobj> checkThing = this::PIT_CheckThing;
    private final Predicate<Intercept> slideTraverse = this::PTR_SlideTraverse;
    private final Predicate<Intercept> aimTraverse = this::PTR_AimTraverse;
    private final Predicate<Intercept> shootTraverse = this::PTR_ShootTraverse;
    private final Predicate<Intercept> useTraverse = this::PTR_UseTraverse;
    private final Predicate<Mobj> radiusAttack = this::PIT_RadiusAttack;
    private final Predicate<Mobj> changeSector = this::PIT_ChangeSector;
    public Mobj tmthing;
    public int tmflags;
    public int tmx;
    public int tmy;
    public boolean floatok;
    public int tmfloorz;
    public int tmceilingz;
    public int tmdropoffz;
    public @Nullable Line ceilingline;
    public @Nullable Mobj linetarget;
    public int attackrange;
    private Line[] spechit = new Line[8];
    private int numspechit;
    private int bestslidefrac;
    private int secondslidefrac;
    private @Nullable Line bestslideline;
    private @Nullable Line secondslideline;
    private Mobj slidemo;
    private int tmxmove;
    private int tmymove;
    private Mobj shootthing;
    private int shootz;
    private int la_damage;
    private int aimslope;
    private int topslope;
    private int bottomslope;
    private Mobj usething;
    private Mobj bombsource;
    private Mobj bombspot;
    private int bombdamage;
    private boolean crushchange;
    private boolean nofit;

    public MapMove(final Game g) {
        this.g = g;
    }

    /**
     * {@return how many special lines the last position check crossed}
     */
    public int numSpecHit() {
        return numspechit;
    }

    /**
     * {@return the count after decrementing it, as {@code numspechit--} leaves it}
     */
    public int decrementSpecHit() {
        return --numspechit;
    }

    public Line specHit(final int index) {
        return spechit[index];
    }

    private boolean PIT_StompThing(final Mobj thing) {
        if ((thing.flags & MF_SHOOTABLE) == 0) {
            return true;
        }
        final int blockdist = thing.radius + tmthing.radius;
        if (Math.abs(thing.x - tmx) >= blockdist || Math.abs(thing.y - tmy) >= blockdist) {
            return true;
        }
        if (thing == tmthing) {
            return true;
        }
        if (tmthing.player == null && g.gamemap != 30) {
            return false;
        }
        g.inter.P_DamageMobj(thing, tmthing, tmthing, 10000);
        return true;
    }

    public boolean P_TeleportMove(final Mobj thing, final int x, final int y) {
        final Level level = g.level;
        tmthing = thing;
        tmflags = thing.flags;
        tmx = x;
        tmy = y;
        tmbbox[BOXTOP] = y + tmthing.radius;
        tmbbox[BOXBOTTOM] = y - tmthing.radius;
        tmbbox[BOXRIGHT] = x + tmthing.radius;
        tmbbox[BOXLEFT] = x - tmthing.radius;
        final Subsector newsubsec = level.pointInSubsector(x, y);
        ceilingline = null;
        tmfloorz = tmdropoffz = newsubsec.sector.floorheight;
        tmceilingz = newsubsec.sector.ceilingheight;
        g.validcount++;
        numspechit = 0;
        final int xl = (tmbbox[BOXLEFT] - level.bmaporgx - MAXRADIUS) >> MAPBLOCKSHIFT;
        final int xh = (tmbbox[BOXRIGHT] - level.bmaporgx + MAXRADIUS) >> MAPBLOCKSHIFT;
        final int yl = (tmbbox[BOXBOTTOM] - level.bmaporgy - MAXRADIUS) >> MAPBLOCKSHIFT;
        final int yh = (tmbbox[BOXTOP] - level.bmaporgy + MAXRADIUS) >> MAPBLOCKSHIFT;
        for (int bx = xl; bx <= xh; bx++) {
            for (int by = yl; by <= yh; by++) {
                if (!g.mapUtil.P_BlockThingsIterator(bx, by, stompThing)) {
                    return false;
                }
            }
        }
        g.mapUtil.P_UnsetThingPosition(thing);
        thing.floorz = tmfloorz;
        thing.ceilingz = tmceilingz;
        thing.x = x;
        thing.y = y;
        g.mapUtil.P_SetThingPosition(thing);
        return true;
    }

    private boolean PIT_CheckLine(final Line ld) {
        if (tmbbox[BOXRIGHT] <= ld.bbox[BOXLEFT] || tmbbox[BOXLEFT] >= ld.bbox[BOXRIGHT]
                || tmbbox[BOXTOP] <= ld.bbox[BOXBOTTOM] || tmbbox[BOXBOTTOM] >= ld.bbox[BOXTOP]) {
            return true;
        }
        if (P_BoxOnLineSide(tmbbox, ld) != -1) {
            return true;
        }
        if (ld.backsector == null) {
            return false;
        }
        if ((tmthing.flags & MF_MISSILE) == 0) {
            if ((ld.flags & ML_BLOCKING) != 0) {
                return false;
            }
            if (tmthing.player == null && (ld.flags & ML_BLOCKMONSTERS) != 0) {
                return false;
            }
        }
        final MapUtil mu = g.mapUtil;
        mu.P_LineOpening(ld);
        if (mu.opentop < tmceilingz) {
            tmceilingz = mu.opentop;
            ceilingline = ld;
        }
        if (mu.openbottom > tmfloorz) {
            tmfloorz = mu.openbottom;
        }
        if (mu.lowfloor < tmdropoffz) {
            tmdropoffz = mu.lowfloor;
        }
        if (ld.special != 0) {
            if (numspechit == spechit.length) {
                final Line[] grown = new Line[spechit.length * 2];
                System.arraycopy(spechit, 0, grown, 0, spechit.length);
                spechit = grown;
            }
            spechit[numspechit] = ld;
            numspechit++;
        }
        return true;
    }

    private boolean PIT_CheckThing(final Mobj thing) {
        if ((thing.flags & (MF_SOLID | MF_SPECIAL | MF_SHOOTABLE)) == 0) {
            return true;
        }
        final int blockdist = thing.radius + tmthing.radius;
        if (Math.abs(thing.x - tmx) >= blockdist || Math.abs(thing.y - tmy) >= blockdist) {
            return true;
        }
        if (thing == tmthing) {
            return true;
        }
        if ((tmthing.flags & MF_SKULLFLY) != 0) {
            final int damage = ((g.random.P_Random() % 8) + 1) * tmthing.info.damage();
            g.inter.P_DamageMobj(thing, tmthing, tmthing, damage);
            tmthing.flags &= ~MF_SKULLFLY;
            tmthing.momx = tmthing.momy = tmthing.momz = 0;
            g.mobjs.P_SetMobjState(tmthing, tmthing.info.spawnstate());
            return false;
        }
        if ((tmthing.flags & MF_MISSILE) != 0) {
            if (tmthing.z > thing.z + thing.height) {
                return true;
            }
            if (tmthing.z + tmthing.height < thing.z) {
                return true;
            }
            final Mobj target = tmthing.target;
            if (target != null && (target.type == thing.type
                    || (target.type == MT_KNIGHT && thing.type == MT_BRUISER)
                    || (target.type == MT_BRUISER && thing.type == MT_KNIGHT))) {
                if (thing == target) {
                    return true;
                }
                if (thing.type != MT_PLAYER) {
                    return false;
                }
            }
            if ((thing.flags & MF_SHOOTABLE) == 0) {
                return (thing.flags & MF_SOLID) == 0;
            }
            final int damage = ((g.random.P_Random() % 8) + 1) * tmthing.info.damage();
            g.inter.P_DamageMobj(thing, tmthing, tmthing.target, damage);
            return false;
        }
        if ((thing.flags & MF_SPECIAL) != 0) {
            final boolean solid = (thing.flags & MF_SOLID) != 0;
            if ((tmflags & MF_PICKUP) != 0) {
                g.inter.P_TouchSpecialThing(thing, tmthing);
            }
            return !solid;
        }
        return (thing.flags & MF_SOLID) == 0;
    }

    public boolean P_CheckPosition(final Mobj thing, final int x, final int y) {
        final Level level = g.level;
        tmthing = thing;
        tmflags = thing.flags;
        tmx = x;
        tmy = y;
        tmbbox[BOXTOP] = y + tmthing.radius;
        tmbbox[BOXBOTTOM] = y - tmthing.radius;
        tmbbox[BOXRIGHT] = x + tmthing.radius;
        tmbbox[BOXLEFT] = x - tmthing.radius;
        final Subsector newsubsec = level.pointInSubsector(x, y);
        ceilingline = null;
        tmfloorz = tmdropoffz = newsubsec.sector.floorheight;
        tmceilingz = newsubsec.sector.ceilingheight;
        g.validcount++;
        numspechit = 0;
        if ((tmflags & MF_NOCLIP) != 0) {
            return true;
        }
        int xl = (tmbbox[BOXLEFT] - level.bmaporgx - MAXRADIUS) >> MAPBLOCKSHIFT;
        int xh = (tmbbox[BOXRIGHT] - level.bmaporgx + MAXRADIUS) >> MAPBLOCKSHIFT;
        int yl = (tmbbox[BOXBOTTOM] - level.bmaporgy - MAXRADIUS) >> MAPBLOCKSHIFT;
        int yh = (tmbbox[BOXTOP] - level.bmaporgy + MAXRADIUS) >> MAPBLOCKSHIFT;
        for (int bx = xl; bx <= xh; bx++) {
            for (int by = yl; by <= yh; by++) {
                if (!g.mapUtil.P_BlockThingsIterator(bx, by, checkThing)) {
                    return false;
                }
            }
        }
        xl = (tmbbox[BOXLEFT] - level.bmaporgx) >> MAPBLOCKSHIFT;
        xh = (tmbbox[BOXRIGHT] - level.bmaporgx) >> MAPBLOCKSHIFT;
        yl = (tmbbox[BOXBOTTOM] - level.bmaporgy) >> MAPBLOCKSHIFT;
        yh = (tmbbox[BOXTOP] - level.bmaporgy) >> MAPBLOCKSHIFT;
        for (int bx = xl; bx <= xh; bx++) {
            for (int by = yl; by <= yh; by++) {
                if (!g.mapUtil.P_BlockLinesIterator(bx, by, checkLine)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean P_TryMove(final Mobj thing, final int x, final int y) {
        floatok = false;
        if (!P_CheckPosition(thing, x, y)) {
            return false;
        }
        if ((thing.flags & MF_NOCLIP) == 0) {
            if (tmceilingz - tmfloorz < thing.height) {
                return false;
            }
            floatok = true;
            if ((thing.flags & MF_TELEPORT) == 0 && tmceilingz - thing.z < thing.height) {
                return false;
            }
            if ((thing.flags & MF_TELEPORT) == 0 && tmfloorz - thing.z > 24 * FRACUNIT) {
                return false;
            }
            if ((thing.flags & (MF_DROPOFF | MF_FLOAT)) == 0 && tmfloorz - tmdropoffz > 24 * FRACUNIT) {
                return false;
            }
        }
        g.mapUtil.P_UnsetThingPosition(thing);
        final int oldx = thing.x;
        final int oldy = thing.y;
        thing.floorz = tmfloorz;
        thing.ceilingz = tmceilingz;
        thing.x = x;
        thing.y = y;
        g.mapUtil.P_SetThingPosition(thing);
        if ((thing.flags & (MF_TELEPORT | MF_NOCLIP)) == 0) {
            while (numspechit-- > 0) {
                final Line ld = spechit[numspechit];
                final int side = P_PointOnLineSide(thing.x, thing.y, ld);
                final int oldside = P_PointOnLineSide(oldx, oldy, ld);
                if (side != oldside) {
                    if (ld.special != 0) {
                        g.spec.P_CrossSpecialLine(ld.id, oldside, thing);
                    }
                }
            }
        }
        return true;
    }

    public boolean P_ThingHeightClip(final Mobj thing) {
        final boolean onfloor = thing.z == thing.floorz;
        P_CheckPosition(thing, thing.x, thing.y);
        thing.floorz = tmfloorz;
        thing.ceilingz = tmceilingz;
        if (onfloor) {
            thing.z = thing.floorz;
        } else {
            if (thing.z + thing.height > thing.ceilingz) {
                thing.z = thing.ceilingz - thing.height;
            }
        }
        return thing.ceilingz - thing.floorz >= thing.height;
    }

    private void P_HitSlideLine(final Line ld) {
        if (ld.slopetype == Line.ST_HORIZONTAL) {
            tmymove = 0;
            return;
        }
        if (ld.slopetype == Line.ST_VERTICAL) {
            tmxmove = 0;
            return;
        }
        final int side = P_PointOnLineSide(slidemo.x, slidemo.y, ld);
        int lineangle = Angles.pointToAngle2(0, 0, ld.dx, ld.dy);
        if (side == 1) {
            lineangle += ANG180;
        }
        final int moveangle = Angles.pointToAngle2(0, 0, tmxmove, tmymove);
        int deltaangle = moveangle - lineangle;
        if (Integer.compareUnsigned(deltaangle, ANG180) > 0) {
            deltaangle += ANG180;
        }
        lineangle >>>= ANGLETOFINESHIFT;
        deltaangle >>>= ANGLETOFINESHIFT;
        final int movelen = P_AproxDistance(tmxmove, tmymove);
        final int newlen = mul(movelen, finecosine(deltaangle));
        tmxmove = mul(newlen, finecosine(lineangle));
        tmymove = mul(newlen, finesine[lineangle]);
    }

    private boolean PTR_SlideTraverse(final Intercept in) {
        if (!in.isaline) {
            throw new IllegalStateException("PTR_SlideTraverse: not a line?");
        }
        final Line li = in.line;
        final MapUtil mu = g.mapUtil;
        boolean blocking = false;
        if ((li.flags & ML_TWOSIDED) == 0) {
            if (P_PointOnLineSide(slidemo.x, slidemo.y, li) != 0) {
                return true;
            }
            blocking = true;
        } else {
            mu.P_LineOpening(li);
            if (mu.openrange < slidemo.height) {
                blocking = true;
            } else if (mu.opentop - slidemo.z < slidemo.height) {
                blocking = true;
            } else if (mu.openbottom - slidemo.z > 24 * FRACUNIT) {
                blocking = true;
            }
        }
        if (!blocking) {
            return true;
        }
        if (in.frac < bestslidefrac) {
            secondslidefrac = bestslidefrac;
            secondslideline = bestslideline;
            bestslidefrac = in.frac;
            bestslideline = li;
        }
        return false;
    }

    public void P_SlideMove(final Mobj mo) {
        slidemo = mo;
        int hitcount = 0;
        while (true) {
            if (++hitcount == 3) {
                stairstep(mo);
                return;
            }
            final int leadx;
            final int leady;
            final int trailx;
            final int traily;
            if (mo.momx > 0) {
                leadx = mo.x + mo.radius;
                trailx = mo.x - mo.radius;
            } else {
                leadx = mo.x - mo.radius;
                trailx = mo.x + mo.radius;
            }
            if (mo.momy > 0) {
                leady = mo.y + mo.radius;
                traily = mo.y - mo.radius;
            } else {
                leady = mo.y - mo.radius;
                traily = mo.y + mo.radius;
            }
            bestslidefrac = FRACUNIT + 1;
            final MapUtil mu = g.mapUtil;
            mu.P_PathTraverse(leadx, leady, leadx + mo.momx, leady + mo.momy, PT_ADDLINES, slideTraverse);
            mu.P_PathTraverse(trailx, leady, trailx + mo.momx, leady + mo.momy, PT_ADDLINES, slideTraverse);
            mu.P_PathTraverse(leadx, traily, leadx + mo.momx, traily + mo.momy, PT_ADDLINES, slideTraverse);
            if (bestslidefrac == FRACUNIT + 1) {
                stairstep(mo);
                return;
            }
            bestslidefrac -= 0x800;
            if (bestslidefrac > 0) {
                final int newx = mul(mo.momx, bestslidefrac);
                final int newy = mul(mo.momy, bestslidefrac);
                if (!P_TryMove(mo, mo.x + newx, mo.y + newy)) {
                    stairstep(mo);
                    return;
                }
            }
            bestslidefrac = FRACUNIT - (bestslidefrac + 0x800);
            if (bestslidefrac > FRACUNIT) {
                bestslidefrac = FRACUNIT;
            }
            if (bestslidefrac <= 0) {
                return;
            }
            tmxmove = mul(mo.momx, bestslidefrac);
            tmymove = mul(mo.momy, bestslidefrac);
            P_HitSlideLine(bestslideline);
            mo.momx = tmxmove;
            mo.momy = tmymove;
            if (P_TryMove(mo, mo.x + tmxmove, mo.y + tmymove)) {
                return;
            }
        }
    }

    private void stairstep(final Mobj mo) {
        if (!P_TryMove(mo, mo.x, mo.y + mo.momy)) {
            P_TryMove(mo, mo.x + mo.momx, mo.y);
        }
    }

    private boolean PTR_AimTraverse(final Intercept in) {
        final MapUtil mu = g.mapUtil;
        if (in.isaline) {
            final Line li = in.line;
            if ((li.flags & ML_TWOSIDED) == 0) {
                return false;
            }
            mu.P_LineOpening(li);
            if (mu.openbottom >= mu.opentop) {
                return false;
            }
            final int dist = mul(attackrange, in.frac);
            if (li.frontsector.floorheight != li.backsector.floorheight) {
                final int slope = div(mu.openbottom - shootz, dist);
                if (slope > bottomslope) {
                    bottomslope = slope;
                }
            }
            if (li.frontsector.ceilingheight != li.backsector.ceilingheight) {
                final int slope = div(mu.opentop - shootz, dist);
                if (slope < topslope) {
                    topslope = slope;
                }
            }
            return topslope > bottomslope;
        }
        final Mobj th = in.thing;
        if (th == shootthing) {
            return true;
        }
        if ((th.flags & MF_SHOOTABLE) == 0) {
            return true;
        }
        final int dist = mul(attackrange, in.frac);
        int thingtopslope = div(th.z + th.height - shootz, dist);
        if (thingtopslope < bottomslope) {
            return true;
        }
        int thingbottomslope = div(th.z - shootz, dist);
        if (thingbottomslope > topslope) {
            return true;
        }
        if (thingtopslope > topslope) {
            thingtopslope = topslope;
        }
        if (thingbottomslope < bottomslope) {
            thingbottomslope = bottomslope;
        }
        aimslope = (thingtopslope + thingbottomslope) / 2;
        linetarget = th;
        return false;
    }

    private boolean PTR_ShootTraverse(final Intercept in) {
        final MapUtil mu = g.mapUtil;
        if (in.isaline) {
            final Line li = in.line;
            if (li.special != 0) {
                g.spec.P_ShootSpecialLine(shootthing, li);
            }
            boolean hit = false;
            if ((li.flags & ML_TWOSIDED) == 0) {
                hit = true;
            } else {
                mu.P_LineOpening(li);
                final int dist = mul(attackrange, in.frac);
                if (li.frontsector.floorheight != li.backsector.floorheight) {
                    final int slope = div(mu.openbottom - shootz, dist);
                    if (slope > aimslope) {
                        hit = true;
                    }
                }
                if (!hit && li.frontsector.ceilingheight != li.backsector.ceilingheight) {
                    final int slope = div(mu.opentop - shootz, dist);
                    if (slope < aimslope) {
                        hit = true;
                    }
                }
                if (!hit) {
                    return true;
                }
            }
            final int frac = in.frac - div(4 * FRACUNIT, attackrange);
            final int x = mu.trace.x + mul(mu.trace.dx, frac);
            final int y = mu.trace.y + mul(mu.trace.dy, frac);
            final int z = shootz + mul(aimslope, mul(frac, attackrange));
            if (li.frontsector.ceilingpic == g.skyflatnum) {
                if (z > li.frontsector.ceilingheight) {
                    return false;
                }
                if (li.backsector != null && li.backsector.ceilingpic == g.skyflatnum) {
                    return false;
                }
            }
            g.mobjs.P_SpawnPuff(x, y, z);
            return false;
        }
        final Mobj th = in.thing;
        if (th == shootthing) {
            return true;
        }
        if ((th.flags & MF_SHOOTABLE) == 0) {
            return true;
        }
        final int dist = mul(attackrange, in.frac);
        final int thingtopslope = div(th.z + th.height - shootz, dist);
        if (thingtopslope < aimslope) {
            return true;
        }
        final int thingbottomslope = div(th.z - shootz, dist);
        if (thingbottomslope > aimslope) {
            return true;
        }
        final int frac = in.frac - div(10 * FRACUNIT, attackrange);
        final int x = mu.trace.x + mul(mu.trace.dx, frac);
        final int y = mu.trace.y + mul(mu.trace.dy, frac);
        final int z = shootz + mul(aimslope, mul(frac, attackrange));
        if ((th.flags & MF_NOBLOOD) != 0) {
            g.mobjs.P_SpawnPuff(x, y, z);
        } else {
            g.mobjs.P_SpawnBlood(x, y, z, la_damage);
        }
        if (la_damage != 0) {
            g.inter.P_DamageMobj(th, shootthing, shootthing, la_damage);
        }
        return false;
    }

    public int P_AimLineAttack(final Mobj t1, int angle, final int distance) {
        angle >>>= ANGLETOFINESHIFT;
        shootthing = t1;
        final int x2 = t1.x + (distance >> FRACBITS) * finecosine(angle);
        final int y2 = t1.y + (distance >> FRACBITS) * finesine[angle];
        shootz = t1.z + (t1.height >> 1) + 8 * FRACUNIT;
        topslope = 100 * FRACUNIT / 160;
        bottomslope = -100 * FRACUNIT / 160;
        attackrange = distance;
        linetarget = null;
        g.mapUtil.P_PathTraverse(t1.x, t1.y, x2, y2, PT_ADDLINES | PT_ADDTHINGS, aimTraverse);
        if (linetarget != null) {
            return aimslope;
        }
        return 0;
    }

    public void P_LineAttack(final Mobj t1, int angle, final int distance, final int slope, final int damage) {
        angle >>>= ANGLETOFINESHIFT;
        shootthing = t1;
        la_damage = damage;
        final int x2 = t1.x + (distance >> FRACBITS) * finecosine(angle);
        final int y2 = t1.y + (distance >> FRACBITS) * finesine[angle];
        shootz = t1.z + (t1.height >> 1) + 8 * FRACUNIT;
        attackrange = distance;
        aimslope = slope;
        g.mapUtil.P_PathTraverse(t1.x, t1.y, x2, y2, PT_ADDLINES | PT_ADDTHINGS, shootTraverse);
    }

    private boolean PTR_UseTraverse(final Intercept in) {
        final Line line = in.line;
        if (line.special == 0) {
            g.mapUtil.P_LineOpening(line);
            if (g.mapUtil.openrange <= 0) {
                g.sound.S_StartSound(usething, sfx_noway);
                return false;
            }
            return true;
        }
        int side = 0;
        if (P_PointOnLineSide(usething.x, usething.y, line) == 1) {
            side = 1;
        }
        g.spec.P_UseSpecialLine(usething, line, side);
        return false;
    }

    public void P_UseLines(final Player player) {
        usething = player.mo;
        final int angle = player.mo.angle >>> ANGLETOFINESHIFT;
        final int x1 = player.mo.x;
        final int y1 = player.mo.y;
        final int x2 = x1 + (USERANGE >> FRACBITS) * finecosine(angle);
        final int y2 = y1 + (USERANGE >> FRACBITS) * finesine[angle];
        g.mapUtil.P_PathTraverse(x1, y1, x2, y2, PT_ADDLINES, useTraverse);
    }

    private boolean PIT_RadiusAttack(final Mobj thing) {
        if ((thing.flags & MF_SHOOTABLE) == 0) {
            return true;
        }
        if (thing.type == MT_CYBORG || thing.type == MT_SPIDER) {
            return true;
        }
        final int dx = Math.abs(thing.x - bombspot.x);
        final int dy = Math.abs(thing.y - bombspot.y);
        int dist = dx > dy ? dx : dy;
        dist = (dist - thing.radius) >> FRACBITS;
        if (dist < 0) {
            dist = 0;
        }
        if (dist >= bombdamage) {
            return true;
        }
        if (g.sight.P_CheckSight(thing, bombspot)) {
            g.inter.P_DamageMobj(thing, bombspot, bombsource, bombdamage - dist);
        }
        return true;
    }

    public void P_RadiusAttack(final Mobj spot, final Mobj source, final int damage) {
        final Level level = g.level;
        final int dist = (damage + MAXRADIUS) << FRACBITS;
        final int yh = (spot.y + dist - level.bmaporgy) >> MAPBLOCKSHIFT;
        final int yl = (spot.y - dist - level.bmaporgy) >> MAPBLOCKSHIFT;
        final int xh = (spot.x + dist - level.bmaporgx) >> MAPBLOCKSHIFT;
        final int xl = (spot.x - dist - level.bmaporgx) >> MAPBLOCKSHIFT;
        bombspot = spot;
        bombsource = source;
        bombdamage = damage;
        for (int y = yl; y <= yh; y++) {
            for (int x = xl; x <= xh; x++) {
                g.mapUtil.P_BlockThingsIterator(x, y, radiusAttack);
            }
        }
    }

    private boolean PIT_ChangeSector(final Mobj thing) {
        if (P_ThingHeightClip(thing)) {
            return true;
        }
        if (thing.health <= 0) {
            g.mobjs.P_SetMobjState(thing, S_GIBS);
            thing.flags &= ~MF_SOLID;
            thing.height = 0;
            thing.radius = 0;
            return true;
        }
        if ((thing.flags & MF_DROPPED) != 0) {
            g.mobjs.P_RemoveMobj(thing);
            return true;
        }
        if ((thing.flags & MF_SHOOTABLE) == 0) {
            return true;
        }
        nofit = true;
        if (crushchange && (g.leveltime & 3) == 0) {
            g.inter.P_DamageMobj(thing, null, null, 10);
            final Mobj mo = g.mobjs.P_SpawnMobj(thing.x, thing.y, thing.z + thing.height / 2, MT_BLOOD);
            mo.momx = (g.random.P_Random() - g.random.P_Random()) << 12;
            mo.momy = (g.random.P_Random() - g.random.P_Random()) << 12;
        }
        return true;
    }

    public boolean P_ChangeSector(final Sector sector, final boolean crunch) {
        nofit = false;
        crushchange = crunch;
        for (int x = sector.blockbox[BOXLEFT]; x <= sector.blockbox[BOXRIGHT]; x++) {
            for (int y = sector.blockbox[BOXBOTTOM]; y <= sector.blockbox[BOXTOP]; y++) {
                g.mapUtil.P_BlockThingsIterator(x, y, changeSector);
            }
        }
        return nofit;
    }
}
