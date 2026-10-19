// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine;

/**
 * Map object flags ({@code mobjflag_t}).
 */
public final class MobjFlags {

    public static final int MF_SPECIAL = 1;
    public static final int MF_SOLID = 2;
    public static final int MF_SHOOTABLE = 4;
    public static final int MF_NOSECTOR = 8;
    public static final int MF_NOBLOCKMAP = 16;
    public static final int MF_AMBUSH = 32;
    public static final int MF_JUSTHIT = 64;
    public static final int MF_JUSTATTACKED = 128;
    public static final int MF_SPAWNCEILING = 256;
    public static final int MF_NOGRAVITY = 512;
    public static final int MF_DROPOFF = 0x400;
    public static final int MF_PICKUP = 0x800;
    public static final int MF_NOCLIP = 0x1000;
    public static final int MF_SLIDE = 0x2000;
    public static final int MF_FLOAT = 0x4000;
    public static final int MF_TELEPORT = 0x8000;
    public static final int MF_MISSILE = 0x10000;
    public static final int MF_DROPPED = 0x20000;
    public static final int MF_SHADOW = 0x40000;
    public static final int MF_NOBLOOD = 0x80000;
    public static final int MF_CORPSE = 0x100000;
    public static final int MF_INFLOAT = 0x200000;
    public static final int MF_COUNTKILL = 0x400000;
    public static final int MF_COUNTITEM = 0x800000;
    public static final int MF_SKULLFLY = 0x1000000;
    public static final int MF_NOTDMATCH = 0x2000000;
    public static final int MF_TRANSLATION = 0xc000000;
    public static final int MF_TRANSSHIFT = 26;

    private MobjFlags() {
    }
}
