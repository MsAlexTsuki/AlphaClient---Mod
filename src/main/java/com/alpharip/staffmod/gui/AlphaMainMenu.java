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

/** AlphaClient landing screen. Keeps the vanilla destinations while applying the shared client visual system. */
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
        int sidebarWidth = Math.max(174, Math.min(210, width / 5));
        int contentLeft = sidebarWidth + 34;
        int contentWidth = Math.max(250, width - contentLeft - 34);
        int buttonWidth = Math.min(300, contentWidth);
        int buttonY = Math.max(190, height / 2 - 6);

        buttonList.add(new AlphaButton(PLAY, contentLeft, buttonY, buttonWidth, 32, "SINGLEPLAYER", true));
        buttonList.add(new AlphaButton(MULTIPLAYER, contentLeft, buttonY + 42, buttonWidth, 30, "MULTIPLAYER"));
        buttonList.add(new AlphaButton(MODULES, contentLeft, buttonY + 80, buttonWidth, 30, "MODULES"));
        buttonList.add(new AlphaButton(OPTIONS, contentLeft, buttonY + 118, buttonWidth, 30, "SETTINGS"));
        buttonList.add(new AlphaButton(PARTNER, contentLeft, buttonY + 156, buttonWidth, 30, "JOIN ALPHA.RIP"));
        buttonList.add(new AlphaButton(QUIT, width - 92, height - 32, 78, 20, "QUIT"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawGradientRect(0, 0, width, height, 0xFF181B22, 0xFF090A0E);

        int sidebarWidth = Math.max(174, Math.min(210, width / 5));
        drawRect(0, 0, sidebarWidth, height, 0xFF111319);
        drawRect(sidebarWidth - 1, 0, sidebarWidth, height, 0xFF292D36);
        drawRect(22, 26, 25, 69, 0xFFFFD866);
        drawString(fontRendererObj, "ALPHA", 36, 28, 0xFFFFD866);
        drawString(fontRendererObj, "CLIENT", 36, 42, 0xFFF4F5F8);
        drawString(fontRendererObj, "MINECRAFT 1.8.9", 22, 82, 0xFF9A9FAC);
        drawString(fontRendererObj, "FORGE EDITION", 22, 96, 0xFF686E7A);
        drawRect(22, 117, sidebarWidth - 22, 118, 0xFF2A2D35);
        drawString(fontRendererObj, "YOUR CLIENT", 22, 132, 0xFF686E7A);
        drawString(fontRendererObj, "ALPHA.RIP", 22, 148, 0xFFE2E4EA);
        drawString(fontRendererObj, "COMMUNITY", 22, 161, 0xFF9A9FAC);
        drawString(fontRendererObj, "v" + com.alpharip.staffmod.BuildInfo.VERSION,
                22, height - 28, 0xFF686E7A);

        int contentLeft = sidebarWidth + 34;
        int contentRight = width - 30;
        drawString(fontRendererObj, "WELCOME BACK", contentLeft, 28, 0xFFFFD866);
        drawString(fontRendererObj, "Your Minecraft. Your Alpha.", contentLeft, 47, 0xFFF4F5F8);
        drawString(fontRendererObj, "Choose how you want to continue.", contentLeft, 63, 0xFF9A9FAC);

        int cardTop = 83;
        int cardBottom = 143;
        drawRect(contentLeft, cardTop, contentRight, cardBottom, 0xFF171A21);
        drawRect(contentLeft, cardTop, contentRight, cardTop + 1, 0xFF343944);
        drawRect(contentLeft, cardTop, contentLeft + 3, cardBottom, 0xFFFFD866);
        drawString(fontRendererObj, "CLIENT STATUS", contentLeft + 15, cardTop + 10, 0xFF9A9FAC);
        drawString(fontRendererObj, "AlphaClient is ready", contentLeft + 15, cardTop + 25, 0xFFF4F5F8);
        drawString(fontRendererObj, "Version " + com.alpharip.staffmod.BuildInfo.VERSION,
                contentLeft + 15, cardTop + 42, 0xFF858B98);

        int actionY = Math.max(190, height / 2 - 6);
        drawString(fontRendererObj, "GET STARTED", contentLeft, actionY - 22, 0xFF686E7A);
        drawString(fontRendererObj, "Launch a world or manage your client setup.",
                contentLeft, actionY + 204, 0xFF858B98);

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
