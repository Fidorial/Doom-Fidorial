package fr.euphyllia.fidorial.doom.display;

import java.util.ArrayList;
import java.util.List;

/**
 * Maps the 256 Doom palette indexes onto the 244 map color ids a client can show.
 *
 * <p>Indexes with the same color in every palette share an id. Doom palettes have about 249 distinct
 * entries, so the closest remaining pairs are merged until 244 are left; only indexes above 31 are
 * merged, which keeps the small values free for the header.</p>
 *
 * <p>A <em>symbol</em> is the position of an id among the usable ones: id {@code 4 + symbol}.</p>
 */
public final class DoomPalette {

    /**
     * How many palettes Doom has: normal, 8 red, 4 bonus, radiation suit.
     */
    public static final int PALETTES = 14;

    private final int[][] rgb = new int[PALETTES][256];
    private final int[] representative = new int[256];
    private final int[] symbolOfIndex = new int[256];
    private final int[] indexOfSymbol;
    private final byte[] mapIdOfIndex = new byte[256];

    /**
     * Builds the mapping.
     *
     * @param playpal the {@code PLAYPAL} lump, at least one palette of 768 bytes
     */
    public DoomPalette(final byte[] playpal) {
        final int count = Math.max(1, Math.min(PALETTES, playpal.length / 768));
        for (int p = 0; p < PALETTES; p++) {
            final int base = (p < count ? p : 0) * 768;
            for (int i = 0; i < 256; i++) {
                rgb[p][i] = (playpal[base + i * 3] & 0xff) << 16 | (playpal[base + i * 3 + 1] & 0xff) << 8
                        | (playpal[base + i * 3 + 2] & 0xff);
            }
        }
        for (int i = 0; i < 256; i++) {
            representative[i] = i;
            for (int j = 0; j < i; j++) {
                if (representative[j] == j && sameColor(i, j)) {
                    representative[i] = j;
                    break;
                }
            }
        }
        List<Integer> reps = representatives();
        while (reps.size() > MapColors.COUNT) {
            int bestA = -1;
            int bestB = -1;
            long best = Long.MAX_VALUE;
            for (int a = 0; a < reps.size(); a++) {
                final int ra = reps.get(a);
                if (ra < 32) {
                    continue;
                }
                for (int b = a + 1; b < reps.size(); b++) {
                    final int rb = reps.get(b);
                    final long d = distance(ra, rb);
                    if (d < best) {
                        best = d;
                        bestA = ra;
                        bestB = rb;
                    }
                }
            }
            for (int i = 0; i < 256; i++) {
                if (representative[i] == bestB) {
                    representative[i] = bestA;
                }
            }
            reps = representatives();
        }
        indexOfSymbol = new int[reps.size()];
        for (int s = 0; s < reps.size(); s++) {
            indexOfSymbol[s] = reps.get(s);
        }
        for (int i = 0; i < 256; i++) {
            symbolOfIndex[i] = reps.indexOf(representative[i]);
            mapIdOfIndex[i] = (byte) (MapColors.FIRST_ID + symbolOfIndex[i]);
        }
    }

    private boolean sameColor(final int a, final int b) {
        for (int p = 0; p < PALETTES; p++) {
            if (rgb[p][a] != rgb[p][b]) {
                return false;
            }
        }
        return true;
    }

    private long distance(final int a, final int b) {
        long sum = 0;
        for (int p = 0; p < PALETTES; p++) {
            final int ca = rgb[p][a];
            final int cb = rgb[p][b];
            final int dr = ((ca >> 16) & 0xff) - ((cb >> 16) & 0xff);
            final int dg = ((ca >> 8) & 0xff) - ((cb >> 8) & 0xff);
            final int db = (ca & 0xff) - (cb & 0xff);
            // the normal palette matters most
            final int weight = p == 0 ? 8 : 1;
            sum += (long) weight * (dr * dr * 3 + dg * dg * 4 + db * db * 2);
        }
        return sum;
    }

    private List<Integer> representatives() {
        final List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 256; i++) {
            if (representative[i] == i) {
                list.add(i);
            }
        }
        return list;
    }

    /**
     * {@return how many symbols are used, at most 244}
     */
    public int symbols() {
        return indexOfSymbol.length;
    }

    /**
     * {@return the map color id of each Doom palette index}
     */
    public byte[] mapIds() {
        return mapIdOfIndex.clone();
    }

    /**
     * {@return the map color id of a Doom palette index}
     */
    public byte mapId(final int index) {
        return mapIdOfIndex[index & 0xff];
    }

    /**
     * {@return the color shown for a symbol in a palette, as {@code 0xRRGGBB}}
     */
    public int symbolColor(final int palette, final int symbol) {
        return rgb[palette][indexOfSymbol[symbol]];
    }

    /**
     * {@return the color of a Doom palette index, as {@code 0xRRGGBB}}
     */
    public int color(final int palette, final int index) {
        return rgb[palette][index & 0xff];
    }
}
