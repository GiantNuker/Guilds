package io.github.giantnuker.guilds;

import net.minecraft.server.PlayerManager;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Guild {
	public List<UUID> members = new ArrayList<>();
	public List<UUID> requests = new ArrayList<>();
	protected Formatting color;
	protected String name;
	protected UUID owner;
	protected Visibility visibility = Visibility.ASK;
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

	public void join(UUID player) {
		if (!members.contains(player)) {
			members.add(player);
		}
	}

	public void leave(UUID player) {
		members.remove(player);
	}

	public Formatting getColor() {
		return color;
	}

	public void setColor(Formatting color) {
		this.color = color;
	}

	public UUID getOwner() {
		return owner;
	}

	public void setOwner(UUID owner) {
		this.owner = owner;
	}

	public void setVisibility(Visibility visibility, PlayerManager playerManager) {
		this.visibility = visibility;

		switch (visibility) {
		case OPEN:
			for (UUID request : requests) {
				ASAPMessages.message(request, playerManager, new LiteralText("Your request to join ").formatted(Formatting.GREEN).append(new LiteralText(getName()).formatted(getColor())).append(" was accepted"));
			}

			requests.clear();
			break;
		case CLOSED:
			for (UUID request : requests) {
				ASAPMessages.message(request, playerManager, new LiteralText("Your request to join ").formatted(Formatting.RED).append(new LiteralText(getName()).formatted(getColor())).append(" was denied"));
			}

			requests.clear();
			break;
		}
	}

	public enum Visibility {
		OPEN, ASK, CLOSED
	}
}
