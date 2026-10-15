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

/** Fully custom AlphaClient menu; deliberately does not render GuiMainMenu or its panorama. */
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

        buttonWidth = Math.min(292, Math.max(220, width / 4));
        buttonHeight = 32;
        buttonGap = 8;
        menuLeft = Math.max(34, width / 2 - buttonWidth - 48);
        int menuHeight = 6 * buttonHeight + 5 * buttonGap;
        menuTop = Math.max(170, (height - menuHeight) / 2 + 28);

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

        buttonList.add(new AlphaButton(ACCOUNT_MENU, 24, 22, 112, 27,
                accountMenuOpen ? "ACCOUNT  -" : "ACCOUNT  +"));
        if (accountMenuOpen) {
            buttonList.add(new AlphaButton(ADD_ACCOUNT, 24, 55, 112, 25, "ACCOUNT INFO"));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawAlphaBackground();

        // Left navigation card and title hierarchy.
        int menuCardLeft = menuLeft - 22;
        int menuCardRight = menuLeft + buttonWidth + 22;
        int menuCardTop = menuTop - 84;
        int menuCardBottom = menuTop + 6 * buttonHeight + 5 * buttonGap + 22;

        drawRect(menuCardLeft, menuCardTop, menuCardRight, menuCardBottom, 0xA90E1015);
        drawRect(menuCardLeft, menuCardTop, menuCardRight, menuCardTop + 2, 0x887D682D);
        drawRect(menuCardLeft, menuCardTop, menuCardLeft + 1, menuCardBottom, 0x557F8794);

        drawString(fontRendererObj, "ALPHA CLIENT", menuLeft, menuTop - 67, 0xFFF3F0E8);
        drawString(fontRendererObj, "YOUR NEXT MINECRAFT EXPERIENCE", menuLeft, menuTop - 49, ACCENT);
        drawRect(menuLeft, menuTop - 30, menuLeft + 46, menuTop - 28, ACCENT);
        drawRect(menuLeft + 51, menuTop - 30, menuLeft + 98, menuTop - 29, 0x557F8794);

        // Right-side brand panel.
        int panelLeft = Math.min(width - 170, width / 2 + 28);
        int panelRight = width - 28;
        int panelTop = Math.max(46, height / 2 - 180);
        int panelBottom = Math.min(height - 48, height / 2 + 178);
        drawRect(panelLeft + 5, panelTop + 5, panelRight + 1, panelBottom + 1, 0x66000000);
        drawRect(panelLeft, panelTop, panelRight, panelBottom, 0xE6101218);
        drawRect(panelLeft, panelTop, panelRight, panelTop + 2, ACCENT);
        drawRect(panelLeft, panelTop, panelLeft + 1, panelBottom, 0x887D682D);
        drawRect(panelLeft + 24, panelTop + 25, panelRight - 24, panelTop + 26, 0x557F8794);

        int availableWidth = Math.max(1, panelRight - panelLeft - 42);
        int logoWidth = Math.min(250, availableWidth);
        int logoHeight = logoWidth * 448 / 512;
        int logoX = panelLeft + (panelRight - panelLeft - logoWidth) / 2;
        int logoY = panelTop + 42;
        Minecraft.getMinecraft().getTextureManager().bindTexture(ALPHA_LOGO);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        drawScaledCustomSizeModalRect(logoX, logoY, 0.0F, 0.0F, 512, 448,
                logoWidth, logoHeight, 512.0F, 448.0F);
        GlStateManager.disableBlend();

        int centerX = (panelLeft + panelRight) / 2;
        int taglineY = Math.max(logoY + logoHeight + 12, panelTop + 205);
        drawCenteredString(fontRendererObj, "PLAY DIFFERENT.", centerX, taglineY, 0xFFF5F2E8);
        drawCenteredString(fontRendererObj, "BUILT AROUND YOUR GAME", centerX, taglineY + 19, ACCENT);

        int dividerY = Math.min(panelBottom - 55, taglineY + 47);
        drawRect(panelLeft + 30, dividerY, panelRight - 30, dividerY + 1, 0x557F8794);
        drawString(fontRendererObj, "MINECRAFT 1.8.9", panelLeft + 26, dividerY + 15, 0xFFD0D3DB);
        drawString(fontRendererObj, "FORGE  |  ALPHACLIENT", panelLeft + 26, dividerY + 30, 0xFF858B98);

        // Compact footer.
        drawString(fontRendererObj, "ALPHA CLIENT", 24, height - 32, ACCENT);
        drawString(fontRendererObj, "v" + com.alpharip.staffmod.BuildInfo.VERSION
                + "  /  FORGE 1.8.9", 24, height - 19, 0xFF969DAA);
        drawString(fontRendererObj, "ALPHA.RIP", width - fontRendererObj.getStringWidth("ALPHA.RIP") - 24,
                height - 23, 0xFFB9BEC8);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawAlphaBackground() {
        // Opaque custom background: no Minecraft panorama is drawn or visible behind the UI.
        drawRect(0, 0, width, height, 0xFF090B0F);
        drawGradientRect(0, 0, width, height, 0xFF171A21, 0xFF090A0D);

        // Large, low-contrast panels add depth without competing with the controls.
        drawRect(0, 0, width / 2, height, 0x241D2028);
        drawGradientRect(0, height / 4, width / 2, height, 0x161E222B, 0x00090A0D);
        drawRect(0, 0, width, 3, ACCENT);
        drawRect(0, height - 3, width, height, 0xFF252017);

        // Fine framing and angled accent motif.
        drawRect(22, 78, 23, height - 66, 0x337F8794);
        drawRect(22, 78, Math.min(width - 22, 160), 79, 0x337F8794);
        drawRect(width - 160, height - 67, width - 22, height - 66, 0x337F8794);
        drawRect(width - 23, 80, width - 22, height - 66, 0x337F8794);
        drawRect(width / 2 - 1, 74, width / 2, height - 74, 0x337D682D);

        // Subtle yellow wash on the left, using Alpha Yellow as an accent rather than a full-screen fill.
        drawGradientRect(0, 0, width / 3, height, 0x12FFD866, 0x00090B0F);
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
