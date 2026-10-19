// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine.play;

import fr.euphyllia.fidorial.doom.engine.Angles;
import fr.euphyllia.fidorial.doom.engine.Game;
import fr.euphyllia.fidorial.doom.engine.Player;
import fr.euphyllia.fidorial.doom.engine.TicCmd;

import static fr.euphyllia.fidorial.doom.engine.Defs.BT_CHANGE;
import static fr.euphyllia.fidorial.doom.engine.Defs.BT_SPECIAL;
import static fr.euphyllia.fidorial.doom.engine.Defs.BT_USE;
import static fr.euphyllia.fidorial.doom.engine.Defs.BT_WEAPONMASK;
import static fr.euphyllia.fidorial.doom.engine.Defs.BT_WEAPONSHIFT;
import static fr.euphyllia.fidorial.doom.engine.Defs.CF_NOCLIP;
import static fr.euphyllia.fidorial.doom.engine.Defs.CF_NOMOMENTUM;
import static fr.euphyllia.fidorial.doom.engine.Defs.PST_DEAD;
import static fr.euphyllia.fidorial.doom.engine.Defs.PST_LIVE;
import static fr.euphyllia.fidorial.doom.engine.Defs.PST_REBORN;
import static fr.euphyllia.fidorial.doom.engine.Defs.VIEWHEIGHT;
import static fr.euphyllia.fidorial.doom.engine.Defs.commercial;
import static fr.euphyllia.fidorial.doom.engine.Defs.pw_infrared;
import static fr.euphyllia.fidorial.doom.engine.Defs.pw_invisibility;
import static fr.euphyllia.fidorial.doom.engine.Defs.pw_invulnerability;
import static fr.euphyllia.fidorial.doom.engine.Defs.pw_ironfeet;
import static fr.euphyllia.fidorial.doom.engine.Defs.pw_strength;
import static fr.euphyllia.fidorial.doom.engine.Defs.shareware;
import static fr.euphyllia.fidorial.doom.engine.Defs.wp_bfg;
import static fr.euphyllia.fidorial.doom.engine.Defs.wp_chainsaw;
import static fr.euphyllia.fidorial.doom.engine.Defs.wp_fist;
import static fr.euphyllia.fidorial.doom.engine.Defs.wp_plasma;
import static fr.euphyllia.fidorial.doom.engine.Defs.wp_shotgun;
import static fr.euphyllia.fidorial.doom.engine.Defs.wp_supershotgun;
import static fr.euphyllia.fidorial.doom.engine.Fixed.FRACUNIT;
import static fr.euphyllia.fidorial.doom.engine.Fixed.mul;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_JUSTATTACKED;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_NOCLIP;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_SHADOW;
import static fr.euphyllia.fidorial.doom.engine.Tables.ANG180;
import static fr.euphyllia.fidorial.doom.engine.Tables.ANG90;
import static fr.euphyllia.fidorial.doom.engine.Tables.ANGLETOFINESHIFT;
import static fr.euphyllia.fidorial.doom.engine.Tables.FINEANGLES;
import static fr.euphyllia.fidorial.doom.engine.Tables.FINEMASK;
import static fr.euphyllia.fidorial.doom.engine.Tables.finecosine;
import static fr.euphyllia.fidorial.doom.engine.Tables.finesine;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_PLAY;
import static fr.euphyllia.fidorial.doom.engine.info.StateNum.S_PLAY_RUN1;

/**
 * Player movement and per-tic upkeep ({@code p_user.c}).
 */
public final class User {

    private static final int INVERSECOLORMAP = 32;
    private static final int MAXBOB = 0x100000;
    private static final int ANG5 = ANG90 / 18;

    private final Game g;
    private boolean onground;

    public User(final Game g) {
        this.g = g;
    }

    public void P_Thrust(final Player player, int angle, final int move) {
        angle >>>= ANGLETOFINESHIFT;
        player.mo.momx += mul(move, finecosine(angle));
        player.mo.momy += mul(move, finesine[angle]);
    }

    public void P_CalcHeight(final Player player) {
        player.bob = mul(player.mo.momx, player.mo.momx) + mul(player.mo.momy, player.mo.momy);
        player.bob >>= 2;
        if (player.bob > MAXBOB) {
            player.bob = MAXBOB;
        }
        if ((player.cheats & CF_NOMOMENTUM) != 0 || !onground) {
            player.viewz = player.mo.z + VIEWHEIGHT;
            if (player.viewz > player.mo.ceilingz - 4 * FRACUNIT) {
                player.viewz = player.mo.ceilingz - 4 * FRACUNIT;
            }
            player.viewz = player.mo.z + player.viewheight;
            return;
        }
        final int angle = (FINEANGLES / 20 * g.leveltime) & FINEMASK;
        final int bob = mul(player.bob / 2, finesine[angle]);
        if (player.playerstate == PST_LIVE) {
            player.viewheight += player.deltaviewheight;
            if (player.viewheight > VIEWHEIGHT) {
                player.viewheight = VIEWHEIGHT;
                player.deltaviewheight = 0;
            }
            if (player.viewheight < VIEWHEIGHT / 2) {
                player.viewheight = VIEWHEIGHT / 2;
                if (player.deltaviewheight <= 0) {
                    player.deltaviewheight = 1;
                }
            }
            if (player.deltaviewheight != 0) {
                player.deltaviewheight += FRACUNIT / 4;
                if (player.deltaviewheight == 0) {
                    player.deltaviewheight = 1;
                }
            }
        }
        player.viewz = player.mo.z + player.viewheight + bob;
        if (player.viewz > player.mo.ceilingz - 4 * FRACUNIT) {
            player.viewz = player.mo.ceilingz - 4 * FRACUNIT;
        }
    }

    private void P_MovePlayer(final Player player) {
        final TicCmd cmd = player.cmd;
        player.mo.angle += cmd.angleturn << 16;
        onground = player.mo.z <= player.mo.floorz;
        if (cmd.forwardmove != 0 && onground) {
            P_Thrust(player, player.mo.angle, cmd.forwardmove * 2048);
        }
        if (cmd.sidemove != 0 && onground) {
            P_Thrust(player, player.mo.angle - ANG90, cmd.sidemove * 2048);
        }
        if ((cmd.forwardmove != 0 || cmd.sidemove != 0) && player.mo.stateIndex() == S_PLAY) {
            g.mobjs.P_SetMobjState(player.mo, S_PLAY_RUN1);
        }
    }

    private void P_DeathThink(final Player player) {
        g.pspr.P_MovePsprites(player);
        if (player.viewheight > 6 * FRACUNIT) {
            player.viewheight -= FRACUNIT;
        }
        if (player.viewheight < 6 * FRACUNIT) {
            player.viewheight = 6 * FRACUNIT;
        }
        player.deltaviewheight = 0;
        onground = player.mo.z <= player.mo.floorz;
        P_CalcHeight(player);
        if (player.attacker != null && player.attacker != player.mo) {
            final int angle = Angles.pointToAngle2(player.mo.x, player.mo.y, player.attacker.x, player.attacker.y);
            final int delta = angle - player.mo.angle;
            if (Integer.compareUnsigned(delta, ANG5) < 0 || Integer.compareUnsigned(delta, -ANG5) > 0) {
                player.mo.angle = angle;
                if (player.damagecount != 0) {
                    player.damagecount--;
                }
            } else if (Integer.compareUnsigned(delta, ANG180) < 0) {
                player.mo.angle += ANG5;
            } else {
                player.mo.angle -= ANG5;
            }
        } else if (player.damagecount != 0) {
            player.damagecount--;
        }
        if ((player.cmd.buttons() & BT_USE) != 0) {
            player.playerstate = PST_REBORN;
        }
    }

    public void P_PlayerThink(final Player player) {
        if ((player.cheats & CF_NOCLIP) != 0) {
            player.mo.flags |= MF_NOCLIP;
        } else {
            player.mo.flags &= ~MF_NOCLIP;
        }
        final TicCmd cmd = player.cmd;
        if ((player.mo.flags & MF_JUSTATTACKED) != 0) {
            cmd.angleturn = 0;
            cmd.forwardmove = (byte) (0xc800 / 512);
            cmd.sidemove = 0;
            player.mo.flags &= ~MF_JUSTATTACKED;
        }
        if (player.playerstate == PST_DEAD) {
            P_DeathThink(player);
            return;
        }
        if (player.mo.reactiontime != 0) {
            player.mo.reactiontime--;
        } else {
            P_MovePlayer(player);
        }
        P_CalcHeight(player);
        if (player.mo.subsector.sector.special != 0) {
            g.spec.P_PlayerInSpecialSector(player);
        }
        if ((cmd.buttons() & BT_SPECIAL) != 0) {
            cmd.buttons = 0;
        }
        if ((cmd.buttons() & BT_CHANGE) != 0) {
            int newweapon = (cmd.buttons() & BT_WEAPONMASK) >> BT_WEAPONSHIFT;
            if (newweapon == wp_fist && player.weaponowned[wp_chainsaw]
                    && !(player.readyweapon == wp_chainsaw && player.powers[pw_strength] != 0)) {
                newweapon = wp_chainsaw;
            }
            if (g.gamemode == commercial && newweapon == wp_shotgun && player.weaponowned[wp_supershotgun]
                    && player.readyweapon != wp_supershotgun) {
                newweapon = wp_supershotgun;
            }
            if (player.weaponowned[newweapon] && newweapon != player.readyweapon) {
                if ((newweapon != wp_plasma && newweapon != wp_bfg) || g.gamemode != shareware) {
                    player.pendingweapon = newweapon;
                }
            }
        }
        if ((cmd.buttons() & BT_USE) != 0) {
            if (player.usedown == 0) {
                g.map.P_UseLines(player);
                player.usedown = 1;
            }
        } else {
            player.usedown = 0;
        }
        g.pspr.P_MovePsprites(player);
        if (player.powers[pw_strength] != 0) {
            player.powers[pw_strength]++;
        }
        if (player.powers[pw_invulnerability] != 0) {
            player.powers[pw_invulnerability]--;
        }
        if (player.powers[pw_invisibility] != 0) {
            if (--player.powers[pw_invisibility] == 0) {
                player.mo.flags &= ~MF_SHADOW;
            }
        }
        if (player.powers[pw_infrared] != 0) {
            player.powers[pw_infrared]--;
        }
        if (player.powers[pw_ironfeet] != 0) {
            player.powers[pw_ironfeet]--;
        }
        if (player.damagecount != 0) {
            player.damagecount--;
        }
        if (player.bonuscount != 0) {
            player.bonuscount--;
        }
        if (player.powers[pw_invulnerability] != 0) {
            if (player.powers[pw_invulnerability] > 4 * 32 || (player.powers[pw_invulnerability] & 8) != 0) {
                player.fixedcolormap = INVERSECOLORMAP;
            } else {
                player.fixedcolormap = 0;
            }
        } else if (player.powers[pw_infrared] != 0) {
            if (player.powers[pw_infrared] > 4 * 32 || (player.powers[pw_infrared] & 8) != 0) {
                player.fixedcolormap = 1;
            } else {
                player.fixedcolormap = 0;
            }
        } else {
            player.fixedcolormap = 0;
        }
    }
}
