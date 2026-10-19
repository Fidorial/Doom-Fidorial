// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine;

import fr.euphyllia.fidorial.doom.engine.info.Action;
import fr.euphyllia.fidorial.doom.engine.info.MobjInfo;
import fr.euphyllia.fidorial.doom.engine.info.State;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * A reader for DEHACKED patches, enough for the lumps shipped in IWADs such as Freedoom.
 *
 * <p>Supported: {@code Thing}, {@code Frame}, {@code Pointer}, old style {@code Text} replacing
 * messages, and the BEX sections {@code [STRINGS]}, {@code [PARS]} and {@code [CODEPTR]}. Other
 * sections are skipped with a warning.</p>
 */
public final class Dehacked {

    private static final String[] THING_FIELDS = {
            "id #", "initial frame", "hit points", "first moving frame", "alert sound", "reaction time",
            "attack sound", "injury frame", "pain chance", "pain sound", "close attack frame", "far attack frame",
            "death frame", "exploding frame", "death sound", "speed", "width", "height", "mass", "missile damage",
            "action sound", "bits", "respawn frame"
    };

    private static final Map<String, String> STRINGS = new HashMap<>();
    private static final Map<String, Integer> FLAGS = new HashMap<>();

    static {
        for (final Field f : Strings.class.getFields()) {
            if (Modifier.isStatic(f.getModifiers()) && f.getType() == String.class) {
                try {
                    STRINGS.put(f.getName(), (String) f.get(null));
                } catch (final IllegalAccessException ignored) {
                    // public constants
                }
            }
        }
        final String[][] flats = {{"BGFLATE1", "FLOOR4_8"}, {"BGFLATE2", "SFLR6_1"}, {"BGFLATE3", "MFLR8_4"},
                {"BGFLATE4", "MFLR8_3"}, {"BGFLAT06", "SLIME16"}, {"BGFLAT11", "RROCK14"}, {"BGFLAT20", "RROCK07"},
                {"BGFLAT30", "RROCK17"}, {"BGFLAT15", "RROCK13"}, {"BGFLAT31", "RROCK19"}};
        for (final String[] flat : flats) {
            STRINGS.put(flat[0], flat[1]);
        }
        for (final Field f : MobjFlags.class.getFields()) {
            if (Modifier.isStatic(f.getModifiers()) && f.getType() == int.class && f.getName().startsWith("MF_")) {
                try {
                    FLAGS.put(f.getName().substring(3), f.getInt(null));
                } catch (final IllegalAccessException ignored) {
                    // public constants
                }
            }
        }
    }

    private final Game g;
    private final String[] lines;
    private final State[] originalStates;
    private int pos;

    private Dehacked(final Game g, final String text) {
        this.g = g;
        this.lines = text.replace("\r", "").split("\n", -1);
        this.originalStates = g.baseStates.clone();
    }

    /**
     * Applies a patch to a game before it starts.
     *
     * @param g    the game
     * @param text the patch
     */
    public static void apply(final Game g, final String text) {
        new Dehacked(g, text).run();
        System.arraycopy(g.baseStates, 0, g.states, 0, g.states.length);
        System.arraycopy(g.baseMobjinfo, 0, g.mobjinfo, 0, g.mobjinfo.length);
    }

    private static String stripComment(final String line) {
        final String t = line.strip();
        return t.startsWith("#") ? "" : t;
    }

    private static int parseInt(final String s, final int fallback) {
        try {
            return Integer.parseInt(s.strip());
        } catch (final NumberFormatException e) {
            return fallback;
        }
    }

    private static String unescape(final String s) {
        final StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                final char n = s.charAt(++i);
                switch (n) {
                    case 'n' -> out.append('\n');
                    case 't' -> out.append('\t');
                    case '"' -> out.append('"');
                    case '\\' -> out.append('\\');
                    default -> out.append('\\').append(n);
                }
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    private void run() {
        while (pos < lines.length) {
            final String line = stripComment(lines[pos++]);
            if (line.isEmpty()) {
                continue;
            }
            final String lower = line.toLowerCase(Locale.ROOT);
            final String[] words = line.split("\\s+");
            if (lower.startsWith("thing ") && words.length >= 2) {
                thing(parseInt(words[1], -1) - 1);
            } else if (lower.startsWith("frame ") && words.length >= 2) {
                frame(parseInt(words[1], -1));
            } else if (lower.startsWith("pointer ")) {
                pointer(line);
            } else if (lower.startsWith("text ") && words.length >= 3) {
                text(parseInt(words[1], 0), parseInt(words[2], 0));
            } else if (lower.equals("[strings]")) {
                strings();
            } else if (lower.equals("[pars]")) {
                pars();
            } else if (lower.equals("[codeptr]")) {
                codeptr();
            } else if (lower.startsWith("patch file") || lower.startsWith("doom version") || lower.startsWith("patch format")) {
                continue;
            } else if (Character.isLetter(line.charAt(0)) && !line.contains("=") || line.startsWith("[")) {
                g.warn("DEHACKED: section not supported: " + line);
                skipBlock();
            }
        }
    }

    private void skipBlock() {
        while (pos < lines.length) {
            final String line = stripComment(lines[pos]);
            if (line.isEmpty() || line.startsWith("[")) {
                return;
            }
            if (!line.contains("=")) {
                return;
            }
            pos++;
        }
    }

    /**
     * Reads {@code key = value} lines until a blank line or the next section.
     */
    private Map<String, String> block() {
        final Map<String, String> map = new HashMap<>();
        while (pos < lines.length) {
            final String raw = lines[pos];
            final String line = raw.strip();
            if (line.startsWith("#")) {
                pos++;
                continue;
            }
            final int eq = line.indexOf('=');
            if (line.isEmpty() || eq < 0) {
                return map;
            }
            map.put(line.substring(0, eq).strip().toLowerCase(Locale.ROOT), line.substring(eq + 1).strip());
            pos++;
        }
        return map;
    }

    private int flags(final String value) {
        final int number = parseInt(value, Integer.MIN_VALUE);
        if (number != Integer.MIN_VALUE) {
            return number;
        }
        int bits = 0;
        for (final String name : value.split("[+|,\\s]+")) {
            final Integer flag = FLAGS.get(name.toUpperCase(Locale.ROOT));
            if (flag != null) {
                bits |= flag;
            } else if (!name.isEmpty()) {
                g.warn("DEHACKED: unknown flag " + name);
            }
        }
        return bits;
    }

    private void thing(final int type) {
        final Map<String, String> fields = block();
        if (type < 0 || type >= g.baseMobjinfo.length) {
            g.warn("DEHACKED: bad thing number " + (type + 1));
            return;
        }
        final MobjInfo m = g.baseMobjinfo[type];
        final int[] v = {m.doomednum(), m.spawnstate(), m.spawnhealth(), m.seestate(), m.seesound(), m.reactiontime(),
                m.attacksound(), m.painstate(), m.painchance(), m.painsound(), m.meleestate(), m.missilestate(),
                m.deathstate(), m.xdeathstate(), m.deathsound(), m.speed(), m.radius(), m.height(), m.mass(), m.damage(),
                m.activesound(), m.flags(), m.raisestate()};
        for (final Map.Entry<String, String> e : fields.entrySet()) {
            boolean known = false;
            for (int i = 0; i < THING_FIELDS.length; i++) {
                if (THING_FIELDS[i].equals(e.getKey())) {
                    v[i] = i == 21 ? flags(e.getValue()) : parseInt(e.getValue(), v[i]);
                    known = true;
                }
            }
            if (!known) {
                g.warn("DEHACKED: unknown thing field " + e.getKey());
            }
        }
        g.baseMobjinfo[type] = new MobjInfo(v[0], v[1], v[2], v[3], v[4], v[5], v[6], v[7], v[8], v[9], v[10], v[11],
                v[12], v[13], v[14], v[15], v[16], v[17], v[18], v[19], v[20], v[21], v[22]);
    }

    private void frame(final int index) {
        final Map<String, String> fields = block();
        if (index < 0 || index >= g.baseStates.length) {
            g.warn("DEHACKED: bad frame number " + index);
            return;
        }
        final State s = g.baseStates[index];
        int sprite = s.sprite();
        int frame = s.frame();
        int tics = s.tics();
        int next = s.nextstate();
        int misc1 = s.misc1();
        int misc2 = s.misc2();
        for (final Map.Entry<String, String> e : fields.entrySet()) {
            switch (e.getKey()) {
                case "sprite number" -> sprite = parseInt(e.getValue(), sprite);
                case "sprite subnumber" -> frame = parseInt(e.getValue(), frame);
                case "duration" -> tics = parseInt(e.getValue(), tics);
                case "next frame" -> next = parseInt(e.getValue(), next);
                case "unknown 1" -> misc1 = parseInt(e.getValue(), misc1);
                case "unknown 2" -> misc2 = parseInt(e.getValue(), misc2);
                default -> g.warn("DEHACKED: unknown frame field " + e.getKey());
            }
        }
        g.baseStates[index] = new State(index, sprite, frame, tics, s.action(), next, misc1, misc2);
    }

    private void setAction(final int index, final @Nullable Action action) {
        if (index < 0 || index >= g.baseStates.length) {
            g.warn("DEHACKED: bad frame number " + index);
            return;
        }
        final State s = g.baseStates[index];
        g.baseStates[index] = new State(index, s.sprite(), s.frame(), s.tics(), action, s.nextstate(), s.misc1(), s.misc2());
    }

    private void pointer(final String header) {
        final int open = header.toLowerCase(Locale.ROOT).indexOf("(frame");
        final Map<String, String> fields = block();
        if (open < 0) {
            return;
        }
        final int target = parseInt(header.substring(open + 6, header.indexOf(')', open)), -1);
        final String codep = fields.get("codep frame");
        if (codep != null) {
            final int from = parseInt(codep, -1);
            if (from >= 0 && from < originalStates.length) {
                setAction(target, originalStates[from].action());
            }
        }
    }

    private void codeptr() {
        for (final Map.Entry<String, String> e : block().entrySet()) {
            if (!e.getKey().startsWith("frame")) {
                continue;
            }
            final int index = parseInt(e.getKey().substring(5), -1);
            final String name = e.getValue();
            if (name.equalsIgnoreCase("NULL")) {
                setAction(index, null);
                continue;
            }
            final String full = name.regionMatches(true, 0, "A_", 0, 2) ? name.substring(2) : name;
            Action found = null;
            for (final Action a : Action.values()) {
                if (a.name().substring(2).equalsIgnoreCase(full)) {
                    found = a;
                }
            }
            if (found == null) {
                g.warn("DEHACKED: unknown code pointer " + name);
            } else {
                setAction(index, found);
            }
        }
    }

    private void strings() {
        while (pos < lines.length) {
            final String line = lines[pos].strip();
            if (line.isEmpty() || line.startsWith("#")) {
                pos++;
                continue;
            }
            if (line.startsWith("[")) {
                return;
            }
            final int eq = line.indexOf('=');
            if (eq < 0) {
                return;
            }
            pos++;
            final String key = line.substring(0, eq).strip().toUpperCase(Locale.ROOT);
            final StringBuilder value = new StringBuilder();
            String part = line.substring(eq + 1).strip();
            while (part.endsWith("\\") && pos < lines.length) {
                value.append(part, 0, part.length() - 1);
                part = lines[pos++].strip();
            }
            value.append(part);
            final String original = STRINGS.get(key);
            if (original != null) {
                g.setTextOverride(original, unescape(value.toString()));
            }
        }
    }

    private void pars() {
        while (pos < lines.length) {
            String line = lines[pos].strip();
            final int hash = line.indexOf('#');
            if (hash >= 0) {
                line = line.substring(0, hash).strip();
            }
            if (line.isEmpty()) {
                pos++;
                if (pos < lines.length && lines[pos].strip().startsWith("[")) {
                    return;
                }
                continue;
            }
            if (!line.toLowerCase(Locale.ROOT).startsWith("par")) {
                return;
            }
            pos++;
            final String[] w = line.split("\\s+");
            if (w.length == 4) {
                g.setParTime(parseInt(w[1], 0), parseInt(w[2], 0), parseInt(w[3], 0));
            } else if (w.length == 3) {
                g.setParTime(1, parseInt(w[1], 0), parseInt(w[2], 0));
            }
        }
    }

    private void text(final int fromLength, final int toLength) {
        final StringBuilder sb = new StringBuilder();
        while (pos < lines.length && sb.length() < fromLength + toLength) {
            if (!sb.isEmpty()) {
                sb.append('\n');
            }
            sb.append(lines[pos++]);
        }
        if (sb.length() < fromLength + toLength) {
            return;
        }
        final String from = sb.substring(0, fromLength);
        final String to = sb.substring(fromLength, fromLength + toLength);
        if (STRINGS.containsValue(from)) {
            g.setTextOverride(from, to);
        }
    }
}
