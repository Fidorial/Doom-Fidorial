// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine.play;

import fr.euphyllia.fidorial.doom.engine.Angles;
import fr.euphyllia.fidorial.doom.engine.Game;
import fr.euphyllia.fidorial.doom.engine.Mobj;
import fr.euphyllia.fidorial.doom.engine.Player;
import org.jspecify.annotations.Nullable;

import static fr.euphyllia.fidorial.doom.engine.Defs.BASETHRESHOLD;
import static fr.euphyllia.fidorial.doom.engine.Defs.CF_GODMODE;
import static fr.euphyllia.fidorial.doom.engine.Defs.INFRATICS;
import static fr.euphyllia.fidorial.doom.engine.Defs.INVISTICS;
import static fr.euphyllia.fidorial.doom.engine.Defs.INVULNTICS;
import static fr.euphyllia.fidorial.doom.engine.Defs.IRONTICS;
import static fr.euphyllia.fidorial.doom.engine.Defs.MAXHEALTH;
import static fr.euphyllia.fidorial.doom.engine.Defs.NUMAMMO;
import static fr.euphyllia.fidorial.doom.engine.Defs.ONFLOORZ;
import static fr.euphyllia.fidorial.doom.engine.Defs.PST_DEAD;
import static fr.euphyllia.fidorial.doom.engine.Defs.am_cell;
import static fr.euphyllia.fidorial.doom.engine.Defs.am_clip;
import static fr.euphyllia.fidorial.doom.engine.Defs.am_misl;
import static fr.euphyllia.fidorial.doom.engine.Defs.am_noammo;
import static fr.euphyllia.fidorial.doom.engine.Defs.am_shell;
import static fr.euphyllia.fidorial.doom.engine.Defs.commercial;
import static fr.euphyllia.fidorial.doom.engine.Defs.it_bluecard;
import static fr.euphyllia.fidorial.doom.engine.Defs.it_blueskull;
import static fr.euphyllia.fidorial.doom.engine.Defs.it_redcard;
import static fr.euphyllia.fidorial.doom.engine.Defs.it_redskull;
import static fr.euphyllia.fidorial.doom.engine.Defs.it_yellowcard;
import static fr.euphyllia.fidorial.doom.engine.Defs.it_yellowskull;
import static fr.euphyllia.fidorial.doom.engine.Defs.pw_allmap;
import static fr.euphyllia.fidorial.doom.engine.Defs.pw_infrared;
import static fr.euphyllia.fidorial.doom.engine.Defs.pw_invisibility;
import static fr.euphyllia.fidorial.doom.engine.Defs.pw_invulnerability;
import static fr.euphyllia.fidorial.doom.engine.Defs.pw_ironfeet;
import static fr.euphyllia.fidorial.doom.engine.Defs.pw_strength;
import static fr.euphyllia.fidorial.doom.engine.Defs.sk_baby;
import static fr.euphyllia.fidorial.doom.engine.Defs.sk_nightmare;
import static fr.euphyllia.fidorial.doom.engine.Defs.wp_bfg;
import static fr.euphyllia.fidorial.doom.engine.Defs.wp_chaingun;
import static fr.euphyllia.fidorial.doom.engine.Defs.wp_chainsaw;
import static fr.euphyllia.fidorial.doom.engine.Defs.wp_fist;
import static fr.euphyllia.fidorial.doom.engine.Defs.wp_missile;
import static fr.euphyllia.fidorial.doom.engine.Defs.wp_pistol;
import static fr.euphyllia.fidorial.doom.engine.Defs.wp_plasma;
import static fr.euphyllia.fidorial.doom.engine.Defs.wp_shotgun;
import static fr.euphyllia.fidorial.doom.engine.Defs.wp_supershotgun;
import static fr.euphyllia.fidorial.doom.engine.Fixed.FRACUNIT;
import static fr.euphyllia.fidorial.doom.engine.Fixed.mul;
import static fr.euphyllia.fidorial.doom.engine.Items.WEAPONINFO;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_CORPSE;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_COUNTITEM;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_COUNTKILL;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_DROPOFF;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_DROPPED;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_FLOAT;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_JUSTHIT;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_NOCLIP;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_NOGRAVITY;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_SHADOW;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_SHOOTABLE;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_SKULLFLY;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_SOLID;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_getpow;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_itemup;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_wpnup;
import static fr.euphyllia.fidorial.doom.engine.Strings.GOTARMBONUS;
import static fr.euphyllia.fidorial.doom.engine.Strings.GOTARMOR;
import static fr.euphyllia.fidorial.doom.engine.Strings.GOTBACKPACK;
import static fr.euphyllia.fidorial.doom.engine.Strings.GOTBERSERK;
import static fr.euphyllia.fidorial.doom.engine.Strings.GOTBFG9000;
import static fr.euphyllia.fidorial.doom.engine.Strings.GOTBLUECARD;
import static fr.euphyllia.fidorial.doom.engine.Strings.GOTBLUESKUL;
import static fr.euphyllia.fidorial.doom.engine.Strings.GOTCELL;
import static fr.euphyllia.fidorial.doom.engine.Strings.GOTCELLBOX;
import static fr.euphyllia.fidorial.doom.engine.Strings.GOTCHAINGUN;
import static fr.euphyllia.fidorial.doom.engine.Strings.GOTCHAINSAW;
import static fr.euphyllia.fidorial.doom.engine.Strings.GOTCLIP;
import static fr.euphyllia.fidorial.doom.engine.Strings.GOTCLIPBOX;
import static fr.euphyllia.fidorial.doom.engine.Strings.GOTHTHBONUS;
import static fr.euphyllia.fidorial.doom.engine.Strings.GOTINVIS;
import static fr.euphyllia.fidorial.doom.engine.Strings.GOTINVUL;
import static fr.euphyllia.fidorial.doom.engine.Strings.GOTLAUNCHER;
import static fr.euphyllia.fidorial.doom.engine.Strings.GOTMAP;
import static fr.euphyllia.fidorial.doom.engine.Strings.GOTMEDIKIT;
import static fr.euphyllia.fidorial.doom.engine.Strings.GOTMEDINEED;
import static fr.euphyllia.fidorial.doom.engine.Strings.GOTMEGA;
import static fr.euphyllia.fidorial.doom.engine.Strings.GOTMSPHERE;
import static fr.euphyllia.fidorial.doom.engine.Strings.GOTPLASMA;
import static fr.euphyllia.fidorial.doom.engine.Strings.GOTREDCARD;
import static fr.euphyllia.fidorial.doom.engine.Strings.GOTREDSKULL;
import static fr.euphyllia.fidorial.doom.engine.Strings.GOTROCKBOX;
import static fr.euphyllia.fidorial.doom.engine.Strings.GOTROCKET;
import static fr.euphyllia.fidorial.doom.engine.Strings.GOTSHELLBOX;
import static fr.euphyllia.fidorial.doom.engine.Strings.GOTSHELLS;
import static fr.euphyllia.fidorial.doom.engine.Strings.GOTSHOTGUN;
import static fr.euphyllia.fidorial.doom.engine.Strings.GOTSHOTGUN2;
import static fr.euphyllia.fidorial.doom.engine.Strings.GOTSTIM;
import static fr.euphyllia.fidorial.doom.engine.Strings.GOTSUIT;
import static fr.euphyllia.fidorial.doom.engine.Strings.GOTSUPER;
import static fr.euphyllia.fidorial.doom.engine.Strings.GOTVISOR;
import static fr.euphyllia.fidorial.doom.engine.Strings.GOTYELWCARD;
import static fr.euphyllia.fidorial.doom.engine.Strings.GOTYELWSKUL;
import static fr.euphyllia.fidorial.doom.engine.Tables.ANG180;
import static fr.euphyllia.fidorial.doom.engine.Tables.ANGLETOFINESHIFT;
import static fr.euphyllia.fidorial.doom.engine.Tables.finecosine;
import static fr.euphyllia.fidorial.doom.engine.Tables.finesine;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_CHAINGUN;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_CHAINGUY;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_CLIP;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_POSSESSED;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_SHOTGUN;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_SHOTGUY;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_SKULL;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_VILE;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_WOLFSS;
import static fr.euphyllia.fidorial.doom.engine.info.SpriteNum.SPR_AMMO;
import static fr.euphyllia.fidorial.doom.engine.info.SpriteNum.SPR_ARM1;
import static fr.euphyllia.fidorial.doom.engine.info.SpriteNum.SPR_ARM2;
import static fr.euphyllia.fidorial.doom.engine.info.SpriteNum.SPR_BFUG;
import static fr.euphyllia.fidorial.doom.engine.info.SpriteNum.SPR_BKEY;
import static fr.euphyllia.fidorial.doom.engine.info.SpriteNum.SPR_BON1;
import static fr.euphyllia.fidorial.doom.engine.info.SpriteNum.SPR_BON2;
import static fr.euphyllia.fidorial.doom.engine.info.SpriteNum.SPR_BPAK;
import static fr.euphyllia.fidorial.doom.engine.info.SpriteNum.SPR_BROK;
import static fr.euphyllia.fidorial.doom.engine.info.SpriteNum.SPR_BSKU;
import static fr.euphyllia.fidorial.doom.engine.info.SpriteNum.SPR_CELL;
import static fr.euphyllia.fidorial.doom.engine.info.SpriteNum.SPR_CELP;
import static fr.euphyllia.fidorial.doom.engine.info.SpriteNum.SPR_CLIP;
import static fr.euphyllia.fidorial.doom.engine.info.SpriteNum.SPR_CSAW;
import static fr.euphyllia.fidorial.doom.engine.info.SpriteNum.SPR_LAUN;
import static fr.euphyllia.fidorial.doom.engine.info.SpriteNum.SPR_MEDI;
import static fr.euphyllia.fidorial.doom.engine.info.SpriteNum.SPR_MEGA;
import static fr.euphyllia.fidorial.doom.engine.info.SpriteNum.SPR_MGUN;
import static fr.euphyllia.fidorial.doom.engine.info.SpriteNum.SPR_PINS;
import static fr.euphyllia.fidorial.doom.engine.info.SpriteNum.SPR_PINV;
import static fr.euphyllia.fidorial.doom.engine.info.SpriteNum.SPR_PLAS;
import static fr.euphyllia.fidorial.doom.engine.info.SpriteNum.SPR_PMAP;
import static fr.euphyllia.fidorial.doom.engine.info.SpriteNum.SPR_PSTR;
import static fr.euphyllia.fidorial.doom.engine.info.SpriteNum.SPR_PVIS;
import static fr.euphyllia.fidorial.doom.engine.info.SpriteNum.SPR_RKEY;
import static fr.euphyllia.fidorial.doom.engine.info.SpriteNum.SPR_ROCK;
import static fr.euphyllia.fidorial.doom.engine.info.SpriteNum.SPR_RSKU;
import static fr.euphyllia.fidorial.doom.engine.info.SpriteNum.SPR_SBOX;
import static fr.euphyllia.fidorial.doom.engine.info.SpriteNum.SPR_SGN2;
import static fr.euphyllia.fidorial.doom.engine.info.SpriteNum.SPR_SHEL;
import static fr.euphyllia.fidorial.doom.engine.info.SpriteNum.SPR_SHOT;
import static fr.euphyllia.fidorial.doom.engine.info.SpriteNum.SPR_SOUL;
import static fr.euphyllia.fidorial.doom.engine.info.SpriteNum.SPR_STIM;
import static fr.euphyllia.fidorial.doom.engine.info.SpriteNum.SPR_SUIT;
import static fr.euphyllia.fidorial.doom.engine.info.SpriteNum.SPR_YKEY;
import static fr.euphyllia.fidorial.doom.engine.info.SpriteNum.SPR_YSKU;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_NULL;

/**
 * Item pickups, damage and death ({@code p_inter.c}).
 */
public final class Inter {

    public static final int[] MAXAMMO = {200, 50, 300, 50};
    public static final int[] CLIPAMMO = {10, 4, 20, 1};
    private static final int BONUSADD = 6;
    private final Game g;

    public Inter(final Game g) {
        this.g = g;
    }

    public boolean P_GiveAmmo(final Player player, final int ammo, int num) {
        if (ammo == am_noammo) {
            return false;
        }
        if (ammo < 0 || ammo > NUMAMMO) {
            throw new IllegalArgumentException("P_GiveAmmo: bad type " + ammo);
        }
        if (player.ammo[ammo] == player.maxammo[ammo]) {
            return false;
        }
        if (num != 0) {
            num *= CLIPAMMO[ammo];
        } else {
            num = CLIPAMMO[ammo] / 2;
        }
        if (g.gameskill == sk_baby || g.gameskill == sk_nightmare) {
            num <<= 1;
        }
        final int oldammo = player.ammo[ammo];
        player.ammo[ammo] += num;
        if (player.ammo[ammo] > player.maxammo[ammo]) {
            player.ammo[ammo] = player.maxammo[ammo];
        }
        if (oldammo != 0) {
            return true;
        }
        switch (ammo) {
            case am_clip -> {
                if (player.readyweapon == wp_fist) {
                    if (player.weaponowned[wp_chaingun]) {
                        player.pendingweapon = wp_chaingun;
                    } else {
                        player.pendingweapon = wp_pistol;
                    }
                }
            }
            case am_shell -> {
                if (player.readyweapon == wp_fist || player.readyweapon == wp_pistol) {
                    if (player.weaponowned[wp_shotgun]) {
                        player.pendingweapon = wp_shotgun;
                    }
                }
            }
            case am_cell -> {
                if (player.readyweapon == wp_fist || player.readyweapon == wp_pistol) {
                    if (player.weaponowned[wp_plasma]) {
                        player.pendingweapon = wp_plasma;
                    }
                }
            }
            case am_misl -> {
                if (player.readyweapon == wp_fist) {
                    if (player.weaponowned[wp_missile]) {
                        player.pendingweapon = wp_missile;
                    }
                }
            }
            default -> {
            }
        }
        return true;
    }

    private boolean P_GiveWeapon(final Player player, final int weapon, final boolean dropped) {
        if (g.netgame && g.deathmatch != 2 && !dropped) {
            if (player.weaponowned[weapon]) {
                return false;
            }
            player.bonuscount += BONUSADD;
            player.weaponowned[weapon] = true;
            if (g.deathmatch != 0) {
                P_GiveAmmo(player, WEAPONINFO[weapon].ammo(), 5);
            } else {
                P_GiveAmmo(player, WEAPONINFO[weapon].ammo(), 2);
            }
            player.pendingweapon = weapon;
            g.sound.S_StartLocalSound(player, sfx_wpnup);
            return false;
        }
        final boolean gaveammo;
        if (WEAPONINFO[weapon].ammo() != am_noammo) {
            if (dropped) {
                gaveammo = P_GiveAmmo(player, WEAPONINFO[weapon].ammo(), 1);
            } else {
                gaveammo = P_GiveAmmo(player, WEAPONINFO[weapon].ammo(), 2);
            }
        } else {
            gaveammo = false;
        }
        final boolean gaveweapon;
        if (player.weaponowned[weapon]) {
            gaveweapon = false;
        } else {
            gaveweapon = true;
            player.weaponowned[weapon] = true;
            player.pendingweapon = weapon;
        }
        return gaveweapon || gaveammo;
    }

    public boolean P_GiveBody(final Player player, final int num) {
        if (player.health >= MAXHEALTH) {
            return false;
        }
        player.health += num;
        if (player.health > MAXHEALTH) {
            player.health = MAXHEALTH;
        }
        player.mo.health = player.health;
        return true;
    }

    public boolean P_GiveArmor(final Player player, final int armortype) {
        final int hits = armortype * 100;
        if (player.armorpoints >= hits) {
            return false;
        }
        player.armortype = armortype;
        player.armorpoints = hits;
        return true;
    }

    private void P_GiveCard(final Player player, final int card) {
        if (player.cards[card]) {
            return;
        }
        player.bonuscount = BONUSADD;
        player.cards[card] = true;
    }

    public boolean P_GivePower(final Player player, final int power) {
        if (power == pw_invulnerability) {
            player.powers[power] = INVULNTICS;
            return true;
        }
        if (power == pw_invisibility) {
            player.powers[power] = INVISTICS;
            player.mo.flags |= MF_SHADOW;
            return true;
        }
        if (power == pw_infrared) {
            player.powers[power] = INFRATICS;
            return true;
        }
        if (power == pw_ironfeet) {
            player.powers[power] = IRONTICS;
            return true;
        }
        if (power == pw_strength) {
            P_GiveBody(player, 100);
            player.powers[power] = 1;
            return true;
        }
        if (player.powers[power] != 0) {
            return false;
        }
        player.powers[power] = 1;
        return true;
    }

    public void P_TouchSpecialThing(final Mobj special, final Mobj toucher) {
        final int delta = special.z - toucher.z;
        if (delta > toucher.height || delta < -8 * FRACUNIT) {
            return;
        }
        int sound = sfx_itemup;
        final Player player = toucher.player;
        if (toucher.health <= 0) {
            return;
        }
        if (player == null) {
            return;
        }
        switch (special.sprite) {
            case SPR_ARM1 -> {
                if (!P_GiveArmor(player, 1)) {
                    return;
                }
                player.message = GOTARMOR;
            }
            case SPR_ARM2 -> {
                if (!P_GiveArmor(player, 2)) {
                    return;
                }
                player.message = GOTMEGA;
            }
            case SPR_BON1 -> {
                player.health++;
                if (player.health > 200) {
                    player.health = 200;
                }
                player.mo.health = player.health;
                player.message = GOTHTHBONUS;
            }
            case SPR_BON2 -> {
                player.armorpoints++;
                if (player.armorpoints > 200) {
                    player.armorpoints = 200;
                }
                if (player.armortype == 0) {
                    player.armortype = 1;
                }
                player.message = GOTARMBONUS;
            }
            case SPR_SOUL -> {
                player.health += 100;
                if (player.health > 200) {
                    player.health = 200;
                }
                player.mo.health = player.health;
                player.message = GOTSUPER;
                sound = sfx_getpow;
            }
            case SPR_MEGA -> {
                if (g.gamemode != commercial) {
                    return;
                }
                player.health = 200;
                player.mo.health = player.health;
                P_GiveArmor(player, 2);
                player.message = GOTMSPHERE;
                sound = sfx_getpow;
            }
            case SPR_BKEY, SPR_YKEY, SPR_RKEY, SPR_BSKU, SPR_YSKU, SPR_RSKU -> {
                final int card;
                final String msg;
                switch (special.sprite) {
                    case SPR_BKEY -> {
                        card = it_bluecard;
                        msg = GOTBLUECARD;
                    }
                    case SPR_YKEY -> {
                        card = it_yellowcard;
                        msg = GOTYELWCARD;
                    }
                    case SPR_RKEY -> {
                        card = it_redcard;
                        msg = GOTREDCARD;
                    }
                    case SPR_BSKU -> {
                        card = it_blueskull;
                        msg = GOTBLUESKUL;
                    }
                    case SPR_YSKU -> {
                        card = it_yellowskull;
                        msg = GOTYELWSKUL;
                    }
                    default -> {
                        card = it_redskull;
                        msg = GOTREDSKULL;
                    }
                }
                if (!player.cards[card]) {
                    player.message = msg;
                }
                P_GiveCard(player, card);
                if (g.netgame) {
                    return;
                }
            }
            case SPR_STIM -> {
                if (!P_GiveBody(player, 10)) {
                    return;
                }
                player.message = GOTSTIM;
            }
            case SPR_MEDI -> {
                if (!P_GiveBody(player, 25)) {
                    return;
                }
                // vanilla tests the health after healing, so this message never shows
                if (player.health < 25) {
                    player.message = GOTMEDINEED;
                } else {
                    player.message = GOTMEDIKIT;
                }
            }
            case SPR_PINV -> {
                if (!P_GivePower(player, pw_invulnerability)) {
                    return;
                }
                player.message = GOTINVUL;
                sound = sfx_getpow;
            }
            case SPR_PSTR -> {
                if (!P_GivePower(player, pw_strength)) {
                    return;
                }
                player.message = GOTBERSERK;
                if (player.readyweapon != wp_fist) {
                    player.pendingweapon = wp_fist;
                }
                sound = sfx_getpow;
            }
            case SPR_PINS -> {
                if (!P_GivePower(player, pw_invisibility)) {
                    return;
                }
                player.message = GOTINVIS;
                sound = sfx_getpow;
            }
            case SPR_SUIT -> {
                if (!P_GivePower(player, pw_ironfeet)) {
                    return;
                }
                player.message = GOTSUIT;
                sound = sfx_getpow;
            }
            case SPR_PMAP -> {
                if (!P_GivePower(player, pw_allmap)) {
                    return;
                }
                player.message = GOTMAP;
                sound = sfx_getpow;
            }
            case SPR_PVIS -> {
                if (!P_GivePower(player, pw_infrared)) {
                    return;
                }
                player.message = GOTVISOR;
                sound = sfx_getpow;
            }
            case SPR_CLIP -> {
                if ((special.flags & MF_DROPPED) != 0) {
                    if (!P_GiveAmmo(player, am_clip, 0)) {
                        return;
                    }
                } else {
                    if (!P_GiveAmmo(player, am_clip, 1)) {
                        return;
                    }
                }
                player.message = GOTCLIP;
            }
            case SPR_AMMO -> {
                if (!P_GiveAmmo(player, am_clip, 5)) {
                    return;
                }
                player.message = GOTCLIPBOX;
            }
            case SPR_ROCK -> {
                if (!P_GiveAmmo(player, am_misl, 1)) {
                    return;
                }
                player.message = GOTROCKET;
            }
            case SPR_BROK -> {
                if (!P_GiveAmmo(player, am_misl, 5)) {
                    return;
                }
                player.message = GOTROCKBOX;
            }
            case SPR_CELL -> {
                if (!P_GiveAmmo(player, am_cell, 1)) {
                    return;
                }
                player.message = GOTCELL;
            }
            case SPR_CELP -> {
                if (!P_GiveAmmo(player, am_cell, 5)) {
                    return;
                }
                player.message = GOTCELLBOX;
            }
            case SPR_SHEL -> {
                if (!P_GiveAmmo(player, am_shell, 1)) {
                    return;
                }
                player.message = GOTSHELLS;
            }
            case SPR_SBOX -> {
                if (!P_GiveAmmo(player, am_shell, 5)) {
                    return;
                }
                player.message = GOTSHELLBOX;
            }
            case SPR_BPAK -> {
                if (!player.backpack) {
                    for (int i = 0; i < NUMAMMO; i++) {
                        player.maxammo[i] *= 2;
                    }
                    player.backpack = true;
                }
                for (int i = 0; i < NUMAMMO; i++) {
                    P_GiveAmmo(player, i, 1);
                }
                player.message = GOTBACKPACK;
            }
            case SPR_BFUG -> {
                if (!P_GiveWeapon(player, wp_bfg, false)) {
                    return;
                }
                player.message = GOTBFG9000;
                sound = sfx_wpnup;
            }
            case SPR_MGUN -> {
                if (!P_GiveWeapon(player, wp_chaingun, (special.flags & MF_DROPPED) != 0)) {
                    return;
                }
                player.message = GOTCHAINGUN;
                sound = sfx_wpnup;
            }
            case SPR_CSAW -> {
                if (!P_GiveWeapon(player, wp_chainsaw, false)) {
                    return;
                }
                player.message = GOTCHAINSAW;
                sound = sfx_wpnup;
            }
            case SPR_LAUN -> {
                if (!P_GiveWeapon(player, wp_missile, false)) {
                    return;
                }
                player.message = GOTLAUNCHER;
                sound = sfx_wpnup;
            }
            case SPR_PLAS -> {
                if (!P_GiveWeapon(player, wp_plasma, false)) {
                    return;
                }
                player.message = GOTPLASMA;
                sound = sfx_wpnup;
            }
            case SPR_SHOT -> {
                if (!P_GiveWeapon(player, wp_shotgun, (special.flags & MF_DROPPED) != 0)) {
                    return;
                }
                player.message = GOTSHOTGUN;
                sound = sfx_wpnup;
            }
            case SPR_SGN2 -> {
                if (!P_GiveWeapon(player, wp_supershotgun, (special.flags & MF_DROPPED) != 0)) {
                    return;
                }
                player.message = GOTSHOTGUN2;
                sound = sfx_wpnup;
            }
            default -> throw new IllegalStateException("P_SpecialThing: Unknown gettable thing");
        }
        if ((special.flags & MF_COUNTITEM) != 0) {
            player.itemcount++;
        }
        g.mobjs.P_RemoveMobj(special);
        player.bonuscount += BONUSADD;
        g.sound.S_StartLocalSound(player, sound);
    }

    public void P_KillMobj(final @Nullable Mobj source, final Mobj target) {
        target.flags &= ~(MF_SHOOTABLE | MF_FLOAT | MF_SKULLFLY);
        if (target.type != MT_SKULL) {
            target.flags &= ~MF_NOGRAVITY;
        }
        target.flags |= MF_CORPSE | MF_DROPOFF;
        target.height >>= 2;
        if (source != null && source.player != null) {
            if ((target.flags & MF_COUNTKILL) != 0) {
                source.player.killcount++;
            }
            if (target.player != null) {
                source.player.frags[target.player.index]++;
            }
        } else if (!g.netgame && (target.flags & MF_COUNTKILL) != 0) {
            g.players[0].killcount++;
        }
        if (target.player != null) {
            if (source == null) {
                target.player.frags[target.player.index]++;
            }
            target.flags &= ~MF_SOLID;
            target.player.playerstate = PST_DEAD;
            g.pspr.P_DropWeapon(target.player);
            g.listener().playerKilled(target.player);
        }
        if (target.health < -target.info.spawnhealth() && target.info.xdeathstate() != 0) {
            g.mobjs.P_SetMobjState(target, target.info.xdeathstate());
        } else {
            g.mobjs.P_SetMobjState(target, target.info.deathstate());
        }
        target.tics -= g.random.P_Random() & 3;
        if (target.tics < 1) {
            target.tics = 1;
        }
        final int item;
        switch (target.type) {
            case MT_WOLFSS, MT_POSSESSED -> item = MT_CLIP;
            case MT_SHOTGUY -> item = MT_SHOTGUN;
            case MT_CHAINGUY -> item = MT_CHAINGUN;
            default -> {
                return;
            }
        }
        final Mobj mo = g.mobjs.P_SpawnMobj(target.x, target.y, ONFLOORZ, item);
        mo.flags |= MF_DROPPED;
    }

    public void P_DamageMobj(final Mobj target, final @Nullable Mobj inflictor, final @Nullable Mobj source, int damage) {
        if ((target.flags & MF_SHOOTABLE) == 0) {
            return;
        }
        if (target.health <= 0) {
            return;
        }
        if ((target.flags & MF_SKULLFLY) != 0) {
            target.momx = target.momy = target.momz = 0;
        }
        final Player player = target.player;
        if (player != null && g.gameskill == sk_baby) {
            damage >>= 1;
        }
        if (inflictor != null && (target.flags & MF_NOCLIP) == 0
                && (source == null || source.player == null || source.player.readyweapon != wp_chainsaw)) {
            int ang = Angles.pointToAngle2(inflictor.x, inflictor.y, target.x, target.y);
            int thrust = damage * (FRACUNIT >> 3) * 100 / target.info.mass();
            if (damage < 40 && damage > target.health && target.z - inflictor.z > 64 * FRACUNIT
                    && (g.random.P_Random() & 1) != 0) {
                ang += ANG180;
                thrust *= 4;
            }
            ang >>>= ANGLETOFINESHIFT;
            target.momx += mul(thrust, finecosine(ang));
            target.momy += mul(thrust, finesine[ang]);
        }
        if (player != null) {
            if (target.subsector.sector.special == 11 && damage >= target.health) {
                damage = target.health - 1;
            }
            if (damage < 1000 && ((player.cheats & CF_GODMODE) != 0 || player.powers[pw_invulnerability] != 0)) {
                return;
            }
            if (player.armortype != 0) {
                int saved;
                if (player.armortype == 1) {
                    saved = damage / 3;
                } else {
                    saved = damage / 2;
                }
                if (player.armorpoints <= saved) {
                    saved = player.armorpoints;
                    player.armortype = 0;
                }
                player.armorpoints -= saved;
                damage -= saved;
            }
            player.health -= damage;
            if (player.health < 0) {
                player.health = 0;
            }
            player.attacker = source;
            player.damagecount += damage;
            if (player.damagecount > 100) {
                player.damagecount = 100;
            }
        }
        target.health -= damage;
        if (target.health <= 0) {
            P_KillMobj(source, target);
            return;
        }
        if (g.random.P_Random() < target.info.painchance() && (target.flags & MF_SKULLFLY) == 0) {
            target.flags |= MF_JUSTHIT;
            g.mobjs.P_SetMobjState(target, target.info.painstate());
        }
        target.reactiontime = 0;
        if ((target.threshold == 0 || target.type == MT_VILE) && source != null && source != target
                && source.type != MT_VILE) {
            target.target = source;
            target.threshold = BASETHRESHOLD;
            if (target.state != null && target.state.index() == target.info.spawnstate()
                    && target.info.seestate() != S_NULL) {
                g.mobjs.P_SetMobjState(target, target.info.seestate());
            }
        }
    }
}
