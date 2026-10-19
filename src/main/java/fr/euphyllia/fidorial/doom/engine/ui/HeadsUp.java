// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine.ui;

import fr.euphyllia.fidorial.doom.engine.Game;
import fr.euphyllia.fidorial.doom.engine.MapNames;
import fr.euphyllia.fidorial.doom.engine.Player;
import fr.euphyllia.fidorial.doom.engine.render.Video;
import org.jspecify.annotations.Nullable;

import static fr.euphyllia.fidorial.doom.engine.Defs.TICRATE;

/**
 * The message line and the automap title of one viewer ({@code hu_stuff.c}, {@code hu_lib.c}).
 *
 * <p>Chat is left to Minecraft.</p>
 */
public final class HeadsUp {

    private static final int HU_MSGTIMEOUT = 4 * TICRATE;
    private static final int HU_MSGX = 0;
    private static final int HU_MSGY = 0;

    private final Game g;
    private final HuFont font;
    private final Video video = new Video();
    private @Nullable Player plyr;
    private @Nullable String message;
    private int messageCounter;
    private boolean showMessages = true;
    private String title = "";

    public HeadsUp(final Game g, final HuFont font) {
        this.g = g;
        this.font = font;
    }

    /**
     * {@code HU_Start}.
     */
    public void start(final Player player) {
        plyr = player;
        message = null;
        messageCounter = 0;
        title = MapNames.title(g, g.gameepisode, g.gamemap);
    }

    public void setShowMessages(final boolean show) {
        showMessages = show;
    }

    public boolean showMessages() {
        return showMessages;
    }

    /**
     * Shows a message from outside the game, such as a cheat confirmation.
     */
    public void post(final String text) {
        message = text;
        messageCounter = HU_MSGTIMEOUT;
    }

    /**
     * {@code HU_Ticker}.
     */
    public void ticker() {
        if (messageCounter > 0 && --messageCounter == 0) {
            message = null;
        }
        final Player p = plyr;
        if (p != null && p.message != null) {
            if (showMessages) {
                message = g.text(p.message);
                messageCounter = HU_MSGTIMEOUT;
            }
            p.message = null;
        }
    }

    /**
     * Draws the message line and, on the automap, the level title ({@code HU_Drawer}).
     *
     * @param screen  the 320x200 screen
     * @param automap whether the automap is shown
     */
    public void draw(final byte[] screen, final boolean automap) {
        final String msg = message;
        if (msg != null) {
            int y = HU_MSGY;
            for (final String line : msg.split("\n")) {
                font.draw(video, screen, HU_MSGX, y, line);
                y += font.height() + 1;
            }
        }
        if (automap) {
            font.draw(video, screen, 0, StatusBar.ST_Y - font.height(), title);
        }
    }
}
