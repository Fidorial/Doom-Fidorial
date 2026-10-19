package fr.euphyllia.fidorial.doom.audio;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A small Ogg Vorbis encoder for mono sounds, so that a resource pack can be built from any WAD
 * without native tools.
 *
 * <p>It uses a single block size, a straight line floor and one scalar residue codebook whose code
 * lengths are fitted to each sound. That is far from libvorbis, but plenty for 8-bit Doom sounds and
 * synthesized music, and every Vorbis I decoder accepts it.</p>
 */
public final class VorbisEncoder {

    private static final int BLOCK_EXP = 9;
    /**
     * The block size: each audio packet covers {@code N} samples, half of them overlapping.
     */
    private static final int N = 1 << BLOCK_EXP;
    private static final int HALF = N / 2;
    private static final int PARTITION = 32;
    private static final int RESIDUE_MAX = 127;
    private static final int RESIDUE_ENTRIES = 256;

    private static final float[] INVERSE_DB = new float[256];
    private static final float[] WINDOW = new float[N];
    private static final double[][] COS = new double[HALF][N];

    static {
        for (int i = 0; i < 256; i++) {
            // the floor1 inverse dB table of the Vorbis I specification is this geometric series
            INVERSE_DB[i] = (float) (1.0649863e-07 * Math.pow(1.0 / 1.0649863e-07, i / 255.0));
        }
        INVERSE_DB[255] = 1.0f;
        for (int n = 0; n < N; n++) {
            final double s = Math.sin((n + 0.5) / N * Math.PI);
            WINDOW[n] = (float) Math.sin(Math.PI / 2 * s * s);
        }
        for (int k = 0; k < HALF; k++) {
            for (int n = 0; n < N; n++) {
                COS[k][n] = Math.cos(2 * Math.PI / N * (n + 0.5 + N / 4.0) * (k + 0.5));
            }
        }
    }

    private VorbisEncoder() {
    }

    private static int ilog(int v) {
        int r = 0;
        while (v > 0) {
            r++;
            v >>>= 1;
        }
        return r;
    }

    /**
     * The Vorbis float format: 21-bit mantissa, 10-bit exponent biased by 788, sign.
     */
    private static long vorbisFloat(final int mantissa, final int exponent, final boolean negative) {
        return (mantissa & 0x1fffffL) | ((long) (exponent + 788) << 21) | (negative ? 0x80000000L : 0);
    }

    /**
     * Encodes signed samples in {@code [-1, 1]}.
     *
     * @param samples    the samples
     * @param sampleRate the sample rate in hertz
     * @return the Ogg Vorbis file
     */
    public static byte[] encode(final float[] samples, final int sampleRate) {
        return encode(samples, sampleRate, RESIDUE_MAX);
    }

    /**
     * Encodes signed samples in {@code [-1, 1]} at a given precision.
     *
     * @param samples    the samples
     * @param sampleRate the sample rate in hertz
     * @param residueMax the quantization range of the loudest coefficient of each block, up to 127;
     *                   lower values give smaller files
     * @return the Ogg Vorbis file
     */
    public static byte[] encode(final float[] samples, final int sampleRate, final int residueMax) {
        final int range = Math.clamp(residueMax, 1, RESIDUE_MAX);
        final int blocks = (samples.length + HALF - 1) / HALF + 1;
        final int[] floorY = new int[blocks];
        final byte[][] residues = new byte[blocks][];
        final long[] histogram = new long[RESIDUE_ENTRIES];
        final double[] frame = new double[N];
        for (int b = 0; b < blocks; b++) {
            // block b covers samples [(b - 1) * HALF, (b + 1) * HALF)
            final int start = (b - 1) * HALF;
            for (int n = 0; n < N; n++) {
                final int i = start + n;
                frame[n] = i >= 0 && i < samples.length ? samples[i] * WINDOW[n] : 0.0;
            }
            final double[] coef = new double[HALF];
            double max = 0;
            for (int k = 0; k < HALF; k++) {
                double sum = 0;
                final double[] cos = COS[k];
                for (int n = 0; n < N; n++) {
                    sum += frame[n] * cos[n];
                }
                coef[k] = sum * (2.0 / HALF);
                max = Math.max(max, Math.abs(coef[k]));
            }
            if (max < INVERSE_DB[0] * 0.5) {
                floorY[b] = -1;
                continue;
            }
            int y = 0;
            while (y < 255 && INVERSE_DB[y] * range < max) {
                y++;
            }
            floorY[b] = y;
            final byte[] r = new byte[HALF];
            final double step = INVERSE_DB[y];
            for (int k = 0; k < HALF; k++) {
                final int q = (int) Math.max(-range, Math.min(range, Math.round(coef[k] / step)));
                r[k] = (byte) q;
            }
            residues[b] = r;
            for (int p = 0; p < HALF; p += PARTITION) {
                if (!silent(r, p)) {
                    for (int k = p; k < p + PARTITION; k++) {
                        histogram[r[k] + 128]++;
                    }
                }
            }
        }
        final int[] lengths = Huffman.lengths(histogram, 24);
        final int[] codes = Huffman.vorbisCodewords(lengths);

        final List<byte[]> packets = new ArrayList<>();
        packets.add(identification(sampleRate));
        packets.add(comment());
        packets.add(setup(lengths));
        for (int b = 0; b < blocks; b++) {
            packets.add(audio(floorY[b], residues[b], lengths, codes));
        }
        return Ogg.write(packets, samples.length, HALF);
    }

    private static boolean silent(final byte[] r, final int from) {
        for (int k = from; k < from + PARTITION; k++) {
            if (r[k] != 0) {
                return false;
            }
        }
        return true;
    }

    private static void header(final BitWriter w, final int type) {
        w.write(type, 8);
        w.bytes("vorbis".getBytes(StandardCharsets.US_ASCII));
    }

    private static byte[] identification(final int sampleRate) {
        final BitWriter w = new BitWriter();
        header(w, 1);
        w.write(0, 32);
        w.write(1, 8);
        w.write(sampleRate, 32);
        w.write(0, 32);
        w.write(0, 32);
        w.write(0, 32);
        w.write(BLOCK_EXP, 4);
        w.write(BLOCK_EXP, 4);
        w.write(1, 1);
        return w.toByteArray();
    }

    private static byte[] comment() {
        final BitWriter w = new BitWriter();
        header(w, 3);
        final byte[] vendor = "Fidorial Doom".getBytes(StandardCharsets.UTF_8);
        w.write(vendor.length, 32);
        w.bytes(vendor);
        w.write(0, 32);
        w.write(1, 1);
        return w.toByteArray();
    }

    private static void codebook(final BitWriter w, final int dimensions, final int[] lengths) {
        w.write(0x564342, 24);
        w.write(dimensions, 16);
        w.write(lengths.length, 24);
        boolean sparse = false;
        for (final int l : lengths) {
            if (l == 0) {
                sparse = true;
                break;
            }
        }
        w.write(0, 1);
        w.write(sparse ? 1 : 0, 1);
        for (final int l : lengths) {
            if (sparse) {
                w.write(l > 0 ? 1 : 0, 1);
                if (l > 0) {
                    w.write(l - 1, 5);
                }
            } else {
                w.write(l - 1, 5);
            }
        }
    }

    private static byte[] setup(final int[] residueLengths) {
        final BitWriter w = new BitWriter();
        header(w, 5);
        // codebooks: 0 = residue classification (2 classes), 1 = residue values -128..127
        w.write(2 - 1, 8);
        codebook(w, 1, new int[]{1, 1});
        w.write(0, 4);
        codebook(w, 1, residueLengths);
        w.write(1, 4);
        w.write(vorbisFloat(128, 0, true), 32);
        w.write(vorbisFloat(1, 0, false), 32);
        w.write(8 - 1, 4);
        w.write(0, 1);
        for (int i = 0; i < RESIDUE_ENTRIES; i++) {
            w.write(i, 8);
        }
        // time domain transforms, unused
        w.write(0, 6);
        w.write(0, 16);
        // one floor1 without extra posts: a straight line over the half block
        w.write(0, 6);
        w.write(1, 16);
        w.write(0, 5);
        w.write(0, 2);
        w.write(BLOCK_EXP - 1, 4);
        // one residue type 1
        w.write(0, 6);
        w.write(1, 16);
        w.write(0, 24);
        w.write(HALF, 24);
        w.write(PARTITION - 1, 24);
        w.write(2 - 1, 6);
        w.write(0, 8);
        // class 0: nothing coded; class 1: values in pass 0
        w.write(0, 3);
        w.write(0, 1);
        w.write(1, 3);
        w.write(0, 1);
        w.write(1, 8);
        // one mapping
        w.write(0, 6);
        w.write(0, 16);
        w.write(0, 1);
        w.write(0, 1);
        w.write(0, 2);
        w.write(0, 8);
        w.write(0, 8);
        w.write(0, 8);
        // one mode
        w.write(0, 6);
        w.write(0, 1);
        w.write(0, 16);
        w.write(0, 16);
        w.write(0, 8);
        w.write(1, 1);
        return w.toByteArray();
    }

    private static void huffman(final BitWriter w, final int code, final int length) {
        // codewords are read most significant bit first
        for (int i = length - 1; i >= 0; i--) {
            w.write((code >> i) & 1, 1);
        }
    }

    private static byte[] audio(final int y, final byte[] residue, final int[] lengths, final int[] codes) {
        final BitWriter w = new BitWriter();
        w.write(0, 1);
        if (y < 0 || residue == null) {
            w.write(0, 1);
            return w.toByteArray();
        }
        w.write(1, 1);
        w.write(y, 8);
        w.write(y, 8);
        final boolean[] coded = new boolean[HALF / PARTITION];
        for (int p = 0; p < coded.length; p++) {
            coded[p] = !silent(residue, p * PARTITION);
        }
        for (int pass = 0; pass < 8; pass++) {
            for (int p = 0; p < coded.length; p++) {
                if (pass == 0) {
                    w.write(coded[p] ? 1 : 0, 1);
                }
                if (pass == 0 && coded[p]) {
                    for (int k = p * PARTITION; k < (p + 1) * PARTITION; k++) {
                        final int entry = residue[k] + 128;
                        huffman(w, codes[entry], lengths[entry]);
                    }
                }
            }
        }
        return w.toByteArray();
    }

    /**
     * Bits written least significant first, as Vorbis packets want them.
     */
    static final class BitWriter {
        private byte[] data = new byte[256];
        private long bits;

        void write(final long value, final int count) {
            for (int i = 0; i < count; i++) {
                final int byteIndex = (int) (bits >> 3);
                if (byteIndex >= data.length) {
                    data = Arrays.copyOf(data, data.length * 2);
                }
                if (((value >> i) & 1) != 0) {
                    data[byteIndex] |= (byte) (1 << (bits & 7));
                }
                bits++;
            }
        }

        void bytes(final byte[] b) {
            for (final byte x : b) {
                write(x & 0xff, 8);
            }
        }

        byte[] toByteArray() {
            return Arrays.copyOf(data, (int) ((bits + 7) >> 3));
        }
    }

    /**
     * Huffman code lengths and the codeword assignment Vorbis decoders use.
     */
    static final class Huffman {

        private Huffman() {
        }

        /**
         * Computes code lengths for a complete code over every symbol, limited in length.
         *
         * @param counts    how often each symbol appears; absent symbols still get a code
         * @param maxLength the longest code allowed
         * @return the length of each symbol
         */
        static int[] lengths(final long[] counts, final int maxLength) {
            final int n = counts.length;
            final long[] weight = new long[n];
            for (int i = 0; i < n; i++) {
                weight[i] = counts[i] + 1;
            }
            int[] lengths = build(weight);
            while (max(lengths) > maxLength) {
                for (int i = 0; i < n; i++) {
                    weight[i] = (weight[i] >> 1) + 1;
                }
                lengths = build(weight);
            }
            return lengths;
        }

        private static int max(final int[] a) {
            int m = 0;
            for (final int v : a) {
                m = Math.max(m, v);
            }
            return m;
        }

        private static int[] build(final long[] weight) {
            final int n = weight.length;
            final long[] w = new long[2 * n];
            final int[] parent = new int[2 * n];
            final boolean[] used = new boolean[2 * n];
            System.arraycopy(weight, 0, w, 0, n);
            int nodes = n;
            for (int step = 0; step < n - 1; step++) {
                int a = -1;
                int b = -1;
                for (int i = 0; i < nodes; i++) {
                    if (used[i]) {
                        continue;
                    }
                    if (a < 0 || w[i] < w[a]) {
                        b = a;
                        a = i;
                    } else if (b < 0 || w[i] < w[b]) {
                        b = i;
                    }
                }
                used[a] = used[b] = true;
                w[nodes] = w[a] + w[b];
                parent[a] = parent[b] = nodes;
                nodes++;
            }
            final int[] lengths = new int[n];
            for (int i = 0; i < n; i++) {
                int len = 0;
                for (int j = i; j != nodes - 1; j = parent[j]) {
                    len++;
                }
                lengths[i] = Math.max(1, len);
            }
            return lengths;
        }

        /**
         * Assigns codewords in symbol order, each taking the lowest free branch of its length, as
         * the Vorbis I specification describes.
         */
        static int[] vorbisCodewords(final int[] lengths) {
            final int[] codes = new int[lengths.length];
            final int[] available = new int[33];
            boolean first = true;
            for (int i = 0; i < lengths.length; i++) {
                final int len = lengths[i];
                if (len == 0) {
                    continue;
                }
                if (first) {
                    codes[i] = 0;
                    for (int j = 1; j <= len; j++) {
                        available[j] = (int) (1L << (32 - j));
                    }
                    first = false;
                    continue;
                }
                int z = len;
                while (z > 0 && available[z] == 0) {
                    z--;
                }
                if (z == 0) {
                    throw new IllegalStateException("over-specified Huffman lengths");
                }
                final int res = available[z];
                available[z] = 0;
                codes[i] = res >>> (32 - len);
                if (z != len) {
                    for (int y = len; y > z; y--) {
                        available[y] = res + (int) (1L << (32 - y));
                    }
                }
            }
            return codes;
        }
    }

    /**
     * Ogg page writer.
     */
    static final class Ogg {

        private static final int[] CRC = new int[256];

        static {
            for (int i = 0; i < 256; i++) {
                int r = i << 24;
                for (int j = 0; j < 8; j++) {
                    r = (r & 0x80000000) != 0 ? (r << 1) ^ 0x04c11db7 : r << 1;
                }
                CRC[i] = r;
            }
        }

        private Ogg() {
        }

        static byte[] write(final List<byte[]> packets, final long totalSamples, final int hop) {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final int serial = 0x46494452;
            int sequence = 0;
            // page 0: identification; page 1: comment and setup; then audio
            sequence = page(out, serial, sequence, 0x02, 0, List.of(packets.get(0)));
            sequence = page(out, serial, sequence, 0, 0, List.of(packets.get(1), packets.get(2)));
            final List<byte[]> current = new ArrayList<>();
            int segments = 0;
            long granule = 0;
            for (int i = 3; i < packets.size(); i++) {
                final byte[] p = packets.get(i);
                final int audioIndex = i - 3;
                final int needed = p.length / 255 + 1;
                if (segments + needed > 255 && !current.isEmpty()) {
                    sequence = page(out, serial, sequence, 0, granule, current);
                    current.clear();
                    segments = 0;
                }
                current.add(p);
                segments += needed;
                // packet k (k >= 1) completes the samples up to k * hop
                granule = Math.min(totalSamples, (long) audioIndex * hop);
            }
            page(out, serial, sequence, 0x04, totalSamples, current);
            return out.toByteArray();
        }

        private static int page(final ByteArrayOutputStream out, final int serial, final int sequence, final int flags,
                                final long granule, final List<byte[]> packets) {
            final ByteArrayOutputStream lacing = new ByteArrayOutputStream();
            final ByteArrayOutputStream body = new ByteArrayOutputStream();
            for (final byte[] p : packets) {
                int left = p.length;
                while (left >= 255) {
                    lacing.write(255);
                    left -= 255;
                }
                lacing.write(left);
                body.writeBytes(p);
            }
            final byte[] lace = lacing.toByteArray();
            if (lace.length > 255) {
                throw new IllegalStateException("packet too large for one page");
            }
            final byte[] header = new byte[27 + lace.length];
            header[0] = 'O';
            header[1] = 'g';
            header[2] = 'g';
            header[3] = 'S';
            header[4] = 0;
            header[5] = (byte) flags;
            for (int i = 0; i < 8; i++) {
                header[6 + i] = (byte) (granule >>> (8 * i));
            }
            for (int i = 0; i < 4; i++) {
                header[14 + i] = (byte) (serial >>> (8 * i));
                header[18 + i] = (byte) (sequence >>> (8 * i));
            }
            header[26] = (byte) lace.length;
            System.arraycopy(lace, 0, header, 27, lace.length);
            final byte[] data = body.toByteArray();
            int crc = 0;
            for (final byte b : header) {
                crc = (crc << 8) ^ CRC[((crc >>> 24) ^ b) & 0xff];
            }
            for (final byte b : data) {
                crc = (crc << 8) ^ CRC[((crc >>> 24) ^ b) & 0xff];
            }
            for (int i = 0; i < 4; i++) {
                header[22 + i] = (byte) (crc >>> (8 * i));
            }
            out.writeBytes(header);
            out.writeBytes(data);
            return sequence + 1;
        }
    }
}
