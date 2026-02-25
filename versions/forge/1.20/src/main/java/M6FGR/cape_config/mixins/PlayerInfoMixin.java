package M6FGR.cape_config.mixins;

import M6FGR.cape_config.client.event.CapeEventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInfo.class)
public class PlayerInfoMixin {
    @Inject(method = "getCapeLocation", at = @At("HEAD"), cancellable = true)
    private void injectServerCape(CallbackInfoReturnable<ResourceLocation> cir) {
        PlayerInfo info = (PlayerInfo) (Object) this;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && info.getProfile().getId().equals(mc.player.getUUID())) {
            if (CapeEventHandler.currentCape != null) {
                cir.setReturnValue(CapeEventHandler.currentCape);
            }
        }
    }
}
