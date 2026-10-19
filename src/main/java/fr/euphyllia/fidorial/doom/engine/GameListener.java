// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine;

import org.jspecify.annotations.Nullable;

/**
 * What a game tells its viewers about, in place of the console player hooks of vanilla.
 */
public interface GameListener {

    /**
     * A listener that ignores everything.
     */
    GameListener NONE = new GameListener() {
    };

    /**
     * A player map object was spawned ({@code ST_Start} and {@code HU_Start} in vanilla).
     */
    default void playerSpawned(final Player player) {
    }

    /**
     * A player died ({@code AM_Stop} in vanilla).
     */
    default void playerKilled(final Player player) {
    }

    /**
     * A player entered a secret sector.
     */
    default void secretFound(final Player player) {
    }

    /**
     * The game entered a new level.
     */
    default void levelLoaded() {
    }

    /**
     * The game state changed (level, intermission, finale).
     */
    default void gameStateChanged(final int gamestate) {
    }

    /**
     * A sound starts.
     *
     * @param origin  where it comes from, null for full volume
     * @param sfx     the sound number
     * @param onlyFor the only player who hears it, null for everyone
     */
    default void startSound(final @Nullable DegenMobj origin, final int sfx, final @Nullable Player onlyFor) {
    }

    /**
     * Sounds from this origin stop.
     */
    default void stopSound(final DegenMobj origin) {
    }

    /**
     * The music changes.
     */
    default void changeMusic(final int musicnum, final boolean looping) {
    }

    /**
     * Something worth logging, such as a malformed map.
     */
    default void warn(final String message) {
    }
}
