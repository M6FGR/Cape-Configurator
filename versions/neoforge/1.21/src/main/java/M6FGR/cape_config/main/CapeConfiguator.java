package M6FGR.cape_config.main;

import M6FGR.cape_config.client.data.CapeData;
import M6FGR.cape_config.client.gui.screen.CapeEditScreen;
import M6FGR.cape_config.client.gui.screen.WarningScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CapeConfiguator.MODID)
public class CapeConfiguator {

    public static final String MODID = "cape_config";
    public static final Logger LOGGER = LogManager.getLogger("CapeConfigurator");

    public CapeConfiguator(IEventBus modus, ModContainer container) {
        String savedUrl = CapeData.loadCape();
        container.registerExtensionPoint(IConfigScreenFactory.class, (ModContainer modContainer, Screen lastScreen) -> {
            if (Minecraft.getInstance().level == null || Minecraft.getInstance().player == null) {
                return new WarningScreen(lastScreen);
            }
            return new CapeEditScreen(null, lastScreen);
        });
        if (savedUrl != null && !savedUrl.isEmpty()) {
            CapeData.currentCape = CapeEditScreen.loadWebCape("Default", savedUrl, true, null);
        }

    }



}
