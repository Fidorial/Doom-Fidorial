package fr.euphyllia.fidorial.doom.session;

import fr.euphyllia.fidorial.doom.display.ScreenEncoder;
import fr.euphyllia.fidorial.doom.engine.ui.ViewScreen;
import fr.euphyllia.fidorial.doom.pack.ResourcePackBuilder;
import fr.fidorial.attribute.Attribute;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.player.Player;
import fr.fidorial.inventory.PlayerInventory;
import fr.fidorial.item.ItemStack;
import fr.fidorial.item.data.DataComponentTypes;
import fr.fidorial.map.MapFrame;
import fr.fidorial.map.MapManager;
import fr.fidorial.map.MapView;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Location;
import fr.fidorial.math.Position;
import fr.fidorial.world.BlockFace;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * One Minecraft player inside a Doom session: what they see, what they press, and what to give back
 * when they leave.
 *
 * <p>The screen is shown through {@value ScreenEncoder#TILES} temporary maps hung in invisible
 * frames that only the player sees, in the block of their head. The client culls
 * entities with a box inflated by half a block, so frames that close are drawn whichever way the
 * player looks; the resource pack shader then stretches them over the whole screen.</p>
 */
public final class DoomPlayer {

    /**
     * The hotbar slot kept selected, so that every number key sends a selection.
     */
    public static final int NEUTRAL_SLOT = 7;

    private static final Key BLANK_ITEM = Key.key("minecraft:paper");
    /**
     * The attributes set to {@code 0} while playing: no walking, jumping or reaching anything.
     */
    private static final List<Key> FROZEN_ATTRIBUTES = List.of(
            Attribute.MOVEMENT_SPEED,
            Attribute.JUMP_STRENGTH,
            Attribute.BLOCK_INTERACTION_RANGE,
            Attribute.ENTITY_INTERACTION_RANGE);

    private final UUID uuid;
    private final String name;
    private final int slot;
    private final DoomSession session;
    private final ViewScreen view;
    private final ScreenEncoder encoder;
    private final InputState input = new InputState();
    private MapFrame[] frames = new MapFrame[0];
    private volatile MapView[] screens = new MapView[0];
    private @Nullable Saved saved;

    DoomPlayer(final Player player, final int slot, final DoomSession session, final ViewScreen view,
               final ScreenEncoder encoder) {
        this.uuid = player.uuid();
        this.name = player.name();
        this.slot = slot;
        this.session = session;
        this.view = view;
        this.encoder = encoder;
    }

    private static ItemStack blankItem() {
        return ItemStack.builder(BLANK_ITEM)
                .set(DataComponentTypes.ITEM_MODEL, Key.key(ResourcePackBuilder.BLANK_MODEL))
                .set(DataComponentTypes.ITEM_NAME, Component.empty())
                .build();
    }

    public UUID uuid() {
        return uuid;
    }

    public String name() {
        return name;
    }

    public int slot() {
        return slot;
    }

    public DoomSession session() {
        return session;
    }

    ViewScreen view() {
        return view;
    }

    InputState input() {
        return input;
    }

    /**
     * {@return the Minecraft player if still online}
     */
    @Nullable Player online() {
        return session.manager().server().players().get(uuid).orElse(null);
    }

    /**
     * Freezes the player, empties their hands and hangs the screen frames. Run on the player's thread.
     *
     * @param player the Minecraft player
     */
    void enter(final Player player) {
        final PlayerInventory inventory = player.inventory();
        final ItemStack[] items = new ItemStack[PlayerInventory.SIZE];
        for (int i = 0; i < items.length; i++) {
            items[i] = inventory.get(i);
        }
        final Location location = player.location();
        saved = new Saved(player.gameMode(), location, items, player.selectedSlot());

        for (int i = 0; i < PlayerInventory.SIZE; i++) {
            inventory.set(i, ItemStack.EMPTY);
        }
        final ItemStack blank = blankItem();
        for (int i = 0; i < PlayerInventory.HOTBAR_SIZE; i++) {
            inventory.set(PlayerInventory.HOTBAR_START + i, blank);
        }
        player.updateInventory();
        player.setGameMode(GameMode.ADVENTURE);
        player.setSelectedSlot(NEUTRAL_SLOT);

        // stand in the middle of the block, so the frames below are close enough in every direction
        final Location centred = Location.of(location.world(), Math.floor(location.x()) + 0.5, location.y(),
                Math.floor(location.z()) + 0.5, location.yaw(), 0f);
        player.teleport(centred);
        for (final Key attribute : FROZEN_ATTRIBUTES) {
            player.setAttributeBase(attribute, 0.0);
        }

        final double eye = centred.y() + 1.62;
        final BlockPosition frameBlock = Position.block((int) Math.floor(centred.x()), (int) Math.floor(eye - 0.25),
                (int) Math.floor(centred.z()));
        final MapManager maps = session.manager().server().maps();
        final MapFrame[] hung = new MapFrame[ScreenEncoder.TILES];
        final MapView[] views = new MapView[ScreenEncoder.TILES];
        for (int tile = 0; tile < ScreenEncoder.TILES; tile++) {
            views[tile] = maps.createTemporary();
            hung[tile] = views[tile].hang(player, frameBlock, BlockFace.SOUTH, true);
        }
        frames = hung;
        screens = views;
    }

    /**
     * Gives everything back. Run on the player's thread, or while they disconnect.
     *
     * @param player the Minecraft player
     */
    void leave(final Player player) {
        for (final MapFrame frame : frames) {
            frame.remove();
        }
        frames = new MapFrame[0];
        final MapManager maps = session.manager().server().maps();
        for (final MapView screen : screens) {
            maps.delete(screen);
        }
        screens = new MapView[0];
        for (final Key attribute : FROZEN_ATTRIBUTES) {
            player.resetAttributeBase(attribute);
        }
        final Saved s = saved;
        saved = null;
        if (s == null) {
            return;
        }
        final PlayerInventory inventory = player.inventory();
        for (int i = 0; i < s.inventory().length; i++) {
            inventory.set(i, s.inventory()[i]);
        }
        player.updateInventory();
        player.setSelectedSlot(s.selectedSlot());
        player.setGameMode(s.gameMode());
        if (s.location().world().equals(player.location().world())) {
            player.teleport(s.location());
        }
    }

    /**
     * Draws the frame on the screen maps and sends what changed. Runs on the session thread.
     */
    void sendFrame() {
        final MapView[] views = screens;
        if (views.length != ScreenEncoder.TILES) {
            return;
        }
        final byte[][] canvases = encoder.encode(view.draw(), view.palette());
        for (int tile = 0; tile < views.length; tile++) {
            views[tile].canvas().pixels(canvases[tile]);
        }
        for (final MapView screen : views) {
            screen.flush();
        }
    }

    /**
     * What the player had before playing.
     */
    private record Saved(GameMode gameMode, Location location, ItemStack[] inventory, int selectedSlot) {
    }
}
