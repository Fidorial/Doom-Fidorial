package fr.euphyllia.fidorial.doom.audio;

import org.jspecify.annotations.Nullable;

/**
 * A digital sound lump in the DMX format used by Doom: a small header, then 8-bit unsigned PCM.
 *
 * @param sampleRate the sample rate in hertz
 * @param samples    the samples in {@code [-1, 1]}
 */
public record DmxSound(int sampleRate, float[] samples) {

    /**
     * Reads a sound lump.
     *
     * @param lump the lump
     * @return the sound, or null if the lump is not a DMX sound
     */
    public static @Nullable DmxSound read(final byte[] lump) {
        if (lump.length < 8) {
            return null;
        }
        final int format = (lump[0] & 0xff) | (lump[1] & 0xff) << 8;
        final int rate = (lump[2] & 0xff) | (lump[3] & 0xff) << 8;
        final long length = (lump[4] & 0xffL) | (lump[5] & 0xffL) << 8 | (lump[6] & 0xffL) << 16 | (lump[7] & 0xffL) << 24;
        if (format != 3 || rate == 0 || length > lump.length - 8L || length <= 32) {
            return null;
        }
        // DMX pads each sound with 16 bytes at both ends
        final int count = (int) length - 32;
        final float[] samples = new float[count];
        for (int i = 0; i < count; i++) {
            samples[i] = ((lump[8 + 16 + i] & 0xff) - 128) / 128f;
        }
        return new DmxSound(rate, samples);
    }
}
