package M6FGR.cape_config.client.data;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class CapeData {
    private static final Path SAVE_LOCATION = Minecraft.getInstance().gameDirectory.toPath().resolve("config/cape_list.txt");
    public static ResourceLocation currentCape = null;
    public static void saveCape(String capeIdentifier) {
        try {
            Files.writeString(SAVE_LOCATION, capeIdentifier, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String loadCape() {
        if (Files.exists(SAVE_LOCATION)) {
            try {
                return Files.readString(SAVE_LOCATION, StandardCharsets.UTF_8).trim();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}