// The voice handling follows i_oplmusic.c of Chocolate Doom (Copyright (C) 1993-1996 id Software,
// Inc., Copyright (C) 2005-2014 Simon Howard), which emulates the DMX sound library of Doom 1.9.
// Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.audio;

import fr.euphyllia.fidorial.doom.engine.Music;
import fr.euphyllia.fidorial.doom.engine.Wad;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Plays the music lumps (MUS or MIDI) through an emulated OPL2 chip with the GENMIDI instruments,
 * the way Doom sounds on a Sound Blaster, and encodes each track to Ogg Vorbis.
 */
public final class MusicRenderer {

    /**
     * How many chip samples make one output sample.
     */
    private static final int DECIMATION = 3;
    /**
     * The output rate: the chip rate divided by {@value #DECIMATION}.
     */
    static final int OUTPUT_RATE = Opl2.RATE / DECIMATION;
    private static final int TAPS = 64;
    /**
     * Windowed sinc low-pass at 0.42 times the output rate, to decimate without aliasing.
     */
    private static final float[] FIR = new float[TAPS];
    private static final double MAX_SECONDS = 600;
    private static final double TAIL_SECONDS = 1.5;
    private static final int RESIDUE_MAX = 48;

    static {
        final double cutoff = 0.42 / DECIMATION;
        double sum = 0;
        for (int i = 0; i < TAPS; i++) {
            final double x = i - (TAPS - 1) / 2.0;
            final double sinc = x == 0 ? 2 * cutoff : Math.sin(2 * Math.PI * cutoff * x) / (Math.PI * x);
            final double window = 0.42 - 0.5 * Math.cos(2 * Math.PI * i / (TAPS - 1))
                    + 0.08 * Math.cos(4 * Math.PI * i / (TAPS - 1));
            FIR[i] = (float) (sinc * window);
            sum += FIR[i];
        }
        for (int i = 0; i < TAPS; i++) {
            FIR[i] /= (float) sum;
        }
    }

    private MusicRenderer() {
    }

    /**
     * Renders every music lump of the WAD.
     *
     * @param wad      the WADs
     * @param progress receives a line from time to time
     * @return the tracks keyed by sound event, such as {@code music.e1m1}
     */
    public static Map<String, MusicTrack> renderAll(final Wad wad, final Consumer<String> progress) {
        final byte[] genmidi = wad.readLumpOrNull("GENMIDI");
        if (genmidi == null || genmidi.length < 8 + 175 * 36) {
            progress.accept("no GENMIDI lump, music skipped");
            return Collections.emptyMap();
        }
        final Instrument[] instruments = Instrument.parse(genmidi);
        final List<String> names = new ArrayList<>();
        for (int i = 1; i < Music.NAME.length; i++) {
            if (wad.checkNumForName("D_" + Music.NAME[i].toUpperCase(Locale.ROOT)) >= 0) {
                names.add(Music.NAME[i]);
            }
        }
        final Map<String, MusicTrack> tracks = Collections.synchronizedMap(new LinkedHashMap<>());
        final long start = System.nanoTime();
        names.parallelStream().forEach(name -> {
            final byte[] lump = wad.readLump("D_" + name.toUpperCase(Locale.ROOT));
            final List<Event> events = Event.parse(lump);
            if (events == null || events.isEmpty()) {
                return;
            }
            final float[] samples = render(events, instruments);
            tracks.put("music." + name, new MusicTrack(VorbisEncoder.encode(samples, OUTPUT_RATE, RESIDUE_MAX),
                    samples.length / (float) OUTPUT_RATE));
        });
        // keep a stable order
        final Map<String, MusicTrack> ordered = new LinkedHashMap<>();
        for (final String name : names) {
            final MusicTrack t = tracks.get("music." + name);
            if (t != null) {
                ordered.put("music." + name, t);
            }
        }
        progress.accept("rendered " + ordered.size() + " music tracks in "
                + (System.nanoTime() - start) / 1_000_000 + " ms");
        return ordered;
    }

    /**
     * Renders a song at {@link #OUTPUT_RATE}.
     */
    static float[] render(final List<Event> events, final Instrument[] instruments) {
        final Opl2 chip = new Opl2();
        final Dmx dmx = new Dmx(chip, instruments);
        final double end = Math.min(MAX_SECONDS, events.getLast().time()) + TAIL_SECONDS;
        final int total = (int) (end * OUTPUT_RATE);
        final float[] out = new float[total];
        final float[] ring = new float[TAPS];
        int pos = 0;
        int next = 0;
        long chipSamples = 0;
        for (int i = 0; i < total; i++) {
            for (int k = 0; k < DECIMATION; k++) {
                final double now = (double) chipSamples++ / Opl2.RATE;
                while (next < events.size() && events.get(next).time() <= now) {
                    dmx.handle(events.get(next++));
                }
                ring[pos] = chip.generate() / 16384f;
                pos = (pos + 1) % TAPS;
            }
            float acc = 0;
            for (int t = 0; t < TAPS; t++) {
                acc += FIR[t] * ring[(pos + t) % TAPS];
            }
            out[i] = Math.max(-1f, Math.min(1f, acc));
        }
        return out;
    }

    /**
     * One operator of a GENMIDI voice.
     */
    record Operator(int tremolo, int attack, int sustain, int waveform, int scale, int level) {
    }

    /**
     * One of the two voices of a GENMIDI instrument.
     */
    record Voice(Operator modulator, int feedback, Operator carrier, int baseNoteOffset) {
    }

    /**
     * A GENMIDI instrument.
     */
    record Instrument(int flags, int fineTuning, int fixedNote, Voice[] voices) {

        static final int FIXED = 0x0001;
        static final int DOUBLE = 0x0004;

        static Instrument[] parse(final byte[] lump) {
            final Instrument[] list = new Instrument[175];
            for (int i = 0; i < list.length; i++) {
                final int p = 8 + i * 36;
                final Voice[] voices = new Voice[2];
                for (int v = 0; v < 2; v++) {
                    final int q = p + 4 + v * 16;
                    voices[v] = new Voice(op(lump, q), lump[q + 6] & 0xff, op(lump, q + 7),
                            (short) ((lump[q + 14] & 0xff) | (lump[q + 15] & 0xff) << 8));
                }
                list[i] = new Instrument((lump[p] & 0xff) | (lump[p + 1] & 0xff) << 8, lump[p + 2] & 0xff,
                        lump[p + 3] & 0xff, voices);
            }
            return list;
        }

        private static Operator op(final byte[] b, final int p) {
            return new Operator(b[p] & 0xff, b[p + 1] & 0xff, b[p + 2] & 0xff, b[p + 3] & 0xff, b[p + 4] & 0xff,
                    b[p + 5] & 0xff);
        }
    }

    /**
     * A channel event at a time in seconds; channels use the MUS numbering, where 15 is percussion.
     */
    record Event(double time, int order, int type, int channel, int p1, int p2) {

        static final int NOTE_OFF = 0;
        static final int NOTE_ON = 1;
        static final int PROGRAM = 2;
        static final int VOLUME = 3;
        static final int BEND = 4;
        static final int ALL_NOTES_OFF = 5;
        static final int END = 6;

        static @Nullable List<Event> parse(final byte[] lump) {
            if (lump.length > 4 && lump[0] == 'M' && lump[1] == 'U' && lump[2] == 'S' && lump[3] == 0x1a) {
                return parseMus(lump);
            }
            if (lump.length > 14 && lump[0] == 'M' && lump[1] == 'T' && lump[2] == 'h' && lump[3] == 'd') {
                return parseMidi(lump);
            }
            return null;
        }

        private static List<Event> parseMus(final byte[] b) {
            final List<Event> events = new ArrayList<>();
            final int start = (b[6] & 0xff) | (b[7] & 0xff) << 8;
            final int[] lastVolume = new int[16];
            java.util.Arrays.fill(lastVolume, 127);
            int p = start;
            long ticks = 0;
            int order = 0;
            while (p < b.length) {
                final int desc = b[p++] & 0xff;
                final int type = (desc >> 4) & 7;
                final int channel = desc & 15;
                final double time = ticks / 140.0;
                switch (type) {
                    case 0 -> events.add(new Event(time, order++, NOTE_OFF, channel, b[p++] & 0x7f, 0));
                    case 1 -> {
                        final int note = b[p++] & 0xff;
                        if ((note & 0x80) != 0) {
                            lastVolume[channel] = b[p++] & 0x7f;
                        }
                        events.add(new Event(time, order++, NOTE_ON, channel, note & 0x7f, lastVolume[channel]));
                    }
                    case 2 -> events.add(new Event(time, order++, BEND, channel, ((b[p++] & 0xff) >> 1) - 64, 0));
                    case 3 -> {
                        final int sys = b[p++] & 0x7f;
                        if (sys == 10 || sys == 11) {
                            events.add(new Event(time, order++, ALL_NOTES_OFF, channel, 0, 0));
                        }
                    }
                    case 4 -> {
                        final int ctrl = b[p++] & 0x7f;
                        final int value = b[p++] & 0x7f;
                        if (ctrl == 0) {
                            events.add(new Event(time, order++, PROGRAM, channel, value, 0));
                        } else if (ctrl == 3) {
                            events.add(new Event(time, order++, VOLUME, channel, value, 0));
                        }
                    }
                    case 5 -> {
                    }
                    case 6 -> {
                        events.add(new Event(time, order++, END, 0, 0, 0));
                        return events;
                    }
                    default -> {
                        events.add(new Event(time, order++, END, 0, 0, 0));
                        return events;
                    }
                }
                if ((desc & 0x80) != 0 && p < b.length) {
                    long delay = 0;
                    int v;
                    do {
                        v = b[p++] & 0xff;
                        delay = delay * 128 + (v & 0x7f);
                    } while ((v & 0x80) != 0 && p < b.length);
                    ticks += delay;
                }
            }
            events.add(new Event(ticks / 140.0, order, END, 0, 0, 0));
            return events;
        }

        /**
         * MIDI channel 9 is percussion; DMX works on MUS channels, where it is 15.
         */
        private static int musChannel(final int midi) {
            return midi == 9 ? 15 : midi == 15 ? 9 : midi;
        }

        private static List<Event> parseMidi(final byte[] b) {
            final int tracks = (b[10] & 0xff) << 8 | (b[11] & 0xff);
            final int division = (b[12] & 0xff) << 8 | (b[13] & 0xff);
            final List<Raw> raw = new ArrayList<>();
            int p = 8 + ((b[4] & 0xff) << 24 | (b[5] & 0xff) << 16 | (b[6] & 0xff) << 8 | (b[7] & 0xff));
            int order = 0;
            for (int t = 0; t < tracks && p + 8 <= b.length; t++) {
                final int len = (b[p + 4] & 0xff) << 24 | (b[p + 5] & 0xff) << 16 | (b[p + 6] & 0xff) << 8 | (b[p + 7] & 0xff);
                int q = p + 8;
                final int end = Math.min(b.length, q + len);
                p = end;
                long tick = 0;
                int running = 0;
                while (q < end) {
                    long delta = 0;
                    int v;
                    do {
                        v = b[q++] & 0xff;
                        delta = delta * 128 + (v & 0x7f);
                    } while ((v & 0x80) != 0 && q < end);
                    tick += delta;
                    if (q >= end) {
                        break;
                    }
                    int status = b[q] & 0xff;
                    if (status < 0x80) {
                        status = running;
                    } else {
                        q++;
                    }
                    if (status == 0xff) {
                        final int type = b[q++] & 0xff;
                        long mlen = 0;
                        do {
                            v = b[q++] & 0xff;
                            mlen = mlen * 128 + (v & 0x7f);
                        } while ((v & 0x80) != 0);
                        if (type == 0x51 && mlen == 3) {
                            final int tempo = (b[q] & 0xff) << 16 | (b[q + 1] & 0xff) << 8 | (b[q + 2] & 0xff);
                            raw.add(new Raw(tick, order++, 0xff, 0, 0, tempo));
                        }
                        if (type == 0x2f) {
                            q = end;
                            break;
                        }
                        q += (int) mlen;
                        continue;
                    }
                    if (status == 0xf0 || status == 0xf7) {
                        long slen = 0;
                        do {
                            v = b[q++] & 0xff;
                            slen = slen * 128 + (v & 0x7f);
                        } while ((v & 0x80) != 0);
                        q += (int) slen;
                        continue;
                    }
                    running = status;
                    final int kind = status & 0xf0;
                    final int d1 = b[q++] & 0x7f;
                    int d2 = 0;
                    if (kind != 0xc0 && kind != 0xd0) {
                        d2 = b[q++] & 0x7f;
                    }
                    raw.add(new Raw(tick, order++, status, d1, d2, 0));
                }
            }
            raw.sort(Comparator.comparingLong(Raw::tick).thenComparingInt(Raw::order));
            final List<Event> events = new ArrayList<>();
            double seconds = 0;
            long lastTick = 0;
            double usPerBeat = 500000;
            int n = 0;
            for (final Raw r : raw) {
                seconds += (r.tick() - lastTick) * usPerBeat / 1e6 / Math.max(1, division);
                lastTick = r.tick();
                if (r.status() == 0xff) {
                    usPerBeat = r.tempo();
                    continue;
                }
                final int ch = musChannel(r.status() & 15);
                switch (r.status() & 0xf0) {
                    case 0x80 -> events.add(new Event(seconds, n++, NOTE_OFF, ch, r.d1(), 0));
                    case 0x90 ->
                            events.add(new Event(seconds, n++, r.d2() == 0 ? NOTE_OFF : NOTE_ON, ch, r.d1(), r.d2()));
                    case 0xb0 -> {
                        if (r.d1() == 7) {
                            events.add(new Event(seconds, n++, VOLUME, ch, r.d2(), 0));
                        } else if (r.d1() == 123 || r.d1() == 120) {
                            events.add(new Event(seconds, n++, ALL_NOTES_OFF, ch, 0, 0));
                        }
                    }
                    case 0xc0 -> events.add(new Event(seconds, n++, PROGRAM, ch, r.d1(), 0));
                    case 0xe0 -> events.add(new Event(seconds, n++, BEND, ch, r.d2() - 64, 0));
                    default -> {
                    }
                }
            }
            events.add(new Event(seconds, n, END, 0, 0, 0));
            return events;
        }

        private record Raw(long tick, int order, int status, int d1, int d2, int tempo) {
        }
    }

    /**
     * The OPL2 driver of the DMX library, Doom 1.9 flavour.
     */
    private static final class Dmx {

        private static final int VOICES = 9;
        private static final int[][] OPERATORS = {
                {0x00, 0x01, 0x02, 0x08, 0x09, 0x0a, 0x10, 0x11, 0x12},
                {0x03, 0x04, 0x05, 0x0b, 0x0c, 0x0d, 0x13, 0x14, 0x15}
        };
        private static final int[] VOLUME_MAPPING = {
                0, 1, 3, 5, 6, 8, 10, 11, 13, 14, 16, 17, 19, 20, 22, 23, 25, 26, 27, 29, 30, 32, 33, 34, 36, 37, 39, 41, 43,
                45, 47, 49, 50, 52, 54, 55, 57, 59, 60, 61, 63, 64, 66, 67, 68, 69, 71, 72, 73, 74, 75, 76, 77, 79, 80, 81, 82,
                83, 84, 84, 85, 86, 87, 88, 89, 90, 91, 92, 92, 93, 94, 95, 96, 96, 97, 98, 99, 99, 100, 101, 101, 102, 103,
                103, 104, 105, 105, 106, 107, 107, 108, 109, 109, 110, 110, 111, 112, 112, 113, 113, 114, 114, 115, 115, 116,
                117, 117, 118, 118, 119, 119, 120, 120, 121, 121, 122, 122, 123, 123, 123, 124, 124, 125, 125, 126, 126, 127,
                127
        };
        /**
         * OPL frequency register values, 32 steps per semitone ({@code frequency_curve} of DMX).
         */
        private static final int[] FREQUENCY_CURVE = {
                0x133, 0x133, 0x134, 0x134, 0x135, 0x136, 0x136, 0x137, 0x137, 0x138, 0x138, 0x139, 0x139, 0x13a, 0x13b,
                0x13b, 0x13c, 0x13c, 0x13d, 0x13d, 0x13e, 0x13f, 0x13f, 0x140, 0x140, 0x141, 0x142, 0x142, 0x143, 0x143,
                0x144, 0x144, 0x145, 0x146, 0x146, 0x147, 0x147, 0x148, 0x149, 0x149, 0x14a, 0x14a, 0x14b, 0x14c, 0x14c,
                0x14d, 0x14d, 0x14e, 0x14f, 0x14f, 0x150, 0x150, 0x151, 0x152, 0x152, 0x153, 0x153, 0x154, 0x155, 0x155,
                0x156, 0x157, 0x157, 0x158, 0x158, 0x159, 0x15a, 0x15a, 0x15b, 0x15b, 0x15c, 0x15d, 0x15d, 0x15e, 0x15f,
                0x15f, 0x160, 0x161, 0x161, 0x162, 0x162, 0x163, 0x164, 0x164, 0x165, 0x166, 0x166, 0x167, 0x168, 0x168,
                0x169, 0x16a, 0x16a, 0x16b, 0x16c, 0x16c, 0x16d, 0x16e, 0x16e, 0x16f, 0x170, 0x170, 0x171, 0x172, 0x172,
                0x173, 0x174, 0x174, 0x175, 0x176, 0x176, 0x177, 0x178, 0x178, 0x179, 0x17a, 0x17a, 0x17b, 0x17c, 0x17c,
                0x17d, 0x17e, 0x17e, 0x17f, 0x180, 0x181, 0x181, 0x182, 0x183, 0x183, 0x184, 0x185, 0x185, 0x186, 0x187,
                0x188, 0x188, 0x189, 0x18a, 0x18a, 0x18b, 0x18c, 0x18d, 0x18d, 0x18e, 0x18f, 0x18f, 0x190, 0x191, 0x192,
                0x192, 0x193, 0x194, 0x194, 0x195, 0x196, 0x197, 0x197, 0x198, 0x199, 0x19a, 0x19a, 0x19b, 0x19c, 0x19d,
                0x19d, 0x19e, 0x19f, 0x1a0, 0x1a0, 0x1a1, 0x1a2, 0x1a3, 0x1a3, 0x1a4, 0x1a5, 0x1a6, 0x1a6, 0x1a7, 0x1a8,
                0x1a9, 0x1a9, 0x1aa, 0x1ab, 0x1ac, 0x1ad, 0x1ad, 0x1ae, 0x1af, 0x1b0, 0x1b0, 0x1b1, 0x1b2, 0x1b3, 0x1b4,
                0x1b4, 0x1b5, 0x1b6, 0x1b7, 0x1b8, 0x1b8, 0x1b9, 0x1ba, 0x1bb, 0x1bc, 0x1bc, 0x1bd, 0x1be, 0x1bf, 0x1c0,
                0x1c0, 0x1c1, 0x1c2, 0x1c3, 0x1c4, 0x1c4, 0x1c5, 0x1c6, 0x1c7, 0x1c8, 0x1c9, 0x1c9, 0x1ca, 0x1cb, 0x1cc,
                0x1cd, 0x1ce, 0x1ce, 0x1cf, 0x1d0, 0x1d1, 0x1d2, 0x1d3, 0x1d3, 0x1d4, 0x1d5, 0x1d6, 0x1d7, 0x1d8, 0x1d8,
                0x1d9, 0x1da, 0x1db, 0x1dc, 0x1dd, 0x1de, 0x1de, 0x1df, 0x1e0, 0x1e1, 0x1e2, 0x1e3, 0x1e4, 0x1e5, 0x1e5,
                0x1e6, 0x1e7, 0x1e8, 0x1e9, 0x1ea, 0x1eb, 0x1ec, 0x1ed, 0x1ed, 0x1ee, 0x1ef, 0x1f0, 0x1f1, 0x1f2, 0x1f3,
                0x1f4, 0x1f5, 0x1f6, 0x1f6, 0x1f7, 0x1f8, 0x1f9, 0x1fa, 0x1fb, 0x1fc, 0x1fd, 0x1fe, 0x1ff, 0x200, 0x201,
                0x201, 0x202, 0x203, 0x204, 0x205, 0x206, 0x207, 0x208, 0x209, 0x20a, 0x20b, 0x20c, 0x20d, 0x20e, 0x20f,
                0x210, 0x210, 0x211, 0x212, 0x213, 0x214, 0x215, 0x216, 0x217, 0x218, 0x219, 0x21a, 0x21b, 0x21c, 0x21d,
                0x21e, 0x21f, 0x220, 0x221, 0x222, 0x223, 0x224, 0x225, 0x226, 0x227, 0x228, 0x229, 0x22a, 0x22b, 0x22c,
                0x22d, 0x22e, 0x22f, 0x230, 0x231, 0x232, 0x233, 0x234, 0x235, 0x236, 0x237, 0x238, 0x239, 0x23a, 0x23b,
                0x23c, 0x23d, 0x23e, 0x23f, 0x240, 0x241, 0x242, 0x244, 0x245, 0x246, 0x247, 0x248, 0x249, 0x24a, 0x24b,
                0x24c, 0x24d, 0x24e, 0x24f, 0x250, 0x251, 0x252, 0x253, 0x254, 0x256, 0x257, 0x258, 0x259, 0x25a, 0x25b,
                0x25c, 0x25d, 0x25e, 0x25f, 0x260, 0x262, 0x263, 0x264, 0x265, 0x266, 0x267, 0x268, 0x269, 0x26a, 0x26c,
                0x26d, 0x26e, 0x26f, 0x270, 0x271, 0x272, 0x273, 0x275, 0x276, 0x277, 0x278, 0x279, 0x27a, 0x27b, 0x27d,
                0x27e, 0x27f, 0x280, 0x281, 0x282, 0x284, 0x285, 0x286, 0x287, 0x288, 0x289, 0x28b, 0x28c, 0x28d, 0x28e,
                0x28f, 0x290, 0x292, 0x293, 0x294, 0x295, 0x296, 0x298, 0x299, 0x29a, 0x29b, 0x29c, 0x29e, 0x29f, 0x2a0,
                0x2a1, 0x2a2, 0x2a4, 0x2a5, 0x2a6, 0x2a7, 0x2a9, 0x2aa, 0x2ab, 0x2ac, 0x2ae, 0x2af, 0x2b0, 0x2b1, 0x2b2,
                0x2b4, 0x2b5, 0x2b6, 0x2b7, 0x2b9, 0x2ba, 0x2bb, 0x2bd, 0x2be, 0x2bf, 0x2c0, 0x2c2, 0x2c3, 0x2c4, 0x2c5,
                0x2c7, 0x2c8, 0x2c9, 0x2cb, 0x2cc, 0x2cd, 0x2ce, 0x2d0, 0x2d1, 0x2d2, 0x2d4, 0x2d5, 0x2d6, 0x2d8, 0x2d9,
                0x2da, 0x2dc, 0x2dd, 0x2de, 0x2e0, 0x2e1, 0x2e2, 0x2e4, 0x2e5, 0x2e6, 0x2e8, 0x2e9, 0x2ea, 0x2ec, 0x2ed,
                0x2ee, 0x2f0, 0x2f1, 0x2f2, 0x2f4, 0x2f5, 0x2f6, 0x2f8, 0x2f9, 0x2fb, 0x2fc, 0x2fd, 0x2ff, 0x300, 0x302,
                0x303, 0x304, 0x306, 0x307, 0x309, 0x30a, 0x30b, 0x30d, 0x30e, 0x310, 0x311, 0x312, 0x314, 0x315, 0x317,
                0x318, 0x31a, 0x31b, 0x31c, 0x31e, 0x31f, 0x321, 0x322, 0x324, 0x325, 0x327, 0x328, 0x329, 0x32b, 0x32c,
                0x32e, 0x32f, 0x331, 0x332, 0x334, 0x335, 0x337, 0x338, 0x33a, 0x33b, 0x33d, 0x33e, 0x340, 0x341, 0x343,
                0x344, 0x346, 0x347, 0x349, 0x34a, 0x34c, 0x34d, 0x34f, 0x350, 0x352, 0x353, 0x355, 0x357, 0x358, 0x35a,
                0x35b, 0x35d, 0x35e, 0x360, 0x361, 0x363, 0x365, 0x366, 0x368, 0x369, 0x36b, 0x36c, 0x36e, 0x370, 0x371,
                0x373, 0x374, 0x376, 0x378, 0x379, 0x37b, 0x37c, 0x37e, 0x380, 0x381, 0x383, 0x384, 0x386, 0x388, 0x389,
                0x38b, 0x38d, 0x38e, 0x390, 0x392, 0x393, 0x395, 0x397, 0x398, 0x39a, 0x39c, 0x39d, 0x39f, 0x3a1, 0x3a2,
                0x3a4, 0x3a6, 0x3a7, 0x3a9, 0x3ab, 0x3ac, 0x3ae, 0x3b0, 0x3b1, 0x3b3, 0x3b5, 0x3b7, 0x3b8, 0x3ba, 0x3bc,
                0x3bd, 0x3bf, 0x3c1, 0x3c3, 0x3c4, 0x3c6, 0x3c8, 0x3ca, 0x3cb, 0x3cd, 0x3cf, 0x3d1, 0x3d2, 0x3d4, 0x3d6,
                0x3d8, 0x3da, 0x3db, 0x3dd, 0x3df, 0x3e1, 0x3e3, 0x3e4, 0x3e6, 0x3e8, 0x3ea, 0x3ec, 0x3ed, 0x3ef, 0x3f1,
                0x3f3, 0x3f5, 0x3f6, 0x3f8, 0x3fa, 0x3fc, 0x3fe, 0x36c
        };
        private final Opl2 chip;
        private final Instrument[] instruments;
        private final Channel[] channels = new Channel[16];
        private final List<OplVoice> free = new ArrayList<>();
        private final List<OplVoice> allocated = new ArrayList<>();
        Dmx(final Opl2 chip, final Instrument[] instruments) {
            this.chip = chip;
            this.instruments = instruments;
            for (int i = 0; i < 16; i++) {
                channels[i] = new Channel(i);
            }
            for (int i = 0; i < VOICES; i++) {
                free.add(new OplVoice(i));
            }
            for (int r = 0x40; r <= 0x40 + 0x15; r++) {
                chip.write(r, 0x3f);
            }
            for (int r = 0x60; r <= 0xf5; r++) {
                chip.write(r, 0x00);
            }
            chip.write(0x01, 0x20);
            chip.write(0x08, 0x40);
        }

        private static int channelIndex(final OplVoice v) {
            return v.channel == null ? -1 : v.channel.index;
        }

        void handle(final Event e) {
            final Channel channel = channels[e.channel()];
            switch (e.type()) {
                case Event.NOTE_ON -> keyOn(channel, e.p1(), e.p2());
                case Event.NOTE_OFF -> keyOff(channel, e.p1());
                case Event.PROGRAM -> channel.instrument = instruments[e.p1() & 127];
                case Event.VOLUME -> setChannelVolume(channel, e.p1());
                case Event.BEND -> {
                    channel.bend = e.p1();
                    for (final OplVoice v : List.copyOf(allocated)) {
                        if (v.channel == channel) {
                            updateFrequency(v);
                        }
                    }
                }
                case Event.ALL_NOTES_OFF -> {
                    for (int i = 0; i < allocated.size(); i++) {
                        if (allocated.get(i).channel == channel) {
                            release(i);
                            i--;
                        }
                    }
                }
                default -> {
                }
            }
        }

        private void release(final int index) {
            final OplVoice voice = allocated.remove(index);
            chip.write(0xb0 + voice.index, voice.freq >> 8);
            voice.channel = null;
            voice.note = 0;
            free.add(voice);
        }

        private void keyOff(final Channel channel, final int key) {
            for (int i = 0; i < allocated.size(); i++) {
                final OplVoice v = allocated.get(i);
                if (v.channel == channel && v.key == key) {
                    release(i);
                    i--;
                }
            }
        }

        private void replaceExisting() {
            int result = 0;
            for (int i = 0; i < allocated.size(); i++) {
                final OplVoice v = allocated.get(i);
                final OplVoice r = allocated.get(result);
                if (v.instrumentVoice != 0 || channelIndex(v) >= channelIndex(r)) {
                    result = i;
                }
            }
            release(result);
        }

        private void keyOn(final Channel channel, final int key, final int volume) {
            if (volume <= 0) {
                keyOff(channel, key);
                return;
            }
            final Instrument instrument;
            int note = key;
            if (channel.index == 15) {
                if (key < 35 || key > 81) {
                    return;
                }
                instrument = instruments[128 + key - 35];
                note = 60;
            } else {
                instrument = channel.instrument;
            }
            if (free.isEmpty()) {
                replaceExisting();
            }
            voiceKeyOn(channel, instrument, 0, note, key, volume);
            if ((instrument.flags() & Instrument.DOUBLE) != 0) {
                voiceKeyOn(channel, instrument, 1, note, key, volume);
            }
        }

        private void voiceKeyOn(final Channel channel, final Instrument instrument, final int instrumentVoice,
                                final int note, final int key, final int volume) {
            if (free.isEmpty()) {
                return;
            }
            final OplVoice voice = free.removeFirst();
            allocated.add(voice);
            voice.channel = channel;
            voice.key = key;
            voice.note = (instrument.flags() & Instrument.FIXED) != 0 ? instrument.fixedNote() : note;
            setInstrument(voice, instrument, instrumentVoice);
            setVolume(voice, volume);
            voice.freq = 0;
            updateFrequency(voice);
        }

        private int loadOperator(final int operator, final Operator data, final boolean maxLevel) {
            final int level = data.scale() | (maxLevel ? 0x3f : data.level());
            chip.write(0x40 + operator, level);
            chip.write(0x20 + operator, data.tremolo());
            chip.write(0x60 + operator, data.attack());
            chip.write(0x80 + operator, data.sustain());
            chip.write(0xe0 + operator, data.waveform());
            return level;
        }

        private void setInstrument(final OplVoice voice, final Instrument instrument, final int instrumentVoice) {
            if (voice.instrument == instrument && voice.instrumentVoice == instrumentVoice) {
                return;
            }
            voice.instrument = instrument;
            voice.instrumentVoice = instrumentVoice;
            final Voice data = instrument.voices()[instrumentVoice];
            final boolean modulating = (data.feedback() & 0x01) == 0;
            voice.carVolume = loadOperator(voice.op2, data.carrier(), true);
            voice.modVolume = loadOperator(voice.op1, data.modulator(), !modulating);
            chip.write(0xc0 + voice.index, data.feedback() | 0x30);
        }

        private void setVolume(final OplVoice voice, final int volume) {
            voice.noteVolume = volume;
            final Instrument instrument = voice.instrument;
            final Channel channel = voice.channel;
            if (instrument == null || channel == null) {
                return;
            }
            final Voice data = instrument.voices()[voice.instrumentVoice];
            final int midiVolume = 2 * (VOLUME_MAPPING[channel.volume] + 1);
            final int fullVolume = (VOLUME_MAPPING[voice.noteVolume] * midiVolume) >> 9;
            final int carVolume = 0x3f - fullVolume;
            if (carVolume != (voice.carVolume & 0x3f)) {
                voice.carVolume = carVolume | (voice.carVolume & 0xc0);
                chip.write(0x40 + voice.op2, voice.carVolume);
                if ((data.feedback() & 0x01) != 0 && data.modulator().level() != 0x3f) {
                    int modVolume = data.modulator().level();
                    if (modVolume < carVolume) {
                        modVolume = carVolume;
                    }
                    modVolume |= voice.modVolume & 0xc0;
                    if (modVolume != voice.modVolume) {
                        voice.modVolume = modVolume;
                        chip.write(0x40 + voice.op1, modVolume | (data.modulator().scale() & 0xc0));
                    }
                }
            }
        }

        private void setChannelVolume(final Channel channel, final int volume) {
            channel.volume = Math.min(127, volume);
            for (final OplVoice v : allocated) {
                if (v.channel == channel) {
                    setVolume(v, v.noteVolume);
                }
            }
        }

        private int frequency(final OplVoice voice) {
            final Instrument instrument = voice.instrument;
            final Channel channel = voice.channel;
            if (instrument == null || channel == null) {
                return 0;
            }
            int note = voice.note;
            if ((instrument.flags() & Instrument.FIXED) == 0) {
                note += instrument.voices()[voice.instrumentVoice].baseNoteOffset();
            }
            while (note < 0) {
                note += 12;
            }
            while (note > 95) {
                note -= 12;
            }
            int index = 64 + 32 * note + channel.bend;
            if (voice.instrumentVoice != 0) {
                index += instrument.fineTuning() / 2 - 64;
            }
            if (index < 0) {
                index = 0;
            }
            if (index < 284) {
                return FREQUENCY_CURVE[index];
            }
            final int sub = (index - 284) % (12 * 32);
            final int octave = Math.min(7, (index - 284) / (12 * 32));
            return FREQUENCY_CURVE[sub + 284] | octave << 10;
        }

        private void updateFrequency(final OplVoice voice) {
            final int freq = frequency(voice);
            if (voice.freq != freq) {
                chip.write(0xa0 + voice.index, freq & 0xff);
                chip.write(0xb0 + voice.index, (freq >> 8) | 0x20);
                voice.freq = freq;
            }
        }

        private final class Channel {
            final int index;
            Instrument instrument;
            int volume = 100;
            int bend;

            Channel(final int index) {
                this.index = index;
                this.instrument = instruments[0];
            }
        }

        private final class OplVoice {
            final int index;
            final int op1;
            final int op2;
            @Nullable Instrument instrument;
            int instrumentVoice;
            @Nullable Channel channel;
            int key;
            int note;
            int freq;
            int noteVolume;
            int carVolume;
            int modVolume;

            OplVoice(final int index) {
                this.index = index;
                this.op1 = OPERATORS[0][index];
                this.op2 = OPERATORS[1][index];
            }
        }
    }
}
