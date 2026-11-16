package com.alpharip.staffmod.screenshotcomparison;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;

/** Owns the temporary GPU texture used to display a composed comparison image. */
final class ScreenshotComparisonTexture {
    private ResourceLocation location;

    void update(BufferedImage image) {
        clear();
        if (image == null) return;
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null) return;
        location = minecraft.getTextureManager().getDynamicTextureLocation(
                "alpha_screenshot_comparison", new DynamicTexture(image));
    }

    ResourceLocation getLocation() {
        return location;
    }

    void clear() {
        if (location != null) {
            Minecraft.getMinecraft().getTextureManager().deleteTexture(location);
            location = null;
        }
    }
}
