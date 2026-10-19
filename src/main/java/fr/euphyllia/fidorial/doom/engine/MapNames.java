// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine;

import static fr.euphyllia.fidorial.doom.engine.Defs.commercial;
import static fr.euphyllia.fidorial.doom.engine.Defs.pack_plut;
import static fr.euphyllia.fidorial.doom.engine.Defs.pack_tnt;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_1;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_10;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_11;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_12;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_13;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_14;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_15;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_16;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_17;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_18;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_19;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_2;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_20;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_21;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_22;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_23;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_24;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_25;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_26;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_27;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_28;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_29;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_3;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_30;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_31;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_32;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_4;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_5;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_6;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_7;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_8;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_9;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_E1M1;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_E1M2;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_E1M3;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_E1M4;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_E1M5;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_E1M6;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_E1M7;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_E1M8;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_E1M9;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_E2M1;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_E2M2;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_E2M3;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_E2M4;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_E2M5;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_E2M6;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_E2M7;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_E2M8;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_E2M9;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_E3M1;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_E3M2;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_E3M3;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_E3M4;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_E3M5;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_E3M6;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_E3M7;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_E3M8;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_E3M9;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_E4M1;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_E4M2;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_E4M3;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_E4M4;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_E4M5;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_E4M6;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_E4M7;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_E4M8;
import static fr.euphyllia.fidorial.doom.engine.Strings.HUSTR_E4M9;
import static fr.euphyllia.fidorial.doom.engine.Strings.PHUSTR_1;
import static fr.euphyllia.fidorial.doom.engine.Strings.PHUSTR_10;
import static fr.euphyllia.fidorial.doom.engine.Strings.PHUSTR_11;
import static fr.euphyllia.fidorial.doom.engine.Strings.PHUSTR_12;
import static fr.euphyllia.fidorial.doom.engine.Strings.PHUSTR_13;
import static fr.euphyllia.fidorial.doom.engine.Strings.PHUSTR_14;
import static fr.euphyllia.fidorial.doom.engine.Strings.PHUSTR_15;
import static fr.euphyllia.fidorial.doom.engine.Strings.PHUSTR_16;
import static fr.euphyllia.fidorial.doom.engine.Strings.PHUSTR_17;
import static fr.euphyllia.fidorial.doom.engine.Strings.PHUSTR_18;
import static fr.euphyllia.fidorial.doom.engine.Strings.PHUSTR_19;
import static fr.euphyllia.fidorial.doom.engine.Strings.PHUSTR_2;
import static fr.euphyllia.fidorial.doom.engine.Strings.PHUSTR_20;
import static fr.euphyllia.fidorial.doom.engine.Strings.PHUSTR_21;
import static fr.euphyllia.fidorial.doom.engine.Strings.PHUSTR_22;
import static fr.euphyllia.fidorial.doom.engine.Strings.PHUSTR_23;
import static fr.euphyllia.fidorial.doom.engine.Strings.PHUSTR_24;
import static fr.euphyllia.fidorial.doom.engine.Strings.PHUSTR_25;
import static fr.euphyllia.fidorial.doom.engine.Strings.PHUSTR_26;
import static fr.euphyllia.fidorial.doom.engine.Strings.PHUSTR_27;
import static fr.euphyllia.fidorial.doom.engine.Strings.PHUSTR_28;
import static fr.euphyllia.fidorial.doom.engine.Strings.PHUSTR_29;
import static fr.euphyllia.fidorial.doom.engine.Strings.PHUSTR_3;
import static fr.euphyllia.fidorial.doom.engine.Strings.PHUSTR_30;
import static fr.euphyllia.fidorial.doom.engine.Strings.PHUSTR_31;
import static fr.euphyllia.fidorial.doom.engine.Strings.PHUSTR_32;
import static fr.euphyllia.fidorial.doom.engine.Strings.PHUSTR_4;
import static fr.euphyllia.fidorial.doom.engine.Strings.PHUSTR_5;
import static fr.euphyllia.fidorial.doom.engine.Strings.PHUSTR_6;
import static fr.euphyllia.fidorial.doom.engine.Strings.PHUSTR_7;
import static fr.euphyllia.fidorial.doom.engine.Strings.PHUSTR_8;
import static fr.euphyllia.fidorial.doom.engine.Strings.PHUSTR_9;
import static fr.euphyllia.fidorial.doom.engine.Strings.THUSTR_1;
import static fr.euphyllia.fidorial.doom.engine.Strings.THUSTR_10;
import static fr.euphyllia.fidorial.doom.engine.Strings.THUSTR_11;
import static fr.euphyllia.fidorial.doom.engine.Strings.THUSTR_12;
import static fr.euphyllia.fidorial.doom.engine.Strings.THUSTR_13;
import static fr.euphyllia.fidorial.doom.engine.Strings.THUSTR_14;
import static fr.euphyllia.fidorial.doom.engine.Strings.THUSTR_15;
import static fr.euphyllia.fidorial.doom.engine.Strings.THUSTR_16;
import static fr.euphyllia.fidorial.doom.engine.Strings.THUSTR_17;
import static fr.euphyllia.fidorial.doom.engine.Strings.THUSTR_18;
import static fr.euphyllia.fidorial.doom.engine.Strings.THUSTR_19;
import static fr.euphyllia.fidorial.doom.engine.Strings.THUSTR_2;
import static fr.euphyllia.fidorial.doom.engine.Strings.THUSTR_20;
import static fr.euphyllia.fidorial.doom.engine.Strings.THUSTR_21;
import static fr.euphyllia.fidorial.doom.engine.Strings.THUSTR_22;
import static fr.euphyllia.fidorial.doom.engine.Strings.THUSTR_23;
import static fr.euphyllia.fidorial.doom.engine.Strings.THUSTR_24;
import static fr.euphyllia.fidorial.doom.engine.Strings.THUSTR_25;
import static fr.euphyllia.fidorial.doom.engine.Strings.THUSTR_26;
import static fr.euphyllia.fidorial.doom.engine.Strings.THUSTR_27;
import static fr.euphyllia.fidorial.doom.engine.Strings.THUSTR_28;
import static fr.euphyllia.fidorial.doom.engine.Strings.THUSTR_29;
import static fr.euphyllia.fidorial.doom.engine.Strings.THUSTR_3;
import static fr.euphyllia.fidorial.doom.engine.Strings.THUSTR_30;
import static fr.euphyllia.fidorial.doom.engine.Strings.THUSTR_31;
import static fr.euphyllia.fidorial.doom.engine.Strings.THUSTR_32;
import static fr.euphyllia.fidorial.doom.engine.Strings.THUSTR_4;
import static fr.euphyllia.fidorial.doom.engine.Strings.THUSTR_5;
import static fr.euphyllia.fidorial.doom.engine.Strings.THUSTR_6;
import static fr.euphyllia.fidorial.doom.engine.Strings.THUSTR_7;
import static fr.euphyllia.fidorial.doom.engine.Strings.THUSTR_8;
import static fr.euphyllia.fidorial.doom.engine.Strings.THUSTR_9;

/**
 * Level names shown on the automap ({@code mapnames} in {@code hu_stuff.c}).
 */
public final class MapNames {

    private static final String[] DOOM = {HUSTR_E1M1, HUSTR_E1M2, HUSTR_E1M3, HUSTR_E1M4, HUSTR_E1M5, HUSTR_E1M6, HUSTR_E1M7, HUSTR_E1M8, HUSTR_E1M9, HUSTR_E2M1, HUSTR_E2M2, HUSTR_E2M3, HUSTR_E2M4, HUSTR_E2M5, HUSTR_E2M6, HUSTR_E2M7, HUSTR_E2M8, HUSTR_E2M9, HUSTR_E3M1, HUSTR_E3M2, HUSTR_E3M3, HUSTR_E3M4, HUSTR_E3M5, HUSTR_E3M6, HUSTR_E3M7, HUSTR_E3M8, HUSTR_E3M9, HUSTR_E4M1, HUSTR_E4M2, HUSTR_E4M3, HUSTR_E4M4, HUSTR_E4M5, HUSTR_E4M6, HUSTR_E4M7, HUSTR_E4M8, HUSTR_E4M9};
    private static final String[] DOOM2 = {HUSTR_1, HUSTR_2, HUSTR_3, HUSTR_4, HUSTR_5, HUSTR_6, HUSTR_7, HUSTR_8, HUSTR_9, HUSTR_10, HUSTR_11, HUSTR_12, HUSTR_13, HUSTR_14, HUSTR_15, HUSTR_16, HUSTR_17, HUSTR_18, HUSTR_19, HUSTR_20, HUSTR_21, HUSTR_22, HUSTR_23, HUSTR_24, HUSTR_25, HUSTR_26, HUSTR_27, HUSTR_28, HUSTR_29, HUSTR_30, HUSTR_31, HUSTR_32};
    private static final String[] PLUTONIA = {PHUSTR_1, PHUSTR_2, PHUSTR_3, PHUSTR_4, PHUSTR_5, PHUSTR_6, PHUSTR_7, PHUSTR_8, PHUSTR_9, PHUSTR_10, PHUSTR_11, PHUSTR_12, PHUSTR_13, PHUSTR_14, PHUSTR_15, PHUSTR_16, PHUSTR_17, PHUSTR_18, PHUSTR_19, PHUSTR_20, PHUSTR_21, PHUSTR_22, PHUSTR_23, PHUSTR_24, PHUSTR_25, PHUSTR_26, PHUSTR_27, PHUSTR_28, PHUSTR_29, PHUSTR_30, PHUSTR_31, PHUSTR_32};
    private static final String[] TNT = {THUSTR_1, THUSTR_2, THUSTR_3, THUSTR_4, THUSTR_5, THUSTR_6, THUSTR_7, THUSTR_8, THUSTR_9, THUSTR_10, THUSTR_11, THUSTR_12, THUSTR_13, THUSTR_14, THUSTR_15, THUSTR_16, THUSTR_17, THUSTR_18, THUSTR_19, THUSTR_20, THUSTR_21, THUSTR_22, THUSTR_23, THUSTR_24, THUSTR_25, THUSTR_26, THUSTR_27, THUSTR_28, THUSTR_29, THUSTR_30, THUSTR_31, THUSTR_32};

    private MapNames() {
    }

    /**
     * {@return the title of a level, or its lump name when it has none}
     */
    public static String title(final Game g, final int episode, final int map) {
        if (g.gamemode == commercial) {
            final String[] names = g.gamemission == pack_plut ? PLUTONIA : g.gamemission == pack_tnt ? TNT : DOOM2;
            return map >= 1 && map <= names.length ? g.text(names[map - 1]) : String.format(java.util.Locale.ROOT, "MAP%02d", map);
        }
        final int i = (episode - 1) * 9 + (map - 1);
        return i >= 0 && i < DOOM.length ? g.text(DOOM[i]) : "E" + episode + "M" + map;
    }
}
