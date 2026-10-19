// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine.play;

import fr.euphyllia.fidorial.doom.engine.DegenMobj;
import fr.euphyllia.fidorial.doom.engine.Game;
import fr.euphyllia.fidorial.doom.engine.Line;
import fr.euphyllia.fidorial.doom.engine.Mobj;
import fr.euphyllia.fidorial.doom.engine.Player;
import fr.euphyllia.fidorial.doom.engine.Sector;
import fr.euphyllia.fidorial.doom.engine.Side;
import fr.euphyllia.fidorial.doom.engine.Thinker;
import fr.euphyllia.fidorial.doom.engine.render.RenderData;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static fr.euphyllia.fidorial.doom.engine.Defs.CF_GODMODE;
import static fr.euphyllia.fidorial.doom.engine.Defs.ML_SECRET;
import static fr.euphyllia.fidorial.doom.engine.Defs.ML_TWOSIDED;
import static fr.euphyllia.fidorial.doom.engine.Defs.commercial;
import static fr.euphyllia.fidorial.doom.engine.Defs.pw_ironfeet;
import static fr.euphyllia.fidorial.doom.engine.Defs.registered;
import static fr.euphyllia.fidorial.doom.engine.Defs.retail;
import static fr.euphyllia.fidorial.doom.engine.Fixed.FRACUNIT;
import static fr.euphyllia.fidorial.doom.engine.Fixed.MAXINT;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_swtchn;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_swtchx;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_telept;
import static fr.euphyllia.fidorial.doom.engine.Tables.ANGLETOFINESHIFT;
import static fr.euphyllia.fidorial.doom.engine.Tables.finecosine;
import static fr.euphyllia.fidorial.doom.engine.Tables.finesine;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_BFG;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_BRUISERSHOT;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_HEADSHOT;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_PLASMA;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_ROCKET;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_TELEPORTMAN;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_TFOG;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_TROOPSHOT;

/**
 * Line and sector specials, animations, switches and teleporters ({@code p_spec.c}, {@code p_switch.c}, {@code p_telept.c}).
 */
public final class Spec {

    public static final int MAXBUTTONS = 16;
    public static final int BUTTONTIME = 35;
    public static final int top = 0;
    public static final int middle = 1;
    public static final int bottom = 2;
    private static final AnimDef[] ANIMDEFS = {
            new AnimDef(false, "NUKAGE3", "NUKAGE1", 8),
            new AnimDef(false, "FWATER4", "FWATER1", 8),
            new AnimDef(false, "SWATER4", "SWATER1", 8),
            new AnimDef(false, "LAVA4", "LAVA1", 8),
            new AnimDef(false, "BLOOD3", "BLOOD1", 8),
            new AnimDef(false, "RROCK08", "RROCK05", 8),
            new AnimDef(false, "SLIME04", "SLIME01", 8),
            new AnimDef(false, "SLIME08", "SLIME05", 8),
            new AnimDef(false, "SLIME12", "SLIME09", 8),
            new AnimDef(true, "BLODGR4", "BLODGR1", 8),
            new AnimDef(true, "SLADRIP3", "SLADRIP1", 8),
            new AnimDef(true, "BLODRIP4", "BLODRIP1", 8),
            new AnimDef(true, "FIREWALL", "FIREWALA", 8),
            new AnimDef(true, "GSTFONT3", "GSTFONT1", 8),
            new AnimDef(true, "FIRELAVA", "FIRELAV3", 8),
            new AnimDef(true, "FIREMAG3", "FIREMAG1", 8),
            new AnimDef(true, "FIREBLU2", "FIREBLU1", 8),
            new AnimDef(true, "ROCKRED3", "ROCKRED1", 8),
            new AnimDef(true, "BFALL4", "BFALL1", 8),
            new AnimDef(true, "SFALL4", "SFALL1", 8),
            new AnimDef(true, "WFALL4", "WFALL1", 8),
            new AnimDef(true, "DBRAIN4", "DBRAIN1", 8),
    };
    private static final String[][] SWITCHES = {
            {"SW1BRCOM", "SW2BRCOM", "1"}, {"SW1BRN1", "SW2BRN1", "1"}, {"SW1BRN2", "SW2BRN2", "1"},
            {"SW1BRNGN", "SW2BRNGN", "1"}, {"SW1BROWN", "SW2BROWN", "1"}, {"SW1COMM", "SW2COMM", "1"},
            {"SW1COMP", "SW2COMP", "1"}, {"SW1DIRT", "SW2DIRT", "1"}, {"SW1EXIT", "SW2EXIT", "1"},
            {"SW1GRAY", "SW2GRAY", "1"}, {"SW1GRAY1", "SW2GRAY1", "1"}, {"SW1METAL", "SW2METAL", "1"},
            {"SW1PIPE", "SW2PIPE", "1"}, {"SW1SLAD", "SW2SLAD", "1"}, {"SW1STARG", "SW2STARG", "1"},
            {"SW1STON1", "SW2STON1", "1"}, {"SW1STON2", "SW2STON2", "1"}, {"SW1STONE", "SW2STONE", "1"},
            {"SW1STRTN", "SW2STRTN", "1"},
            {"SW1BLUE", "SW2BLUE", "2"}, {"SW1CMT", "SW2CMT", "2"}, {"SW1GARG", "SW2GARG", "2"},
            {"SW1GSTON", "SW2GSTON", "2"}, {"SW1HOT", "SW2HOT", "2"}, {"SW1LION", "SW2LION", "2"},
            {"SW1SATYR", "SW2SATYR", "2"}, {"SW1SKIN", "SW2SKIN", "2"}, {"SW1VINE", "SW2VINE", "2"},
            {"SW1WOOD", "SW2WOOD", "2"},
            {"SW1PANEL", "SW2PANEL", "3"}, {"SW1ROCK", "SW2ROCK", "3"}, {"SW1MET2", "SW2MET2", "3"},
            {"SW1WDMET", "SW2WDMET", "3"}, {"SW1BRIK", "SW2BRIK", "3"}, {"SW1MOD1", "SW2MOD1", "3"},
            {"SW1ZIM", "SW2ZIM", "3"}, {"SW1STON6", "SW2STON6", "3"}, {"SW1TEK", "SW2TEK", "3"},
            {"SW1MARB", "SW2MARB", "3"}, {"SW1SKULL", "SW2SKULL", "3"},
    };
    public final Button[] buttonlist = new Button[MAXBUTTONS];
    private final Game g;
    private final List<Anim> anims = new ArrayList<>();
    private final List<Line> linespeciallist = new ArrayList<>();
    public boolean levelTimer;
    public int levelTimeCount;
    private int[] switchlist = new int[0];
    private int numswitches;
    public Spec(final Game g) {
        this.g = g;
        for (int i = 0; i < MAXBUTTONS; i++) {
            buttonlist[i] = new Button();
        }
    }

    public static @Nullable Sector getNextSector(final Line line, final Sector sec) {
        if ((line.flags & ML_TWOSIDED) == 0) {
            return null;
        }
        if (line.frontsector == sec) {
            return line.backsector;
        }
        return line.frontsector;
    }

    public void P_InitPicAnims() {
        final RenderData res = g.res;
        anims.clear();
        for (final AnimDef def : ANIMDEFS) {
            final int picnum;
            final int basepic;
            if (def.istexture()) {
                if (res.checkTextureNumForName(def.startname()) == -1) {
                    continue;
                }
                picnum = res.textureNumForName(def.endname());
                basepic = res.textureNumForName(def.startname());
            } else {
                if (g.wad.checkNumForName(def.startname()) == -1) {
                    continue;
                }
                picnum = res.flatNumForName(def.endname());
                basepic = res.flatNumForName(def.startname());
            }
            final int numpics = picnum - basepic + 1;
            if (numpics < 2) {
                throw new IllegalStateException("P_InitPicAnims: bad cycle from " + def.startname() + " to " + def.endname());
            }
            anims.add(new Anim(def.istexture(), picnum, basepic, numpics, def.speed()));
        }
    }

    public void P_InitSwitchList() {
        int episode = 1;
        if (g.gamemode == registered || g.gamemode == retail) {
            episode = 2;
        } else if (g.gamemode == commercial) {
            episode = 3;
        }
        final List<Integer> list = new ArrayList<>();
        for (final String[] sw : SWITCHES) {
            if (Integer.parseInt(sw[2]) <= episode) {
                final int a = g.res.checkTextureNumForName(sw[0]);
                final int b = g.res.checkTextureNumForName(sw[1]);
                if (a < 0 || b < 0) {
                    continue;
                }
                list.add(a);
                list.add(b);
            }
        }
        numswitches = list.size() / 2;
        switchlist = new int[list.size() + 1];
        for (int i = 0; i < list.size(); i++) {
            switchlist[i] = list.get(i);
        }
        switchlist[list.size()] = -1;
    }

    public Side getSide(final int currentSector, final int line, final int side) {
        return g.level.sides[g.level.sectors[currentSector].lines[line].sidenum[side]];
    }

    public Sector getSector(final int currentSector, final int line, final int side) {
        return g.level.sides[g.level.sectors[currentSector].lines[line].sidenum[side]].sector;
    }

    public boolean twoSided(final int sector, final int line) {
        final Sector s = g.level.sectors[sector];
        if (line >= s.linecount) {
            return false;
        }
        return (s.lines[line].flags & ML_TWOSIDED) != 0;
    }

    public int P_FindLowestFloorSurrounding(final Sector sec) {
        int floor = sec.floorheight;
        for (int i = 0; i < sec.linecount; i++) {
            final Sector other = getNextSector(sec.lines[i], sec);
            if (other == null) {
                continue;
            }
            if (other.floorheight < floor) {
                floor = other.floorheight;
            }
        }
        return floor;
    }

    public int P_FindHighestFloorSurrounding(final Sector sec) {
        int floor = -500 * FRACUNIT;
        for (int i = 0; i < sec.linecount; i++) {
            final Sector other = getNextSector(sec.lines[i], sec);
            if (other == null) {
                continue;
            }
            if (other.floorheight > floor) {
                floor = other.floorheight;
            }
        }
        return floor;
    }

    public int P_FindNextHighestFloor(final Sector sec, final int currentheight) {
        final int[] heightlist = new int[sec.linecount + 1];
        int h = 0;
        for (int i = 0; i < sec.linecount; i++) {
            final Sector other = getNextSector(sec.lines[i], sec);
            if (other == null) {
                continue;
            }
            if (other.floorheight > currentheight) {
                heightlist[h++] = other.floorheight;
            }
        }
        if (h == 0) {
            return currentheight;
        }
        int min = heightlist[0];
        for (int i = 1; i < h; i++) {
            if (heightlist[i] < min) {
                min = heightlist[i];
            }
        }
        return min;
    }

    public int P_FindLowestCeilingSurrounding(final Sector sec) {
        int height = MAXINT;
        for (int i = 0; i < sec.linecount; i++) {
            final Sector other = getNextSector(sec.lines[i], sec);
            if (other == null) {
                continue;
            }
            if (other.ceilingheight < height) {
                height = other.ceilingheight;
            }
        }
        return height;
    }

    public int P_FindHighestCeilingSurrounding(final Sector sec) {
        int height = 0;
        for (int i = 0; i < sec.linecount; i++) {
            final Sector other = getNextSector(sec.lines[i], sec);
            if (other == null) {
                continue;
            }
            if (other.ceilingheight > height) {
                height = other.ceilingheight;
            }
        }
        return height;
    }

    public int P_FindSectorFromLineTag(final Line line, final int start) {
        final Sector[] sectors = g.level.sectors;
        for (int i = start + 1; i < sectors.length; i++) {
            if (sectors[i].tag == line.tag) {
                return i;
            }
        }
        return -1;
    }

    public int P_FindMinSurroundingLight(final Sector sector, final int max) {
        int min = max;
        for (int i = 0; i < sector.linecount; i++) {
            final Sector check = getNextSector(sector.lines[i], sector);
            if (check == null) {
                continue;
            }
            if (check.lightlevel < min) {
                min = check.lightlevel;
            }
        }
        return min;
    }

    public void P_CrossSpecialLine(final int linenum, final int side, final Mobj thing) {
        final Line line = g.level.lines[linenum];
        if (thing.player == null) {
            switch (thing.type) {
                case MT_ROCKET, MT_PLASMA, MT_BFG, MT_TROOPSHOT, MT_HEADSHOT, MT_BRUISERSHOT -> {
                    return;
                }
                default -> {
                }
            }
            boolean ok = false;
            switch (line.special) {
                case 39, 97, 125, 126, 4, 10, 88 -> ok = true;
                default -> {
                }
            }
            if (!ok) {
                return;
            }
        }
        final Doors doors = g.doors;
        final Floors floors = g.floors;
        final Ceilings ceilings = g.ceilings;
        final Plats plats = g.plats;
        final Lights lights = g.lights;
        switch (line.special) {
            case 2 -> {
                doors.EV_DoDoor(line, Doors.open);
                line.special = 0;
            }
            case 3 -> {
                doors.EV_DoDoor(line, Doors.close);
                line.special = 0;
            }
            case 4 -> {
                doors.EV_DoDoor(line, Doors.normal);
                line.special = 0;
            }
            case 5 -> {
                floors.EV_DoFloor(line, Floors.raiseFloor);
                line.special = 0;
            }
            case 6 -> {
                ceilings.EV_DoCeiling(line, Ceilings.fastCrushAndRaise);
                line.special = 0;
            }
            case 8 -> {
                floors.EV_BuildStairs(line, Floors.build8);
                line.special = 0;
            }
            case 10 -> {
                plats.EV_DoPlat(line, Plats.downWaitUpStay, 0);
                line.special = 0;
            }
            case 12 -> {
                lights.EV_LightTurnOn(line, 0);
                line.special = 0;
            }
            case 13 -> {
                lights.EV_LightTurnOn(line, 255);
                line.special = 0;
            }
            case 16 -> {
                doors.EV_DoDoor(line, Doors.close30ThenOpen);
                line.special = 0;
            }
            case 17 -> {
                lights.EV_StartLightStrobing(line);
                line.special = 0;
            }
            case 19 -> {
                floors.EV_DoFloor(line, Floors.lowerFloor);
                line.special = 0;
            }
            case 22 -> {
                plats.EV_DoPlat(line, Plats.raiseToNearestAndChange, 0);
                line.special = 0;
            }
            case 25 -> {
                ceilings.EV_DoCeiling(line, Ceilings.crushAndRaise);
                line.special = 0;
            }
            case 30 -> {
                floors.EV_DoFloor(line, Floors.raiseToTexture);
                line.special = 0;
            }
            case 35 -> {
                lights.EV_LightTurnOn(line, 35);
                line.special = 0;
            }
            case 36 -> {
                floors.EV_DoFloor(line, Floors.turboLower);
                line.special = 0;
            }
            case 37 -> {
                floors.EV_DoFloor(line, Floors.lowerAndChange);
                line.special = 0;
            }
            case 38 -> {
                floors.EV_DoFloor(line, Floors.lowerFloorToLowest);
                line.special = 0;
            }
            case 39 -> {
                EV_Teleport(line, side, thing);
                line.special = 0;
            }
            case 40 -> {
                ceilings.EV_DoCeiling(line, Ceilings.raiseToHighest);
                floors.EV_DoFloor(line, Floors.lowerFloorToLowest);
                line.special = 0;
            }
            case 44 -> {
                ceilings.EV_DoCeiling(line, Ceilings.lowerAndCrush);
                line.special = 0;
            }
            case 52 -> g.G_ExitLevel();
            case 53 -> {
                plats.EV_DoPlat(line, Plats.perpetualRaise, 0);
                line.special = 0;
            }
            case 54 -> {
                plats.EV_StopPlat(line);
                line.special = 0;
            }
            case 56 -> {
                floors.EV_DoFloor(line, Floors.raiseFloorCrush);
                line.special = 0;
            }
            case 57 -> {
                ceilings.EV_CeilingCrushStop(line);
                line.special = 0;
            }
            case 58 -> {
                floors.EV_DoFloor(line, Floors.raiseFloor24);
                line.special = 0;
            }
            case 59 -> {
                floors.EV_DoFloor(line, Floors.raiseFloor24AndChange);
                line.special = 0;
            }
            case 104 -> {
                lights.EV_TurnTagLightsOff(line);
                line.special = 0;
            }
            case 108 -> {
                doors.EV_DoDoor(line, Doors.blazeRaise);
                line.special = 0;
            }
            case 109 -> {
                doors.EV_DoDoor(line, Doors.blazeOpen);
                line.special = 0;
            }
            case 100 -> {
                floors.EV_BuildStairs(line, Floors.turbo16);
                line.special = 0;
            }
            case 110 -> {
                doors.EV_DoDoor(line, Doors.blazeClose);
                line.special = 0;
            }
            case 119 -> {
                floors.EV_DoFloor(line, Floors.raiseFloorToNearest);
                line.special = 0;
            }
            case 121 -> {
                plats.EV_DoPlat(line, Plats.blazeDWUS, 0);
                line.special = 0;
            }
            case 124 -> g.G_SecretExitLevel();
            case 125 -> {
                if (thing.player == null) {
                    EV_Teleport(line, side, thing);
                    line.special = 0;
                }
            }
            case 130 -> {
                floors.EV_DoFloor(line, Floors.raiseFloorTurbo);
                line.special = 0;
            }
            case 141 -> {
                ceilings.EV_DoCeiling(line, Ceilings.silentCrushAndRaise);
                line.special = 0;
            }
            case 72 -> ceilings.EV_DoCeiling(line, Ceilings.lowerAndCrush);
            case 73 -> ceilings.EV_DoCeiling(line, Ceilings.crushAndRaise);
            case 74 -> ceilings.EV_CeilingCrushStop(line);
            case 75 -> doors.EV_DoDoor(line, Doors.close);
            case 76 -> doors.EV_DoDoor(line, Doors.close30ThenOpen);
            case 77 -> ceilings.EV_DoCeiling(line, Ceilings.fastCrushAndRaise);
            case 79 -> lights.EV_LightTurnOn(line, 35);
            case 80 -> lights.EV_LightTurnOn(line, 0);
            case 81 -> lights.EV_LightTurnOn(line, 255);
            case 82 -> floors.EV_DoFloor(line, Floors.lowerFloorToLowest);
            case 83 -> floors.EV_DoFloor(line, Floors.lowerFloor);
            case 84 -> floors.EV_DoFloor(line, Floors.lowerAndChange);
            case 86 -> doors.EV_DoDoor(line, Doors.open);
            case 87 -> plats.EV_DoPlat(line, Plats.perpetualRaise, 0);
            case 88 -> plats.EV_DoPlat(line, Plats.downWaitUpStay, 0);
            case 89 -> plats.EV_StopPlat(line);
            case 90 -> doors.EV_DoDoor(line, Doors.normal);
            case 91 -> floors.EV_DoFloor(line, Floors.raiseFloor);
            case 92 -> floors.EV_DoFloor(line, Floors.raiseFloor24);
            case 93 -> floors.EV_DoFloor(line, Floors.raiseFloor24AndChange);
            case 94 -> floors.EV_DoFloor(line, Floors.raiseFloorCrush);
            case 95 -> plats.EV_DoPlat(line, Plats.raiseToNearestAndChange, 0);
            case 96 -> floors.EV_DoFloor(line, Floors.raiseToTexture);
            case 97 -> EV_Teleport(line, side, thing);
            case 98 -> floors.EV_DoFloor(line, Floors.turboLower);
            case 105 -> doors.EV_DoDoor(line, Doors.blazeRaise);
            case 106 -> doors.EV_DoDoor(line, Doors.blazeOpen);
            case 107 -> doors.EV_DoDoor(line, Doors.blazeClose);
            case 120 -> plats.EV_DoPlat(line, Plats.blazeDWUS, 0);
            case 126 -> {
                if (thing.player == null) {
                    EV_Teleport(line, side, thing);
                }
            }
            case 128 -> floors.EV_DoFloor(line, Floors.raiseFloorToNearest);
            case 129 -> floors.EV_DoFloor(line, Floors.raiseFloorTurbo);
            default -> {
            }
        }
    }

    public void P_ShootSpecialLine(final Mobj thing, final Line line) {
        if (thing.player == null) {
            if (line.special != 46) {
                return;
            }
        }
        switch (line.special) {
            case 24 -> {
                g.floors.EV_DoFloor(line, Floors.raiseFloor);
                P_ChangeSwitchTexture(line, 0);
            }
            case 46 -> {
                g.doors.EV_DoDoor(line, Doors.open);
                P_ChangeSwitchTexture(line, 1);
            }
            case 47 -> {
                g.plats.EV_DoPlat(line, Plats.raiseToNearestAndChange, 0);
                P_ChangeSwitchTexture(line, 0);
            }
            default -> {
            }
        }
    }

    public void P_PlayerInSpecialSector(final Player player) {
        final Sector sector = player.mo.subsector.sector;
        if (player.mo.z != sector.floorheight) {
            return;
        }
        switch (sector.special) {
            case 5 -> {
                if (player.powers[pw_ironfeet] == 0) {
                    if ((g.leveltime & 0x1f) == 0) {
                        g.inter.P_DamageMobj(player.mo, null, null, 10);
                    }
                }
            }
            case 7 -> {
                if (player.powers[pw_ironfeet] == 0) {
                    if ((g.leveltime & 0x1f) == 0) {
                        g.inter.P_DamageMobj(player.mo, null, null, 5);
                    }
                }
            }
            case 16, 4 -> {
                if (player.powers[pw_ironfeet] == 0 || g.random.P_Random() < 5) {
                    if ((g.leveltime & 0x1f) == 0) {
                        g.inter.P_DamageMobj(player.mo, null, null, 20);
                    }
                }
            }
            case 9 -> {
                player.secretcount++;
                sector.special = 0;
                g.listener().secretFound(player);
            }
            case 11 -> {
                player.cheats &= ~CF_GODMODE;
                if ((g.leveltime & 0x1f) == 0) {
                    g.inter.P_DamageMobj(player.mo, null, null, 20);
                }
                if (player.health <= 10) {
                    g.G_ExitLevel();
                }
            }
            default -> {
                // vanilla aborts with "P_PlayerInSpecialSector: unknown special"
            }
        }
    }

    public void P_UpdateSpecials() {
        if (levelTimer) {
            levelTimeCount--;
            if (levelTimeCount == 0) {
                g.G_ExitLevel();
            }
        }
        for (final Anim anim : anims) {
            for (int i = anim.basepic(); i < anim.basepic() + anim.numpics(); i++) {
                final int pic = anim.basepic() + ((g.leveltime / anim.speed() + i) % anim.numpics());
                if (anim.istexture()) {
                    g.texturetranslation[i] = pic;
                } else {
                    g.flattranslation[i] = pic;
                }
            }
        }
        for (final Line line : linespeciallist) {
            if (line.special == 48) {
                g.level.sides[line.sidenum[0]].textureoffset += FRACUNIT;
            }
        }
        for (int i = 0; i < MAXBUTTONS; i++) {
            final Button b = buttonlist[i];
            if (b.btimer != 0) {
                b.btimer--;
                if (b.btimer == 0) {
                    final Side side = g.level.sides[b.line.sidenum[0]];
                    switch (b.where) {
                        case top -> side.toptexture = (short) b.btexture;
                        case middle -> side.midtexture = (short) b.btexture;
                        case bottom -> side.bottomtexture = (short) b.btexture;
                        default -> {
                        }
                    }
                    g.sound.S_StartSound(b.soundorg, sfx_swtchn);
                    b.clear();
                }
            }
        }
    }

    public void P_SpawnSpecials() {
        levelTimer = false;
        if (g.deathmatch != 0 && g.timelimitMinutes > 0) {
            levelTimer = true;
            levelTimeCount = g.timelimitMinutes * 60 * 35;
        }
        final Sector[] sectors = g.level.sectors;
        for (int i = 0; i < sectors.length; i++) {
            final Sector sector = sectors[i];
            if (sector.special == 0) {
                continue;
            }
            switch (sector.special) {
                case 1 -> g.lights.P_SpawnLightFlash(sector);
                case 2 -> g.lights.P_SpawnStrobeFlash(sector, Lights.FASTDARK, 0);
                case 3 -> g.lights.P_SpawnStrobeFlash(sector, Lights.SLOWDARK, 0);
                case 4 -> {
                    g.lights.P_SpawnStrobeFlash(sector, Lights.FASTDARK, 0);
                    sector.special = 4;
                }
                case 8 -> g.lights.P_SpawnGlowingLight(sector);
                case 9 -> g.totalsecret++;
                case 10 -> g.doors.P_SpawnDoorCloseIn30(sector);
                case 12 -> g.lights.P_SpawnStrobeFlash(sector, Lights.SLOWDARK, 1);
                case 13 -> g.lights.P_SpawnStrobeFlash(sector, Lights.FASTDARK, 1);
                case 14 -> g.doors.P_SpawnDoorRaiseIn5Mins(sector, i);
                case 17 -> g.lights.P_SpawnFireFlicker(sector);
                default -> {
                }
            }
        }
        linespeciallist.clear();
        for (final Line line : g.level.lines) {
            if (line.special == 48) {
                linespeciallist.add(line);
            }
        }
        g.ceilings.activeceilings.clear();
        g.plats.activeplats.clear();
        for (final Button b : buttonlist) {
            b.clear();
        }
    }

    private void P_StartButton(final Line line, final int w, final int texture, final int time) {
        for (final Button b : buttonlist) {
            if (b.btimer != 0 && b.line == line) {
                return;
            }
        }
        for (final Button b : buttonlist) {
            if (b.btimer == 0) {
                b.line = line;
                b.where = w;
                b.btexture = texture;
                b.btimer = time;
                b.soundorg = line.frontsector.soundorg;
                return;
            }
        }
        g.warn("P_StartButton: no button slots left!");
    }

    public void P_ChangeSwitchTexture(final Line line, final int useAgain) {
        if (useAgain == 0) {
            line.special = 0;
        }
        final Side side = g.level.sides[line.sidenum[0]];
        final int texTop = side.toptexture;
        final int texMid = side.midtexture;
        final int texBot = side.bottomtexture;
        int sound = sfx_swtchn;
        if (line.special == 11) {
            sound = sfx_swtchx;
        }
        // vanilla plays the sound from buttonlist[0]; use the switch's own sector instead
        final DegenMobj origin = line.frontsector != null ? line.frontsector.soundorg : null;
        for (int i = 0; i < numswitches * 2; i++) {
            if (switchlist[i] == texTop) {
                g.sound.S_StartSound(origin, sound);
                side.toptexture = (short) switchlist[i ^ 1];
                if (useAgain != 0) {
                    P_StartButton(line, top, switchlist[i], BUTTONTIME);
                }
                return;
            } else if (switchlist[i] == texMid) {
                g.sound.S_StartSound(origin, sound);
                side.midtexture = (short) switchlist[i ^ 1];
                if (useAgain != 0) {
                    P_StartButton(line, middle, switchlist[i], BUTTONTIME);
                }
                return;
            } else if (switchlist[i] == texBot) {
                g.sound.S_StartSound(origin, sound);
                side.bottomtexture = (short) switchlist[i ^ 1];
                if (useAgain != 0) {
                    P_StartButton(line, bottom, switchlist[i], BUTTONTIME);
                }
                return;
            }
        }
    }

    public boolean P_UseSpecialLine(final Mobj thing, final Line line, final int side) {
        if (side != 0) {
            if (line.special != 124) {
                return false;
            }
        }
        if (thing.player == null) {
            if ((line.flags & ML_SECRET) != 0) {
                return false;
            }
            switch (line.special) {
                case 1, 32, 33, 34 -> {
                }
                default -> {
                    return false;
                }
            }
        }
        final Doors doors = g.doors;
        final Floors floors = g.floors;
        final Ceilings ceilings = g.ceilings;
        final Plats plats = g.plats;
        switch (line.special) {
            case 1, 26, 27, 28, 31, 32, 33, 34, 117, 118 -> doors.EV_VerticalDoor(line, thing);
            case 7 -> {
                if (floors.EV_BuildStairs(line, Floors.build8) != 0) {
                    P_ChangeSwitchTexture(line, 0);
                }
            }
            case 9 -> {
                if (floors.EV_DoDonut(line) != 0) {
                    P_ChangeSwitchTexture(line, 0);
                }
            }
            case 11 -> {
                P_ChangeSwitchTexture(line, 0);
                g.G_ExitLevel();
            }
            case 14 -> {
                if (plats.EV_DoPlat(line, Plats.raiseAndChange, 32) != 0) {
                    P_ChangeSwitchTexture(line, 0);
                }
            }
            case 15 -> {
                if (plats.EV_DoPlat(line, Plats.raiseAndChange, 24) != 0) {
                    P_ChangeSwitchTexture(line, 0);
                }
            }
            case 18 -> {
                if (floors.EV_DoFloor(line, Floors.raiseFloorToNearest) != 0) {
                    P_ChangeSwitchTexture(line, 0);
                }
            }
            case 20 -> {
                if (plats.EV_DoPlat(line, Plats.raiseToNearestAndChange, 0) != 0) {
                    P_ChangeSwitchTexture(line, 0);
                }
            }
            case 21 -> {
                if (plats.EV_DoPlat(line, Plats.downWaitUpStay, 0) != 0) {
                    P_ChangeSwitchTexture(line, 0);
                }
            }
            case 23 -> {
                if (floors.EV_DoFloor(line, Floors.lowerFloorToLowest) != 0) {
                    P_ChangeSwitchTexture(line, 0);
                }
            }
            case 29 -> {
                if (doors.EV_DoDoor(line, Doors.normal) != 0) {
                    P_ChangeSwitchTexture(line, 0);
                }
            }
            case 41 -> {
                if (ceilings.EV_DoCeiling(line, Ceilings.lowerToFloor) != 0) {
                    P_ChangeSwitchTexture(line, 0);
                }
            }
            case 71 -> {
                if (floors.EV_DoFloor(line, Floors.turboLower) != 0) {
                    P_ChangeSwitchTexture(line, 0);
                }
            }
            case 49 -> {
                if (ceilings.EV_DoCeiling(line, Ceilings.crushAndRaise) != 0) {
                    P_ChangeSwitchTexture(line, 0);
                }
            }
            case 50 -> {
                if (doors.EV_DoDoor(line, Doors.close) != 0) {
                    P_ChangeSwitchTexture(line, 0);
                }
            }
            case 51 -> {
                P_ChangeSwitchTexture(line, 0);
                g.G_SecretExitLevel();
            }
            case 55 -> {
                if (floors.EV_DoFloor(line, Floors.raiseFloorCrush) != 0) {
                    P_ChangeSwitchTexture(line, 0);
                }
            }
            case 101 -> {
                if (floors.EV_DoFloor(line, Floors.raiseFloor) != 0) {
                    P_ChangeSwitchTexture(line, 0);
                }
            }
            case 102 -> {
                if (floors.EV_DoFloor(line, Floors.lowerFloor) != 0) {
                    P_ChangeSwitchTexture(line, 0);
                }
            }
            case 103 -> {
                if (doors.EV_DoDoor(line, Doors.open) != 0) {
                    P_ChangeSwitchTexture(line, 0);
                }
            }
            case 111 -> {
                if (doors.EV_DoDoor(line, Doors.blazeRaise) != 0) {
                    P_ChangeSwitchTexture(line, 0);
                }
            }
            case 112 -> {
                if (doors.EV_DoDoor(line, Doors.blazeOpen) != 0) {
                    P_ChangeSwitchTexture(line, 0);
                }
            }
            case 113 -> {
                if (doors.EV_DoDoor(line, Doors.blazeClose) != 0) {
                    P_ChangeSwitchTexture(line, 0);
                }
            }
            case 122 -> {
                if (plats.EV_DoPlat(line, Plats.blazeDWUS, 0) != 0) {
                    P_ChangeSwitchTexture(line, 0);
                }
            }
            case 127 -> {
                if (floors.EV_BuildStairs(line, Floors.turbo16) != 0) {
                    P_ChangeSwitchTexture(line, 0);
                }
            }
            case 131 -> {
                if (floors.EV_DoFloor(line, Floors.raiseFloorTurbo) != 0) {
                    P_ChangeSwitchTexture(line, 0);
                }
            }
            case 133, 135, 137 -> {
                if (doors.EV_DoLockedDoor(line, Doors.blazeOpen, thing) != 0) {
                    P_ChangeSwitchTexture(line, 0);
                }
            }
            case 140 -> {
                if (floors.EV_DoFloor(line, Floors.raiseFloor512) != 0) {
                    P_ChangeSwitchTexture(line, 0);
                }
            }
            case 42 -> {
                if (doors.EV_DoDoor(line, Doors.close) != 0) {
                    P_ChangeSwitchTexture(line, 1);
                }
            }
            case 43 -> {
                if (ceilings.EV_DoCeiling(line, Ceilings.lowerToFloor) != 0) {
                    P_ChangeSwitchTexture(line, 1);
                }
            }
            case 45 -> {
                if (floors.EV_DoFloor(line, Floors.lowerFloor) != 0) {
                    P_ChangeSwitchTexture(line, 1);
                }
            }
            case 60 -> {
                if (floors.EV_DoFloor(line, Floors.lowerFloorToLowest) != 0) {
                    P_ChangeSwitchTexture(line, 1);
                }
            }
            case 61 -> {
                if (doors.EV_DoDoor(line, Doors.open) != 0) {
                    P_ChangeSwitchTexture(line, 1);
                }
            }
            case 62 -> {
                if (plats.EV_DoPlat(line, Plats.downWaitUpStay, 1) != 0) {
                    P_ChangeSwitchTexture(line, 1);
                }
            }
            case 63 -> {
                if (doors.EV_DoDoor(line, Doors.normal) != 0) {
                    P_ChangeSwitchTexture(line, 1);
                }
            }
            case 64 -> {
                if (floors.EV_DoFloor(line, Floors.raiseFloor) != 0) {
                    P_ChangeSwitchTexture(line, 1);
                }
            }
            case 66 -> {
                if (plats.EV_DoPlat(line, Plats.raiseAndChange, 24) != 0) {
                    P_ChangeSwitchTexture(line, 1);
                }
            }
            case 67 -> {
                if (plats.EV_DoPlat(line, Plats.raiseAndChange, 32) != 0) {
                    P_ChangeSwitchTexture(line, 1);
                }
            }
            case 65 -> {
                if (floors.EV_DoFloor(line, Floors.raiseFloorCrush) != 0) {
                    P_ChangeSwitchTexture(line, 1);
                }
            }
            case 68 -> {
                if (plats.EV_DoPlat(line, Plats.raiseToNearestAndChange, 0) != 0) {
                    P_ChangeSwitchTexture(line, 1);
                }
            }
            case 69 -> {
                if (floors.EV_DoFloor(line, Floors.raiseFloorToNearest) != 0) {
                    P_ChangeSwitchTexture(line, 1);
                }
            }
            case 70 -> {
                if (floors.EV_DoFloor(line, Floors.turboLower) != 0) {
                    P_ChangeSwitchTexture(line, 1);
                }
            }
            case 114 -> {
                if (doors.EV_DoDoor(line, Doors.blazeRaise) != 0) {
                    P_ChangeSwitchTexture(line, 1);
                }
            }
            case 115 -> {
                if (doors.EV_DoDoor(line, Doors.blazeOpen) != 0) {
                    P_ChangeSwitchTexture(line, 1);
                }
            }
            case 116 -> {
                if (doors.EV_DoDoor(line, Doors.blazeClose) != 0) {
                    P_ChangeSwitchTexture(line, 1);
                }
            }
            case 123 -> {
                if (plats.EV_DoPlat(line, Plats.blazeDWUS, 0) != 0) {
                    P_ChangeSwitchTexture(line, 1);
                }
            }
            case 132 -> {
                if (floors.EV_DoFloor(line, Floors.raiseFloorTurbo) != 0) {
                    P_ChangeSwitchTexture(line, 1);
                }
            }
            case 99, 134, 136 -> {
                if (doors.EV_DoLockedDoor(line, Doors.blazeOpen, thing) != 0) {
                    P_ChangeSwitchTexture(line, 1);
                }
            }
            case 138 -> {
                g.lights.EV_LightTurnOn(line, 255);
                P_ChangeSwitchTexture(line, 1);
            }
            case 139 -> {
                g.lights.EV_LightTurnOn(line, 35);
                P_ChangeSwitchTexture(line, 1);
            }
            default -> {
            }
        }
        return true;
    }

    public int EV_Teleport(final Line line, final int side, final Mobj thing) {
        if ((thing.flags & fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_MISSILE) != 0) {
            return 0;
        }
        if (side == 1) {
            return 0;
        }
        final int tag = line.tag;
        final Sector[] sectors = g.level.sectors;
        for (int i = 0; i < sectors.length; i++) {
            if (sectors[i].tag == tag) {
                for (Thinker th = g.thinkercap.next; th != g.thinkercap; th = th.next) {
                    if (!(th instanceof final Mobj m) || th.function != Thinker.FN_ACTIVE) {
                        continue;
                    }
                    if (m.type != MT_TELEPORTMAN) {
                        continue;
                    }
                    if (m.subsector.sector.id != i) {
                        continue;
                    }
                    final int oldx = thing.x;
                    final int oldy = thing.y;
                    final int oldz = thing.z;
                    if (!g.map.P_TeleportMove(thing, m.x, m.y)) {
                        return 0;
                    }
                    thing.z = thing.floorz;
                    if (thing.player != null) {
                        thing.player.viewz = thing.z + thing.player.viewheight;
                    }
                    Mobj fog = g.mobjs.P_SpawnMobj(oldx, oldy, oldz, MT_TFOG);
                    g.sound.S_StartSound(fog, sfx_telept);
                    final int an = m.angle >>> ANGLETOFINESHIFT;
                    fog = g.mobjs.P_SpawnMobj(m.x + 20 * finecosine(an), m.y + 20 * finesine[an], thing.z, MT_TFOG);
                    g.sound.S_StartSound(fog, sfx_telept);
                    if (thing.player != null) {
                        thing.reactiontime = 18;
                    }
                    thing.angle = m.angle;
                    thing.momx = thing.momy = thing.momz = 0;
                    return 1;
                }
            }
        }
        return 0;
    }

    private record AnimDef(boolean istexture, String endname, String startname, int speed) {
    }

    /**
     * An animated flat or texture cycle ({@code anim_t}).
     */
    private record Anim(boolean istexture, int picnum, int basepic, int numpics, int speed) {
    }

    /**
     * A switch waiting to pop back ({@code button_t}).
     */
    public static final class Button {
        public @Nullable Line line;
        public int where;
        public int btexture;
        public int btimer;
        public @Nullable DegenMobj soundorg;

        void clear() {
            line = null;
            where = 0;
            btexture = 0;
            btimer = 0;
            soundorg = null;
        }
    }
}
