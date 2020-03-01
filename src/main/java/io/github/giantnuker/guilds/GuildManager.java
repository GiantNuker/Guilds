package io.github.giantnuker.guilds;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.MessageType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GuildManager {
	protected Map<String, Guild> guilds = new HashMap<>();
	protected Map<UUID, List<String>> pendingInvites = new HashMap<>();
	protected Map<UUID, String> guildMembers = new HashMap<>();
	public void addGuild(Guild guild) {
		guilds.put(guild.getName(), guild);
		guildMembers.put(guild.getOwner(), guild.getName());
	}
	public void removeGuild(String name) {
		guilds.remove(name);
		transferMembers(name, null);

	}
	public void renameGuild(String name, String to) {
		guilds.get(name).setName(to);
		guilds.put(to, guilds.get(name));
		guilds.remove(name);
		transferMembers(name, to);
	}
	public void transferMembers(String from, String to) {
		Map<UUID, String> ngm = new HashMap<>(guildMembers);
		guildMembers.forEach((id, guild) -> { if (guild.equals(from)) {if (to == null) ngm.remove(id); else ngm.put(id, to); } });
		guildMembers = ngm;

		pendingInvites.forEach((id, list) -> { list.forEach(guild -> { if (guild.equals(from)) {list.remove(id); if (to != null) { list.add(to); }}});});
	}
	public boolean guildExists(String name) {
		return guilds.containsKey(name);
	}
	public void joinGuild(UUID player, String guild) {
		guilds.get(guild).join(player);
		guildMembers.put(player, guild);
	}
	public void leaveGuild(UUID player) {
		if (guildMembers.get(player) != null) {
			guilds.get(guildMembers.get(player)).leave(player);
		}
		guildMembers.remove(player);
	}
	public Guild getGuild(UUID player) {
		return guilds.get(guildMembers.get(player));
	}
	public void invite(UUID player, String guild) {
		pendingInvites.computeIfAbsent(player, k -> new ArrayList<>());
		List<String> invites = pendingInvites.get(player);
		if (!invites.contains(guild)) {
			invites.add(guild);
		}
	}
	public boolean acceptInvite(UUID player, String guild) {
		if (pendingInvites.get(player) != null) {
			List<String> invites = pendingInvites.get(player);
			if (invites.contains(guild)) {
				invites.remove(guild);
				leaveGuild(player);
				joinGuild(player, guild);
				return true;
			}
		}
		return false;
	}
	public boolean denyInvite(UUID player, String guild) {
		if (pendingInvites.get(player) != null) {
			List<String> invites = pendingInvites.get(player);
			if (invites.contains(guild)) {
				invites.remove(guild);
				return true;
			}
		}
		return false;
	}
	public void sendInviteMessage(ServerPlayerEntity player, String guild) {
		player.sendChatMessage(new LiteralText("You have been invited to join the guild ").formatted(Formatting.YELLOW).append(new LiteralText(guild).formatted(guilds.get(guild).getColor())).append(new LiteralText(" [JOIN]").setStyle(new Style().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/guild accept_invite " + guild)).setColor(Formatting.GREEN))).append(new LiteralText(" [DENY]").setStyle(new Style().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/guild deny_invite " + guild)).setColor(Formatting.DARK_RED))), MessageType.SYSTEM);
	}
}
