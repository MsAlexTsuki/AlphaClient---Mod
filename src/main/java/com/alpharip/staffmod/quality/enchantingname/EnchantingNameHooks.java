// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.quality.enchantingname;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiEnchantment;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.ContainerEnchantment;

/** Runtime hooks used by the GuiEnchantment transformer. */
public final class EnchantingNameHooks {
    private EnchantingNameHooks() {
    }

    public static boolean shouldSuppressHover(Object screen) {
        return screen instanceof GuiEnchantment && EnchantingNameMod.isEnabled();
    }

    /**
     * Replaces only the Standard Galactic option clue at the point where
     * vanilla draws it. The regular font renderer displays the localized name.
     */
    public static void drawEnchantingOptionText(FontRenderer vanillaRenderer,
                                                String vanillaText,
                                                int x, int y, int width, int color) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || !(minecraft.currentScreen instanceof GuiEnchantment)
                || !EnchantingNameMod.isEnabled()) {
            vanillaRenderer.drawSplitString(vanillaText, x, y, width, color);
            return;
        }

        GuiEnchantment screen = (GuiEnchantment) minecraft.currentScreen;
        int guiTop = (screen.height - 166) / 2;
        int option = (y - guiTop - 16) / 19;
        if (option < 0 || option > 2 || !(screen instanceof GuiContainer)) {
            vanillaRenderer.drawSplitString(vanillaText, x, y, width, color);
            return;
        }

        if (!(screen.inventorySlots instanceof ContainerEnchantment)) {
            vanillaRenderer.drawSplitString(vanillaText, x, y, width, color);
            return;
        }

        ContainerEnchantment container = (ContainerEnchantment) screen.inventorySlots;
        String displayName = new EnchantingName().getOptionDisplayName(container, option);
        if (displayName == null || displayName.trim().isEmpty()) {
            vanillaRenderer.drawSplitString(vanillaText, x, y, width, color);
            return;
        }

        minecraft.fontRendererObj.drawSplitString(displayName, x, y, width, color);
    }

    /**
     * Draws vanilla enchanting textures unchanged, except for the small
     * left-hand enchantment icon while Enchanting Names is enabled.
     */
    public static void drawEnchantingOptionTexture(GuiEnchantment screen,
                                                   int x, int y, int textureX, int textureY,
                                                   int width, int height) {
        if (EnchantingNameMod.isEnabled() && (textureY == 223 || textureY == 239)) {
            return;
        }

        Gui.drawModalRectWithCustomSizedTexture(
                x, y, (float) textureX, (float) textureY,
                width, height, 256.0F, 256.0F);
    }
}
