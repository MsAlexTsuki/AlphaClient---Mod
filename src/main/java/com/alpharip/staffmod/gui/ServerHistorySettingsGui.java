// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.serverhistory.ServerHistoryManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/** Settings for automatic Server History recording. */
public final class ServerHistorySettingsGui extends AlphaGuiScreen {
    private static final int BACK = 1;
    private static final int TOGGLE = 2;

    private final GuiScreen parent;
    private int left, right, top, bottom;

    public ServerHistorySettingsGui(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        left = width / 2 - 150;
        right = left + 300;
        top = Math.max(12, height / 2 - 90);
        bottom = Math.min(height - 12, top + 180);

        buttonList.add(new AlphaButton(TOGGLE, left + 20, top + 68, 260, 24,
                "SERVER HISTORY: " + (ServerHistoryManager.isEnabled() ? "ON" : "OFF"),
                ServerHistoryManager.isEnabled()));
        buttonList.add(new AlphaButton(BACK, left + 10, bottom - 29, 80, 20, "BACK"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x18000000);
        drawRect(left, top, right, bottom, 0xD90A0C11);
        drawRect(left, top, right, top + 2, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "SERVER HISTORY SETTINGS",
                width / 2, top + 17, 0xFFF3F4F6);
        drawCenteredString(fontRendererObj, "Automatically save multiplayer servers you join",
                width / 2, top + 42, 0xFFB8BDC8);
        drawCenteredString(fontRendererObj, "Maximum saved servers: "
                + ServerHistoryManager.MAX_SAVED_SERVERS, width / 2, top + 111, 0xFF8E95A3);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button.id == BACK) {
            mc.displayGuiScreen(parent);
        } else if (button.id == TOGGLE) {
            ServerHistoryManager.setEnabled(!ServerHistoryManager.isEnabled());
            initGui();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
