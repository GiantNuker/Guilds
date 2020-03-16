package io.github.giantnuker.guilds.mixin;

import io.github.giantnuker.guilds.Guilds;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
	@Shadow private PlayerManager playerManager;

	@Inject(method = "tick", at = @At("RETURN"))
	private void tickGuilds(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
		Guilds.GM.tick(playerManager);
	}
}
