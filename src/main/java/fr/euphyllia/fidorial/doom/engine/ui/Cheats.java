// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine.ui;

import fr.euphyllia.fidorial.doom.engine.Game;
import fr.euphyllia.fidorial.doom.engine.Music;
import fr.euphyllia.fidorial.doom.engine.Player;
import fr.euphyllia.fidorial.doom.engine.Strings;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

import static fr.euphyllia.fidorial.doom.engine.Defs.CF_GODMODE;
import static fr.euphyllia.fidorial.doom.engine.Defs.CF_NOCLIP;
import static fr.euphyllia.fidorial.doom.engine.Defs.NUMAMMO;
import static fr.euphyllia.fidorial.doom.engine.Defs.NUMCARDS;
import static fr.euphyllia.fidorial.doom.engine.Defs.NUMWEAPONS;
import static fr.euphyllia.fidorial.doom.engine.Defs.commercial;
import static fr.euphyllia.fidorial.doom.engine.Defs.pw_invulnerability;
import static fr.euphyllia.fidorial.doom.engine.Defs.pw_strength;
import static fr.euphyllia.fidorial.doom.engine.Defs.registered;
import static fr.euphyllia.fidorial.doom.engine.Defs.retail;
import static fr.euphyllia.fidorial.doom.engine.Defs.shareware;
import static fr.euphyllia.fidorial.doom.engine.Defs.wp_chainsaw;

/**
 * The cheat codes of the status bar ({@code ST_Responder}, {@code m_cheat.c}).
 *
 * <p>Codes are typed as whole words, for example in the Minecraft chat, instead of being caught
 * from key presses.</p>
 */
public final class Cheats {

    private static final String[] BEHOLD = {"v", "s", "i", "r", "a", "l"};

    private final Game g;

    public Cheats(final Game g) {
        this.g = g;
    }

    /**
     * Applies a cheat code.
     *
     * @param plyr     who typed it
     * @param code     the code, such as {@code iddqd} or {@code idclev12}
     * @param automap  the automap of the player for {@code iddt}, or null
     * @param allowNet whether cheats other than {@code idclev} are allowed in a network game
     * @return whether the code was recognized
     */
    public boolean apply(final Player plyr, final String code, final @Nullable AutoMap automap, final boolean allowNet) {
        final String c = code.trim().toLowerCase(Locale.ROOT);
        if (!g.netgame || allowNet) {
            if (c.equals("iddqd")) {
                plyr.cheats ^= CF_GODMODE;
                if ((plyr.cheats & CF_GODMODE) != 0) {
                    if (plyr.mo != null) {
                        plyr.mo.health = 100;
                    }
                    plyr.health = 100;
                    plyr.message = Strings.STSTR_DQDON;
                } else {
                    plyr.message = Strings.STSTR_DQDOFF;
                }
                return true;
            }
            if (c.equals("idfa") || c.equals("idkfa")) {
                plyr.armorpoints = 200;
                plyr.armortype = 2;
                for (int i = 0; i < NUMWEAPONS; i++) {
                    plyr.weaponowned[i] = true;
                }
                System.arraycopy(plyr.maxammo, 0, plyr.ammo, 0, NUMAMMO);
                if (c.equals("idkfa")) {
                    for (int i = 0; i < NUMCARDS; i++) {
                        plyr.cards[i] = true;
                    }
                    plyr.message = Strings.STSTR_KFAADDED;
                } else {
                    plyr.message = Strings.STSTR_FAADDED;
                }
                return true;
            }
            if (c.startsWith("idmus") && c.length() == 7) {
                final int d0 = c.charAt(5) - '0';
                final int d1 = c.charAt(6) - '0';
                if (d0 < 0 || d0 > 9 || d1 < 0 || d1 > 9) {
                    return false;
                }
                plyr.message = Strings.STSTR_MUS;
                if (g.gamemode == commercial) {
                    final int n = d0 * 10 + d1;
                    if (n < 1 || n > 35) {
                        plyr.message = Strings.STSTR_NOMUS;
                    } else {
                        g.sound.S_ChangeMusic(Music.mus_runnin + n - 1, true);
                    }
                } else {
                    final int n = (d0 - 1) * 9 + (d1 - 1);
                    if (d0 < 1 || d1 < 1 || n > 31) {
                        plyr.message = Strings.STSTR_NOMUS;
                    } else {
                        g.sound.S_ChangeMusic(Music.mus_e1m1 + n, true);
                    }
                }
                return true;
            }
            if (c.equals("idclip") || c.equals("idspispopd")) {
                plyr.cheats ^= CF_NOCLIP;
                plyr.message = (plyr.cheats & CF_NOCLIP) != 0 ? Strings.STSTR_NCON : Strings.STSTR_NCOFF;
                return true;
            }
            if (c.startsWith("idbehold")) {
                final String rest = c.substring("idbehold".length());
                if (rest.isEmpty()) {
                    plyr.message = Strings.STSTR_BEHOLD;
                    return true;
                }
                for (int i = 0; i < BEHOLD.length; i++) {
                    if (rest.equals(BEHOLD[i])) {
                        if (plyr.powers[i] == 0) {
                            g.inter.P_GivePower(plyr, i);
                        } else if (i != pw_strength) {
                            plyr.powers[i] = 1;
                        } else {
                            plyr.powers[i] = 0;
                        }
                        plyr.message = Strings.STSTR_BEHOLDX;
                        return true;
                    }
                }
                return false;
            }
            if (c.equals("idchoppers")) {
                plyr.weaponowned[wp_chainsaw] = true;
                plyr.powers[pw_invulnerability] = 1;
                plyr.message = Strings.STSTR_CHOPPERS;
                return true;
            }
            if (c.equals("idmypos")) {
                if (plyr.mo != null) {
                    plyr.message = String.format(Locale.ROOT, "ang=0x%x;x,y=(0x%x,0x%x)", plyr.mo.angle, plyr.mo.x, plyr.mo.y);
                }
                return true;
            }
            if (c.equals("iddt") && automap != null && g.deathmatch == 0) {
                automap.cycleCheat();
                return true;
            }
        }
        if (c.startsWith("idclev") && c.length() == 8) {
            final int d0 = c.charAt(6) - '0';
            final int d1 = c.charAt(7) - '0';
            if (d0 < 0 || d0 > 9 || d1 < 0 || d1 > 9) {
                return false;
            }
            final int epsd;
            final int map;
            if (g.gamemode == commercial) {
                // linuxdoom left the episode at 0 here, which made the cheat do nothing
                epsd = 1;
                map = d0 * 10 + d1;
            } else {
                epsd = d0;
                map = d1;
            }
            if (epsd < 1 || map < 1) {
                return false;
            }
            if (g.gamemode == retail && (epsd > 4 || map > 9)) {
                return false;
            }
            if (g.gamemode == registered && (epsd > 3 || map > 9)) {
                return false;
            }
            if (g.gamemode == shareware && (epsd > 1 || map > 9)) {
                return false;
            }
            if (g.gamemode == commercial && (epsd > 1 || map > 34)) {
                return false;
            }
            if (!g.mapExists(epsd, map)) {
                return false;
            }
            plyr.message = Strings.STSTR_CLEV;
            g.G_InitNew(g.gameskill, epsd, map);
            return true;
        }
        return false;
    }
}
