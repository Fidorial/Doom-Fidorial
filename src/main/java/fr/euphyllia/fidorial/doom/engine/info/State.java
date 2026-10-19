// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine.info;

import org.jspecify.annotations.Nullable;

/**
 * One entry of the state table ({@code state_t}).
 *
 * @param index     the position of this state in {@link States#STATES}
 * @param sprite    the sprite number
 * @param frame     the frame, possibly or-ed with {@code FF_FULLBRIGHT}
 * @param tics      the duration, {@code -1} for forever
 * @param action    the action run when entering the state
 * @param nextstate the state that follows
 * @param misc1     extra parameter
 * @param misc2     extra parameter
 */
public record State(int index, int sprite, int frame, int tics, @Nullable Action action, int nextstate, int misc1,
                    int misc2) {
}
