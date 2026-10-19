// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine.ui;

import fr.euphyllia.fidorial.doom.engine.Game;
import fr.euphyllia.fidorial.doom.engine.Mobj;
import fr.euphyllia.fidorial.doom.engine.Player;
import fr.euphyllia.fidorial.doom.engine.render.Patch;
import fr.euphyllia.fidorial.doom.engine.render.Renderer;
import fr.euphyllia.fidorial.doom.engine.render.Video;
import org.jspecify.annotations.Nullable;

import static fr.euphyllia.fidorial.doom.engine.Defs.GS_FINALE;
import static fr.euphyllia.fidorial.doom.engine.Defs.GS_INTERMISSION;
import static fr.euphyllia.fidorial.doom.engine.Defs.GS_LEVEL;
import static fr.euphyllia.fidorial.doom.engine.Defs.PST_DEAD;
import static fr.euphyllia.fidorial.doom.engine.Defs.SCREENHEIGHT;
import static fr.euphyllia.fidorial.doom.engine.Defs.SCREENWIDTH;

/**
 * Everything one player sees: the 3D view, the status bar, messages, the automap, the
 * intermission and the finale ({@code D_Display} for one console player).
 *
 * <p>Call {@link #ticker()} after each game tic and {@link #draw()} whenever a frame is wanted.</p>
 */
public final class ViewScreen {

    private final Game g;
    private final Player plyr;
    private final Video video = new Video();
    private final byte[] screen = new byte[SCREENWIDTH * SCREENHEIGHT];
    private final byte[] back = new byte[SCREENWIDTH * SCREENHEIGHT];
    private final Renderer renderer;
    private final StatusBar statusBar;
    private final HeadsUp headsUp;
    private final AutoMap autoMap;
    private final Patch pause;
    private int palette;
    private int levelSerial = -1;
    private @Nullable Mobj lastMobj;
    private int lastPlayerState = -1;
    private int backBlocks = -1;
    private int backDetail = -1;
    private int borderSerial = -1;

    public ViewScreen(final Game g, final Player player, final HuFont font) {
        this.g = g;
        this.plyr = player;
        this.renderer = new Renderer(g);
        this.statusBar = new StatusBar(g);
        this.headsUp = new HeadsUp(g, font);
        this.autoMap = new AutoMap(g, player);
        this.pause = g.res.patch("M_PAUSE");
        renderer.setViewSize(10, 1);
    }

    public Player player() {
        return plyr;
    }

    public Renderer renderer() {
        return renderer;
    }

    public AutoMap autoMap() {
        return autoMap;
    }

    public HeadsUp headsUp() {
        return headsUp;
    }

    /**
     * Changes the view size, as the menu does.
     *
     * @param blocks 3 to 11, 11 hides the status bar
     * @param detail 0 for high detail, 1 for low detail (half horizontal resolution)
     */
    public void setViewSize(final int blocks, final int detail) {
        renderer.setViewSize(blocks, detail);
    }

    /**
     * Toggles the automap.
     */
    public void toggleAutoMap() {
        if (g.gamestate == GS_LEVEL) {
            autoMap.toggle(g.levelSerial);
        }
    }

    /**
     * Updates the status bar, messages and automap; call after each game tic.
     */
    public void ticker() {
        if (g.levelSerial != levelSerial) {
            levelSerial = g.levelSerial;
            autoMap.stop();
            headsUp.start(plyr);
        }
        if (plyr.mo != lastMobj) {
            lastMobj = plyr.mo;
            if (plyr.mo != null) {
                statusBar.start(plyr);
            }
        }
        if (plyr.playerstate == PST_DEAD && lastPlayerState != PST_DEAD) {
            autoMap.stop();
        }
        lastPlayerState = plyr.playerstate;
        if (g.gamestate == GS_LEVEL) {
            autoMap.ticker();
            headsUp.ticker();
            statusBar.ticker();
            palette = statusBar.palette();
        } else {
            autoMap.stop();
            palette = 0;
        }
    }

    /**
     * {@return the palette for the last frame: 0 normal, 1-8 red pain, 9-12 bonus, 13 radiation suit}
     */
    public int palette() {
        return palette;
    }

    /**
     * Draws a frame.
     *
     * @return the 320x200 screen in palette indexes, reused between calls
     */
    public byte[] draw() {
        switch (g.gamestate) {
            case GS_LEVEL -> drawLevel();
            case GS_INTERMISSION -> g.wi.WI_Drawer(screen, plyr.index);
            case GS_FINALE -> g.finale.F_Drawer(screen);
            default -> java.util.Arrays.fill(screen, (byte) 0);
        }
        if (g.paused) {
            final int y = autoMap.isActive() ? 4 : renderer.viewwindowy + 4;
            video.drawPatch(screen, renderer.viewwindowx + (renderer.scaledviewwidth - 68) / 2, y, pause);
        }
        return screen;
    }

    private void drawLevel() {
        if (g.gametic == 0 && plyr.mo == null) {
            java.util.Arrays.fill(screen, (byte) 0);
            return;
        }
        renderer.checkViewSize();
        final boolean fullscreen = renderer.viewheight == SCREENHEIGHT;
        if (autoMap.isActive()) {
            autoMap.draw(screen);
        } else if (plyr.mo != null) {
            if (renderer.screenblocks() != backBlocks || renderer.detail() != backDetail || borderSerial != g.levelSerial) {
                backBlocks = renderer.screenblocks();
                backDetail = renderer.detail();
                borderSerial = g.levelSerial;
                renderer.fillBackScreen(back, video);
            }
            renderer.drawViewBorder(screen, back);
            renderer.renderPlayerView(plyr, screen);
        }
        if (autoMap.isActive() || !fullscreen) {
            statusBar.draw(screen);
        }
        headsUp.draw(screen, autoMap.isActive());
    }
}
