// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine.play;

import fr.euphyllia.fidorial.doom.engine.Game;
import fr.euphyllia.fidorial.doom.engine.MapThing;
import fr.euphyllia.fidorial.doom.engine.Mobj;
import fr.euphyllia.fidorial.doom.engine.Player;
import fr.euphyllia.fidorial.doom.engine.Subsector;
import fr.euphyllia.fidorial.doom.engine.Thinker;
import fr.euphyllia.fidorial.doom.engine.info.MobjInfo;
import fr.euphyllia.fidorial.doom.engine.info.State;

import static fr.euphyllia.fidorial.doom.engine.Defs.CF_NOMOMENTUM;
import static fr.euphyllia.fidorial.doom.engine.Defs.FLOATSPEED;
import static fr.euphyllia.fidorial.doom.engine.Defs.GRAVITY;
import static fr.euphyllia.fidorial.doom.engine.Defs.MAXMOVE;
import static fr.euphyllia.fidorial.doom.engine.Defs.MAXPLAYERS;
import static fr.euphyllia.fidorial.doom.engine.Defs.MELEERANGE;
import static fr.euphyllia.fidorial.doom.engine.Defs.MTF_AMBUSH;
import static fr.euphyllia.fidorial.doom.engine.Defs.NUMCARDS;
import static fr.euphyllia.fidorial.doom.engine.Defs.ONCEILINGZ;
import static fr.euphyllia.fidorial.doom.engine.Defs.ONFLOORZ;
import static fr.euphyllia.fidorial.doom.engine.Defs.PST_LIVE;
import static fr.euphyllia.fidorial.doom.engine.Defs.PST_REBORN;
import static fr.euphyllia.fidorial.doom.engine.Defs.VIEWHEIGHT;
import static fr.euphyllia.fidorial.doom.engine.Defs.sk_baby;
import static fr.euphyllia.fidorial.doom.engine.Defs.sk_nightmare;
import static fr.euphyllia.fidorial.doom.engine.Fixed.FRACBITS;
import static fr.euphyllia.fidorial.doom.engine.Fixed.FRACUNIT;
import static fr.euphyllia.fidorial.doom.engine.Fixed.mul;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_AMBUSH;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_CORPSE;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_COUNTITEM;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_COUNTKILL;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_DROPPED;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_FLOAT;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_INFLOAT;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_MISSILE;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_NOCLIP;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_NOGRAVITY;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_NOTDMATCH;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_SHADOW;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_SKULLFLY;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_SPAWNCEILING;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_SPECIAL;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_TRANSSHIFT;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_itmbk;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_oof;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_telept;
import static fr.euphyllia.fidorial.doom.engine.Tables.ANG45;
import static fr.euphyllia.fidorial.doom.engine.Tables.ANGLETOFINESHIFT;
import static fr.euphyllia.fidorial.doom.engine.Tables.finecosine;
import static fr.euphyllia.fidorial.doom.engine.Tables.finesine;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_BLOOD;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_IFOG;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_INS;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_INV;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_PLAYER;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_PUFF;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_SKULL;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_TFOG;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.NUMMOBJTYPES;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BLOOD2;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BLOOD3;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_NULL;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_PLAY;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_PLAY_RUN1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_PUFF3;

/**
 * Map object spawning, movement and state changes ({@code p_mobj.c}).
 */
public final class MobjSys {

    public static final int ITEMQUESIZE = 128;
    private static final int STOPSPEED = 0x1000;
    private static final int FRICTION = 0xe800;
    public final MapThing[] itemrespawnque = new MapThing[ITEMQUESIZE];
    public final int[] itemrespawntime = new int[ITEMQUESIZE];
    private final Game g;
    public int iquehead;
    public int iquetail;

    public MobjSys(final Game g) {
        this.g = g;
        for (int i = 0; i < ITEMQUESIZE; i++) {
            itemrespawnque[i] = new MapThing();
        }
    }

    public boolean P_SetMobjState(final Mobj mobj, int state) {
        do {
            if (state == S_NULL) {
                mobj.state = null;
                P_RemoveMobj(mobj);
                return false;
            }
            final State st = g.states[state];
            mobj.state = st;
            mobj.tics = st.tics();
            mobj.sprite = st.sprite();
            mobj.frame = st.frame();
            if (st.action() != null) {
                g.actions.callMobj(st.action(), mobj);
            }
            state = st.nextstate();
        } while (mobj.tics == 0);
        return true;
    }

    public void P_ExplodeMissile(final Mobj mo) {
        mo.momx = mo.momy = mo.momz = 0;
        P_SetMobjState(mo, g.mobjinfo[mo.type].deathstate());
        mo.tics -= g.random.P_Random() & 3;
        if (mo.tics < 1) {
            mo.tics = 1;
        }
        mo.flags &= ~MF_MISSILE;
        if (mo.info.deathsound() != 0) {
            g.sound.S_StartSound(mo, mo.info.deathsound());
        }
    }

    private void P_XYMovement(final Mobj mo) {
        if (mo.momx == 0 && mo.momy == 0) {
            if ((mo.flags & MF_SKULLFLY) != 0) {
                mo.flags &= ~MF_SKULLFLY;
                mo.momx = mo.momy = mo.momz = 0;
                P_SetMobjState(mo, mo.info.spawnstate());
            }
            return;
        }
        final Player player = mo.player;
        if (mo.momx > MAXMOVE) {
            mo.momx = MAXMOVE;
        } else if (mo.momx < -MAXMOVE) {
            mo.momx = -MAXMOVE;
        }
        if (mo.momy > MAXMOVE) {
            mo.momy = MAXMOVE;
        } else if (mo.momy < -MAXMOVE) {
            mo.momy = -MAXMOVE;
        }
        int xmove = mo.momx;
        int ymove = mo.momy;
        do {
            final int ptryx;
            final int ptryy;
            if (xmove > MAXMOVE / 2 || ymove > MAXMOVE / 2) {
                ptryx = mo.x + xmove / 2;
                ptryy = mo.y + ymove / 2;
                xmove >>= 1;
                ymove >>= 1;
            } else {
                ptryx = mo.x + xmove;
                ptryy = mo.y + ymove;
                xmove = ymove = 0;
            }
            if (!g.map.P_TryMove(mo, ptryx, ptryy)) {
                if (mo.player != null) {
                    g.map.P_SlideMove(mo);
                } else if ((mo.flags & MF_MISSILE) != 0) {
                    final var ceilingline = g.map.ceilingline;
                    if (ceilingline != null && ceilingline.backsector != null
                            && ceilingline.backsector.ceilingpic == g.skyflatnum) {
                        P_RemoveMobj(mo);
                        return;
                    }
                    P_ExplodeMissile(mo);
                } else {
                    mo.momx = mo.momy = 0;
                }
            }
        } while (xmove != 0 || ymove != 0);
        if (player != null && (player.cheats & CF_NOMOMENTUM) != 0) {
            mo.momx = mo.momy = 0;
            return;
        }
        if ((mo.flags & (MF_MISSILE | MF_SKULLFLY)) != 0) {
            return;
        }
        if (mo.z > mo.floorz) {
            return;
        }
        if ((mo.flags & MF_CORPSE) != 0) {
            if (mo.momx > FRACUNIT / 4 || mo.momx < -FRACUNIT / 4 || mo.momy > FRACUNIT / 4 || mo.momy < -FRACUNIT / 4) {
                if (mo.floorz != mo.subsector.sector.floorheight) {
                    return;
                }
            }
        }
        if (mo.momx > -STOPSPEED && mo.momx < STOPSPEED && mo.momy > -STOPSPEED && mo.momy < STOPSPEED
                && (player == null || (player.cmd.forwardmove == 0 && player.cmd.sidemove == 0))) {
            if (player != null && Integer.compareUnsigned(player.mo.stateIndex() - S_PLAY_RUN1, 4) < 0) {
                P_SetMobjState(player.mo, S_PLAY);
            }
            mo.momx = 0;
            mo.momy = 0;
        } else {
            mo.momx = mul(mo.momx, FRICTION);
            mo.momy = mul(mo.momy, FRICTION);
        }
    }

    private void P_ZMovement(final Mobj mo) {
        if (mo.player != null && mo.z < mo.floorz) {
            mo.player.viewheight -= mo.floorz - mo.z;
            mo.player.deltaviewheight = (VIEWHEIGHT - mo.player.viewheight) >> 3;
        }
        mo.z += mo.momz;
        if ((mo.flags & MF_FLOAT) != 0 && mo.target != null) {
            if ((mo.flags & MF_SKULLFLY) == 0 && (mo.flags & MF_INFLOAT) == 0) {
                final int dist = MapUtil.P_AproxDistance(mo.x - mo.target.x, mo.y - mo.target.y);
                final int delta = (mo.target.z + (mo.height >> 1)) - mo.z;
                if (delta < 0 && dist < -(delta * 3)) {
                    mo.z -= FLOATSPEED;
                } else if (delta > 0 && dist < (delta * 3)) {
                    mo.z += FLOATSPEED;
                }
            }
        }
        if (mo.z <= mo.floorz) {
            // linuxdoom has the Ultimate Doom bounce; Doom 1.9 and Doom II reverse the momentum after zeroing it
            final boolean correctBounce = g.correctLostSoulBounce;
            if (correctBounce && (mo.flags & MF_SKULLFLY) != 0) {
                mo.momz = -mo.momz;
            }
            if (mo.momz < 0) {
                if (mo.player != null && mo.momz < -GRAVITY * 8) {
                    mo.player.deltaviewheight = mo.momz >> 3;
                    g.sound.S_StartSound(mo, sfx_oof);
                }
                mo.momz = 0;
            }
            mo.z = mo.floorz;
            if (!correctBounce && (mo.flags & MF_SKULLFLY) != 0) {
                mo.momz = -mo.momz;
            }
            if ((mo.flags & MF_MISSILE) != 0 && (mo.flags & MF_NOCLIP) == 0) {
                P_ExplodeMissile(mo);
                return;
            }
        } else if ((mo.flags & MF_NOGRAVITY) == 0) {
            if (mo.momz == 0) {
                mo.momz = -GRAVITY * 2;
            } else {
                mo.momz -= GRAVITY;
            }
        }
        if (mo.z + mo.height > mo.ceilingz) {
            if (mo.momz > 0) {
                mo.momz = 0;
            }
            mo.z = mo.ceilingz - mo.height;
            if ((mo.flags & MF_SKULLFLY) != 0) {
                mo.momz = -mo.momz;
            }
            if ((mo.flags & MF_MISSILE) != 0 && (mo.flags & MF_NOCLIP) == 0) {
                P_ExplodeMissile(mo);
            }
        }
    }

    private void P_NightmareRespawn(final Mobj mobj) {
        final int x = mobj.spawnpoint.x << FRACBITS;
        final int y = mobj.spawnpoint.y << FRACBITS;
        if (!g.map.P_CheckPosition(mobj, x, y)) {
            return;
        }
        Mobj mo = P_SpawnMobj(mobj.x, mobj.y, mobj.subsector.sector.floorheight, MT_TFOG);
        g.sound.S_StartSound(mo, sfx_telept);
        final Subsector ss = g.level.pointInSubsector(x, y);
        mo = P_SpawnMobj(x, y, ss.sector.floorheight, MT_TFOG);
        g.sound.S_StartSound(mo, sfx_telept);
        final MapThing mthing = mobj.spawnpoint;
        final int z = (mobj.info.flags() & MF_SPAWNCEILING) != 0 ? ONCEILINGZ : ONFLOORZ;
        mo = P_SpawnMobj(x, y, z, mobj.type);
        mo.spawnpoint.copyFrom(mobj.spawnpoint);
        mo.angle = ANG45 * (mthing.angle / 45);
        if ((mthing.options & MTF_AMBUSH) != 0) {
            mo.flags |= MF_AMBUSH;
        }
        mo.reactiontime = 18;
        P_RemoveMobj(mobj);
    }

    public void P_MobjThinker(final Mobj mobj) {
        if (mobj.momx != 0 || mobj.momy != 0 || (mobj.flags & MF_SKULLFLY) != 0) {
            P_XYMovement(mobj);
            if (mobj.function == Thinker.FN_REMOVED) {
                return;
            }
        }
        if (mobj.z != mobj.floorz || mobj.momz != 0) {
            P_ZMovement(mobj);
            if (mobj.function == Thinker.FN_REMOVED) {
                return;
            }
        }
        if (mobj.tics != -1) {
            mobj.tics--;
            if (mobj.tics == 0) {
                if (!P_SetMobjState(mobj, mobj.state.nextstate())) {
                }
            }
        } else {
            if ((mobj.flags & MF_COUNTKILL) == 0) {
                return;
            }
            if (!g.respawnmonsters) {
                return;
            }
            mobj.movecount++;
            if (mobj.movecount < 12 * 35) {
                return;
            }
            if ((g.leveltime & 31) != 0) {
                return;
            }
            if (g.random.P_Random() > 4) {
                return;
            }
            P_NightmareRespawn(mobj);
        }
    }

    public Mobj P_SpawnMobj(final int x, final int y, final int z, final int type) {
        final Mobj mobj = new Mobj();
        final MobjInfo info = g.mobjinfo[type];
        mobj.type = type;
        mobj.info = info;
        mobj.x = x;
        mobj.y = y;
        mobj.radius = info.radius();
        mobj.height = info.height();
        mobj.flags = info.flags();
        mobj.health = info.spawnhealth();
        if (g.gameskill != sk_nightmare) {
            mobj.reactiontime = info.reactiontime();
        }
        mobj.lastlook = g.random.P_Random() % MAXPLAYERS;
        final State st = g.states[info.spawnstate()];
        mobj.state = st;
        mobj.tics = st.tics();
        mobj.sprite = st.sprite();
        mobj.frame = st.frame();
        g.mapUtil.P_SetThingPosition(mobj);
        mobj.floorz = mobj.subsector.sector.floorheight;
        mobj.ceilingz = mobj.subsector.sector.ceilingheight;
        if (z == ONFLOORZ) {
            mobj.z = mobj.floorz;
        } else if (z == ONCEILINGZ) {
            mobj.z = mobj.ceilingz - mobj.info.height();
        } else {
            mobj.z = z;
        }
        mobj.function = Thinker.FN_ACTIVE;
        g.P_AddThinker(mobj);
        return mobj;
    }

    public void P_RemoveMobj(final Mobj mobj) {
        if ((mobj.flags & MF_SPECIAL) != 0 && (mobj.flags & MF_DROPPED) == 0 && mobj.type != MT_INV && mobj.type != MT_INS) {
            itemrespawnque[iquehead].copyFrom(mobj.spawnpoint);
            itemrespawntime[iquehead] = g.leveltime;
            iquehead = (iquehead + 1) & (ITEMQUESIZE - 1);
            if (iquehead == iquetail) {
                iquetail = (iquetail + 1) & (ITEMQUESIZE - 1);
            }
        }
        g.mapUtil.P_UnsetThingPosition(mobj);
        g.sound.S_StopSound(mobj);
        g.P_RemoveThinker(mobj);
    }

    public void P_RespawnSpecials() {
        if (g.deathmatch != 2) {
            return;
        }
        if (iquehead == iquetail) {
            return;
        }
        if (g.leveltime - itemrespawntime[iquetail] < 30 * 35) {
            return;
        }
        final MapThing mthing = itemrespawnque[iquetail];
        final int x = mthing.x << FRACBITS;
        final int y = mthing.y << FRACBITS;
        final Subsector ss = g.level.pointInSubsector(x, y);
        Mobj mo = P_SpawnMobj(x, y, ss.sector.floorheight, MT_IFOG);
        g.sound.S_StartSound(mo, sfx_itmbk);
        int i;
        for (i = 0; i < NUMMOBJTYPES; i++) {
            if (mthing.type == g.mobjinfo[i].doomednum()) {
                break;
            }
        }
        if (i == NUMMOBJTYPES) {
            iquetail = (iquetail + 1) & (ITEMQUESIZE - 1);
            return;
        }
        final int z = (g.mobjinfo[i].flags() & MF_SPAWNCEILING) != 0 ? ONCEILINGZ : ONFLOORZ;
        mo = P_SpawnMobj(x, y, z, i);
        mo.spawnpoint.copyFrom(mthing);
        mo.angle = ANG45 * (mthing.angle / 45);
        iquetail = (iquetail + 1) & (ITEMQUESIZE - 1);
    }

    public void P_SpawnPlayer(final MapThing mthing) {
        if (!g.playeringame[mthing.type - 1]) {
            return;
        }
        final Player p = g.players[mthing.type - 1];
        if (p.playerstate == PST_REBORN) {
            g.G_PlayerReborn(mthing.type - 1);
        }
        final int x = mthing.x << FRACBITS;
        final int y = mthing.y << FRACBITS;
        final Mobj mobj = P_SpawnMobj(x, y, ONFLOORZ, MT_PLAYER);
        if (mthing.type > 1) {
            mobj.flags |= (mthing.type - 1) << MF_TRANSSHIFT;
        }
        mobj.angle = ANG45 * (mthing.angle / 45);
        mobj.player = p;
        mobj.health = p.health;
        p.mo = mobj;
        p.playerstate = PST_LIVE;
        p.refire = 0;
        p.message = null;
        p.damagecount = 0;
        p.bonuscount = 0;
        p.extralight = 0;
        p.fixedcolormap = 0;
        p.viewheight = VIEWHEIGHT;
        g.pspr.P_SetupPsprites(p);
        if (g.deathmatch != 0) {
            for (int i = 0; i < NUMCARDS; i++) {
                p.cards[i] = true;
            }
        }
        g.listener().playerSpawned(p);
    }

    public void P_SpawnMapThing(final MapThing mthing) {
        if (mthing.type == 11) {
            if (g.deathmatchCount < g.deathmatchstarts.length) {
                g.deathmatchstarts[g.deathmatchCount].copyFrom(mthing);
                g.deathmatchCount++;
            }
            return;
        }
        if (mthing.type <= 0) {
            return;
        }
        if (mthing.type <= 4) {
            g.playerstarts[mthing.type - 1].copyFrom(mthing);
            g.playerstartSet[mthing.type - 1] = true;
            if (g.deathmatch == 0) {
                P_SpawnPlayer(mthing);
            }
            return;
        }
        if (!g.netgame && (mthing.options & 16) != 0) {
            return;
        }
        final int bit;
        if (g.gameskill == sk_baby) {
            bit = 1;
        } else if (g.gameskill == sk_nightmare) {
            bit = 4;
        } else {
            bit = 1 << (g.gameskill - 1);
        }
        if ((mthing.options & bit) == 0) {
            return;
        }
        int i;
        for (i = 0; i < NUMMOBJTYPES; i++) {
            if (mthing.type == g.mobjinfo[i].doomednum()) {
                break;
            }
        }
        if (i == NUMMOBJTYPES) {
            g.warn("P_SpawnMapThing: Unknown type " + mthing.type + " at (" + mthing.x + ", " + mthing.y + ")");
            return;
        }
        if (g.deathmatch != 0 && (g.mobjinfo[i].flags() & MF_NOTDMATCH) != 0) {
            return;
        }
        if (g.nomonsters && (i == MT_SKULL || (g.mobjinfo[i].flags() & MF_COUNTKILL) != 0)) {
            return;
        }
        final int x = mthing.x << FRACBITS;
        final int y = mthing.y << FRACBITS;
        final int z = (g.mobjinfo[i].flags() & MF_SPAWNCEILING) != 0 ? ONCEILINGZ : ONFLOORZ;
        final Mobj mobj = P_SpawnMobj(x, y, z, i);
        mobj.spawnpoint.copyFrom(mthing);
        if (mobj.tics > 0) {
            mobj.tics = 1 + (g.random.P_Random() % mobj.tics);
        }
        if ((mobj.flags & MF_COUNTKILL) != 0) {
            g.totalkills++;
        }
        if ((mobj.flags & MF_COUNTITEM) != 0) {
            g.totalitems++;
        }
        mobj.angle = ANG45 * (mthing.angle / 45);
        if ((mthing.options & MTF_AMBUSH) != 0) {
            mobj.flags |= MF_AMBUSH;
        }
    }

    public void P_SpawnPuff(final int x, final int y, int z) {
        z += (g.random.P_Random() - g.random.P_Random()) << 10;
        final Mobj th = P_SpawnMobj(x, y, z, MT_PUFF);
        th.momz = FRACUNIT;
        th.tics -= g.random.P_Random() & 3;
        if (th.tics < 1) {
            th.tics = 1;
        }
        if (g.map.attackrange == MELEERANGE) {
            P_SetMobjState(th, S_PUFF3);
        }
    }

    public void P_SpawnBlood(final int x, final int y, int z, final int damage) {
        z += (g.random.P_Random() - g.random.P_Random()) << 10;
        final Mobj th = P_SpawnMobj(x, y, z, MT_BLOOD);
        th.momz = FRACUNIT * 2;
        th.tics -= g.random.P_Random() & 3;
        if (th.tics < 1) {
            th.tics = 1;
        }
        if (damage <= 12 && damage >= 9) {
            P_SetMobjState(th, S_BLOOD2);
        } else if (damage < 9) {
            P_SetMobjState(th, S_BLOOD3);
        }
    }

    public void P_CheckMissileSpawn(final Mobj th) {
        th.tics -= g.random.P_Random() & 3;
        if (th.tics < 1) {
            th.tics = 1;
        }
        th.x += th.momx >> 1;
        th.y += th.momy >> 1;
        th.z += th.momz >> 1;
        if (!g.map.P_TryMove(th, th.x, th.y)) {
            P_ExplodeMissile(th);
        }
    }

    public Mobj P_SpawnMissile(final Mobj source, final Mobj dest, final int type) {
        final Mobj th = P_SpawnMobj(source.x, source.y, source.z + 4 * 8 * FRACUNIT, type);
        if (th.info.seesound() != 0) {
            g.sound.S_StartSound(th, th.info.seesound());
        }
        th.target = source;
        int an = fr.euphyllia.fidorial.doom.engine.Angles.pointToAngle2(source.x, source.y, dest.x, dest.y);
        if ((dest.flags & MF_SHADOW) != 0) {
            an += (g.random.P_Random() - g.random.P_Random()) << 20;
        }
        th.angle = an;
        an >>>= ANGLETOFINESHIFT;
        th.momx = mul(th.info.speed(), finecosine(an));
        th.momy = mul(th.info.speed(), finesine[an]);
        int dist = MapUtil.P_AproxDistance(dest.x - source.x, dest.y - source.y);
        dist = dist / th.info.speed();
        if (dist < 1) {
            dist = 1;
        }
        th.momz = (dest.z - source.z) / dist;
        P_CheckMissileSpawn(th);
        return th;
    }

    public void P_SpawnPlayerMissile(final Mobj source, final int type) {
        int an = source.angle;
        int slope = g.map.P_AimLineAttack(source, an, 16 * 64 * FRACUNIT);
        if (g.map.linetarget == null) {
            an += 1 << 26;
            slope = g.map.P_AimLineAttack(source, an, 16 * 64 * FRACUNIT);
            if (g.map.linetarget == null) {
                an -= 2 << 26;
                slope = g.map.P_AimLineAttack(source, an, 16 * 64 * FRACUNIT);
            }
            if (g.map.linetarget == null) {
                an = source.angle;
                slope = 0;
            }
        }
        final int x = source.x;
        final int y = source.y;
        final int z = source.z + 4 * 8 * FRACUNIT;
        final Mobj th = P_SpawnMobj(x, y, z, type);
        if (th.info.seesound() != 0) {
            g.sound.S_StartSound(th, th.info.seesound());
        }
        th.target = source;
        th.angle = an;
        th.momx = mul(th.info.speed(), finecosine(an >>> ANGLETOFINESHIFT));
        th.momy = mul(th.info.speed(), finesine[an >>> ANGLETOFINESHIFT]);
        th.momz = mul(th.info.speed(), slope);
        P_CheckMissileSpawn(th);
    }
}
