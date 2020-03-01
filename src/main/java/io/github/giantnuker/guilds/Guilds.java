package io.github.giantnuker.guilds;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.github.nyliummc.commands.BetterCommandContext;
import io.github.nyliummc.commands.CommandFeedback;
import io.github.nyliummc.commands.ServerCommandBuilder;
import io.github.voidpointerdev.minecraft.offlineinfo.OfflineInfo;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.CommandRegistry;
import net.minecraft.command.arguments.ColorArgumentType;
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
					{"recolor", "color", "Change your guild's color"},
					{"delete", "Delete your guild"},
					{"visibility", "Change your guild's visibility"},
					{"promote", "Make someone else the guild leader"},
					{"invite", "player", "Invite a player to your guild"},
					{"invites", "Lists all the guilds you've been invited to"},
					{"members", "Lists everyone in the guild"},
					{"kick", "player", "Kicks a player from the guild"},
					{"request", "guild", "Request that a player joins your guild"},
					{"requests", "guild", "View all join requests"},
					{"leave", "Leave your guild"},
					{"chat", "Send a message in private guild chat"},
					{"help", "Show this message"}
	};
	public static Config CONFIG = new Config();
	public static GuildManager GM = new GuildManager();
	private static final SuggestionProvider<ServerCommandSource> INVITES_PROVIDER = (context, builder) -> {
		if (GM.pendingInvites.get(uuid(context)) == null || GM.pendingInvites.get(uuid(context)).isEmpty()) {
			return null;
		} else {
			for (String guild : GM.pendingInvites.get(uuid(context))) {
				builder.suggest(guild);
			}

			return builder.buildFuture();
		}
	};

	private static void delete(BetterCommandContext<ServerCommandSource> context, CommandFeedback feedback, boolean confirmed) throws CommandSyntaxException {
		if (doOwnerCheck(context)) {
			if (confirmed) {
				for (UUID member : GM.getGuild(uuid(context)).members) {
					if (!member.equals(uuid(context))) {
						ASAPMessages.message(member, context, new LiteralText("Your guild was deleted by the owner").formatted(Formatting.DARK_RED));
					}
				}

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
				GM.joinGuild(uuid(context), name);
				context.getSource().sendFeedback(new LiteralText("Created the guild ").formatted(Formatting.GREEN).append(new LiteralText(name).formatted(color)), false);
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

	private static void acceptInvite(BetterCommandContext<ServerCommandSource> context, CommandFeedback feedback) throws CommandSyntaxException {
		if (GM.getGuild(uuid(context)) == null) {
			if (GM.acceptInvite(uuid(context), StringArgumentType.getString(context, "invite"))) {
				Guild guild = GM.guilds.get(StringArgumentType.getString(context, "invite"));
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
			UUID player = OfflineInfo.getUUID(context, "player");

			if (player == uuid(context)) {
				context.getSource().sendFeedback(new LiteralText("You can't invite yourself to your own guild!").formatted(Formatting.RED), false);
			} else {
				if (GM.getGuild(uuid(context)).requests.contains(player)) {
					context.getSource().sendFeedback(new LiteralText("This player has already sent a request to join your guild. Just accept that").formatted(Formatting.RED), false);
				} else {
					GM.invite(player, GM.getGuild(uuid(context)).getName());
					String guild = GM.getGuild(uuid(context)).getName();
					ASAPMessages.message(player, context, new LiteralText("You have been invited to join the guild ").formatted(Formatting.YELLOW).append(new LiteralText(guild).formatted(GM.guilds.get(guild).getColor())).append(new LiteralText(" [JOIN]").setStyle(new Style().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/guild accept_invite " + guild)).setColor(Formatting.GREEN))).append(new LiteralText(" [DENY]").setStyle(new Style().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/guild deny_invite " + guild)).setColor(Formatting.RED))));
					context.getSource().sendFeedback(new LiteralText("You have invited ").formatted(Formatting.GREEN).append(StringArgumentType.getString(context, "player")).append(new LiteralText(" to the guild").formatted(Formatting.GREEN)), false);
				}
			}
		}
	}

	private static void chat(BetterCommandContext<ServerCommandSource> context, CommandFeedback feedback) throws CommandSyntaxException {
		Guild guild = GM.getGuild(uuid(context));

		if (guild != null) {
			Text player = Team.modifyText(context.getSource().getPlayer().getScoreboardTeam(), context.getSource().getPlayer().getName());
			Text message = new LiteralText("").append(new LiteralText(CONFIG.guildChatPrefix)).append(player).append(CONFIG.guildChatSuffix).append(StringArgumentType.getString(context, "message"));
			context.getSource().getMinecraftServer().sendMessage(message);

			for (ServerPlayerEntity oplayer : context.getSource().getMinecraftServer().getPlayerManager().getPlayerList()) {
				if (GM.getGuild(oplayer.getGameProfile().getId()) != null && GM.getGuild(oplayer.getGameProfile().getId()).equals(guild)) {
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

	private static void listInvites(BetterCommandContext<ServerCommandSource> context, CommandFeedback feedback) throws CommandSyntaxException {
		if (GM.pendingInvites.get(uuid(context)) == null || GM.pendingInvites.get(uuid(context)).isEmpty()) {
			context.getSource().sendFeedback(new LiteralText("You have no pending invites").formatted(Formatting.YELLOW), false);
		} else {
			for (String guild : GM.pendingInvites.get(uuid(context))) {
				GM.sendInviteMessage(context.getSource().getPlayer(), guild);
			}
		}
	}

	private static void denyInvite(BetterCommandContext<ServerCommandSource> context, CommandFeedback feedback) throws CommandSyntaxException {
		if (GM.denyInvite(uuid(context), StringArgumentType.getString(context, "invite"))) {
			Guild guild = GM.guilds.get(StringArgumentType.getString(context, "invite"));
			context.getSource().sendFeedback(new LiteralText("You have denied the invite to ").formatted(Formatting.GREEN).append(new LiteralText(guild.getName()).formatted(guild.getColor())), false);
		} else {
			context.getSource().sendFeedback(new LiteralText("You don't have an invite from that guild").formatted(Formatting.RED), false);
		}
	}

	private static void promote(BetterCommandContext<ServerCommandSource> context, CommandFeedback feedback) throws CommandSyntaxException {
		if (doOwnerCheck(context)) {
			UUID player = OfflineInfo.getUUID(context, "player");

			if (GM.getGuild(player) == GM.getGuild(uuid(context))) {
				GM.getGuild(uuid(context)).setOwner(player);
				ASAPMessages.message(player, context, new LiteralText("You have been promoted to leader of your guild").formatted(Formatting.GREEN));
				context.getSource().sendFeedback(new LiteralText("You have promoted ").formatted(Formatting.GREEN).append(StringArgumentType.getString(context, "player")).append(new LiteralText(" to guild leader").formatted(Formatting.GREEN)), false);
			} else {
				context.getSource().sendFeedback(new LiteralText(String.format("%s is not a member of your guild", StringArgumentType.getString(context, "player"))).formatted(Formatting.RED), false);
			}
		}
	}

	private static void leave(BetterCommandContext<ServerCommandSource> context, CommandFeedback feedback, boolean confirm) throws CommandSyntaxException {
		if (GM.getGuild(uuid(context)) != null) {
			if (!GM.getGuild(uuid(context)).getOwner().equals(uuid(context))) {
				if (confirm) {
					GM.leaveGuild(uuid(context));
					context.getSource().sendFeedback(new LiteralText("You have left your guild").formatted(Formatting.GREEN), false);
				} else {
					context.getSource().sendFeedback(new LiteralText("Are you sure you want to leave your guild? ").formatted(Formatting.YELLOW).append(new LiteralText("").formatted(Formatting.BOLD).append(new LiteralText("[YES]").setStyle(new Style().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/guild leave confirmed")).setColor(Formatting.DARK_RED)))), false);
				}
			} else {
				context.getSource().sendFeedback(new LiteralText("As the guild leader, you cannot leave your guild. Promote someone else or delete the guild to leave").formatted(Formatting.RED), false);
			}
		} else {
			context.getSource().sendFeedback(new LiteralText("You are not a member of a guild").formatted(Formatting.RED), false);
		}
	}

	private static void denyRequest(BetterCommandContext<ServerCommandSource> context, CommandFeedback feedback) throws CommandSyntaxException {
		if (doOwnerCheck(context)) {
			Guild g = GM.getGuild(uuid(context));
			UUID player = OfflineInfo.getUUID(context, "player");

			if (g.requests.contains(player)) {
				g.requests.remove(player);
				context.getSource().sendFeedback(new LiteralText("Request denied").formatted(Formatting.GREEN), false);
				ASAPMessages.message(player, context, new LiteralText("Your request to join ").formatted(Formatting.RED).append(new LiteralText(g.getName()).formatted(g.getColor())).append(" was denied"));
			} else {
				context.getSource().sendFeedback(new LiteralText("That player has not requested to join your guild").formatted(Formatting.RED), false);
			}
		}
	}

	private static void acceptRequest(BetterCommandContext<ServerCommandSource> context, CommandFeedback feedback) throws CommandSyntaxException {
		if (doOwnerCheck(context)) {
			Guild g = GM.getGuild(uuid(context));
			UUID player = OfflineInfo.getUUID(context, "player");

			if (g.requests.contains(player)) {
				g.requests.remove(player);
				GM.joinGuild(player, g.getName());
				context.getSource().sendFeedback(new LiteralText("Request accepted").formatted(Formatting.GREEN), false);
				ASAPMessages.message(player, context, new LiteralText("Your request to join ").formatted(Formatting.GREEN).append(new LiteralText(g.getName()).formatted(g.getColor())).append(" was accepted"));
			} else {
				context.getSource().sendFeedback(new LiteralText("That player has not requested to join your guild").formatted(Formatting.RED), false);
			}
		}
	}

	private static void requests(BetterCommandContext<ServerCommandSource> context, CommandFeedback feedback) throws CommandSyntaxException {
		if (doOwnerCheck(context)) {
			Guild guild = GM.getGuild(uuid(context));
			Text text = new LiteralText("").append(new LiteralText(String.format("Requests (%d): ", guild.members.size())).formatted(Formatting.YELLOW));

			for (int i = 0; i < guild.requests.size(); i++) {
				String n = OfflineInfo.getNameById(context.getSource().getMinecraftServer().getUserCache(), guild.requests.get(i));
				text.append(new LiteralText(n).formatted(Formatting.GOLD));
				text.append(new LiteralText(" [A]").setStyle(new Style().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/guild accept_request " + n)).setColor(Formatting.GREEN)));
				text.append(new LiteralText(" [D]").setStyle(new Style().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/guild deny_request " + n)).setColor(Formatting.RED)));

				if (i < guild.requests.size() - 1) {
					text.append(new LiteralText(", "));
				}
			}

			context.getSource().sendFeedback(text, false);
		}
	}

	private static void request(BetterCommandContext<ServerCommandSource> context, CommandFeedback feedback) throws CommandSyntaxException {
		if (GM.getGuild(uuid(context)) == null) {
			String guild = StringArgumentType.getString(context, "guild");

			if (GM.guildExists(guild)) {
				Guild g = GM.guilds.get(guild);

				if (!g.requests.contains(uuid(context))) {
					switch (g.visibility) {
					case OPEN:
						context.getSource().sendFeedback(new LiteralText("You have joined the open guild ").formatted(Formatting.GREEN).append(new LiteralText(guild).formatted(g.getColor())), false);
						GM.joinGuild(uuid(context), guild);
						break;
					case ASK:
						g.requests.add(uuid(context));
						context.getSource().sendFeedback(new LiteralText("You have requested to join ").formatted(Formatting.GREEN).append(new LiteralText(guild).formatted(g.getColor())), false);
						ServerPlayerEntity owner = context.getSource().getMinecraftServer().getPlayerManager().getPlayer(g.owner);

						if (owner != null) {
							owner.sendMessage(new LiteralText(context.getSource().getPlayer().getGameProfile().getName()).formatted(Formatting.GOLD).append(new LiteralText(" has requested to join your guild").formatted(Formatting.YELLOW)).append(new LiteralText(" [ACCEPT]").setStyle(new Style().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/guild accept_request " + context.getSource().getPlayer().getGameProfile().getName())).setColor(Formatting.GREEN))).append(new LiteralText(" [DENY]").setStyle(new Style().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/guild deny_request " + context.getSource().getPlayer().getGameProfile().getName())).setColor(Formatting.RED))));
						}

						break;
					case CLOSED:
						context.getSource().sendFeedback(new LiteralText("You cannot join the guild ").formatted(Formatting.RED).append(new LiteralText(guild).formatted(g.getColor())).append(" because it is closed"), false);
						break;
					}
				} else {
					context.getSource().sendFeedback(new LiteralText("You have already requested to join this guild").formatted(Formatting.RED), false);
				}
			} else {
				context.getSource().sendFeedback(new LiteralText("That guild does not exist").formatted(Formatting.RED), false);
			}
		} else {
			context.getSource().sendFeedback(new LiteralText("You are already in a guild. You must leave it first").formatted(Formatting.RED), false);
		}
	}

	private static void kick(BetterCommandContext<ServerCommandSource> context, CommandFeedback feedback) throws CommandSyntaxException {
		if (doOwnerCheck(context)) {
			String toKickName = StringArgumentType.getString(context, "player");
			UUID toKickID = OfflineInfo.getUUID(context, "player");

			if (!toKickID.equals(uuid(context))) {
				Guild guild = GM.getGuild(uuid(context));

				if (GM.getGuild(toKickID).equals(guild)) {
					GM.leaveGuild(toKickID);
					context.getSource().sendFeedback(new LiteralText("Kicked ").formatted(Formatting.GREEN).append(new LiteralText(toKickName).formatted(Formatting.GOLD)), false);
					ASAPMessages.message(toKickID, context, new LiteralText("You have been ").formatted(Formatting.YELLOW).append(new LiteralText("kicked").formatted(Formatting.DARK_RED)).append(" from your guild."));
				} else {
					context.getSource().sendFeedback(new LiteralText("That player is not in your guild").formatted(Formatting.RED), false);
				}
			} else {
				context.getSource().sendFeedback(new LiteralText("You can't kick yourself from your guild").formatted(Formatting.RED), false);
			}
		}
	}

	private static void listMembers(BetterCommandContext<ServerCommandSource> context, CommandFeedback feedback) throws CommandSyntaxException {
		if (GM.getGuild(uuid(context)) != null) {
			Guild guild = GM.getGuild(uuid(context));
			Text text = new LiteralText("").append(new LiteralText(String.format("Members (%d): ", guild.members.size())).formatted(Formatting.YELLOW));

			for (int i = 0; i < guild.members.size(); i++) {
				text.append(new LiteralText(OfflineInfo.getNameById(context.getSource().getMinecraftServer().getUserCache(), guild.members.get(i))).formatted(Formatting.GOLD));

				if (i < guild.members.size() - 1) {
					text.append(new LiteralText(", "));
				}
			}

			context.getSource().sendFeedback(text, false);
		} else {
			context.getSource().sendFeedback(new LiteralText("You are not a member of a guild").formatted(Formatting.RED), false);
		}
	}

	private static void setVisibility(BetterCommandContext<ServerCommandSource> context, CommandFeedback feedback, Guild.Visibility visibility) throws CommandSyntaxException {
		if (doOwnerCheck(context)) {
			GM.getGuild(uuid(context)).setVisibility(visibility, context.getSource().getMinecraftServer().getPlayerManager());
			context.getSource().sendFeedback(new LiteralText("Your guilds visibility was changed to ").formatted(Formatting.GREEN).append(new LiteralText(visibility.name()).formatted(Formatting.GOLD)), false);
		}
	}

	@Override
	public void onInitialize() {
		/*try {
			File configFile = new File(FabricLoader.getInstance().getConfigDirectory(), "guilds.hocon");
			ConfigurationLoader<CommentedConfigurationNode> config = HoconConfigurationLoader.builder().setFile(configFile).build();

			if (!configFile.exists()) {
				configFile.createNewFile();
			}

			ConfigurationNode node = config.load(ConfigurationOptions.defaults().setHeader("~~owo~~\n~~uwu~~").setObjectMapperFactory(DefaultObjectMapperFactory.getInstance()).setShouldCopyDefaults(true));
			CONFIG = node.getValue(TypeToken.of(Config.class), new Config());
			config.save(node);
		} catch (IOException | ObjectMappingException e) {
			e.printStackTrace();
		}*/
		CommandRegistry.INSTANCE.register(false, dispatcher -> dispatcher.register(new ServerCommandBuilder("guild").executes(Guilds::help)
						.literal("create").argument("name", StringArgumentType.word()).argument("color", ColorArgumentType.color()).executes(Guilds::create).root()
						.literal("rename").argument("name", StringArgumentType.word()).executes(Guilds::rename).root()
						.literal("recolor").argument("color", ColorArgumentType.color()).executes(Guilds::recolor).root()
						.literal("delete").executes((context, feedback) -> Guilds.delete(context, feedback, false)).literal("confirmed").executes((context, feedback) -> Guilds.delete(context, feedback, true)).root()
						.literal("visibility")
						.literal("open").executes((context, feedback) -> Guilds.setVisibility(context, feedback, Guild.Visibility.OPEN)).up()
						.literal("ask").executes((context, feedback) -> Guilds.setVisibility(context, feedback, Guild.Visibility.ASK)).up()
						.literal("closed").executes((context, feedback) -> Guilds.setVisibility(context, feedback, Guild.Visibility.CLOSED)).up()
						.root()
						.defineArgument("player", StringArgumentType.word()).suggest(OfflineInfo.ONLINE_PROVIDER).definitionDone()
						.literal("invite").predefinedArgument("player").executes(Guilds::invite).root()
						.literal("promote").predefinedArgument("player").executes(Guilds::promote).root()
						.literal("invites").executes(Guilds::listInvites).root()
						.literal("members").executes(Guilds::listMembers).root()
						.literal("kick").predefinedArgument("player").executes(Guilds::kick).root()
						.defineArgument("invite", StringArgumentType.word()).suggest(INVITES_PROVIDER).definitionDone()
						.literal("accept_invite").predefinedArgument("invite").executes(Guilds::acceptInvite).root()
						.literal("deny_invite").predefinedArgument("invite").executes(Guilds::denyInvite).root()
						.literal("remove").predefinedArgument("player").root()
						.defineArgument("guild", StringArgumentType.word()).definitionDone()
						.literal("request").predefinedArgument("guild").executes(Guilds::request).root()
						.literal("requests").executes(Guilds::requests).root()
						.literal("accept_request").predefinedArgument("player").executes(Guilds::acceptRequest).root()
						.literal("deny_request").predefinedArgument("player").executes(Guilds::denyRequest).root()
						.literal("leave").executes((context, feedback) -> Guilds.leave(context, feedback, false)).literal("confirmed").executes((context, feedback) -> Guilds.leave(context, feedback, true)).root()
						.literal("chat").argument("message", StringArgumentType.greedyString()).executes(Guilds::chat).root()
						.literal("help").executes(Guilds::help).root()
						.build()));
	}
}
