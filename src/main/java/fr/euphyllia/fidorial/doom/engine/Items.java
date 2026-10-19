// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine;

import static fr.euphyllia.fidorial.doom.engine.Defs.am_cell;
import static fr.euphyllia.fidorial.doom.engine.Defs.am_clip;
import static fr.euphyllia.fidorial.doom.engine.Defs.am_misl;
import static fr.euphyllia.fidorial.doom.engine.Defs.am_noammo;
import static fr.euphyllia.fidorial.doom.engine.Defs.am_shell;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BFG;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BFG1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BFGDOWN;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BFGFLASH1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_BFGUP;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_CHAIN;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_CHAIN1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_CHAINDOWN;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_CHAINFLASH1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_CHAINUP;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_DSGUN;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_DSGUN1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_DSGUNDOWN;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_DSGUNFLASH1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_DSGUNUP;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_MISSILE;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_MISSILE1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_MISSILEDOWN;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_MISSILEFLASH1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_MISSILEUP;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_NULL;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_PISTOL;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_PISTOL1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_PISTOLDOWN;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_PISTOLFLASH;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_PISTOLUP;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_PLASMA;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_PLASMA1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_PLASMADOWN;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_PLASMAFLASH1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_PLASMAUP;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_PUNCH;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_PUNCH1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_PUNCHDOWN;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_PUNCHUP;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SAW;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SAW1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SAWDOWN;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SAWUP;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SGUN;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SGUN1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SGUNDOWN;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SGUNFLASH1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SGUNUP;

/**
 * Weapon descriptions ({@code d_items.c}).
 */
public final class Items {

    public static final WeaponInfo[] WEAPONINFO = {
            new WeaponInfo(am_noammo, S_PUNCHUP, S_PUNCHDOWN, S_PUNCH, S_PUNCH1, S_NULL),
            new WeaponInfo(am_clip, S_PISTOLUP, S_PISTOLDOWN, S_PISTOL, S_PISTOL1, S_PISTOLFLASH),
            new WeaponInfo(am_shell, S_SGUNUP, S_SGUNDOWN, S_SGUN, S_SGUN1, S_SGUNFLASH1),
            new WeaponInfo(am_clip, S_CHAINUP, S_CHAINDOWN, S_CHAIN, S_CHAIN1, S_CHAINFLASH1),
            new WeaponInfo(am_misl, S_MISSILEUP, S_MISSILEDOWN, S_MISSILE, S_MISSILE1, S_MISSILEFLASH1),
            new WeaponInfo(am_cell, S_PLASMAUP, S_PLASMADOWN, S_PLASMA, S_PLASMA1, S_PLASMAFLASH1),
            new WeaponInfo(am_cell, S_BFGUP, S_BFGDOWN, S_BFG, S_BFG1, S_BFGFLASH1),
            new WeaponInfo(am_noammo, S_SAWUP, S_SAWDOWN, S_SAW, S_SAW1, S_NULL),
            new WeaponInfo(am_shell, S_DSGUNUP, S_DSGUNDOWN, S_DSGUN, S_DSGUN1, S_DSGUNFLASH1),
    };

    private Items() {
    }

    /**
     * One weapon ({@code weaponinfo_t}).
     */
    public record WeaponInfo(int ammo, int upstate, int downstate, int readystate, int atkstate, int flashstate) {
    }
}
