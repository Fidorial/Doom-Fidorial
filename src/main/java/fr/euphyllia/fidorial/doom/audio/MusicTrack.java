package fr.euphyllia.fidorial.doom.audio;

/**
 * A rendered music lump.
 *
 * @param ogg     the Ogg Vorbis file
 * @param seconds its duration
 */
public record MusicTrack(byte[] ogg, float seconds) {
}
