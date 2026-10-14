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
    private static final int SCALE_DOWN = 5;
    private static final int SCALE_UP = 6;
    private final GuiScreen parent;

    public AlphaSettingsMenu(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int center = width / 2;
        int panelWidth = Math.min(440, width - 32);
        int left = center - panelWidth / 2;
        int top = 112;
        int buttonWidth = panelWidth - 36;

        buttonList.add(new AlphaButton(BHOP_HUD, left + 18, top + 54, buttonWidth, 28,
                "BHOP HUD  /  " + (BhopHud.isEnabled() ? "ENABLED" : "DISABLED")));
        buttonList.add(new AlphaButton(RESET_BHOP_HUD, left + 18, top + 90, buttonWidth, 28,
                "RESET HUD POSITION"));
        buttonList.add(new AlphaButton(MINECRAFT_OPTIONS, left + 18, top + 126, buttonWidth, 28,
                "MINECRAFT OPTIONS"));
        buttonList.add(new AlphaButton(SCALE_DOWN, center - 80, top + 174, 48, 24, "−"));
        buttonList.add(new AlphaButton(SCALE_UP, center + 32, top + 174, 48, 24, "+"));
        buttonList.add(new AlphaButton(BACK, 14, height - 32, 100, 20, "BACK"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawGradientRect(0, 0, width, height, 0xFF181B22, 0xFF090A0E);
        int panelWidth = Math.min(440, width - 32);
        int left = width / 2 - panelWidth / 2;
        int right = left + panelWidth;
        int top = 28;
        drawRect(left, top, right, height - 44, 0xFF14171D);
        drawRect(left, top, right, top + 1, 0xFF343944);
        drawRect(left, top, left + 3, height - 44, 0xFFFFD866);

        drawString(fontRendererObj, "ALPHA CLIENT", left + 18, top + 14, 0xFFFFD866);
        drawString(fontRendererObj, "Settings", left + 18, top + 31, 0xFFF4F5F8);
        drawString(fontRendererObj, "Manage client features and interface preferences.",
                left + 18, top + 45, 0xFF9298A5);

        int sectionY = top + 76;
        drawString(fontRendererObj, "HUD & INTERFACE", left + 18, sectionY, 0xFF686E7A);
        drawRect(left + 18, sectionY + 13, right - 18, sectionY + 14, 0xFF292D36);

        int scaleY = top + 222;
        drawString(fontRendererObj, "HUD SCALE", left + 18, scaleY, 0xFFFFD866);
        drawString(fontRendererObj, BhopHud.getHudScale() + "%",
                width / 2, scaleY + 20, 0xFFF4F5F8);
        drawString(fontRendererObj, "Position  " + BhopHud.getHudX() + ", " + BhopHud.getHudY(),
                left + 18, scaleY + 39, 0xFF858B98);

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
            case SCALE_DOWN:
                BhopHud.setHudScale(BhopHud.getHudScale() - 10);
                initGui();
                break;
            case SCALE_UP:
                BhopHud.setHudScale(BhopHud.getHudScale() + 10);
                initGui();
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
