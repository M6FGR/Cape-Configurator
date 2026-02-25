package M6FGR.cape_config.mixins;

import M6FGR.cape_config.client.event.CapeEventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerMixin {

    @Inject(method = "getCloakTextureLocation", at = @At("HEAD"), cancellable = true)
    private void onlyShowMyCape(CallbackInfoReturnable<ResourceLocation> cir) {
        AbstractClientPlayer self = (AbstractClientPlayer) (Object) this;
        if (self.getUUID().equals(Minecraft.getInstance().getUser().getProfileId())) {
            if (CapeEventHandler.currentCape != null) {
                cir.setReturnValue(CapeEventHandler.currentCape);
            }
        }
    }

    @Inject(method = "isCapeLoaded", at = @At("HEAD"), cancellable = true)
    private void injectIsCapeLoaded(CallbackInfoReturnable<Boolean> cir) {
        AbstractClientPlayer self = (AbstractClientPlayer) (Object) this;
        if (self.getUUID().equals(Minecraft.getInstance().getUser().getProfileId())) {
            if (CapeEventHandler.currentCape != null) {
                cir.setReturnValue(true);
            }

        }
    }
}