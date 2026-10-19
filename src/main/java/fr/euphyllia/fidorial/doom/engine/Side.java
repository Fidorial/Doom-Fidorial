// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine;

/**
 * A sidedef ({@code side_t}).
 */
public final class Side {

    public int textureoffset;
    public int rowoffset;
    public short toptexture;
    public short bottomtexture;
    public short midtexture;
    public Sector sector;
}
