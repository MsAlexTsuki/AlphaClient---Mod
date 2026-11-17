// --- Made By MsAlexTsuki ---
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
    private static final int CLANS = 901;
    private static final ResourceLocation LOGO =
            new ResourceLocation("alphastaffmod", "textures/gui/alpha_client_logo.png");
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
        buttonList.add(new ClanIconButton(CLANS, (width - 38) / 2, modsY + modsHeight + 7));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // No opaque background: keep the live Minecraft world visible.
        int logoWidth = Math.min(170, Math.max(120, width - 32));
        int logoHeight = logoWidth;
        int logoX = (width - logoWidth) / 2;
        int logoY = Math.max(8, modsY - logoHeight - 28);

        Minecraft.getMinecraft().getTextureManager().bindTexture(LOGO);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        drawScaledCustomSizeModalRect(logoX, logoY, 0.0F, 0.0F, 1024, 1024,
                logoWidth, logoHeight, 1024.0F, 1024.0F);
        GlStateManager.disableBlend();

        // Parent editor renders the actual HUD previews and handles dragging/scaling.
        super.drawScreen(mouseX, mouseY, partialTicks);
        drawCenteredString(fontRendererObj, "DRAG HUD ELEMENTS TO MOVE", width / 2,
                height - 16, 0xBFFFFFFF);
    }

    /** Compact shield-and-members icon used for the AlphaClans shortcut. */
    private static final class ClanIconButton extends GuiButton {
        ClanIconButton(int id, int x, int y) {
            super(id, x, y, 38, 38, "");
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY) {
            if (!visible) return;
            hovered = mouseX >= xPosition && mouseY >= yPosition
                    && mouseX < xPosition + width && mouseY < yPosition + height;
            int accent = hovered ? 0xFFFFD866 : 0xFFE6E8ED;
            drawRect(xPosition, yPosition, xPosition + width, yPosition + height,
                    hovered ? 0xE02A2D35 : 0xC014161D);
            drawRect(xPosition, yPosition, xPosition + width, yPosition + 1,
                    hovered ? 0xFFFFD866 : 0x664B515D);
            drawRect(xPosition, yPosition + height - 1, xPosition + width,
                    yPosition + height, 0xAA090A0E);
            drawRect(xPosition, yPosition, xPosition + 1, yPosition + height,
                    hovered ? 0xFFFFD866 : 0x664B515D);
            drawRect(xPosition + width - 1, yPosition, xPosition + width,
                    yPosition + height, hovered ? 0xFFFFD866 : 0x664B515D);

            int cx = xPosition + 19;
            int top = yPosition + 6;
            // Shield outline, built from short segments to keep the icon crisp at GUI scale.
            drawRect(cx - 7, top, cx + 7, top + 2, accent);
            drawRect(cx - 10, top + 2, cx - 8, top + 10, accent);
            drawRect(cx + 8, top + 2, cx + 10, top + 10, accent);
            drawRect(cx - 8, top + 10, cx - 5, top + 15, accent);
            drawRect(cx + 5, top + 10, cx + 8, top + 15, accent);
            drawRect(cx - 5, top + 15, cx, top + 18, accent);
            drawRect(cx, top + 15, cx + 5, top + 18, accent);

            // Two member silhouettes inside the shield.
            drawRect(cx - 6, top + 5, cx - 2, top + 9, accent);
            drawRect(cx + 2, top + 5, cx + 6, top + 9, accent);
            drawRect(cx - 8, top + 10, cx, top + 13, accent);
            drawRect(cx, top + 10, cx + 8, top + 13, accent);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button != null && button.id == MODS) {
            Minecraft.getMinecraft().displayGuiScreen(new AlphaModMenu(this));
            return;
        }
        if (button != null && button.id == CLANS) {
            com.alpharip.staffmod.AlphaClans.AlphaClanService.INSTANCE.refresh();
            Minecraft.getMinecraft().displayGuiScreen(new com.alpharip.staffmod.AlphaClans.AlphaClansGui(this));
            return;
        }
        super.actionPerformed(button);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
