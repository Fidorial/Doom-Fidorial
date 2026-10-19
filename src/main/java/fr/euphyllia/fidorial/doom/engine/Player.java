// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine;

import org.jspecify.annotations.Nullable;

import static fr.euphyllia.fidorial.doom.engine.Defs.MAXPLAYERS;
import static fr.euphyllia.fidorial.doom.engine.Defs.NUMAMMO;
import static fr.euphyllia.fidorial.doom.engine.Defs.NUMCARDS;
import static fr.euphyllia.fidorial.doom.engine.Defs.NUMPOWERS;
import static fr.euphyllia.fidorial.doom.engine.Defs.NUMWEAPONS;

/**
 * Everything about a player that survives between frames ({@code player_t}).
 */
public final class Player {

    public final int index;
    public final TicCmd cmd = new TicCmd();
    public final int[] powers = new int[NUMPOWERS];
    public final boolean[] cards = new boolean[NUMCARDS];
    public final int[] frags = new int[MAXPLAYERS];
    public final boolean[] weaponowned = new boolean[NUMWEAPONS];
    public final int[] ammo = new int[NUMAMMO];
    public final int[] maxammo = new int[NUMAMMO];
    public final PspDef[] psprites = {new PspDef(), new PspDef()};
    public @Nullable Mobj mo;
    public int playerstate;
    public int viewz;
    public int viewheight;
    public int deltaviewheight;
    public int bob;
    public int health;
    public int armorpoints;
    public int armortype;
    public boolean backpack;
    public int readyweapon;
    public int pendingweapon;
    public int attackdown;
    public int usedown;
    public int cheats;
    public int refire;
    public int killcount;
    public int itemcount;
    public int secretcount;
    public @Nullable String message;
    public int damagecount;
    public int bonuscount;
    public @Nullable Mobj attacker;
    public int extralight;
    public int fixedcolormap;
    public int colormap;
    public boolean didsecret;

    public Player(final int index) {
        this.index = index;
    }

    /**
     * Resets everything, as the {@code memset} in {@code G_PlayerReborn}.
     */
    public void clear() {
        mo = null;
        playerstate = 0;
        cmd.clear();
        viewz = viewheight = deltaviewheight = bob = 0;
        health = armorpoints = armortype = 0;
        java.util.Arrays.fill(powers, 0);
        java.util.Arrays.fill(cards, false);
        backpack = false;
        java.util.Arrays.fill(frags, 0);
        readyweapon = pendingweapon = 0;
        java.util.Arrays.fill(weaponowned, false);
        java.util.Arrays.fill(ammo, 0);
        java.util.Arrays.fill(maxammo, 0);
        attackdown = usedown = cheats = refire = 0;
        killcount = itemcount = secretcount = 0;
        message = null;
        damagecount = bonuscount = 0;
        attacker = null;
        extralight = fixedcolormap = colormap = 0;
        for (final PspDef psp : psprites) {
            psp.state = null;
            psp.tics = psp.sx = psp.sy = 0;
        }
        didsecret = false;
    }
}
