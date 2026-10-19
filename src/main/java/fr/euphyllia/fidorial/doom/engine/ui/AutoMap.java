// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine.ui;

import fr.euphyllia.fidorial.doom.engine.Game;
import fr.euphyllia.fidorial.doom.engine.Line;
import fr.euphyllia.fidorial.doom.engine.Mobj;
import fr.euphyllia.fidorial.doom.engine.Player;
import fr.euphyllia.fidorial.doom.engine.Sector;
import fr.euphyllia.fidorial.doom.engine.Strings;
import fr.euphyllia.fidorial.doom.engine.Vertex;
import fr.euphyllia.fidorial.doom.engine.render.Patch;
import fr.euphyllia.fidorial.doom.engine.render.Video;

import static fr.euphyllia.fidorial.doom.engine.Defs.MAPBLOCKUNITS;
import static fr.euphyllia.fidorial.doom.engine.Defs.MAXPLAYERS;
import static fr.euphyllia.fidorial.doom.engine.Defs.ML_DONTDRAW;
import static fr.euphyllia.fidorial.doom.engine.Defs.ML_MAPPED;
import static fr.euphyllia.fidorial.doom.engine.Defs.ML_SECRET;
import static fr.euphyllia.fidorial.doom.engine.Defs.PLAYERRADIUS;
import static fr.euphyllia.fidorial.doom.engine.Defs.SCREENHEIGHT;
import static fr.euphyllia.fidorial.doom.engine.Defs.SCREENWIDTH;
import static fr.euphyllia.fidorial.doom.engine.Defs.pw_allmap;
import static fr.euphyllia.fidorial.doom.engine.Defs.pw_invisibility;
import static fr.euphyllia.fidorial.doom.engine.Fixed.FRACBITS;
import static fr.euphyllia.fidorial.doom.engine.Fixed.FRACUNIT;
import static fr.euphyllia.fidorial.doom.engine.Fixed.MAXINT;
import static fr.euphyllia.fidorial.doom.engine.Fixed.div;
import static fr.euphyllia.fidorial.doom.engine.Fixed.mul;
import static fr.euphyllia.fidorial.doom.engine.Tables.ANGLETOFINESHIFT;
import static fr.euphyllia.fidorial.doom.engine.Tables.finecosine;
import static fr.euphyllia.fidorial.doom.engine.Tables.finesine;

/**
 * The automap of one viewer ({@code am_map.c}).
 */
public final class AutoMap {

    private static final int REDS = 256 - 5 * 16;
    private static final int REDRANGE = 16;
    private static final int GREENS = 7 * 16;
    private static final int GRAYS = 6 * 16;
    private static final int GRAYSRANGE = 16;
    private static final int BROWNS = 4 * 16;
    private static final int YELLOWS = 256 - 32 + 7;
    private static final int BLACK = 0;
    private static final int WHITE = 256 - 47;

    private static final int BACKGROUND = BLACK;
    private static final int WALLCOLORS = REDS;
    private static final int WALLRANGE = REDRANGE;
    private static final int TSWALLCOLORS = GRAYS;
    private static final int FDWALLCOLORS = BROWNS;
    private static final int CDWALLCOLORS = YELLOWS;
    private static final int THINGCOLORS = GREENS;
    private static final int SECRETWALLCOLORS = WALLCOLORS;
    private static final int GRIDCOLORS = GRAYS + GRAYSRANGE / 2;
    private static final int XHAIRCOLORS = GRAYS;

    private static final int AM_NUMMARKPOINTS = 10;
    private static final int INITSCALEMTOF = (int) (.2 * FRACUNIT);
    private static final int F_PANINC = 4;
    private static final int M_ZOOMIN = (int) (1.02 * FRACUNIT);
    private static final int M_ZOOMOUT = (int) (FRACUNIT / 1.02);
    private static final int R = (8 * PLAYERRADIUS) / 7;
    private static final int[][] PLAYER_ARROW = {
            ml(-R + R / 8, 0, R, 0),
            ml(R, 0, R - R / 2, R / 4),
            ml(R, 0, R - R / 2, -R / 4),
            ml(-R + R / 8, 0, -R - R / 8, R / 4),
            ml(-R + R / 8, 0, -R - R / 8, -R / 4),
            ml(-R + 3 * R / 8, 0, -R + R / 8, R / 4),
            ml(-R + 3 * R / 8, 0, -R + R / 8, -R / 4),
    };
    private static final int[][] CHEAT_PLAYER_ARROW = {
            ml(-R + R / 8, 0, R, 0),
            ml(R, 0, R - R / 2, R / 6),
            ml(R, 0, R - R / 2, -R / 6),
            ml(-R + R / 8, 0, -R - R / 8, R / 6),
            ml(-R + R / 8, 0, -R - R / 8, -R / 6),
            ml(-R + 3 * R / 8, 0, -R + R / 8, R / 6),
            ml(-R + 3 * R / 8, 0, -R + R / 8, -R / 6),
            ml(-R / 2, 0, -R / 2, -R / 6),
            ml(-R / 2, -R / 6, -R / 2 + R / 6, -R / 6),
            ml(-R / 2 + R / 6, -R / 6, -R / 2 + R / 6, R / 4),
            ml(-R / 6, 0, -R / 6, -R / 6),
            ml(-R / 6, -R / 6, 0, -R / 6),
            ml(0, -R / 6, 0, R / 4),
            ml(R / 6, R / 4, R / 6, -R / 7),
            ml(R / 6, -R / 7, R / 6 + R / 32, -R / 7 - R / 32),
            ml(R / 6 + R / 32, -R / 7 - R / 32, R / 6 + R / 10, -R / 7),
    };
    private static final int[][] THINTRIANGLE_GUY = {
            ml((int) (-.5 * FRACUNIT), (int) (-.7 * FRACUNIT), FRACUNIT, 0),
            ml(FRACUNIT, 0, (int) (-.5 * FRACUNIT), (int) (.7 * FRACUNIT)),
            ml((int) (-.5 * FRACUNIT), (int) (.7 * FRACUNIT), (int) (-.5 * FRACUNIT), (int) (-.7 * FRACUNIT)),
    };
    private static final int[] THEIR_COLORS = {GREENS, GRAYS, BROWNS, REDS};
    private static final int LEFT = 1;
    private static final int RIGHT = 2;
    private static final int BOTTOM = 4;
    private static final int TOP = 8;
    private final Game g;
    private final Video video = new Video();
    private final Patch[] marknums = new Patch[10];
    private final int f_x = 0;
    private final int f_y = 0;
    private final int f_w = SCREENWIDTH;
    private final int f_h = SCREENHEIGHT - StatusBar.ST_HEIGHT;
    private final int lightlev = 0;
    private final int[] markpoints_x = new int[AM_NUMMARKPOINTS];
    private final int[] markpoints_y = new int[AM_NUMMARKPOINTS];
    private final int[] fl = new int[4];
    private final Player plr;
    private int cheating;
    private boolean grid;
    private boolean active;
    private byte[] fb = new byte[0];
    private int amclock;
    private int m_paninc_x;
    private int m_paninc_y;
    private int mtof_zoommul;
    private int ftom_zoommul;
    private int m_x;
    private int m_y;
    private int m_x2;
    private int m_y2;
    private int m_w;
    private int m_h;
    private int min_x;
    private int min_y;
    private int max_x;
    private int max_y;
    private int min_scale_mtof;
    private int max_scale_mtof;
    private int old_m_w;
    private int old_m_h;
    private int old_m_x;
    private int old_m_y;
    private int f_oldloc_x;
    private int f_oldloc_y;
    private int scale_mtof = INITSCALEMTOF;
    private int scale_ftom;
    private int markpointnum;
    private boolean followplayer = true;
    private boolean bigstate;
    private int lastlevel = -1;
    private int lastepisode = -1;
    private int levelSerial = -1;

    public AutoMap(final Game g, final Player player) {
        this.g = g;
        this.plr = player;
        for (int i = 0; i < 10; i++) {
            marknums[i] = g.res.patch("AMMNUM" + i);
        }
        clearMarks();
    }

    /**
     * A line in map coordinates, {a.x, a.y, b.x, b.y}.
     */
    private static int[] ml(final int ax, final int ay, final int bx, final int by) {
        return new int[]{ax, ay, bx, by};
    }

    public boolean isActive() {
        return active;
    }

    private int ftom(final int x) {
        return mul(x << 16, scale_ftom);
    }

    private int mtof(final int x) {
        return mul(x, scale_mtof) >> 16;
    }

    private int cxmtof(final int x) {
        return f_x + mtof(x - m_x);
    }

    private int cymtof(final int y) {
        return f_y + (f_h - mtof(y - m_y));
    }

    private void activateNewScale() {
        m_x += m_w / 2;
        m_y += m_h / 2;
        m_w = ftom(f_w);
        m_h = ftom(f_h);
        m_x -= m_w / 2;
        m_y -= m_h / 2;
        m_x2 = m_x + m_w;
        m_y2 = m_y + m_h;
    }

    private void saveScaleAndLoc() {
        old_m_x = m_x;
        old_m_y = m_y;
        old_m_w = m_w;
        old_m_h = m_h;
    }

    private void restoreScaleAndLoc() {
        m_w = old_m_w;
        m_h = old_m_h;
        if (!followplayer || plr.mo == null) {
            m_x = old_m_x;
            m_y = old_m_y;
        } else {
            m_x = plr.mo.x - m_w / 2;
            m_y = plr.mo.y - m_h / 2;
        }
        m_x2 = m_x + m_w;
        m_y2 = m_y + m_h;
        scale_mtof = div(f_w << FRACBITS, m_w);
        scale_ftom = div(FRACUNIT, scale_mtof);
    }

    private void addMark() {
        markpoints_x[markpointnum] = m_x + m_w / 2;
        markpoints_y[markpointnum] = m_y + m_h / 2;
        markpointnum = (markpointnum + 1) % AM_NUMMARKPOINTS;
    }

    private void findMinMaxBoundaries() {
        min_x = min_y = MAXINT;
        max_x = max_y = -MAXINT;
        for (final Vertex v : g.level.vertexes) {
            if (v.x < min_x) {
                min_x = v.x;
            } else if (v.x > max_x) {
                max_x = v.x;
            }
            if (v.y < min_y) {
                min_y = v.y;
            } else if (v.y > max_y) {
                max_y = v.y;
            }
        }
        final int max_w = Math.max(max_x - min_x, FRACUNIT);
        final int max_h = Math.max(max_y - min_y, FRACUNIT);
        final int a = div(f_w << FRACBITS, max_w);
        final int b = div(f_h << FRACBITS, max_h);
        min_scale_mtof = Math.min(a, b);
        max_scale_mtof = div(f_h << FRACBITS, 2 * PLAYERRADIUS);
    }

    private void changeWindowLoc() {
        if (m_paninc_x != 0 || m_paninc_y != 0) {
            followplayer = false;
            f_oldloc_x = MAXINT;
        }
        m_x += m_paninc_x;
        m_y += m_paninc_y;
        if (m_x + m_w / 2 > max_x) {
            m_x = max_x - m_w / 2;
        } else if (m_x + m_w / 2 < min_x) {
            m_x = min_x - m_w / 2;
        }
        if (m_y + m_h / 2 > max_y) {
            m_y = max_y - m_h / 2;
        } else if (m_y + m_h / 2 < min_y) {
            m_y = min_y - m_h / 2;
        }
        m_x2 = m_x + m_w;
        m_y2 = m_y + m_h;
    }

    private void initVariables() {
        active = true;
        f_oldloc_x = MAXINT;
        amclock = 0;
        m_paninc_x = m_paninc_y = 0;
        ftom_zoommul = FRACUNIT;
        mtof_zoommul = FRACUNIT;
        m_w = ftom(f_w);
        m_h = ftom(f_h);
        if (plr.mo != null) {
            m_x = plr.mo.x - m_w / 2;
            m_y = plr.mo.y - m_h / 2;
        }
        changeWindowLoc();
        old_m_x = m_x;
        old_m_y = m_y;
        old_m_w = m_w;
        old_m_h = m_h;
    }

    private void clearMarks() {
        for (int i = 0; i < AM_NUMMARKPOINTS; i++) {
            markpoints_x[i] = -1;
        }
        markpointnum = 0;
    }

    private void levelInit() {
        clearMarks();
        findMinMaxBoundaries();
        scale_mtof = div(min_scale_mtof, (int) (0.7 * FRACUNIT));
        if (scale_mtof > max_scale_mtof) {
            scale_mtof = min_scale_mtof;
        }
        scale_ftom = div(FRACUNIT, scale_mtof);
    }

    /**
     * {@code AM_Stop}.
     */
    public void stop() {
        active = false;
        bigstate = false;
    }

    /**
     * {@code AM_Start}.
     *
     * @param serial a number that changes each time a level is loaded
     */
    public void start(final int serial) {
        if (lastlevel != g.gamemap || lastepisode != g.gameepisode || levelSerial != serial) {
            levelInit();
            lastlevel = g.gamemap;
            lastepisode = g.gameepisode;
            levelSerial = serial;
        }
        initVariables();
    }

    /**
     * Toggles the map, as the tab key.
     */
    public void toggle(final int serial) {
        if (active) {
            stop();
        } else if (plr.mo != null) {
            start(serial);
        }
    }

    /**
     * Sets the zoom direction while held.
     *
     * @param direction 1 to zoom in, -1 to zoom out, 0 to stop
     */
    public void zoom(final int direction) {
        if (direction > 0) {
            mtof_zoommul = M_ZOOMIN;
            ftom_zoommul = M_ZOOMOUT;
        } else if (direction < 0) {
            mtof_zoommul = M_ZOOMOUT;
            ftom_zoommul = M_ZOOMIN;
        } else {
            mtof_zoommul = FRACUNIT;
            ftom_zoommul = FRACUNIT;
        }
    }

    /**
     * Sets the panning while held, ignored in follow mode.
     *
     * @param dx -1, 0 or 1
     * @param dy -1, 0 or 1
     */
    public void pan(final int dx, final int dy) {
        if (followplayer) {
            return;
        }
        m_paninc_x = dx * ftom(F_PANINC);
        m_paninc_y = dy * ftom(F_PANINC);
    }

    public void toggleFollow() {
        followplayer = !followplayer;
        f_oldloc_x = MAXINT;
        m_paninc_x = m_paninc_y = 0;
        plr.message = followplayer ? Strings.AMSTR_FOLLOWON : Strings.AMSTR_FOLLOWOFF;
    }

    public void toggleGrid() {
        grid = !grid;
        plr.message = grid ? Strings.AMSTR_GRIDON : Strings.AMSTR_GRIDOFF;
    }

    public void mark() {
        plr.message = Strings.AMSTR_MARKEDSPOT + " " + markpointnum;
        addMark();
    }

    public void clearAllMarks() {
        clearMarks();
        plr.message = Strings.AMSTR_MARKSCLEARED;
    }

    /**
     * Toggles between the whole level and the previous zoom, as the 0 key.
     */
    public void toggleBig() {
        bigstate = !bigstate;
        if (bigstate) {
            saveScaleAndLoc();
            minOutWindowScale();
        } else {
            restoreScaleAndLoc();
        }
    }

    /**
     * The {@code iddt} cheat: cycles through all lines, then things.
     */
    public void cycleCheat() {
        cheating = (cheating + 1) % 3;
    }

    private void minOutWindowScale() {
        scale_mtof = min_scale_mtof;
        scale_ftom = div(FRACUNIT, scale_mtof);
        activateNewScale();
    }

    private void maxOutWindowScale() {
        scale_mtof = max_scale_mtof;
        scale_ftom = div(FRACUNIT, scale_mtof);
        activateNewScale();
    }

    private void changeWindowScale() {
        scale_mtof = mul(scale_mtof, mtof_zoommul);
        scale_ftom = div(FRACUNIT, scale_mtof);
        if (scale_mtof < min_scale_mtof) {
            minOutWindowScale();
        } else if (scale_mtof > max_scale_mtof) {
            maxOutWindowScale();
        } else {
            activateNewScale();
        }
    }

    private void doFollowPlayer() {
        final Mobj mo = plr.mo;
        if (mo == null) {
            return;
        }
        if (f_oldloc_x != mo.x || f_oldloc_y != mo.y) {
            m_x = ftom(mtof(mo.x)) - m_w / 2;
            m_y = ftom(mtof(mo.y)) - m_h / 2;
            m_x2 = m_x + m_w;
            m_y2 = m_y + m_h;
            f_oldloc_x = mo.x;
            f_oldloc_y = mo.y;
        }
    }

    /**
     * {@code AM_Ticker}.
     */
    public void ticker() {
        if (!active) {
            return;
        }
        amclock++;
        if (followplayer) {
            doFollowPlayer();
        }
        if (ftom_zoommul != FRACUNIT) {
            changeWindowScale();
        }
        if (m_paninc_x != 0 || m_paninc_y != 0) {
            changeWindowLoc();
        }
    }

    private int outcode(final int mx, final int my) {
        int oc = 0;
        if (my < 0) {
            oc |= TOP;
        } else if (my >= f_h) {
            oc |= BOTTOM;
        }
        if (mx < 0) {
            oc |= LEFT;
        } else if (mx >= f_w) {
            oc |= RIGHT;
        }
        return oc;
    }

    private boolean clipMline(final int ax, final int ay, final int bx, final int by) {
        int outcode1 = 0;
        int outcode2 = 0;
        if (ay > m_y2) {
            outcode1 = TOP;
        } else if (ay < m_y) {
            outcode1 = BOTTOM;
        }
        if (by > m_y2) {
            outcode2 = TOP;
        } else if (by < m_y) {
            outcode2 = BOTTOM;
        }
        if ((outcode1 & outcode2) != 0) {
            return false;
        }
        if (ax < m_x) {
            outcode1 |= LEFT;
        } else if (ax > m_x2) {
            outcode1 |= RIGHT;
        }
        if (bx < m_x) {
            outcode2 |= LEFT;
        } else if (bx > m_x2) {
            outcode2 |= RIGHT;
        }
        if ((outcode1 & outcode2) != 0) {
            return false;
        }
        fl[0] = cxmtof(ax);
        fl[1] = cymtof(ay);
        fl[2] = cxmtof(bx);
        fl[3] = cymtof(by);
        outcode1 = outcode(fl[0], fl[1]);
        outcode2 = outcode(fl[2], fl[3]);
        if ((outcode1 & outcode2) != 0) {
            return false;
        }
        int guard = 0;
        while ((outcode1 | outcode2) != 0) {
            if (++guard > 8) {
                return false;
            }
            final int outside = outcode1 != 0 ? outcode1 : outcode2;
            int tx = 0;
            int ty = 0;
            final int dx;
            final int dy;
            if ((outside & TOP) != 0) {
                dy = fl[1] - fl[3];
                dx = fl[2] - fl[0];
                tx = fl[0] + (int) ((long) dx * fl[1] / dy);
                ty = 0;
            } else if ((outside & BOTTOM) != 0) {
                dy = fl[1] - fl[3];
                dx = fl[2] - fl[0];
                tx = fl[0] + (int) ((long) dx * (fl[1] - f_h) / dy);
                ty = f_h - 1;
            } else if ((outside & RIGHT) != 0) {
                dy = fl[3] - fl[1];
                dx = fl[2] - fl[0];
                ty = fl[1] + (int) ((long) dy * (f_w - 1 - fl[0]) / dx);
                tx = f_w - 1;
            } else if ((outside & LEFT) != 0) {
                dy = fl[3] - fl[1];
                dx = fl[2] - fl[0];
                ty = fl[1] + (int) ((long) dy * -fl[0] / dx);
                tx = 0;
            }
            if (outside == outcode1) {
                fl[0] = tx;
                fl[1] = ty;
                outcode1 = outcode(tx, ty);
            } else {
                fl[2] = tx;
                fl[3] = ty;
                outcode2 = outcode(tx, ty);
            }
            if ((outcode1 & outcode2) != 0) {
                return false;
            }
        }
        return true;
    }

    private void drawFline(final int color) {
        final int ax0 = fl[0];
        final int ay0 = fl[1];
        final int bx0 = fl[2];
        final int by0 = fl[3];
        if (ax0 < 0 || ax0 >= f_w || ay0 < 0 || ay0 >= f_h || bx0 < 0 || bx0 >= f_w || by0 < 0 || by0 >= f_h) {
            return;
        }
        final byte c = (byte) color;
        final int dx = bx0 - ax0;
        final int ax = 2 * Math.abs(dx);
        final int sx = dx < 0 ? -1 : 1;
        final int dy = by0 - ay0;
        final int ay = 2 * Math.abs(dy);
        final int sy = dy < 0 ? -1 : 1;
        int x = ax0;
        int y = ay0;
        int d;
        if (ax > ay) {
            d = ay - ax / 2;
            while (true) {
                fb[y * f_w + x] = c;
                if (x == bx0) {
                    return;
                }
                if (d >= 0) {
                    y += sy;
                    d -= ax;
                }
                x += sx;
                d += ay;
            }
        } else {
            d = ax - ay / 2;
            while (true) {
                fb[y * f_w + x] = c;
                if (y == by0) {
                    return;
                }
                if (d >= 0) {
                    x += sx;
                    d -= ay;
                }
                y += sy;
                d += ax;
            }
        }
    }

    private void drawMline(final int ax, final int ay, final int bx, final int by, final int color) {
        if (clipMline(ax, ay, bx, by)) {
            drawFline(color);
        }
    }

    private void drawGrid(final int color) {
        final int unit = MAPBLOCKUNITS << FRACBITS;
        int start = m_x;
        if ((start - g.level.bmaporgx) % unit != 0) {
            start += unit - ((start - g.level.bmaporgx) % unit);
        }
        int end = m_x + m_w;
        for (int x = start; x < end; x += unit) {
            drawMline(x, m_y, x, m_y + m_h, color);
        }
        start = m_y;
        if ((start - g.level.bmaporgy) % unit != 0) {
            start += unit - ((start - g.level.bmaporgy) % unit);
        }
        end = m_y + m_h;
        for (int y = start; y < end; y += unit) {
            drawMline(m_x, y, m_x + m_w, y, color);
        }
    }

    private void drawWalls() {
        for (final Line line : g.level.lines) {
            final int ax = line.v1.x;
            final int ay = line.v1.y;
            final int bx = line.v2.x;
            final int by = line.v2.y;
            if (cheating != 0 || (line.flags & ML_MAPPED) != 0) {
                if ((line.flags & ML_DONTDRAW) != 0 && cheating == 0) {
                    continue;
                }
                final Sector back = line.backsector;
                final Sector front = line.frontsector;
                if (back == null || front == null) {
                    drawMline(ax, ay, bx, by, WALLCOLORS + lightlev);
                } else if (line.special == 39) {
                    drawMline(ax, ay, bx, by, WALLCOLORS + WALLRANGE / 2);
                } else if ((line.flags & ML_SECRET) != 0) {
                    drawMline(ax, ay, bx, by, (cheating != 0 ? SECRETWALLCOLORS : WALLCOLORS) + lightlev);
                } else if (back.floorheight != front.floorheight) {
                    drawMline(ax, ay, bx, by, FDWALLCOLORS + lightlev);
                } else if (back.ceilingheight != front.ceilingheight) {
                    drawMline(ax, ay, bx, by, CDWALLCOLORS + lightlev);
                } else if (cheating != 0) {
                    drawMline(ax, ay, bx, by, TSWALLCOLORS + lightlev);
                }
            } else if (plr.powers[pw_allmap] != 0) {
                if ((line.flags & ML_DONTDRAW) == 0) {
                    drawMline(ax, ay, bx, by, GRAYS + 3);
                }
            }
        }
    }

    private void drawLineCharacter(final int[][] lineguy, final int scale, final int angle, final int color,
                                   final int x, final int y) {
        final int fine = angle >>> ANGLETOFINESHIFT;
        final int sin = finesine[fine];
        final int cos = finecosine(fine);
        for (final int[] l : lineguy) {
            int ax = l[0];
            int ay = l[1];
            int bx = l[2];
            int by = l[3];
            if (scale != 0) {
                ax = mul(scale, ax);
                ay = mul(scale, ay);
                bx = mul(scale, bx);
                by = mul(scale, by);
            }
            if (angle != 0) {
                final int rax = mul(ax, cos) - mul(ay, sin);
                ay = mul(ax, sin) + mul(ay, cos);
                ax = rax;
                final int rbx = mul(bx, cos) - mul(by, sin);
                by = mul(bx, sin) + mul(by, cos);
                bx = rbx;
            }
            drawMline(ax + x, ay + y, bx + x, by + y, color);
        }
    }

    private void drawPlayers() {
        if (!g.netgame) {
            final Mobj mo = plr.mo;
            if (mo != null) {
                drawLineCharacter(cheating != 0 ? CHEAT_PLAYER_ARROW : PLAYER_ARROW, 0, mo.angle, WHITE, mo.x, mo.y);
            }
            return;
        }
        for (int i = 0; i < MAXPLAYERS; i++) {
            final Player p = g.players[i];
            if (g.deathmatch != 0 && p != plr) {
                continue;
            }
            if (!g.playeringame[i] || p.mo == null) {
                continue;
            }
            final int color = p.powers[pw_invisibility] != 0 ? 246 : THEIR_COLORS[i];
            drawLineCharacter(PLAYER_ARROW, 0, p.mo.angle, color, p.mo.x, p.mo.y);
        }
    }

    private void drawThings(final int colors) {
        for (final Sector sector : g.level.sectors) {
            for (Mobj t = sector.thinglist; t != null; t = t.snext) {
                drawLineCharacter(THINTRIANGLE_GUY, 16 << FRACBITS, t.angle, colors + lightlev, t.x, t.y);
            }
        }
    }

    private void drawMarks(final byte[] screen) {
        for (int i = 0; i < AM_NUMMARKPOINTS; i++) {
            if (markpoints_x[i] != -1) {
                final int w = 5;
                final int h = 6;
                final int fx = cxmtof(markpoints_x[i]);
                final int fy = cymtof(markpoints_y[i]);
                if (fx >= f_x && fx <= f_w - w && fy >= f_y && fy <= f_h - h) {
                    video.drawPatch(screen, fx, fy, marknums[i]);
                }
            }
        }
    }

    /**
     * Draws the map over the view area ({@code AM_Drawer}).
     *
     * @param screen the 320x200 screen
     */
    public void draw(final byte[] screen) {
        if (!active) {
            return;
        }
        fb = screen;
        java.util.Arrays.fill(screen, 0, f_w * f_h, (byte) BACKGROUND);
        if (grid) {
            drawGrid(GRIDCOLORS);
        }
        drawWalls();
        drawPlayers();
        if (cheating == 2) {
            drawThings(THINGCOLORS);
        }
        screen[(f_w * (f_h + 1)) / 2] = (byte) XHAIRCOLORS;
        drawMarks(screen);
    }
}
