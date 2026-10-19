// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine.info;

/**
 * Static description of a thing type ({@code mobjinfo_t}).
 */
public record MobjInfo(
        int doomednum,
        int spawnstate,
        int spawnhealth,
        int seestate,
        int seesound,
        int reactiontime,
        int attacksound,
        int painstate,
        int painchance,
        int painsound,
        int meleestate,
        int missilestate,
        int deathstate,
        int xdeathstate,
        int deathsound,
        int speed,
        int radius,
        int height,
        int mass,
        int damage,
        int activesound,
        int flags,
        int raisestate) {
}
