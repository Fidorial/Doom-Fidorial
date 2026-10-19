// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine.play;

import fr.euphyllia.fidorial.doom.engine.Game;
import fr.euphyllia.fidorial.doom.engine.Line;
import fr.euphyllia.fidorial.doom.engine.Mobj;
import fr.euphyllia.fidorial.doom.engine.Player;
import fr.euphyllia.fidorial.doom.engine.Sector;
import fr.euphyllia.fidorial.doom.engine.Thinker;

import static fr.euphyllia.fidorial.doom.engine.Defs.it_bluecard;
import static fr.euphyllia.fidorial.doom.engine.Defs.it_blueskull;
import static fr.euphyllia.fidorial.doom.engine.Defs.it_redcard;
import static fr.euphyllia.fidorial.doom.engine.Defs.it_redskull;
import static fr.euphyllia.fidorial.doom.engine.Defs.it_yellowcard;
import static fr.euphyllia.fidorial.doom.engine.Defs.it_yellowskull;
import static fr.euphyllia.fidorial.doom.engine.Fixed.FRACUNIT;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_bdcls;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_bdopn;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_dorcls;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_doropn;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_oof;
import static fr.euphyllia.fidorial.doom.engine.Strings.PD_BLUEK;
import static fr.euphyllia.fidorial.doom.engine.Strings.PD_BLUEO;
import static fr.euphyllia.fidorial.doom.engine.Strings.PD_REDK;
import static fr.euphyllia.fidorial.doom.engine.Strings.PD_REDO;
import static fr.euphyllia.fidorial.doom.engine.Strings.PD_YELLOWK;
import static fr.euphyllia.fidorial.doom.engine.Strings.PD_YELLOWO;
import static fr.euphyllia.fidorial.doom.engine.play.Floors.crushed;
import static fr.euphyllia.fidorial.doom.engine.play.Floors.pastdest;

/**
 * Vertical doors ({@code p_doors.c}).
 */
public final class Doors {

    public static final int normal = 0;
    public static final int close30ThenOpen = 1;
    public static final int close = 2;
    public static final int open = 3;
    public static final int raiseIn5Mins = 4;
    public static final int blazeRaise = 5;
    public static final int blazeOpen = 6;
    public static final int blazeClose = 7;

    public static final int VDOORSPEED = FRACUNIT * 2;
    public static final int VDOORWAIT = 150;

    private final Game g;

    public Doors(final Game g) {
        this.g = g;
    }

    public void T_VerticalDoor(final VlDoor door) {
        switch (door.direction) {
            case 0 -> {
                if (--door.topcountdown == 0) {
                    switch (door.type) {
                        case blazeRaise -> {
                            door.direction = -1;
                            g.sound.S_StartSound(door.sector.soundorg, sfx_bdcls);
                        }
                        case normal -> {
                            door.direction = -1;
                            g.sound.S_StartSound(door.sector.soundorg, sfx_dorcls);
                        }
                        case close30ThenOpen -> {
                            door.direction = 1;
                            g.sound.S_StartSound(door.sector.soundorg, sfx_doropn);
                        }
                        default -> {
                        }
                    }
                }
            }
            case 2 -> {
                if (--door.topcountdown == 0) {
                    if (door.type == raiseIn5Mins) {
                        door.direction = 1;
                        door.type = normal;
                        g.sound.S_StartSound(door.sector.soundorg, sfx_doropn);
                    }
                }
            }
            case -1 -> {
                final int res = g.floors.T_MovePlane(door.sector, door.speed, door.sector.floorheight, false, 1,
                        door.direction);
                if (res == pastdest) {
                    switch (door.type) {
                        case blazeRaise, blazeClose -> {
                            door.sector.specialdata = null;
                            g.P_RemoveThinker(door);
                            g.sound.S_StartSound(door.sector.soundorg, sfx_bdcls);
                        }
                        case normal, close -> {
                            door.sector.specialdata = null;
                            g.P_RemoveThinker(door);
                        }
                        case close30ThenOpen -> {
                            door.direction = 0;
                            door.topcountdown = 35 * 30;
                        }
                        default -> {
                        }
                    }
                } else if (res == crushed) {
                    switch (door.type) {
                        case blazeClose, close -> {
                        }
                        default -> {
                            door.direction = 1;
                            g.sound.S_StartSound(door.sector.soundorg, sfx_doropn);
                        }
                    }
                }
            }
            case 1 -> {
                final int res = g.floors.T_MovePlane(door.sector, door.speed, door.topheight, false, 1, door.direction);
                if (res == pastdest) {
                    switch (door.type) {
                        case blazeRaise, normal -> {
                            door.direction = 0;
                            door.topcountdown = door.topwait;
                        }
                        case close30ThenOpen, blazeOpen, open -> {
                            door.sector.specialdata = null;
                            g.P_RemoveThinker(door);
                        }
                        default -> {
                        }
                    }
                }
            }
            default -> {
            }
        }
    }

    public int EV_DoLockedDoor(final Line line, final int type, final Mobj thing) {
        final Player p = thing.player;
        if (p == null) {
            return 0;
        }
        switch (line.special) {
            case 99, 133 -> {
                if (!p.cards[it_bluecard] && !p.cards[it_blueskull]) {
                    p.message = PD_BLUEO;
                    g.sound.S_StartLocalSound(p, sfx_oof);
                    return 0;
                }
            }
            case 134, 135 -> {
                if (!p.cards[it_redcard] && !p.cards[it_redskull]) {
                    p.message = PD_REDO;
                    g.sound.S_StartLocalSound(p, sfx_oof);
                    return 0;
                }
            }
            case 136, 137 -> {
                if (!p.cards[it_yellowcard] && !p.cards[it_yellowskull]) {
                    p.message = PD_YELLOWO;
                    g.sound.S_StartLocalSound(p, sfx_oof);
                    return 0;
                }
            }
            default -> {
            }
        }
        return EV_DoDoor(line, type);
    }

    public int EV_DoDoor(final Line line, final int type) {
        int secnum = -1;
        int rtn = 0;
        while ((secnum = g.spec.P_FindSectorFromLineTag(line, secnum)) >= 0) {
            final Sector sec = g.level.sectors[secnum];
            if (sec.specialdata != null) {
                continue;
            }
            rtn = 1;
            final VlDoor door = new VlDoor();
            g.P_AddThinker(door);
            sec.specialdata = door;
            door.sector = sec;
            door.type = type;
            door.topwait = VDOORWAIT;
            door.speed = VDOORSPEED;
            switch (type) {
                case blazeClose -> {
                    door.topheight = g.spec.P_FindLowestCeilingSurrounding(sec);
                    door.topheight -= 4 * FRACUNIT;
                    door.direction = -1;
                    door.speed = VDOORSPEED * 4;
                    g.sound.S_StartSound(door.sector.soundorg, sfx_bdcls);
                }
                case close -> {
                    door.topheight = g.spec.P_FindLowestCeilingSurrounding(sec);
                    door.topheight -= 4 * FRACUNIT;
                    door.direction = -1;
                    g.sound.S_StartSound(door.sector.soundorg, sfx_dorcls);
                }
                case close30ThenOpen -> {
                    door.topheight = sec.ceilingheight;
                    door.direction = -1;
                    g.sound.S_StartSound(door.sector.soundorg, sfx_dorcls);
                }
                case blazeRaise, blazeOpen -> {
                    door.direction = 1;
                    door.topheight = g.spec.P_FindLowestCeilingSurrounding(sec);
                    door.topheight -= 4 * FRACUNIT;
                    door.speed = VDOORSPEED * 4;
                    if (door.topheight != sec.ceilingheight) {
                        g.sound.S_StartSound(door.sector.soundorg, sfx_bdopn);
                    }
                }
                case normal, open -> {
                    door.direction = 1;
                    door.topheight = g.spec.P_FindLowestCeilingSurrounding(sec);
                    door.topheight -= 4 * FRACUNIT;
                    if (door.topheight != sec.ceilingheight) {
                        g.sound.S_StartSound(door.sector.soundorg, sfx_doropn);
                    }
                }
                default -> {
                }
            }
        }
        return rtn;
    }

    public void EV_VerticalDoor(final Line line, final Mobj thing) {
        final int side = 0;
        final Player player = thing.player;
        switch (line.special) {
            case 26, 32 -> {
                if (player == null) {
                    return;
                }
                if (!player.cards[it_bluecard] && !player.cards[it_blueskull]) {
                    player.message = PD_BLUEK;
                    g.sound.S_StartLocalSound(player, sfx_oof);
                    return;
                }
            }
            case 27, 34 -> {
                if (player == null) {
                    return;
                }
                if (!player.cards[it_yellowcard] && !player.cards[it_yellowskull]) {
                    player.message = PD_YELLOWK;
                    g.sound.S_StartLocalSound(player, sfx_oof);
                    return;
                }
            }
            case 28, 33 -> {
                if (player == null) {
                    return;
                }
                if (!player.cards[it_redcard] && !player.cards[it_redskull]) {
                    player.message = PD_REDK;
                    g.sound.S_StartLocalSound(player, sfx_oof);
                    return;
                }
            }
            default -> {
            }
        }
        if (line.sidenum[side ^ 1] == -1) {
            return;
        }
        final Sector sec = g.level.sides[line.sidenum[side ^ 1]].sector;
        if (sec.specialdata != null) {
            if (sec.specialdata instanceof final VlDoor door) {
                switch (line.special) {
                    case 1, 26, 27, 28, 117 -> {
                        if (door.direction == -1) {
                            door.direction = 1;
                        } else {
                            if (thing.player == null) {
                                return;
                            }
                            door.direction = -1;
                        }
                        return;
                    }
                    default -> {
                    }
                }
            } else {
                // vanilla reinterprets whatever special is there as a door, which is undefined behaviour
                switch (line.special) {
                    case 1, 26, 27, 28, 117 -> {
                        return;
                    }
                    default -> {
                    }
                }
            }
        }
        switch (line.special) {
            case 117, 118 -> g.sound.S_StartSound(sec.soundorg, sfx_bdopn);
            default -> g.sound.S_StartSound(sec.soundorg, sfx_doropn);
        }
        final VlDoor door = new VlDoor();
        g.P_AddThinker(door);
        sec.specialdata = door;
        door.sector = sec;
        door.direction = 1;
        door.speed = VDOORSPEED;
        door.topwait = VDOORWAIT;
        switch (line.special) {
            case 1, 26, 27, 28 -> door.type = normal;
            case 31, 32, 33, 34 -> {
                door.type = open;
                line.special = 0;
            }
            case 117 -> {
                door.type = blazeRaise;
                door.speed = VDOORSPEED * 4;
            }
            case 118 -> {
                door.type = blazeOpen;
                line.special = 0;
                door.speed = VDOORSPEED * 4;
            }
            default -> {
            }
        }
        door.topheight = g.spec.P_FindLowestCeilingSurrounding(sec);
        door.topheight -= 4 * FRACUNIT;
    }

    public void P_SpawnDoorCloseIn30(final Sector sec) {
        final VlDoor door = new VlDoor();
        g.P_AddThinker(door);
        sec.specialdata = door;
        sec.special = 0;
        door.sector = sec;
        door.direction = 0;
        door.type = normal;
        door.speed = VDOORSPEED;
        door.topcountdown = 30 * 35;
    }

    public void P_SpawnDoorRaiseIn5Mins(final Sector sec, final int secnum) {
        final VlDoor door = new VlDoor();
        g.P_AddThinker(door);
        sec.specialdata = door;
        sec.special = 0;
        door.sector = sec;
        door.direction = 2;
        door.type = raiseIn5Mins;
        door.speed = VDOORSPEED;
        door.topheight = g.spec.P_FindLowestCeilingSurrounding(sec);
        door.topheight -= 4 * FRACUNIT;
        door.topwait = VDOORWAIT;
        door.topcountdown = 5 * 60 * 35;
    }

    /**
     * {@code vldoor_t}.
     */
    public static final class VlDoor extends Thinker {
        public int type;
        public Sector sector;
        public int topheight;
        public int speed;
        public int direction;
        public int topwait;
        public int topcountdown;

        @Override
        public void think(final Game g) {
            g.doors.T_VerticalDoor(this);
        }
    }
}
