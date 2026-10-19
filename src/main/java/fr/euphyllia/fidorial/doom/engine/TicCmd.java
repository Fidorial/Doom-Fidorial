// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine;

/**
 * One tic of player input ({@code ticcmd_t}).
 */
public final class TicCmd {

    /**
     * Signed byte.
     */
    public byte forwardmove;
    /**
     * Signed byte.
     */
    public byte sidemove;
    public short angleturn;
    public short consistancy;
    public byte chatchar;
    /**
     * Unsigned byte, use {@code buttons & 0xff}.
     */
    public byte buttons;

    public void clear() {
        forwardmove = 0;
        sidemove = 0;
        angleturn = 0;
        consistancy = 0;
        chatchar = 0;
        buttons = 0;
    }

    public void copyFrom(final TicCmd other) {
        forwardmove = other.forwardmove;
        sidemove = other.sidemove;
        angleturn = other.angleturn;
        consistancy = other.consistancy;
        chatchar = other.chatchar;
        buttons = other.buttons;
    }

    public int buttons() {
        return buttons & 0xff;
    }
}
