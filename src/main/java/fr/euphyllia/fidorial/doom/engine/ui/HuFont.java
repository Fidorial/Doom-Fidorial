// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine.ui;

import fr.euphyllia.fidorial.doom.engine.render.Patch;
import fr.euphyllia.fidorial.doom.engine.render.RenderData;
import fr.euphyllia.fidorial.doom.engine.render.Video;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

import static fr.euphyllia.fidorial.doom.engine.Defs.SCREENWIDTH;

/**
 * The small red font of the heads-up display ({@code hu_font}).
 */
public final class HuFont {

    public static final int HU_FONTSTART = '!';
    public static final int HU_FONTEND = '_';
    public static final int HU_FONTSIZE = HU_FONTEND - HU_FONTSTART + 1;

    private final Patch[] font = new Patch[HU_FONTSIZE];

    public HuFont(final RenderData res) {
        for (int i = 0; i < HU_FONTSIZE; i++) {
            font[i] = res.patch(String.format(Locale.ROOT, "STCFN%03d", HU_FONTSTART + i));
        }
    }

    /**
     * {@return the glyph of a character, or null for a space}
     */
    public @Nullable Patch glyph(final char ch) {
        final int c = Character.toUpperCase(ch) - HU_FONTSTART;
        if (c < 0 || c >= HU_FONTSIZE) {
            return null;
        }
        return font[c];
    }

    public int height() {
        return font[0].height;
    }

    /**
     * {@return the width of a string in pixels}
     */
    public int width(final String text) {
        int w = 0;
        for (int i = 0; i < text.length(); i++) {
            final Patch p = glyph(text.charAt(i));
            w += p == null ? 4 : p.width;
        }
        return w;
    }

    /**
     * Draws a line of text, stopping at the right edge.
     *
     * @return the x coordinate after the text
     */
    public int draw(final Video video, final byte[] screen, int x, final int y, final String text) {
        for (int i = 0; i < text.length(); i++) {
            final Patch p = glyph(text.charAt(i));
            if (p == null) {
                x += 4;
                continue;
            }
            if (x + p.width > SCREENWIDTH) {
                break;
            }
            video.drawPatch(screen, x, y, p);
            x += p.width;
        }
        return x;
    }
}
