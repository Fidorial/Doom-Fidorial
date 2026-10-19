// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine.render;

import static fr.euphyllia.fidorial.doom.engine.Defs.SCREENHEIGHT;
import static fr.euphyllia.fidorial.doom.engine.Defs.SCREENWIDTH;

/**
 * Drawing of pictures on 320x200 palette indexed screens ({@code v_video.c}).
 *
 * <p>Pictures that go past the screen edge are clipped pixel by pixel instead of being skipped.</p>
 */
public final class Video {

    private static void drawColumn(final byte[] screen, final int x, final int y, final Patch.Column column,
                                   final byte[] translation) {
        for (int p = 0; p < column.tops().length; p++) {
            final byte[] post = column.posts()[p];
            int dy = y + column.tops()[p];
            for (int k = 0; k < post.length; k++, dy++) {
                if (dy < 0 || dy >= SCREENHEIGHT) {
                    continue;
                }
                screen[dy * SCREENWIDTH + x] = translation == null ? post[k] : translation[post[k] & 0xff];
            }
        }
    }

    /**
     * Draws a patch at its offsets ({@code V_DrawPatch}).
     */
    public void drawPatch(final byte[] screen, int x, int y, final Patch patch) {
        y -= patch.topoffset;
        x -= patch.leftoffset;
        for (int col = 0; col < patch.width; col++, x++) {
            if (x < 0 || x >= SCREENWIDTH) {
                continue;
            }
            drawColumn(screen, x, y, patch.columns[col], null);
        }
    }

    /**
     * Draws a patch through a color remap.
     */
    public void drawPatchTranslated(final byte[] screen, int x, int y, final Patch patch, final byte[] translation) {
        y -= patch.topoffset;
        x -= patch.leftoffset;
        for (int col = 0; col < patch.width; col++, x++) {
            if (x < 0 || x >= SCREENWIDTH) {
                continue;
            }
            drawColumn(screen, x, y, patch.columns[col], translation);
        }
    }

    /**
     * Draws a patch mirrored horizontally ({@code V_DrawPatchFlipped}).
     */
    public void drawPatchFlipped(final byte[] screen, int x, int y, final Patch patch) {
        y -= patch.topoffset;
        x -= patch.leftoffset;
        for (int col = 0; col < patch.width; col++, x++) {
            if (x < 0 || x >= SCREENWIDTH) {
                continue;
            }
            drawColumn(screen, x, y, patch.columns[patch.width - 1 - col], null);
        }
    }

    /**
     * Fills the whole screen with a tiled 64x64 flat.
     */
    public void tileFlat(final byte[] screen, final byte[] flat) {
        for (int y = 0; y < SCREENHEIGHT; y++) {
            for (int x = 0; x < SCREENWIDTH; x++) {
                screen[y * SCREENWIDTH + x] = flat[((y & 63) << 6) + (x & 63)];
            }
        }
    }

    /**
     * Draws a raw 320 wide picture such as a fullscreen title lump.
     */
    public void drawRaw(final byte[] screen, final byte[] raw) {
        System.arraycopy(raw, 0, screen, 0, Math.min(raw.length, screen.length));
    }

    /**
     * Copies a rectangle between screens ({@code V_CopyRect}).
     */
    public void copyRect(final byte[] src, final int srcx, final int srcy, final int width, final int height,
                         final byte[] dest, final int destx, final int desty) {
        for (int row = 0; row < height; row++) {
            System.arraycopy(src, (srcy + row) * SCREENWIDTH + srcx, dest, (desty + row) * SCREENWIDTH + destx, width);
        }
    }
}
