package M6FGR.cape_config.client.gui.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class WarningScreen extends Screen {
    private final Screen lastScreen;

    public WarningScreen(Screen lastScreen) {
        super(Component.literal("Access Denied"));
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (btn) -> this.onClose())
                .bounds(this.width / 2 - 100, this.height / 2 + 40, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        // Draw the warning text
        guiGraphics.drawCenteredString(this.font, "§c§lCANNOT CONFIGURE CAPES", this.width / 2, this.height / 2 - 30, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font, "§7You must be inside a world or server to change your cape.", this.width / 2, this.height / 2 - 10, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font, "§8(This is required to render your player preview correctly)", this.width / 2, this.height / 2 + 5, 0xFFFFFF);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(lastScreen);
    }
}