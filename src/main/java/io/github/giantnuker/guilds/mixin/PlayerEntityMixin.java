package io.github.giantnuker.guilds.mixin;

import com.mojang.authlib.GameProfile;
import io.github.giantnuker.guilds.Guild;
import io.github.giantnuker.guilds.Guilds;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
	@Shadow public abstract GameProfile getGameProfile();

	@Shadow @Final public PlayerAbilities abilities;

	@Redirect(method = "getDisplayName", at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/Team;modifyText(Lnet/minecraft/scoreboard/AbstractTeam;Lnet/minecraft/text/Text;)Lnet/minecraft/text/Text;"))
	private Text modifyByGuild(AbstractTeam abstractTeam, Text text) {
		Guild g = Guilds.GM.getGuild(getGameProfile().getId());
		if (g != null) {
			return Team.modifyText(abstractTeam, new LiteralText("").append(new LiteralText(g.getName()).formatted(g.getColor())).append(new LiteralText(Guilds.CONFIG.guildChatDivider)).append(text));
		} else {
			return Team.modifyText(abstractTeam, text);
		}
	}
}
