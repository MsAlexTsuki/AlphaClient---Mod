package com.alpharip.staffmod.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.GlStateManager;

import java.io.IOException;

/** Completely reworked AlphaClient landing screen for Minecraft 1.8.9. */
public final class AlphaMainMenu extends GuiScreen {
    private static final int PLAY = 10;
    private static final int MULTIPLAYER = 11;
    private static final int MODULES = 12;
    private static final int OPTIONS = 13;
    private static final int PARTNER = 14;
    private static final int QUIT = 15;

    @Override
    public void initGui() {
        buttonList.clear();
        int sidebarWidth = Math.max(190, Math.min(230, width / 4));
        int contentLeft = sidebarWidth + 30;
        int contentWidth = Math.max(220, width - contentLeft - 28);
        int buttonWidth = Math.min(270, contentWidth);
        int buttonX = contentLeft;
        int buttonY = Math.max(174, height / 2 - 12);

        buttonList.add(new AlphaButton(PLAY, buttonX, buttonY, buttonWidth, 28, "SINGLEPLAYER"));
        buttonList.add(new AlphaButton(MULTIPLAYER, buttonX, buttonY + 36, buttonWidth, 28, "MULTIPLAYER"));
        buttonList.add(new AlphaButton(MODULES, buttonX, buttonY + 72, buttonWidth, 28, "MODULES"));
        buttonList.add(new AlphaButton(OPTIONS, buttonX, buttonY + 108, buttonWidth, 28, "SETTINGS"));
        buttonList.add(new AlphaButton(PARTNER, buttonX, buttonY + 144, buttonWidth, 28, "JOIN ALPHA.RIP"));
        buttonList.add(new AlphaButton(QUIT, width - 92, height - 32, 78, 20, "QUIT"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawGradientRect(0, 0, width, height, 0xFF14161B, 0xFF090A0D);

        int sidebarWidth = Math.max(190, Math.min(230, width / 4));
        drawRect(0, 0, sidebarWidth, height, 0xFF101116);
        drawRect(sidebarWidth - 1, 0, sidebarWidth, height, 0xFF343740);
        drawRect(22, 25, 25, 65, 0xFFFFD866);
        drawString(fontRendererObj, "ALPHA", 36, 28, 0xFFFFD866);
        drawString(fontRendererObj, "CLIENT", 36, 41, 0xFFFFFFFF);
        drawString(fontRendererObj, "MINECRAFT 1.8.9", 22, 78, 0xFF858A96);
        drawString(fontRendererObj, "Forge Edition", 22, 91, 0xFF666B76);

        int contentLeft = sidebarWidth + 30;
        drawString(fontRendererObj, "WELCOME BACK", contentLeft, 35, 0xFFFFD866);
        drawString(fontRendererObj, "Your Minecraft, your Alpha.", contentLeft, 53, 0xFFFFFFFF);
        drawString(fontRendererObj, "Choose a destination to get started.", contentLeft, 68, 0xFF9DA2AE);

        int cardTop = 91;
        int cardRight = width - 28;
        drawRect(contentLeft, cardTop, cardRight, cardTop + 56, 0xAA202229);
        drawRect(contentLeft, cardTop, cardRight, cardTop + 1, 0xFF454954);
        drawString(fontRendererObj, "CLIENT STATUS", contentLeft + 12, cardTop + 10, 0xFFFFD866);
        drawString(fontRendererObj, "AlphaClient is running locally", contentLeft + 12, cardTop + 25, 0xFFE8E9ED);
        drawString(fontRendererObj, "Version  " + com.alpharip.staffmod.BuildInfo.VERSION,
                contentLeft + 12, cardTop + 39, 0xFF858A96);

        super.drawScreen(mouseX, mouseY, partialTicks);
        GlStateManager.disableLighting();
        drawString(fontRendererObj, "ALPHA.RIP  /  COMMUNITY", 22, height - 30, 0xFF858A96);
        drawString(fontRendererObj, "v" + com.alpharip.staffmod.BuildInfo.VERSION,
                width - 84, height - 18, 0xFF858A96);
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
                minecraft.displayGuiScreen(new GuiOptions(this, minecraft.gameSettings));
                break;
            case PARTNER:
                // Only this explicit button click initiates a connection.
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
