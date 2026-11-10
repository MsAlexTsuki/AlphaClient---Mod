package com.alpharip.staffmod.Viewer;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/** Standalone overview screen for locally observable player connection details. */
public final class PlayerConnectionViewerGui extends GuiScreen {
    private static final int BACK_BUTTON = 1;

    private final GuiScreen parent;
    private final PlayerConnectionViewer viewer;
    private ConnectionSnapshot snapshot;
    private int panelLeft;
    private int panelTop;
    private int panelRight;
    private int panelBottom;

    public PlayerConnectionViewerGui(GuiScreen parent, PlayerConnectionViewer viewer) {
        this.parent = parent;
        this.viewer = viewer;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int panelWidth = Math.min(420, Math.max(280, width - 24));
        int panelHeight = Math.min(230, Math.max(190, height - 24));
        panelLeft = (width - panelWidth) / 2;
        panelRight = panelLeft + panelWidth;
        panelTop = (height - panelHeight) / 2;
        panelBottom = panelTop + panelHeight;
        buttonList.add(new ViewerButton(BACK_BUTTON, panelLeft + 14,
                panelBottom - 31, 74, 20, "BACK"));
        refreshSnapshot();
    }

    @Override
    public void updateScreen() {
        refreshSnapshot();
    }

    private void refreshSnapshot() {
        snapshot = viewer.captureCurrentClientState();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawRect(panelLeft, panelTop, panelRight, panelBottom, 0xF00A0C11);
        drawRect(panelLeft, panelTop, panelRight, panelTop + 2, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "PLAYER CONNECTION VIEWER",
                width / 2, panelTop + 14, 0xFFF3F4F6);
        drawCenteredString(fontRendererObj, "LOCAL CLIENT CONNECTION OVERVIEW",
                width / 2, panelTop + 31, 0xFFFFD866);

        if (snapshot == null) {
            drawCenteredString(fontRendererObj, "Collecting connection information...",
                    width / 2, panelTop + 82, 0xFFB8BDC8);
        } else {
            int left = panelLeft + 20;
            int valueX = panelLeft + 142;
            int y = panelTop + 62;
            drawRow("STATUS", snapshot.getState().name(), left, valueX, y);
            drawRow("PLAYER", snapshot.getPlayerName(), left, valueX, y + 22);
            drawRow("CONNECTION", snapshot.getConnectionType(), left, valueX, y + 44);
            drawRow("SERVER ADDRESS", snapshot.getServerAddress(), left, valueX, y + 66);
            drawRow("SESSION DURATION",
                    ConnectionFormatter.duration(snapshot.getSessionDurationMillis()),
                    left, valueX, y + 88);
            drawRow("LAST UPDATED",
                    ConnectionFormatter.timestamp(snapshot.getCapturedAtMillis()),
                    left, valueX, y + 110);
        }

        drawCenteredString(fontRendererObj,
                "Only information available to this client is displayed.",
                width / 2, panelBottom - 54, 0xFF858D9B);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawRow(String label, String value, int labelX, int valueX, int y) {
        drawString(fontRendererObj, label, labelX, y, 0xFF9AA3B2);
        String fitted = fontRendererObj.trimStringToWidth(value,
                Math.max(20, panelRight - valueX - 18));
        drawString(fontRendererObj, fitted, valueX, y, 0xFFF3F4F6);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button != null && button.id == BACK_BUTTON) {
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            mc.displayGuiScreen(parent);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private static final class ViewerButton extends GuiButton {
        ViewerButton(int id, int x, int y, int width, int height, String label) {
            super(id, x, y, width, height, label);
        }

        @Override
        public void drawButton(net.minecraft.client.Minecraft minecraft, int mouseX, int mouseY) {
            if (!visible) return;
            hovered = mouseX >= xPosition && mouseY >= yPosition
                    && mouseX < xPosition + width && mouseY < yPosition + height;
            int background = hovered ? 0xFF292D36 : 0xFF171A21;
            int accent = hovered ? 0xFFFFD866 : 0x664B515D;
            drawRect(xPosition, yPosition, xPosition + width, yPosition + height, background);
            drawRect(xPosition, yPosition, xPosition + width, yPosition + 1, accent);
            drawCenteredString(minecraft.fontRendererObj, displayString,
                    xPosition + width / 2, yPosition + (height - 8) / 2,
                    hovered ? 0xFFFFD866 : 0xFFE6E8ED);
        }
    }
}
