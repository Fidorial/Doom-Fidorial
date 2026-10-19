// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine.play;

import fr.euphyllia.fidorial.doom.engine.Game;
import fr.euphyllia.fidorial.doom.engine.Line;
import fr.euphyllia.fidorial.doom.engine.Sector;
import fr.euphyllia.fidorial.doom.engine.Side;
import fr.euphyllia.fidorial.doom.engine.Thinker;

import static fr.euphyllia.fidorial.doom.engine.Defs.ML_TWOSIDED;
import static fr.euphyllia.fidorial.doom.engine.Fixed.FRACUNIT;
import static fr.euphyllia.fidorial.doom.engine.Fixed.MAXINT;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_pstop;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_stnmov;

/**
 * Moving floors, stairs and donuts ({@code p_floor.c}, {@code EV_DoDonut}).
 */
public final class Floors {

    public static final int lowerFloor = 0;
    public static final int lowerFloorToLowest = 1;
    public static final int turboLower = 2;
    public static final int raiseFloor = 3;
    public static final int raiseFloorToNearest = 4;
    public static final int raiseToTexture = 5;
    public static final int lowerAndChange = 6;
    public static final int raiseFloor24 = 7;
    public static final int raiseFloor24AndChange = 8;
    public static final int raiseFloorCrush = 9;
    public static final int raiseFloorTurbo = 10;
    public static final int donutRaise = 11;
    public static final int raiseFloor512 = 12;

    public static final int build8 = 0;
    public static final int turbo16 = 1;

    public static final int ok = 0;
    public static final int crushed = 1;
    public static final int pastdest = 2;

    public static final int FLOORSPEED = FRACUNIT;

    private final Game g;

    public Floors(final Game g) {
        this.g = g;
    }

    public int T_MovePlane(final Sector sector, final int speed, final int dest, final boolean crush,
                           final int floorOrCeiling, final int direction) {
        final MapMove map = g.map;
        final boolean flag;
        final int lastpos;
        switch (floorOrCeiling) {
            case 0 -> {
                switch (direction) {
                    case -1 -> {
                        if (sector.floorheight - speed < dest) {
                            lastpos = sector.floorheight;
                            sector.floorheight = dest;
                            flag = map.P_ChangeSector(sector, crush);
                            if (flag) {
                                sector.floorheight = lastpos;
                                map.P_ChangeSector(sector, crush);
                            }
                            return pastdest;
                        } else {
                            lastpos = sector.floorheight;
                            sector.floorheight -= speed;
                            flag = map.P_ChangeSector(sector, crush);
                            if (flag) {
                                sector.floorheight = lastpos;
                                map.P_ChangeSector(sector, crush);
                                return crushed;
                            }
                        }
                    }
                    case 1 -> {
                        if (sector.floorheight + speed > dest) {
                            lastpos = sector.floorheight;
                            sector.floorheight = dest;
                            flag = map.P_ChangeSector(sector, crush);
                            if (flag) {
                                sector.floorheight = lastpos;
                                map.P_ChangeSector(sector, crush);
                            }
                            return pastdest;
                        } else {
                            lastpos = sector.floorheight;
                            sector.floorheight += speed;
                            flag = map.P_ChangeSector(sector, crush);
                            if (flag) {
                                if (crush) {
                                    return crushed;
                                }
                                sector.floorheight = lastpos;
                                map.P_ChangeSector(sector, crush);
                                return crushed;
                            }
                        }
                    }
                    default -> {
                    }
                }
            }
            case 1 -> {
                switch (direction) {
                    case -1 -> {
                        if (sector.ceilingheight - speed < dest) {
                            lastpos = sector.ceilingheight;
                            sector.ceilingheight = dest;
                            flag = map.P_ChangeSector(sector, crush);
                            if (flag) {
                                sector.ceilingheight = lastpos;
                                map.P_ChangeSector(sector, crush);
                            }
                            return pastdest;
                        } else {
                            lastpos = sector.ceilingheight;
                            sector.ceilingheight -= speed;
                            flag = map.P_ChangeSector(sector, crush);
                            if (flag) {
                                if (crush) {
                                    return crushed;
                                }
                                sector.ceilingheight = lastpos;
                                map.P_ChangeSector(sector, crush);
                                return crushed;
                            }
                        }
                    }
                    case 1 -> {
                        if (sector.ceilingheight + speed > dest) {
                            lastpos = sector.ceilingheight;
                            sector.ceilingheight = dest;
                            flag = map.P_ChangeSector(sector, crush);
                            if (flag) {
                                sector.ceilingheight = lastpos;
                                map.P_ChangeSector(sector, crush);
                            }
                            return pastdest;
                        } else {
                            sector.ceilingheight += speed;
                            map.P_ChangeSector(sector, crush);
                        }
                    }
                    default -> {
                    }
                }
            }
            default -> {
            }
        }
        return ok;
    }

    public void T_MoveFloor(final FloorMove floor) {
        final int res = T_MovePlane(floor.sector, floor.speed, floor.floordestheight, floor.crush, 0, floor.direction);
        if ((g.leveltime & 7) == 0) {
            g.sound.S_StartSound(floor.sector.soundorg, sfx_stnmov);
        }
        if (res == pastdest) {
            floor.sector.specialdata = null;
            if (floor.direction == 1) {
                if (floor.type == donutRaise) {
                    floor.sector.special = (short) floor.newspecial;
                    floor.sector.floorpic = floor.texture;
                }
            } else if (floor.direction == -1) {
                if (floor.type == lowerAndChange) {
                    floor.sector.special = (short) floor.newspecial;
                    floor.sector.floorpic = floor.texture;
                }
            }
            g.P_RemoveThinker(floor);
            g.sound.S_StartSound(floor.sector.soundorg, sfx_pstop);
        }
    }

    public int EV_DoFloor(final Line line, final int floortype) {
        final Spec spec = g.spec;
        int secnum = -1;
        int rtn = 0;
        while ((secnum = spec.P_FindSectorFromLineTag(line, secnum)) >= 0) {
            Sector sec = g.level.sectors[secnum];
            if (sec.specialdata != null) {
                continue;
            }
            rtn = 1;
            final FloorMove floor = new FloorMove();
            g.P_AddThinker(floor);
            sec.specialdata = floor;
            floor.type = floortype;
            floor.crush = false;
            switch (floortype) {
                case lowerFloor -> {
                    floor.direction = -1;
                    floor.sector = sec;
                    floor.speed = FLOORSPEED;
                    floor.floordestheight = spec.P_FindHighestFloorSurrounding(sec);
                }
                case lowerFloorToLowest -> {
                    floor.direction = -1;
                    floor.sector = sec;
                    floor.speed = FLOORSPEED;
                    floor.floordestheight = spec.P_FindLowestFloorSurrounding(sec);
                }
                case turboLower -> {
                    floor.direction = -1;
                    floor.sector = sec;
                    floor.speed = FLOORSPEED * 4;
                    floor.floordestheight = spec.P_FindHighestFloorSurrounding(sec);
                    if (floor.floordestheight != sec.floorheight) {
                        floor.floordestheight += 8 * FRACUNIT;
                    }
                }
                case raiseFloorCrush, raiseFloor -> {
                    if (floortype == raiseFloorCrush) {
                        floor.crush = true;
                    }
                    floor.direction = 1;
                    floor.sector = sec;
                    floor.speed = FLOORSPEED;
                    floor.floordestheight = spec.P_FindLowestCeilingSurrounding(sec);
                    if (floor.floordestheight > sec.ceilingheight) {
                        floor.floordestheight = sec.ceilingheight;
                    }
                    floor.floordestheight -= (8 * FRACUNIT) * (floortype == raiseFloorCrush ? 1 : 0);
                }
                case raiseFloorTurbo -> {
                    floor.direction = 1;
                    floor.sector = sec;
                    floor.speed = FLOORSPEED * 4;
                    floor.floordestheight = spec.P_FindNextHighestFloor(sec, sec.floorheight);
                }
                case raiseFloorToNearest -> {
                    floor.direction = 1;
                    floor.sector = sec;
                    floor.speed = FLOORSPEED;
                    floor.floordestheight = spec.P_FindNextHighestFloor(sec, sec.floorheight);
                }
                case raiseFloor24 -> {
                    floor.direction = 1;
                    floor.sector = sec;
                    floor.speed = FLOORSPEED;
                    floor.floordestheight = floor.sector.floorheight + 24 * FRACUNIT;
                }
                case raiseFloor512 -> {
                    floor.direction = 1;
                    floor.sector = sec;
                    floor.speed = FLOORSPEED;
                    floor.floordestheight = floor.sector.floorheight + 512 * FRACUNIT;
                }
                case raiseFloor24AndChange -> {
                    floor.direction = 1;
                    floor.sector = sec;
                    floor.speed = FLOORSPEED;
                    floor.floordestheight = floor.sector.floorheight + 24 * FRACUNIT;
                    sec.floorpic = line.frontsector.floorpic;
                    sec.special = line.frontsector.special;
                }
                case raiseToTexture -> {
                    int minsize = MAXINT;
                    floor.direction = 1;
                    floor.sector = sec;
                    floor.speed = FLOORSPEED;
                    for (int i = 0; i < sec.linecount; i++) {
                        if (spec.twoSided(secnum, i)) {
                            Side side = spec.getSide(secnum, i, 0);
                            if (side.bottomtexture >= 0) {
                                final int h = g.textureheight(side.bottomtexture);
                                if (h < minsize) {
                                    minsize = h;
                                }
                            }
                            side = spec.getSide(secnum, i, 1);
                            if (side.bottomtexture >= 0) {
                                final int h = g.textureheight(side.bottomtexture);
                                if (h < minsize) {
                                    minsize = h;
                                }
                            }
                        }
                    }
                    floor.floordestheight = floor.sector.floorheight + minsize;
                }
                case lowerAndChange -> {
                    floor.direction = -1;
                    floor.sector = sec;
                    floor.speed = FLOORSPEED;
                    floor.floordestheight = spec.P_FindLowestFloorSurrounding(sec);
                    floor.texture = sec.floorpic;
                    for (int i = 0; i < sec.linecount; i++) {
                        if (spec.twoSided(secnum, i)) {
                            if (spec.getSide(secnum, i, 0).sector.id == secnum) {
                                sec = spec.getSector(secnum, i, 1);
                            } else {
                                sec = spec.getSector(secnum, i, 0);
                            }
                            if (sec.floorheight == floor.floordestheight) {
                                floor.texture = sec.floorpic;
                                floor.newspecial = sec.special;
                                break;
                            }
                        }
                    }
                }
                default -> {
                }
            }
        }
        return rtn;
    }

    public int EV_BuildStairs(final Line line, final int type) {
        final Sector[] sectors = g.level.sectors;
        int secnum = -1;
        int rtn = 0;
        while ((secnum = g.spec.P_FindSectorFromLineTag(line, secnum)) >= 0) {
            Sector sec = sectors[secnum];
            if (sec.specialdata != null) {
                continue;
            }
            rtn = 1;
            FloorMove floor = new FloorMove();
            g.P_AddThinker(floor);
            sec.specialdata = floor;
            floor.direction = 1;
            floor.sector = sec;
            final int speed;
            final int stairsize;
            if (type == build8) {
                speed = FLOORSPEED / 4;
                stairsize = 8 * FRACUNIT;
            } else {
                speed = FLOORSPEED * 4;
                stairsize = 16 * FRACUNIT;
            }
            floor.speed = speed;
            int height = sec.floorheight + stairsize;
            floor.floordestheight = height;
            final short texture = sec.floorpic;
            boolean ok;
            do {
                ok = false;
                for (int i = 0; i < sec.linecount; i++) {
                    final Line li = sec.lines[i];
                    if ((li.flags & ML_TWOSIDED) == 0) {
                        continue;
                    }
                    Sector tsec = li.frontsector;
                    int newsecnum = tsec.id;
                    if (secnum != newsecnum) {
                        continue;
                    }
                    tsec = li.backsector;
                    if (tsec == null) {
                        continue;
                    }
                    newsecnum = tsec.id;
                    if (tsec.floorpic != texture) {
                        continue;
                    }
                    height += stairsize;
                    if (tsec.specialdata != null) {
                        continue;
                    }
                    sec = tsec;
                    secnum = newsecnum;
                    floor = new FloorMove();
                    g.P_AddThinker(floor);
                    sec.specialdata = floor;
                    floor.direction = 1;
                    floor.sector = sec;
                    floor.speed = speed;
                    floor.floordestheight = height;
                    ok = true;
                    break;
                }
            } while (ok);
        }
        return rtn;
    }

    public int EV_DoDonut(final Line line) {
        int secnum = -1;
        int rtn = 0;
        while ((secnum = g.spec.P_FindSectorFromLineTag(line, secnum)) >= 0) {
            final Sector s1 = g.level.sectors[secnum];
            if (s1.specialdata != null) {
                continue;
            }
            rtn = 1;
            final Sector s2 = Spec.getNextSector(s1.lines[0], s1);
            if (s2 == null) {
                continue;
            }
            for (int i = 0; i < s2.linecount; i++) {
                // vanilla: (!flags & ML_TWOSIDED) is always false, only the back sector test counts
                if (s2.lines[i].backsector == s1) {
                    continue;
                }
                final Sector s3 = s2.lines[i].backsector;
                if (s3 == null) {
                    continue;
                }
                FloorMove floor = new FloorMove();
                g.P_AddThinker(floor);
                s2.specialdata = floor;
                floor.type = donutRaise;
                floor.crush = false;
                floor.direction = 1;
                floor.sector = s2;
                floor.speed = FLOORSPEED / 2;
                floor.texture = s3.floorpic;
                floor.newspecial = 0;
                floor.floordestheight = s3.floorheight;
                floor = new FloorMove();
                g.P_AddThinker(floor);
                s1.specialdata = floor;
                floor.type = lowerFloor;
                floor.crush = false;
                floor.direction = -1;
                floor.sector = s1;
                floor.speed = FLOORSPEED / 2;
                floor.floordestheight = s3.floorheight;
                break;
            }
        }
        return rtn;
    }

    /**
     * {@code floormove_t}.
     */
    public static final class FloorMove extends Thinker {
        public int type;
        public boolean crush;
        public Sector sector;
        public int direction;
        public int newspecial;
        public short texture;
        public int floordestheight;
        public int speed;

        @Override
        public void think(final Game g) {
            g.floors.T_MoveFloor(this);
        }
    }
}
