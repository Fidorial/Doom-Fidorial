// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine.render;

/**
 * A wall texture composed from patches ({@code texture_t} and its composite).
 */
public final class Texture {

    public final String name;
    public final int width;
    public final int height;
    public final int widthmask;
    /**
     * Height in fixed point ({@code textureheight[]}).
     */
    public final int heightFixed;
    /**
     * Opaque composite of every column, {@code height} pixels each.
     */
    public final byte[][] composite;
    /**
     * Columns as posts, for masked mid textures.
     */
    public final Patch.Column[] masked;

    Texture(final String name, final int width, final int height, final byte[][] composite, final Patch.Column[] masked) {
        this.name = name;
        this.width = width;
        this.height = height;
        int j = 1;
        while (j * 2 <= width) {
            j <<= 1;
        }
        this.widthmask = j - 1;
        this.heightFixed = height << 16;
        this.composite = composite;
        this.masked = masked;
    }
}
