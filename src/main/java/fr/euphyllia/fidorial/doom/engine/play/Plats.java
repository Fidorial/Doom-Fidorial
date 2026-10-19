// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine.play;

import fr.euphyllia.fidorial.doom.engine.Game;
import fr.euphyllia.fidorial.doom.engine.Line;
import fr.euphyllia.fidorial.doom.engine.Sector;
import fr.euphyllia.fidorial.doom.engine.Thinker;

import java.util.ArrayList;
import java.util.List;

import static fr.euphyllia.fidorial.doom.engine.Fixed.FRACUNIT;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_pstart;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_pstop;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_stnmov;
import static fr.euphyllia.fidorial.doom.engine.play.Floors.crushed;
import static fr.euphyllia.fidorial.doom.engine.play.Floors.pastdest;

/**
 * Lifts and platforms ({@code p_plats.c}).
 */
public final class Plats {

    public static final int up = 0;
    public static final int down = 1;
    public static final int waiting = 2;
    public static final int in_stasis = 3;

    public static final int perpetualRaise = 0;
    public static final int downWaitUpStay = 1;
    public static final int raiseAndChange = 2;
    public static final int raiseToNearestAndChange = 3;
    public static final int blazeDWUS = 4;

    public static final int PLATWAIT = 3;
    public static final int PLATSPEED = FRACUNIT;
    /**
     * {@code activeplats[]} without the 30 entry limit.
     */
    public final List<Plat> activeplats = new ArrayList<>();
    private final Game g;

    public Plats(final Game g) {
        this.g = g;
    }

    public void T_PlatRaise(final Plat plat) {
        switch (plat.status) {
            case up -> {
                final int res = g.floors.T_MovePlane(plat.sector, plat.speed, plat.high, plat.crush, 0, 1);
                if (plat.type == raiseAndChange || plat.type == raiseToNearestAndChange) {
                    if ((g.leveltime & 7) == 0) {
                        g.sound.S_StartSound(plat.sector.soundorg, sfx_stnmov);
                    }
                }
                if (res == crushed && !plat.crush) {
                    plat.count = plat.wait;
                    plat.status = down;
                    g.sound.S_StartSound(plat.sector.soundorg, sfx_pstart);
                } else if (res == pastdest) {
                    plat.count = plat.wait;
                    plat.status = waiting;
                    g.sound.S_StartSound(plat.sector.soundorg, sfx_pstop);
                    switch (plat.type) {
                        case blazeDWUS, downWaitUpStay, raiseAndChange, raiseToNearestAndChange ->
                                P_RemoveActivePlat(plat);
                        default -> {
                        }
                    }
                }
            }
            case down -> {
                final int res = g.floors.T_MovePlane(plat.sector, plat.speed, plat.low, false, 0, -1);
                if (res == pastdest) {
                    plat.count = plat.wait;
                    plat.status = waiting;
                    g.sound.S_StartSound(plat.sector.soundorg, sfx_pstop);
                }
            }
            case waiting -> {
                if (--plat.count == 0) {
                    if (plat.sector.floorheight == plat.low) {
                        plat.status = up;
                    } else {
                        plat.status = down;
                    }
                    g.sound.S_StartSound(plat.sector.soundorg, sfx_pstart);
                }
            }
            default -> {
            }
        }
    }

    public int EV_DoPlat(final Line line, final int type, final int amount) {
        final Spec spec = g.spec;
        int secnum = -1;
        int rtn = 0;
        if (type == perpetualRaise) {
            P_ActivateInStasis(line.tag);
        }
        while ((secnum = spec.P_FindSectorFromLineTag(line, secnum)) >= 0) {
            final Sector sec = g.level.sectors[secnum];
            if (sec.specialdata != null) {
                continue;
            }
            rtn = 1;
            final Plat plat = new Plat();
            g.P_AddThinker(plat);
            plat.type = type;
            plat.sector = sec;
            plat.sector.specialdata = plat;
            plat.crush = false;
            plat.tag = line.tag;
            switch (type) {
                case raiseToNearestAndChange -> {
                    plat.speed = PLATSPEED / 2;
                    sec.floorpic = g.level.sides[line.sidenum[0]].sector.floorpic;
                    plat.high = spec.P_FindNextHighestFloor(sec, sec.floorheight);
                    plat.wait = 0;
                    plat.status = up;
                    sec.special = 0;
                    g.sound.S_StartSound(sec.soundorg, sfx_stnmov);
                }
                case raiseAndChange -> {
                    plat.speed = PLATSPEED / 2;
                    sec.floorpic = g.level.sides[line.sidenum[0]].sector.floorpic;
                    plat.high = sec.floorheight + amount * FRACUNIT;
                    plat.wait = 0;
                    plat.status = up;
                    g.sound.S_StartSound(sec.soundorg, sfx_stnmov);
                }
                case downWaitUpStay -> {
                    plat.speed = PLATSPEED * 4;
                    plat.low = spec.P_FindLowestFloorSurrounding(sec);
                    if (plat.low > sec.floorheight) {
                        plat.low = sec.floorheight;
                    }
                    plat.high = sec.floorheight;
                    plat.wait = 35 * PLATWAIT;
                    plat.status = down;
                    g.sound.S_StartSound(sec.soundorg, sfx_pstart);
                }
                case blazeDWUS -> {
                    plat.speed = PLATSPEED * 8;
                    plat.low = spec.P_FindLowestFloorSurrounding(sec);
                    if (plat.low > sec.floorheight) {
                        plat.low = sec.floorheight;
                    }
                    plat.high = sec.floorheight;
                    plat.wait = 35 * PLATWAIT;
                    plat.status = down;
                    g.sound.S_StartSound(sec.soundorg, sfx_pstart);
                }
                case perpetualRaise -> {
                    plat.speed = PLATSPEED;
                    plat.low = spec.P_FindLowestFloorSurrounding(sec);
                    if (plat.low > sec.floorheight) {
                        plat.low = sec.floorheight;
                    }
                    plat.high = spec.P_FindHighestFloorSurrounding(sec);
                    if (plat.high < sec.floorheight) {
                        plat.high = sec.floorheight;
                    }
                    plat.wait = 35 * PLATWAIT;
                    plat.status = g.random.P_Random() & 1;
                    g.sound.S_StartSound(sec.soundorg, sfx_pstart);
                }
                default -> {
                }
            }
            P_AddActivePlat(plat);
        }
        return rtn;
    }

    public void P_ActivateInStasis(final int tag) {
        for (final Plat p : activeplats) {
            if (p.tag == tag && p.status == in_stasis) {
                p.status = p.oldstatus;
                p.function = Thinker.FN_ACTIVE;
            }
        }
    }

    public void EV_StopPlat(final Line line) {
        for (final Plat p : activeplats) {
            if (p.status != in_stasis && p.tag == line.tag) {
                p.oldstatus = p.status;
                p.status = in_stasis;
                p.function = Thinker.FN_NONE;
            }
        }
    }

    public void P_AddActivePlat(final Plat plat) {
        activeplats.add(plat);
    }

    public void P_RemoveActivePlat(final Plat plat) {
        if (activeplats.remove(plat)) {
            plat.sector.specialdata = null;
            g.P_RemoveThinker(plat);
        }
    }

    /**
     * {@code plat_t}.
     */
    public static final class Plat extends Thinker {
        public Sector sector;
        public int speed;
        public int low;
        public int high;
        public int wait;
        public int count;
        public int status;
        public int oldstatus;
        public boolean crush;
        public int tag;
        public int type;

        @Override
        public void think(final Game g) {
            g.plats.T_PlatRaise(this);
        }
    }
}
