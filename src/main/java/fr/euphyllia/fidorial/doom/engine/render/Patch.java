// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine.render;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * A column based picture ({@code patch_t}) decoded into posts.
 *
 * <p>Each column is a list of posts: a vertical run of opaque pixels starting at {@code top}.</p>
 */
public final class Patch {

    public final int width;
    public final int height;
    public final int leftoffset;
    public final int topoffset;
    public final Column[] columns;
    private Patch(final int width, final int height, final int leftoffset, final int topoffset, final Column[] columns) {
        this.width = width;
        this.height = height;
        this.leftoffset = leftoffset;
        this.topoffset = topoffset;
        this.columns = columns;
    }

    /**
     * An empty one pixel patch, used when a lump is missing or corrupt.
     */
    public static Patch empty() {
        return new Patch(1, 1, 0, 0, new Column[]{new Column(new int[0], new byte[0][])});
    }

    /**
     * Decodes a patch lump.
     *
     * @param data the lump bytes
     * @return the patch, or an empty patch when the data is malformed
     */
    public static Patch decode(final byte[] data) {
        try {
            final ByteBuffer buf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
            final int width = buf.getShort(0);
            final int height = buf.getShort(2);
            final int left = buf.getShort(4);
            final int top = buf.getShort(6);
            if (width <= 0 || width > 4096 || height < 0 || height > 4096) {
                return empty();
            }
            final Column[] columns = new Column[width];
            for (int x = 0; x < width; x++) {
                int ofs = buf.getInt(8 + x * 4);
                int count = 0;
                int scan = ofs;
                while (scan < data.length && (data[scan] & 0xff) != 0xff) {
                    final int len = data[scan + 1] & 0xff;
                    scan += len + 4;
                    count++;
                }
                final int[] tops = new int[count];
                final byte[][] posts = new byte[count][];
                int lastTop = -1;
                for (int p = 0; p < count; p++) {
                    int topdelta = data[ofs] & 0xff;
                    // tall patches: a delta not below the previous one is relative to it
                    if (topdelta <= lastTop) {
                        topdelta += lastTop;
                    }
                    lastTop = topdelta;
                    final int len = data[ofs + 1] & 0xff;
                    final byte[] px = new byte[len];
                    System.arraycopy(data, ofs + 3, px, 0, Math.min(len, Math.max(0, data.length - ofs - 3)));
                    tops[p] = topdelta;
                    posts[p] = px;
                    ofs += len + 4;
                }
                columns[x] = new Column(tops, posts);
            }
            return new Patch(width, height, left, top, columns);
        } catch (final IndexOutOfBoundsException e) {
            return empty();
        }
    }

    /**
         * One column: parallel arrays of post tops, lengths and pixel runs.
         */
        public record Column(int[] tops, byte[][] posts) {
    }
}
