package io.github.giantnuker.guilds;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConfigSerializable
public class Config {
	@Setting(value = "chatSpacer", comment = "The character between a players guild and name")
	public String guildChatDivider = " ";
	@Setting(value = "guildchat", comment = "Settings for guild chat")
	public GuildChat guildChat = new GuildChat();
	@Setting(value = "leveling")
	public Leveling leveling = new Leveling();

	@ConfigSerializable
	public static class GuildChat {
		@Setting(value = "prefix", comment = "Everything in guild chat gets prefixed with this")
		public String prefix = "§aG §7>§r ";
		@Setting(value = "suffix", comment = "Spacer between a guild chat name and message")
		public String suffix = " §7>>§r ";
	}

	@ConfigSerializable
	public static class Leveling {
		@Setting(value = "details")
		public List<LevelDetails> details = Arrays.asList(new LevelDetails(0, 3, false), new LevelDetails(1, 5, false), new LevelDetails(2, 7, true));
		@Setting(value = "xptime", comment = "Minutes per XP per player")
		public int xptime = 1;
		@Setting(value = "xpmode", comment = "MULTIPLY: multiplies last level's XP by xpincrease\nADD: adds xpincrease to last level's XP")
		public XpMode mode = XpMode.MULTIPLY;
		@Setting(value = "xpincrease", comment = "See xpmode")
		public double increase = 2;
		@Setting(value = "basexp", comment = "Amout of XP required to get to level 1")
		public int base = 60;
		@Setting(value = "boosting")
		public Boosting boosting = new Boosting();

		public int xpForLevel(int level) {
			return (int) Math.ceil(base + ((level - 1) * (mode == XpMode.MULTIPLY ? base * increase : increase)));
		}

		public LevelDetails lastDetails(int level) {
			int best = -1;
			LevelDetails bestD = null;

			for (LevelDetails detail : details) {
				if (detail.level <= level && detail.level > best) {
					best = detail.level;
					bestD = detail;
				}
			}

			return bestD;
		}

		public boolean canChat(int level) {
			LevelDetails details = lastDetails(level);
			return details == null || details.guildChat;
		}

		public int maxMembers(int level) {
			LevelDetails details = lastDetails(level);
			return details == null ? -1 : details.maxMembers;
		}

		public enum XpMode {
			MULTIPLY, ADD
		}

		@ConfigSerializable
		public static class Boosting {
			@Setting(value = "boostMultiplier")
			public int boostMultiplier = 2;
			@Setting(value = "scoreboardExchange", comment = "Exchange of 1 scoreboard value to X boost minutes")
			public Map<String, Integer> exchange = new HashMap<>();
		}

		@ConfigSerializable
		public static class LevelDetails {
			@Setting(value = "level")
			public int level = -1;
			@Setting(value = "guildchat")
			public boolean guildChat = false;
			@Setting(value = "members")
			public int maxMembers = -1;

			public LevelDetails(int level, int members, boolean guildChat) {
				this.maxMembers = members;
				this.guildChat = guildChat;
				this.level = level;
			}

			public LevelDetails() {
			}
		}
	}
}
