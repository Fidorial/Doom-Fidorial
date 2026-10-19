// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine.ui;

import fr.euphyllia.fidorial.doom.engine.Angles;
import fr.euphyllia.fidorial.doom.engine.Game;
import fr.euphyllia.fidorial.doom.engine.Player;
import fr.euphyllia.fidorial.doom.engine.render.Patch;
import fr.euphyllia.fidorial.doom.engine.render.RenderData;
import fr.euphyllia.fidorial.doom.engine.render.Video;

import static fr.euphyllia.fidorial.doom.engine.Defs.CF_GODMODE;
import static fr.euphyllia.fidorial.doom.engine.Defs.MAXPLAYERS;
import static fr.euphyllia.fidorial.doom.engine.Defs.NUMCARDS;
import static fr.euphyllia.fidorial.doom.engine.Defs.NUMWEAPONS;
import static fr.euphyllia.fidorial.doom.engine.Defs.SCREENHEIGHT;
import static fr.euphyllia.fidorial.doom.engine.Defs.TICRATE;
import static fr.euphyllia.fidorial.doom.engine.Defs.am_noammo;
import static fr.euphyllia.fidorial.doom.engine.Defs.pw_invulnerability;
import static fr.euphyllia.fidorial.doom.engine.Defs.pw_ironfeet;
import static fr.euphyllia.fidorial.doom.engine.Defs.pw_strength;
import static fr.euphyllia.fidorial.doom.engine.Items.WEAPONINFO;
import static fr.euphyllia.fidorial.doom.engine.Tables.ANG180;
import static fr.euphyllia.fidorial.doom.engine.Tables.ANG45;

/**
 * The status bar of one viewer ({@code st_stuff.c}, {@code st_lib.c}).
 *
 * <p>Unlike vanilla it redraws everything each frame instead of patching dirty rectangles.</p>
 */
public final class StatusBar {

    public static final int ST_HEIGHT = 32;
    public static final int ST_Y = SCREENHEIGHT - ST_HEIGHT;

    private static final int STARTREDPALS = 1;
    private static final int STARTBONUSPALS = 9;
    private static final int NUMREDPALS = 8;
    private static final int NUMBONUSPALS = 4;
    private static final int RADIATIONPAL = 13;

    private static final int ST_FX = 143;
    private static final int ST_NUMPAINFACES = 5;
    private static final int ST_NUMSTRAIGHTFACES = 3;
    private static final int ST_NUMTURNFACES = 2;
    private static final int ST_NUMSPECIALFACES = 3;
    private static final int ST_FACESTRIDE = ST_NUMSTRAIGHTFACES + ST_NUMTURNFACES + ST_NUMSPECIALFACES;
    private static final int ST_NUMEXTRAFACES = 2;
    private static final int ST_NUMFACES = ST_FACESTRIDE * ST_NUMPAINFACES + ST_NUMEXTRAFACES;
    private static final int ST_TURNOFFSET = ST_NUMSTRAIGHTFACES;
    private static final int ST_OUCHOFFSET = ST_TURNOFFSET + ST_NUMTURNFACES;
    private static final int ST_EVILGRINOFFSET = ST_OUCHOFFSET + 1;
    private static final int ST_RAMPAGEOFFSET = ST_EVILGRINOFFSET + 1;
    private static final int ST_GODFACE = ST_NUMPAINFACES * ST_FACESTRIDE;
    private static final int ST_DEADFACE = ST_GODFACE + 1;
    private static final int ST_FACESX = 143;
    private static final int ST_FACESY = 168;
    private static final int ST_EVILGRINCOUNT = 2 * TICRATE;
    private static final int ST_STRAIGHTFACECOUNT = TICRATE / 2;
    private static final int ST_TURNCOUNT = TICRATE;
    private static final int ST_RAMPAGEDELAY = 2 * TICRATE;
    private static final int ST_MUCHPAIN = 20;

    private static final int ST_AMMOX = 44;
    private static final int ST_AMMOY = 171;
    private static final int ST_HEALTHX = 90;
    private static final int ST_HEALTHY = 171;
    private static final int ST_ARMSX = 111;
    private static final int ST_ARMSY = 172;
    private static final int ST_ARMSBGX = 104;
    private static final int ST_ARMSBGY = 168;
    private static final int ST_ARMSXSPACE = 12;
    private static final int ST_ARMSYSPACE = 10;
    private static final int ST_FRAGSX = 138;
    private static final int ST_FRAGSY = 171;
    private static final int ST_ARMORX = 221;
    private static final int ST_ARMORY = 171;
    private static final int[] ST_KEYY = {171, 181, 191};
    private static final int ST_KEYX = 239;
    private static final int[] ST_AMMOSY = {173, 179, 191, 185};
    private static final int ST_AMMOSX = 288;
    private static final int ST_MAXAMMOSX = 314;

    private final Game g;
    private final RenderData res;
    private final Video video = new Video();
    private final Patch[] tallnum = new Patch[10];
    private final Patch[] shortnum = new Patch[10];
    private final Patch[] keys = new Patch[NUMCARDS];
    private final Patch[] faces = new Patch[ST_NUMFACES];
    private final Patch[][] arms = new Patch[6][2];
    private final boolean[] oldweaponsowned = new boolean[NUMWEAPONS];
    private final int[] keyboxes = new int[3];
    private Player plyr;
    private final Patch tallpercent;
    private final Patch armsbg;
    private final Patch sbar;
    private Patch faceback;
    private final Patch sttminus;
    private int st_oldhealth = -1;
    private int st_facecount;
    private int st_faceindex;
    private int st_randomnumber;
    private int lastcalc;
    private int oldhealth = -1;
    private int lastattackdown = -1;
    private int priority;
    private int st_fragscount;

    public StatusBar(final Game g) {
        this.g = g;
        this.res = g.res;
        for (int i = 0; i < 10; i++) {
            tallnum[i] = res.patch("STTNUM" + i);
            shortnum[i] = res.patch("STYSNUM" + i);
        }
        tallpercent = res.patch("STTPRCNT");
        for (int i = 0; i < NUMCARDS; i++) {
            keys[i] = res.patch("STKEYS" + i);
        }
        armsbg = res.patch("STARMS");
        for (int i = 0; i < 6; i++) {
            arms[i][0] = res.patch("STGNUM" + (i + 2));
            arms[i][1] = shortnum[i + 2];
        }
        sbar = res.patch("STBAR");
        sttminus = res.patch("STTMINUS");
        int facenum = 0;
        for (int i = 0; i < ST_NUMPAINFACES; i++) {
            for (int j = 0; j < ST_NUMSTRAIGHTFACES; j++) {
                faces[facenum++] = res.patch("STFST" + i + j);
            }
            faces[facenum++] = res.patch("STFTR" + i + "0");
            faces[facenum++] = res.patch("STFTL" + i + "0");
            faces[facenum++] = res.patch("STFOUCH" + i);
            faces[facenum++] = res.patch("STFEVL" + i);
            faces[facenum++] = res.patch("STFKILL" + i);
        }
        faces[facenum++] = res.patch("STFGOD0");
        faces[facenum] = res.patch("STFDEAD0");
    }

    /**
     * {@code ST_Start}: binds the bar to a player.
     */
    public void start(final Player player) {
        plyr = player;
        faceback = res.patch("STFB" + player.index);
        st_faceindex = 0;
        st_oldhealth = -1;
        System.arraycopy(plyr.weaponowned, 0, oldweaponsowned, 0, NUMWEAPONS);
        for (int i = 0; i < 3; i++) {
            keyboxes[i] = -1;
        }
        priority = 0;
        lastattackdown = -1;
    }

    private int calcPainOffset() {
        final int health = Math.min(plyr.health, 100);
        if (health != oldhealth) {
            lastcalc = ST_FACESTRIDE * (((100 - health) * ST_NUMPAINFACES) / 101);
            oldhealth = health;
        }
        return lastcalc;
    }

    private void updateFaceWidget() {
        if (priority < 10) {
            if (plyr.health == 0) {
                priority = 9;
                st_faceindex = ST_DEADFACE;
                st_facecount = 1;
            }
        }
        if (priority < 9) {
            if (plyr.bonuscount != 0) {
                boolean doevilgrin = false;
                for (int i = 0; i < NUMWEAPONS; i++) {
                    if (oldweaponsowned[i] != plyr.weaponowned[i]) {
                        doevilgrin = true;
                        oldweaponsowned[i] = plyr.weaponowned[i];
                    }
                }
                if (doevilgrin) {
                    priority = 8;
                    st_facecount = ST_EVILGRINCOUNT;
                    st_faceindex = calcPainOffset() + ST_EVILGRINOFFSET;
                }
            }
        }
        if (priority < 8) {
            if (plyr.damagecount != 0 && plyr.attacker != null && plyr.attacker != plyr.mo) {
                priority = 7;
                if (plyr.health - st_oldhealth > ST_MUCHPAIN) {
                    st_facecount = ST_TURNCOUNT;
                    st_faceindex = calcPainOffset() + ST_OUCHOFFSET;
                } else {
                    final int badguyangle = Angles.pointToAngle2(plyr.mo.x, plyr.mo.y, plyr.attacker.x, plyr.attacker.y);
                    final int diffang;
                    final boolean turn;
                    if (Integer.compareUnsigned(badguyangle, plyr.mo.angle) > 0) {
                        diffang = badguyangle - plyr.mo.angle;
                        turn = Integer.compareUnsigned(diffang, ANG180) > 0;
                    } else {
                        diffang = plyr.mo.angle - badguyangle;
                        turn = Integer.compareUnsigned(diffang, ANG180) <= 0;
                    }
                    st_facecount = ST_TURNCOUNT;
                    st_faceindex = calcPainOffset();
                    if (Integer.compareUnsigned(diffang, ANG45) < 0) {
                        st_faceindex += ST_RAMPAGEOFFSET;
                    } else if (turn) {
                        st_faceindex += ST_TURNOFFSET;
                    } else {
                        st_faceindex += ST_TURNOFFSET + 1;
                    }
                }
            }
        }
        if (priority < 7) {
            if (plyr.damagecount != 0) {
                if (plyr.health - st_oldhealth > ST_MUCHPAIN) {
                    priority = 7;
                    st_facecount = ST_TURNCOUNT;
                    st_faceindex = calcPainOffset() + ST_OUCHOFFSET;
                } else {
                    priority = 6;
                    st_facecount = ST_TURNCOUNT;
                    st_faceindex = calcPainOffset() + ST_RAMPAGEOFFSET;
                }
            }
        }
        if (priority < 6) {
            if (plyr.attackdown != 0) {
                if (lastattackdown == -1) {
                    lastattackdown = ST_RAMPAGEDELAY;
                } else if (--lastattackdown == 0) {
                    priority = 5;
                    st_faceindex = calcPainOffset() + ST_RAMPAGEOFFSET;
                    st_facecount = 1;
                    lastattackdown = 1;
                }
            } else {
                lastattackdown = -1;
            }
        }
        if (priority < 5) {
            if ((plyr.cheats & CF_GODMODE) != 0 || plyr.powers[pw_invulnerability] != 0) {
                priority = 4;
                st_faceindex = ST_GODFACE;
                st_facecount = 1;
            }
        }
        if (st_facecount == 0) {
            st_faceindex = calcPainOffset() + (st_randomnumber % 3);
            st_facecount = ST_STRAIGHTFACECOUNT;
            priority = 0;
        }
        st_facecount--;
    }

    /**
     * {@code ST_Ticker}.
     */
    public void ticker() {
        if (plyr == null) {
            return;
        }
        st_randomnumber = g.random.M_Random();
        for (int i = 0; i < 3; i++) {
            keyboxes[i] = plyr.cards[i] ? i : -1;
            if (plyr.cards[i + 3]) {
                keyboxes[i] = i + 3;
            }
        }
        updateFaceWidget();
        st_fragscount = 0;
        for (int i = 0; i < MAXPLAYERS; i++) {
            if (i != plyr.index) {
                st_fragscount += plyr.frags[i];
            } else {
                st_fragscount -= plyr.frags[i];
            }
        }
        st_oldhealth = plyr.health;
    }

    /**
     * {@return the palette to show, from damage, bonus and radiation suit ({@code ST_doPaletteStuff})}
     */
    public int palette() {
        if (plyr == null) {
            return 0;
        }
        int cnt = plyr.damagecount;
        if (plyr.powers[pw_strength] != 0) {
            final int bzc = 12 - (plyr.powers[pw_strength] >> 6);
            if (bzc > cnt) {
                cnt = bzc;
            }
        }
        int palette;
        if (cnt != 0) {
            palette = (cnt + 7) >> 3;
            if (palette >= NUMREDPALS) {
                palette = NUMREDPALS - 1;
            }
            palette += STARTREDPALS;
        } else if (plyr.bonuscount != 0) {
            palette = (plyr.bonuscount + 7) >> 3;
            if (palette >= NUMBONUSPALS) {
                palette = NUMBONUSPALS - 1;
            }
            palette += STARTBONUSPALS;
        } else if (plyr.powers[pw_ironfeet] > 4 * 32 || (plyr.powers[pw_ironfeet] & 8) != 0) {
            palette = RADIATIONPAL;
        } else {
            palette = 0;
        }
        return palette;
    }

    private void drawNum(final byte[] screen, final int x0, final int y, int num, final int numdigits, final Patch[] font) {
        final int w = font[0].width;
        int x = x0;
        final boolean neg = num < 0;
        if (neg) {
            if (numdigits == 2 && num < -9) {
                num = -9;
            } else if (numdigits == 3 && num < -99) {
                num = -99;
            }
            num = -num;
        }
        if (num == 1994) {
            return;
        }
        if (num == 0) {
            video.drawPatch(screen, x - w, y, font[0]);
        }
        int digits = numdigits;
        while (num != 0 && digits-- > 0) {
            x -= w;
            video.drawPatch(screen, x, y, font[num % 10]);
            num /= 10;
        }
        if (neg) {
            video.drawPatch(screen, x - 8, y, sttminus);
        }
    }

    /**
     * Draws the bar at the bottom of the screen ({@code ST_Drawer} with a full refresh).
     *
     * @param screen the 320x200 screen
     */
    public void draw(final byte[] screen) {
        if (plyr == null) {
            return;
        }
        video.drawPatch(screen, 0, ST_Y, sbar);
        if (g.netgame) {
            video.drawPatch(screen, ST_FX, ST_Y, faceback);
        }
        final int ammotype = WEAPONINFO[plyr.readyweapon].ammo();
        drawNum(screen, ST_AMMOX, ST_AMMOY, ammotype == am_noammo ? 1994 : plyr.ammo[ammotype], 3, tallnum);
        for (int i = 0; i < 4; i++) {
            drawNum(screen, ST_AMMOSX, ST_AMMOSY[i], plyr.ammo[i], 3, shortnum);
            drawNum(screen, ST_MAXAMMOSX, ST_AMMOSY[i], plyr.maxammo[i], 3, shortnum);
        }
        video.drawPatch(screen, ST_HEALTHX, ST_HEALTHY, tallpercent);
        drawNum(screen, ST_HEALTHX, ST_HEALTHY, plyr.health, 3, tallnum);
        video.drawPatch(screen, ST_ARMORX, ST_ARMORY, tallpercent);
        drawNum(screen, ST_ARMORX, ST_ARMORY, plyr.armorpoints, 3, tallnum);
        if (g.deathmatch == 0) {
            video.drawPatch(screen, ST_ARMSBGX, ST_ARMSBGY, armsbg);
            for (int i = 0; i < 6; i++) {
                video.drawPatch(screen, ST_ARMSX + (i % 3) * ST_ARMSXSPACE, ST_ARMSY + (i / 3) * ST_ARMSYSPACE,
                        arms[i][plyr.weaponowned[i + 1] ? 1 : 0]);
            }
        } else {
            drawNum(screen, ST_FRAGSX, ST_FRAGSY, st_fragscount, 2, tallnum);
        }
        if (st_faceindex >= 0 && st_faceindex < faces.length) {
            video.drawPatch(screen, ST_FACESX, ST_FACESY, faces[st_faceindex]);
        }
        for (int i = 0; i < 3; i++) {
            if (keyboxes[i] != -1) {
                video.drawPatch(screen, ST_KEYX, ST_KEYY[i], keys[keyboxes[i]]);
            }
        }
    }
}
