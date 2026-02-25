package M6FGR.cape_config.mixins.client;

import M6FGR.cape_config.client.data.CapeData;
import M6FGR.cape_config.main.CapeConfiguator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.mojang.authlib.GameProfile;

@Mixin(value = PlayerInfo.class, remap = false, priority = 1500)
public abstract class PlayerInfoMixin {
    @Shadow public abstract GameProfile getProfile();

    @Inject(method = "getSkin", at = @At("RETURN"), cancellable = true, remap = false)
    private void injectServerCape(CallbackInfoReturnable<PlayerSkin> cir) {
        PlayerSkin originalSkin = cir.getReturnValue();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && this.getProfile().getId().equals(mc.player.getUUID())) {
            if (CapeData.currentCape != null) {
                PlayerSkin customSkin = new PlayerSkin(
                        originalSkin.texture(),
                        originalSkin.textureUrl(),
                        CapeData.currentCape,
                        originalSkin.elytraTexture(),
                        originalSkin.model(),
                        originalSkin.secure()
                );

                cir.setReturnValue(customSkin);
            }
        }
    }
}