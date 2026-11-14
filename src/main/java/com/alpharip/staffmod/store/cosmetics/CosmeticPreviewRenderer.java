package com.alpharip.staffmod.store.cosmetics;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;

/**
 * Displays a cosmetic's real texture when its PNG has been added.
 * It deliberately renders a missing-asset state instead of inventing artwork.
 */
public final class CosmeticPreviewRenderer {
    private CosmeticPreviewRenderer() { }

    public static boolean draw(Minecraft minecraft, CosmeticEntry entry,
                               int x, int y, int size) {
        if (minecraft == null || entry == null || size < 16) return false;

        if (!CosmeticAssetManager.supportsTexturePreview(entry.getCategory())) {
            drawPendingPreview(minecraft, x, y, size);
            return false;
        }
        ResourceLocation texture = CosmeticAssetManager.getTextureLocation(entry);
        if (texture == null || !CosmeticAssetManager.isTextureAvailable(minecraft, entry)) {
            drawMissingAsset(minecraft, x, y, size);
            return false;
        }

        Gui.drawRect(x - 2, y - 2, x + size + 2, y + size + 2, 0xFF101010);
        minecraft.getTextureManager().bindTexture(texture);
        boolean cape = entry.getCategory() == CosmeticCategory.CAPES;
        int previewHeight = cape ? Math.max(16, size / 2) : size;
        float textureHeight = cape ? 32.0F : 64.0F;
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 0.0F,
                size, previewHeight, 64.0F, textureHeight);
        return true;
    }

    private static void drawMissingAsset(Minecraft minecraft, CosmeticEntry entry,
                                         int x, int y, int size) {
        Gui.drawRect(x - 2, y - 2, x + size + 2, y + size + 2, 0xFF101010);
        Gui.drawRect(x, y, x + size, y + size, 0xFF292929);
        String label = "PNG NOT ADDED";
        minecraft.fontRendererObj.drawString(label,
                x + Math.max(2, (size - minecraft.fontRendererObj.getStringWidth(label)) / 2),
                y + size / 2 - 4, 0xFFFFC857);
    }

    private static void drawPendingPreview(Minecraft minecraft, int x, int y, int size) {
        Gui.drawRect(x - 2, y - 2, x + size + 2, y + size + 2, 0xFF101010);
        Gui.drawRect(x, y, x + size, y + size, 0xFF292929);
        String label = "PREVIEW PENDING";
        minecraft.fontRendererObj.drawString(label,
                x + Math.max(2, (size - minecraft.fontRendererObj.getStringWidth(label)) / 2),
                y + size / 2 - 4, 0xFFB8BDC8);
    }
}
