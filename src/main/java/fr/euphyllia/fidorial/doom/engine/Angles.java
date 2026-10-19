// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine;

import static fr.euphyllia.fidorial.doom.engine.Tables.ANG180;
import static fr.euphyllia.fidorial.doom.engine.Tables.ANG270;
import static fr.euphyllia.fidorial.doom.engine.Tables.ANG90;
import static fr.euphyllia.fidorial.doom.engine.Tables.SlopeDiv;
import static fr.euphyllia.fidorial.doom.engine.Tables.tantoangle;

/**
 * Binary angle helpers from {@code r_main.c} that the simulation also uses.
 */
public final class Angles {

    private Angles() {
    }

    /**
     * {@code R_PointToAngle} for a vector relative to the viewer.
     *
     * @param x the x delta
     * @param y the y delta
     * @return the binary angle of the vector
     */
    public static int pointToAngle(int x, int y) {
        if (x == 0 && y == 0) {
            return 0;
        }
        if (x >= 0) {
            if (y >= 0) {
                if (x > y) {
                    return tantoangle[SlopeDiv(y, x)];
                }
                return ANG90 - 1 - tantoangle[SlopeDiv(x, y)];
            }
            y = -y;
            if (x > y) {
                return -tantoangle[SlopeDiv(y, x)];
            }
            return ANG270 + tantoangle[SlopeDiv(x, y)];
        }
        x = -x;
        if (y >= 0) {
            if (x > y) {
                return ANG180 - 1 - tantoangle[SlopeDiv(y, x)];
            }
            return ANG90 + tantoangle[SlopeDiv(x, y)];
        }
        y = -y;
        if (x > y) {
            return ANG180 + tantoangle[SlopeDiv(y, x)];
        }
        return ANG270 - 1 - tantoangle[SlopeDiv(x, y)];
    }

    /**
     * {@code R_PointToAngle2}.
     */
    public static int pointToAngle2(final int x1, final int y1, final int x2, final int y2) {
        return pointToAngle(x2 - x1, y2 - y1);
    }
}
