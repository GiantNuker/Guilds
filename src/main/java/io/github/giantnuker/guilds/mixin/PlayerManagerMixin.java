package io.github.giantnuker.guilds.mixin;

import io.github.giantnuker.guilds.ASAPMessages;
import io.github.giantnuker.guilds.Guild;
import io.github.giantnuker.guilds.Guilds;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
	@Inject(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;sendCommandTree(Lnet/minecraft/server/network/ServerPlayerEntity;)V"))
	private void sendASAPMessages(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
		ASAPMessages.sendMessages(player);
		UUID id = player.getGameProfile().getId();
		Guild g = Guilds.GM.getGuild(id);
		if (g != null && g.getOwner().equals(id)) {
			player.sendMessage(new LiteralText(g.members.size() + " player(s) want to join your guild ").formatted(Formatting.YELLOW).append(new LiteralText("[VIEW]").setStyle(new Style().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/guild requests")).setColor(Formatting.GREEN))));
		}
	}
}
