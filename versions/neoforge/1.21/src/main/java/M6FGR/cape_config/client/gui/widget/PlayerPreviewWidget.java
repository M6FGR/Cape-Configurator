package M6FGR.cape_config.client.gui.widget;

import M6FGR.cape_config.client.data.CapeData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class PlayerPreviewWidget extends AbstractWidget {
    private final RemotePlayer previewPlayer;
    private float rotation = 0.0F;

    public PlayerPreviewWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.literal("Player Preview"));
        Minecraft mc = Minecraft.getInstance();
        this.previewPlayer = new RemotePlayer(mc.level, mc.getGameProfile()) {
            @Override
            public boolean shouldShowName() { return false; }

            @Override
            public Component getName() { return Component.empty(); }

            @Override
            public Component getDisplayName() {
               return Component.empty();
            }

            @Override
            public boolean isCustomNameVisible() {
                return false;
            }

            @Override
            public PlayerSkin getSkin() {
                PlayerSkin original = super.getSkin();
                if (CapeData.currentCape != null) {
                    return new PlayerSkin(
                            original.texture(),
                            original.textureUrl(),
                            CapeData.currentCape,
                            original.elytraTexture(),
                            original.model(),
                            original.secure()
                    );
                }
                return original;
            }
        };

        this.previewPlayer.xCloak = this.previewPlayer.getX();
        this.previewPlayer.yCloak = this.previewPlayer.getY();
        this.previewPlayer.zCloak = this.previewPlayer.getZ();
        this.previewPlayer.xCloakO = this.previewPlayer.getX();
        this.previewPlayer.yCloakO = this.previewPlayer.getY();
        this.previewPlayer.zCloakO = this.previewPlayer.getZ();

        this.previewPlayer.getEntityData().set(Player.DATA_PLAYER_MODE_CUSTOMISATION, (byte) 0xFF);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int centerX = this.getX() + (this.width / 2);
        int centerY = this.getY() + this.height - 30;
        int scale = this.height / 3;

        Quaternionf rotationQuaternion = new Quaternionf()
                .rotationXYZ(0.2F, (float) Math.toRadians(rotation), (float) Math.PI);

        InventoryScreen.renderEntityInInventory(
                guiGraphics,
                (float) centerX,
                (float) centerY,
                (float) scale,
                new Vector3f(0, 0, 0),
                rotationQuaternion,
                null,
                this.previewPlayer
        );
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        this.rotation -= (float) dragX * 1.5F;
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {}

    public void tick() {
        if (this.previewPlayer != null) {
            this.previewPlayer.xCloakO = this.previewPlayer.xCloak;
            this.previewPlayer.yCloakO = this.previewPlayer.yCloak;
            this.previewPlayer.zCloakO = this.previewPlayer.zCloak;
            this.previewPlayer.xCloak += (this.previewPlayer.getX() - this.previewPlayer.xCloak) * 0.1;
            this.previewPlayer.yCloak += (this.previewPlayer.getY() - this.previewPlayer.yCloak) * 0.1;
            this.previewPlayer.zCloak += (this.previewPlayer.getZ() - this.previewPlayer.zCloak) * 0.1;
        }
    }
}