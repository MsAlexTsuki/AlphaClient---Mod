// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.quality.enchantingname;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiEnchantment;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.ContainerEnchantment;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Replaces the vanilla obfuscated enchanting-table option text with
 * localized enchantment names. The vanilla screen and option interactions
 * remain intact; only the three option rows are redrawn.
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
    private static final int ICON_WIDTH = 16;
    private static final int TEXT_X_OFFSET = 20;
    private static final int TEXT_WIDTH = OPTION_ROW_WIDTH - TEXT_X_OFFSET - 4;
    private static final int TEXT_COLOR = 0xFFFFFF;
    private static final int EMPTY_ROW_COLOR = 0xFF252525;

    private final EnchantingName enchantingName = new EnchantingName();

    @SubscribeEvent
    public void onEnchantingTableDraw(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!EnchantingNameMod.isEnabled()
                || event == null
                || !(event.gui instanceof GuiEnchantment)) {
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.thePlayer == null
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
            int rowX = guiLeft + OPTION_X_OFFSET;
            int rowY = guiTop + OPTION_Y_OFFSET + OPTION_ROW_HEIGHT * option;

            boolean unavailable = requiredLevel <= 0;
            boolean disabled = unavailable
                    || (!minecraft.thePlayer.capabilities.isCreativeMode
                    && (lapisAmount < option + 1
                    || minecraft.thePlayer.experienceLevel < requiredLevel));

            boolean hovered = !disabled
                    && event.mouseX >= rowX
                    && event.mouseX < rowX + OPTION_ROW_WIDTH
                    && event.mouseY >= rowY
                    && event.mouseY < rowY + OPTION_ROW_HEIGHT;

            redrawOptionRow(rowX, rowY, option, disabled, hovered);

            // Cover the complete vanilla text area on every row, including
            // unavailable options. This removes both the Standard Galactic
            // clue and vanilla's green level/cost text.
            Gui.drawRect(
                    rowX + TEXT_X_OFFSET,
                    rowY,
                    rowX + OPTION_ROW_WIDTH,
                    rowY + OPTION_ROW_HEIGHT,
                    EMPTY_ROW_COLOR
            );

            if (unavailable) {
                continue;
            }

            String displayName = enchantingName.getOptionDisplayName(container, option);
            if (displayName == null || displayName.trim().isEmpty()) {
                continue;
            }

            drawFittedText(minecraft, displayName,
                    rowX + TEXT_X_OFFSET + 2, rowY + 5, TEXT_WIDTH, TEXT_COLOR);
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
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
                ICON_WIDTH, ICON_WIDTH, 256.0F, 256.0F);
    }

    private void drawFittedText(Minecraft minecraft, String text,
                                int x, int y, int availableWidth, int color) {
        int textWidth = minecraft.fontRendererObj.getStringWidth(text);
        float horizontalScale = textWidth > availableWidth
                ? (float) availableWidth / (float) textWidth
                : 1.0F;

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, 0.0F);
        GlStateManager.scale(horizontalScale, 1.0F, 1.0F);
        minecraft.fontRendererObj.drawString(text, 0.0F, 0.0F, color);
        GlStateManager.popMatrix();
    }
}
