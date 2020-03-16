package io.github.giantnuker.guilds.mixin;

import com.mojang.authlib.GameProfile;
import io.github.giantnuker.guilds.Guild;
import io.github.giantnuker.guilds.Guilds;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
	private int ticksSinceGuildLevel = 0;

	@Shadow
	public abstract GameProfile getGameProfile();

	@Redirect(method = "getDisplayName", at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/Team;modifyText(Lnet/minecraft/scoreboard/AbstractTeam;Lnet/minecraft/text/Text;)Lnet/minecraft/text/Text;"))
	private Text modifyByGuild(AbstractTeam abstractTeam, Text text) {
		Guild g = Guilds.GM.getGuild(getGameProfile().getId());

		if (g != null) {
			return Team.modifyText(abstractTeam, new LiteralText("").append(new LiteralText(g.getName()).formatted(g.getColor())).append(new LiteralText(Guilds.CONFIG.guildChatDivider)).append(text));
		} else {
			return Team.modifyText(abstractTeam, text);
		}
	}

	@Inject(method = "tick", at = @At("RETURN"))
	public void doGuildUpdate(CallbackInfo ci) {
		Guild g = Guilds.GM.getGuild(getGameProfile().getId());
		if (g != null) {
			ticksSinceGuildLevel++;
			if (ticksSinceGuildLevel >= Guilds.CONFIG.leveling.xptime * 1200) {
				ticksSinceGuildLevel = 0;
				g.addXp(1, ((PlayerEntity)(Object)this).getServer().getPlayerManager());
			}
		}
	}
}
