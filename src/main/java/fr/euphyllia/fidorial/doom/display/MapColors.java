package fr.euphyllia.fidorial.doom.display;

import fr.fidorial.map.MapColor;
import fr.fidorial.map.MapShade;

/**
 * The colors a vanilla client paints map pixels with.
 *
 * <p>A pixel holds an id: {@code color * 4 + shade}, see {@link MapColor} and {@link MapShade}. The
 * client multiplies each channel of the color by the shade multiplier, then divides by {@code 255}
 * rounding down. Color {@code 0} is transparent, so ids {@code 4..247} are the usable ones; all 244
 * give distinct colors.</p>
 */
public final class MapColors {

    private static final MapColor[] COLORS = MapColor.values();
    private static final MapShade[] SHADES = MapShade.values();

    /**
     * The first usable id.
     */
    public static final int FIRST_ID = MapColor.GRASS.id() * SHADES.length;
    /**
     * How many usable ids there are.
     */
    public static final int COUNT = COLORS.length * SHADES.length - FIRST_ID;

    private MapColors() {
    }

    /**
     * {@return the color the client shows for a map pixel id, as {@code 0xRRGGBB}}
     *
     * @param id a usable id, {@code 4..247}
     */
    public static int rgb(final int id) {
        final int color = COLORS[id / SHADES.length].rgb();
        final int multiplier = SHADES[id % SHADES.length].multiplier();
        final int red = (color >> 16 & 0xFF) * multiplier / 255;
        final int green = (color >> 8 & 0xFF) * multiplier / 255;
        final int blue = (color & 0xFF) * multiplier / 255;
        return red << 16 | green << 8 | blue;
    }
}
