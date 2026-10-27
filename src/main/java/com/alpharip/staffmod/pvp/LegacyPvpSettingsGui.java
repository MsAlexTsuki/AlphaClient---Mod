// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.pvp;

import com.alpharip.staffmod.gui.ModernButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.io.IOException;

/** Navigation grid for the Legacy PvP feature settings. */
public final class LegacyPvpSettingsGui extends GuiScreen {
    private static final int FEATURE_BUTTON_BASE = 1000;
    private static final int BACK = 2;
    private static final int GRID_TOP = 78;
    private static final int BUTTON_HEIGHT = 22;
    private static final int ROW_GAP = 7;
    private static final int COLUMN_GAP = 7;
    private static final int COLUMNS = 3;

    private final GuiScreen parent;
    private int scrollRow;

    public LegacyPvpSettingsGui(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        LegacyPvpSettings.load();

        LegacyPvpFeatureCatalog.Feature[] features = LegacyPvpFeatureCatalog.getAll();
        int rows = getTotalRows(features.length);
        scrollRow = Math.max(0, Math.min(scrollRow, Math.max(0, rows - getVisibleRows())));

        int panelLeft = getPanelLeft();
        int panelRight = getPanelRight();
        int innerLeft = panelLeft + 10;
        int innerWidth = panelRight - panelLeft - 20;
        int buttonWidth = (innerWidth - COLUMN_GAP * (COLUMNS - 1)) / COLUMNS;
        int visibleRows = getVisibleRows();

        for (int row = 0; row < visibleRows; row++) {
            int featureRow = scrollRow + row;
            for (int column = 0; column < COLUMNS; column++) {
                int index = featureRow * COLUMNS + column;
                if (index >= features.length) break;
                int x = innerLeft + column * (buttonWidth + COLUMN_GAP);
                int y = GRID_TOP + row * (BUTTON_HEIGHT + ROW_GAP);
                String label = fitLabel(features[index].label, buttonWidth - 12);
                buttonList.add(new ModernButton(FEATURE_BUTTON_BASE + index,
                        x, y, buttonWidth, BUTTON_HEIGHT, label));
            }
        }

        buttonList.add(new ModernButton(BACK, width / 2 - 70, height - 29, 140, 20, "BACK"));
    }

    private int getPanelLeft() {
        return Math.max(8, width / 2 - 190);
    }

    private int getPanelRight() {
        return Math.min(width - 8, width / 2 + 190);
    }

    private int getTotalRows(int featureCount) {
        return (featureCount + COLUMNS - 1) / COLUMNS;
    }

    private int getVisibleRows() {
        return Math.max(1, (height - GRID_TOP - 62) / (BUTTON_HEIGHT + ROW_GAP));
    }

    private String fitLabel(String label, int maxWidth) {
        if (label == null || fontRendererObj == null || maxWidth <= 0) return "";
        if (fontRendererObj.getStringWidth(label) <= maxWidth) return label;
        String ellipsis = "...";
        int textWidth = Math.max(0, maxWidth - fontRendererObj.getStringWidth(ellipsis));
        return fontRendererObj.trimStringToWidth(label, textWidth) + ellipsis;
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button.id == BACK) {
            mc.displayGuiScreen(parent);
            return;
        }

        int index = button.id - FEATURE_BUTTON_BASE;
        LegacyPvpFeatureCatalog.Feature[] features = LegacyPvpFeatureCatalog.getAll();
        if (index >= 0 && index < features.length) {
            mc.displayGuiScreen(new LegacyPvpFeatureGui(this, features[index].id));
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            LegacyPvpFeatureCatalog.Feature[] features = LegacyPvpFeatureCatalog.getAll();
            int maxScroll = Math.max(0, getTotalRows(features.length) - getVisibleRows());
            scrollRow = Math.max(0, Math.min(maxScroll, scrollRow + (wheel < 0 ? 1 : -1)));
            initGui();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x88000000);
        int panelLeft = getPanelLeft();
        int panelRight = getPanelRight();
        int panelBottom = height - 20;
        int centerX = width / 2;

        drawRect(panelLeft, 12, panelRight, panelBottom, 0xDD0C0C0C);
        drawRect(panelLeft, 12, panelRight, 14, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "LEGACY PVP", centerX, 24, 0xFFFFFFFF);
        drawCenteredString(fontRendererObj, "Select a module to open its settings", centerX, 41, 0xFFB8BDC8);
        drawRect(panelLeft + 10, 62, panelRight - 10, 63, 0x553F424A);

        LegacyPvpFeatureCatalog.Feature[] features = LegacyPvpFeatureCatalog.getAll();
        int totalRows = getTotalRows(features.length);
        int first = features.length == 0 ? 0 : scrollRow * COLUMNS + 1;
        int last = Math.min(features.length, (scrollRow + getVisibleRows()) * COLUMNS);
        drawString(fontRendererObj, first + "-" + last + " of " + features.length,
                panelLeft + 12, panelBottom - 15, 0xFFB8BDC8);
        if (totalRows > getVisibleRows()) {
            drawString(fontRendererObj, "Scroll to see more modules",
                    panelRight - 142, panelBottom - 15, 0xFFB8BDC8);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
