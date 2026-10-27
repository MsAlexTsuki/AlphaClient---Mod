// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.founderfun;

import com.alpharip.staffmod.gui.AlphaButton;
import com.alpharip.staffmod.permissions.PermissionService;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/**
 * Initial Pixel Garden screen entry point.
 * The idle-game systems will be implemented separately from this menu entry.
 */
public final class PixelGardenGUI extends GuiScreen {
    private static final int BACK_BUTTON = 1;
    private final GuiScreen parent;

    public PixelGardenGUI(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        buttonList.add(new AlphaButton(BACK_BUTTON, width / 2 - 45, height - 38, 90, 20, "BACK"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button != null && button.id == BACK_BUTTON) {
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0xD90A0C11);
        drawRect(width / 2 - 150, 34, width / 2 + 150, 36, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "PIXEL GARDEN", width / 2, 48, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "Your garden starts here.", width / 2, 78, 0xFFF0F1F4);
        drawCenteredString(fontRendererObj, "Planting, growing, harvesting and selling will be added", width / 2, 104, 0xFFB8BDC8);
        drawCenteredString(fontRendererObj, "as the idle-game systems are implemented.", width / 2, 116, 0xFFB8BDC8);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
