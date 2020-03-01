package io.github.giantnuker.guilds;

import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Guild {
	protected Formatting color;
	protected String name;
	protected UUID owner;
	public List<UUID> members = new ArrayList<>();
	public List<UUID> requests = new ArrayList<>();
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
	public void join(UUID player) {
		if (!members.contains(player)) {
			members.add(player);
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
