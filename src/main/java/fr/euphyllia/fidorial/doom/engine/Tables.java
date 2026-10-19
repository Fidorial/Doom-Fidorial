// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

/**
 * Lookup tables for trigonometry ({@code tables.c}).
 *
 * <p>Angles are unsigned 32-bit binary angles stored in Java {@code int}s: compare them with
 * {@link Integer#compareUnsigned} and shift them with {@code >>>}.</p>
 */
public final class Tables {

    public static final int FINEANGLES = 8192;
    public static final int FINEMASK = FINEANGLES - 1;
    public static final int ANGLETOFINESHIFT = 19;

    public static final int ANG45 = 0x20000000;
    public static final int ANG90 = 0x40000000;
    public static final int ANG180 = 0x80000000;
    public static final int ANG270 = 0xc0000000;
    public static final int ANG1 = ANG45 / 45;
    public static final int ANGLE_MAX = 0xffffffff;

    public static final int SLOPERANGE = 2048;
    public static final int SLOPEBITS = 11;
    public static final int DBITS = Fixed.FRACBITS - SLOPEBITS;

    /**
     * {@code finesine[5*FINEANGLES/4]}.
     */
    public static final int[] finesine = new int[5 * FINEANGLES / 4];
    /**
     * {@code finetangent[FINEANGLES/2]}.
     */
    public static final int[] finetangent = new int[FINEANGLES / 2];
    /**
     * {@code tantoangle[SLOPERANGE+1]}, unsigned.
     */
    public static final int[] tantoangle = new int[SLOPERANGE + 1];

    static {
        try (final InputStream raw = Tables.class.getResourceAsStream("tables.bin")) {
            if (raw == null) {
                throw new IllegalStateException("tables.bin missing");
            }
            final DataInputStream in = new DataInputStream(raw);
            for (int i = 0; i < finetangent.length; i++) {
                finetangent[i] = in.readInt();
            }
            for (int i = 0; i < finesine.length; i++) {
                finesine[i] = in.readInt();
            }
            for (int i = 0; i < tantoangle.length; i++) {
                tantoangle[i] = in.readInt();
            }
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Tables() {
    }

    /**
     * {@code finecosine[i]}, which in C is an alias into {@link #finesine}.
     */
    public static int finecosine(final int i) {
        return finesine[i + FINEANGLES / 4];
    }

    public static int SlopeDiv(final int num, final int den) {
        if (Integer.compareUnsigned(den, 512) < 0) {
            return SLOPERANGE;
        }
        final int ans = Integer.divideUnsigned(num << 3, den >>> 8);
        return Integer.compareUnsigned(ans, SLOPERANGE) <= 0 ? ans : SLOPERANGE;
    }

    /**
     * Fine angle index of a binary angle.
     */
    public static int fine(final int angle) {
        return angle >>> ANGLETOFINESHIFT;
    }

    public static boolean angleGreater(final int a, final int b) {
        return Integer.compareUnsigned(a, b) > 0;
    }

    public static boolean angleLess(final int a, final int b) {
        return Integer.compareUnsigned(a, b) < 0;
    }
}
