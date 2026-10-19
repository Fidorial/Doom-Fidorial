// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine.play;

import fr.euphyllia.fidorial.doom.engine.Line;
import fr.euphyllia.fidorial.doom.engine.Mobj;
import org.jspecify.annotations.Nullable;

/**
 * A line or thing crossed by a trace ({@code intercept_t}).
 */
public final class Intercept {

    public int frac;
    public boolean isaline;
    public @Nullable Mobj thing;
    public @Nullable Line line;
}
