package com.alpharip.staffmod.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.io.IOException;

/** Browser for the planned AlphaClient Quality of Life features. */
public final class QualityGui extends GuiScreen {
    private static final int BACK = 1;
    private static final int ACCENT = 0xFFFFD866;
    private static final String[] FEATURES = {
            "Alpha Module Manager", "Playtime Tracker", "Auto GG", "Keybind Profiles",
            "Auto Respawn", "AlphaClient Core Experience", "AFK Detector", "Mod Profiles",
            "Config Manager", "Auto Reconnect", "Alpha System Monitor", "Alpha Crash Reporter",
            "Alpha Update Checker", "Alpha Control Center", "Keybind Manager", "Config Profiles",
            "Session Timer", "Alpha Feature Flags"
    };

    private final GuiScreen parent;
    private int scrollOffset;
    private int visibleRows;
    private int panelLeft, panelRight, panelTop, panelBottom;

    public QualityGui(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        panelLeft = Math.max(12, width / 2 - Math.min(230, width / 2 - 12));
        panelRight = Math.min(width - 12, panelLeft + 460);
        panelTop = Math.max(12, height / 2 - Math.min(150, height / 2 - 12));
        panelBottom = Math.min(height - 12, panelTop + 300);
        visibleRows = Math.max(1, (panelBottom - panelTop - 92) / 20);
        int maxScroll = Math.max(0, FEATURES.length - visibleRows);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
        buttonList.add(new GuiButton(BACK, panelLeft + 10, panelBottom - 29, 90, 20, "BACK"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x18000000);
        drawRect(panelLeft, panelTop, panelRight, panelBottom, 0xD90A0C11);
        drawRect(panelLeft, panelTop, panelRight, panelTop + 2, ACCENT);
        drawCenteredString(fontRendererObj, "QUALITY", width / 2, panelTop + 13, 0xFFF3F4F6);
        drawCenteredString(fontRendererObj, "QUALITY OF LIFE FEATURES", width / 2, panelTop + 30, ACCENT);
        drawString(fontRendererObj, "Ideas from the AlphaClient roadmap", panelLeft + 12, panelTop + 49, 0xFFB8BDC8);

        int listTop = panelTop + 68;
        int listBottom = panelBottom - 38;
        int rowHeight = 20;
        for (int i = scrollOffset; i < FEATURES.length && i < scrollOffset + visibleRows; i++) {
            int y = listTop + (i - scrollOffset) * rowHeight;
            drawRect(panelLeft + 10, y, panelRight - 10, y + 17, 0xB5161820);
            drawString(fontRendererObj, FEATURES[i], panelLeft + 17, y + 5, 0xFFE7E9EE);
            String status = "Config Profiles".equals(FEATURES[i]) ? "AVAILABLE" : "PLANNED";
            int statusColor = "AVAILABLE".equals(status) ? ACCENT : 0xFFB8BDC8;
            drawString(fontRendererObj, status, panelRight - 17 - fontRendererObj.getStringWidth(status), y + 5, statusColor);
        }
        if (FEATURES.length > visibleRows) {
            drawString(fontRendererObj, "Scroll to view all " + FEATURES.length + " ideas", panelLeft + 112,
                    panelBottom - 23, 0xFF8E95A3);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button != null && button.id == BACK) {
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            int maxScroll = Math.max(0, FEATURES.length - visibleRows);
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset + (wheel < 0 ? 2 : -2)));
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
