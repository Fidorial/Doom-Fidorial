package fr.euphyllia.fidorial.doom.display;

import fr.fidorial.map.MapCanvas;

/**
 * Splits a 320x200 Doom screen into the map canvases the resource pack shader puts back together.
 *
 * <p>The screen is cut into {@value #TILES} vertical strips of {@value #STRIP_WIDTH} columns. Each
 * strip goes to one 128x128 canvas: a {@value #HEADER}-pixel header, then the strip pixels row by row.
 * The header starts with a signature that tells the shader the canvas holds Doom, then gives the strip
 * index, the strip count, the palette and flags.</p>
 *
 * <p>The server sends only what changed in each canvas, see {@link fr.fidorial.map.MapView}.</p>
 */
public final class ScreenEncoder {

    /**
     * The width of the Doom screen.
     */
    public static final int WIDTH = 320;
    /**
     * The height of the Doom screen.
     */
    public static final int HEIGHT = 200;
    /**
     * How many canvases make one screen.
     */
    public static final int TILES = 4;
    /**
     * The width of the strip held by one canvas.
     */
    public static final int STRIP_WIDTH = WIDTH / TILES;
    /**
     * How many pixels the header takes at the start of each canvas.
     */
    public static final int HEADER = 16;
    /**
     * Header flag: keep the 4:3 aspect ratio with black bars.
     */
    public static final int FLAG_LETTERBOX = 1;
    /**
     * The symbols the header starts with.
     */
    static final int[] SIGNATURE = {37, 201, 13, 150, 99, 222, 61, 177};
    private static final int CANVAS = MapCanvas.SIZE * MapCanvas.SIZE;

    private final DoomPalette palette;
    private final byte[] mapIds;
    private final byte[][] canvases = new byte[TILES][CANVAS];
    private int flags = FLAG_LETTERBOX;

    public ScreenEncoder(final DoomPalette palette) {
        this.palette = palette;
        this.mapIds = palette.mapIds();
    }

    private static byte symbol(final int value) {
        return (byte) (MapColors.FIRST_ID + value);
    }

    public DoomPalette palette() {
        return palette;
    }

    /**
     * Sets the header flags, such as {@link #FLAG_LETTERBOX}.
     */
    public void setFlags(final int flags) {
        this.flags = flags;
    }

    /**
     * Encodes a frame.
     *
     * @param screen    the 320x200 screen in Doom palette indexes
     * @param paletteNo the palette to show it with, {@code 0..13}
     * @return the {@value #TILES} canvases, {@code 128x128} map pixels each; reused by the next call
     */
    public byte[][] encode(final byte[] screen, final int paletteNo) {
        for (int tile = 0; tile < TILES; tile++) {
            final byte[] canvas = canvases[tile];
            for (int i = 0; i < SIGNATURE.length; i++) {
                canvas[i] = symbol(SIGNATURE[i]);
            }
            canvas[8] = symbol(tile);
            canvas[9] = symbol(TILES);
            canvas[10] = symbol(Math.clamp(paletteNo, 0, DoomPalette.PALETTES - 1));
            canvas[11] = symbol(flags);
            for (int i = 12; i < HEADER; i++) {
                canvas[i] = symbol(0);
            }
            int p = HEADER;
            final int x0 = tile * STRIP_WIDTH;
            for (int y = 0; y < HEIGHT; y++) {
                final int row = y * WIDTH + x0;
                for (int x = 0; x < STRIP_WIDTH; x++) {
                    canvas[p++] = mapIds[screen[row + x] & 0xff];
                }
            }
            for (; p < CANVAS; p++) {
                canvas[p] = symbol(0);
            }
        }
        return canvases;
    }
}
