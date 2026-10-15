package com.alpharip.staffmod.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

/**
 * AlphaClient's compact main menu.
 *
 * GuiMainMenu supplies Minecraft's existing animated panorama background;
 * this screen layers the Alpha interface over it without replacing the
 * underlying world panorama with a static image.
 */
public final class AlphaMainMenu extends GuiMainMenu {
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
            new ResourceLocation("alphastaffmod", "textures/gui/alpha_launcher_logo.png");
    private int sidebarWidth;
    private int menuButtonWidth;
    private int menuButtonHeight;
    private int menuButtonGap;
    private boolean accountMenuOpen;

    @Override
    public void initGui() {
        // Keep vanilla's animated Minecraft panorama and replace only its controls.
        super.initGui();
        buttonList.clear();
        menuButtonWidth = Math.min(310, Math.max(220, width / 4));
        menuButtonHeight = 34;
        menuButtonGap = 8;
        int centerX = width / 2;
        int firstY = Math.max(245, height / 2 + 20);
        int buttonX = centerX - menuButtonWidth / 2;
        buttonList.add(new AlphaButton(PLAY, buttonX, firstY, menuButtonWidth, menuButtonHeight, "PLAY", true));
        buttonList.add(new AlphaButton(OPTIONS, buttonX, firstY + 42, menuButtonWidth, menuButtonHeight, "SETTINGS"));
        buttonList.add(new AlphaButton(MODULES, buttonX, firstY + 84, menuButtonWidth, menuButtonHeight, "MODS"));
        buttonList.add(new AlphaButton(QUIT, buttonX, firstY + 126, menuButtonWidth, menuButtonHeight, "QUIT"));
        buttonList.add(new AlphaButton(ACCOUNT_MENU, 20, 18, 112, 24, accountMenuOpen ? "ACCOUNT  v" : "ACCOUNT  >"));
        if (accountMenuOpen) {
            buttonList.add(new AlphaButton(MULTIPLAYER, 20, 46, 112, 22, "MULTIPLAYER"));
            buttonList.add(new AlphaButton(ADD_ACCOUNT, 136, 18, 26, 24, "+", true));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        // Cover the vanilla Minecraft logo while keeping the animated panorama visible.
        int logoCoverWidth = Math.min(560, width - 40);
        int logoCoverLeft = (width - logoCoverWidth) / 2;
        drawRect(logoCoverLeft, 22, logoCoverLeft + logoCoverWidth, Math.min(height / 2, 174), 0xC0101115);
        drawRect(0, 0, width, height, 0x12FFD866);
        drawGradientRect(0, 0, width, Math.min(height, 180), 0x28FFD866, 0x00FFD866);
        drawGradientRect(0, Math.max(0, height - 90), width, height, 0x00FFD866, 0x35FFD866);
        int logoSize = Math.min(220, Math.max(150, height / 4));
        int logoX = (width - logoSize) / 2;
        int logoY = 30;
        Minecraft.getMinecraft().getTextureManager().bindTexture(ALPHA_LOGO);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        drawModalRectWithCustomSizedTexture(logoX, logoY, 0, 0, logoSize, logoSize, 256, 256);
        drawString(fontRendererObj, "Play Different.", width - 116, height - 42, ACCENT);
        drawString(fontRendererObj, "AlphaClient  |  Version 1.8.9", 20, height - 18, 0xFFB2B6C0);
        drawString(fontRendererObj, "v" + com.alpharip.staffmod.BuildInfo.VERSION, width - 70, height - 18, 0xFFB2B6C0);
        for (GuiButton button : buttonList) button.drawButton(Minecraft.getMinecraft(), mouseX, mouseY);
        GlStateManager.disableLighting();
    }
    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) {
            return;
        }

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
