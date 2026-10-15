package com.alpharip.staffmod.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

/**
 * Fully custom AlphaClient main menu.
 *
 * This screen deliberately extends GuiScreen rather than GuiMainMenu: it does
 * not initialize, render, or expose the vanilla panorama or vanilla menu.
 */
public final class AlphaMainMenu extends GuiScreen {
    private static final int PLAY = 10;
    private static final int MULTIPLAYER = 11;
    private static final int MODULES = 12;
    private static final int OPTIONS = 13;
    private static final int PARTNER = 14;
    private static final int QUIT = 15;
    private static final int ACCOUNT_MENU = 16;
    private static final int ADD_ACCOUNT = 17;

    private static final int ACCENT = 0xFFFFD866;
    private static final ResourceLocation ALPHA_LOGO =
            new ResourceLocation("alphastaffmod", "textures/gui/alpha-client-icon.png");

    private boolean accountMenuOpen;
    private int buttonWidth;
    private int buttonHeight;
    private int buttonGap;
    private int menuLeft;
    private int menuTop;

    @Override
    public void initGui() {
        buttonList.clear();
        buttonWidth = Math.min(300, Math.max(210, width / 4));
        buttonHeight = 34;
        buttonGap = 9;
        menuLeft = Math.max(42, width / 2 - buttonWidth - 30);
        menuTop = Math.max(190, height / 2 - 10);

        buttonList.add(new AlphaButton(PLAY, menuLeft, menuTop, buttonWidth, buttonHeight, "PLAY", true));
        buttonList.add(new AlphaButton(MULTIPLAYER, menuLeft, menuTop + buttonHeight + buttonGap,
                buttonWidth, buttonHeight, "MULTIPLAYER"));
        buttonList.add(new AlphaButton(MODULES, menuLeft, menuTop + 2 * (buttonHeight + buttonGap),
                buttonWidth, buttonHeight, "MODS"));
        buttonList.add(new AlphaButton(OPTIONS, menuLeft, menuTop + 3 * (buttonHeight + buttonGap),
                buttonWidth, buttonHeight, "SETTINGS"));
        buttonList.add(new AlphaButton(PARTNER, menuLeft, menuTop + 4 * (buttonHeight + buttonGap),
                buttonWidth, buttonHeight, "CONNECT TO ALPHA.RIP"));
        buttonList.add(new AlphaButton(QUIT, menuLeft, menuTop + 5 * (buttonHeight + buttonGap),
                buttonWidth, buttonHeight, "QUIT"));

        buttonList.add(new AlphaButton(ACCOUNT_MENU, 22, 20, 116, 28,
                accountMenuOpen ? "ACCOUNT  -" : "ACCOUNT  +"));
        if (accountMenuOpen) {
            buttonList.add(new AlphaButton(ADD_ACCOUNT, 22, 54, 116, 26, "ACCOUNT INFO"));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawAlphaBackground();

        int panelLeft = Math.max(20, width / 2 + 20);
        int panelRight = width - 28;
        int panelTop = Math.max(54, height / 2 - 170);
        int panelBottom = height - 54;

        // Brand panel: opaque surfaces ensure no vanilla panorama can show through.
        drawRect(panelLeft, panelTop, panelRight, panelBottom, 0xE6101217);
        drawRect(panelLeft, panelTop, panelRight, panelTop + 2, ACCENT);
        drawRect(panelLeft, panelTop, panelLeft + 1, panelBottom, 0x887D682D);
        drawRect(panelLeft + 24, panelTop + 26, panelRight - 24, panelTop + 27, 0x557F8794);

        int availableWidth = Math.max(1, panelRight - panelLeft - 48);
        int logoWidth = Math.min(250, availableWidth);
        int logoHeight = logoWidth * 448 / 512;
        int logoX = panelLeft + (panelRight - panelLeft - logoWidth) / 2;
        int logoY = panelTop + 35;
        Minecraft.getMinecraft().getTextureManager().bindTexture(ALPHA_LOGO);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        drawScaledCustomSizeModalRect(logoX, logoY, 0.0F, 0.0F, 512, 448,
                logoWidth, logoHeight, 512.0F, 448.0F);
        GlStateManager.disableBlend();

        int textY = logoY + logoHeight + 22;
        drawCenteredString(fontRendererObj, "YOUR GAME. YOUR RULES.",
                (panelLeft + panelRight) / 2, textY, 0xFFF5F2E8);
        drawCenteredString(fontRendererObj, "A DIFFERENT WAY TO PLAY",
                (panelLeft + panelRight) / 2, textY + 20, ACCENT);

        int lineY = Math.min(panelBottom - 72, textY + 60);
        drawRect(panelLeft + 34, lineY, panelRight - 34, lineY + 1, 0x557F8794);
        drawString(fontRendererObj, "MINECRAFT 1.8.9", panelLeft + 28, lineY + 18, 0xFFD0D3DB);
        drawString(fontRendererObj, "CUSTOM CLIENT EXPERIENCE", panelLeft + 28, lineY + 34, 0xFF858B98);

        // Small decorative status indicator, purely visual (not a fake loading/progress value).
        drawRect(panelRight - 46, panelBottom - 27, panelRight - 38, panelBottom - 19, ACCENT);
        drawString(fontRendererObj, "ALPHA CLIENT", panelRight - 130, panelBottom - 27, 0xFFB9BEC8);

        drawString(fontRendererObj, "ALPHA CLIENT", 22, height - 32, ACCENT);
        drawString(fontRendererObj, "v" + com.alpharip.staffmod.BuildInfo.VERSION
                + "  |  FORGE 1.8.9", 22, height - 19, 0xFF969DAA);
        drawString(fontRendererObj, "Play Different.", width - fontRendererObj.getStringWidth("Play Different.") - 22,
                height - 23, 0xFFB9BEC8);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawAlphaBackground() {
        // Fully opaque, custom-drawn background; no world panorama or vanilla menu is rendered.
        drawRect(0, 0, width, height, 0xFF0B0D11);
        drawGradientRect(0, 0, width, height, 0xFF171A21, 0xFF090A0D);

        int split = width / 2;
        drawRect(0, 0, split, height, 0x551D2028);
        drawRect(split - 1, 0, split + 1, height, 0x557B652B);

        // Restrained angular brand geometry.
        drawRect(0, 0, width, 3, ACCENT);
        drawRect(0, height - 3, width, height, 0xFF252017);
        drawRect(24, 70, 25, height - 72, 0x337F8794);
        drawRect(24, 70, Math.min(width - 24, 190), 71, 0x337F8794);
        drawRect(width - 190, height - 72, width - 24, height - 71, 0x337F8794);
        drawRect(width - 25, 72, width - 24, height - 72, 0x337F8794);

        // Subtle yellow light field, entirely contained in the custom background.
        drawGradientRect(0, 0, width / 3, height, 0x18FFD866, 0x000B0D11);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;

        Minecraft minecraft = Minecraft.getMinecraft();
        switch (button.id) {
            case PLAY:
                minecraft.displayGuiScreen(new GuiSelectWorld(this));
                break;
            case ACCOUNT_MENU:
                accountMenuOpen = !accountMenuOpen;
                initGui();
                break;
            case ADD_ACCOUNT:
                minecraft.displayGuiScreen(new AccountInfoGui(this));
                break;
            case MULTIPLAYER:
                minecraft.displayGuiScreen(new GuiMultiplayer(this));
                break;
            case MODULES:
                minecraft.displayGuiScreen(new AlphaModMenu(this));
                break;
            case OPTIONS:
                minecraft.displayGuiScreen(new AlphaSettingsMenu(this));
                break;
            case PARTNER:
                minecraft.displayGuiScreen(new GuiConnecting(this, minecraft,
                        new ServerData("Alpha.Rip", "alpha.rip:25565", false)));
                break;
            case QUIT:
                minecraft.shutdown();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
