package io.github.giantnuker.guilds;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.network.MessageType;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ASAPMessages {
	private static Map<UUID, List<Text>> messageMap = new HashMap<>();
	public static void message(UUID player, CommandContext<ServerCommandSource> context, Text message) {
		message(player, context.getSource().getMinecraftServer().getPlayerManager(), message);
	}
	public static void message(UUID player, PlayerManager manager, Text message) {
		ServerPlayerEntity pe = manager.getPlayer(player);
		if (pe == null) {
			messageMap.computeIfAbsent(player, id -> new ArrayList<>()).add(message);
		} else {
			pe.sendChatMessage(message, MessageType.SYSTEM);
		}
	}
	public static void sendMessages(ServerPlayerEntity playerEntity) {
		if (messageMap.containsKey(playerEntity.getGameProfile().getId())) {
			for (Text message: messageMap.get(playerEntity.getGameProfile().getId())) {
				playerEntity.sendChatMessage(message, MessageType.SYSTEM);
			}
			messageMap.remove(playerEntity.getGameProfile().getId());
		}
	}
}
