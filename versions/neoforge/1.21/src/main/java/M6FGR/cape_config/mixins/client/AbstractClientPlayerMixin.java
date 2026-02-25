package M6FGR.cape_config.mixins.client;

import M6FGR.cape_config.client.data.CapeData;
import M6FGR.cape_config.main.CapeConfiguator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AbstractClientPlayer.class, remap = false, priority = 2000)
public class AbstractClientPlayerMixin {
    @Inject(method = "getSkin", at = @At("RETURN"), remap = false, cancellable = true)
    private void injectCustomCape(CallbackInfoReturnable<PlayerSkin> cir) {
        AbstractClientPlayer self = (AbstractClientPlayer) (Object) this;
        if (self.getUUID().equals(Minecraft.getInstance().getUser().getProfileId())) {
            if (CapeData.currentCape != null) {
                PlayerSkin original = cir.getReturnValue();
                cir.setReturnValue(new PlayerSkin(
                        original.texture(),
                        original.textureUrl(),
                        CapeData.currentCape,
                        original.elytraTexture(),
                        original.model(),
                        original.secure()
                ));
            }
        }
    }
}