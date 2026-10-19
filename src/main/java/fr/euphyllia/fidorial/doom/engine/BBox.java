// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine;

/**
 * Bounding boxes ({@code m_bbox.c}).
 */
public final class BBox {

    public static final int BOXTOP = 0;
    public static final int BOXBOTTOM = 1;
    public static final int BOXLEFT = 2;
    public static final int BOXRIGHT = 3;

    private BBox() {
    }

    public static void clear(final int[] box) {
        box[BOXTOP] = box[BOXRIGHT] = Fixed.MININT;
        box[BOXBOTTOM] = box[BOXLEFT] = Fixed.MAXINT;
    }

    public static void add(final int[] box, final int x, final int y) {
        if (x < box[BOXLEFT]) {
            box[BOXLEFT] = x;
        } else if (x > box[BOXRIGHT]) {
            box[BOXRIGHT] = x;
        }
        if (y < box[BOXBOTTOM]) {
            box[BOXBOTTOM] = y;
        } else if (y > box[BOXTOP]) {
            box[BOXTOP] = y;
        }
    }
}
