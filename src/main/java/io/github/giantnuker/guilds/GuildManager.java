package io.github.giantnuker.guilds;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GuildManager {
	protected Map<String, Guild> guilds = new HashMap<>();
	protected Map<UUID, String> guildMembers = new HashMap<>();
	public void addGuild(Guild guild) {
		guilds.put(guild.getName(), guild);
		guildMembers.put(guild.getOwner(), guild.getName());
	}
	public void removeGuild(String name) {
		guilds.remove(name);
		Map<UUID, String> ngm = new HashMap<>(guildMembers);
		guildMembers.forEach((id, guild) -> { if (name.equals(guild)) ngm.remove(id); });
		guildMembers = ngm;
	}
	public boolean guildExists(String name) {
		return guilds.containsKey(name);
	}
	public void joinGuild(UUID player, String guild) {
		guildMembers.put(player, guild);
	}
	public void leaveGuild(UUID player) {
		guildMembers.remove(player);
	}
	public Guild getGuild(UUID player) {
		return guilds.get(guildMembers.get(player));
	}
}
