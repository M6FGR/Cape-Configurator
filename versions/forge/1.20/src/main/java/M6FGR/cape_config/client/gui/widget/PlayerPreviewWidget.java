package M6FGR.cape_config.client.gui.widget;

import M6FGR.cape_config.client.event.CapeEventHandler;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.joml.Quaternionf;

public class PlayerPreviewWidget extends AbstractWidget {
    private final RemotePlayer previewPlayer;
    private float rotation = 0.0F; // Current rotation value
    private ResourceLocation texture;

    public PlayerPreviewWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.literal("Player Preview"));
        this.texture = Minecraft.getInstance().player.getSkinTextureLocation();
        Minecraft mc = Minecraft.getInstance();
        this.previewPlayer = new RemotePlayer(mc.level, mc.getUser().getGameProfile()) {
            @Override
            public boolean shouldShowName() { return false; }

            @Override
            public boolean isCustomNameVisible() { return false; }

            @Override
            public Component getName() {
                return Component.empty();
            }

            @Override
            public Component getDisplayName() {
                return Component.empty();
            }

            @Override
            public boolean hasCustomName() {
                return false;
            }

            @Override
            public ResourceLocation getCloakTextureLocation() {
                return CapeEventHandler.currentCape;
            }

            @Override
            public boolean isCapeLoaded() {
                return CapeEventHandler.currentCape != null;
            }

            @Override
            public ResourceLocation getSkinTextureLocation() {
                if (Minecraft.getInstance().player != null) {
                    return Minecraft.getInstance().player.getSkinTextureLocation();
                }
                return super.getSkinTextureLocation();
            }

            @Override
            public String getModelName() {
                if (Minecraft.getInstance().player != null) {
                    return Minecraft.getInstance().player.getModelName();
                }
                return super.getModelName();
            }
        };

        try {
            this.previewPlayer.getEntityData().set(Player.DATA_PLAYER_MODE_CUSTOMISATION, (byte) 0xFF);
        } catch (Exception ignored) {}

        previewPlayer.setCustomNameVisible(false);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.clear(256, Minecraft.ON_OSX);

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();

        int centerX = this.getX() + (this.width / 2);
        int centerY = this.getY() + this.height - 50;
        int scale = this.height / 3;

        Lighting.setupForEntityInInventory();
        poseStack.translate(0, 0, 50.0);

        InventoryScreen.renderEntityInInventory(
                guiGraphics,
                centerX,
                centerY,
                scale,
                new Quaternionf().rotationXYZ(0.4F, (float) Math.toRadians(rotation), (float) Math.PI),
                null,
                this.previewPlayer
        );

        poseStack.popPose();
    }


    public void tick() {
        if (this.previewPlayer != null) {
            this.previewPlayer.xCloakO = this.previewPlayer.xCloak;
            this.previewPlayer.yCloakO = this.previewPlayer.yCloak;
            this.previewPlayer.zCloakO = this.previewPlayer.zCloak;

            double d0 = this.previewPlayer.getX() - this.previewPlayer.xCloak;
            double d1 = this.previewPlayer.getY() - this.previewPlayer.yCloak;
            double d2 = this.previewPlayer.getZ() - this.previewPlayer.zCloak;

            this.previewPlayer.yCloak += d1;
            this.previewPlayer.xCloak += d0;
            this.previewPlayer.zCloak += d2;
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        this.rotation -= (float) dragX * 0.8F;
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {}

    public void setWidgetTexture(ResourceLocation texture) {
        this.texture = texture;
    }
}