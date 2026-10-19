// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine;

import fr.euphyllia.fidorial.doom.engine.info.State;
import org.jspecify.annotations.Nullable;

/**
 * A player sprite: the weapon or its flash ({@code pspdef_t}).
 */
public final class PspDef {

    public @Nullable State state;
    public int tics;
    public int sx;
    public int sy;
}
