// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import com.alpharip.staffmod.founderfun.PixelGardenModuleSettings;
import com.alpharip.staffmod.founderfun.eastereggs.EasterEggManager;
import com.alpharip.staffmod.founderfun.eastereggs.EasterEggSettingsGui;

import java.io.IOException;

/** AlphaClient settings screen using Minecraft 1.8.9's native screen and button style. */
public final class AlphaSettingsMenu extends GuiScreen {
    private static final int PIXEL_GARDEN_TOGGLE = 1;
    private static final int EASTER_EGGS_TOGGLE = 2;
    private static final int EASTER_EGGS_SETTINGS = 3;
    private static final int DONE = 200;
    private final GuiScreen parent;

    public AlphaSettingsMenu(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int center = width / 2;
        int firstRow = height / 6 + 24;

        int row = firstRow;
        if (com.alpharip.staffmod.permissions.PermissionService.hasPixelGardenRankAccess()) {
            buttonList.add(new GuiButton(PIXEL_GARDEN_TOGGLE, center - 100, row, 200, 20,
                    getPixelGardenLabel()));
            row += 26;
        }
        buttonList.add(new GuiButton(EASTER_EGGS_TOGGLE, center - 100, row, 200, 20,
                "Easter Eggs: " + (EasterEggManager.isGloballyEnabled() ? "ON" : "OFF")));
        buttonList.add(new GuiButton(EASTER_EGGS_SETTINGS, center - 100, row + 26, 200, 20,
                "Easter Eggs Settings"));
        buttonList.add(new GuiButton(DONE, center - 100, row + 54, 200, 20, "Done"));
    }

    private static String getPixelGardenLabel() {
        return "Pixel Garden: " + (PixelGardenModuleSettings.getInstance().isEnabled() ? "ON" : "OFF");
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null || !button.enabled) return;
        if (button.id == PIXEL_GARDEN_TOGGLE) {
            if (!com.alpharip.staffmod.permissions.PermissionService.hasPixelGardenRankAccess()) {
                initGui();
                return;
            }
            PixelGardenModuleSettings settings = PixelGardenModuleSettings.getInstance();
            settings.setEnabled(!settings.isEnabled());
            button.displayString = getPixelGardenLabel();
        } else if (button.id == EASTER_EGGS_TOGGLE) {
            EasterEggManager.setGloballyEnabled(!EasterEggManager.isGloballyEnabled());
            button.displayString = "Easter Eggs: " + (EasterEggManager.isGloballyEnabled() ? "ON" : "OFF");
        } else if (button.id == EASTER_EGGS_SETTINGS) {
            Minecraft.getMinecraft().displayGuiScreen(new EasterEggSettingsGui(this));
        } else if (button.id == DONE) {
            Minecraft.getMinecraft().displayGuiScreen(parent);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "Alpha Settings", width / 2, 15, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
