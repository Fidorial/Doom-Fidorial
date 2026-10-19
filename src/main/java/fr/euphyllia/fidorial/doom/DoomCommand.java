package fr.euphyllia.fidorial.doom;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.euphyllia.fidorial.doom.engine.Defs;
import fr.euphyllia.fidorial.doom.session.DoomPlayer;
import fr.euphyllia.fidorial.doom.session.DoomSession;
import fr.euphyllia.fidorial.doom.session.SessionManager;
import fr.fidorial.command.CommandSource;
import fr.fidorial.entity.player.Player;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jspecify.annotations.Nullable;

import java.util.stream.Collectors;

import static fr.fidorial.command.Commands.argument;
import static fr.fidorial.command.Commands.literal;

/**
 * {@code /doom}: start, join and leave games, and a few settings.
 */
final class DoomCommand {

    private static final String[] SKILLS = {
            "I'm too young to die", "Hey, not too rough", "Hurt me plenty", "Ultra-Violence", "Nightmare!"
    };

    private final DoomPlugin plugin;

    DoomCommand(final DoomPlugin plugin) {
        this.plugin = plugin;
    }

    private static Component button(final String label, final String command) {
        return Component.text("[" + label + "]", NamedTextColor.GREEN)
                .clickEvent(ClickEvent.runCommand(command))
                .hoverEvent(HoverEvent.showText(Component.text(command)));
    }

    LiteralCommandNode<CommandSource> create() {
        return literal("doom")
                .executes(this::menu)
                .then(start("play", DoomSession.Mode.SOLO))
                .then(start("coop", DoomSession.Mode.COOP))
                .then(start("deathmatch", DoomSession.Mode.DEATHMATCH))
                .then(literal("join")
                        .then(argument("player", StringArgumentType.word())
                                .executes(this::join)))
                .then(literal("quit").executes(this::quit))
                .then(literal("sessions").executes(this::sessions))
                .then(literal("map").executes(ctx -> withPlayer(ctx, (m, p) -> m.toggleAutoMap(p.uuid()))))
                .then(literal("run").executes(ctx -> withPlayer(ctx, (m, p) -> m.toggleRun(p.uuid()))))
                .then(literal("size")
                        .then(argument("size", IntegerArgumentType.integer(3, 11))
                                .executes(ctx -> withPlayer(ctx,
                                        (m, p) -> m.viewSize(p.uuid(), ctx.getArgument("size", Integer.class))))))
                .then(literal("cheat")
                        .then(argument("code", StringArgumentType.word())
                                .executes(ctx -> withPlayer(ctx,
                                        (m, p) -> m.cheat(p.uuid(), ctx.getArgument("code", String.class))))))
                .build();
    }

    private LiteralArgumentBuilder<CommandSource> start(final String name, final DoomSession.Mode mode) {
        return literal(name)
                .executes(ctx -> start(ctx, mode, 3, 1, 1))
                .then(argument("skill", IntegerArgumentType.integer(1, 5))
                        .executes(ctx -> start(ctx, mode, ctx.getArgument("skill", Integer.class), 1, 1))
                        .then(argument("episode", IntegerArgumentType.integer(1, 32))
                                .executes(ctx -> start(ctx, mode, ctx.getArgument("skill", Integer.class),
                                        ctx.getArgument("episode", Integer.class), 1))
                                .then(argument("map", IntegerArgumentType.integer(1, 32))
                                        .executes(ctx -> start(ctx, mode, ctx.getArgument("skill", Integer.class),
                                                ctx.getArgument("episode", Integer.class),
                                                ctx.getArgument("map", Integer.class))))));
    }

    private @Nullable Player player(final CommandContext<CommandSource> ctx) {
        if (ctx.getSource().executor() instanceof final Player player) {
            return player;
        }
        ctx.getSource().sendMessage(Component.text("Run this command in game.", NamedTextColor.RED));
        return null;
    }

    private @Nullable SessionManager manager(final Audience audience) {
        final SessionManager manager = plugin.sessions();
        if (manager == null) {
            audience.sendMessage(Component.text(plugin.status(), NamedTextColor.RED));
        }
        return manager;
    }

    private int withPlayer(final CommandContext<CommandSource> ctx, final PlayerAction action) {
        final Player player = player(ctx);
        if (player == null) {
            return 0;
        }
        final SessionManager manager = manager(player);
        if (manager == null) {
            return 0;
        }
        if (!action.run(manager, player)) {
            player.sendMessage(Component.text("You are not playing Doom. Try /doom", NamedTextColor.RED));
            return 0;
        }
        return Command.SINGLE_SUCCESS;
    }

    private int start(final CommandContext<CommandSource> ctx, final DoomSession.Mode mode, final int skill,
                      final int episode, final int map) {
        final Player player = player(ctx);
        if (player == null) {
            return 0;
        }
        final SessionManager manager = manager(player);
        if (manager == null) {
            return 0;
        }
        final boolean commercial = manager.gameMode() == Defs.commercial;
        final int e = commercial ? 1 : episode;
        final int m = commercial && episode > 1 && map == 1 ? episode : map;
        final DoomSession session = manager.create(player, mode, skill - 1, e, m);
        player.sendMessage(Component.text("Doom session #" + session.id() + " started ("
                + SKILLS[skill - 1] + "). WASD move, mouse turn, right click fire, space use, 1-7 weapons,"
                + " 9 next weapon, F automap, Q always run, /doom quit to leave.", NamedTextColor.GREEN));
        if (mode != DoomSession.Mode.SOLO) {
            plugin.server().players().online().forEach(other -> {
                if (other != player) {
                    other.sendMessage(Component.text(player.name() + " started a Doom " + mode.name().toLowerCase(java.util.Locale.ROOT)
                                    + " game. ", NamedTextColor.GOLD)
                            .append(Component.text("[Join]", NamedTextColor.GREEN)
                                    .clickEvent(ClickEvent.runCommand("/doom join " + player.name()))
                                    .hoverEvent(HoverEvent.showText(Component.text("/doom join " + player.name())))));
                }
            });
        }
        return Command.SINGLE_SUCCESS;
    }

    private int join(final CommandContext<CommandSource> ctx) {
        final Player player = player(ctx);
        if (player == null) {
            return 0;
        }
        final SessionManager manager = manager(player);
        if (manager == null) {
            return 0;
        }
        final String name = ctx.getArgument("player", String.class);
        final Player host = plugin.server().players().get(name).orElse(null);
        final DoomPlayer hostPlayer = host == null ? null : manager.player(host.uuid());
        if (hostPlayer == null) {
            player.sendMessage(Component.text(name + " is not playing Doom.", NamedTextColor.RED));
            return 0;
        }
        final DoomSession session = hostPlayer.session();
        if (session.mode() == DoomSession.Mode.SOLO || !manager.join(player, session)) {
            player.sendMessage(Component.text("That game is full or single player.", NamedTextColor.RED));
            return 0;
        }
        player.sendMessage(Component.text("Joined Doom session #" + session.id() + ".", NamedTextColor.GREEN));
        return Command.SINGLE_SUCCESS;
    }

    private int quit(final CommandContext<CommandSource> ctx) {
        final Player player = player(ctx);
        if (player == null) {
            return 0;
        }
        final SessionManager manager = manager(player);
        if (manager == null || !manager.leave(player.uuid())) {
            player.sendMessage(Component.text("You are not playing Doom.", NamedTextColor.RED));
            return 0;
        }
        player.sendMessage(Component.text("Left Doom.", NamedTextColor.GREEN));
        return Command.SINGLE_SUCCESS;
    }

    private int sessions(final CommandContext<CommandSource> ctx) {
        final SessionManager manager = manager(ctx.getSource());
        if (manager == null) {
            return 0;
        }
        if (manager.sessions().isEmpty()) {
            ctx.getSource().sendMessage(Component.text("No Doom game running.", NamedTextColor.GRAY));
            return 0;
        }
        for (final DoomSession session : manager.sessions()) {
            final String names = session.players().stream().map(DoomPlayer::name).collect(Collectors.joining(", "));
            ctx.getSource().sendMessage(Component.text("#" + session.id() + " " + session.mode() + ": " + names,
                    NamedTextColor.YELLOW));
        }
        return manager.sessions().size();
    }

    private int menu(final CommandContext<CommandSource> ctx) {
        final Audience out = ctx.getSource();
        out.sendMessage(Component.text("DOOM on Fidorial", NamedTextColor.RED));
        final SessionManager manager = manager(out);
        if (manager == null) {
            return 0;
        }
        final boolean commercial = manager.gameMode() == Defs.commercial;
        for (int skill = 1; skill <= SKILLS.length; skill++) {
            Component line = Component.text(" " + SKILLS[skill - 1] + ": ", NamedTextColor.GRAY);
            if (commercial) {
                line = line.append(button("MAP01", "/doom play " + skill + " 1 1"));
            } else {
                final int episodes = manager.gameMode() == Defs.retail ? 4 : manager.gameMode() == Defs.registered ? 3 : 1;
                for (int e = 1; e <= episodes; e++) {
                    line = line.append(button("E" + e, "/doom play " + skill + " " + e + " 1"))
                            .append(Component.space());
                }
            }
            out.sendMessage(line);
        }
        out.sendMessage(Component.text(" /doom coop | deathmatch [skill] [episode] [map], /doom join <player>, /doom quit",
                NamedTextColor.DARK_GRAY));
        return Command.SINGLE_SUCCESS;
    }

    @FunctionalInterface
    private interface PlayerAction {
        boolean run(SessionManager manager, Player player);
    }
}
