package io.github.giantnuker.guilds;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class Config {
	@Setting(value = "chatSpacer", comment = "The character between a players guild and name")
	public String guildChatDivider = " ";
	@ConfigSerializable
	public class GuildChat {
		@Setting(value = "prefix", comment = "Everything in guild chat gets prefixed with this")
		public String prefix = "§aG §7>§r ";
		@Setting(value = "suffix", comment = "Spacer between a guild chat name and message")
		public String suffix = " §7>>§r ";
	}
	@Setting(value = "guildchat", comment = "Settings for guild chat")
	public GuildChat guildChat = new GuildChat();
}
