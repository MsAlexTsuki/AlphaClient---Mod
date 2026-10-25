package com.alpharip.staffmod.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

/**
 * Compact AlphaClient overlay opened with Right Control.
 * The game remains visible behind it and HUD elements can be dragged directly.
 */
public final class AlphaClientHomeMenu extends HudEditorGui {
    private static final int MODS = 900;
    private static final ResourceLocation LOGO =
            new ResourceLocation("alphastaffmod", "textures/gui/alpha-client-icon.png");
    private static final int ACCENT = 0xFFFFD866;

    private int modsX;
    private int modsY;
    private int modsWidth;
    private int modsHeight;

    public AlphaClientHomeMenu() {
        super(null);
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.clear();
        modsWidth = Math.min(220, Math.max(150, width - 32));
        modsHeight = 30;
        modsX = (width - modsWidth) / 2;
        modsY = Math.max(84, height / 2 - 4);
        buttonList.add(new AlphaButton(MODS, modsX, modsY, modsWidth, modsHeight, "MODS", true));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // No opaque background: keep the live Minecraft world visible.
        int logoWidth = Math.min(132, Math.max(80, width - 32));
        int logoHeight = logoWidth * 448 / 512;
        int logoX = (width - logoWidth) / 2;
        int logoY = Math.max(8, modsY - logoHeight - 28);

        Minecraft.getMinecraft().getTextureManager().bindTexture(LOGO);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        drawScaledCustomSizeModalRect(logoX, logoY, 0.0F, 0.0F, 512, 448,
                logoWidth, logoHeight, 512.0F, 448.0F);
        GlStateManager.disableBlend();
        drawCenteredString(fontRendererObj, "ALPHA CLIENT", width / 2,
                logoY + logoHeight + 3, 0xFFF3F0E8);

        // Parent editor renders the actual HUD previews and handles dragging/scaling.
        super.drawScreen(mouseX, mouseY, partialTicks);
        drawCenteredString(fontRendererObj, "DRAG HUD ELEMENTS TO MOVE", width / 2,
                height - 16, 0xBFFFFFFF);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button != null && button.id == MODS) {
            Minecraft.getMinecraft().displayGuiScreen(new AlphaModMenu(this));
            return;
        }
        super.actionPerformed(button);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
