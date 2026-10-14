package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.hud.BhopHud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/** AlphaClient settings hub; Minecraft's native options remain available as a sub-screen. */
public final class AlphaSettingsMenu extends GuiScreen {
    private static final int BACK = 1;
    private static final int BHOP_HUD = 2;
    private static final int RESET_BHOP_HUD = 3;
    private static final int MINECRAFT_OPTIONS = 4;

    private final GuiScreen parent;

    public AlphaSettingsMenu(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int center = width / 2;
        int buttonWidth = Math.min(320, width - 36);
        int left = center - buttonWidth / 2;
        int y = Math.max(140, height / 2 - 48);

        buttonList.add(new AlphaButton(BHOP_HUD, left, y, buttonWidth, 26,
                "BHOP HUD: " + (BhopHud.isEnabled() ? "ON" : "OFF")));
        buttonList.add(new AlphaButton(RESET_BHOP_HUD, left, y + 34, buttonWidth, 26,
                "RESET BHOP HUD POSITION"));
        buttonList.add(new AlphaButton(MINECRAFT_OPTIONS, left, y + 68, buttonWidth, 26,
                "MINECRAFT OPTIONS"));
        buttonList.add(new AlphaButton(BACK, 14, height - 32, 100, 20, "BACK"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawGradientRect(0, 0, width, height, 0xFF15171C, 0xFF090A0D);
        drawRect(width / 2 - 190, 34, width / 2 + 190, 92, 0x77202229);
        drawRect(width / 2 - 190, 34, width / 2 + 190, 35, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "ALPHA CLIENT", width / 2, 47, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "SETTINGS", width / 2, 65, 0xFFFFFFFF);
        drawCenteredString(fontRendererObj, "Client controls and HUD preferences", width / 2, 80, 0xFF858A96);

        int y = Math.max(140, height / 2 - 48);
        drawCenteredString(fontRendererObj, "BHOP HUD", width / 2, y - 15, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "Position: " + BhopHud.getHudX() + ", " + BhopHud.getHudY()
                + "    Scale: " + BhopHud.getHudScale(), width / 2, y + 105, 0xFF858A96);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        Minecraft minecraft = Minecraft.getMinecraft();
        switch (button.id) {
            case BACK:
                minecraft.displayGuiScreen(parent);
                break;
            case BHOP_HUD:
                BhopHud.toggle();
                initGui();
                break;
            case RESET_BHOP_HUD:
                BhopHud.resetPosition();
                initGui();
                break;
            case MINECRAFT_OPTIONS:
                minecraft.displayGuiScreen(new GuiOptions(this, minecraft.gameSettings));
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
