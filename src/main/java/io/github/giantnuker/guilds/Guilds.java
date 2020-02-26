package io.github.giantnuker.guilds;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.nyliummc.commands.BetterCommandContext;
import io.github.nyliummc.commands.CommandFeedback;
import io.github.nyliummc.commands.ServerCommandBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.CommandRegistry;
import net.minecraft.command.arguments.ColorArgumentType;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.UUID;

public class Guilds implements ModInitializer {
	private static final String[][] helpArray = new String[][] {
					{"create", "guild", "color", "Create a new guild"},
					{"rename", "name", "Rename your guild"},
					{"recolor", "Change your guild's color"},
					{"delete", "Delete your guild"},
					{"visibility", "Change your guild's visibility"},
					{"ranks", "Change ranks in your guild"},
					{"invite", "player", "Invite a player to your guild"},
					{"accept", "guild", "Accept an invite to a guild"},
					{"remove", "player", "Remove a player from your guild"},
					{"ignore", "guild", "Ignore a request to join a guild"},
					{"request", "guild", "Request that a player joins your guild"},
					{"chat", "Send a message in private guild chat"},
					{"help", "Show this message"}
	};
	private static final String[][] rankHelpArray = new String[][] {
					{"create", "rank", "priority", "Create a new rank"},
					{"prioritize", "rank", "priority", "Change a rank's priority"},
					{"remove", "rank", "Remove a rank"},
					{"set", "player", "rank", "Change a guild member's rank"},
					{"color", "rank", "color", "Change the color of a rank"}
	};
	public static Config CONFIG = new Config();
	public static GuildManager GM = new GuildManager();

	private static void delete(BetterCommandContext<ServerCommandSource> context, CommandFeedback feedback, boolean confirmed) throws CommandSyntaxException {
		if (doOwnerCheck(context)) {
			if (confirmed) {
				GM.removeGuild(GM.getGuild(uuid(context)).getName());
				context.getSource().sendFeedback(new LiteralText("Your guild was removed").formatted(Formatting.GREEN), false);
			} else {
				context.getSource().sendFeedback(new LiteralText("Do you really want to delete your guild? ").formatted(Formatting.YELLOW).append(new LiteralText("").formatted(Formatting.BOLD).append(new LiteralText("[YES]").setStyle(new Style().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/guild delete confirmed")).setColor(Formatting.DARK_RED)))), false);
			}
		}
	}

	private static UUID uuid(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		return context.getSource().getPlayer().getGameProfile().getId();
	}

	private static boolean doOwnerCheck(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		if (GM.getGuild(uuid(context)) != null) {
			if (GM.getGuild(uuid(context)).getOwner().equals(uuid(context))) {
				return true;
			} else {
				context.getSource().sendFeedback(new LiteralText("You are not the owner of your guild").formatted(Formatting.RED), false);
			}
		} else {
			context.getSource().sendFeedback(new LiteralText("You are not a member of a guild").formatted(Formatting.RED), false);
		}
		return false;
	}

	private static void create(BetterCommandContext<ServerCommandSource> context, CommandFeedback feedback) throws CommandSyntaxException {
		if (GM.getGuild(uuid(context)) == null) {
			String name = StringArgumentType.getString(context, "name");
			if (!GM.guildExists(name)) {
				Formatting color = ColorArgumentType.getColor(context, "color");
				Guild g = new Guild(name, color, uuid(context));
				GM.addGuild(g);
				context.getSource().sendFeedback(new LiteralText(String.format("Created the guild %s", name)).formatted(Formatting.GREEN), false);
			} else {
				context.getSource().sendFeedback(new LiteralText("A guild with that name already exists").formatted(Formatting.RED), false);
			}
		} else {
			context.getSource().sendFeedback(new LiteralText("You are already in a guild. You must leave it first").formatted(Formatting.RED), false);
		}
	}

	private static void help(BetterCommandContext<ServerCommandSource> context, CommandFeedback feedback) {
		context.getSource().sendFeedback(new LiteralText("---------- Guild Help ----------").formatted(Formatting.DARK_GREEN), false);
		for (String[] cmd : helpArray) {
			String command = "/guild " + cmd[0];
			String fullcommand = command;
			for (int i = 1; i < cmd.length - 1; i++) {
				fullcommand += " <" + cmd[i] + ">";
			}
			context.getSource().sendFeedback(new LiteralText(fullcommand + " - ").setStyle(new Style().setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command)).setColor(Formatting.GRAY)).append(new LiteralText(cmd[cmd.length - 1]).formatted(Formatting.GREEN)), false);
		}
		context.getSource().sendFeedback(new LiteralText("-----------------------------").formatted(Formatting.DARK_GREEN), false);
	}

	private static void rankHelp(BetterCommandContext<ServerCommandSource> context, CommandFeedback feedback) {
		context.getSource().sendFeedback(new LiteralText("---------- Guild Help: Ranks ----------").formatted(Formatting.DARK_GREEN), false);
		for (String[] cmd : rankHelpArray) {
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

	private static void acceptInvite(BetterCommandContext<ServerCommandSource> context, CommandFeedback feedback) throws CommandSyntaxException {
		if (GM.getGuild(uuid(context)) == null) {
			if (GM.acceptInvite(uuid(context), StringArgumentType.getString(context, "guild"))) {
				Guild guild = GM.guilds.get(StringArgumentType.getString(context, "guild"));
				context.getSource().sendFeedback(new LiteralText("You have joined the guild ").formatted(Formatting.GREEN).append(new LiteralText(guild.getName()).formatted(guild.getColor())), false);
			} else {
				context.getSource().sendFeedback(new LiteralText("You don't have an invite from that guild").formatted(Formatting.RED), false);
			}
		} else {
			context.getSource().sendFeedback(new LiteralText("You are already in a guild. You must leave it first").formatted(Formatting.RED), false);
		}
	}

	private static void invite(BetterCommandContext<ServerCommandSource> context, CommandFeedback feedback) throws CommandSyntaxException {
		if (doOwnerCheck(context)) {
			UUID player = EntityArgumentType.getPlayer(context, "player").getGameProfile().getId();
			GM.invite(player, GM.getGuild(uuid(context)).getName());
			context.getSource().sendFeedback(new LiteralText("You have invited ").formatted(Formatting.GREEN).append(Team.modifyText(EntityArgumentType.getPlayer(context, "player").getScoreboardTeam(), EntityArgumentType.getPlayer(context, "player").getName())).append(new LiteralText(" to the guild").formatted(Formatting.GREEN)), false);
		}
	}

	private static void chat(BetterCommandContext<ServerCommandSource> context, CommandFeedback feedback) throws CommandSyntaxException {
		Guild guild = GM.getGuild(uuid(context));
		if (guild != null) {
			Guild.Rank rank = guild.getRank(uuid(context));
			Text rankedName = rank != null ? new LiteralText("").append(rank.getColor() != null ? new LiteralText(rank.getName()).formatted(rank.getColor()) : new LiteralText(rank.getName())).append(context.getSource().getPlayer().getName()) : context.getSource().getPlayer().getName();
			Text player = Team.modifyText(context.getSource().getPlayer().getScoreboardTeam(), rankedName);
			Text message = new LiteralText("").append(new LiteralText(CONFIG.guildChatPrefix)).append(player).append(CONFIG.guildChatSuffix).append(StringArgumentType.getString(context, "message"));
			context.getSource().getMinecraftServer().sendMessage(message);
			for (ServerPlayerEntity oplayer : context.getSource().getMinecraftServer().getPlayerManager().getPlayerList()) {
				if (GM.getGuild(oplayer.getGameProfile().getId()).equals(guild)) {
					oplayer.sendMessage(message);
				}
			}
		} else {
			context.getSource().sendFeedback(new LiteralText("You are not a member of a guild").formatted(Formatting.RED), false);
		}
	}

	private static void recolor(BetterCommandContext<ServerCommandSource> context, CommandFeedback feedback) throws CommandSyntaxException {
		if (doOwnerCheck(context)) {
			Formatting color = ColorArgumentType.getColor(context, "color");
			GM.getGuild(uuid(context)).setColor(color);
			context.getSource().sendFeedback(new LiteralText("Your guild's color was changed to ").formatted(Formatting.GREEN).append(new LiteralText(color.getName()).formatted(color)), false);
		}
	}

	private static void rename(BetterCommandContext<ServerCommandSource> context, CommandFeedback feedback) throws CommandSyntaxException {
		if (doOwnerCheck(context)) {
			String name = StringArgumentType.getString(context, "name");
			if (!GM.guildExists(name)) {
				GM.renameGuild(GM.getGuild(uuid(context)).getName(), name);
				context.getSource().sendFeedback(new LiteralText(String.format("Renamed your guild to %s", name)).formatted(Formatting.GREEN), false);
			} else {
				context.getSource().sendFeedback(new LiteralText("A guild with that name already exists").formatted(Formatting.RED), false);
			}
		}
	}

	@Override
	public void onInitialize() {
		CommandRegistry.INSTANCE.register(false, dispatcher -> dispatcher.register(new ServerCommandBuilder("guild").executes(Guilds::help)
						.defineArgument("guild", StringArgumentType.word()).definitionDone()
						.literal("create").argument("name", StringArgumentType.word()).argument("color", ColorArgumentType.color()).executes(Guilds::create).root()
						.literal("rename").argument("name", StringArgumentType.word()).executes(Guilds::rename).root()
						.literal("recolor").argument("color", ColorArgumentType.color()).executes(Guilds::recolor).root()
						.literal("delete").executes((context, feedback) -> Guilds.delete(context, feedback, false)).literal("confirmed").executes((context, feedback) -> Guilds.delete(context, feedback, true)).root()
						.literal("visibility")
						.literal("open").up()
						.literal("ask").up()
						.literal("close").up()
						.root()
						.defineArgument("player", EntityArgumentType.player()).definitionDone()
						.literal("ranks").executes(Guilds::rankHelp)
						.literal("create").argument("rank", StringArgumentType.word()).up("ranks")
						.defineArgument("rank", StringArgumentType.word()).definitionDone()
						.literal("remove").predefinedArgument("rank").up("ranks")
						.literal("set").predefinedArgument("player").predefinedArgument("rank").up("ranks")
						.literal("color").predefinedArgument("rank").argument("color", ColorArgumentType.color()).up("ranks")
						.literal("help").executes(Guilds::rankHelp).up()
						.root()
						.literal("invite").predefinedArgument("player").executes(Guilds::invite).root()
						.literal("accept").predefinedArgument("guild").executes(Guilds::acceptInvite).root()
						.literal("remove").predefinedArgument("player").root()
						.literal("ignore").predefinedArgument("guild").root()
						.literal("request").predefinedArgument("guild").root()
						.literal("chat").argument("message", StringArgumentType.greedyString()).executes(Guilds::chat).root()
						.literal("help").executes(Guilds::help).root()
						.build()));
	}
}
