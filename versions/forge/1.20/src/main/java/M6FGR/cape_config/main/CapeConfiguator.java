package M6FGR.cape_config.main;

import M6FGR.cape_config.client.data.CapeData;
import M6FGR.cape_config.client.event.CapeEventHandler;
import M6FGR.cape_config.client.gui.screen.CapeEditScreen;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("removal")
@Mod(CapeConfiguator.MODID)
public class CapeConfiguator {

    public static final String MODID = "cape_config";
    private static final Logger LOGGER = LogManager.getLogger("CapeConfigurator");

    public CapeConfiguator() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);
        String savedUrl = CapeData.loadCape();

        if (savedUrl != null && !savedUrl.isEmpty()) {
            // We pass 'null' for the CapeEditScreen parameter because
            // the UI isn't open yet during startup.
            CapeEventHandler.currentCape = CapeEditScreen.loadWebCape("Default", savedUrl, true, null);
        }

    }



}
