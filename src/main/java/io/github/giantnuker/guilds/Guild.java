package io.github.giantnuker.guilds;

import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Guild {
	public class Rank {
		protected Formatting color;
		protected String name;
		public Rank(String name, Formatting color) {
			this.color = color;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public Formatting getColor() {
			return color;
		}
		public void setColor(Formatting color) {
			this.color = color;
		}
	}
	protected Formatting color;
	protected String name;
	protected UUID owner;
	protected Map<String, Rank> ranks = new HashMap<>();
	protected Map<UUID, Rank> members = new HashMap<>();
	public Guild(String name, Formatting color, UUID owner) {
		this.name = name;
		this.color = color;
		this.owner = owner;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
		if (!members.containsKey(player)) {
			members.put(player, null);
		}
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
	public void setOwner(UUID owner) {
		this.owner = owner;
	}
	public UUID getOwner() {
		return owner;
	}
}
