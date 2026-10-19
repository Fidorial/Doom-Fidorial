// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine;

/**
 * A thing as placed in the map ({@code mapthing_t}).
 */
public final class MapThing {

    public short x;
    public short y;
    public short angle;
    public short type;
    public short options;

    public MapThing() {
    }

    public MapThing(final MapThing other) {
        copyFrom(other);
    }

    public void copyFrom(final MapThing other) {
        this.x = other.x;
        this.y = other.y;
        this.angle = other.angle;
        this.type = other.type;
        this.options = other.options;
    }
}
