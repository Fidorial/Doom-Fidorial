// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine.play;

import fr.euphyllia.fidorial.doom.engine.Angles;
import fr.euphyllia.fidorial.doom.engine.Game;
import fr.euphyllia.fidorial.doom.engine.Mobj;
import fr.euphyllia.fidorial.doom.engine.Player;
import fr.euphyllia.fidorial.doom.engine.PspDef;
import fr.euphyllia.fidorial.doom.engine.info.State;

import static fr.euphyllia.fidorial.doom.engine.Defs.BT_ATTACK;
import static fr.euphyllia.fidorial.doom.engine.Defs.MELEERANGE;
import static fr.euphyllia.fidorial.doom.engine.Defs.MISSILERANGE;
import static fr.euphyllia.fidorial.doom.engine.Defs.NUMPSPRITES;
import static fr.euphyllia.fidorial.doom.engine.Defs.PST_DEAD;
import static fr.euphyllia.fidorial.doom.engine.Defs.am_cell;
import static fr.euphyllia.fidorial.doom.engine.Defs.am_clip;
import static fr.euphyllia.fidorial.doom.engine.Defs.am_misl;
import static fr.euphyllia.fidorial.doom.engine.Defs.am_noammo;
import static fr.euphyllia.fidorial.doom.engine.Defs.am_shell;
import static fr.euphyllia.fidorial.doom.engine.Defs.commercial;
import static fr.euphyllia.fidorial.doom.engine.Defs.ps_flash;
import static fr.euphyllia.fidorial.doom.engine.Defs.ps_weapon;
import static fr.euphyllia.fidorial.doom.engine.Defs.pw_strength;
import static fr.euphyllia.fidorial.doom.engine.Defs.shareware;
import static fr.euphyllia.fidorial.doom.engine.Defs.wp_bfg;
import static fr.euphyllia.fidorial.doom.engine.Defs.wp_chaingun;
import static fr.euphyllia.fidorial.doom.engine.Defs.wp_chainsaw;
import static fr.euphyllia.fidorial.doom.engine.Defs.wp_fist;
import static fr.euphyllia.fidorial.doom.engine.Defs.wp_missile;
import static fr.euphyllia.fidorial.doom.engine.Defs.wp_nochange;
import static fr.euphyllia.fidorial.doom.engine.Defs.wp_pistol;
import static fr.euphyllia.fidorial.doom.engine.Defs.wp_plasma;
import static fr.euphyllia.fidorial.doom.engine.Defs.wp_shotgun;
import static fr.euphyllia.fidorial.doom.engine.Defs.wp_supershotgun;
import static fr.euphyllia.fidorial.doom.engine.Fixed.FRACBITS;
import static fr.euphyllia.fidorial.doom.engine.Fixed.FRACUNIT;
import static fr.euphyllia.fidorial.doom.engine.Fixed.mul;
import static fr.euphyllia.fidorial.doom.engine.Items.WEAPONINFO;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_JUSTATTACKED;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_bfg;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_dshtgn;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_pistol;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_punch;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_sawful;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_sawhit;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_sawidl;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_sawup;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_shotgn;
import static fr.euphyllia.fidorial.doom.engine.Tables.ANG180;
import static fr.euphyllia.fidorial.doom.engine.Tables.ANG90;
import static fr.euphyllia.fidorial.doom.engine.Tables.FINEANGLES;
import static fr.euphyllia.fidorial.doom.engine.Tables.FINEMASK;
import static fr.euphyllia.fidorial.doom.engine.Tables.finecosine;
import static fr.euphyllia.fidorial.doom.engine.Tables.finesine;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_BFG;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_EXTRABFG;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_PLASMA;
import static fr.euphyllia.fidorial.doom.engine.info.MobjType.MT_ROCKET;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_CHAIN1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_NULL;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_PLAY;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_PLAY_ATK1;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_PLAY_ATK2;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_SAW;

/**
 * Weapon sprite animation and weapon actions ({@code p_pspr.c}).
 */
public final class Pspr {

    public static final int WEAPONBOTTOM = 128 * FRACUNIT;
    public static final int WEAPONTOP = 32 * FRACUNIT;
    private static final int LOWERSPEED = FRACUNIT * 6;
    private static final int RAISESPEED = FRACUNIT * 6;
    private static final int BFGCELLS = 40;

    private final Game g;
    private int bulletslope;

    public Pspr(final Game g) {
        this.g = g;
    }

    public void P_SetPsprite(final Player player, final int position, int stnum) {
        final PspDef psp = player.psprites[position];
        do {
            if (stnum == 0) {
                psp.state = null;
                break;
            }
            final State state = g.states[stnum];
            psp.state = state;
            psp.tics = state.tics();
            if (state.misc1() != 0) {
                psp.sx = state.misc1() << FRACBITS;
                psp.sy = state.misc2() << FRACBITS;
            }
            if (state.action() != null) {
                g.actions.callPlayer(state.action(), player, psp);
                if (psp.state == null) {
                    break;
                }
            }
            stnum = psp.state.nextstate();
        } while (psp.tics == 0);
    }

    private void P_BringUpWeapon(final Player player) {
        if (player.pendingweapon == wp_nochange) {
            player.pendingweapon = player.readyweapon;
        }
        if (player.pendingweapon == wp_chainsaw) {
            g.sound.S_StartSound(player.mo, sfx_sawup);
        }
        final int newstate = WEAPONINFO[player.pendingweapon].upstate();
        player.pendingweapon = wp_nochange;
        player.psprites[ps_weapon].sy = WEAPONBOTTOM;
        P_SetPsprite(player, ps_weapon, newstate);
    }

    private boolean P_CheckAmmo(final Player player) {
        final int ammo = WEAPONINFO[player.readyweapon].ammo();
        final int count;
        if (player.readyweapon == wp_bfg) {
            count = BFGCELLS;
        } else if (player.readyweapon == wp_supershotgun) {
            count = 2;
        } else {
            count = 1;
        }
        if (ammo == am_noammo || player.ammo[ammo] >= count) {
            return true;
        }
        do {
            if (player.weaponowned[wp_plasma] && player.ammo[am_cell] != 0 && g.gamemode != shareware) {
                player.pendingweapon = wp_plasma;
            } else if (player.weaponowned[wp_supershotgun] && player.ammo[am_shell] > 2 && g.gamemode == commercial) {
                player.pendingweapon = wp_supershotgun;
            } else if (player.weaponowned[wp_chaingun] && player.ammo[am_clip] != 0) {
                player.pendingweapon = wp_chaingun;
            } else if (player.weaponowned[wp_shotgun] && player.ammo[am_shell] != 0) {
                player.pendingweapon = wp_shotgun;
            } else if (player.ammo[am_clip] != 0) {
                player.pendingweapon = wp_pistol;
            } else if (player.weaponowned[wp_chainsaw]) {
                player.pendingweapon = wp_chainsaw;
            } else if (player.weaponowned[wp_missile] && player.ammo[am_misl] != 0) {
                player.pendingweapon = wp_missile;
            } else if (player.weaponowned[wp_bfg] && player.ammo[am_cell] > 40 && g.gamemode != shareware) {
                player.pendingweapon = wp_bfg;
            } else {
                player.pendingweapon = wp_fist;
            }
        } while (player.pendingweapon == wp_nochange);
        P_SetPsprite(player, ps_weapon, WEAPONINFO[player.readyweapon].downstate());
        return false;
    }

    private void P_FireWeapon(final Player player) {
        if (!P_CheckAmmo(player)) {
            return;
        }
        g.mobjs.P_SetMobjState(player.mo, S_PLAY_ATK1);
        final int newstate = WEAPONINFO[player.readyweapon].atkstate();
        P_SetPsprite(player, ps_weapon, newstate);
        g.enemy.P_NoiseAlert(player.mo, player.mo);
    }

    public void P_DropWeapon(final Player player) {
        P_SetPsprite(player, ps_weapon, WEAPONINFO[player.readyweapon].downstate());
    }

    public void A_WeaponReady(final Player player, final PspDef psp) {
        final int mostate = player.mo.stateIndex();
        if (mostate == S_PLAY_ATK1 || mostate == S_PLAY_ATK2) {
            g.mobjs.P_SetMobjState(player.mo, S_PLAY);
        }
        if (player.readyweapon == wp_chainsaw && psp.state != null && psp.state.index() == S_SAW) {
            g.sound.S_StartSound(player.mo, sfx_sawidl);
        }
        if (player.pendingweapon != wp_nochange || player.health == 0) {
            final int newstate = WEAPONINFO[player.readyweapon].downstate();
            P_SetPsprite(player, ps_weapon, newstate);
            return;
        }
        if ((player.cmd.buttons() & BT_ATTACK) != 0) {
            if (player.attackdown == 0 || (player.readyweapon != wp_missile && player.readyweapon != wp_bfg)) {
                player.attackdown = 1;
                P_FireWeapon(player);
                return;
            }
        } else {
            player.attackdown = 0;
        }
        int angle = (128 * g.leveltime) & FINEMASK;
        psp.sx = FRACUNIT + mul(player.bob, finecosine(angle));
        angle &= FINEANGLES / 2 - 1;
        psp.sy = WEAPONTOP + mul(player.bob, finesine[angle]);
    }

    public void A_ReFire(final Player player, final PspDef psp) {
        if ((player.cmd.buttons() & BT_ATTACK) != 0 && player.pendingweapon == wp_nochange && player.health != 0) {
            player.refire++;
            P_FireWeapon(player);
        } else {
            player.refire = 0;
            P_CheckAmmo(player);
        }
    }

    public void A_CheckReload(final Player player, final PspDef psp) {
        P_CheckAmmo(player);
    }

    public void A_Lower(final Player player, final PspDef psp) {
        psp.sy += LOWERSPEED;
        if (psp.sy < WEAPONBOTTOM) {
            return;
        }
        if (player.playerstate == PST_DEAD) {
            psp.sy = WEAPONBOTTOM;
            return;
        }
        if (player.health == 0) {
            P_SetPsprite(player, ps_weapon, S_NULL);
            return;
        }
        player.readyweapon = player.pendingweapon;
        P_BringUpWeapon(player);
    }

    public void A_Raise(final Player player, final PspDef psp) {
        psp.sy -= RAISESPEED;
        if (psp.sy > WEAPONTOP) {
            return;
        }
        psp.sy = WEAPONTOP;
        final int newstate = WEAPONINFO[player.readyweapon].readystate();
        P_SetPsprite(player, ps_weapon, newstate);
    }

    public void A_GunFlash(final Player player, final PspDef psp) {
        g.mobjs.P_SetMobjState(player.mo, S_PLAY_ATK2);
        P_SetPsprite(player, ps_flash, WEAPONINFO[player.readyweapon].flashstate());
    }

    public void A_Punch(final Player player, final PspDef psp) {
        int damage = (g.random.P_Random() % 10 + 1) << 1;
        if (player.powers[pw_strength] != 0) {
            damage *= 10;
        }
        int angle = player.mo.angle;
        angle += (g.random.P_Random() - g.random.P_Random()) << 18;
        final int slope = g.map.P_AimLineAttack(player.mo, angle, MELEERANGE);
        g.map.P_LineAttack(player.mo, angle, MELEERANGE, slope, damage);
        final Mobj linetarget = g.map.linetarget;
        if (linetarget != null) {
            g.sound.S_StartSound(player.mo, sfx_punch);
            player.mo.angle = Angles.pointToAngle2(player.mo.x, player.mo.y, linetarget.x, linetarget.y);
        }
    }

    public void A_Saw(final Player player, final PspDef psp) {
        final int damage = 2 * (g.random.P_Random() % 10 + 1);
        int angle = player.mo.angle;
        angle += (g.random.P_Random() - g.random.P_Random()) << 18;
        final int slope = g.map.P_AimLineAttack(player.mo, angle, MELEERANGE + 1);
        g.map.P_LineAttack(player.mo, angle, MELEERANGE + 1, slope, damage);
        final Mobj linetarget = g.map.linetarget;
        if (linetarget == null) {
            g.sound.S_StartSound(player.mo, sfx_sawful);
            return;
        }
        g.sound.S_StartSound(player.mo, sfx_sawhit);
        angle = Angles.pointToAngle2(player.mo.x, player.mo.y, linetarget.x, linetarget.y);
        final int diff = angle - player.mo.angle;
        if (Integer.compareUnsigned(diff, ANG180) > 0) {
            if (Integer.compareUnsigned(diff, -ANG90 / 20) < 0) {
                player.mo.angle = angle + ANG90 / 21;
            } else {
                player.mo.angle -= ANG90 / 20;
            }
        } else {
            if (Integer.compareUnsigned(diff, ANG90 / 20) > 0) {
                player.mo.angle = angle - ANG90 / 21;
            } else {
                player.mo.angle += ANG90 / 20;
            }
        }
        player.mo.flags |= MF_JUSTATTACKED;
    }

    public void A_FireMissile(final Player player, final PspDef psp) {
        player.ammo[WEAPONINFO[player.readyweapon].ammo()]--;
        g.mobjs.P_SpawnPlayerMissile(player.mo, MT_ROCKET);
    }

    public void A_FireBFG(final Player player, final PspDef psp) {
        player.ammo[WEAPONINFO[player.readyweapon].ammo()] -= BFGCELLS;
        g.mobjs.P_SpawnPlayerMissile(player.mo, MT_BFG);
    }

    public void A_FirePlasma(final Player player, final PspDef psp) {
        player.ammo[WEAPONINFO[player.readyweapon].ammo()]--;
        P_SetPsprite(player, ps_flash, WEAPONINFO[player.readyweapon].flashstate() + (g.random.P_Random() & 1));
        g.mobjs.P_SpawnPlayerMissile(player.mo, MT_PLASMA);
    }

    private void P_BulletSlope(final Mobj mo) {
        int an = mo.angle;
        bulletslope = g.map.P_AimLineAttack(mo, an, 16 * 64 * FRACUNIT);
        if (g.map.linetarget == null) {
            an += 1 << 26;
            bulletslope = g.map.P_AimLineAttack(mo, an, 16 * 64 * FRACUNIT);
            if (g.map.linetarget == null) {
                an -= 2 << 26;
                bulletslope = g.map.P_AimLineAttack(mo, an, 16 * 64 * FRACUNIT);
            }
        }
    }

    private void P_GunShot(final Mobj mo, final boolean accurate) {
        final int damage = 5 * (g.random.P_Random() % 3 + 1);
        int angle = mo.angle;
        if (!accurate) {
            angle += (g.random.P_Random() - g.random.P_Random()) << 18;
        }
        g.map.P_LineAttack(mo, angle, MISSILERANGE, bulletslope, damage);
    }

    public void A_FirePistol(final Player player, final PspDef psp) {
        g.sound.S_StartSound(player.mo, sfx_pistol);
        g.mobjs.P_SetMobjState(player.mo, S_PLAY_ATK2);
        player.ammo[WEAPONINFO[player.readyweapon].ammo()]--;
        P_SetPsprite(player, ps_flash, WEAPONINFO[player.readyweapon].flashstate());
        P_BulletSlope(player.mo);
        P_GunShot(player.mo, player.refire == 0);
    }

    public void A_FireShotgun(final Player player, final PspDef psp) {
        g.sound.S_StartSound(player.mo, sfx_shotgn);
        g.mobjs.P_SetMobjState(player.mo, S_PLAY_ATK2);
        player.ammo[WEAPONINFO[player.readyweapon].ammo()]--;
        P_SetPsprite(player, ps_flash, WEAPONINFO[player.readyweapon].flashstate());
        P_BulletSlope(player.mo);
        for (int i = 0; i < 7; i++) {
            P_GunShot(player.mo, false);
        }
    }

    public void A_FireShotgun2(final Player player, final PspDef psp) {
        g.sound.S_StartSound(player.mo, sfx_dshtgn);
        g.mobjs.P_SetMobjState(player.mo, S_PLAY_ATK2);
        player.ammo[WEAPONINFO[player.readyweapon].ammo()] -= 2;
        P_SetPsprite(player, ps_flash, WEAPONINFO[player.readyweapon].flashstate());
        P_BulletSlope(player.mo);
        for (int i = 0; i < 20; i++) {
            final int damage = 5 * (g.random.P_Random() % 3 + 1);
            int angle = player.mo.angle;
            angle += (g.random.P_Random() - g.random.P_Random()) << 19;
            g.map.P_LineAttack(player.mo, angle, MISSILERANGE,
                    bulletslope + ((g.random.P_Random() - g.random.P_Random()) << 5), damage);
        }
    }

    public void A_FireCGun(final Player player, final PspDef psp) {
        g.sound.S_StartSound(player.mo, sfx_pistol);
        if (player.ammo[WEAPONINFO[player.readyweapon].ammo()] == 0) {
            return;
        }
        g.mobjs.P_SetMobjState(player.mo, S_PLAY_ATK2);
        player.ammo[WEAPONINFO[player.readyweapon].ammo()]--;
        P_SetPsprite(player, ps_flash,
                WEAPONINFO[player.readyweapon].flashstate() + psp.state.index() - S_CHAIN1);
        P_BulletSlope(player.mo);
        P_GunShot(player.mo, player.refire == 0);
    }

    public void A_Light0(final Player player, final PspDef psp) {
        player.extralight = 0;
    }

    public void A_Light1(final Player player, final PspDef psp) {
        player.extralight = 1;
    }

    public void A_Light2(final Player player, final PspDef psp) {
        player.extralight = 2;
    }

    public void A_BFGSpray(final Mobj mo) {
        for (int i = 0; i < 40; i++) {
            final int an = mo.angle - ANG90 / 2 + ANG90 / 40 * i;
            g.map.P_AimLineAttack(mo.target, an, 16 * 64 * FRACUNIT);
            final Mobj linetarget = g.map.linetarget;
            if (linetarget == null) {
                continue;
            }
            g.mobjs.P_SpawnMobj(linetarget.x, linetarget.y, linetarget.z + (linetarget.height >> 2), MT_EXTRABFG);
            int damage = 0;
            for (int j = 0; j < 15; j++) {
                damage += (g.random.P_Random() & 7) + 1;
            }
            g.inter.P_DamageMobj(linetarget, mo.target, mo.target, damage);
        }
    }

    public void A_BFGsound(final Player player, final PspDef psp) {
        g.sound.S_StartSound(player.mo, sfx_bfg);
    }

    public void P_SetupPsprites(final Player player) {
        for (int i = 0; i < NUMPSPRITES; i++) {
            player.psprites[i].state = null;
        }
        player.pendingweapon = player.readyweapon;
        P_BringUpWeapon(player);
    }

    public void P_MovePsprites(final Player player) {
        for (int i = 0; i < NUMPSPRITES; i++) {
            final PspDef psp = player.psprites[i];
            final State state = psp.state;
            if (state != null) {
                if (psp.tics != -1) {
                    psp.tics--;
                    if (psp.tics == 0) {
                        P_SetPsprite(player, i, psp.state.nextstate());
                    }
                }
            }
        }
        player.psprites[ps_flash].sx = player.psprites[ps_weapon].sx;
        player.psprites[ps_flash].sy = player.psprites[ps_weapon].sy;
    }
}
