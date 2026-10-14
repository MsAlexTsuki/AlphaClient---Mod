package com.alpharip.staffmod.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.GlStateManager;

import java.io.IOException;

/** Compact AlphaClient landing screen; adapts its spacing to the scaled Minecraft resolution. */
public final class AlphaMainMenu extends GuiScreen {
    private static final int PLAY = 10;
    private static final int MULTIPLAYER = 11;
    private static final int MODULES = 12;
    private static final int OPTIONS = 13;
    private static final int PARTNER = 14;
    private static final int QUIT = 15;

    private int sidebarWidth;
    private int contentLeft;
    private int buttonWidth;
    private int buttonY;

    @Override
    public void initGui() {
        buttonList.clear();
        sidebarWidth = Math.max(126, Math.min(158, width / 7));
        contentLeft = sidebarWidth + 24;
        int contentWidth = Math.max(190, width - contentLeft - 24);
        buttonWidth = Math.min(250, contentWidth);
        int contentCenter = contentLeft + contentWidth / 2;
        int buttonX = contentCenter - buttonWidth / 2;
        buttonY = Math.max(156, Math.min(height - 174, height / 2 - 52));
        int buttonHeight = 25;
        int gap = 7;

        buttonList.add(new AlphaButton(PLAY, buttonX, buttonY, buttonWidth, buttonHeight, "PLAY SINGLEPLAYER", true));
        buttonList.add(new AlphaButton(MULTIPLAYER, buttonX, buttonY + buttonHeight + gap,
                buttonWidth, buttonHeight, "MULTIPLAYER"));
        buttonList.add(new AlphaButton(MODULES, buttonX, buttonY + (buttonHeight + gap) * 2,
                buttonWidth, buttonHeight, "MODULES"));
        buttonList.add(new AlphaButton(OPTIONS, buttonX, buttonY + (buttonHeight + gap) * 3,
                buttonWidth, buttonHeight, "SETTINGS"));
        buttonList.add(new AlphaButton(PARTNER, buttonX, buttonY + (buttonHeight + gap) * 4,
                buttonWidth, buttonHeight, "JOIN ALPHA.RIP"));
        buttonList.add(new AlphaButton(QUIT, width - 72, height - 25, 60, 16, "QUIT"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawGradientRect(0, 0, width, height, 0xFF181B22, 0xFF090A0E);

        drawRect(0, 0, sidebarWidth, height, 0xFF111319);
        drawRect(sidebarWidth - 1, 0, sidebarWidth, height, 0xFF292D36);
        drawRect(16, 22, 19, 54, 0xFFFFD866);
        drawString(fontRendererObj, "ALPHA", 27, 23, 0xFFFFD866);
        drawString(fontRendererObj, "CLIENT", 27, 35, 0xFFF4F5F8);
        drawString(fontRendererObj, "MINECRAFT 1.8.9", 16, 64, 0xFF9A9FAC);
        drawString(fontRendererObj, "FORGE EDITION", 16, 76, 0xFF686E7A);
        drawRect(16, 94, sidebarWidth - 16, 95, 0xFF2A2D35);
        drawString(fontRendererObj, "ALPHA.RIP", 16, 108, 0xFFE2E4EA);
        drawString(fontRendererObj, "COMMUNITY", 16, 120, 0xFF9A9FAC);
        drawString(fontRendererObj, "v" + com.alpharip.staffmod.BuildInfo.VERSION,
                16, height - 18, 0xFF686E7A);

        int contentWidth = width - contentLeft - 24;
        int contentRight = width - 24;
        drawString(fontRendererObj, "WELCOME BACK", contentLeft, 22, 0xFFFFD866);
        drawString(fontRendererObj, "Your Minecraft. Your Alpha.", contentLeft, 38, 0xFFF4F5F8);
        drawString(fontRendererObj, "Choose where to go.", contentLeft, 51, 0xFF9A9FAC);

        int cardWidth = Math.min(330, contentWidth);
        int cardLeft = contentLeft + (contentWidth - cardWidth) / 2;
        int cardTop = 70;
        int cardBottom = 112;
        drawRect(cardLeft, cardTop, cardLeft + cardWidth, cardBottom, 0xFF171A21);
        drawRect(cardLeft, cardTop, cardLeft + cardWidth, cardTop + 1, 0xFF343944);
        drawRect(cardLeft, cardTop, cardLeft + 2, cardBottom, 0xFFFFD866);
        drawString(fontRendererObj, "CLIENT READY", cardLeft + 11, cardTop + 8, 0xFF9A9FAC);
        drawString(fontRendererObj, "AlphaClient  v" + com.alpharip.staffmod.BuildInfo.VERSION,
                cardLeft + 11, cardTop + 23, 0xFFF4F5F8);

        drawString(fontRendererObj, "QUICK ACCESS", contentLeft, buttonY - 16, 0xFF686E7A);
        super.drawScreen(mouseX, mouseY, partialTicks);
        GlStateManager.disableLighting();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
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
