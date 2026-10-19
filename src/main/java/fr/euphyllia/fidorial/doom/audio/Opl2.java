// Nuked OPL3 by Nuke.YKT (Alexey Khokholov), Copyright (C) 2013-2020.
// Reduced to the OPL2 (YM3812) subset and ported to Java for Fidorial: 9 two-operator channels,
// no rhythm mode, mono output. Licensed under the GNU Lesser General Public License v2.1 or later.
package fr.euphyllia.fidorial.doom.audio;

/**
 * A cycle accurate YM3812 (OPL2) FM synthesizer, as the Sound Blaster and AdLib cards Doom played
 * its music on. It runs at the chip rate of {@value #RATE} Hz.
 */
final class Opl2 {

    /**
     * The native sample rate: a 14.31818 MHz clock divided by 288.
     */
    static final int RATE = 49716;

    private static final int[] LOGSIN = {
            0x859, 0x6c3, 0x607, 0x58b, 0x52e, 0x4e4, 0x4a6, 0x471, 0x443, 0x41a, 0x3f5, 0x3d3, 0x3b5, 0x398, 0x37e, 0x365,
            0x34e, 0x339, 0x324, 0x311, 0x2ff, 0x2ed, 0x2dc, 0x2cd, 0x2bd, 0x2af, 0x2a0, 0x293, 0x286, 0x279, 0x26d, 0x261,
            0x256, 0x24b, 0x240, 0x236, 0x22c, 0x222, 0x218, 0x20f, 0x206, 0x1fd, 0x1f5, 0x1ec, 0x1e4, 0x1dc, 0x1d4, 0x1cd,
            0x1c5, 0x1be, 0x1b7, 0x1b0, 0x1a9, 0x1a2, 0x19b, 0x195, 0x18f, 0x188, 0x182, 0x17c, 0x177, 0x171, 0x16b, 0x166,
            0x160, 0x15b, 0x155, 0x150, 0x14b, 0x146, 0x141, 0x13c, 0x137, 0x133, 0x12e, 0x129, 0x125, 0x121, 0x11c, 0x118,
            0x114, 0x10f, 0x10b, 0x107, 0x103, 0x0ff, 0x0fb, 0x0f8, 0x0f4, 0x0f0, 0x0ec, 0x0e9, 0x0e5, 0x0e2, 0x0de, 0x0db,
            0x0d7, 0x0d4, 0x0d1, 0x0cd, 0x0ca, 0x0c7, 0x0c4, 0x0c1, 0x0be, 0x0bb, 0x0b8, 0x0b5, 0x0b2, 0x0af, 0x0ac, 0x0a9,
            0x0a7, 0x0a4, 0x0a1, 0x09f, 0x09c, 0x099, 0x097, 0x094, 0x092, 0x08f, 0x08d, 0x08a, 0x088, 0x086, 0x083, 0x081,
            0x07f, 0x07d, 0x07a, 0x078, 0x076, 0x074, 0x072, 0x070, 0x06e, 0x06c, 0x06a, 0x068, 0x066, 0x064, 0x062, 0x060,
            0x05e, 0x05c, 0x05b, 0x059, 0x057, 0x055, 0x053, 0x052, 0x050, 0x04e, 0x04d, 0x04b, 0x04a, 0x048, 0x046, 0x045,
            0x043, 0x042, 0x040, 0x03f, 0x03e, 0x03c, 0x03b, 0x039, 0x038, 0x037, 0x035, 0x034, 0x033, 0x031, 0x030, 0x02f,
            0x02e, 0x02d, 0x02b, 0x02a, 0x029, 0x028, 0x027, 0x026, 0x025, 0x024, 0x023, 0x022, 0x021, 0x020, 0x01f, 0x01e,
            0x01d, 0x01c, 0x01b, 0x01a, 0x019, 0x018, 0x017, 0x017, 0x016, 0x015, 0x014, 0x014, 0x013, 0x012, 0x011, 0x011,
            0x010, 0x00f, 0x00f, 0x00e, 0x00d, 0x00d, 0x00c, 0x00c, 0x00b, 0x00a, 0x00a, 0x009, 0x009, 0x008, 0x008, 0x007,
            0x007, 0x007, 0x006, 0x006, 0x005, 0x005, 0x005, 0x004, 0x004, 0x004, 0x003, 0x003, 0x003, 0x002, 0x002, 0x002,
            0x002, 0x001, 0x001, 0x001, 0x001, 0x001, 0x001, 0x001, 0x000, 0x000, 0x000, 0x000, 0x000, 0x000, 0x000, 0x000
    };
    private static final int[] EXP = {
            0x7fa, 0x7f5, 0x7ef, 0x7ea, 0x7e4, 0x7df, 0x7da, 0x7d4, 0x7cf, 0x7c9, 0x7c4, 0x7bf, 0x7b9, 0x7b4, 0x7ae, 0x7a9,
            0x7a4, 0x79f, 0x799, 0x794, 0x78f, 0x78a, 0x784, 0x77f, 0x77a, 0x775, 0x770, 0x76a, 0x765, 0x760, 0x75b, 0x756,
            0x751, 0x74c, 0x747, 0x742, 0x73d, 0x738, 0x733, 0x72e, 0x729, 0x724, 0x71f, 0x71a, 0x715, 0x710, 0x70b, 0x706,
            0x702, 0x6fd, 0x6f8, 0x6f3, 0x6ee, 0x6e9, 0x6e5, 0x6e0, 0x6db, 0x6d6, 0x6d2, 0x6cd, 0x6c8, 0x6c4, 0x6bf, 0x6ba,
            0x6b5, 0x6b1, 0x6ac, 0x6a8, 0x6a3, 0x69e, 0x69a, 0x695, 0x691, 0x68c, 0x688, 0x683, 0x67f, 0x67a, 0x676, 0x671,
            0x66d, 0x668, 0x664, 0x65f, 0x65b, 0x657, 0x652, 0x64e, 0x649, 0x645, 0x641, 0x63c, 0x638, 0x634, 0x630, 0x62b,
            0x627, 0x623, 0x61e, 0x61a, 0x616, 0x612, 0x60e, 0x609, 0x605, 0x601, 0x5fd, 0x5f9, 0x5f5, 0x5f0, 0x5ec, 0x5e8,
            0x5e4, 0x5e0, 0x5dc, 0x5d8, 0x5d4, 0x5d0, 0x5cc, 0x5c8, 0x5c4, 0x5c0, 0x5bc, 0x5b8, 0x5b4, 0x5b0, 0x5ac, 0x5a8,
            0x5a4, 0x5a0, 0x59c, 0x599, 0x595, 0x591, 0x58d, 0x589, 0x585, 0x581, 0x57e, 0x57a, 0x576, 0x572, 0x56f, 0x56b,
            0x567, 0x563, 0x560, 0x55c, 0x558, 0x554, 0x551, 0x54d, 0x549, 0x546, 0x542, 0x53e, 0x53b, 0x537, 0x534, 0x530,
            0x52c, 0x529, 0x525, 0x522, 0x51e, 0x51b, 0x517, 0x514, 0x510, 0x50c, 0x509, 0x506, 0x502, 0x4ff, 0x4fb, 0x4f8,
            0x4f4, 0x4f1, 0x4ed, 0x4ea, 0x4e7, 0x4e3, 0x4e0, 0x4dc, 0x4d9, 0x4d6, 0x4d2, 0x4cf, 0x4cc, 0x4c8, 0x4c5, 0x4c2,
            0x4be, 0x4bb, 0x4b8, 0x4b5, 0x4b1, 0x4ae, 0x4ab, 0x4a8, 0x4a4, 0x4a1, 0x49e, 0x49b, 0x498, 0x494, 0x491, 0x48e,
            0x48b, 0x488, 0x485, 0x482, 0x47e, 0x47b, 0x478, 0x475, 0x472, 0x46f, 0x46c, 0x469, 0x466, 0x463, 0x460, 0x45d,
            0x45a, 0x457, 0x454, 0x451, 0x44e, 0x44b, 0x448, 0x445, 0x442, 0x43f, 0x43c, 0x439, 0x436, 0x433, 0x430, 0x42d,
            0x42a, 0x428, 0x425, 0x422, 0x41f, 0x41c, 0x419, 0x416, 0x414, 0x411, 0x40e, 0x40b, 0x408, 0x406, 0x403, 0x400
    };
    /**
     * Frequency multipliers times two.
     */
    private static final int[] MT = {1, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 20, 24, 24, 30, 30};
    private static final int[] KSL_ROM = {0, 32, 40, 45, 48, 51, 53, 55, 56, 58, 59, 60, 61, 62, 63, 64};
    private static final int[] KSL_SHIFT = {8, 1, 2, 0};
    private static final int[][] EG_INCSTEP = {{0, 0, 0, 0}, {1, 0, 0, 0}, {1, 0, 1, 0}, {1, 1, 1, 0}};
    private static final int[] AD_SLOT = {
            0, 1, 2, 3, 4, 5, -1, -1, 6, 7, 8, 9, 10, 11, -1, -1,
            12, 13, 14, 15, 16, 17, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
    };
    private static final int[] CH_SLOT = {0, 1, 2, 6, 7, 8, 12, 13, 14};

    private static final int ATTACK = 0;
    private static final int DECAY = 1;
    private static final int SUSTAIN = 2;
    private static final int RELEASE = 3;
    private final Slot[] slots = new Slot[18];
    private final Channel[] channels = new Channel[9];
    private int nts;
    private int timer;
    private long egTimer;
    private boolean egTimerRem;
    private int egState;
    private int egAdd;
    private int egTimerLo;
    private int tremoloPos;
    private int tremolo;
    private int tremoloShift = 4;
    private int vibPos;
    private int vibShift = 1;
    Opl2() {
        for (int ch = 0; ch < 9; ch++) {
            final Channel c = new Channel();
            channels[ch] = c;
            c.slot0 = new Slot(c, true);
            c.slot1 = new Slot(c, false);
            slots[CH_SLOT[ch]] = c.slot0;
            slots[CH_SLOT[ch] + 3] = c.slot1;
        }
    }

    private static int calcExp(int level) {
        if (level > 0x1fff) {
            level = 0x1fff;
        }
        return (EXP[level & 0xff] << 1) >> (level >> 8);
    }

    private static int sin(final int wf, int phase, final int envelope) {
        phase &= 0x3ff;
        final int out;
        boolean neg = false;
        switch (wf) {
            case 0 -> {
                neg = (phase & 0x200) != 0;
                out = (phase & 0x100) != 0 ? LOGSIN[(phase & 0xff) ^ 0xff] : LOGSIN[phase & 0xff];
            }
            case 1 -> {
                if ((phase & 0x200) != 0) {
                    out = 0x1000;
                } else {
                    out = (phase & 0x100) != 0 ? LOGSIN[(phase & 0xff) ^ 0xff] : LOGSIN[phase & 0xff];
                }
            }
            case 2 -> out = (phase & 0x100) != 0 ? LOGSIN[(phase & 0xff) ^ 0xff] : LOGSIN[phase & 0xff];
            default -> out = (phase & 0x100) != 0 ? 0x1000 : LOGSIN[phase & 0xff];
        }
        final int value = calcExp(out + (envelope << 3));
        // int16 xor 0xffff is a bitwise not
        return neg ? ~value : value;
    }

    private static void updateKsl(final Slot slot) {
        int ksl = (KSL_ROM[slot.channel.fNum >> 6] << 2) - ((0x08 - slot.channel.block) << 5);
        if (ksl < 0) {
            ksl = 0;
        }
        slot.egKsl = ksl & 0xff;
    }

    private static int modulation(final Slot slot) {
        if (slot.modulator) {
            return slot.fbmod;
        }
        return slot.channel.con == 0 ? slot.channel.slot0.out : 0;
    }

    private void envelopeCalc(final Slot slot) {
        slot.egOut = slot.egRout + (slot.regTl << 2) + (slot.egKsl >> KSL_SHIFT[slot.regKsl]) + (slot.trem ? tremolo : 0);
        int regRate = 0;
        boolean reset = false;
        if (slot.key != 0 && slot.egGen == RELEASE) {
            reset = true;
            regRate = slot.regAr;
        } else {
            switch (slot.egGen) {
                case ATTACK -> regRate = slot.regAr;
                case DECAY -> regRate = slot.regDr;
                case SUSTAIN -> regRate = slot.regType ? 0 : slot.regRr;
                default -> regRate = slot.regRr;
            }
        }
        slot.pgReset = reset;
        final int ks = slot.channel.ksv >> ((slot.regKsr ^ 1) << 1);
        final boolean nonzero = regRate != 0;
        final int rate = ks + (regRate << 2);
        int rateHi = rate >> 2;
        final int rateLo = rate & 0x03;
        if ((rateHi & 0x10) != 0) {
            rateHi = 0x0f;
        }
        final int egShift = rateHi + egAdd;
        int shift = 0;
        if (nonzero) {
            if (rateHi < 12) {
                if (egState != 0) {
                    switch (egShift) {
                        case 12 -> shift = 1;
                        case 13 -> shift = (rateLo >> 1) & 0x01;
                        case 14 -> shift = rateLo & 0x01;
                        default -> {
                        }
                    }
                }
            } else {
                shift = (rateHi & 0x03) + EG_INCSTEP[rateLo][egTimerLo];
                if ((shift & 0x04) != 0) {
                    shift = 0x03;
                }
                if (shift == 0) {
                    shift = egState;
                }
            }
        }
        int egRout = slot.egRout;
        int egInc = 0;
        boolean egOff = false;
        if (reset && rateHi == 0x0f) {
            egRout = 0x00;
        }
        if ((slot.egRout & 0x1f8) == 0x1f8) {
            egOff = true;
        }
        if (slot.egGen != ATTACK && !reset && egOff) {
            egRout = 0x1ff;
        }
        switch (slot.egGen) {
            case ATTACK -> {
                if (slot.egRout == 0) {
                    slot.egGen = DECAY;
                } else if (slot.key != 0 && shift > 0 && rateHi != 0x0f) {
                    egInc = (~slot.egRout) >> (4 - shift);
                }
            }
            case DECAY -> {
                if ((slot.egRout >> 4) == slot.regSl) {
                    slot.egGen = SUSTAIN;
                } else if (!egOff && !reset && shift > 0) {
                    egInc = 1 << (shift - 1);
                }
            }
            default -> {
                if (!egOff && !reset && shift > 0) {
                    egInc = 1 << (shift - 1);
                }
            }
        }
        slot.egRout = (egRout + egInc) & 0x1ff;
        if (reset) {
            slot.egGen = ATTACK;
        }
        if (slot.key == 0) {
            slot.egGen = RELEASE;
        }
    }

    private void phaseGenerate(final Slot slot) {
        int fNum = slot.channel.fNum;
        if (slot.regVib) {
            int range = (fNum >> 7) & 7;
            if ((vibPos & 3) == 0) {
                range = 0;
            } else if ((vibPos & 1) != 0) {
                range >>= 1;
            }
            range >>= vibShift;
            if ((vibPos & 4) != 0) {
                range = -range;
            }
            fNum += range;
        }
        final int basefreq = (fNum << slot.channel.block) >> 1;
        final int phase = (slot.pgPhase >>> 9) & 0xffff;
        if (slot.pgReset) {
            slot.pgPhase = 0;
        }
        slot.pgPhase += (basefreq * MT[slot.regMult]) >> 1;
        slot.pgPhaseOut = phase;
    }

    private void processSlot(final Slot slot) {
        if (slot.channel.fb != 0) {
            slot.fbmod = (short) ((slot.prout + slot.out) >> (0x09 - slot.channel.fb));
        } else {
            slot.fbmod = 0;
        }
        slot.prout = slot.out;
        envelopeCalc(slot);
        phaseGenerate(slot);
        slot.out = (short) sin(slot.regWf, slot.pgPhaseOut + modulation(slot), slot.egOut);
    }

    /**
     * {@return the next sample, a signed 16-bit value}
     */
    int generate() {
        for (final Slot slot : slots) {
            processSlot(slot);
        }
        int mix = 0;
        for (final Channel c : channels) {
            final int accm = c.con == 0 ? c.slot1.out : c.slot0.out + c.slot1.out;
            mix += (short) accm;
        }
        if ((timer & 0x3f) == 0x3f) {
            tremoloPos = (tremoloPos + 1) % 210;
        }
        tremolo = tremoloPos < 105 ? tremoloPos >> tremoloShift : (210 - tremoloPos) >> tremoloShift;
        if ((timer & 0x3ff) == 0x3ff) {
            vibPos = (vibPos + 1) & 7;
        }
        timer++;
        if (egState != 0) {
            int shift = 0;
            while (shift < 13 && ((egTimer >> shift) & 1) == 0) {
                shift++;
            }
            egAdd = shift > 12 ? 0 : shift + 1;
            egTimerLo = (int) (egTimer & 0x3);
        }
        if (egTimerRem || egState != 0) {
            if (egTimer == 0xfffffffffL) {
                egTimer = 0;
                egTimerRem = true;
            } else {
                egTimer++;
                egTimerRem = false;
            }
        }
        egState ^= 1;
        return Math.max(-32768, Math.min(32767, mix));
    }

    private void updateFrequency(final Channel c) {
        c.ksv = (c.block << 1) | ((c.fNum >> (0x09 - nts)) & 0x01);
        updateKsl(c.slot0);
        updateKsl(c.slot1);
    }

    /**
     * Writes a register, as a program would through port 0x388.
     */
    void write(final int reg, final int v) {
        final int regm = reg & 0xff;
        switch (regm & 0xf0) {
            case 0x00 -> {
                if (regm == 0x08) {
                    nts = (v >> 6) & 0x01;
                }
            }
            case 0x20, 0x30 -> {
                final int s = AD_SLOT[regm & 0x1f];
                if (s >= 0) {
                    final Slot slot = slots[s];
                    slot.trem = ((v >> 7) & 0x01) != 0;
                    slot.regVib = ((v >> 6) & 0x01) != 0;
                    slot.regType = ((v >> 5) & 0x01) != 0;
                    slot.regKsr = (v >> 4) & 0x01;
                    slot.regMult = v & 0x0f;
                }
            }
            case 0x40, 0x50 -> {
                final int s = AD_SLOT[regm & 0x1f];
                if (s >= 0) {
                    final Slot slot = slots[s];
                    slot.regKsl = (v >> 6) & 0x03;
                    slot.regTl = v & 0x3f;
                    updateKsl(slot);
                }
            }
            case 0x60, 0x70 -> {
                final int s = AD_SLOT[regm & 0x1f];
                if (s >= 0) {
                    slots[s].regAr = (v >> 4) & 0x0f;
                    slots[s].regDr = v & 0x0f;
                }
            }
            case 0x80, 0x90 -> {
                final int s = AD_SLOT[regm & 0x1f];
                if (s >= 0) {
                    int sl = (v >> 4) & 0x0f;
                    if (sl == 0x0f) {
                        sl = 0x1f;
                    }
                    slots[s].regSl = sl;
                    slots[s].regRr = v & 0x0f;
                }
            }
            case 0xe0, 0xf0 -> {
                final int s = AD_SLOT[regm & 0x1f];
                if (s >= 0) {
                    slots[s].regWf = v & 0x03;
                }
            }
            case 0xa0 -> {
                if ((regm & 0x0f) < 9) {
                    final Channel c = channels[regm & 0x0f];
                    c.fNum = (c.fNum & 0x300) | v;
                    updateFrequency(c);
                }
            }
            case 0xb0 -> {
                if (regm == 0xbd) {
                    tremoloShift = (((v >> 7) ^ 1) << 1) + 2;
                    vibShift = ((v >> 6) & 0x01) ^ 1;
                } else if ((regm & 0x0f) < 9) {
                    final Channel c = channels[regm & 0x0f];
                    c.fNum = (c.fNum & 0xff) | ((v & 0x03) << 8);
                    c.block = (v >> 2) & 0x07;
                    updateFrequency(c);
                    if ((v & 0x20) != 0) {
                        c.slot0.key |= 1;
                        c.slot1.key |= 1;
                    } else {
                        c.slot0.key &= ~1;
                        c.slot1.key &= ~1;
                    }
                }
            }
            case 0xc0 -> {
                if ((regm & 0x0f) < 9) {
                    final Channel c = channels[regm & 0x0f];
                    c.fb = (v & 0x0e) >> 1;
                    c.con = v & 0x01;
                }
            }
            default -> {
            }
        }
    }

    private static final class Slot {
        Channel channel;
        boolean modulator;
        int out;
        int fbmod;
        int prout;
        int egRout = 0x1ff;
        int egOut = 0x1ff;
        int egGen = RELEASE;
        int egKsl;
        int key;
        boolean pgReset;
        int pgPhase;
        int pgPhaseOut;
        boolean trem;
        boolean regVib;
        boolean regType;
        int regKsr;
        int regMult;
        int regKsl;
        int regTl;
        int regAr;
        int regDr;
        int regSl;
        int regRr;
        int regWf;

        Slot(final Channel channel, final boolean modulator) {
            this.channel = channel;
            this.modulator = modulator;
        }
    }

    private static final class Channel {
        Slot slot0;
        Slot slot1;
        int fNum;
        int block;
        int fb;
        int con;
        int ksv;
    }
}
