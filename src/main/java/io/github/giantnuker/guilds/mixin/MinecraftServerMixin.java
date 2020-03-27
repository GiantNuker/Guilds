package io.github.giantnuker.guilds.mixin;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import io.github.giantnuker.guilds.GuildManager;
import io.github.giantnuker.guilds.Guilds;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.world.level.LevelGeneratorOptions;
import net.minecraft.world.level.LevelGeneratorType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
	@Shadow
	private PlayerManager playerManager;
	@Shadow
	@Final
	private File gameDir;

	@Inject(method = "tick", at = @At("RETURN"))
	private void tickGuilds(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
		Guilds.GM.tick(playerManager);
	}

	@Inject(method = "loadWorld", at = @At("RETURN"))
	private void loadGuilds(String name, String serverName, long seed, LevelGeneratorOptions levelGeneratorOptions, CallbackInfo ci) {
		File file = new File(gameDir, "guilds.json");

		if (file.exists()) {
			Gson gson = new Gson();

			try {
				FileInputStream stream = new FileInputStream(file);
				Guilds.GM = gson.fromJson(new InputStreamReader(stream), GuildManager.class);
				stream.close();
				Guilds.GM.assignMembers();
			} catch (IOException e) {
				System.err.println("Failed to read guilds file");
				throw new RuntimeException(e);
			}
		}
	}

	@Inject(method = "save", at = @At("RETURN"))
	private void saveGuilds(boolean bl, boolean bl2, boolean bl3, CallbackInfoReturnable<Boolean> cir) {
		File file = new File(gameDir, "guilds.json");
		try {
			Gson gson = new Gson();
			FileWriter writer = new FileWriter(file, false);
			gson.toJson(Guilds.GM, writer);
			writer.close();
		} catch (IOException e) {
			System.err.println("Failed to save guilds file");
			e.printStackTrace();
		}
	}
}
