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
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_pstop;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_stnmov;
import static fr.euphyllia.fidorial.doom.engine.play.Floors.crushed;
import static fr.euphyllia.fidorial.doom.engine.play.Floors.pastdest;

/**
 * Moving ceilings and crushers ({@code p_ceilng.c}).
 */
public final class Ceilings {

    public static final int lowerToFloor = 0;
    public static final int raiseToHighest = 1;
    public static final int lowerAndCrush = 2;
    public static final int crushAndRaise = 3;
    public static final int fastCrushAndRaise = 4;
    public static final int silentCrushAndRaise = 5;

    public static final int CEILSPEED = FRACUNIT;
    public static final int CEILWAIT = 150;
    /**
     * {@code activeceilings[]} without the 30 entry limit.
     */
    public final List<Ceiling> activeceilings = new ArrayList<>();
    private final Game g;

    public Ceilings(final Game g) {
        this.g = g;
    }

    public void T_MoveCeiling(final Ceiling ceiling) {
        switch (ceiling.direction) {
            case 0 -> {
            }
            case 1 -> {
                final int res = g.floors.T_MovePlane(ceiling.sector, ceiling.speed, ceiling.topheight, false, 1,
                        ceiling.direction);
                if ((g.leveltime & 7) == 0) {
                    if (ceiling.type != silentCrushAndRaise) {
                        g.sound.S_StartSound(ceiling.sector.soundorg, sfx_stnmov);
                    }
                }
                if (res == pastdest) {
                    switch (ceiling.type) {
                        case raiseToHighest -> P_RemoveActiveCeiling(ceiling);
                        case silentCrushAndRaise -> {
                            g.sound.S_StartSound(ceiling.sector.soundorg, sfx_pstop);
                            ceiling.direction = -1;
                        }
                        case fastCrushAndRaise, crushAndRaise -> ceiling.direction = -1;
                        default -> {
                        }
                    }
                }
            }
            case -1 -> {
                final int res = g.floors.T_MovePlane(ceiling.sector, ceiling.speed, ceiling.bottomheight, ceiling.crush,
                        1, ceiling.direction);
                if ((g.leveltime & 7) == 0) {
                    if (ceiling.type != silentCrushAndRaise) {
                        g.sound.S_StartSound(ceiling.sector.soundorg, sfx_stnmov);
                    }
                }
                if (res == pastdest) {
                    switch (ceiling.type) {
                        case silentCrushAndRaise -> {
                            g.sound.S_StartSound(ceiling.sector.soundorg, sfx_pstop);
                            ceiling.speed = CEILSPEED;
                            ceiling.direction = 1;
                        }
                        case crushAndRaise -> {
                            ceiling.speed = CEILSPEED;
                            ceiling.direction = 1;
                        }
                        case fastCrushAndRaise -> ceiling.direction = 1;
                        case lowerAndCrush, lowerToFloor -> P_RemoveActiveCeiling(ceiling);
                        default -> {
                        }
                    }
                } else if (res == crushed) {
                    switch (ceiling.type) {
                        case silentCrushAndRaise, crushAndRaise, lowerAndCrush -> ceiling.speed = CEILSPEED / 8;
                        default -> {
                        }
                    }
                }
            }
            default -> {
            }
        }
    }

    public int EV_DoCeiling(final Line line, final int type) {
        int secnum = -1;
        int rtn = 0;
        switch (type) {
            case fastCrushAndRaise, silentCrushAndRaise, crushAndRaise -> P_ActivateInStasisCeiling(line);
            default -> {
            }
        }
        while ((secnum = g.spec.P_FindSectorFromLineTag(line, secnum)) >= 0) {
            final Sector sec = g.level.sectors[secnum];
            if (sec.specialdata != null) {
                continue;
            }
            rtn = 1;
            final Ceiling ceiling = new Ceiling();
            g.P_AddThinker(ceiling);
            sec.specialdata = ceiling;
            ceiling.sector = sec;
            ceiling.crush = false;
            switch (type) {
                case fastCrushAndRaise -> {
                    ceiling.crush = true;
                    ceiling.topheight = sec.ceilingheight;
                    ceiling.bottomheight = sec.floorheight + (8 * FRACUNIT);
                    ceiling.direction = -1;
                    ceiling.speed = CEILSPEED * 2;
                }
                case silentCrushAndRaise, crushAndRaise, lowerAndCrush, lowerToFloor -> {
                    if (type == silentCrushAndRaise || type == crushAndRaise) {
                        ceiling.crush = true;
                        ceiling.topheight = sec.ceilingheight;
                    }
                    ceiling.bottomheight = sec.floorheight;
                    if (type != lowerToFloor) {
                        ceiling.bottomheight += 8 * FRACUNIT;
                    }
                    ceiling.direction = -1;
                    ceiling.speed = CEILSPEED;
                }
                case raiseToHighest -> {
                    ceiling.topheight = g.spec.P_FindHighestCeilingSurrounding(sec);
                    ceiling.direction = 1;
                    ceiling.speed = CEILSPEED;
                }
                default -> {
                }
            }
            ceiling.tag = sec.tag;
            ceiling.type = type;
            P_AddActiveCeiling(ceiling);
        }
        return rtn;
    }

    public void P_AddActiveCeiling(final Ceiling c) {
        activeceilings.add(c);
    }

    public void P_RemoveActiveCeiling(final Ceiling c) {
        if (activeceilings.remove(c)) {
            c.sector.specialdata = null;
            g.P_RemoveThinker(c);
        }
    }

    public void P_ActivateInStasisCeiling(final Line line) {
        for (final Ceiling c : activeceilings) {
            if (c.tag == line.tag && c.direction == 0) {
                c.direction = c.olddirection;
                c.function = Thinker.FN_ACTIVE;
            }
        }
    }

    public int EV_CeilingCrushStop(final Line line) {
        int rtn = 0;
        for (final Ceiling c : activeceilings) {
            if (c.tag == line.tag && c.direction != 0) {
                c.olddirection = c.direction;
                c.function = Thinker.FN_NONE;
                c.direction = 0;
                rtn = 1;
            }
        }
        return rtn;
    }

    /**
     * {@code ceiling_t}.
     */
    public static final class Ceiling extends Thinker {
        public int type;
        public Sector sector;
        public int bottomheight;
        public int topheight;
        public int speed;
        public boolean crush;
        public int direction;
        public int tag;
        public int olddirection;

        @Override
        public void think(final Game g) {
            g.ceilings.T_MoveCeiling(this);
        }
    }
}
