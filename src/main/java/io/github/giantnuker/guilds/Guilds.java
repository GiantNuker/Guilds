package io.github.giantnuker.guilds;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.nyliummc.commands.BetterCommandContext;
import io.github.nyliummc.commands.CommandFeedback;
import io.github.nyliummc.commands.ServerCommandBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.CommandRegistry;
import net.minecraft.command.arguments.ColorArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;

import java.util.UUID;

public class Guilds implements ModInitializer {
	public static Config CONFIG = new Config();
	public static GuildManager GM = new GuildManager();
	@Override
	public void onInitialize() {
		CommandRegistry.INSTANCE.register(false, dispatcher -> dispatcher.register(new ServerCommandBuilder("guild").executes(Guilds::help)
						.defineArgument("guild", StringArgumentType.word()).definitionDone()
						.literal("create").argument("name", StringArgumentType.word()).argument("color", ColorArgumentType.color()).executes(Guilds::create).root()
						.literal("rename").predefinedArgument("guild").root()
						.literal("color").argument("color", ColorArgumentType.color()).root()
						.literal("delete").root()
						.literal("visibility")
							.literal("open").up()
							.literal("ask").up()
							.literal("close").up()
						.root()
						.defineArgument("player", StringArgumentType.string()).definitionDone()
						.literal("ranks").executes(Guilds::rankHelp)
							.literal("create").argument("rank", StringArgumentType.word()).argument("priority", IntegerArgumentType.integer(1, 10)).up("ranks")
							.defineArgument("rank", StringArgumentType.word()).definitionDone()
							.literal("prioritize").predefinedArgument("rank").argument("priority", IntegerArgumentType.integer(1, 10)).up("ranks")
							.literal("remove").predefinedArgument("rank").up("ranks")
							.literal("set").predefinedArgument("player").predefinedArgument("rank").up("ranks")
							.literal("color").predefinedArgument("rank").argument("color", ColorArgumentType.color()).up("ranks")
							.literal("help").executes(Guilds::rankHelp).up()
						.root()
						.literal("invite").predefinedArgument("player").root()
						.literal("remove").predefinedArgument("player").root()
						.literal("ignore").predefinedArgument("guild").root()
						.literal("request").predefinedArgument("guild").root()
						.literal("chat").argument("message", StringArgumentType.greedyString()).root()
						.literal("help").executes(Guilds::help).root()
						.build()));
	}

	private static UUID uuid(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		return context.getSource().getPlayer().getGameProfile().getId();
	}

	private static void create(BetterCommandContext<ServerCommandSource> context, CommandFeedback feedback) throws CommandSyntaxException {
		if (GM.getGuild(uuid(context)) != null) {
			context.getSource().sendFeedback(new LiteralText("You are already in a guild. You must leave it first").formatted(Formatting.RED), false);
		} else {
			String name = StringArgumentType.getString(context, "name");
			if (GM.guildExists(name)) {
				context.getSource().sendFeedback(new LiteralText("A guild with that name already exists").formatted(Formatting.RED), false);
			} else {
				Formatting color = ColorArgumentType.getColor(context, "color");
				Guild g = new Guild(name, color);
				GM.addGuild(g);
				GM.joinGuild(uuid(context), name);
				context.getSource().sendFeedback(new LiteralText(String.format("Created the guild %s", name)).formatted(Formatting.GREEN), false);
			}
		}
	}

	private static final String[][] helpArray = new String[][] {
					{ "create", "guild", "color", "Create a new guild" },
					{ "rename", "name", "Rename your guild" },
					{ "color", "Change your guild's color" },
					{ "delete", "Delete your guild" },
					{ "visibility", "Change your guild's visibility" },
					{ "ranks", "Change ranks in your guild" },
					{ "invite", "player", "Invite a player to your guild" },
					{ "remove", "player", "Remove a player from your guild" },
					{ "ignore", "guild", "Ignore a request to join a guild" },
					{ "request", "guild", "Request that a player joins your guild" },
					{ "chat", "Send a message in private guild chat" },
					{ "help", "Show this message" }
	};
	private static void help(BetterCommandContext<ServerCommandSource> context, CommandFeedback feedback) {
		context.getSource().sendFeedback(new LiteralText("---------- Guild Help ----------").formatted(Formatting.DARK_GREEN), false);
		for (String[] cmd: helpArray) {
			String command = "/guild " + cmd[0];
			String fullcommand = command;
			for (int i = 1; i < cmd.length - 1; i++) {
				fullcommand += " <" + cmd[i] + ">";
			}
			context.getSource().sendFeedback(new LiteralText(fullcommand + " - ").setStyle(new Style().setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command)).setColor(Formatting.GRAY)).append(new LiteralText(cmd[cmd.length - 1]).formatted(Formatting.GREEN)), false);
		}
		context.getSource().sendFeedback(new LiteralText("-----------------------------").formatted(Formatting.DARK_GREEN), false);
	}
	private static final String[][] rankHelpArray = new String[][] {
					{ "create", "rank", "priority", "Create a new rank" },
					{ "prioritize", "rank", "priority", "Change a rank's priority" },
					{ "remove", "rank", "Remove a rank" },
					{ "set", "player", "rank", "Change a guild member's rank" },
					{ "color", "rank", "color", "Change the color of a rank" }
	};
	private static void rankHelp(BetterCommandContext<ServerCommandSource> context, CommandFeedback feedback) {
		context.getSource().sendFeedback(new LiteralText("---------- Guild Help: Ranks ----------").formatted(Formatting.DARK_GREEN), false);
		for (String[] cmd: rankHelpArray) {
			String command = "/guild ranks " + cmd[0];
			String fullcommand = command;
			for (int i = 1; i < cmd.length - 1; i++) {
				fullcommand += " <" + cmd[i] + ">";
			}
			context.getSource().sendFeedback(new LiteralText(fullcommand + " - ").setStyle(new Style().setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command)).setColor(Formatting.GRAY)).append(new LiteralText(cmd[cmd.length - 1]).formatted(Formatting.GREEN)), false);
		}
		context.getSource().sendFeedback(new LiteralText("Note that priorities are sorted from lowest to highest, lowest being the best").formatted(Formatting.GRAY), false);
		context.getSource().sendFeedback(new LiteralText("------------------------------------").formatted(Formatting.DARK_GREEN), false);
	}
}
