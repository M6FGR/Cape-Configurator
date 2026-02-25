package M6FGR.cape_config.client.gui.screen;

import M6FGR.cape_config.client.data.CapeData;
import M6FGR.cape_config.client.event.CapeEventHandler;
import M6FGR.cape_config.client.gui.widget.PlayerPreviewWidget;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CapeEditScreen extends Screen {
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
    private int tickCounter = 0;
    private boolean isSearching = false;
    private PlayerPreviewWidget playerPreview;
    private final List<WebCape> gallery = new ArrayList<>();
    private final Screen lastScreen;
    private EditBox searchBar;
    private static String statusMessage = "";

    private static class WebCape {
        private final List<String> names;
        private final String url;
        private final ResourceLocation loc;

        public WebCape(String firstName, String url, ResourceLocation loc) {
            this.names = new ArrayList<>();
            this.names.add(firstName);
            this.url = url;
            this.loc = loc;
        }

        public List<String> names() { return names; }
        public String url() { return url; }
        public ResourceLocation loc() { return loc; }
        public String getJoinedNames() { return String.join(", ", names); }
    }

    public CapeEditScreen(Screen lastScreen) {
        super(Component.literal("Cape Configurator"));
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        statusMessage = "";
        int wallSize = 500;
        this.playerPreview = new PlayerPreviewWidget(this.width / 2 - (wallSize / 2), this.height / 2 - (wallSize / 2), wallSize, wallSize);

        this.searchBar = new EditBox(this.font, 20, 45, 200, 20, Component.literal("Search..."));
        this.searchBar.setMaxLength(128);
        this.addRenderableWidget(searchBar);
        this.addRenderableWidget(playerPreview);

        this.addRenderableWidget(Button.builder(Component.literal("Find"), (btn) -> {
            this.triggerSearch(this.searchBar.getValue());
        }).bounds(225, 45, 40, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Clear"), (btn) -> {
            statusMessage = "§7Cleared all capes.";
            CapeEventHandler.currentCape = null;
            this.gallery.clear();
        }).bounds(20, this.height - 65, 200, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (btn) -> {
            if (CapeEventHandler.currentCape != null) {
                gallery.stream().filter(c -> c.loc().equals(CapeEventHandler.currentCape)).findFirst().ifPresent(cape -> {
                    CapeData.saveCape(cape.url());
                    statusMessage = "§bConfig Saved!";
                });
            }
            this.onClose();
        }).bounds(20, this.height - 40, 200, 20).build());
    }

    private void triggerSearch(String query) {
        String inputRaw = query.trim();
        if (inputRaw.isEmpty()) return;

        isSearching = true;
        tickCounter = 0;

        if (inputRaw.length() <= 1) {
            statusMessage = "§7 Enter a valid username.";
            return;
        }

        statusMessage = "§eSearching for: " + inputRaw + "...";

        Util.ioPool().execute(() -> {
            try {
                if (inputRaw.length() >= 64 && inputRaw.matches("^[a-fA-F0-9]+$")) {
                    String hashUrl = "http://textures.minecraft.net/texture/" + inputRaw;
                    loadWebCape("Custom Hash", hashUrl, false, CapeEditScreen.this);
                    return;
                }

                String cleaned = inputRaw.replaceAll("-", "");

                if (cleaned.length() == 32 && cleaned.matches("^[a-fA-F0-9]+$")) {
                    Minecraft.getInstance().execute(() -> statusMessage = "§e[1/6] §7Direct UUID lookup...");
                    lookupSession(cleaned, "UUID: " + cleaned.substring(0, 8));
                    return;
                }

                Minecraft.getInstance().execute(() -> statusMessage = "§e[1/6] §7Resolving username...");

                URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + cleaned.replaceAll("[^a-zA-Z0-9_]", ""));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", USER_AGENT);
                conn.setConnectTimeout(5000);

                int responseCode = conn.getResponseCode();
                if (responseCode != 200) {
                    Minecraft.getInstance().execute(() -> statusMessage = "§cPlayer not found!");
                    return;
                }

                String res = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                Matcher nameMatcher = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"").matcher(res);
                final String officialName = nameMatcher.find() ? nameMatcher.group(1) : inputRaw;

                Matcher uuidMatcher = Pattern.compile("\"id\"\\s*:\\s*\"([^\"]+)\"").matcher(res);
                if (uuidMatcher.find()) {
                    lookupSession(uuidMatcher.group(1), officialName);
                }

            } catch (Exception e) {
                Minecraft.getInstance().execute(() -> statusMessage = "§c[!] Connection Error.");
                e.printStackTrace();
            }
        });
    }

    private void lookupSession(String uuid, String displayName) {
        try {
            Minecraft.getInstance().execute(() -> statusMessage = "§e[3/6] §7Fetching textures...");
            URL sessionUrl = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid);
            HttpURLConnection sConn = (HttpURLConnection) sessionUrl.openConnection();
            sConn.setRequestProperty("User-Agent", USER_AGENT);
            if (sConn.getResponseCode() == 429) { Minecraft.getInstance().execute(() -> statusMessage = "§cRate limited! Wait..."); return; }

            String sRes = new String(sConn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            Matcher valueMatcher = Pattern.compile("\"value\"\\s*:\\s*\"([^\"]+)\"").matcher(sRes);
            if (valueMatcher.find()) {
                String decoded = new String(Base64.getDecoder().decode(valueMatcher.group(1)));
                Matcher capeMatcher = Pattern.compile("\"CAPE\"\\s*:\\s*\\{[^}]*\"url\"\\s*:\\s*\"([^\"]+)\"").matcher(decoded);

                if (capeMatcher.find()) {
                    loadWebCape(displayName, capeMatcher.group(1), false, this);
                } else {
                    String ofUrl = "http://s.optifine.net/capes/" + displayName.replace("UUID: ", "") + ".png";
                    loadWebCape(displayName, ofUrl, false, this);
                }
            }
        } catch (Exception e) { Minecraft.getInstance().execute(() -> statusMessage = "§Can't load texture: " + e.getMessage()); }
    }

    public static ResourceLocation loadWebCape(String name, String url, boolean isSilent, @Nullable CapeEditScreen screen) {
        if (url == null || url.isEmpty()) return null;
        ResourceLocation loc = ResourceLocation.fromNamespaceAndPath("cape_config", "dynamic/capes/cape_" + Math.abs(url.hashCode()));

        Util.ioPool().execute(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestProperty("User-Agent", USER_AGENT);
                conn.setConnectTimeout(2000);
                if (conn.getResponseCode() != 200) {
                    if (!isSilent) statusMessage = "§cPlayer has no capes.";
                    return;
                }

                if (conn.getConnectTimeout() >= 2000) {
                    statusMessage = "§cServer is not responding!";
                }

                try (InputStream is = conn.getInputStream()) {
                    NativeImage original = NativeImage.read(is);
                    NativeImage fixed = new NativeImage(64, 32, true);
                    for (int x = 0; x < original.getWidth(); x++) {
                        for (int y = 0; y < original.getHeight(); y++) {
                            if (x < 64 && y < 32) fixed.setPixelRGBA(x, y, original.getPixelRGBA(x, y));
                        }
                    }

                    Minecraft.getInstance().execute(() -> {
                        Minecraft.getInstance().getTextureManager().register(loc, new DynamicTexture(fixed));
                        if (!isSilent) {
                            statusMessage = "§aLoaded cape from " + name + "!";
                            if (screen != null) screen.fetchToGallery(name, url, loc);
                        }
                    });
                    original.close();
                }
            } catch (Exception e) { if (!isSilent) statusMessage = "§cInvalid Texture."; }
        });
        return loc;
    }

    private void fetchToGallery(String name, String url, ResourceLocation loc) {
        Optional<WebCape> existing = this.gallery.stream()
                .filter(c -> c.url().equals(url))
                .findFirst();

        if (existing.isPresent()) {
            WebCape cape = existing.get();

            if (!cape.names().contains(name)) {
                cape.names().add(name);
                Minecraft.getInstance().getSoundManager().play(
                        SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.5F, 1.0F)
                );
                statusMessage = "§a" + name + "has the same cape!, joined.";
            } else {
                Minecraft.getInstance().getSoundManager().play(
                        SimpleSoundInstance.forUI(SoundEvents.VILLAGER_NO, 1.0F, 1.0F)
                );
                statusMessage = "§f" + name + " §7is already in the list.";
            }
            return;
        }
        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.5F, 1.0F)
        );
        this.gallery.add(new WebCape(name, url, loc));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, "§8Note: Drag/Drop PNGs to import locally", this.width / 2, this.height - 20, 0xFFFFFF);
        if (!statusMessage.isEmpty()) guiGraphics.drawString(this.font, statusMessage, 20, 72, 0xFFAAAAAA);

        if (gallery.isEmpty()) {
            int guideY = 100;
            guiGraphics.drawString(this.font, "§6How to apply Capes:", 20, guideY, 0xFFFFFF);
            guiGraphics.drawString(this.font, "§71. Type a §fUsername, UUID or a Hash", 20, guideY + 15, 0xFFFFFF);
            guiGraphics.drawString(this.font, "§72. Drag a §f.PNG §7file here (Local-capes)", 20, guideY + 30, 0xFFFFFF);
            guiGraphics.drawString(this.font, "§73. Click a list item to §aApply", 20, guideY + 45, 0xFFFFFF);
        } else {
            int currentY = 100;
            for (WebCape cape : gallery) {
                String label = "• " + cape.getJoinedNames();
                int textWidth = this.font.width(label);
                boolean isHovered = mouseX >= 20 && mouseX <= 20 + textWidth && mouseY >= currentY && mouseY <= currentY + 12;
                guiGraphics.drawString(this.font, label, 20, currentY, isHovered ? 0xFFFFFF00 : 0xFFFFFFFF);
                if (isHovered && Minecraft.getInstance().mouseHandler.isLeftPressed()) {
                    CapeEventHandler.currentCape = cape.loc();
                }
                currentY += 15;
            }
        }
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private ResourceLocation loadLocalCape(String fileUrl) {
        try {
            Path path = Paths.get(new URL(fileUrl).toURI());
            try (InputStream is = Files.newInputStream(path)) {
                NativeImage image = NativeImage.read(is);
                ResourceLocation loc = ResourceLocation.fromNamespaceAndPath("cape_config", "local_" + System.currentTimeMillis());
                this.minecraft.getTextureManager().register(loc, new DynamicTexture(image));
                return loc;
            }
        } catch (Exception e) { return null; }
    }

    @Override
    public void onFilesDrop(List<Path> paths) {
        for (Path path : paths) {
            if (path.getFileName().toString().toLowerCase().endsWith(".png")) {
                try {
                    ResourceLocation loc = loadLocalCape(path.toUri().toURL().toString());
                    if (loc != null) fetchToGallery("Local: " + path.getFileName(), path.toUri().toURL().toString(), loc);
                } catch (Exception e) { statusMessage = "§cImport failed: " + e.getMessage(); }
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (mouseX >= 225 && mouseX <= 265 && mouseY >= 45 && mouseY <= 65) {
                this.triggerSearch(this.searchBar.getValue());
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }

            int currentY = 100;
            for (WebCape cape : gallery) {
                int textWidth = this.font.width("• " + cape.getJoinedNames());
                if (mouseX >= 20 && mouseX <= 20 + textWidth && mouseY >= currentY && mouseY <= currentY + 12) {
                    if (CapeEventHandler.currentCape != null && CapeEventHandler.currentCape.equals(cape.loc())) {
                        CapeEventHandler.currentCape = null;
                        statusMessage = "§fRemoved cape.";
                    } else {
                        CapeEventHandler.currentCape = cape.loc();
                        statusMessage = "§aApplied cape!";
                    }
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    return true;
                }
                currentY += 15;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 257 || keyCode == 335) {
            this.triggerSearch(this.searchBar.getValue());
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void tick() {
        if (this.searchBar != null) this.searchBar.tick();
        this.playerPreview.tick();
    }
    @Override public void onClose() { this.minecraft.setScreen(lastScreen); }
}