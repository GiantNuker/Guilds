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
		guildMembers.forEach((id, guild) -> { if (guild.equals(from)) {if (to.equals(null)) ngm.remove(id); else ngm.put(id, to); } });
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
