// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine.render;

import fr.euphyllia.fidorial.doom.engine.Angles;
import fr.euphyllia.fidorial.doom.engine.Game;
import fr.euphyllia.fidorial.doom.engine.Level;
import fr.euphyllia.fidorial.doom.engine.Line;
import fr.euphyllia.fidorial.doom.engine.Mobj;
import fr.euphyllia.fidorial.doom.engine.Node;
import fr.euphyllia.fidorial.doom.engine.Player;
import fr.euphyllia.fidorial.doom.engine.PspDef;
import fr.euphyllia.fidorial.doom.engine.Sector;
import fr.euphyllia.fidorial.doom.engine.Seg;
import fr.euphyllia.fidorial.doom.engine.Side;
import fr.euphyllia.fidorial.doom.engine.Subsector;
import fr.euphyllia.fidorial.doom.engine.info.State;
import org.jspecify.annotations.Nullable;

import static fr.euphyllia.fidorial.doom.engine.BBox.BOXBOTTOM;
import static fr.euphyllia.fidorial.doom.engine.BBox.BOXLEFT;
import static fr.euphyllia.fidorial.doom.engine.BBox.BOXRIGHT;
import static fr.euphyllia.fidorial.doom.engine.BBox.BOXTOP;
import static fr.euphyllia.fidorial.doom.engine.Defs.FF_FRAMEMASK;
import static fr.euphyllia.fidorial.doom.engine.Defs.FF_FULLBRIGHT;
import static fr.euphyllia.fidorial.doom.engine.Defs.ML_DONTPEGBOTTOM;
import static fr.euphyllia.fidorial.doom.engine.Defs.ML_DONTPEGTOP;
import static fr.euphyllia.fidorial.doom.engine.Defs.ML_MAPPED;
import static fr.euphyllia.fidorial.doom.engine.Defs.NF_SUBSECTOR;
import static fr.euphyllia.fidorial.doom.engine.Defs.NUMPSPRITES;
import static fr.euphyllia.fidorial.doom.engine.Defs.SCREENHEIGHT;
import static fr.euphyllia.fidorial.doom.engine.Defs.SCREENWIDTH;
import static fr.euphyllia.fidorial.doom.engine.Defs.commercial;
import static fr.euphyllia.fidorial.doom.engine.Defs.pw_invisibility;
import static fr.euphyllia.fidorial.doom.engine.Fixed.FRACBITS;
import static fr.euphyllia.fidorial.doom.engine.Fixed.FRACUNIT;
import static fr.euphyllia.fidorial.doom.engine.Fixed.MAXINT;
import static fr.euphyllia.fidorial.doom.engine.Fixed.MININT;
import static fr.euphyllia.fidorial.doom.engine.Fixed.div;
import static fr.euphyllia.fidorial.doom.engine.Fixed.mul;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_SHADOW;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_TRANSLATION;
import static fr.euphyllia.fidorial.doom.engine.MobjFlags.MF_TRANSSHIFT;
import static fr.euphyllia.fidorial.doom.engine.Tables.ANG180;
import static fr.euphyllia.fidorial.doom.engine.Tables.ANG45;
import static fr.euphyllia.fidorial.doom.engine.Tables.ANG90;
import static fr.euphyllia.fidorial.doom.engine.Tables.ANGLETOFINESHIFT;
import static fr.euphyllia.fidorial.doom.engine.Tables.DBITS;
import static fr.euphyllia.fidorial.doom.engine.Tables.FINEANGLES;
import static fr.euphyllia.fidorial.doom.engine.Tables.finecosine;
import static fr.euphyllia.fidorial.doom.engine.Tables.finesine;
import static fr.euphyllia.fidorial.doom.engine.Tables.finetangent;
import static fr.euphyllia.fidorial.doom.engine.Tables.tantoangle;

/**
 * The software renderer: BSP walk, walls, flats, sprites and weapon ({@code r_main.c}, {@code r_bsp.c},
 * {@code r_segs.c}, {@code r_plane.c}, {@code r_things.c}, {@code r_draw.c}).
 *
 * <p>One instance per viewer, drawing into a 320x200 palette indexed screen. Unlike vanilla, the
 * visplane, drawseg, vissprite and opening limits grow on demand instead of crashing.</p>
 */
public final class Renderer {

    public static final int LIGHTLEVELS = 16;
    public static final int LIGHTSEGSHIFT = 4;
    public static final int MAXLIGHTSCALE = 48;
    public static final int LIGHTSCALESHIFT = 12;
    public static final int MAXLIGHTZ = 128;
    public static final int LIGHTZSHIFT = 20;
    private static final int FIELDOFVIEW = 2048;
    private static final int NUMCOLORMAPS = 32;
    private static final int DISTMAP = 2;
    private static final int SBARHEIGHT = 32;
    private static final int ANGLETOSKYSHIFT = 22;
    private static final int HEIGHTBITS = 12;
    private static final int HEIGHTUNIT = 1 << HEIGHTBITS;
    private static final int MINZ = FRACUNIT * 4;
    private static final int BASEYCENTER = 100;
    private static final int SIL_BOTTOM = 1;
    private static final int SIL_TOP = 2;
    private static final int SIL_BOTH = 3;
    private static final short MAXSHORT = Short.MAX_VALUE;
    private static final int NO_COLORMAP = -1;
    private static final int UNSET = 0xffff;

    private static final int[][] CHECKCOORD = {
            {3, 0, 2, 1}, {3, 0, 2, 0}, {3, 1, 2, 0}, {0, 0, 0, 0}, {2, 0, 2, 1}, {0, 0, 0, 0},
            {3, 1, 3, 0}, {0, 0, 0, 0}, {2, 0, 3, 1}, {2, 1, 3, 1}, {2, 1, 3, 0}, {0, 0, 0, 0}
    };

    private static final int FUZZTABLE = 50;
    private static final int FUZZOFF = SCREENWIDTH;
    private static final int[] FUZZOFFSET = {
            FUZZOFF, -FUZZOFF, FUZZOFF, -FUZZOFF, FUZZOFF, FUZZOFF, -FUZZOFF,
            FUZZOFF, FUZZOFF, -FUZZOFF, FUZZOFF, FUZZOFF, FUZZOFF, -FUZZOFF,
            FUZZOFF, FUZZOFF, FUZZOFF, -FUZZOFF, -FUZZOFF, -FUZZOFF, -FUZZOFF,
            FUZZOFF, -FUZZOFF, -FUZZOFF, FUZZOFF, FUZZOFF, FUZZOFF, FUZZOFF, -FUZZOFF,
            FUZZOFF, -FUZZOFF, FUZZOFF, FUZZOFF, -FUZZOFF, -FUZZOFF, FUZZOFF,
            FUZZOFF, -FUZZOFF, -FUZZOFF, -FUZZOFF, -FUZZOFF, FUZZOFF, FUZZOFF,
            FUZZOFF, FUZZOFF, -FUZZOFF, FUZZOFF, FUZZOFF, -FUZZOFF, FUZZOFF
    };
    private static final int COL_NORMAL = 0;
    private static final int COL_FUZZ = 1;
    private static final int COL_TRANSLATED = 2;
    private final Game g;
    private final RenderData res;
    private final byte[] colormaps;
    private final int[] ylookup = new int[SCREENHEIGHT];
    private final int[] columnofs = new int[SCREENWIDTH];
    private final int[] viewangletox = new int[FINEANGLES / 2];
    private final int[] xtoviewangle = new int[SCREENWIDTH + 1];
    private final int[][] scalelight = new int[LIGHTLEVELS][MAXLIGHTSCALE];
    private final int[] scalelightfixed = new int[MAXLIGHTSCALE];
    private final int[][] zlight = new int[LIGHTLEVELS][MAXLIGHTZ];
    private final int[] solidFirst = new int[SCREENWIDTH + 4];
    private final int[] solidLast = new int[SCREENWIDTH + 4];
    private final short[] floorclip = new short[SCREENWIDTH];
    private final short[] ceilingclip = new short[SCREENWIDTH];
    private final int[] spanstart = new int[SCREENHEIGHT];
    private final int[] yslope = new int[SCREENHEIGHT];
    private final int[] distscale = new int[SCREENWIDTH];
    private final int[] cachedheight = new int[SCREENHEIGHT];
    private final int[] cacheddistance = new int[SCREENHEIGHT];
    private final int[] cachedxstep = new int[SCREENHEIGHT];
    private final int[] cachedystep = new int[SCREENHEIGHT];
    private final short[] negonearray = new short[SCREENWIDTH];
    private final short[] screenheightarray = new short[SCREENWIDTH];
    private final VisSprite vsprsortedhead = new VisSprite();
    private final VisSprite unsorted = new VisSprite();
    private final short[] clipbot = new short[SCREENWIDTH];
    private final short[] cliptop = new short[SCREENWIDTH];
    // view window
    public int viewwidth;
    public int scaledviewwidth;
    public int viewheight;
    public int viewwindowx;
    public int viewwindowy;
    public int detailshift;
    /**
     * The screen this renderer draws into, {@code SCREENWIDTH * SCREENHEIGHT}.
     */
    private byte[] screen;
    private int setblocks = 10;
    private int setdetail;
    private boolean setsizeneeded = true;
    // r_main
    private int centerx;
    private int centery;
    private int centerxfrac;
    private int centeryfrac;
    private int projection;
    private int viewx;
    private int viewy;
    private int viewz;
    private int viewangle;
    private int viewcos;
    private int viewsin;
    private Player viewplayer;
    private int clipangle;
    private int extralight;
    private int fixedcolormap = NO_COLORMAP;
    private int[] walllights;
    // r_draw
    private int dc_colormap;
    private int dc_x;
    private int dc_yl;
    private int dc_yh;
    private int dc_iscale;
    private int dc_texturemid;
    private byte[] dc_source;
    private int dc_sourceHeight;
    private byte @Nullable [] dc_translation;
    private int fuzzpos;
    private int ds_y;
    private int ds_x1;
    private int ds_x2;
    private int ds_colormap;
    private int ds_xfrac;
    private int ds_yfrac;
    private int ds_xstep;
    private int ds_ystep;
    private byte[] ds_source;
    private int colfunc = COL_NORMAL;
    // r_bsp
    private Seg curline;
    private Side sidedef;
    private Line linedef;
    private Sector frontsector;
    private @Nullable Sector backsector;
    private DrawSeg[] drawsegs = newDrawSegs(256);
    private int ds_p;
    private int newend;
    // r_segs
    private boolean segtextured;
    private boolean markfloor;
    private boolean markceiling;
    private boolean maskedtexture;
    private int toptexture;
    private int bottomtexture;
    private int midtexture;
    private int rw_normalangle;
    private int rw_angle1;
    private int rw_x;
    private int rw_stopx;
    private int rw_centerangle;
    private int rw_offset;
    private int rw_distance;
    private int rw_scale;
    private int rw_scalestep;
    private int rw_midtexturemid;
    private int rw_toptexturemid;
    private int rw_bottomtexturemid;
    private int worldtop;

    // r_plane
    private int worldbottom;
    private int worldhigh;
    private int worldlow;
    private int pixhigh;
    private int pixlow;
    private int pixhighstep;
    private int pixlowstep;
    private int topfrac;
    private int topstep;
    private int bottomfrac;
    private int bottomstep;
    private short[] maskedtexturecol;
    private int maskedtexturecolOfs;
    private Visplane[] visplanes = newVisplanes(128);
    private int lastvisplane;
    private @Nullable Visplane floorplane;
    private @Nullable Visplane ceilingplane;
    private short[] openings = new short[SCREENWIDTH * 64];
    private int lastopening;
    private int[] planezlight;
    private int planeheight;
    private int basexscale;
    private int baseyscale;
    // r_things
    private int pspritescale;
    private int pspriteiscale;
    private int[] spritelights;
    private VisSprite[] vissprites = newVisSprites(128);
    private int vissprite_p;
    private short[] mfloorclip;
    private int mfloorclipOfs;
    private short[] mceilingclip;
    private int mceilingclipOfs;
    private int spryscale;
    private int sprtopscreen;
    public Renderer(final Game g) {
        this.g = g;
        this.res = g.res;
        this.colormaps = res.colormaps;
        for (int i = 0; i < SCREENWIDTH; i++) {
            negonearray[i] = -1;
        }
        initLightTables();
    }

    private static DrawSeg[] newDrawSegs(final int n) {
        final DrawSeg[] a = new DrawSeg[n];
        for (int i = 0; i < n; i++) {
            a[i] = new DrawSeg();
        }
        return a;
    }

    private static Visplane[] newVisplanes(final int n) {
        final Visplane[] a = new Visplane[n];
        for (int i = 0; i < n; i++) {
            a[i] = new Visplane();
        }
        return a;
    }

    private static VisSprite[] newVisSprites(final int n) {
        final VisSprite[] a = new VisSprite[n];
        for (int i = 0; i < n; i++) {
            a[i] = new VisSprite();
        }
        return a;
    }

    private static int pointOnSegSide(final int x, final int y, final Seg line) {
        final int lx = line.v1.x;
        final int ly = line.v1.y;
        final int ldx = line.v2.x - lx;
        final int ldy = line.v2.y - ly;
        if (ldx == 0) {
            if (x <= lx) {
                return ldy > 0 ? 1 : 0;
            }
            return ldy < 0 ? 1 : 0;
        }
        if (ldy == 0) {
            if (y <= ly) {
                return ldx < 0 ? 1 : 0;
            }
            return ldx > 0 ? 1 : 0;
        }
        final int dx = x - lx;
        final int dy = y - ly;
        if (((ldy ^ ldx ^ dx ^ dy) & 0x80000000) != 0) {
            if (((ldy ^ dx) & 0x80000000) != 0) {
                return 1;
            }
            return 0;
        }
        final int left = mul(ldy >> FRACBITS, dx);
        final int right = mul(dy, ldx >> FRACBITS);
        return right < left ? 0 : 1;
    }

    /**
     * {@code R_SetViewSize}: takes effect on the next frame.
     */
    public void setViewSize(final int blocks, final int detail) {
        setsizeneeded = true;
        setblocks = Math.clamp(blocks, 3, 11);
        setdetail = detail == 0 ? 0 : 1;
    }

    public int screenblocks() {
        return setblocks;
    }

    public int detail() {
        return setdetail;
    }

    private void initLightTables() {
        for (int i = 0; i < LIGHTLEVELS; i++) {
            final int startmap = ((LIGHTLEVELS - 1 - i) * 2) * NUMCOLORMAPS / LIGHTLEVELS;
            for (int j = 0; j < MAXLIGHTZ; j++) {
                int scale = div(SCREENWIDTH / 2 * FRACUNIT, (j + 1) << LIGHTZSHIFT);
                scale >>= LIGHTSCALESHIFT;
                int level = startmap - scale / DISTMAP;
                if (level < 0) {
                    level = 0;
                }
                if (level >= NUMCOLORMAPS) {
                    level = NUMCOLORMAPS - 1;
                }
                zlight[i][j] = level * 256;
            }
        }
    }

    private void executeSetViewSize() {
        setsizeneeded = false;
        if (setblocks == 11) {
            scaledviewwidth = SCREENWIDTH;
            viewheight = SCREENHEIGHT;
        } else {
            scaledviewwidth = setblocks * 32;
            viewheight = (setblocks * 168 / 10) & ~7;
        }
        detailshift = setdetail;
        viewwidth = scaledviewwidth >> detailshift;
        centery = viewheight / 2;
        centerx = viewwidth / 2;
        centerxfrac = centerx << FRACBITS;
        centeryfrac = centery << FRACBITS;
        projection = centerxfrac;
        initBuffer(scaledviewwidth, viewheight);
        initTextureMapping();
        pspritescale = FRACUNIT * viewwidth / SCREENWIDTH;
        pspriteiscale = FRACUNIT * SCREENWIDTH / viewwidth;
        for (int i = 0; i < viewwidth; i++) {
            screenheightarray[i] = (short) viewheight;
        }
        for (int i = 0; i < viewheight; i++) {
            int dy = ((i - viewheight / 2) << FRACBITS) + FRACUNIT / 2;
            dy = Math.abs(dy);
            yslope[i] = div((viewwidth << detailshift) / 2 * FRACUNIT, dy);
        }
        for (int i = 0; i < viewwidth; i++) {
            final int cosadj = Math.abs(finecosine(xtoviewangle[i] >>> ANGLETOFINESHIFT));
            distscale[i] = div(FRACUNIT, cosadj);
        }
        for (int i = 0; i < LIGHTLEVELS; i++) {
            final int startmap = ((LIGHTLEVELS - 1 - i) * 2) * NUMCOLORMAPS / LIGHTLEVELS;
            for (int j = 0; j < MAXLIGHTSCALE; j++) {
                int level = startmap - j * SCREENWIDTH / (viewwidth << detailshift) / DISTMAP;
                if (level < 0) {
                    level = 0;
                }
                if (level >= NUMCOLORMAPS) {
                    level = NUMCOLORMAPS - 1;
                }
                scalelight[i][j] = level * 256;
            }
        }
    }

    private void initBuffer(final int width, final int height) {
        viewwindowx = (SCREENWIDTH - width) >> 1;
        for (int i = 0; i < width; i++) {
            columnofs[i] = viewwindowx + i;
        }
        if (width == SCREENWIDTH) {
            viewwindowy = 0;
        } else {
            viewwindowy = (SCREENHEIGHT - SBARHEIGHT - height) >> 1;
        }
        for (int i = 0; i < height; i++) {
            ylookup[i] = (i + viewwindowy) * SCREENWIDTH;
        }
    }

    private void initTextureMapping() {
        final int focallength = div(centerxfrac, finetangent[FINEANGLES / 4 + FIELDOFVIEW / 2]);
        for (int i = 0; i < FINEANGLES / 2; i++) {
            int t;
            if (finetangent[i] > FRACUNIT * 2) {
                t = -1;
            } else if (finetangent[i] < -FRACUNIT * 2) {
                t = viewwidth + 1;
            } else {
                t = mul(finetangent[i], focallength);
                t = (centerxfrac - t + FRACUNIT - 1) >> FRACBITS;
                if (t < -1) {
                    t = -1;
                } else if (t > viewwidth + 1) {
                    t = viewwidth + 1;
                }
            }
            viewangletox[i] = t;
        }
        for (int x = 0; x <= viewwidth; x++) {
            int i = 0;
            while (viewangletox[i] > x) {
                i++;
            }
            xtoviewangle[x] = (i << ANGLETOFINESHIFT) - ANG90;
        }
        for (int i = 0; i < FINEANGLES / 2; i++) {
            if (viewangletox[i] == -1) {
                viewangletox[i] = 0;
            } else if (viewangletox[i] == viewwidth + 1) {
                viewangletox[i] = viewwidth;
            }
        }
        clipangle = xtoviewangle[0];
    }

    /**
     * Applies a pending view size change; call before drawing the border or the view.
     */
    public void checkViewSize() {
        if (setsizeneeded) {
            executeSetViewSize();
        }
    }

    private int pointToAngle(final int x, final int y) {
        return Angles.pointToAngle(x - viewx, y - viewy);
    }

    private int pointToDist(final int x, final int y) {
        int dx = Math.abs(x - viewx);
        int dy = Math.abs(y - viewy);
        if (dy > dx) {
            final int temp = dx;
            dx = dy;
            dy = temp;
        }
        if (dx == 0) {
            return 0;
        }
        final int angle = (tantoangle[div(dy, dx) >> DBITS] + ANG90) >>> ANGLETOFINESHIFT;
        return div(dx, finesine[angle]);
    }

    private int scaleFromGlobalAngle(final int visangle) {
        final int anglea = ANG90 + (visangle - viewangle);
        final int angleb = ANG90 + (visangle - rw_normalangle);
        final int sinea = finesine[anglea >>> ANGLETOFINESHIFT];
        final int sineb = finesine[angleb >>> ANGLETOFINESHIFT];
        final int num = mul(projection, sineb) << detailshift;
        final int den = mul(rw_distance, sinea);
        int scale;
        if (den > num >> 16) {
            scale = div(num, den);
            if (scale > 64 * FRACUNIT) {
                scale = 64 * FRACUNIT;
            } else if (scale < 256) {
                scale = 256;
            }
        } else {
            scale = 64 * FRACUNIT;
        }
        return scale;
    }

    private void setupFrame(final Player player) {
        viewplayer = player;
        viewx = player.mo.x;
        viewy = player.mo.y;
        viewangle = player.mo.angle;
        extralight = player.extralight;
        viewz = player.viewz;
        viewsin = finesine[viewangle >>> ANGLETOFINESHIFT];
        viewcos = finecosine(viewangle >>> ANGLETOFINESHIFT);
        if (player.fixedcolormap != 0) {
            fixedcolormap = player.fixedcolormap * 256;
            walllights = scalelightfixed;
            for (int i = 0; i < MAXLIGHTSCALE; i++) {
                scalelightfixed[i] = fixedcolormap;
            }
        } else {
            fixedcolormap = NO_COLORMAP;
        }
        g.validcount++;
    }

    /**
     * Renders the 3D view of a player into the screen ({@code R_RenderPlayerView}).
     *
     * @param player the viewer, who must have a map object
     * @param target the 320x200 screen to draw into
     */
    public void renderPlayerView(final Player player, final byte[] target) {
        checkViewSize();
        this.screen = target;
        setupFrame(player);
        clearClipSegs();
        ds_p = 0;
        clearPlanes();
        vissprite_p = 0;
        renderBSPNode(g.level.nodes.length - 1);
        drawPlanes();
        drawMasked();
    }

    private void colfunc() {
        switch (colfunc) {
            case COL_FUZZ -> drawFuzzColumn();
            case COL_TRANSLATED -> drawTranslatedColumn();
            default -> drawColumn();
        }
    }

    private int texel(final int frac) {
        final int h = dc_sourceHeight;
        final int v = frac >> FRACBITS;
        if ((h & (h - 1)) == 0) {
            return v & (h - 1);
        }
        return Math.floorMod(v, h);
    }

    private void drawColumn() {
        int count = dc_yh - dc_yl;
        if (count < 0) {
            return;
        }
        if (dc_x < 0 || dc_x >= viewwidth || dc_yl < 0 || dc_yh >= viewheight) {
            return;
        }
        final byte[] scr = screen;
        final byte[] src = dc_source;
        final int cmap = dc_colormap;
        final int fracstep = dc_iscale;
        int frac = dc_texturemid + (dc_yl - centery) * fracstep;
        if (detailshift == 0) {
            int dest = ylookup[dc_yl] + columnofs[dc_x];
            do {
                scr[dest] = colormaps[cmap + (src[texel(frac)] & 0xff)];
                dest += SCREENWIDTH;
                frac += fracstep;
            } while (count-- > 0);
        } else {
            final int x = dc_x << 1;
            int dest = ylookup[dc_yl] + columnofs[x];
            int dest2 = ylookup[dc_yl] + columnofs[x + 1];
            do {
                final byte c = colormaps[cmap + (src[texel(frac)] & 0xff)];
                scr[dest] = c;
                scr[dest2] = c;
                dest += SCREENWIDTH;
                dest2 += SCREENWIDTH;
                frac += fracstep;
            } while (count-- > 0);
        }
    }

    private void drawFuzzColumn() {
        if (dc_yl == 0) {
            dc_yl = 1;
        }
        if (dc_yh == viewheight - 1) {
            dc_yh = viewheight - 2;
        }
        int count = dc_yh - dc_yl;
        if (count < 0) {
            return;
        }
        if (dc_x < 0 || dc_x >= viewwidth || dc_yl < 0 || dc_yh >= viewheight) {
            return;
        }
        final byte[] scr = screen;
        final int x = detailshift == 0 ? dc_x : dc_x << 1;
        int dest = ylookup[dc_yl] + columnofs[x];
        do {
            final int from = dest + FUZZOFFSET[fuzzpos];
            final int c = from >= 0 && from < scr.length ? scr[from] & 0xff : 0;
            scr[dest] = colormaps[6 * 256 + c];
            if (detailshift != 0) {
                scr[dest + 1] = scr[dest];
            }
            if (++fuzzpos == FUZZTABLE) {
                fuzzpos = 0;
            }
            dest += SCREENWIDTH;
        } while (count-- > 0);
    }

    private void drawTranslatedColumn() {
        int count = dc_yh - dc_yl;
        if (count < 0) {
            return;
        }
        if (dc_x < 0 || dc_x >= viewwidth || dc_yl < 0 || dc_yh >= viewheight) {
            return;
        }
        final byte[] scr = screen;
        final byte[] src = dc_source;
        final byte[] trans = dc_translation;
        final int fracstep = dc_iscale;
        int frac = dc_texturemid + (dc_yl - centery) * fracstep;
        final int x = detailshift == 0 ? dc_x : dc_x << 1;
        int dest = ylookup[dc_yl] + columnofs[x];
        do {
            final int idx = frac >> FRACBITS;
            final int s = idx >= 0 && idx < src.length ? src[idx] & 0xff : 0;
            final byte c = colormaps[dc_colormap + (trans[s] & 0xff)];
            scr[dest] = c;
            if (detailshift != 0) {
                scr[dest + 1] = c;
            }
            dest += SCREENWIDTH;
            frac += fracstep;
        } while (count-- > 0);
    }

    private void drawSpan() {
        int xfrac = ds_xfrac;
        int yfrac = ds_yfrac;
        final byte[] scr = screen;
        final byte[] src = ds_source;
        final int cmap = ds_colormap;
        if (detailshift == 0) {
            int dest = ylookup[ds_y] + columnofs[ds_x1];
            int count = ds_x2 - ds_x1;
            do {
                final int spot = ((yfrac >> (16 - 6)) & (63 * 64)) + ((xfrac >> 16) & 63);
                scr[dest++] = colormaps[cmap + (src[spot] & 0xff)];
                xfrac += ds_xstep;
                yfrac += ds_ystep;
            } while (count-- > 0);
        } else {
            int dest = ylookup[ds_y] + columnofs[ds_x1 << 1];
            int count = ds_x2 - ds_x1;
            do {
                final int spot = ((yfrac >> (16 - 6)) & (63 * 64)) + ((xfrac >> 16) & 63);
                final byte c = colormaps[cmap + (src[spot] & 0xff)];
                scr[dest++] = c;
                scr[dest++] = c;
                xfrac += ds_xstep;
                yfrac += ds_ystep;
            } while (count-- > 0);
        }
    }

    private void clearClipSegs() {
        solidFirst[0] = -0x7fffffff;
        solidLast[0] = -1;
        solidFirst[1] = viewwidth;
        solidLast[1] = 0x7fffffff;
        newend = 2;
    }

    private void clipSolidWallSegment(final int first, final int last) {
        int start = 0;
        while (solidLast[start] < first - 1) {
            start++;
        }
        if (first < solidFirst[start]) {
            if (last < solidFirst[start] - 1) {
                storeWallRange(first, last);
                int next = newend;
                newend++;
                while (next != start) {
                    solidFirst[next] = solidFirst[next - 1];
                    solidLast[next] = solidLast[next - 1];
                    next--;
                }
                solidFirst[next] = first;
                solidLast[next] = last;
                return;
            }
            storeWallRange(first, solidFirst[start] - 1);
            solidFirst[start] = first;
        }
        if (last <= solidLast[start]) {
            return;
        }
        int next = start;
        boolean crunch = false;
        while (last >= solidFirst[next + 1] - 1) {
            storeWallRange(solidLast[next] + 1, solidFirst[next + 1] - 1);
            next++;
            if (last <= solidLast[next]) {
                solidLast[start] = solidLast[next];
                crunch = true;
                break;
            }
        }
        if (!crunch) {
            storeWallRange(solidLast[next] + 1, last);
            solidLast[start] = last;
        }
        if (next == start) {
            return;
        }
        while (next++ != newend) {
            ++start;
            solidFirst[start] = solidFirst[next];
            solidLast[start] = solidLast[next];
        }
        newend = start + 1;
    }

    private void clipPassWallSegment(final int first, final int last) {
        int start = 0;
        while (solidLast[start] < first - 1) {
            start++;
        }
        if (first < solidFirst[start]) {
            if (last < solidFirst[start] - 1) {
                storeWallRange(first, last);
                return;
            }
            storeWallRange(first, solidFirst[start] - 1);
        }
        if (last <= solidLast[start]) {
            return;
        }
        while (last >= solidFirst[start + 1] - 1) {
            storeWallRange(solidLast[start] + 1, solidFirst[start + 1] - 1);
            start++;
            if (last <= solidLast[start]) {
                return;
            }
        }
        storeWallRange(solidLast[start] + 1, last);
    }

    private void addLine(final Seg line) {
        curline = line;
        int angle1 = pointToAngle(line.v1.x, line.v1.y);
        int angle2 = pointToAngle(line.v2.x, line.v2.y);
        final int span = angle1 - angle2;
        if (Integer.compareUnsigned(span, ANG180) >= 0) {
            return;
        }
        rw_angle1 = angle1;
        angle1 -= viewangle;
        angle2 -= viewangle;
        int tspan = angle1 + clipangle;
        if (Integer.compareUnsigned(tspan, 2 * clipangle) > 0) {
            tspan -= 2 * clipangle;
            if (Integer.compareUnsigned(tspan, span) >= 0) {
                return;
            }
            angle1 = clipangle;
        }
        tspan = clipangle - angle2;
        if (Integer.compareUnsigned(tspan, 2 * clipangle) > 0) {
            tspan -= 2 * clipangle;
            if (Integer.compareUnsigned(tspan, span) >= 0) {
                return;
            }
            angle2 = -clipangle;
        }
        angle1 = (angle1 + ANG90) >>> ANGLETOFINESHIFT;
        angle2 = (angle2 + ANG90) >>> ANGLETOFINESHIFT;
        final int x1 = viewangletox[angle1];
        final int x2 = viewangletox[angle2];
        if (x1 == x2) {
            return;
        }
        backsector = line.backsector;
        if (backsector == null) {
            clipSolidWallSegment(x1, x2 - 1);
            return;
        }
        if (backsector.ceilingheight <= frontsector.floorheight || backsector.floorheight >= frontsector.ceilingheight) {
            clipSolidWallSegment(x1, x2 - 1);
            return;
        }
        if (backsector.ceilingheight != frontsector.ceilingheight || backsector.floorheight != frontsector.floorheight) {
            clipPassWallSegment(x1, x2 - 1);
            return;
        }
        if (backsector.ceilingpic == frontsector.ceilingpic && backsector.floorpic == frontsector.floorpic
                && backsector.lightlevel == frontsector.lightlevel && curline.sidedef.midtexture == 0) {
            return;
        }
        clipPassWallSegment(x1, x2 - 1);
    }

    private boolean checkBBox(final int[] bspcoord) {
        final int boxx;
        final int boxy;
        if (viewx <= bspcoord[BOXLEFT]) {
            boxx = 0;
        } else if (viewx < bspcoord[BOXRIGHT]) {
            boxx = 1;
        } else {
            boxx = 2;
        }
        if (viewy >= bspcoord[BOXTOP]) {
            boxy = 0;
        } else if (viewy > bspcoord[BOXBOTTOM]) {
            boxy = 1;
        } else {
            boxy = 2;
        }
        final int boxpos = (boxy << 2) + boxx;
        if (boxpos == 5) {
            return true;
        }
        final int x1 = bspcoord[CHECKCOORD[boxpos][0]];
        final int y1 = bspcoord[CHECKCOORD[boxpos][1]];
        final int x2 = bspcoord[CHECKCOORD[boxpos][2]];
        final int y2 = bspcoord[CHECKCOORD[boxpos][3]];
        int angle1 = pointToAngle(x1, y1) - viewangle;
        int angle2 = pointToAngle(x2, y2) - viewangle;
        final int span = angle1 - angle2;
        if (Integer.compareUnsigned(span, ANG180) >= 0) {
            return true;
        }
        int tspan = angle1 + clipangle;
        if (Integer.compareUnsigned(tspan, 2 * clipangle) > 0) {
            tspan -= 2 * clipangle;
            if (Integer.compareUnsigned(tspan, span) >= 0) {
                return false;
            }
            angle1 = clipangle;
        }
        tspan = clipangle - angle2;
        if (Integer.compareUnsigned(tspan, 2 * clipangle) > 0) {
            tspan -= 2 * clipangle;
            if (Integer.compareUnsigned(tspan, span) >= 0) {
                return false;
            }
            angle2 = -clipangle;
        }
        angle1 = (angle1 + ANG90) >>> ANGLETOFINESHIFT;
        angle2 = (angle2 + ANG90) >>> ANGLETOFINESHIFT;
        final int sx1 = viewangletox[angle1];
        int sx2 = viewangletox[angle2];
        if (sx1 == sx2) {
            return false;
        }
        sx2--;
        int start = 0;
        while (solidLast[start] < sx2) {
            start++;
        }
        return !(sx1 >= solidFirst[start] && sx2 <= solidLast[start]);
    }

    private void subsector(final int num) {
        final Level level = g.level;
        if (num >= level.subsectors.length) {
            return;
        }
        final Subsector sub = level.subsectors[num];
        frontsector = sub.sector;
        int count = sub.numlines;
        int line = sub.firstline;
        if (frontsector.floorheight < viewz) {
            floorplane = findPlane(frontsector.floorheight, frontsector.floorpic, frontsector.lightlevel);
        } else {
            floorplane = null;
        }
        if (frontsector.ceilingheight > viewz || frontsector.ceilingpic == g.skyflatnum) {
            ceilingplane = findPlane(frontsector.ceilingheight, frontsector.ceilingpic, frontsector.lightlevel);
        } else {
            ceilingplane = null;
        }
        addSprites(frontsector);
        while (count-- > 0) {
            addLine(level.segs[line]);
            line++;
        }
    }

    private void renderBSPNode(final int bspnum) {
        if ((bspnum & NF_SUBSECTOR) != 0) {
            if (bspnum == -1) {
                subsector(0);
            } else {
                subsector(bspnum & ~NF_SUBSECTOR);
            }
            return;
        }
        final Node bsp = g.level.nodes[bspnum];
        final int side = Level.pointOnSide(viewx, viewy, bsp);
        renderBSPNode(bsp.children[side]);
        if (checkBBox(bsp.bbox[side ^ 1])) {
            renderBSPNode(bsp.children[side ^ 1]);
        }
    }

    private void setSourceTextureColumn(final int tex, final int col) {
        final Texture t = res.textures[tex];
        dc_source = t.composite[col & t.widthmask];
        dc_sourceHeight = t.height;
    }

    private void renderMaskedSegRange(final DrawSeg ds, final int x1, final int x2) {
        curline = ds.curline;
        frontsector = curline.frontsector;
        backsector = curline.backsector;
        final int texnum = g.texturetranslation[curline.sidedef.midtexture];
        final Texture texture = res.textures[texnum];
        int lightnum = (frontsector.lightlevel >> LIGHTSEGSHIFT) + extralight;
        if (curline.v1.y == curline.v2.y) {
            lightnum--;
        } else if (curline.v1.x == curline.v2.x) {
            lightnum++;
        }
        if (lightnum < 0) {
            walllights = scalelight[0];
        } else if (lightnum >= LIGHTLEVELS) {
            walllights = scalelight[LIGHTLEVELS - 1];
        } else {
            walllights = scalelight[lightnum];
        }
        maskedtexturecol = ds.maskedtexturecol;
        maskedtexturecolOfs = ds.maskedtexturecolOfs;
        rw_scalestep = ds.scalestep;
        spryscale = ds.scale1 + (x1 - ds.x1) * rw_scalestep;
        mfloorclip = ds.sprbottomclip;
        mfloorclipOfs = ds.sprbottomclipOfs;
        mceilingclip = ds.sprtopclip;
        mceilingclipOfs = ds.sprtopclipOfs;
        if ((curline.linedef.flags & ML_DONTPEGBOTTOM) != 0) {
            dc_texturemid = Math.max(frontsector.floorheight, backsector.floorheight);
            dc_texturemid = dc_texturemid + texture.heightFixed - viewz;
        } else {
            dc_texturemid = Math.min(frontsector.ceilingheight, backsector.ceilingheight);
            dc_texturemid = dc_texturemid - viewz;
        }
        dc_texturemid += curline.sidedef.rowoffset;
        if (fixedcolormap != NO_COLORMAP) {
            dc_colormap = fixedcolormap;
        }
        for (dc_x = x1; dc_x <= x2; dc_x++) {
            if (maskedtexturecol[maskedtexturecolOfs + dc_x] != MAXSHORT) {
                if (fixedcolormap == NO_COLORMAP) {
                    int index = spryscale >>> LIGHTSCALESHIFT;
                    if (index >= MAXLIGHTSCALE) {
                        index = MAXLIGHTSCALE - 1;
                    }
                    dc_colormap = walllights[index];
                }
                sprtopscreen = centeryfrac - mul(dc_texturemid, spryscale);
                dc_iscale = (int) (0xffffffffL / (spryscale & 0xffffffffL));
                final Patch.Column col = texture.masked[maskedtexturecol[maskedtexturecolOfs + dc_x] & texture.widthmask];
                drawMaskedColumn(col);
                maskedtexturecol[maskedtexturecolOfs + dc_x] = MAXSHORT;
            }
            spryscale += rw_scalestep;
        }
    }

    private void renderSegLoop() {
        int texturecolumn = 0;
        for (; rw_x < rw_stopx; rw_x++) {
            int yl = (topfrac + HEIGHTUNIT - 1) >> HEIGHTBITS;
            if (yl < ceilingclip[rw_x] + 1) {
                yl = ceilingclip[rw_x] + 1;
            }
            if (markceiling) {
                final int top = ceilingclip[rw_x] + 1;
                int bottom = yl - 1;
                if (bottom >= floorclip[rw_x]) {
                    bottom = floorclip[rw_x] - 1;
                }
                if (top <= bottom) {
                    ceilingplane.top[rw_x + 1] = top;
                    ceilingplane.bottom[rw_x + 1] = bottom;
                }
            }
            int yh = bottomfrac >> HEIGHTBITS;
            if (yh >= floorclip[rw_x]) {
                yh = floorclip[rw_x] - 1;
            }
            if (markfloor) {
                int top = yh + 1;
                final int bottom = floorclip[rw_x] - 1;
                if (top <= ceilingclip[rw_x]) {
                    top = ceilingclip[rw_x] + 1;
                }
                if (top <= bottom) {
                    floorplane.top[rw_x + 1] = top;
                    floorplane.bottom[rw_x + 1] = bottom;
                }
            }
            if (segtextured) {
                final int angle = (rw_centerangle + xtoviewangle[rw_x]) >>> ANGLETOFINESHIFT;
                texturecolumn = rw_offset - mul(finetangent[angle & (FINEANGLES / 2 - 1)], rw_distance);
                texturecolumn >>= FRACBITS;
                int index = rw_scale >>> LIGHTSCALESHIFT;
                if (index >= MAXLIGHTSCALE) {
                    index = MAXLIGHTSCALE - 1;
                }
                dc_colormap = walllights[index];
                dc_x = rw_x;
                dc_iscale = (int) (0xffffffffL / (rw_scale & 0xffffffffL));
            }
            if (midtexture != 0) {
                dc_yl = yl;
                dc_yh = yh;
                dc_texturemid = rw_midtexturemid;
                setSourceTextureColumn(midtexture, texturecolumn);
                drawColumn();
                ceilingclip[rw_x] = (short) viewheight;
                floorclip[rw_x] = -1;
            } else {
                if (toptexture != 0) {
                    int mid = pixhigh >> HEIGHTBITS;
                    pixhigh += pixhighstep;
                    if (mid >= floorclip[rw_x]) {
                        mid = floorclip[rw_x] - 1;
                    }
                    if (mid >= yl) {
                        dc_yl = yl;
                        dc_yh = mid;
                        dc_texturemid = rw_toptexturemid;
                        setSourceTextureColumn(toptexture, texturecolumn);
                        drawColumn();
                        ceilingclip[rw_x] = (short) mid;
                    } else {
                        ceilingclip[rw_x] = (short) (yl - 1);
                    }
                } else if (markceiling) {
                    ceilingclip[rw_x] = (short) (yl - 1);
                }
                if (bottomtexture != 0) {
                    int mid = (pixlow + HEIGHTUNIT - 1) >> HEIGHTBITS;
                    pixlow += pixlowstep;
                    if (mid <= ceilingclip[rw_x]) {
                        mid = ceilingclip[rw_x] + 1;
                    }
                    if (mid <= yh) {
                        dc_yl = mid;
                        dc_yh = yh;
                        dc_texturemid = rw_bottomtexturemid;
                        setSourceTextureColumn(bottomtexture, texturecolumn);
                        drawColumn();
                        floorclip[rw_x] = (short) mid;
                    } else {
                        floorclip[rw_x] = (short) (yh + 1);
                    }
                } else if (markfloor) {
                    floorclip[rw_x] = (short) (yh + 1);
                }
                if (maskedtexture) {
                    maskedtexturecol[maskedtexturecolOfs + rw_x] = (short) texturecolumn;
                }
            }
            rw_scale += rw_scalestep;
            topfrac += topstep;
            bottomfrac += bottomstep;
        }
    }

    private void ensureOpenings(final int extra) {
        if (lastopening + extra > openings.length) {
            final short[] grown = new short[Math.max(openings.length * 2, lastopening + extra)];
            System.arraycopy(openings, 0, grown, 0, openings.length);
            // drawsegs keep references to the old array, which stays valid for them
            openings = grown;
        }
    }

    private void storeWallRange(final int start, final int stop) {
        if (start >= viewwidth || start > stop) {
            return;
        }
        if (ds_p == drawsegs.length) {
            final DrawSeg[] grown = newDrawSegs(drawsegs.length * 2);
            System.arraycopy(drawsegs, 0, grown, 0, drawsegs.length);
            drawsegs = grown;
        }
        final DrawSeg ds = drawsegs[ds_p];
        sidedef = curline.sidedef;
        linedef = curline.linedef;
        linedef.flags |= ML_MAPPED;
        rw_normalangle = curline.angle + ANG90;
        int offsetangle = Math.abs(rw_normalangle - rw_angle1);
        if (Integer.compareUnsigned(offsetangle, ANG90) > 0) {
            offsetangle = ANG90;
        }
        final int distangle = ANG90 - offsetangle;
        final int hyp = pointToDist(curline.v1.x, curline.v1.y);
        int sineval = finesine[distangle >>> ANGLETOFINESHIFT];
        rw_distance = mul(hyp, sineval);
        ds.x1 = rw_x = start;
        ds.x2 = stop;
        ds.curline = curline;
        rw_stopx = stop + 1;
        ds.scale1 = rw_scale = scaleFromGlobalAngle(viewangle + xtoviewangle[start]);
        if (stop > start) {
            ds.scale2 = scaleFromGlobalAngle(viewangle + xtoviewangle[stop]);
            ds.scalestep = rw_scalestep = (ds.scale2 - rw_scale) / (stop - start);
        } else {
            ds.scale2 = ds.scale1;
        }
        worldtop = frontsector.ceilingheight - viewz;
        worldbottom = frontsector.floorheight - viewz;
        midtexture = toptexture = bottomtexture = 0;
        maskedtexture = false;
        ds.maskedtexturecol = null;
        ds.maskedtexturecolOfs = 0;
        if (backsector == null) {
            midtexture = g.texturetranslation[sidedef.midtexture];
            markfloor = markceiling = true;
            if ((linedef.flags & ML_DONTPEGBOTTOM) != 0) {
                final int vtop = frontsector.floorheight + g.textureheight(sidedef.midtexture);
                rw_midtexturemid = vtop - viewz;
            } else {
                rw_midtexturemid = worldtop;
            }
            rw_midtexturemid += sidedef.rowoffset;
            ds.silhouette = SIL_BOTH;
            ds.sprtopclip = screenheightarray;
            ds.sprtopclipOfs = 0;
            ds.sprbottomclip = negonearray;
            ds.sprbottomclipOfs = 0;
            ds.bsilheight = MAXINT;
            ds.tsilheight = MININT;
        } else {
            ds.sprtopclip = ds.sprbottomclip = null;
            ds.silhouette = 0;
            if (frontsector.floorheight > backsector.floorheight) {
                ds.silhouette = SIL_BOTTOM;
                ds.bsilheight = frontsector.floorheight;
            } else if (backsector.floorheight > viewz) {
                ds.silhouette = SIL_BOTTOM;
                ds.bsilheight = MAXINT;
            }
            if (frontsector.ceilingheight < backsector.ceilingheight) {
                ds.silhouette |= SIL_TOP;
                ds.tsilheight = frontsector.ceilingheight;
            } else if (backsector.ceilingheight < viewz) {
                ds.silhouette |= SIL_TOP;
                ds.tsilheight = MININT;
            }
            if (backsector.ceilingheight <= frontsector.floorheight) {
                ds.sprbottomclip = negonearray;
                ds.sprbottomclipOfs = 0;
                ds.bsilheight = MAXINT;
                ds.silhouette |= SIL_BOTTOM;
            }
            if (backsector.floorheight >= frontsector.ceilingheight) {
                ds.sprtopclip = screenheightarray;
                ds.sprtopclipOfs = 0;
                ds.tsilheight = MININT;
                ds.silhouette |= SIL_TOP;
            }
            worldhigh = backsector.ceilingheight - viewz;
            worldlow = backsector.floorheight - viewz;
            if (frontsector.ceilingpic == g.skyflatnum && backsector.ceilingpic == g.skyflatnum) {
                worldtop = worldhigh;
            }
            markfloor = worldlow != worldbottom || backsector.floorpic != frontsector.floorpic
                    || backsector.lightlevel != frontsector.lightlevel;
            markceiling = worldhigh != worldtop || backsector.ceilingpic != frontsector.ceilingpic
                    || backsector.lightlevel != frontsector.lightlevel;
            if (backsector.ceilingheight <= frontsector.floorheight || backsector.floorheight >= frontsector.ceilingheight) {
                markceiling = markfloor = true;
            }
            if (worldhigh < worldtop) {
                toptexture = g.texturetranslation[sidedef.toptexture];
                if ((linedef.flags & ML_DONTPEGTOP) != 0) {
                    rw_toptexturemid = worldtop;
                } else {
                    final int vtop = backsector.ceilingheight + g.textureheight(sidedef.toptexture);
                    rw_toptexturemid = vtop - viewz;
                }
            }
            if (worldlow > worldbottom) {
                bottomtexture = g.texturetranslation[sidedef.bottomtexture];
                if ((linedef.flags & ML_DONTPEGBOTTOM) != 0) {
                    rw_bottomtexturemid = worldtop;
                } else {
                    rw_bottomtexturemid = worldlow;
                }
            }
            rw_toptexturemid += sidedef.rowoffset;
            rw_bottomtexturemid += sidedef.rowoffset;
            if (sidedef.midtexture != 0) {
                maskedtexture = true;
                ensureOpenings(rw_stopx - rw_x);
                ds.maskedtexturecol = maskedtexturecol = openings;
                ds.maskedtexturecolOfs = maskedtexturecolOfs = lastopening - rw_x;
                lastopening += rw_stopx - rw_x;
            }
        }
        segtextured = midtexture != 0 || toptexture != 0 || bottomtexture != 0 || maskedtexture;
        if (segtextured) {
            offsetangle = rw_normalangle - rw_angle1;
            if (Integer.compareUnsigned(offsetangle, ANG180) > 0) {
                offsetangle = -offsetangle;
            }
            if (Integer.compareUnsigned(offsetangle, ANG90) > 0) {
                offsetangle = ANG90;
            }
            sineval = finesine[offsetangle >>> ANGLETOFINESHIFT];
            rw_offset = mul(hyp, sineval);
            if (Integer.compareUnsigned(rw_normalangle - rw_angle1, ANG180) < 0) {
                rw_offset = -rw_offset;
            }
            rw_offset += sidedef.textureoffset + curline.offset;
            rw_centerangle = ANG90 + viewangle - rw_normalangle;
            if (fixedcolormap == NO_COLORMAP) {
                int lightnum = (frontsector.lightlevel >> LIGHTSEGSHIFT) + extralight;
                if (curline.v1.y == curline.v2.y) {
                    lightnum--;
                } else if (curline.v1.x == curline.v2.x) {
                    lightnum++;
                }
                if (lightnum < 0) {
                    walllights = scalelight[0];
                } else if (lightnum >= LIGHTLEVELS) {
                    walllights = scalelight[LIGHTLEVELS - 1];
                } else {
                    walllights = scalelight[lightnum];
                }
            }
        }
        if (frontsector.floorheight >= viewz) {
            markfloor = false;
        }
        if (frontsector.ceilingheight <= viewz && frontsector.ceilingpic != g.skyflatnum) {
            markceiling = false;
        }
        worldtop >>= 4;
        worldbottom >>= 4;
        topstep = -mul(rw_scalestep, worldtop);
        topfrac = (centeryfrac >> 4) - mul(worldtop, rw_scale);
        bottomstep = -mul(rw_scalestep, worldbottom);
        bottomfrac = (centeryfrac >> 4) - mul(worldbottom, rw_scale);
        if (backsector != null) {
            worldhigh >>= 4;
            worldlow >>= 4;
            if (worldhigh < worldtop) {
                pixhigh = (centeryfrac >> 4) - mul(worldhigh, rw_scale);
                pixhighstep = -mul(rw_scalestep, worldhigh);
            }
            if (worldlow > worldbottom) {
                pixlow = (centeryfrac >> 4) - mul(worldlow, rw_scale);
                pixlowstep = -mul(rw_scalestep, worldlow);
            }
        }
        if (markceiling) {
            ceilingplane = checkPlane(ceilingplane, rw_x, rw_stopx - 1);
        }
        if (markfloor) {
            floorplane = checkPlane(floorplane, rw_x, rw_stopx - 1);
        }
        renderSegLoop();
        if (((ds.silhouette & SIL_TOP) != 0 || maskedtexture) && ds.sprtopclip == null) {
            ensureOpenings(rw_stopx - start);
            System.arraycopy(ceilingclip, start, openings, lastopening, rw_stopx - start);
            ds.sprtopclip = openings;
            ds.sprtopclipOfs = lastopening - start;
            lastopening += rw_stopx - start;
        }
        if (((ds.silhouette & SIL_BOTTOM) != 0 || maskedtexture) && ds.sprbottomclip == null) {
            ensureOpenings(rw_stopx - start);
            System.arraycopy(floorclip, start, openings, lastopening, rw_stopx - start);
            ds.sprbottomclip = openings;
            ds.sprbottomclipOfs = lastopening - start;
            lastopening += rw_stopx - start;
        }
        if (maskedtexture && (ds.silhouette & SIL_TOP) == 0) {
            ds.silhouette |= SIL_TOP;
            ds.tsilheight = MININT;
        }
        if (maskedtexture && (ds.silhouette & SIL_BOTTOM) == 0) {
            ds.silhouette |= SIL_BOTTOM;
            ds.bsilheight = MAXINT;
        }
        ds_p++;
    }

    private void mapPlane(final int y, final int x1, final int x2) {
        if (x2 < x1 || x1 < 0 || x2 >= viewwidth || y < 0 || y >= viewheight) {
            return;
        }
        final int distance;
        if (planeheight != cachedheight[y]) {
            cachedheight[y] = planeheight;
            distance = cacheddistance[y] = mul(planeheight, yslope[y]);
            ds_xstep = cachedxstep[y] = mul(distance, basexscale);
            ds_ystep = cachedystep[y] = mul(distance, baseyscale);
        } else {
            distance = cacheddistance[y];
            ds_xstep = cachedxstep[y];
            ds_ystep = cachedystep[y];
        }
        final int length = mul(distance, distscale[x1]);
        final int angle = (viewangle + xtoviewangle[x1]) >>> ANGLETOFINESHIFT;
        ds_xfrac = viewx + mul(finecosine(angle), length);
        ds_yfrac = -viewy - mul(finesine[angle], length);
        if (fixedcolormap != NO_COLORMAP) {
            ds_colormap = fixedcolormap;
        } else {
            int index = distance >>> LIGHTZSHIFT;
            if (index >= MAXLIGHTZ) {
                index = MAXLIGHTZ - 1;
            }
            ds_colormap = planezlight[index];
        }
        ds_y = y;
        ds_x1 = x1;
        ds_x2 = x2;
        drawSpan();
    }

    private void clearPlanes() {
        for (int i = 0; i < viewwidth; i++) {
            floorclip[i] = (short) viewheight;
            ceilingclip[i] = -1;
        }
        lastvisplane = 0;
        lastopening = 0;
        java.util.Arrays.fill(cachedheight, 0);
        final int angle = (viewangle - ANG90) >>> ANGLETOFINESHIFT;
        basexscale = div(finecosine(angle), centerxfrac);
        baseyscale = -div(finesine[angle], centerxfrac);
    }

    private Visplane newVisplane() {
        if (lastvisplane == visplanes.length) {
            final Visplane[] grown = newVisplanes(visplanes.length * 2);
            System.arraycopy(visplanes, 0, grown, 0, visplanes.length);
            visplanes = grown;
        }
        return visplanes[lastvisplane++];
    }

    private Visplane findPlane(int height, final int picnum, int lightlevel) {
        if (picnum == g.skyflatnum) {
            height = 0;
            lightlevel = 0;
        }
        for (int i = 0; i < lastvisplane; i++) {
            final Visplane check = visplanes[i];
            if (height == check.height && picnum == check.picnum && lightlevel == check.lightlevel) {
                return check;
            }
        }
        final Visplane check = newVisplane();
        check.height = height;
        check.picnum = picnum;
        check.lightlevel = lightlevel;
        check.minx = SCREENWIDTH;
        check.maxx = -1;
        java.util.Arrays.fill(check.top, UNSET);
        return check;
    }

    private Visplane checkPlane(Visplane pl, final int start, final int stop) {
        final int intrl;
        final int intrh;
        final int unionl;
        final int unionh;
        if (start < pl.minx) {
            intrl = pl.minx;
            unionl = start;
        } else {
            unionl = pl.minx;
            intrl = start;
        }
        if (stop > pl.maxx) {
            intrh = pl.maxx;
            unionh = stop;
        } else {
            unionh = pl.maxx;
            intrh = stop;
        }
        int x;
        for (x = intrl; x <= intrh; x++) {
            if (pl.top[x + 1] != UNSET) {
                break;
            }
        }
        if (x > intrh) {
            pl.minx = unionl;
            pl.maxx = unionh;
            return pl;
        }
        final int height = pl.height;
        final int picnum = pl.picnum;
        final int lightlevel = pl.lightlevel;
        pl = newVisplane();
        pl.height = height;
        pl.picnum = picnum;
        pl.lightlevel = lightlevel;
        pl.minx = start;
        pl.maxx = stop;
        java.util.Arrays.fill(pl.top, UNSET);
        return pl;
    }

    private void makeSpans(final int x, int t1, int b1, int t2, int b2) {
        while (t1 < t2 && t1 <= b1) {
            mapPlane(t1, spanstart[t1], x - 1);
            t1++;
        }
        while (b1 > b2 && b1 >= t1) {
            mapPlane(b1, spanstart[b1], x - 1);
            b1--;
        }
        while (t2 < t1 && t2 <= b2) {
            spanstart[t2] = x;
            t2++;
        }
        while (b2 > b1 && b2 >= t2) {
            spanstart[b2] = x;
            b2--;
        }
    }

    private void drawPlanes() {
        for (int p = 0; p < lastvisplane; p++) {
            final Visplane pl = visplanes[p];
            if (pl.minx > pl.maxx) {
                continue;
            }
            if (pl.picnum == g.skyflatnum) {
                dc_iscale = pspriteiscale >> detailshift;
                dc_colormap = 0;
                dc_texturemid = g.skytexturemid;
                final Texture sky = res.textures[g.skytexture];
                for (int x = pl.minx; x <= pl.maxx; x++) {
                    dc_yl = pl.top[x + 1];
                    dc_yh = pl.bottom[x + 1];
                    if (dc_yl <= dc_yh && dc_yl != UNSET) {
                        final int angle = (viewangle + xtoviewangle[x]) >>> ANGLETOSKYSHIFT;
                        dc_x = x;
                        dc_source = sky.composite[angle & sky.widthmask];
                        dc_sourceHeight = sky.height;
                        drawColumn();
                    }
                }
                continue;
            }
            final int flat = g.flattranslation[pl.picnum];
            ds_source = res.flats[flat < res.flats.length ? flat : 0];
            planeheight = Math.abs(pl.height - viewz);
            int light = (pl.lightlevel >> LIGHTSEGSHIFT) + extralight;
            if (light >= LIGHTLEVELS) {
                light = LIGHTLEVELS - 1;
            }
            if (light < 0) {
                light = 0;
            }
            planezlight = zlight[light];
            pl.top[pl.maxx + 2] = UNSET;
            pl.top[pl.minx] = UNSET;
            final int stop = pl.maxx + 1;
            for (int x = pl.minx; x <= stop; x++) {
                makeSpans(x, pl.top[x], pl.bottom[x], pl.top[x + 1], pl.bottom[x + 1]);
            }
        }
    }

    private VisSprite newVisSprite() {
        if (vissprite_p == vissprites.length) {
            final VisSprite[] grown = newVisSprites(vissprites.length * 2);
            System.arraycopy(vissprites, 0, grown, 0, vissprites.length);
            vissprites = grown;
        }
        return vissprites[vissprite_p++];
    }

    private void drawMaskedColumn(final Patch.Column column) {
        final int basetexturemid = dc_texturemid;
        for (int p = 0; p < column.tops().length; p++) {
            final int topdelta = column.tops()[p];
            final byte[] post = column.posts()[p];
            final int topscreen = sprtopscreen + spryscale * topdelta;
            final int bottomscreen = topscreen + spryscale * post.length;
            dc_yl = (topscreen + FRACUNIT - 1) >> FRACBITS;
            dc_yh = (bottomscreen - 1) >> FRACBITS;
            final int fclip = mfloorclip[mfloorclipOfs + dc_x];
            final int cclip = mceilingclip[mceilingclipOfs + dc_x];
            if (dc_yh >= fclip) {
                dc_yh = fclip - 1;
            }
            if (dc_yl <= cclip) {
                dc_yl = cclip + 1;
            }
            if (dc_yl <= dc_yh) {
                dc_source = post;
                dc_sourceHeight = Math.max(1, post.length);
                dc_texturemid = basetexturemid - (topdelta << FRACBITS);
                if (colfunc == COL_NORMAL) {
                    drawMaskedPost();
                } else {
                    colfunc();
                }
            }
        }
        dc_texturemid = basetexturemid;
    }

    /**
     * {@code R_DrawColumn} for a post: like vanilla it reads the post without wrapping.
     */
    private void drawMaskedPost() {
        int count = dc_yh - dc_yl;
        if (count < 0) {
            return;
        }
        if (dc_x < 0 || dc_x >= viewwidth || dc_yl < 0 || dc_yh >= viewheight) {
            return;
        }
        final byte[] scr = screen;
        final byte[] src = dc_source;
        final int last = src.length - 1;
        final int fracstep = dc_iscale;
        int frac = dc_texturemid + (dc_yl - centery) * fracstep;
        final int x = detailshift == 0 ? dc_x : dc_x << 1;
        int dest = ylookup[dc_yl] + columnofs[x];
        do {
            int idx = frac >> FRACBITS;
            if (idx < 0) {
                idx = 0;
            } else if (idx > last) {
                idx = last;
            }
            final byte c = colormaps[dc_colormap + (src[idx] & 0xff)];
            scr[dest] = c;
            if (detailshift != 0) {
                scr[dest + 1] = c;
            }
            dest += SCREENWIDTH;
            frac += fracstep;
        } while (count-- > 0);
    }

    private void drawVisSprite(final VisSprite vis) {
        final Patch patch = res.spritePatches[vis.patch];
        dc_colormap = vis.colormap;
        if (dc_colormap == NO_COLORMAP) {
            colfunc = COL_FUZZ;
        } else if ((vis.mobjflags & MF_TRANSLATION) != 0) {
            colfunc = COL_TRANSLATED;
            dc_translation = res.translations[((vis.mobjflags & MF_TRANSLATION) >> MF_TRANSSHIFT) - 1];
        }
        dc_iscale = Math.abs(vis.xiscale) >> detailshift;
        dc_texturemid = vis.texturemid;
        int frac = vis.startfrac;
        spryscale = vis.scale;
        sprtopscreen = centeryfrac - mul(dc_texturemid, spryscale);
        for (dc_x = vis.x1; dc_x <= vis.x2; dc_x++, frac += vis.xiscale) {
            final int texturecolumn = frac >> FRACBITS;
            if (texturecolumn < 0 || texturecolumn >= patch.width) {
                continue;
            }
            drawMaskedColumn(patch.columns[texturecolumn]);
        }
        colfunc = COL_NORMAL;
    }

    private void projectSprite(final Mobj thing) {
        final int trX = thing.x - viewx;
        final int trY = thing.y - viewy;
        int gxt = mul(trX, viewcos);
        int gyt = -mul(trY, viewsin);
        final int tz = gxt - gyt;
        if (tz < MINZ) {
            return;
        }
        final int xscale = div(projection, tz);
        gxt = -mul(trX, viewsin);
        gyt = mul(trY, viewcos);
        int tx = -(gyt + gxt);
        if (Math.abs(tx) > (tz << 2)) {
            return;
        }
        if (thing.sprite < 0 || thing.sprite >= res.sprites.length) {
            return;
        }
        final RenderData.SpriteDef sprdef = res.sprites[thing.sprite];
        final int frameIndex = thing.frame & FF_FRAMEMASK;
        if (frameIndex >= sprdef.frames().length) {
            return;
        }
        final RenderData.SpriteFrame sprframe = sprdef.frames()[frameIndex];
        final int lump;
        final boolean flip;
        if (sprframe.rotate()) {
            final int ang = pointToAngle(thing.x, thing.y);
            final int rot = (ang - thing.angle + (ANG45 / 2) * 9) >>> 29;
            lump = sprframe.lump()[rot];
            flip = sprframe.flip()[rot];
        } else {
            lump = sprframe.lump()[0];
            flip = sprframe.flip()[0];
        }
        if (lump < 0 || lump >= res.spritePatches.length) {
            return;
        }
        tx -= res.spriteoffset[lump];
        final int x1 = (centerxfrac + mul(tx, xscale)) >> FRACBITS;
        if (x1 > viewwidth) {
            return;
        }
        tx += res.spritewidth[lump];
        final int x2 = ((centerxfrac + mul(tx, xscale)) >> FRACBITS) - 1;
        if (x2 < 0) {
            return;
        }
        final VisSprite vis = newVisSprite();
        vis.mobjflags = thing.flags;
        vis.scale = xscale << detailshift;
        vis.gx = thing.x;
        vis.gy = thing.y;
        vis.gz = thing.z;
        vis.gzt = thing.z + res.spritetopoffset[lump];
        vis.texturemid = vis.gzt - viewz;
        vis.x1 = Math.max(x1, 0);
        vis.x2 = x2 >= viewwidth ? viewwidth - 1 : x2;
        final int iscale = div(FRACUNIT, xscale);
        if (flip) {
            vis.startfrac = res.spritewidth[lump] - 1;
            vis.xiscale = -iscale;
        } else {
            vis.startfrac = 0;
            vis.xiscale = iscale;
        }
        if (vis.x1 > x1) {
            vis.startfrac += vis.xiscale * (vis.x1 - x1);
        }
        vis.patch = lump;
        if ((thing.flags & MF_SHADOW) != 0) {
            vis.colormap = NO_COLORMAP;
        } else if (fixedcolormap != NO_COLORMAP) {
            vis.colormap = fixedcolormap;
        } else if ((thing.frame & FF_FULLBRIGHT) != 0) {
            vis.colormap = 0;
        } else {
            int index = xscale >> (LIGHTSCALESHIFT - detailshift);
            if (index >= MAXLIGHTSCALE) {
                index = MAXLIGHTSCALE - 1;
            }
            vis.colormap = spritelights[index];
        }
    }

    private void addSprites(final Sector sec) {
        if (sec.validcount == g.validcount) {
            return;
        }
        sec.validcount = g.validcount;
        final int lightnum = (sec.lightlevel >> LIGHTSEGSHIFT) + extralight;
        if (lightnum < 0) {
            spritelights = scalelight[0];
        } else if (lightnum >= LIGHTLEVELS) {
            spritelights = scalelight[LIGHTLEVELS - 1];
        } else {
            spritelights = scalelight[lightnum];
        }
        for (Mobj thing = sec.thinglist; thing != null; thing = thing.snext) {
            projectSprite(thing);
        }
    }

    private void drawPSprite(final PspDef psp) {
        final State state = psp.state;
        if (state.sprite() < 0 || state.sprite() >= res.sprites.length) {
            return;
        }
        final RenderData.SpriteDef sprdef = res.sprites[state.sprite()];
        final int frameIndex = state.frame() & FF_FRAMEMASK;
        if (frameIndex >= sprdef.frames().length) {
            return;
        }
        final RenderData.SpriteFrame sprframe = sprdef.frames()[frameIndex];
        final int lump = sprframe.lump()[0];
        final boolean flip = sprframe.flip()[0];
        if (lump < 0 || lump >= res.spritePatches.length) {
            return;
        }
        int tx = psp.sx - 160 * FRACUNIT;
        tx -= res.spriteoffset[lump];
        final int x1 = (centerxfrac + mul(tx, pspritescale)) >> FRACBITS;
        if (x1 > viewwidth) {
            return;
        }
        tx += res.spritewidth[lump];
        final int x2 = ((centerxfrac + mul(tx, pspritescale)) >> FRACBITS) - 1;
        if (x2 < 0) {
            return;
        }
        final VisSprite vis = new VisSprite();
        vis.mobjflags = 0;
        vis.texturemid = (BASEYCENTER << FRACBITS) + FRACUNIT / 2 - (psp.sy - res.spritetopoffset[lump]);
        vis.x1 = Math.max(x1, 0);
        vis.x2 = x2 >= viewwidth ? viewwidth - 1 : x2;
        vis.scale = pspritescale << detailshift;
        if (flip) {
            vis.xiscale = -pspriteiscale;
            vis.startfrac = res.spritewidth[lump] - 1;
        } else {
            vis.xiscale = pspriteiscale;
            vis.startfrac = 0;
        }
        if (vis.x1 > x1) {
            vis.startfrac += vis.xiscale * (vis.x1 - x1);
        }
        vis.patch = lump;
        if (viewplayer.powers[pw_invisibility] > 4 * 32 || (viewplayer.powers[pw_invisibility] & 8) != 0) {
            vis.colormap = NO_COLORMAP;
        } else if (fixedcolormap != NO_COLORMAP) {
            vis.colormap = fixedcolormap;
        } else if ((state.frame() & FF_FULLBRIGHT) != 0) {
            vis.colormap = 0;
        } else {
            vis.colormap = spritelights[MAXLIGHTSCALE - 1];
        }
        drawVisSprite(vis);
    }

    private void drawPlayerSprites() {
        final int lightnum = (viewplayer.mo.subsector.sector.lightlevel >> LIGHTSEGSHIFT) + extralight;
        if (lightnum < 0) {
            spritelights = scalelight[0];
        } else if (lightnum >= LIGHTLEVELS) {
            spritelights = scalelight[LIGHTLEVELS - 1];
        } else {
            spritelights = scalelight[lightnum];
        }
        mfloorclip = screenheightarray;
        mfloorclipOfs = 0;
        mceilingclip = negonearray;
        mceilingclipOfs = 0;
        for (int i = 0; i < NUMPSPRITES; i++) {
            final PspDef psp = viewplayer.psprites[i];
            if (psp.state != null) {
                drawPSprite(psp);
            }
        }
    }

    private void sortVisSprites() {
        final int count = vissprite_p;
        unsorted.next = unsorted.prev = unsorted;
        if (count == 0) {
            return;
        }
        for (int i = 0; i < count; i++) {
            vissprites[i].next = i + 1 < count ? vissprites[i + 1] : unsorted;
            vissprites[i].prev = i > 0 ? vissprites[i - 1] : unsorted;
        }
        unsorted.next = vissprites[0];
        unsorted.prev = vissprites[count - 1];
        vsprsortedhead.next = vsprsortedhead.prev = vsprsortedhead;
        for (int i = 0; i < count; i++) {
            int bestscale = MAXINT;
            VisSprite best = unsorted.next;
            for (VisSprite ds = unsorted.next; ds != unsorted; ds = ds.next) {
                if (ds.scale < bestscale) {
                    bestscale = ds.scale;
                    best = ds;
                }
            }
            best.next.prev = best.prev;
            best.prev.next = best.next;
            best.next = vsprsortedhead;
            best.prev = vsprsortedhead.prev;
            vsprsortedhead.prev.next = best;
            vsprsortedhead.prev = best;
        }
    }

    private void drawSprite(final VisSprite spr) {
        for (int x = spr.x1; x <= spr.x2; x++) {
            clipbot[x] = cliptop[x] = -2;
        }
        for (int i = ds_p - 1; i >= 0; i--) {
            final DrawSeg ds = drawsegs[i];
            if (ds.x1 > spr.x2 || ds.x2 < spr.x1 || (ds.silhouette == 0 && ds.maskedtexturecol == null)) {
                continue;
            }
            final int r1 = Math.max(ds.x1, spr.x1);
            final int r2 = Math.min(ds.x2, spr.x2);
            final int lowscale;
            final int scale;
            if (ds.scale1 > ds.scale2) {
                lowscale = ds.scale2;
                scale = ds.scale1;
            } else {
                lowscale = ds.scale1;
                scale = ds.scale2;
            }
            if (scale < spr.scale || (lowscale < spr.scale && pointOnSegSide(spr.gx, spr.gy, ds.curline) == 0)) {
                if (ds.maskedtexturecol != null) {
                    renderMaskedSegRange(ds, r1, r2);
                }
                continue;
            }
            int silhouette = ds.silhouette;
            if (spr.gz >= ds.bsilheight) {
                silhouette &= ~SIL_BOTTOM;
            }
            if (spr.gzt <= ds.tsilheight) {
                silhouette &= ~SIL_TOP;
            }
            if (silhouette == 1) {
                for (int x = r1; x <= r2; x++) {
                    if (clipbot[x] == -2) {
                        clipbot[x] = ds.sprbottomclip[ds.sprbottomclipOfs + x];
                    }
                }
            } else if (silhouette == 2) {
                for (int x = r1; x <= r2; x++) {
                    if (cliptop[x] == -2) {
                        cliptop[x] = ds.sprtopclip[ds.sprtopclipOfs + x];
                    }
                }
            } else if (silhouette == 3) {
                for (int x = r1; x <= r2; x++) {
                    if (clipbot[x] == -2) {
                        clipbot[x] = ds.sprbottomclip[ds.sprbottomclipOfs + x];
                    }
                    if (cliptop[x] == -2) {
                        cliptop[x] = ds.sprtopclip[ds.sprtopclipOfs + x];
                    }
                }
            }
        }
        for (int x = spr.x1; x <= spr.x2; x++) {
            if (clipbot[x] == -2) {
                clipbot[x] = (short) viewheight;
            }
            if (cliptop[x] == -2) {
                cliptop[x] = -1;
            }
        }
        mfloorclip = clipbot;
        mfloorclipOfs = 0;
        mceilingclip = cliptop;
        mceilingclipOfs = 0;
        drawVisSprite(spr);
    }

    private void drawMasked() {
        sortVisSprites();
        if (vissprite_p > 0) {
            for (VisSprite spr = vsprsortedhead.next; spr != vsprsortedhead; spr = spr.next) {
                drawSprite(spr);
            }
        }
        for (int i = ds_p - 1; i >= 0; i--) {
            final DrawSeg ds = drawsegs[i];
            if (ds.maskedtexturecol != null) {
                renderMaskedSegRange(ds, ds.x1, ds.x2);
            }
        }
        drawPlayerSprites();
    }

    /**
     * Fills the background around a reduced view and draws the bevel ({@code R_FillBackScreen}).
     *
     * @param back  the background screen to fill
     * @param video the patch drawer
     */
    public void fillBackScreen(final byte[] back, final Video video) {
        checkViewSize();
        if (scaledviewwidth == SCREENWIDTH) {
            return;
        }
        final String name = g.gamemode == commercial ? "GRNROCK" : "FLOOR7_2";
        final int flat = res.checkFlatNumForName(name);
        final byte[] src = flat >= 0 ? res.flats[flat] : new byte[4096];
        int dest = 0;
        for (int y = 0; y < SCREENHEIGHT - SBARHEIGHT; y++) {
            for (int x = 0; x < SCREENWIDTH; x++) {
                back[dest++] = src[((y & 63) << 6) + (x & 63)];
            }
        }
        for (int x = 0; x < scaledviewwidth; x += 8) {
            video.drawPatch(back, viewwindowx + x, viewwindowy - 8, res.patch("BRDR_T"));
        }
        for (int x = 0; x < scaledviewwidth; x += 8) {
            video.drawPatch(back, viewwindowx + x, viewwindowy + viewheight, res.patch("BRDR_B"));
        }
        for (int y = 0; y < viewheight; y += 8) {
            video.drawPatch(back, viewwindowx - 8, viewwindowy + y, res.patch("BRDR_L"));
        }
        for (int y = 0; y < viewheight; y += 8) {
            video.drawPatch(back, viewwindowx + scaledviewwidth, viewwindowy + y, res.patch("BRDR_R"));
        }
        video.drawPatch(back, viewwindowx - 8, viewwindowy - 8, res.patch("BRDR_TL"));
        video.drawPatch(back, viewwindowx + scaledviewwidth, viewwindowy - 8, res.patch("BRDR_TR"));
        video.drawPatch(back, viewwindowx - 8, viewwindowy + viewheight, res.patch("BRDR_BL"));
        video.drawPatch(back, viewwindowx + scaledviewwidth, viewwindowy + viewheight, res.patch("BRDR_BR"));
    }

    /**
     * Copies the border from the background screen ({@code R_DrawViewBorder}).
     *
     * @param target the screen
     * @param back   the background filled by {@link #fillBackScreen}
     */
    public void drawViewBorder(final byte[] target, final byte[] back) {
        if (scaledviewwidth == SCREENWIDTH) {
            return;
        }
        final int limit = (SCREENHEIGHT - SBARHEIGHT) * SCREENWIDTH;
        for (int i = 0; i < limit; i++) {
            final int y = i / SCREENWIDTH;
            final int x = i % SCREENWIDTH;
            if (y < viewwindowy || y >= viewwindowy + viewheight || x < viewwindowx || x >= viewwindowx + scaledviewwidth) {
                target[i] = back[i];
            }
        }
    }

    /**
     * {@code drawseg_t}; clip arrays are references into the openings buffer or the static arrays.
     */
    private static final class DrawSeg {
        Seg curline;
        int x1;
        int x2;
        int scale1;
        int scale2;
        int scalestep;
        int silhouette;
        int bsilheight;
        int tsilheight;
        short @Nullable [] sprtopclip;
        int sprtopclipOfs;
        short @Nullable [] sprbottomclip;
        int sprbottomclipOfs;
        short @Nullable [] maskedtexturecol;
        int maskedtexturecolOfs;
    }

    /**
     * {@code visplane_t}; top and bottom are offset by one so index -1 and width are valid.
     */
    private static final class Visplane {
        final int[] top = new int[SCREENWIDTH + 2];
        final int[] bottom = new int[SCREENWIDTH + 2];
        int height;
        int picnum;
        int lightlevel;
        int minx;
        int maxx;
    }

    /**
     * {@code vissprite_t}.
     */
    private static final class VisSprite {
        VisSprite prev;
        VisSprite next;
        int x1;
        int x2;
        int gx;
        int gy;
        int gz;
        int gzt;
        int startfrac;
        int scale;
        int xiscale;
        int texturemid;
        int patch;
        int colormap;
        int mobjflags;
    }
}
