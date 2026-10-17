package com.alpharip.staffmod.pvp;

import com.alpharip.staffmod.gui.ModernButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.io.IOException;

/**
 * Legacy PvP settings and capability inventory.
 * Only settings backed by real behavior are interactive; planned/server-bound
 * mechanics are displayed with their actual implementation requirements.
 */
public final class LegacyPvpSettingsGui extends GuiScreen {
    private final GuiScreen parent;
    private static final int TOGGLE_VIEW_BOBBING = 1;
    private static final int CLOSE = 2;
    private static final int TOGGLE_BLOCKHIT = 3;
    private static final int ROW_HEIGHT = 24;
    private static final int LIST_TOP = 124;
    private static final int LIST_BOTTOM_MARGIN = 42;
    private int scrollOffset;

    public LegacyPvpSettingsGui(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        LegacyPvpSettings.load();
        int centerX = width / 2;
        LegacyPvpFeatureCatalog.Feature[] features = LegacyPvpFeatureCatalog.getAll();
        int visibleRows = getVisibleRows();
        scrollOffset = Math.max(0, Math.min(scrollOffset, Math.max(0, features.length - visibleRows)));

        int cameraIndex = indexOf("camera-bobbing", features);
        int cameraY = LIST_TOP + (cameraIndex - scrollOffset) * ROW_HEIGHT;
        if (cameraIndex >= scrollOffset && cameraIndex < scrollOffset + visibleRows) {
            buttonList.add(new ModernButton(TOGGLE_VIEW_BOBBING, getToggleX(), cameraY + 1, 94, 20,
                    LegacyPvpSettings.isDisableViewBobbing() ? "BOBBING OFF" : "BOBBING ON"));
        }

        int blockhitIndex = indexOf("blockhit", features);
        int blockhitY = LIST_TOP + (blockhitIndex - scrollOffset) * ROW_HEIGHT;
        if (blockhitIndex >= scrollOffset && blockhitIndex < scrollOffset + visibleRows
                && LegacyPvpTransformer.isInstalled()) {
            buttonList.add(new ModernButton(TOGGLE_BLOCKHIT, getToggleX(), blockhitY + 1, 94, 20,
                    LegacyPvpSettings.isBlockhitEnabled() ? "BLOCKHIT: ON" : "BLOCKHIT: OFF"));
        }
        buttonList.add(new ModernButton(CLOSE, centerX - 70, height - 30, 140, 20, "BACK"));
    }

    private int indexOf(String id, LegacyPvpFeatureCatalog.Feature[] features) {
        for (int i = 0; i < features.length; i++) {
            if (features[i].id.equals(id)) return i;
        }
        return -1;
    }

    private int getVisibleRows() {
        return Math.max(1, (height - 183) / ROW_HEIGHT);
    }

    private int getToggleX() {
        int panelRight = Math.min(width - 8, width / 2 + 180);
        return panelRight - 102;
    }

    private String fitLabel(String label, int maxWidth) {
        if (label == null || fontRendererObj == null || maxWidth <= 0) {
            return "";
        }
        if (fontRendererObj.getStringWidth(label) <= maxWidth) {
            return label;
        }

        String ellipsis = "...";
        int textWidth = Math.max(0, maxWidth - fontRendererObj.getStringWidth(ellipsis));
        return fontRendererObj.trimStringToWidth(label, textWidth) + ellipsis;
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == TOGGLE_VIEW_BOBBING) {
            LegacyPvpSettings.setDisableViewBobbing(!LegacyPvpSettings.isDisableViewBobbing());
            initGui();
        } else if (button.id == TOGGLE_BLOCKHIT) {
            LegacyPvpSettings.setBlockhitEnabled(!LegacyPvpSettings.isBlockhitEnabled());
            initGui();
        } else if (button.id == CLOSE) {
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0 && mouseY >= LIST_TOP && mouseY < Math.min(height - 24 - 35,
                LIST_TOP + getVisibleRows() * ROW_HEIGHT)) {
            int row = (mouseY - LIST_TOP) / ROW_HEIGHT;
            int index = scrollOffset + row;
            LegacyPvpFeatureCatalog.Feature[] features = LegacyPvpFeatureCatalog.getAll();
            int panelLeft = Math.max(8, width / 2 - 180);
            int panelRight = Math.min(width - 8, width / 2 + 180);
            boolean insideRow = mouseX >= panelLeft + 8 && mouseX < panelRight - 8;
            boolean onToggle = mouseX >= getToggleX() && mouseX < getToggleX() + 94
                    && ((index == indexOf("camera-bobbing", features))
                    || (index == indexOf("blockhit", features) && LegacyPvpTransformer.isInstalled()));
            if (insideRow && !onToggle && index >= 0 && index < features.length) {
                mc.displayGuiScreen(new LegacyPvpFeatureGui(this, features[index].id));
                return;
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            int maxOffset = Math.max(0, LegacyPvpFeatureCatalog.getAll().length - getVisibleRows());
            scrollOffset = Math.max(0, Math.min(maxOffset, scrollOffset + (wheel < 0 ? 2 : -2)));
            initGui();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x88000000);
        int centerX = width / 2;
        int panelLeft = Math.max(8, centerX - 180);
        int panelRight = Math.min(width - 8, centerX + 180);
        int panelBottom = height - 24;
        drawRect(panelLeft, 12, panelRight, panelBottom, 0xDD0C0C0C);
        drawRect(panelLeft, 12, panelRight, 13, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "LEGACY PVP SETTINGS", centerX, 24, 0xFFFFFFFF);
        drawCenteredString(fontRendererObj, "Only implemented settings are interactive.", centerX, 39, 0xFFB8BDC8);
        drawCenteredString(fontRendererObj, "Client-side mechanics only.", centerX, 51, 0xFFB8BDC8);

        drawRect(panelLeft + 8, 68, panelRight - 8, 69, 0x553F424A);
        drawString(fontRendererObj, "FEATURE", panelLeft + 13, 76, 0xFFFFD866);
        drawString(fontRendererObj, "IMPLEMENTATION STATUS", centerX + 4, 76, 0xFFFFD866);

        LegacyPvpFeatureCatalog.Feature[] features = LegacyPvpFeatureCatalog.getAll();
        int visibleRows = getVisibleRows();
        int listBottom = Math.min(panelBottom - 35, LIST_TOP + visibleRows * ROW_HEIGHT);
        for (int row = 0; row < visibleRows; row++) {
            int index = scrollOffset + row;
            if (index >= features.length) break;
            LegacyPvpFeatureCatalog.Feature feature = features[index];
            int y = LIST_TOP + row * ROW_HEIGHT;
            if (y + ROW_HEIGHT > listBottom) break;

            int background = (row % 2 == 0) ? 0x332A2D35 : 0x221A1C22;
            drawRect(panelLeft + 8, y, panelRight - 8, y + ROW_HEIGHT - 1, background);
            drawString(fontRendererObj, fitLabel(feature.label, centerX - panelLeft - 20), panelLeft + 13, y + 4, 0xFFE7E9EE);

            String status = LegacyPvpFeatureCatalog.getStatusLabel(feature.status);
            int statusColor = feature.status == LegacyPvpFeatureCatalog.Status.CONFIGURABLE
                    ? 0xFF9CE2AC : 0xFFB8BDC8;
            int statusX = centerX + 4;
            if (feature.id.equals("camera-bobbing")) {
                status = "CONFIGURABLE";
                statusColor = 0xFF9CE2AC;
            } else if (feature.id.equals("blockhit")) {
                if (LegacyPvpTransformer.isInstalled()) {
                    status = "CONFIGURABLE";
                    statusColor = 0xFF9CE2AC;
                } else {
                    status = "HOOK NOT LOADED";
                    statusColor = 0xFFFFD866;
                }
            } else if (feature.id.equals("missed-swing-delay")) {
                if (LegacyPvpTransformer.isMissedSwingDelayInstalled()) {
                    status = "CONFIGURABLE";
                    statusColor = 0xFF9CE2AC;
                } else {
                    status = "HOOK NOT LOADED";
                    statusColor = 0xFFFFD866;
                }
            }
            drawString(fontRendererObj, status, statusX, y + 4, statusColor);
        }

        int total = features.length;
        int first = total == 0 ? 0 : scrollOffset + 1;
        int last = Math.min(total, scrollOffset + visibleRows);
        drawString(fontRendererObj, first + "-" + last + " of " + total,
                panelLeft + 13, panelBottom - 26, 0xFFB8BDC8);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
