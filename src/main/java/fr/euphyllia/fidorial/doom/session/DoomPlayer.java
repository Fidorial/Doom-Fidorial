package fr.euphyllia.fidorial.doom.session;

import fr.euphyllia.fidorial.doom.display.ScreenEncoder;
import fr.euphyllia.fidorial.doom.engine.ui.ViewScreen;
import fr.euphyllia.fidorial.doom.pack.ResourcePackBuilder;
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
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Attribute;
import fr.fidorial.registry.data.Item;
import fr.fidorial.registry.keys.AttributeKeys;
import fr.fidorial.registry.keys.ItemKeys;
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
 * player looks; the resource pack shader then stretches them over the whole screen. When something
 * moves the player out of that box (a fall once flying stops, a push, water...), the frames are
 * hung again around their head.</p>
 */
public final class DoomPlayer {

    /**
     * The hotbar slot kept selected, so that every number key sends a selection.
     */
    public static final int NEUTRAL_SLOT = 7;

    private static final double EYE_HEIGHT = 1.62;
    private static final double SNEAKING_EYE_HEIGHT = 1.27;
    /**
     * How far the camera may be from a frame, past its edges, for the client to still draw it whichever way
     * it looks: the half block the client inflates its box by, less the near plane ({@code 0.05}).
     */
    private static final double REACH = 0.5 - 0.05;
    /**
     * Half the width of a frame, counted as without a map ({@code 0.75}), whose box the client may not grow.
     */
    private static final double FRAME_HALF_WIDTH = 0.375;
    private static final double FRAME_DEPTH = 0.0625;

    private static final TypedKey<Item> BLANK_ITEM = ItemKeys.PAPER;
    /**
     * The attributes set to {@code 0} while playing: no walking, jumping or reaching anything.
     */
    private static final List<TypedKey<Attribute>> FROZEN_ATTRIBUTES = List.of(
            AttributeKeys.MOVEMENT_SPEED,
            AttributeKeys.JUMP_STRENGTH,
            AttributeKeys.BLOCK_INTERACTION_RANGE,
            AttributeKeys.ENTITY_INTERACTION_RANGE);

    private final UUID uuid;
    private final String name;
    private final int slot;
    private final DoomSession session;
    private final ViewScreen view;
    private final ScreenEncoder encoder;
    private final InputState input = new InputState();
    private MapFrame[] frames = new MapFrame[0];
    private @Nullable BlockPosition frameBlock;
    private BlockFace frameFacing = BlockFace.SOUTH;
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
        for (final TypedKey<Attribute> attribute : FROZEN_ATTRIBUTES) {
            player.setAttributeBase(attribute, 0.0);
        }

        final MapManager maps = session.manager().server().maps();
        final MapView[] views = new MapView[ScreenEncoder.TILES];
        for (int tile = 0; tile < ScreenEncoder.TILES; tile++) {
            views[tile] = maps.createTemporary();
        }
        screens = views;
        hangFrames(player, centred.x(), centred.y(), centred.z());
    }

    /**
     * Hangs the frames again if the player moved too far from them for the client to draw them whichever
     * way they look. Run on the player's thread.
     *
     * @param player the Minecraft player
     * @param at     where the player is now
     */
    void follow(final Player player, final Location at) {
        final BlockPosition block = frameBlock;
        if (block == null || covers(block, frameFacing, at.x(), at.y(), at.z())) {
            return;
        }
        hangFrames(player, at.x(), at.y(), at.z());
    }

    /**
     * Hangs the screen maps where {@link #placement} puts them for a player standing at the given position.
     */
    private void hangFrames(final Player player, final double x, final double y, final double z) {
        final Placement placement = placement(x, y, z);
        final MapView[] views = screens;
        final MapFrame[] hung = new MapFrame[views.length];
        for (int tile = 0; tile < views.length; tile++) {
            hung[tile] = views[tile].hang(player, placement.block(), placement.facing(), true);
        }
        final MapFrame[] previous = frames;
        frames = hung;
        frameBlock = placement.block();
        frameFacing = placement.facing();
        for (final MapFrame frame : previous) {
            frame.remove();
        }
    }

    /**
     * {@return where to hang the frames for a player standing at the given position}: in the block of their
     * head, on the wall of the half of the block they stand in, at most half a block in front of them.
     * {@link #covers} holds there.
     */
    static Placement placement(final double x, final double y, final double z) {
        final BlockPosition block = Position.block((int) Math.floor(x), (int) Math.floor(y + EYE_HEIGHT - 0.25),
                (int) Math.floor(z));
        final BlockFace facing = z - Math.floor(z) <= 0.5 ? BlockFace.SOUTH : BlockFace.NORTH;
        return new Placement(block, facing);
    }

    /**
     * {@return whether frames hanging in {@code block} towards {@code facing} are drawn whichever way a player
     * standing at the given position looks, sneaking or not}
     *
     * <p>The camera must stay inside the box the client culls the frames with, further than the near plane
     * from its sides.</p>
     */
    static boolean covers(final BlockPosition block, final BlockFace facing, final double x, final double y,
                          final double z) {
        final double reach = FRAME_HALF_WIDTH + REACH;
        final double centreX = block.x() + 0.5;
        final double centreY = block.y() + 0.5;
        final double wall = facing == BlockFace.SOUTH ? block.z() : block.z() + 1.0;
        final double front = facing == BlockFace.SOUTH ? z - wall : wall - z;
        return Math.abs(x - centreX) <= reach
                && y + EYE_HEIGHT <= centreY + reach
                && y + SNEAKING_EYE_HEIGHT >= centreY - reach
                && front >= -REACH
                && front <= FRAME_DEPTH + REACH;
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
        frameBlock = null;
        final MapManager maps = session.manager().server().maps();
        for (final MapView screen : screens) {
            maps.delete(screen);
        }
        screens = new MapView[0];
        for (final TypedKey<Attribute> attribute : FROZEN_ATTRIBUTES) {
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
     * Where the screen frames hang.
     *
     * @param block  the block they take up
     * @param facing the direction their front faces
     */
    record Placement(BlockPosition block, BlockFace facing) {
    }

    /**
     * What the player had before playing.
     */
    private record Saved(GameMode gameMode, Location location, ItemStack[] inventory, int selectedSlot) {
    }
}
