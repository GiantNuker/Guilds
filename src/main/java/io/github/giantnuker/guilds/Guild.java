package io.github.giantnuker.guilds;

import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Guild {
	public class Rank {
		protected Formatting color;
		protected int priority;
		public Rank(int priority, Formatting color) {
			this.priority = priority;
			this.color = color;
		}
		public Formatting getColor() {
			return color;
		}
		public void setColor(Formatting color) {
			this.color = color;
		}
		public int getPriority() {
			return priority;
		}
		public void setPriority(int priority) {
			this.priority = priority;
		}
	}
	protected Formatting color = Formatting.RESET;
	protected String name;
	protected Map<String, Rank> ranks = new HashMap<>();
	protected Map<UUID, Rank> members = new HashMap<>();
	public Guild(String name, Formatting color) {
		this.name = name;
		this.color = color;
	}
	public String getName() {
		return name;
	}
	public void rename(String name) {
		this.name = name;
	}
	public Rank getRank(String name) {
		return ranks.get(name);
	}
	public void removeRank(String name) {
		ranks.remove(name);
	}
	public Rank getRank(UUID player) {
		return members.get(player);
	}
	public void setRank(UUID player, Rank rank) {
		members.put(player, rank);
	}
	public void setRank(UUID player, String rank) {
		members.put(player, getRank(rank));
	}
	public void join(UUID player) {
		members.put(player, null);
	}
	public void leave(UUID player) {
		members.remove(player);
	}
	public void setColor(Formatting color) {
		this.color = color;
	}
	public Formatting getColor() {
		return color;
	}
}
