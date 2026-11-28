package com.alpharip.staffmod.store.cosmetics;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.Locale;

/**
 * Resolves optional cosmetic textures from the AlphaClient resource pack.
 * Missing textures are normal while the cosmetic artwork is still being created.
 */
public final class CosmeticAssetManager {
    private static final String DOMAIN = "alphastaffmod";
    private static final String TEXTURE_ROOT = "textures/cosmetics/";

    private CosmeticAssetManager() { }

    public static ResourceLocation getTextureLocation(CosmeticEntry entry) {
        if (entry == null || !supportsTexturePreview(entry.getCategory())) return null;
        String id = entry.getId();
        if (!isSafeId(id)) return null;

        String category = entry.getCategory().name().toLowerCase(Locale.ROOT);
        return new ResourceLocation(DOMAIN, TEXTURE_ROOT + category + "/" + id + ".png");
    }

    public static boolean supportsTexturePreview(CosmeticCategory category) {
        return category == CosmeticCategory.CAPES
                || category == CosmeticCategory.TRAILS
                || category == CosmeticCategory.EFFECTS
                || category == CosmeticCategory.UI;
    }

    public static String getExpectedAssetPath(CosmeticEntry entry) {
        ResourceLocation location = getTextureLocation(entry);
        return location == null ? "" : "assets/" + location.getResourceDomain()
                + "/" + location.getResourcePath();
    }

    public static boolean isTextureAvailable(Minecraft minecraft, CosmeticEntry entry) {
        if (minecraft == null) return false;
        ResourceLocation location = getTextureLocation(entry);
        if (location == null) return false;
        try {
            minecraft.getResourceManager().getResource(location);
            return true;
        } catch (IOException missing) {
            return false;
        }
    }

    private static boolean isSafeId(String id) {
        if (id == null || id.isEmpty() || id.length() > 80) return false;
        for (int i = 0; i < id.length(); i++) {
            char c = id.charAt(i);
            if (!((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '-'))
                return false;
        }
        return id.charAt(0) != '-' && id.charAt(id.length() - 1) != '-';
    }
}
