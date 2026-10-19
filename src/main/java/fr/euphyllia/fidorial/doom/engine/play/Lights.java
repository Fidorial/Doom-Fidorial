// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine.play;

import fr.euphyllia.fidorial.doom.engine.Game;
import fr.euphyllia.fidorial.doom.engine.Line;
import fr.euphyllia.fidorial.doom.engine.Sector;
import fr.euphyllia.fidorial.doom.engine.Thinker;

/**
 * Sector light effects ({@code p_lights.c}).
 */
public final class Lights {

    public static final int GLOWSPEED = 8;
    public static final int STROBEBRIGHT = 5;
    public static final int FASTDARK = 15;
    public static final int SLOWDARK = 35;

    private final Game g;

    public Lights(final Game g) {
        this.g = g;
    }

    public void P_SpawnFireFlicker(final Sector sector) {
        sector.special = 0;
        final FireFlicker flick = new FireFlicker();
        g.P_AddThinker(flick);
        flick.sector = sector;
        flick.maxlight = sector.lightlevel;
        flick.minlight = g.spec.P_FindMinSurroundingLight(sector, sector.lightlevel) + 16;
        flick.count = 4;
    }

    public void P_SpawnLightFlash(final Sector sector) {
        sector.special = 0;
        final LightFlash flash = new LightFlash();
        g.P_AddThinker(flash);
        flash.sector = sector;
        flash.maxlight = sector.lightlevel;
        flash.minlight = g.spec.P_FindMinSurroundingLight(sector, sector.lightlevel);
        flash.maxtime = 64;
        flash.mintime = 7;
        flash.count = (g.random.P_Random() & flash.maxtime) + 1;
    }

    public void P_SpawnStrobeFlash(final Sector sector, final int fastOrSlow, final int inSync) {
        final Strobe flash = new Strobe();
        g.P_AddThinker(flash);
        flash.sector = sector;
        flash.darktime = fastOrSlow;
        flash.brighttime = STROBEBRIGHT;
        flash.maxlight = sector.lightlevel;
        flash.minlight = g.spec.P_FindMinSurroundingLight(sector, sector.lightlevel);
        if (flash.minlight == flash.maxlight) {
            flash.minlight = 0;
        }
        sector.special = 0;
        if (inSync == 0) {
            flash.count = (g.random.P_Random() & 7) + 1;
        } else {
            flash.count = 1;
        }
    }

    public void EV_StartLightStrobing(final Line line) {
        int secnum = -1;
        while ((secnum = g.spec.P_FindSectorFromLineTag(line, secnum)) >= 0) {
            final Sector sec = g.level.sectors[secnum];
            if (sec.specialdata != null) {
                continue;
            }
            P_SpawnStrobeFlash(sec, SLOWDARK, 0);
        }
    }

    public void EV_TurnTagLightsOff(final Line line) {
        for (final Sector sector : g.level.sectors) {
            if (sector.tag == line.tag) {
                int min = sector.lightlevel;
                for (int i = 0; i < sector.linecount; i++) {
                    final Sector tsec = Spec.getNextSector(sector.lines[i], sector);
                    if (tsec == null) {
                        continue;
                    }
                    if (tsec.lightlevel < min) {
                        min = tsec.lightlevel;
                    }
                }
                sector.lightlevel = (short) min;
            }
        }
    }

    public void EV_LightTurnOn(final Line line, int bright) {
        for (final Sector sector : g.level.sectors) {
            if (sector.tag == line.tag) {
                if (bright == 0) {
                    for (int j = 0; j < sector.linecount; j++) {
                        final Sector temp = Spec.getNextSector(sector.lines[j], sector);
                        if (temp == null) {
                            continue;
                        }
                        if (temp.lightlevel > bright) {
                            bright = temp.lightlevel;
                        }
                    }
                }
                sector.lightlevel = (short) bright;
            }
        }
    }

    public void P_SpawnGlowingLight(final Sector sector) {
        final Glow glow = new Glow();
        g.P_AddThinker(glow);
        glow.sector = sector;
        glow.minlight = g.spec.P_FindMinSurroundingLight(sector, sector.lightlevel);
        glow.maxlight = sector.lightlevel;
        glow.direction = -1;
        sector.special = 0;
    }

    /**
     * {@code fireflicker_t}.
     */
    public static final class FireFlicker extends Thinker {
        public Sector sector;
        public int count;
        public int maxlight;
        public int minlight;

        @Override
        public void think(final Game g) {
            if (--count != 0) {
                return;
            }
            final int amount = (g.random.P_Random() & 3) * 16;
            if (sector.lightlevel - amount < minlight) {
                sector.lightlevel = (short) minlight;
            } else {
                sector.lightlevel = (short) (maxlight - amount);
            }
            count = 4;
        }
    }

    /**
     * {@code lightflash_t}.
     */
    public static final class LightFlash extends Thinker {
        public Sector sector;
        public int count;
        public int maxlight;
        public int minlight;
        public int maxtime;
        public int mintime;

        @Override
        public void think(final Game g) {
            if (--count != 0) {
                return;
            }
            if (sector.lightlevel == maxlight) {
                sector.lightlevel = (short) minlight;
                count = (g.random.P_Random() & mintime) + 1;
            } else {
                sector.lightlevel = (short) maxlight;
                count = (g.random.P_Random() & maxtime) + 1;
            }
        }
    }

    /**
     * {@code strobe_t}.
     */
    public static final class Strobe extends Thinker {
        public Sector sector;
        public int count;
        public int minlight;
        public int maxlight;
        public int darktime;
        public int brighttime;

        @Override
        public void think(final Game g) {
            if (--count != 0) {
                return;
            }
            if (sector.lightlevel == minlight) {
                sector.lightlevel = (short) maxlight;
                count = brighttime;
            } else {
                sector.lightlevel = (short) minlight;
                count = darktime;
            }
        }
    }

    /**
     * {@code glow_t}.
     */
    public static final class Glow extends Thinker {
        public Sector sector;
        public int minlight;
        public int maxlight;
        public int direction;

        @Override
        public void think(final Game g) {
            switch (direction) {
                case -1 -> {
                    sector.lightlevel -= GLOWSPEED;
                    if (sector.lightlevel <= minlight) {
                        sector.lightlevel += GLOWSPEED;
                        direction = 1;
                    }
                }
                case 1 -> {
                    sector.lightlevel += GLOWSPEED;
                    if (sector.lightlevel >= maxlight) {
                        sector.lightlevel -= GLOWSPEED;
                        direction = -1;
                    }
                }
                default -> {
                }
            }
        }
    }
}
