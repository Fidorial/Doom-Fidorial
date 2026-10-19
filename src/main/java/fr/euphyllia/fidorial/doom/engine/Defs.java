// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine;

/**
 * Global constants and small enumerations ({@code doomdef.h}, {@code p_local.h}, ...).
 */
public final class Defs {

    public static final int SCREENWIDTH = 320;
    public static final int SCREENHEIGHT = 200;
    public static final int MAXPLAYERS = 4;
    public static final int TICRATE = 35;

    // game modes
    public static final int shareware = 0;
    public static final int registered = 1;
    public static final int commercial = 2;
    public static final int retail = 3;
    public static final int indetermined = 4;

    // missions
    public static final int doom = 0;
    public static final int doom2 = 1;
    public static final int pack_tnt = 2;
    public static final int pack_plut = 3;
    public static final int none = 4;

    // game states
    public static final int GS_LEVEL = 0;
    public static final int GS_INTERMISSION = 1;
    public static final int GS_FINALE = 2;
    public static final int GS_DEMOSCREEN = 3;

    // map thing options
    public static final int MTF_EASY = 1;
    public static final int MTF_NORMAL = 2;
    public static final int MTF_HARD = 4;
    public static final int MTF_AMBUSH = 8;

    // skills
    public static final int sk_baby = 0;
    public static final int sk_easy = 1;
    public static final int sk_medium = 2;
    public static final int sk_hard = 3;
    public static final int sk_nightmare = 4;

    // cards
    public static final int it_bluecard = 0;
    public static final int it_yellowcard = 1;
    public static final int it_redcard = 2;
    public static final int it_blueskull = 3;
    public static final int it_yellowskull = 4;
    public static final int it_redskull = 5;
    public static final int NUMCARDS = 6;

    // weapons
    public static final int wp_fist = 0;
    public static final int wp_pistol = 1;
    public static final int wp_shotgun = 2;
    public static final int wp_chaingun = 3;
    public static final int wp_missile = 4;
    public static final int wp_plasma = 5;
    public static final int wp_bfg = 6;
    public static final int wp_chainsaw = 7;
    public static final int wp_supershotgun = 8;
    public static final int NUMWEAPONS = 9;
    public static final int wp_nochange = 10;

    // ammo
    public static final int am_clip = 0;
    public static final int am_shell = 1;
    public static final int am_cell = 2;
    public static final int am_misl = 3;
    public static final int NUMAMMO = 4;
    public static final int am_noammo = 5;

    // powers
    public static final int pw_invulnerability = 0;
    public static final int pw_strength = 1;
    public static final int pw_invisibility = 2;
    public static final int pw_ironfeet = 3;
    public static final int pw_allmap = 4;
    public static final int pw_infrared = 5;
    public static final int NUMPOWERS = 6;

    public static final int INVULNTICS = 30 * TICRATE;
    public static final int INVISTICS = 60 * TICRATE;
    public static final int INFRATICS = 120 * TICRATE;
    public static final int IRONTICS = 60 * TICRATE;

    // p_local.h
    public static final int FLOATSPEED = Fixed.FRACUNIT * 4;
    public static final int MAXHEALTH = 100;
    public static final int VIEWHEIGHT = 41 * Fixed.FRACUNIT;
    public static final int MAPBLOCKUNITS = 128;
    public static final int MAPBLOCKSIZE = MAPBLOCKUNITS * Fixed.FRACUNIT;
    public static final int MAPBLOCKSHIFT = Fixed.FRACBITS + 7;
    public static final int MAPBMASK = MAPBLOCKSIZE - 1;
    public static final int MAPBTOFRAC = MAPBLOCKSHIFT - Fixed.FRACBITS;
    public static final int PLAYERRADIUS = 16 * Fixed.FRACUNIT;
    public static final int MAXRADIUS = 32 * Fixed.FRACUNIT;
    public static final int GRAVITY = Fixed.FRACUNIT;
    public static final int MAXMOVE = 30 * Fixed.FRACUNIT;
    public static final int USERANGE = 64 * Fixed.FRACUNIT;
    public static final int MELEERANGE = 64 * Fixed.FRACUNIT;
    public static final int MISSILERANGE = 32 * 64 * Fixed.FRACUNIT;
    public static final int BASETHRESHOLD = 100;
    public static final int ONFLOORZ = Fixed.MININT;
    public static final int ONCEILINGZ = Fixed.MAXINT;

    // player states
    public static final int PST_LIVE = 0;
    public static final int PST_DEAD = 1;
    public static final int PST_REBORN = 2;

    // cheats
    public static final int CF_NOCLIP = 1;
    public static final int CF_GODMODE = 2;
    public static final int CF_NOMOMENTUM = 4;

    // ticcmd buttons
    public static final int BT_ATTACK = 1;
    public static final int BT_USE = 2;
    public static final int BT_SPECIAL = 128;
    public static final int BT_SPECIALMASK = 3;
    public static final int BT_CHANGE = 4;
    public static final int BT_WEAPONMASK = 8 + 16 + 32;
    public static final int BT_WEAPONSHIFT = 3;
    public static final int BTS_PAUSE = 1;
    public static final int BTS_SAVEGAME = 2;

    // psprites
    public static final int ps_weapon = 0;
    public static final int ps_flash = 1;
    public static final int NUMPSPRITES = 2;
    public static final int FF_FULLBRIGHT = 0x8000;
    public static final int FF_FRAMEMASK = 0x7fff;

    // line flags
    public static final int ML_BLOCKING = 1;
    public static final int ML_BLOCKMONSTERS = 2;
    public static final int ML_TWOSIDED = 4;
    public static final int ML_DONTPEGTOP = 8;
    public static final int ML_DONTPEGBOTTOM = 16;
    public static final int ML_SECRET = 32;
    public static final int ML_SOUNDBLOCK = 64;
    public static final int ML_DONTDRAW = 128;
    public static final int ML_MAPPED = 256;

    public static final int NF_SUBSECTOR = 0x8000;

    // game actions
    public static final int ga_nothing = 0;
    public static final int ga_loadlevel = 1;
    public static final int ga_newgame = 2;
    public static final int ga_loadgame = 3;
    public static final int ga_savegame = 4;
    public static final int ga_playdemo = 5;
    public static final int ga_completed = 6;
    public static final int ga_victory = 7;
    public static final int ga_worlddone = 8;
    public static final int ga_screenshot = 9;

    private Defs() {
    }
}
