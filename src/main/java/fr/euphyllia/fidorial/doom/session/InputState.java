package fr.euphyllia.fidorial.doom.session;

import fr.euphyllia.fidorial.doom.engine.Player;
import fr.euphyllia.fidorial.doom.engine.TicCmd;
import fr.fidorial.entity.player.PlayerInput;

import static fr.euphyllia.fidorial.doom.engine.Defs.BT_ATTACK;
import static fr.euphyllia.fidorial.doom.engine.Defs.BT_CHANGE;
import static fr.euphyllia.fidorial.doom.engine.Defs.BT_USE;
import static fr.euphyllia.fidorial.doom.engine.Defs.BT_WEAPONSHIFT;
import static fr.euphyllia.fidorial.doom.engine.Defs.wp_bfg;
import static fr.euphyllia.fidorial.doom.engine.Defs.wp_chaingun;
import static fr.euphyllia.fidorial.doom.engine.Defs.wp_chainsaw;
import static fr.euphyllia.fidorial.doom.engine.Defs.wp_fist;
import static fr.euphyllia.fidorial.doom.engine.Defs.wp_missile;
import static fr.euphyllia.fidorial.doom.engine.Defs.wp_pistol;
import static fr.euphyllia.fidorial.doom.engine.Defs.wp_plasma;
import static fr.euphyllia.fidorial.doom.engine.Defs.wp_shotgun;
import static fr.euphyllia.fidorial.doom.engine.Defs.wp_supershotgun;

/**
 * What a Minecraft player does, collected from the network threads and turned into one Doom tic
 * command per tic ({@code G_BuildTiccmd}).
 */
final class InputState {

    /**
     * Weapon key that selects the next owned weapon.
     */
    static final int NEXT_WEAPON = 8;

    private static final int[] FORWARD_MOVE = {0x19, 0x32};
    private static final int[] SIDE_MOVE = {0x18, 0x28};
    /**
     * Right click repeats every 4 client ticks while held: keep firing a bit longer than that.
     */
    private static final long USE_HOLD_NANOS = 260_000_000L;
    private static final int PUNCH_TICS = 4;

    private PlayerInput keys = PlayerInput.NONE;
    private double yaw;
    private long lastUse;
    private boolean everUsed;
    private int punchTics;
    private int pendingWeapon;
    private boolean alwaysRun;
    /**
     * A jump press not yet seen by a tic: a short tap can start and end between two tics.
     */
    private boolean usePressed;

    private static int weaponKey(final int weapon) {
        return switch (weapon) {
            case wp_fist, wp_chainsaw -> 1;
            case wp_pistol -> 2;
            case wp_shotgun, wp_supershotgun -> 3;
            case wp_chaingun -> 4;
            case wp_missile -> 5;
            case wp_plasma -> 6;
            case wp_bfg -> 7;
            default -> 2;
        };
    }

    private static boolean ownsKey(final Player player, final int key, final boolean commercial) {
        return switch (key) {
            case 1 -> true;
            case 2 -> player.weaponowned[wp_pistol];
            case 3 -> player.weaponowned[wp_shotgun] || commercial && player.weaponowned[wp_supershotgun];
            case 4 -> player.weaponowned[wp_chaingun];
            case 5 -> player.weaponowned[wp_missile];
            case 6 -> player.weaponowned[wp_plasma];
            case 7 -> player.weaponowned[wp_bfg];
            default -> false;
        };
    }

    synchronized void keys(final PlayerInput input) {
        if (input.jump() && !keys.jump()) {
            usePressed = true;
        }
        this.keys = input;
    }

    /**
     * Adds a mouse turn, in Minecraft degrees (positive to the right).
     */
    synchronized void turn(final double degrees) {
        yaw += degrees;
    }

    synchronized void use() {
        lastUse = System.nanoTime();
        everUsed = true;
    }

    synchronized void punch() {
        punchTics = PUNCH_TICS;
    }

    /**
     * Selects a weapon key {@code 1..7}, or {@link #NEXT_WEAPON}.
     */
    synchronized void weapon(final int key) {
        pendingWeapon = key;
    }

    synchronized boolean toggleAlwaysRun() {
        alwaysRun = !alwaysRun;
        return alwaysRun;
    }

    synchronized void reset() {
        keys = PlayerInput.NONE;
        yaw = 0;
        punchTics = 0;
        pendingWeapon = 0;
        everUsed = false;
        usePressed = false;
    }

    /**
     * Fills the command of the next tic.
     *
     * @param cmd         the command to fill
     * @param player      the Doom player, for the next weapon key
     * @param sensitivity how many Doom turn units a Minecraft degree is worth, relative to 1:1
     * @param commercial  whether the game is Doom II, which has the super shotgun
     */
    synchronized void build(final TicCmd cmd, final Player player, final double sensitivity, final boolean commercial) {
        cmd.clear();
        final boolean run = alwaysRun ^ (keys.sprint() || keys.sneak());
        final int speed = run ? 1 : 0;
        int forward = 0;
        int side = 0;
        if (keys.forward()) {
            forward += FORWARD_MOVE[speed];
        }
        if (keys.backward()) {
            forward -= FORWARD_MOVE[speed];
        }
        if (keys.right()) {
            side += SIDE_MOVE[speed];
        }
        if (keys.left()) {
            side -= SIDE_MOVE[speed];
        }
        cmd.forwardmove = (byte) forward;
        cmd.sidemove = (byte) side;
        // a full turn is 65536 units; Doom turns left for positive values, Minecraft right
        final double units = -yaw * 65536.0 / 360.0 * sensitivity;
        final int turn = (int) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, Math.round(units)));
        yaw -= -turn / sensitivity * 360.0 / 65536.0;
        cmd.angleturn = (short) turn;
        int buttons = 0;
        if (everUsed && System.nanoTime() - lastUse < USE_HOLD_NANOS || punchTics > 0) {
            buttons |= BT_ATTACK;
        }
        if (punchTics > 0) {
            punchTics--;
        }
        if (keys.jump() || usePressed) {
            buttons |= BT_USE;
        }
        usePressed = false;
        int key = pendingWeapon;
        pendingWeapon = 0;
        if (key == NEXT_WEAPON) {
            final int current = weaponKey(player.pendingweapon < 9 && player.pendingweapon >= 0
                    ? player.pendingweapon : player.readyweapon);
            key = 0;
            for (int i = 1; i <= 7; i++) {
                final int candidate = (current + i - 1) % 7 + 1;
                if (ownsKey(player, candidate, commercial)) {
                    key = candidate;
                    break;
                }
            }
        }
        if (key >= 1 && key <= 7) {
            buttons |= BT_CHANGE | (key - 1) << BT_WEAPONSHIFT;
        }
        cmd.buttons = (byte) buttons;
    }
}
