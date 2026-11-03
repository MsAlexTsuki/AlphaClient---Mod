package com.alpharip.staffmod.enchantingname;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiEnchantment;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.ContainerEnchantment;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Replaces only the three obfuscated enchantment clue text rows in the
 * vanilla 1.8.9 enchanting table.
 *
 * <p>The vanilla enchanting-table screen, book, slots, buttons, layout and
 * interaction remain unchanged. After vanilla draws the screen, this handler
 * redraws each option's existing row background and icon to cover the
 * Standard Galactic Alphabet text, then draws the localized enchantment name
 * in the same text area.</p>
 *
 * <p>Idea-only code for the mod-ideas branch. Register this listener on the
 * Forge event bus when the feature is moved into the runtime mod.</p>
 */
public final class EnchantingNameGuiText {

    private static final ResourceLocation ENCHANTING_TEXTURE =
            new ResourceLocation("textures/gui/container/enchanting_table.png");

    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 166;
    private static final int OPTION_X_OFFSET = 60;
    private static final int OPTION_Y_OFFSET = 14;
    private static final int OPTION_ROW_WIDTH = 108;
    private static final int OPTION_ROW_HEIGHT = 19;
    private static final int TEXT_X_OFFSET = 20;
    private static final int TEXT_WIDTH = 86;

    private final EnchantingName enchantingName = new EnchantingName();

    @SubscribeEvent
    public void onEnchantingTableDraw(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!(event.gui instanceof GuiEnchantment)) {
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.thePlayer == null
                || !(minecraft.thePlayer.openContainer instanceof ContainerEnchantment)) {
            return;
        }

        ContainerEnchantment container =
                (ContainerEnchantment) minecraft.thePlayer.openContainer;

        int guiLeft = (event.gui.width - GUI_WIDTH) / 2;
        int guiTop = (event.gui.height - GUI_HEIGHT) / 2;
        int lapisAmount = container.getLapisAmount();

        minecraft.getTextureManager().bindTexture(ENCHANTING_TEXTURE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        for (int option = 0; option < 3; option++) {
            int requiredLevel = container.enchantLevels[option];
            if (requiredLevel <= 0) {
                continue;
            }

            int rowX = guiLeft + OPTION_X_OFFSET;
            int rowY = guiTop + OPTION_Y_OFFSET + OPTION_ROW_HEIGHT * option;

            boolean disabled = !minecraft.thePlayer.capabilities.isCreativeMode
                    && (lapisAmount < option + 1
                    || minecraft.thePlayer.experienceLevel < requiredLevel);

            boolean hovered = !disabled
                    && event.mouseX >= rowX
                    && event.mouseX < rowX + OPTION_ROW_WIDTH
                    && event.mouseY >= rowY
                    && event.mouseY < rowY + OPTION_ROW_HEIGHT;

            redrawOptionRow(rowX, rowY, option, disabled, hovered);

            String displayName = enchantingName.getOptionDisplayName(container, option);
            if (displayName == null || displayName.trim().isEmpty()) {
                displayName = "Unknown";
            }

            int textColor = getTextColor(disabled, hovered);
            drawFittedText(minecraft, displayName, rowX + TEXT_X_OFFSET,
                    rowY + 5, textColor);
        }
    }

    private void redrawOptionRow(int x, int y, int option,
                                 boolean disabled, boolean hovered) {
        int backgroundV = disabled ? 185 : (hovered ? 204 : 166);
        int iconV = disabled ? 239 : 223;

        Gui.drawModalRectWithCustomSizedTexture(
                x, y, 0.0F, (float) backgroundV,
                OPTION_ROW_WIDTH, OPTION_ROW_HEIGHT, 256.0F, 256.0F);

        Gui.drawModalRectWithCustomSizedTexture(
                x + 1, y + 1, (float) (16 * option), (float) iconV,
                16, 16, 256.0F, 256.0F);
    }

    private int getTextColor(boolean disabled, boolean hovered) {
        int color = 6839882;
        if (disabled) {
            return (color & 16711422) >> 1;
        }
        return hovered ? 16777088 : color;
    }

    private void drawFittedText(Minecraft minecraft, String text,
                                int x, int y, int color) {
        int textWidth = minecraft.fontRendererObj.getStringWidth(text);
        float horizontalScale = textWidth > TEXT_WIDTH
                ? (float) TEXT_WIDTH / (float) textWidth
                : 1.0F;

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, 0.0F);
        GlStateManager.scale(horizontalScale, 1.0F, 1.0F);
        minecraft.fontRendererObj.drawStringWithShadow(text, 0.0F, 0.0F, color);
        GlStateManager.popMatrix();
    }
}
