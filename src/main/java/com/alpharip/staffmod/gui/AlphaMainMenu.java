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

/** Original dark AlphaClient menu for Minecraft 1.8.9. */
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
        int center = width / 2;
        int buttonWidth = Math.min(300, width - 40);
        int left = center - buttonWidth / 2;
        int y = Math.max(130, height / 2 - 30);
        buttonList.add(new AlphaButton(PLAY, left, y, buttonWidth, 24, "SINGLEPLAYER"));
        buttonList.add(new GuiButton(MULTIPLAYER, left, y + 29, buttonWidth, 24, "MULTIPLAYER"));
        buttonList.add(new GuiButton(MODULES, left, y + 58, buttonWidth, 24, "MOD MENU"));
        buttonList.add(new GuiButton(OPTIONS, left, y + 87, buttonWidth, 24, "SETTINGS"));
        buttonList.add(new GuiButton(PARTNER, left, y + 116, buttonWidth, 24, "JOIN ALPHA.RIP"));
        buttonList.add(new GuiButton(QUIT, left, y + 145, buttonWidth, 24, "QUIT"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawGradientRect(0, 0, width, height, 0xFF17191F, 0xFF0B0C10);
        drawGradientRect(width / 2 - 170, 38, width / 2 + 170, 116, 0x332A2D35, 0x0017191F);
        drawCenteredString(fontRendererObj, "A", width / 2, 30, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "ALPHA CLIENT", width / 2, 57, 0xFFFFFFFF);
        drawCenteredString(fontRendererObj, "MINECRAFT 1.8.9  •  FORGE", width / 2, 78, 0xFFB8BBC4);
        drawCenteredString(fontRendererObj, "A client built around the Alpha community", width / 2, 98, 0xFF858A96);
        super.drawScreen(mouseX, mouseY, partialTicks);
        GlStateManager.disableLighting();
        drawString(fontRendererObj, "AlphaClient", 8, height - 18, 0xFFFFD866);
        drawString(fontRendererObj, "v" + com.alpharip.staffmod.BuildInfo.VERSION, width - 74, height - 18, 0xFF858A96);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        switch (button.id) {
            case PLAY:
                mc.displayGuiScreen(new GuiSelectWorld(this));
                break;
            case MULTIPLAYER:
                mc.displayGuiScreen(new GuiMultiplayer(this));
                break;
            case MODULES:
                mc.displayGuiScreen(new AlphaModMenu(this));
                break;
            case OPTIONS:
                mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings));
                break;
            case PARTNER:
                // Connecting starts only after the custom main menu is open and the user clicks.
                mc.displayGuiScreen(new GuiConnecting(this, mc,
                        new ServerData("Alpha.Rip", "alpha.rip:25565", false)));
                break;
            case QUIT:
                mc.shutdown();
                break;
            default:
                break;
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}