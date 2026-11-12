package com.alpharip.staffmod.Viewer.PlayerDirectionViewer;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.List;

/** Standalone live list of player facing directions known to the local client. */
public final class PlayerDirectionViewerGui extends GuiScreen {
    private static final int BACK_BUTTON = 1;
    private static final int ROW_HEIGHT = 18;

    private final GuiScreen parent;
    private final PlayerDirectionViewer viewer;
    private int panelLeft;
    private int panelTop;
    private int panelRight;
    private int panelBottom;

    public PlayerDirectionViewerGui(GuiScreen parent, PlayerDirectionViewer viewer) {
        this.parent = parent;
        this.viewer = viewer;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int panelWidth = Math.min(440, Math.max(300, width - 24));
        int panelHeight = Math.min(300, Math.max(190, height - 24));
        panelLeft = (width - panelWidth) / 2;
        panelRight = panelLeft + panelWidth;
        panelTop = (height - panelHeight) / 2;
        panelBottom = panelTop + panelHeight;
        buttonList.add(new DirectionButton(BACK_BUTTON, panelLeft + 14,
                panelBottom - 29, 74, 20, "BACK"));
    }

    @Override
    public void updateScreen() {
        viewer.captureVisiblePlayers();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawRect(panelLeft, panelTop, panelRight, panelBottom, 0xF00A0C11);
        drawRect(panelLeft, panelTop, panelRight, panelTop + 2, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "PLAYER DIRECTION VIEWER",
                width / 2, panelTop + 13, 0xFFF3F4F6);
        drawCenteredString(fontRendererObj, "PLAYERS CURRENTLY LOADED BY YOUR CLIENT",
                width / 2, panelTop + 29, 0xFFFFD866);

        drawString(fontRendererObj, "PLAYER", panelLeft + 18, panelTop + 49, 0xFF9AA3B2);
        drawString(fontRendererObj, "FACING", panelLeft + 164, panelTop + 49, 0xFF9AA3B2);
        drawString(fontRendererObj, "ANGLES", panelLeft + 218, panelTop + 49, 0xFF9AA3B2);
        drawString(fontRendererObj, "DIST.", panelRight - 52, panelTop + 49, 0xFF9AA3B2);
        drawRect(panelLeft + 14, panelTop + 62, panelRight - 14,
                panelTop + 63, 0x664B515D);

        List<PlayerDirectionSnapshot> players = viewer.getSnapshots();
        int availableRows = Math.max(0, (panelBottom - panelTop - 112) / ROW_HEIGHT);
        if (players.isEmpty()) {
            drawCenteredString(fontRendererObj, "No other loaded players found.",
                    width / 2, panelTop + 91, 0xFFB8BDC8);
        } else {
            int count = Math.min(players.size(), availableRows);
            for (int i = 0; i < count; i++) {
                PlayerDirectionSnapshot player = players.get(i);
                int y = panelTop + 72 + i * ROW_HEIGHT;
                String name = fontRendererObj.trimStringToWidth(
                        player.getPlayerName(), 138);
                drawString(fontRendererObj, name, panelLeft + 18, y, 0xFFF3F4F6);
                drawString(fontRendererObj,
                        PlayerDirectionFormatter.compass(player.getYaw()),
                        panelLeft + 164, y, 0xFFFFD866);
                drawString(fontRendererObj,
                        PlayerDirectionFormatter.angles(player.getYaw(), player.getPitch()),
                        panelLeft + 218, y, 0xFFD5D9E0);
                drawString(fontRendererObj,
                        String.format(java.util.Locale.ROOT, "%.1fm", player.getDistance()),
                        panelRight - 52, y, 0xFFD5D9E0);
            }
            if (players.size() > count) {
                drawCenteredString(fontRendererObj,
                        (players.size() - count) + " more player(s) not shown",
                        width / 2, panelBottom - 49, 0xFF858D9B);
            }
        }

        drawCenteredString(fontRendererObj,
                "Direction data is limited to entities loaded by this client.",
                width / 2, panelBottom - 40, 0xFF858D9B);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button != null && button.id == BACK_BUTTON) mc.displayGuiScreen(parent);
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

    private static final class DirectionButton extends GuiButton {
        DirectionButton(int id, int x, int y, int width, int height, String label) {
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
