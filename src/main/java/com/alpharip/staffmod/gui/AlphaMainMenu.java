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

    private static final int ACCENT = 0xFFFFD866;
    private static final ResourceLocation ALPHA_LOGO =
            new ResourceLocation("alphastaffmod", "textures/gui/alpha_launcher_logo.png");
    private int sidebarWidth;
    private int menuButtonWidth;
    private int menuButtonHeight;
    private int menuButtonGap;

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
        buttonList.add(new AlphaButton(PLAY, buttonX, firstY, menuButtonWidth, menuButtonHeight, "SPELEN", true));
        buttonList.add(new AlphaButton(OPTIONS, buttonX, firstY + 42, menuButtonWidth, menuButtonHeight, "INSTELLINGEN"));
        buttonList.add(new AlphaButton(MODULES, buttonX, firstY + 84, menuButtonWidth, menuButtonHeight, "MODS"));
        buttonList.add(new AlphaButton(QUIT, buttonX, firstY + 126, menuButtonWidth, menuButtonHeight, "AFSLUITEN"));
        buttonList.add(new AlphaButton(MULTIPLAYER, 20, 18, 190, 36, "ACCOUNT / MULTIPLAYER"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        drawRect(0, 0, width, height, 0x22000000);
        drawGradientRect(0, 0, width, Math.min(height, 180), 0x66070A10, 0x00070A10);
        drawGradientRect(0, Math.max(0, height - 90), width, height, 0x00070A10, 0x99070A10);
        int logoSize = Math.min(230, Math.max(150, height / 3));
        int logoX = (width - logoSize) / 2;
        int logoY = Math.max(20, height / 2 - logoSize - 32);
        Minecraft.getMinecraft().getTextureManager().bindTexture(ALPHA_LOGO);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        drawModalRectWithCustomSizedTexture(logoX, logoY, 0, 0, logoSize, logoSize, 1238, 1238);
        drawString(fontRendererObj, "Play Different.", width - 116, height - 42, ACCENT);
        drawString(fontRendererObj, "AlphaClient  |  Minecraft 1.8.9", 20, height - 18, 0xFFB2B6C0);
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
