// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine.ui;

import fr.euphyllia.fidorial.doom.engine.Game;
import fr.euphyllia.fidorial.doom.engine.Player;
import fr.euphyllia.fidorial.doom.engine.WbStart;
import fr.euphyllia.fidorial.doom.engine.render.Patch;
import fr.euphyllia.fidorial.doom.engine.render.RenderData;
import fr.euphyllia.fidorial.doom.engine.render.Video;

import static fr.euphyllia.fidorial.doom.engine.Defs.BT_ATTACK;
import static fr.euphyllia.fidorial.doom.engine.Defs.BT_USE;
import static fr.euphyllia.fidorial.doom.engine.Defs.MAXPLAYERS;
import static fr.euphyllia.fidorial.doom.engine.Defs.SCREENHEIGHT;
import static fr.euphyllia.fidorial.doom.engine.Defs.SCREENWIDTH;
import static fr.euphyllia.fidorial.doom.engine.Defs.TICRATE;
import static fr.euphyllia.fidorial.doom.engine.Defs.commercial;
import static fr.euphyllia.fidorial.doom.engine.Defs.retail;
import static fr.euphyllia.fidorial.doom.engine.Music.mus_dm2int;
import static fr.euphyllia.fidorial.doom.engine.Music.mus_inter;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_barexp;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_pistol;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_pldeth;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_sgcock;
import static fr.euphyllia.fidorial.doom.engine.Sfx.sfx_slop;

/**
 * The intermission screen between levels ({@code wi_stuff.c}).
 *
 * <p>The counting state machine belongs to the game; drawing takes the viewing player so every
 * viewer sees its own star.</p>
 */
public final class Intermission {

    private static final int WI_TITLEY = 2;
    private static final int WI_SPACINGY = 33;
    private static final int SP_STATSX = 50;
    private static final int SP_STATSY = 50;
    private static final int SP_TIMEX = 16;
    private static final int SP_TIMEY = SCREENHEIGHT - 32;
    private static final int NG_STATSY = 50;
    private static final int NG_SPACINGX = 64;
    private static final int DM_MATRIXX = 42;
    private static final int DM_MATRIXY = 68;
    private static final int DM_SPACINGX = 40;
    private static final int DM_TOTALSX = 269;
    private static final int DM_KILLERSX = 10;
    private static final int DM_KILLERSY = 100;
    private static final int DM_VICTIMSX = 5;
    private static final int DM_VICTIMSY = 50;
    private static final int SHOWNEXTLOCDELAY = 4;

    private static final int ANIM_ALWAYS = 0;
    private static final int ANIM_LEVEL = 2;

    private static final int StatCount = 0;
    private static final int ShowNextLoc = 1;
    private static final int NoState = -1;

    private static final int[][][] LNODES = {
            {{185, 164}, {148, 143}, {69, 122}, {209, 102}, {116, 89}, {166, 55}, {71, 56}, {135, 29}, {71, 24}},
            {{254, 25}, {97, 50}, {188, 64}, {128, 78}, {214, 92}, {133, 130}, {208, 136}, {148, 140}, {235, 158}},
            {{156, 168}, {48, 154}, {174, 95}, {265, 75}, {130, 48}, {279, 23}, {198, 48}, {140, 25}, {281, 136}}
    };

    /**
     * {@code anim_t}: type, period, frames, x, y, data1.
     */
    private static final int[][][] ANIMINFO = {
            {
                    {ANIM_ALWAYS, TICRATE / 3, 3, 224, 104, 0}, {ANIM_ALWAYS, TICRATE / 3, 3, 184, 160, 0},
                    {ANIM_ALWAYS, TICRATE / 3, 3, 112, 136, 0}, {ANIM_ALWAYS, TICRATE / 3, 3, 72, 112, 0},
                    {ANIM_ALWAYS, TICRATE / 3, 3, 88, 96, 0}, {ANIM_ALWAYS, TICRATE / 3, 3, 64, 48, 0},
                    {ANIM_ALWAYS, TICRATE / 3, 3, 192, 40, 0}, {ANIM_ALWAYS, TICRATE / 3, 3, 136, 16, 0},
                    {ANIM_ALWAYS, TICRATE / 3, 3, 80, 16, 0}, {ANIM_ALWAYS, TICRATE / 3, 3, 64, 24, 0}
            },
            {
                    {ANIM_LEVEL, TICRATE / 3, 1, 128, 136, 1}, {ANIM_LEVEL, TICRATE / 3, 1, 128, 136, 2},
                    {ANIM_LEVEL, TICRATE / 3, 1, 128, 136, 3}, {ANIM_LEVEL, TICRATE / 3, 1, 128, 136, 4},
                    {ANIM_LEVEL, TICRATE / 3, 1, 128, 136, 5}, {ANIM_LEVEL, TICRATE / 3, 1, 128, 136, 6},
                    {ANIM_LEVEL, TICRATE / 3, 1, 128, 136, 7}, {ANIM_LEVEL, TICRATE / 3, 3, 192, 144, 8},
                    {ANIM_LEVEL, TICRATE / 3, 1, 128, 136, 8}
            },
            {
                    {ANIM_ALWAYS, TICRATE / 3, 3, 104, 168, 0}, {ANIM_ALWAYS, TICRATE / 3, 3, 40, 136, 0},
                    {ANIM_ALWAYS, TICRATE / 3, 3, 160, 96, 0}, {ANIM_ALWAYS, TICRATE / 3, 3, 104, 80, 0},
                    {ANIM_ALWAYS, TICRATE / 3, 3, 120, 32, 0}, {ANIM_ALWAYS, TICRATE / 4, 3, 40, 0, 0}
            }
    };

    private final Game g;
    private final RenderData res;
    private final Video video = new Video();
    private final int[] cnt_kills = new int[MAXPLAYERS];
    private final int[] cnt_items = new int[MAXPLAYERS];
    private final int[] cnt_secret = new int[MAXPLAYERS];
    private final int[] cnt_frags = new int[MAXPLAYERS];
    private final int[][] dm_frags = new int[MAXPLAYERS][MAXPLAYERS];
    private final int[] dm_totals = new int[MAXPLAYERS];
    private int acceleratestage;
    private int state;
    private WbStart wbs;
    private WbStart.WbPlayer[] plrs;
    private int cnt;
    private int bcnt;
    private int cnt_time;
    private int cnt_par;
    private int cnt_pause;
    private int dofrags;
    private int sp_state;
    private int ng_state;
    private int dm_state;
    private boolean snl_pointeron;
    private int me;

    private int[] animCtr = new int[0];
    private int[] animNexttic = new int[0];

    public Intermission(final Game g) {
        this.g = g;
        this.res = g.res;
    }

    private Patch p(final String name) {
        return res.patch(name);
    }

    private Patch num(final int i) {
        return p("WINUM" + i);
    }

    private Patch lname(final int map) {
        if (g.gamemode == commercial) {
            return p(String.format("CWILV%02d", map));
        }
        return p("WILV" + wbs.epsd + map);
    }

    private Patch animPatch(final int epsd, final int j, final int i) {
        if (epsd == 1 && j == 8) {
            return p(String.format("WIA%d%02d%02d", 1, 4, i));
        }
        return p(String.format("WIA%d%02d%02d", epsd, j, i));
    }

    private void drawBackground(final byte[] screen) {
        String name;
        if (g.gamemode == commercial) {
            name = "INTERPIC";
        } else {
            name = "WIMAP" + wbs.epsd;
        }
        if (g.gamemode == retail && wbs.epsd == 3) {
            name = "INTERPIC";
        }
        if (!res.hasLump(name)) {
            name = "INTERPIC";
        }
        video.drawPatch(screen, 0, 0, p(name));
    }

    private void WI_drawLF(final byte[] screen) {
        int y = WI_TITLEY;
        final Patch last = lname(wbs.last);
        video.drawPatch(screen, (SCREENWIDTH - last.width) / 2, y, last);
        y += (5 * last.height) / 4;
        final Patch finished = p("WIF");
        video.drawPatch(screen, (SCREENWIDTH - finished.width) / 2, y, finished);
    }

    private void WI_drawEL(final byte[] screen) {
        int y = WI_TITLEY;
        final Patch entering = p("WIENTER");
        video.drawPatch(screen, (SCREENWIDTH - entering.width) / 2, y, entering);
        final Patch next = lname(wbs.next);
        y += (5 * next.height) / 4;
        video.drawPatch(screen, (SCREENWIDTH - next.width) / 2, y, next);
    }

    private void WI_drawOnLnode(final byte[] screen, final int n, final Patch[] c) {
        if (wbs.epsd < 0 || wbs.epsd >= LNODES.length || n < 0 || n >= 9) {
            return;
        }
        final int[] node = LNODES[wbs.epsd][n];
        int i = 0;
        boolean fits = false;
        do {
            final int left = node[0] - c[i].leftoffset;
            final int top = node[1] - c[i].topoffset;
            final int right = left + c[i].width;
            final int bottom = top + c[i].height;
            if (left >= 0 && right < SCREENWIDTH && top >= 0 && bottom < SCREENHEIGHT) {
                fits = true;
            } else {
                i++;
            }
        } while (!fits && i != c.length);
        if (fits) {
            video.drawPatch(screen, node[0], node[1], c[i]);
        }
    }

    private void WI_initAnimatedBack() {
        if (g.gamemode == commercial || wbs.epsd > 2) {
            animCtr = new int[0];
            return;
        }
        final int[][] info = ANIMINFO[wbs.epsd];
        animCtr = new int[info.length];
        animNexttic = new int[info.length];
        for (int i = 0; i < info.length; i++) {
            animCtr[i] = -1;
            if (info[i][0] == ANIM_ALWAYS) {
                animNexttic[i] = bcnt + 1 + (g.random.M_Random() % info[i][1]);
            } else if (info[i][0] == ANIM_LEVEL) {
                animNexttic[i] = bcnt + 1;
            }
        }
    }

    private void WI_updateAnimatedBack() {
        if (g.gamemode == commercial || wbs.epsd > 2) {
            return;
        }
        final int[][] info = ANIMINFO[wbs.epsd];
        for (int i = 0; i < Math.min(info.length, animCtr.length); i++) {
            if (bcnt == animNexttic[i]) {
                if (info[i][0] == ANIM_ALWAYS) {
                    if (++animCtr[i] >= info[i][2]) {
                        animCtr[i] = 0;
                    }
                    animNexttic[i] = bcnt + info[i][1];
                } else if (info[i][0] == ANIM_LEVEL) {
                    if (!(state == StatCount && i == 7) && wbs.next == info[i][5]) {
                        animCtr[i]++;
                        if (animCtr[i] == info[i][2]) {
                            animCtr[i]--;
                        }
                        animNexttic[i] = bcnt + info[i][1];
                    }
                }
            }
        }
    }

    private void WI_drawAnimatedBack(final byte[] screen) {
        if (g.gamemode == commercial || wbs.epsd > 2) {
            return;
        }
        final int[][] info = ANIMINFO[wbs.epsd];
        for (int i = 0; i < Math.min(info.length, animCtr.length); i++) {
            if (animCtr[i] >= 0) {
                video.drawPatch(screen, info[i][3], info[i][4], animPatch(wbs.epsd, i, animCtr[i]));
            }
        }
    }

    private int WI_drawNum(final byte[] screen, int x, final int y, int n, int digits) {
        final int fontwidth = num(0).width;
        if (digits < 0) {
            if (n == 0) {
                digits = 1;
            } else {
                digits = 0;
                int temp = n;
                while (temp != 0) {
                    temp /= 10;
                    digits++;
                }
            }
        }
        final boolean neg = n < 0;
        if (neg) {
            n = -n;
        }
        if (n == 1994) {
            return 0;
        }
        while (digits-- > 0) {
            x -= fontwidth;
            video.drawPatch(screen, x, y, num(n % 10));
            n /= 10;
        }
        if (neg) {
            video.drawPatch(screen, x -= 8, y, p("WIMINUS"));
        }
        return x;
    }

    private void WI_drawPercent(final byte[] screen, final int x, final int y, final int pc) {
        if (pc < 0) {
            return;
        }
        video.drawPatch(screen, x, y, p("WIPCNT"));
        WI_drawNum(screen, x, y, pc, -1);
    }

    private void WI_drawTime(final byte[] screen, int x, final int y, final int t) {
        if (t < 0) {
            return;
        }
        if (t <= 61 * 59) {
            int div = 1;
            final Patch colon = p("WICOLON");
            do {
                final int n = (t / div) % 60;
                x = WI_drawNum(screen, x, y, n, 2) - colon.width;
                div *= 60;
                if (div == 60 || t / div != 0) {
                    video.drawPatch(screen, x, y, colon);
                }
            } while (t / div != 0);
        } else {
            final Patch sucks = p("WISUCKS");
            video.drawPatch(screen, x - sucks.width, y, sucks);
        }
    }

    private void WI_initNoState() {
        state = NoState;
        acceleratestage = 0;
        cnt = 10;
    }

    private void WI_updateNoState() {
        WI_updateAnimatedBack();
        if (--cnt == 0) {
            g.G_WorldDone();
        }
    }

    private void WI_initShowNextLoc() {
        state = ShowNextLoc;
        acceleratestage = 0;
        cnt = SHOWNEXTLOCDELAY * TICRATE;
        WI_initAnimatedBack();
    }

    private void WI_updateShowNextLoc() {
        WI_updateAnimatedBack();
        if (--cnt == 0 || acceleratestage != 0) {
            WI_initNoState();
        } else {
            snl_pointeron = (cnt & 31) < 20;
        }
    }

    private void WI_drawShowNextLoc(final byte[] screen) {
        drawBackground(screen);
        WI_drawAnimatedBack(screen);
        if (g.gamemode != commercial) {
            if (wbs.epsd > 2) {
                WI_drawEL(screen);
                return;
            }
            final int last = (wbs.last == 8) ? wbs.next - 1 : wbs.last;
            final Patch[] splat = {p("WISPLAT")};
            for (int i = 0; i <= last; i++) {
                WI_drawOnLnode(screen, i, splat);
            }
            if (wbs.didsecret) {
                WI_drawOnLnode(screen, 8, splat);
            }
            if (snl_pointeron) {
                WI_drawOnLnode(screen, wbs.next, new Patch[]{p("WIURH0"), p("WIURH1")});
            }
        }
        if (g.gamemode != commercial || wbs.next != 30) {
            WI_drawEL(screen);
        }
    }

    private int WI_fragSum(final int playernum) {
        int frags = 0;
        for (int i = 0; i < MAXPLAYERS; i++) {
            if (g.playeringame[i] && i != playernum) {
                frags += plrs[playernum].frags[i];
            }
        }
        frags -= plrs[playernum].frags[playernum];
        return frags;
    }

    private void WI_initDeathmatchStats() {
        state = StatCount;
        acceleratestage = 0;
        dm_state = 1;
        cnt_pause = TICRATE;
        for (int i = 0; i < MAXPLAYERS; i++) {
            if (g.playeringame[i]) {
                for (int j = 0; j < MAXPLAYERS; j++) {
                    if (g.playeringame[j]) {
                        dm_frags[i][j] = 0;
                    }
                }
                dm_totals[i] = 0;
            }
        }
        WI_initAnimatedBack();
    }

    private void WI_updateDeathmatchStats() {
        WI_updateAnimatedBack();
        if (acceleratestage != 0 && dm_state != 4) {
            acceleratestage = 0;
            for (int i = 0; i < MAXPLAYERS; i++) {
                if (g.playeringame[i]) {
                    for (int j = 0; j < MAXPLAYERS; j++) {
                        if (g.playeringame[j]) {
                            dm_frags[i][j] = plrs[i].frags[j];
                        }
                    }
                    dm_totals[i] = WI_fragSum(i);
                }
            }
            g.sound.S_StartSound(null, sfx_barexp);
            dm_state = 4;
        }
        if (dm_state == 2) {
            if ((bcnt & 3) == 0) {
                g.sound.S_StartSound(null, sfx_pistol);
            }
            boolean stillticking = false;
            for (int i = 0; i < MAXPLAYERS; i++) {
                if (g.playeringame[i]) {
                    for (int j = 0; j < MAXPLAYERS; j++) {
                        if (g.playeringame[j] && dm_frags[i][j] != plrs[i].frags[j]) {
                            if (plrs[i].frags[j] < 0) {
                                dm_frags[i][j]--;
                            } else {
                                dm_frags[i][j]++;
                            }
                            dm_frags[i][j] = Math.clamp(dm_frags[i][j], -99, 99);
                            stillticking = true;
                        }
                    }
                    dm_totals[i] = Math.clamp(WI_fragSum(i), -99, 99);
                }
            }
            if (!stillticking) {
                g.sound.S_StartSound(null, sfx_barexp);
                dm_state++;
            }
        } else if (dm_state == 4) {
            if (acceleratestage != 0) {
                g.sound.S_StartSound(null, sfx_slop);
                if (g.gamemode == commercial) {
                    WI_initNoState();
                } else {
                    WI_initShowNextLoc();
                }
            }
        } else if ((dm_state & 1) != 0) {
            if (--cnt_pause == 0) {
                dm_state++;
                cnt_pause = TICRATE;
            }
        }
    }

    private void WI_drawDeathmatchStats(final byte[] screen) {
        drawBackground(screen);
        WI_drawAnimatedBack(screen);
        WI_drawLF(screen);
        final Patch total = p("WIMSTT");
        video.drawPatch(screen, DM_TOTALSX - total.width / 2, DM_MATRIXY - WI_SPACINGY + 10, total);
        video.drawPatch(screen, DM_KILLERSX, DM_KILLERSY, p("WIKILRS"));
        video.drawPatch(screen, DM_VICTIMSX, DM_VICTIMSY, p("WIVCTMS"));
        int x = DM_MATRIXX + DM_SPACINGX;
        int y = DM_MATRIXY;
        for (int i = 0; i < MAXPLAYERS; i++) {
            if (g.playeringame[i]) {
                final Patch pi = p("STPB" + i);
                video.drawPatch(screen, x - pi.width / 2, DM_MATRIXY - WI_SPACINGY, pi);
                video.drawPatch(screen, DM_MATRIXX - pi.width / 2, y, pi);
                if (i == me) {
                    video.drawPatch(screen, x - pi.width / 2, DM_MATRIXY - WI_SPACINGY, p("STFDEAD0"));
                    video.drawPatch(screen, DM_MATRIXX - pi.width / 2, y, p("STFST01"));
                }
            }
            x += DM_SPACINGX;
            y += WI_SPACINGY;
        }
        y = DM_MATRIXY + 10;
        final int w = num(0).width;
        for (int i = 0; i < MAXPLAYERS; i++) {
            x = DM_MATRIXX + DM_SPACINGX;
            if (g.playeringame[i]) {
                for (int j = 0; j < MAXPLAYERS; j++) {
                    if (g.playeringame[j]) {
                        WI_drawNum(screen, x + w, y, dm_frags[i][j], 2);
                    }
                    x += DM_SPACINGX;
                }
                WI_drawNum(screen, DM_TOTALSX + w, y, dm_totals[i], 2);
            }
            y += WI_SPACINGY;
        }
    }

    private void WI_initNetgameStats() {
        state = StatCount;
        acceleratestage = 0;
        ng_state = 1;
        cnt_pause = TICRATE;
        dofrags = 0;
        for (int i = 0; i < MAXPLAYERS; i++) {
            if (!g.playeringame[i]) {
                continue;
            }
            cnt_kills[i] = cnt_items[i] = cnt_secret[i] = cnt_frags[i] = 0;
            dofrags += WI_fragSum(i);
        }
        dofrags = dofrags != 0 ? 1 : 0;
        WI_initAnimatedBack();
    }

    private void WI_updateNetgameStats() {
        WI_updateAnimatedBack();
        if (acceleratestage != 0 && ng_state != 10) {
            acceleratestage = 0;
            for (int i = 0; i < MAXPLAYERS; i++) {
                if (!g.playeringame[i]) {
                    continue;
                }
                cnt_kills[i] = (plrs[i].skills * 100) / wbs.maxkills;
                cnt_items[i] = (plrs[i].sitems * 100) / wbs.maxitems;
                cnt_secret[i] = (plrs[i].ssecret * 100) / wbs.maxsecret;
                if (dofrags != 0) {
                    cnt_frags[i] = WI_fragSum(i);
                }
            }
            g.sound.S_StartSound(null, sfx_barexp);
            ng_state = 10;
        }
        if (ng_state == 2) {
            if ((bcnt & 3) == 0) {
                g.sound.S_StartSound(null, sfx_pistol);
            }
            boolean stillticking = false;
            for (int i = 0; i < MAXPLAYERS; i++) {
                if (!g.playeringame[i]) {
                    continue;
                }
                cnt_kills[i] += 2;
                if (cnt_kills[i] >= (plrs[i].skills * 100) / wbs.maxkills) {
                    cnt_kills[i] = (plrs[i].skills * 100) / wbs.maxkills;
                } else {
                    stillticking = true;
                }
            }
            if (!stillticking) {
                g.sound.S_StartSound(null, sfx_barexp);
                ng_state++;
            }
        } else if (ng_state == 4) {
            if ((bcnt & 3) == 0) {
                g.sound.S_StartSound(null, sfx_pistol);
            }
            boolean stillticking = false;
            for (int i = 0; i < MAXPLAYERS; i++) {
                if (!g.playeringame[i]) {
                    continue;
                }
                cnt_items[i] += 2;
                if (cnt_items[i] >= (plrs[i].sitems * 100) / wbs.maxitems) {
                    cnt_items[i] = (plrs[i].sitems * 100) / wbs.maxitems;
                } else {
                    stillticking = true;
                }
            }
            if (!stillticking) {
                g.sound.S_StartSound(null, sfx_barexp);
                ng_state++;
            }
        } else if (ng_state == 6) {
            if ((bcnt & 3) == 0) {
                g.sound.S_StartSound(null, sfx_pistol);
            }
            boolean stillticking = false;
            for (int i = 0; i < MAXPLAYERS; i++) {
                if (!g.playeringame[i]) {
                    continue;
                }
                cnt_secret[i] += 2;
                if (cnt_secret[i] >= (plrs[i].ssecret * 100) / wbs.maxsecret) {
                    cnt_secret[i] = (plrs[i].ssecret * 100) / wbs.maxsecret;
                } else {
                    stillticking = true;
                }
            }
            if (!stillticking) {
                g.sound.S_StartSound(null, sfx_barexp);
                ng_state += 1 + 2 * (dofrags == 0 ? 1 : 0);
            }
        } else if (ng_state == 8) {
            if ((bcnt & 3) == 0) {
                g.sound.S_StartSound(null, sfx_pistol);
            }
            boolean stillticking = false;
            for (int i = 0; i < MAXPLAYERS; i++) {
                if (!g.playeringame[i]) {
                    continue;
                }
                cnt_frags[i] += 1;
                final int fsum = WI_fragSum(i);
                if (cnt_frags[i] >= fsum) {
                    cnt_frags[i] = fsum;
                } else {
                    stillticking = true;
                }
            }
            if (!stillticking) {
                g.sound.S_StartSound(null, sfx_pldeth);
                ng_state++;
            }
        } else if (ng_state == 10) {
            if (acceleratestage != 0) {
                g.sound.S_StartSound(null, sfx_sgcock);
                if (g.gamemode == commercial) {
                    WI_initNoState();
                } else {
                    WI_initShowNextLoc();
                }
            }
        } else if ((ng_state & 1) != 0) {
            if (--cnt_pause == 0) {
                ng_state++;
                cnt_pause = TICRATE;
            }
        }
    }

    private void WI_drawNetgameStats(final byte[] screen) {
        final Patch percent = p("WIPCNT");
        final Patch kills = p("WIOSTK");
        final Patch items = p("WIOSTI");
        final Patch secret = p("WIOSTS");
        final Patch frags = p("WIFRGS");
        final Patch star = p("STFST01");
        final int pwidth = percent.width;
        final int ngStatsX = 32 + star.width / 2 + 32 * (dofrags == 0 ? 1 : 0);
        drawBackground(screen);
        WI_drawAnimatedBack(screen);
        WI_drawLF(screen);
        video.drawPatch(screen, ngStatsX + NG_SPACINGX - kills.width, NG_STATSY, kills);
        video.drawPatch(screen, ngStatsX + 2 * NG_SPACINGX - items.width, NG_STATSY, items);
        video.drawPatch(screen, ngStatsX + 3 * NG_SPACINGX - secret.width, NG_STATSY, secret);
        if (dofrags != 0) {
            video.drawPatch(screen, ngStatsX + 4 * NG_SPACINGX - frags.width, NG_STATSY, frags);
        }
        int y = NG_STATSY + kills.height;
        for (int i = 0; i < MAXPLAYERS; i++) {
            if (!g.playeringame[i]) {
                continue;
            }
            int x = ngStatsX;
            final Patch pi = p("STPB" + i);
            video.drawPatch(screen, x - pi.width, y, pi);
            if (i == me) {
                video.drawPatch(screen, x - pi.width, y, star);
            }
            x += NG_SPACINGX;
            WI_drawPercent(screen, x - pwidth, y + 10, cnt_kills[i]);
            x += NG_SPACINGX;
            WI_drawPercent(screen, x - pwidth, y + 10, cnt_items[i]);
            x += NG_SPACINGX;
            WI_drawPercent(screen, x - pwidth, y + 10, cnt_secret[i]);
            x += NG_SPACINGX;
            if (dofrags != 0) {
                WI_drawNum(screen, x, y + 10, cnt_frags[i], -1);
            }
            y += WI_SPACINGY;
        }
    }

    private void WI_initStats() {
        state = StatCount;
        acceleratestage = 0;
        sp_state = 1;
        cnt_kills[0] = cnt_items[0] = cnt_secret[0] = -1;
        cnt_time = cnt_par = -1;
        cnt_pause = TICRATE;
        WI_initAnimatedBack();
    }

    private void WI_updateStats() {
        final int sp = wbs.pnum;
        WI_updateAnimatedBack();
        if (acceleratestage != 0 && sp_state != 10) {
            acceleratestage = 0;
            cnt_kills[0] = (plrs[sp].skills * 100) / wbs.maxkills;
            cnt_items[0] = (plrs[sp].sitems * 100) / wbs.maxitems;
            cnt_secret[0] = (plrs[sp].ssecret * 100) / wbs.maxsecret;
            cnt_time = plrs[sp].stime / TICRATE;
            cnt_par = wbs.partime / TICRATE;
            g.sound.S_StartSound(null, sfx_barexp);
            sp_state = 10;
        }
        if (sp_state == 2) {
            cnt_kills[0] += 2;
            if ((bcnt & 3) == 0) {
                g.sound.S_StartSound(null, sfx_pistol);
            }
            if (cnt_kills[0] >= (plrs[sp].skills * 100) / wbs.maxkills) {
                cnt_kills[0] = (plrs[sp].skills * 100) / wbs.maxkills;
                g.sound.S_StartSound(null, sfx_barexp);
                sp_state++;
            }
        } else if (sp_state == 4) {
            cnt_items[0] += 2;
            if ((bcnt & 3) == 0) {
                g.sound.S_StartSound(null, sfx_pistol);
            }
            if (cnt_items[0] >= (plrs[sp].sitems * 100) / wbs.maxitems) {
                cnt_items[0] = (plrs[sp].sitems * 100) / wbs.maxitems;
                g.sound.S_StartSound(null, sfx_barexp);
                sp_state++;
            }
        } else if (sp_state == 6) {
            cnt_secret[0] += 2;
            if ((bcnt & 3) == 0) {
                g.sound.S_StartSound(null, sfx_pistol);
            }
            if (cnt_secret[0] >= (plrs[sp].ssecret * 100) / wbs.maxsecret) {
                cnt_secret[0] = (plrs[sp].ssecret * 100) / wbs.maxsecret;
                g.sound.S_StartSound(null, sfx_barexp);
                sp_state++;
            }
        } else if (sp_state == 8) {
            if ((bcnt & 3) == 0) {
                g.sound.S_StartSound(null, sfx_pistol);
            }
            cnt_time += 3;
            if (cnt_time >= plrs[sp].stime / TICRATE) {
                cnt_time = plrs[sp].stime / TICRATE;
            }
            cnt_par += 3;
            if (cnt_par >= wbs.partime / TICRATE) {
                cnt_par = wbs.partime / TICRATE;
                if (cnt_time >= plrs[sp].stime / TICRATE) {
                    g.sound.S_StartSound(null, sfx_barexp);
                    sp_state++;
                }
            }
        } else if (sp_state == 10) {
            if (acceleratestage != 0) {
                g.sound.S_StartSound(null, sfx_sgcock);
                if (g.gamemode == commercial) {
                    WI_initNoState();
                } else {
                    WI_initShowNextLoc();
                }
            }
        } else if ((sp_state & 1) != 0) {
            if (--cnt_pause == 0) {
                sp_state++;
                cnt_pause = TICRATE;
            }
        }
    }

    private void WI_drawStats(final byte[] screen) {
        final int lh = (3 * num(0).height) / 2;
        drawBackground(screen);
        WI_drawAnimatedBack(screen);
        WI_drawLF(screen);
        video.drawPatch(screen, SP_STATSX, SP_STATSY, p("WIOSTK"));
        WI_drawPercent(screen, SCREENWIDTH - SP_STATSX, SP_STATSY, cnt_kills[0]);
        video.drawPatch(screen, SP_STATSX, SP_STATSY + lh, p("WIOSTI"));
        WI_drawPercent(screen, SCREENWIDTH - SP_STATSX, SP_STATSY + lh, cnt_items[0]);
        video.drawPatch(screen, SP_STATSX, SP_STATSY + 2 * lh, p("WISCRT2"));
        WI_drawPercent(screen, SCREENWIDTH - SP_STATSX, SP_STATSY + 2 * lh, cnt_secret[0]);
        video.drawPatch(screen, SP_TIMEX, SP_TIMEY, p("WITIME"));
        WI_drawTime(screen, SCREENWIDTH / 2 - SP_TIMEX, SP_TIMEY, cnt_time);
        if (wbs.epsd < 3) {
            video.drawPatch(screen, SCREENWIDTH / 2 + SP_TIMEX, SP_TIMEY, p("WIPAR"));
            WI_drawTime(screen, SCREENWIDTH - SP_TIMEX, SP_TIMEY, cnt_par);
        }
    }

    private void WI_checkForAccelerate() {
        for (int i = 0; i < MAXPLAYERS; i++) {
            if (!g.playeringame[i]) {
                continue;
            }
            final Player player = g.players[i];
            if ((player.cmd.buttons() & BT_ATTACK) != 0) {
                if (player.attackdown == 0) {
                    acceleratestage = 1;
                }
                player.attackdown = 1;
            } else {
                player.attackdown = 0;
            }
            if ((player.cmd.buttons() & BT_USE) != 0) {
                if (player.usedown == 0) {
                    acceleratestage = 1;
                }
                player.usedown = 1;
            } else {
                player.usedown = 0;
            }
        }
    }

    public void WI_Ticker() {
        bcnt++;
        if (bcnt == 1) {
            if (g.gamemode == commercial) {
                g.sound.S_ChangeMusic(mus_dm2int, true);
            } else {
                g.sound.S_ChangeMusic(mus_inter, true);
            }
        }
        WI_checkForAccelerate();
        switch (state) {
            case StatCount -> {
                if (g.deathmatch != 0) {
                    WI_updateDeathmatchStats();
                } else if (g.netgame) {
                    WI_updateNetgameStats();
                } else {
                    WI_updateStats();
                }
            }
            case ShowNextLoc -> WI_updateShowNextLoc();
            default -> WI_updateNoState();
        }
    }

    /**
     * Draws the intermission as a player sees it ({@code WI_Drawer}).
     *
     * @param screen the 320x200 screen
     * @param viewer the player slot of the viewer, for the star next to their stats
     */
    public void WI_Drawer(final byte[] screen, final int viewer) {
        if (wbs == null) {
            return;
        }
        me = viewer;
        switch (state) {
            case StatCount -> {
                if (g.deathmatch != 0) {
                    WI_drawDeathmatchStats(screen);
                } else if (g.netgame) {
                    WI_drawNetgameStats(screen);
                } else {
                    WI_drawStats(screen);
                }
            }
            case ShowNextLoc -> WI_drawShowNextLoc(screen);
            default -> {
                snl_pointeron = true;
                WI_drawShowNextLoc(screen);
            }
        }
    }

    public void WI_Start(final WbStart wbstartstruct) {
        wbs = wbstartstruct;
        acceleratestage = 0;
        cnt = bcnt = 0;
        me = wbs.pnum;
        plrs = wbs.plyr;
        if (wbs.maxkills == 0) {
            wbs.maxkills = 1;
        }
        if (wbs.maxitems == 0) {
            wbs.maxitems = 1;
        }
        if (wbs.maxsecret == 0) {
            wbs.maxsecret = 1;
        }
        if (g.gamemode != retail && wbs.epsd > 2) {
            wbs.epsd -= 3;
        }
        if (g.deathmatch != 0) {
            WI_initDeathmatchStats();
        } else if (g.netgame) {
            WI_initNetgameStats();
        } else {
            WI_initStats();
        }
    }
}
