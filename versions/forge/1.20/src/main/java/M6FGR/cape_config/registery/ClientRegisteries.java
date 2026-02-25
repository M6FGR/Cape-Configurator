package M6FGR.cape_config.registery;

import M6FGR.cape_config.client.gui.screen.CapeEditScreen;
import M6FGR.cape_config.client.gui.screen.WarningScreen;
import M6FGR.cape_config.main.CapeConfiguator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(
        modid = CapeConfiguator.MODID,
        bus = Bus.MOD,
        value = Dist.CLIENT
)
@SuppressWarnings("removal")
public class ClientRegisteries {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((mc, lastScreen) -> {
                    if (mc.level == null && mc.player == null) {
                        return new WarningScreen(lastScreen);
                    }
                    return new CapeEditScreen(lastScreen);
                })
        );
    }
}