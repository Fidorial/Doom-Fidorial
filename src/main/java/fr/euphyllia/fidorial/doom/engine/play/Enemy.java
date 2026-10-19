// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine.play;

import fr.euphyllia.fidorial.doom.engine.Angles;
import fr.euphyllia.fidorial.doom.engine.Game;
import fr.euphyllia.fidorial.doom.engine.Level;
import fr.euphyllia.fidorial.doom.engine.Line;
import fr.euphyllia.fidorial.doom.engine.Mobj;
import fr.euphyllia.fidorial.doom.engine.Player;
import fr.euphyllia.fidorial.doom.engine.PspDef;
import fr.euphyllia.fidorial.doom.engine.Sector;
import fr.euphyllia.fidorial.doom.engine.Thinker;
import fr.euphyllia.fidorial.doom.engine.info.MobjInfo;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static fr.euphyllia.fidorial.doom.engine.Defs.FLOATSPEED;
import static fr.euphyllia.fidorial.doom.engine.Defs.MAPBLOCKSHIFT;
import static fr.euphyllia.fidorial.doom.engine.Defs.MAXPLAYERS;
import static fr.euphyllia.fidorial.doom.engine.Defs.MAXRADIUS;
import static fr.euphyllia.fidorial.doom.engine.Defs.MELEERANGE;
import static fr.euphyllia.fidorial.doom.engine.Defs.MISSILERANGE;
import static fr.euphyllia.fidorial.doom.engine.Defs.ML_SOUNDBLOCK;
import static fr.euphyllia.fidorial.doom.engine.Defs.ML_TWOSIDED;
import static fr.euphyllia.fidorial.doom.engine.Defs.commercial;
import static fr.euphyllia.fidorial.doom.engine.Defs.sk_easy;
import static fr.euphyllia.fidorial.doom.engine.Defs.sk_nightmare;
import static fr.euphyllia.fidorial.doom.engine.Fixed.FRACUNIT;
import static fr.euphyllia.fidorial.doom.engine.Fixed.mul;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_AMBUSH;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_CORPSE;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_FLOAT;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_INFLOAT;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_JUSTATTACKED;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_JUSTHIT;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_SHADOW;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_SHOOTABLE;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_SKULLFLY;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_SOLID;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_barexp;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_bgdth1;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_bgdth2;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_bgsit1;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_bgsit2;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_boscub;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_bosdth;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_bospit;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_bospn;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_bossit;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_bspwlk;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_claw;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_dbcls;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_dbload;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_dbopn;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_flame;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_flamst;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_hoof;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_manatk;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_metal;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_pdiehi;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_pistol;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_pldeth;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_podth1;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_podth2;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_podth3;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_posit1;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_posit2;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_posit3;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_shotgn;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_skepch;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_skeswg;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_slop;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_telept;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_vilatk;
import static fr.euphyllia.fidorial.doom.engine.Tables.ANG180;
import static fr.euphyllia.fidorial.doom.engine.Tables.ANG270;
import static fr.euphyllia.fidorial.doom.engine.Tables.ANG90;
import static fr.euphyllia.fidorial.doom.engine.Tables.ANGLETOFINESHIFT;
import static fr.euphyllia.fidorial.doom.engine.Tables.finecosine;
import static fr.euphyllia.fidorial.doom.engine.Tables.finesine;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_ARACHPLAZ;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_BABY;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_BOSSTARGET;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_BRUISER;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_BRUISERSHOT;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_CYBORG;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_FATSHOT;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_FATSO;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_FIRE;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_HEAD;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_HEADSHOT;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_KNIGHT;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_PAIN;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_ROCKET;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_SERGEANT;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_SHADOWS;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_SKULL;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_SMOKE;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_SPAWNFIRE;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_SPAWNSHOT;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_SPIDER;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_TRACER;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_TROOP;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_TROOPSHOT;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_UNDEAD;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_VILE;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BRAINEXPLODE1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_NULL;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_VILE_HEAL1;
import static fr.euphyllia.fidorial.doom.engine.play.MapUtil.P_AproxDistance;

/**
 * Monster AI and the monster action functions ({@code p_enemy.c}).
 */
public final class Enemy {

    public static final int DI_EAST = 0;
    public static final int DI_NORTHEAST = 1;
    public static final int DI_NORTH = 2;
    public static final int DI_NORTHWEST = 3;
    public static final int DI_WEST = 4;
    public static final int DI_SOUTHWEST = 5;
    public static final int DI_SOUTH = 6;
    public static final int DI_SOUTHEAST = 7;
    public static final int DI_NODIR = 8;

    private static final int[] OPPOSITE = {
            DI_WEST, DI_SOUTHWEST, DI_SOUTH, DI_SOUTHEAST, DI_EAST, DI_NORTHEAST, DI_NORTH, DI_NORTHWEST, DI_NODIR
    };
    private static final int[] DIAGS = {DI_NORTHWEST, DI_NORTHEAST, DI_SOUTHWEST, DI_SOUTHEAST};
    private static final int[] XSPEED = {FRACUNIT, 47000, 0, -47000, -FRACUNIT, -47000, 0, 47000};
    private static final int[] YSPEED = {0, 47000, FRACUNIT, 47000, 0, -47000, -FRACUNIT, -47000};
    private static final int TRACEANGLE = 0xc000000;
    private static final int FATSPREAD = ANG90 / 8;
    private static final int SKULLSPEED = 20 * FRACUNIT;

    private final Game g;
    private final Predicate<Mobj> vileCheck = this::PIT_VileCheck;
    private final List<Mobj> braintargets = new ArrayList<>();
    private @Nullable Mobj soundtarget;
    private Mobj corpsehit;
    private Mobj vileobj;
    private int viletryx;
    private int viletryy;
    private int braintargeton;
    private int easy;

    public Enemy(final Game g) {
        this.g = g;
    }

    private static Line junkLine(final int tag) {
        final Line junk = new Line(-1);
        junk.tag = (short) tag;
        return junk;
    }

    private void P_RecursiveSound(final Sector sec, final int soundblocks) {
        if (sec.validcount == g.validcount && sec.soundtraversed <= soundblocks + 1) {
            return;
        }
        sec.validcount = g.validcount;
        sec.soundtraversed = soundblocks + 1;
        sec.soundtarget = soundtarget;
        final MapUtil mu = g.mapUtil;
        for (int i = 0; i < sec.linecount; i++) {
            final Line check = sec.lines[i];
            if ((check.flags & ML_TWOSIDED) == 0) {
                continue;
            }
            mu.P_LineOpening(check);
            if (mu.openrange <= 0) {
                continue;
            }
            final Sector other;
            if (g.level.sides[check.sidenum[0]].sector == sec) {
                other = g.level.sides[check.sidenum[1]].sector;
            } else {
                other = g.level.sides[check.sidenum[0]].sector;
            }
            if ((check.flags & ML_SOUNDBLOCK) != 0) {
                if (soundblocks == 0) {
                    P_RecursiveSound(other, 1);
                }
            } else {
                P_RecursiveSound(other, soundblocks);
            }
        }
    }

    public void P_NoiseAlert(final Mobj target, final Mobj emmiter) {
        soundtarget = target;
        g.validcount++;
        P_RecursiveSound(emmiter.subsector.sector, 0);
    }

    private boolean P_CheckMeleeRange(final Mobj actor) {
        if (actor.target == null) {
            return false;
        }
        final Mobj pl = actor.target;
        final int dist = P_AproxDistance(pl.x - actor.x, pl.y - actor.y);
        if (dist >= MELEERANGE - 20 * FRACUNIT + pl.info.radius()) {
            return false;
        }
        return g.sight.P_CheckSight(actor, actor.target);
    }

    private boolean P_CheckMissileRange(final Mobj actor) {
        if (!g.sight.P_CheckSight(actor, actor.target)) {
            return false;
        }
        if ((actor.flags & MF_JUSTHIT) != 0) {
            actor.flags &= ~MF_JUSTHIT;
            return true;
        }
        if (actor.reactiontime != 0) {
            return false;
        }
        int dist = P_AproxDistance(actor.x - actor.target.x, actor.y - actor.target.y) - 64 * FRACUNIT;
        if (actor.info.meleestate() == 0) {
            dist -= 128 * FRACUNIT;
        }
        dist >>= 16;
        if (actor.type == MT_VILE) {
            if (dist > 14 * 64) {
                return false;
            }
        }
        if (actor.type == MT_UNDEAD) {
            if (dist < 196) {
                return false;
            }
            dist >>= 1;
        }
        if (actor.type == MT_CYBORG || actor.type == MT_SPIDER || actor.type == MT_SKULL) {
            dist >>= 1;
        }
        if (dist > 200) {
            dist = 200;
        }
        if (actor.type == MT_CYBORG && dist > 160) {
            dist = 160;
        }
        return g.random.P_Random() >= dist;
    }

    private boolean P_Move(final Mobj actor) {
        if (actor.movedir == DI_NODIR) {
            return false;
        }
        if (Integer.compareUnsigned(actor.movedir, 8) >= 0) {
            throw new IllegalStateException("Weird actor->movedir!");
        }
        final int tryx = actor.x + actor.info.speed() * XSPEED[actor.movedir];
        final int tryy = actor.y + actor.info.speed() * YSPEED[actor.movedir];
        final MapMove map = g.map;
        final boolean tryOk = map.P_TryMove(actor, tryx, tryy);
        if (!tryOk) {
            if ((actor.flags & MF_FLOAT) != 0 && map.floatok) {
                if (actor.z < map.tmfloorz) {
                    actor.z += FLOATSPEED;
                } else {
                    actor.z -= FLOATSPEED;
                }
                actor.flags |= MF_INFLOAT;
                return true;
            }
            if (map.numSpecHit() == 0) {
                return false;
            }
            actor.movedir = DI_NODIR;
            boolean good = false;
            while (map.decrementSpecHit() >= 0) {
                final Line ld = map.specHit(map.numSpecHit());
                if (g.spec.P_UseSpecialLine(actor, ld, 0)) {
                    good = true;
                }
            }
            return good;
        } else {
            actor.flags &= ~MF_INFLOAT;
        }
        if ((actor.flags & MF_FLOAT) == 0) {
            actor.z = actor.floorz;
        }
        return true;
    }

    private boolean P_TryWalk(final Mobj actor) {
        if (!P_Move(actor)) {
            return false;
        }
        actor.movecount = g.random.P_Random() & 15;
        return true;
    }

    private void P_NewChaseDir(final Mobj actor) {
        if (actor.target == null) {
            throw new IllegalStateException("P_NewChaseDir: called with no target");
        }
        final int olddir = actor.movedir;
        final int turnaround = OPPOSITE[olddir];
        final int deltax = actor.target.x - actor.x;
        final int deltay = actor.target.y - actor.y;
        final int[] d = new int[3];
        if (deltax > 10 * FRACUNIT) {
            d[1] = DI_EAST;
        } else if (deltax < -10 * FRACUNIT) {
            d[1] = DI_WEST;
        } else {
            d[1] = DI_NODIR;
        }
        if (deltay < -10 * FRACUNIT) {
            d[2] = DI_SOUTH;
        } else if (deltay > 10 * FRACUNIT) {
            d[2] = DI_NORTH;
        } else {
            d[2] = DI_NODIR;
        }
        if (d[1] != DI_NODIR && d[2] != DI_NODIR) {
            actor.movedir = DIAGS[((deltay < 0 ? 1 : 0) << 1) + (deltax > 0 ? 1 : 0)];
            if (actor.movedir != turnaround && P_TryWalk(actor)) {
                return;
            }
        }
        if (g.random.P_Random() > 200 || Math.abs(deltay) > Math.abs(deltax)) {
            final int tdir = d[1];
            d[1] = d[2];
            d[2] = tdir;
        }
        if (d[1] == turnaround) {
            d[1] = DI_NODIR;
        }
        if (d[2] == turnaround) {
            d[2] = DI_NODIR;
        }
        if (d[1] != DI_NODIR) {
            actor.movedir = d[1];
            if (P_TryWalk(actor)) {
                return;
            }
        }
        if (d[2] != DI_NODIR) {
            actor.movedir = d[2];
            if (P_TryWalk(actor)) {
                return;
            }
        }
        if (olddir != DI_NODIR) {
            actor.movedir = olddir;
            if (P_TryWalk(actor)) {
                return;
            }
        }
        if ((g.random.P_Random() & 1) != 0) {
            for (int tdir = DI_EAST; tdir <= DI_SOUTHEAST; tdir++) {
                if (tdir != turnaround) {
                    actor.movedir = tdir;
                    if (P_TryWalk(actor)) {
                        return;
                    }
                }
            }
        } else {
            for (int tdir = DI_SOUTHEAST; tdir != DI_EAST - 1; tdir--) {
                if (tdir != turnaround) {
                    actor.movedir = tdir;
                    if (P_TryWalk(actor)) {
                        return;
                    }
                }
            }
        }
        if (turnaround != DI_NODIR) {
            actor.movedir = turnaround;
            if (P_TryWalk(actor)) {
                return;
            }
        }
        actor.movedir = DI_NODIR;
    }

    public boolean P_LookForPlayers(final Mobj actor, final boolean allaround) {
        int c = 0;
        final int stop = (actor.lastlook - 1) & 3;
        for (; ; actor.lastlook = (actor.lastlook + 1) & 3) {
            if (!g.playeringame[actor.lastlook]) {
                continue;
            }
            if (c++ == 2 || actor.lastlook == stop) {
                return false;
            }
            final Player player = g.players[actor.lastlook];
            if (player.health <= 0) {
                continue;
            }
            if (!g.sight.P_CheckSight(actor, player.mo)) {
                continue;
            }
            if (!allaround) {
                final int an = Angles.pointToAngle2(actor.x, actor.y, player.mo.x, player.mo.y) - actor.angle;
                if (Integer.compareUnsigned(an, ANG90) > 0 && Integer.compareUnsigned(an, ANG270) < 0) {
                    final int dist = P_AproxDistance(player.mo.x - actor.x, player.mo.y - actor.y);
                    if (dist > MELEERANGE) {
                        continue;
                    }
                }
            }
            actor.target = player.mo;
            return true;
        }
    }

    private boolean anyOtherAlive(final Mobj mo) {
        for (Thinker th = g.thinkercap.next; th != g.thinkercap; th = th.next) {
            if (!(th instanceof final Mobj mo2) || th.function != Thinker.FN_ACTIVE) {
                continue;
            }
            if (mo2 != mo && mo2.type == mo.type && mo2.health > 0) {
                return true;
            }
        }
        return false;
    }

    public void A_KeenDie(final Mobj mo) {
        A_Fall(mo);
        if (anyOtherAlive(mo)) {
            return;
        }
        g.doors.EV_DoDoor(junkLine(666), Doors.open);
    }

    public void A_Look(final Mobj actor) {
        actor.threshold = 0;
        final Mobj targ = actor.subsector.sector.soundtarget;
        boolean see = false;
        if (targ != null && (targ.flags & MF_SHOOTABLE) != 0) {
            actor.target = targ;
            if ((actor.flags & MF_AMBUSH) != 0) {
                if (g.sight.P_CheckSight(actor, actor.target)) {
                    see = true;
                }
            } else {
                see = true;
            }
        }
        if (!see && !P_LookForPlayers(actor, false)) {
            return;
        }
        if (actor.info.seesound() != 0) {
            final int sound;
            switch (actor.info.seesound()) {
                case sfx_posit1, sfx_posit2, sfx_posit3 -> sound = sfx_posit1 + g.random.P_Random() % 3;
                case sfx_bgsit1, sfx_bgsit2 -> sound = sfx_bgsit1 + g.random.P_Random() % 2;
                default -> sound = actor.info.seesound();
            }
            if (actor.type == MT_SPIDER || actor.type == MT_CYBORG) {
                g.sound.S_StartSound(null, sound);
            } else {
                g.sound.S_StartSound(actor, sound);
            }
        }
        g.mobjs.P_SetMobjState(actor, actor.info.seestate());
    }

    public void A_Chase(final Mobj actor) {
        if (actor.reactiontime != 0) {
            actor.reactiontime--;
        }
        if (actor.threshold != 0) {
            if (actor.target == null || actor.target.health <= 0) {
                actor.threshold = 0;
            } else {
                actor.threshold--;
            }
        }
        if (actor.movedir < 8) {
            actor.angle &= (7 << 29);
            final int delta = actor.angle - (actor.movedir << 29);
            if (delta > 0) {
                actor.angle -= ANG90 / 2;
            } else if (delta < 0) {
                actor.angle += ANG90 / 2;
            }
        }
        if (actor.target == null || (actor.target.flags & MF_SHOOTABLE) == 0) {
            if (P_LookForPlayers(actor, true)) {
                return;
            }
            g.mobjs.P_SetMobjState(actor, actor.info.spawnstate());
            return;
        }
        if ((actor.flags & MF_JUSTATTACKED) != 0) {
            actor.flags &= ~MF_JUSTATTACKED;
            if (g.gameskill != sk_nightmare && !g.fastparm) {
                P_NewChaseDir(actor);
            }
            return;
        }
        if (actor.info.meleestate() != 0 && P_CheckMeleeRange(actor)) {
            if (actor.info.attacksound() != 0) {
                g.sound.S_StartSound(actor, actor.info.attacksound());
            }
            g.mobjs.P_SetMobjState(actor, actor.info.meleestate());
            return;
        }
        if (actor.info.missilestate() != 0) {
            final boolean skip = g.gameskill < sk_nightmare && !g.fastparm && actor.movecount != 0;
            if (!skip && P_CheckMissileRange(actor)) {
                g.mobjs.P_SetMobjState(actor, actor.info.missilestate());
                actor.flags |= MF_JUSTATTACKED;
                return;
            }
        }
        if (g.netgame && actor.threshold == 0 && !g.sight.P_CheckSight(actor, actor.target)) {
            if (P_LookForPlayers(actor, true)) {
                return;
            }
        }
        if (--actor.movecount < 0 || !P_Move(actor)) {
            P_NewChaseDir(actor);
        }
        if (actor.info.activesound() != 0 && g.random.P_Random() < 3) {
            g.sound.S_StartSound(actor, actor.info.activesound());
        }
    }

    public void A_FaceTarget(final Mobj actor) {
        if (actor.target == null) {
            return;
        }
        actor.flags &= ~MF_AMBUSH;
        actor.angle = Angles.pointToAngle2(actor.x, actor.y, actor.target.x, actor.target.y);
        if ((actor.target.flags & MF_SHADOW) != 0) {
            actor.angle += (g.random.P_Random() - g.random.P_Random()) << 21;
        }
    }

    public void A_PosAttack(final Mobj actor) {
        if (actor.target == null) {
            return;
        }
        A_FaceTarget(actor);
        int angle = actor.angle;
        final int slope = g.map.P_AimLineAttack(actor, angle, MISSILERANGE);
        g.sound.S_StartSound(actor, sfx_pistol);
        angle += (g.random.P_Random() - g.random.P_Random()) << 20;
        final int damage = ((g.random.P_Random() % 5) + 1) * 3;
        g.map.P_LineAttack(actor, angle, MISSILERANGE, slope, damage);
    }

    public void A_SPosAttack(final Mobj actor) {
        if (actor.target == null) {
            return;
        }
        g.sound.S_StartSound(actor, sfx_shotgn);
        A_FaceTarget(actor);
        final int bangle = actor.angle;
        final int slope = g.map.P_AimLineAttack(actor, bangle, MISSILERANGE);
        for (int i = 0; i < 3; i++) {
            final int angle = bangle + ((g.random.P_Random() - g.random.P_Random()) << 20);
            final int damage = ((g.random.P_Random() % 5) + 1) * 3;
            g.map.P_LineAttack(actor, angle, MISSILERANGE, slope, damage);
        }
    }

    public void A_CPosAttack(final Mobj actor) {
        if (actor.target == null) {
            return;
        }
        g.sound.S_StartSound(actor, sfx_shotgn);
        A_FaceTarget(actor);
        final int bangle = actor.angle;
        final int slope = g.map.P_AimLineAttack(actor, bangle, MISSILERANGE);
        final int angle = bangle + ((g.random.P_Random() - g.random.P_Random()) << 20);
        final int damage = ((g.random.P_Random() % 5) + 1) * 3;
        g.map.P_LineAttack(actor, angle, MISSILERANGE, slope, damage);
    }

    public void A_CPosRefire(final Mobj actor) {
        A_FaceTarget(actor);
        if (g.random.P_Random() < 40) {
            return;
        }
        if (actor.target == null || actor.target.health <= 0 || !g.sight.P_CheckSight(actor, actor.target)) {
            g.mobjs.P_SetMobjState(actor, actor.info.seestate());
        }
    }

    public void A_SpidRefire(final Mobj actor) {
        A_FaceTarget(actor);
        if (g.random.P_Random() < 10) {
            return;
        }
        if (actor.target == null || actor.target.health <= 0 || !g.sight.P_CheckSight(actor, actor.target)) {
            g.mobjs.P_SetMobjState(actor, actor.info.seestate());
        }
    }

    public void A_BspiAttack(final Mobj actor) {
        if (actor.target == null) {
            return;
        }
        A_FaceTarget(actor);
        g.mobjs.P_SpawnMissile(actor, actor.target, MT_ARACHPLAZ);
    }

    public void A_TroopAttack(final Mobj actor) {
        if (actor.target == null) {
            return;
        }
        A_FaceTarget(actor);
        if (P_CheckMeleeRange(actor)) {
            g.sound.S_StartSound(actor, sfx_claw);
            final int damage = (g.random.P_Random() % 8 + 1) * 3;
            g.inter.P_DamageMobj(actor.target, actor, actor, damage);
            return;
        }
        g.mobjs.P_SpawnMissile(actor, actor.target, MT_TROOPSHOT);
    }

    public void A_SargAttack(final Mobj actor) {
        if (actor.target == null) {
            return;
        }
        A_FaceTarget(actor);
        if (P_CheckMeleeRange(actor)) {
            final int damage = ((g.random.P_Random() % 10) + 1) * 4;
            g.inter.P_DamageMobj(actor.target, actor, actor, damage);
        }
    }

    public void A_HeadAttack(final Mobj actor) {
        if (actor.target == null) {
            return;
        }
        A_FaceTarget(actor);
        if (P_CheckMeleeRange(actor)) {
            final int damage = (g.random.P_Random() % 6 + 1) * 10;
            g.inter.P_DamageMobj(actor.target, actor, actor, damage);
            return;
        }
        g.mobjs.P_SpawnMissile(actor, actor.target, MT_HEADSHOT);
    }

    public void A_CyberAttack(final Mobj actor) {
        if (actor.target == null) {
            return;
        }
        A_FaceTarget(actor);
        g.mobjs.P_SpawnMissile(actor, actor.target, MT_ROCKET);
    }

    public void A_BruisAttack(final Mobj actor) {
        if (actor.target == null) {
            return;
        }
        if (P_CheckMeleeRange(actor)) {
            g.sound.S_StartSound(actor, sfx_claw);
            final int damage = (g.random.P_Random() % 8 + 1) * 10;
            g.inter.P_DamageMobj(actor.target, actor, actor, damage);
            return;
        }
        g.mobjs.P_SpawnMissile(actor, actor.target, MT_BRUISERSHOT);
    }

    public void A_SkelMissile(final Mobj actor) {
        if (actor.target == null) {
            return;
        }
        A_FaceTarget(actor);
        actor.z += 16 * FRACUNIT;
        final Mobj mo = g.mobjs.P_SpawnMissile(actor, actor.target, MT_TRACER);
        actor.z -= 16 * FRACUNIT;
        mo.x += mo.momx;
        mo.y += mo.momy;
        mo.tracer = actor.target;
    }

    public void A_Tracer(final Mobj actor) {
        if ((g.gametic & 3) != 0) {
            return;
        }
        g.mobjs.P_SpawnPuff(actor.x, actor.y, actor.z);
        final Mobj th = g.mobjs.P_SpawnMobj(actor.x - actor.momx, actor.y - actor.momy, actor.z, MT_SMOKE);
        th.momz = FRACUNIT;
        th.tics -= g.random.P_Random() & 3;
        if (th.tics < 1) {
            th.tics = 1;
        }
        final Mobj dest = actor.tracer;
        if (dest == null || dest.health <= 0) {
            return;
        }
        int exact = Angles.pointToAngle2(actor.x, actor.y, dest.x, dest.y);
        if (exact != actor.angle) {
            if (Integer.compareUnsigned(exact - actor.angle, 0x80000000) > 0) {
                actor.angle -= TRACEANGLE;
                if (Integer.compareUnsigned(exact - actor.angle, 0x80000000) < 0) {
                    actor.angle = exact;
                }
            } else {
                actor.angle += TRACEANGLE;
                if (Integer.compareUnsigned(exact - actor.angle, 0x80000000) > 0) {
                    actor.angle = exact;
                }
            }
        }
        exact = actor.angle >>> ANGLETOFINESHIFT;
        actor.momx = mul(actor.info.speed(), finecosine(exact));
        actor.momy = mul(actor.info.speed(), finesine[exact]);
        int dist = P_AproxDistance(dest.x - actor.x, dest.y - actor.y);
        dist = dist / actor.info.speed();
        if (dist < 1) {
            dist = 1;
        }
        final int slope = (dest.z + 40 * FRACUNIT - actor.z) / dist;
        if (slope < actor.momz) {
            actor.momz -= FRACUNIT / 8;
        } else {
            actor.momz += FRACUNIT / 8;
        }
    }

    public void A_SkelWhoosh(final Mobj actor) {
        if (actor.target == null) {
            return;
        }
        A_FaceTarget(actor);
        g.sound.S_StartSound(actor, sfx_skeswg);
    }

    public void A_SkelFist(final Mobj actor) {
        if (actor.target == null) {
            return;
        }
        A_FaceTarget(actor);
        if (P_CheckMeleeRange(actor)) {
            final int damage = ((g.random.P_Random() % 10) + 1) * 6;
            g.sound.S_StartSound(actor, sfx_skepch);
            g.inter.P_DamageMobj(actor.target, actor, actor, damage);
        }
    }

    private boolean PIT_VileCheck(final Mobj thing) {
        if ((thing.flags & MF_CORPSE) == 0) {
            return true;
        }
        if (thing.tics != -1) {
            return true;
        }
        if (thing.info.raisestate() == S_NULL) {
            return true;
        }
        final int maxdist = thing.info.radius() + g.mobjinfo[MT_VILE].radius();
        if (Math.abs(thing.x - viletryx) > maxdist || Math.abs(thing.y - viletryy) > maxdist) {
            return true;
        }
        corpsehit = thing;
        corpsehit.momx = corpsehit.momy = 0;
        corpsehit.height <<= 2;
        final boolean check = g.map.P_CheckPosition(corpsehit, corpsehit.x, corpsehit.y);
        corpsehit.height >>= 2;
        return !check;
    }

    public void A_VileChase(final Mobj actor) {
        if (actor.movedir != DI_NODIR) {
            final Level level = g.level;
            viletryx = actor.x + actor.info.speed() * XSPEED[actor.movedir];
            viletryy = actor.y + actor.info.speed() * YSPEED[actor.movedir];
            final int xl = (viletryx - level.bmaporgx - MAXRADIUS * 2) >> MAPBLOCKSHIFT;
            final int xh = (viletryx - level.bmaporgx + MAXRADIUS * 2) >> MAPBLOCKSHIFT;
            final int yl = (viletryy - level.bmaporgy - MAXRADIUS * 2) >> MAPBLOCKSHIFT;
            final int yh = (viletryy - level.bmaporgy + MAXRADIUS * 2) >> MAPBLOCKSHIFT;
            vileobj = actor;
            for (int bx = xl; bx <= xh; bx++) {
                for (int by = yl; by <= yh; by++) {
                    if (!g.mapUtil.P_BlockThingsIterator(bx, by, vileCheck)) {
                        final Mobj temp = actor.target;
                        actor.target = corpsehit;
                        A_FaceTarget(actor);
                        actor.target = temp;
                        g.mobjs.P_SetMobjState(actor, S_VILE_HEAL1);
                        g.sound.S_StartSound(corpsehit, sfx_slop);
                        final MobjInfo info = corpsehit.info;
                        g.mobjs.P_SetMobjState(corpsehit, info.raisestate());
                        corpsehit.height <<= 2;
                        corpsehit.flags = info.flags();
                        corpsehit.health = info.spawnhealth();
                        corpsehit.target = null;
                        return;
                    }
                }
            }
        }
        A_Chase(actor);
    }

    public void A_VileStart(final Mobj actor) {
        g.sound.S_StartSound(actor, sfx_vilatk);
    }

    public void A_StartFire(final Mobj actor) {
        g.sound.S_StartSound(actor, sfx_flamst);
        A_Fire(actor);
    }

    public void A_FireCrackle(final Mobj actor) {
        g.sound.S_StartSound(actor, sfx_flame);
        A_Fire(actor);
    }

    public void A_Fire(final Mobj actor) {
        final Mobj dest = actor.tracer;
        if (dest == null) {
            return;
        }
        if (!g.sight.P_CheckSight(actor.target, dest)) {
            return;
        }
        final int an = dest.angle >>> ANGLETOFINESHIFT;
        g.mapUtil.P_UnsetThingPosition(actor);
        actor.x = dest.x + mul(24 * FRACUNIT, finecosine(an));
        actor.y = dest.y + mul(24 * FRACUNIT, finesine[an]);
        actor.z = dest.z;
        g.mapUtil.P_SetThingPosition(actor);
    }

    public void A_VileTarget(final Mobj actor) {
        if (actor.target == null) {
            return;
        }
        A_FaceTarget(actor);
        // vanilla passes target x as both coordinates
        final Mobj fog = g.mobjs.P_SpawnMobj(actor.target.x, actor.target.x, actor.target.z, MT_FIRE);
        actor.tracer = fog;
        fog.target = actor;
        fog.tracer = actor.target;
        A_Fire(fog);
    }

    public void A_VileAttack(final Mobj actor) {
        if (actor.target == null) {
            return;
        }
        A_FaceTarget(actor);
        if (!g.sight.P_CheckSight(actor, actor.target)) {
            return;
        }
        g.sound.S_StartSound(actor, sfx_barexp);
        g.inter.P_DamageMobj(actor.target, actor, actor, 20);
        actor.target.momz = 1000 * FRACUNIT / actor.target.info.mass();
        final int an = actor.angle >>> ANGLETOFINESHIFT;
        final Mobj fire = actor.tracer;
        if (fire == null) {
            return;
        }
        fire.x = actor.target.x - mul(24 * FRACUNIT, finecosine(an));
        fire.y = actor.target.y - mul(24 * FRACUNIT, finesine[an]);
        g.map.P_RadiusAttack(fire, actor, 70);
    }

    public void A_FatRaise(final Mobj actor) {
        A_FaceTarget(actor);
        g.sound.S_StartSound(actor, sfx_manatk);
    }

    private void aimFat(final Mobj mo) {
        final int an = mo.angle >>> ANGLETOFINESHIFT;
        mo.momx = mul(mo.info.speed(), finecosine(an));
        mo.momy = mul(mo.info.speed(), finesine[an]);
    }

    public void A_FatAttack1(final Mobj actor) {
        A_FaceTarget(actor);
        actor.angle += FATSPREAD;
        g.mobjs.P_SpawnMissile(actor, actor.target, MT_FATSHOT);
        final Mobj mo = g.mobjs.P_SpawnMissile(actor, actor.target, MT_FATSHOT);
        mo.angle += FATSPREAD;
        aimFat(mo);
    }

    public void A_FatAttack2(final Mobj actor) {
        A_FaceTarget(actor);
        actor.angle -= FATSPREAD;
        g.mobjs.P_SpawnMissile(actor, actor.target, MT_FATSHOT);
        final Mobj mo = g.mobjs.P_SpawnMissile(actor, actor.target, MT_FATSHOT);
        mo.angle -= FATSPREAD * 2;
        aimFat(mo);
    }

    public void A_FatAttack3(final Mobj actor) {
        A_FaceTarget(actor);
        Mobj mo = g.mobjs.P_SpawnMissile(actor, actor.target, MT_FATSHOT);
        mo.angle -= FATSPREAD / 2;
        aimFat(mo);
        mo = g.mobjs.P_SpawnMissile(actor, actor.target, MT_FATSHOT);
        mo.angle += FATSPREAD / 2;
        aimFat(mo);
    }

    public void A_SkullAttack(final Mobj actor) {
        if (actor.target == null) {
            return;
        }
        final Mobj dest = actor.target;
        actor.flags |= MF_SKULLFLY;
        g.sound.S_StartSound(actor, actor.info.attacksound());
        A_FaceTarget(actor);
        final int an = actor.angle >>> ANGLETOFINESHIFT;
        actor.momx = mul(SKULLSPEED, finecosine(an));
        actor.momy = mul(SKULLSPEED, finesine[an]);
        int dist = P_AproxDistance(dest.x - actor.x, dest.y - actor.y);
        dist = dist / SKULLSPEED;
        if (dist < 1) {
            dist = 1;
        }
        actor.momz = (dest.z + (dest.height >> 1) - actor.z) / dist;
    }

    private void A_PainShootSkull(final Mobj actor, final int angle) {
        int count = 0;
        for (Thinker th = g.thinkercap.next; th != g.thinkercap; th = th.next) {
            if (th instanceof final Mobj m && th.function == Thinker.FN_ACTIVE && m.type == MT_SKULL) {
                count++;
            }
        }
        if (count > 20) {
            return;
        }
        final int an = angle >>> ANGLETOFINESHIFT;
        final int prestep = 4 * FRACUNIT + 3 * (actor.info.radius() + g.mobjinfo[MT_SKULL].radius()) / 2;
        final int x = actor.x + mul(prestep, finecosine(an));
        final int y = actor.y + mul(prestep, finesine[an]);
        final int z = actor.z + 8 * FRACUNIT;
        final Mobj newmobj = g.mobjs.P_SpawnMobj(x, y, z, MT_SKULL);
        if (!g.map.P_TryMove(newmobj, newmobj.x, newmobj.y)) {
            g.inter.P_DamageMobj(newmobj, actor, actor, 10000);
            return;
        }
        newmobj.target = actor.target;
        A_SkullAttack(newmobj);
    }

    public void A_PainAttack(final Mobj actor) {
        if (actor.target == null) {
            return;
        }
        A_FaceTarget(actor);
        A_PainShootSkull(actor, actor.angle);
    }

    public void A_PainDie(final Mobj actor) {
        A_Fall(actor);
        A_PainShootSkull(actor, actor.angle + ANG90);
        A_PainShootSkull(actor, actor.angle + ANG180);
        A_PainShootSkull(actor, actor.angle + ANG270);
    }

    public void A_Scream(final Mobj actor) {
        final int sound;
        switch (actor.info.deathsound()) {
            case 0 -> {
                return;
            }
            case sfx_podth1, sfx_podth2, sfx_podth3 -> sound = sfx_podth1 + g.random.P_Random() % 3;
            case sfx_bgdth1, sfx_bgdth2 -> sound = sfx_bgdth1 + g.random.P_Random() % 2;
            default -> sound = actor.info.deathsound();
        }
        if (actor.type == MT_SPIDER || actor.type == MT_CYBORG) {
            g.sound.S_StartSound(null, sound);
        } else {
            g.sound.S_StartSound(actor, sound);
        }
    }

    public void A_XScream(final Mobj actor) {
        g.sound.S_StartSound(actor, sfx_slop);
    }

    public void A_Pain(final Mobj actor) {
        if (actor.info.painsound() != 0) {
            g.sound.S_StartSound(actor, actor.info.painsound());
        }
    }

    public void A_Fall(final Mobj actor) {
        actor.flags &= ~MF_SOLID;
    }

    public void A_Explode(final Mobj thingy) {
        g.map.P_RadiusAttack(thingy, thingy.target, 128);
    }

    public void A_BossDeath(final Mobj mo) {
        if (g.gamemode == commercial) {
            if (g.gamemap != 7) {
                return;
            }
            if (mo.type != MT_FATSO && mo.type != MT_BABY) {
                return;
            }
        } else {
            switch (g.gameepisode) {
                case 1 -> {
                    if (g.gamemap != 8 || mo.type != MT_BRUISER) {
                        return;
                    }
                }
                case 2 -> {
                    if (g.gamemap != 8 || mo.type != MT_CYBORG) {
                        return;
                    }
                }
                case 3 -> {
                    if (g.gamemap != 8 || mo.type != MT_SPIDER) {
                        return;
                    }
                }
                case 4 -> {
                    switch (g.gamemap) {
                        case 6 -> {
                            if (mo.type != MT_CYBORG) {
                                return;
                            }
                        }
                        case 8 -> {
                            if (mo.type != MT_SPIDER) {
                                return;
                            }
                        }
                        default -> {
                            return;
                        }
                    }
                }
                default -> {
                    if (g.gamemap != 8) {
                        return;
                    }
                }
            }
        }
        int i;
        for (i = 0; i < MAXPLAYERS; i++) {
            if (g.playeringame[i] && g.players[i].health > 0) {
                break;
            }
        }
        if (i == MAXPLAYERS) {
            return;
        }
        if (anyOtherAlive(mo)) {
            return;
        }
        if (g.gamemode == commercial) {
            if (g.gamemap == 7) {
                if (mo.type == MT_FATSO) {
                    g.floors.EV_DoFloor(junkLine(666), Floors.lowerFloorToLowest);
                    return;
                }
                if (mo.type == MT_BABY) {
                    g.floors.EV_DoFloor(junkLine(667), Floors.raiseToTexture);
                    return;
                }
            }
        } else {
            switch (g.gameepisode) {
                case 1 -> {
                    g.floors.EV_DoFloor(junkLine(666), Floors.lowerFloorToLowest);
                    return;
                }
                case 4 -> {
                    switch (g.gamemap) {
                        case 6 -> {
                            g.doors.EV_DoDoor(junkLine(666), Doors.blazeOpen);
                            return;
                        }
                        case 8 -> {
                            g.floors.EV_DoFloor(junkLine(666), Floors.lowerFloorToLowest);
                            return;
                        }
                        default -> {
                        }
                    }
                }
                default -> {
                }
            }
        }
        g.G_ExitLevel();
    }

    public void A_Hoof(final Mobj mo) {
        g.sound.S_StartSound(mo, sfx_hoof);
        A_Chase(mo);
    }

    public void A_Metal(final Mobj mo) {
        g.sound.S_StartSound(mo, sfx_metal);
        A_Chase(mo);
    }

    public void A_BabyMetal(final Mobj mo) {
        g.sound.S_StartSound(mo, sfx_bspwlk);
        A_Chase(mo);
    }

    public void A_OpenShotgun2(final Player player, final PspDef psp) {
        g.sound.S_StartSound(player.mo, sfx_dbopn);
    }

    public void A_LoadShotgun2(final Player player, final PspDef psp) {
        g.sound.S_StartSound(player.mo, sfx_dbload);
    }

    public void A_CloseShotgun2(final Player player, final PspDef psp) {
        g.sound.S_StartSound(player.mo, sfx_dbcls);
        g.pspr.A_ReFire(player, psp);
    }

    public void A_BrainAwake(final Mobj mo) {
        braintargets.clear();
        braintargeton = 0;
        for (Thinker th = g.thinkercap.next; th != g.thinkercap; th = th.next) {
            if (th instanceof final Mobj m && th.function == Thinker.FN_ACTIVE && m.type == MT_BOSSTARGET) {
                braintargets.add(m);
            }
        }
        g.sound.S_StartSound(null, sfx_bossit);
    }

    public void A_BrainPain(final Mobj mo) {
        g.sound.S_StartSound(null, sfx_bospn);
    }

    public void A_BrainScream(final Mobj mo) {
        for (int x = mo.x - 196 * FRACUNIT; x < mo.x + 320 * FRACUNIT; x += FRACUNIT * 8) {
            final int y = mo.y - 320 * FRACUNIT;
            final int z = 128 + g.random.P_Random() * 2 * FRACUNIT;
            final Mobj th = g.mobjs.P_SpawnMobj(x, y, z, MT_ROCKET);
            th.momz = g.random.P_Random() * 512;
            g.mobjs.P_SetMobjState(th, S_BRAINEXPLODE1);
            th.tics -= g.random.P_Random() & 7;
            if (th.tics < 1) {
                th.tics = 1;
            }
        }
        g.sound.S_StartSound(null, sfx_bosdth);
    }

    public void A_BrainExplode(final Mobj mo) {
        final int x = mo.x + (g.random.P_Random() - g.random.P_Random()) * 2048;
        final int y = mo.y;
        final int z = 128 + g.random.P_Random() * 2 * FRACUNIT;
        final Mobj th = g.mobjs.P_SpawnMobj(x, y, z, MT_ROCKET);
        th.momz = g.random.P_Random() * 512;
        g.mobjs.P_SetMobjState(th, S_BRAINEXPLODE1);
        th.tics -= g.random.P_Random() & 7;
        if (th.tics < 1) {
            th.tics = 1;
        }
    }

    public void A_BrainDie(final Mobj mo) {
        g.G_ExitLevel();
    }

    public void A_BrainSpit(final Mobj mo) {
        easy ^= 1;
        if (g.gameskill <= sk_easy && easy == 0) {
            return;
        }
        if (braintargets.isEmpty()) {
            return;
        }
        final Mobj targ = braintargets.get(braintargeton);
        braintargeton = (braintargeton + 1) % braintargets.size();
        final Mobj newmobj = g.mobjs.P_SpawnMissile(mo, targ, MT_SPAWNSHOT);
        newmobj.target = targ;
        if (newmobj.momy != 0 && newmobj.state != null && newmobj.state.tics() != 0) {
            newmobj.reactiontime = ((targ.y - mo.y) / newmobj.momy) / newmobj.state.tics();
        }
        g.sound.S_StartSound(null, sfx_bospit);
    }

    public void A_SpawnSound(final Mobj mo) {
        g.sound.S_StartSound(mo, sfx_boscub);
        A_SpawnFly(mo);
    }

    public void A_SpawnFly(final Mobj mo) {
        if (--mo.reactiontime != 0) {
            return;
        }
        final Mobj targ = mo.target;
        final Mobj fog = g.mobjs.P_SpawnMobj(targ.x, targ.y, targ.z, MT_SPAWNFIRE);
        g.sound.S_StartSound(fog, sfx_telept);
        final int r = g.random.P_Random();
        final int type;
        if (r < 50) {
            type = MT_TROOP;
        } else if (r < 90) {
            type = MT_SERGEANT;
        } else if (r < 120) {
            type = MT_SHADOWS;
        } else if (r < 130) {
            type = MT_PAIN;
        } else if (r < 160) {
            type = MT_HEAD;
        } else if (r < 162) {
            type = MT_VILE;
        } else if (r < 172) {
            type = MT_UNDEAD;
        } else if (r < 192) {
            type = MT_BABY;
        } else if (r < 222) {
            type = MT_FATSO;
        } else if (r < 246) {
            type = MT_KNIGHT;
        } else {
            type = MT_BRUISER;
        }
        final Mobj newmobj = g.mobjs.P_SpawnMobj(targ.x, targ.y, targ.z, type);
        if (P_LookForPlayers(newmobj, true)) {
            g.mobjs.P_SetMobjState(newmobj, newmobj.info.seestate());
        }
        g.map.P_TeleportMove(newmobj, newmobj.x, newmobj.y);
        g.mobjs.P_RemoveMobj(mo);
    }

    public void A_PlayerScream(final Mobj mo) {
        int sound = sfx_pldeth;
        if (g.gamemode == commercial && mo.health < -50) {
            sound = sfx_pdiehi;
        }
        g.sound.S_StartSound(mo, sound);
    }
}
