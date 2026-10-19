// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine;

/**
 * 16.16 fixed point arithmetic ({@code m_fixed.c}).
 */
public final class Fixed {

    public static final int FRACBITS = 16;
    public static final int FRACUNIT = 1 << FRACBITS;
    public static final int MAXINT = Integer.MAX_VALUE;
    public static final int MININT = Integer.MIN_VALUE;

    private Fixed() {
    }

    public static int mul(final int a, final int b) {
        return (int) (((long) a * (long) b) >> FRACBITS);
    }

    public static int div(final int a, final int b) {
        if ((Math.abs(a) >> 14) >= Math.abs(b)) {
            return (a ^ b) < 0 ? MININT : MAXINT;
        }
        return div2(a, b);
    }

    public static int div2(final int a, final int b) {
        final double c = ((double) a) / ((double) b) * FRACUNIT;
        if (c >= 2147483648.0 || c < -2147483648.0) {
            throw new ArithmeticException("FixedDiv: divide by zero");
        }
        return (int) c;
    }
}
