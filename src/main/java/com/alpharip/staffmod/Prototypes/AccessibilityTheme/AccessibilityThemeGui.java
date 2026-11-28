package com.alpharip.staffmod.accessibilitytheme;

import java.io.IOException;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

/**
 * Standalone Minecraft 1.8.9 prototype screen for the existing
 * AccessibilityTheme enabled flag and generic record collection.
 *
 * This screen does not change Minecraft rendering, fonts, colors, contrast,
 * or any other visual accessibility behavior.
 */
public final class AccessibilityThemeGui extends GuiScreen {
    private static final int TOGGLE_BUTTON = 1;
    private static final int ADD_BUTTON = 2;
    private static final int CLEAR_BUTTON = 3;
    private static final int BACK_BUTTON = 4;

    private final GuiScreen parent;
    private final AccessibilityThemeSettings settings;
    private GuiTextField recordField;
    private String feedback = "";
    private int scrollOffset;

    public AccessibilityThemeGui(GuiScreen parent) {
        this(parent, new AccessibilityThemeSettings());
    }

    public AccessibilityThemeGui(GuiScreen parent, AccessibilityThemeSettings settings) {
        if (settings == null) {
            throw new IllegalArgumentException("Accessibility theme settings are required.");
        }
        this.parent = parent;
        this.settings = settings;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        buttonList.clear();

        int panelWidth = Math.min(360, Math.max(280, width - 24));
        int left = (width - panelWidth) / 2;
        int fieldWidth = panelWidth - 32;

        recordField = new GuiTextField(0, fontRendererObj, left + 16, 91, fieldWidth, 18);
        recordField.setMaxStringLength(80);

        buttonList.add(new GuiButton(TOGGLE_BUTTON, left + 16, 52, fieldWidth, 20,
                "Accessibility Theme: " + onOff(settings.isEnabled())));
        buttonList.add(new GuiButton(ADD_BUTTON, left + 16, 116, fieldWidth / 2 - 3, 20, "Add Record"));
        buttonList.add(new GuiButton(CLEAR_BUTTON, left + 19 + fieldWidth / 2, 116,
                fieldWidth / 2 - 3, 20, "Clear Records"));
        buttonList.add(new GuiButton(BACK_BUTTON, left + panelWidth - 92, height - 30, 76, 20, "Back"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;

        if (button.id == TOGGLE_BUTTON) {
            settings.setEnabled(!settings.isEnabled());
            button.displayString = "Accessibility Theme: " + onOff(settings.isEnabled());
            feedback = "Enabled state updated.";
        } else if (button.id == ADD_BUTTON) {
            String value = recordField.getText().trim();
            if (value.isEmpty()) {
                feedback = "Enter a record first.";
            } else {
                settings.addRecord(value);
                recordField.setText("");
                scrollOffset = Math.max(0, settings.getRecords().size() - visibleRows());
                feedback = "Record added.";
            }
        } else if (button.id == CLEAR_BUTTON) {
            settings.clearRecords();
            scrollOffset = 0;
            feedback = "Records cleared.";
        } else if (button.id == BACK_BUTTON) {
            mc.displayGuiScreen(parent);
        }
    }

    private int visibleRows() {
        return Math.max(1, (height - 190) / 14);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (recordField.textboxKeyTyped(typedChar, keyCode)) return;
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(parent);
        } else if (keyCode == Keyboard.KEY_UP) {
            scrollOffset = Math.max(0, scrollOffset - 1);
        } else if (keyCode == Keyboard.KEY_DOWN) {
            scrollOffset = Math.min(Math.max(0, settings.getRecords().size() - visibleRows()),
                    scrollOffset + 1);
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        recordField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = org.lwjgl.input.Mouse.getEventDWheel();
        if (wheel != 0) {
            int max = Math.max(0, settings.getRecords().size() - visibleRows());
            scrollOffset = Math.max(0, Math.min(max, scrollOffset + (wheel > 0 ? -1 : 1)));
        }
    }

    @Override
    public void updateScreen() {
        recordField.updateCursorCounter();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        int panelWidth = Math.min(360, Math.max(280, width - 24));
        int panelHeight = Math.min(height - 24, 270);
        int left = (width - panelWidth) / 2;
        int top = (height - panelHeight) / 2;
        int right = left + panelWidth;
        int bottom = top + panelHeight;

        drawRect(left, top, right, bottom, 0xF00B0D12);
        drawRect(left, top, right, top + 2, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "ACCESSIBILITY THEME", width / 2, top + 13, 0xFFF2F3F5);
        drawCenteredString(fontRendererObj, "Prototype settings", width / 2, top + 29, 0xFFB8BDC8);

        drawString(fontRendererObj, "MODULE STATE", left + 16, 43, 0xFFB8BDC8);
        drawString(fontRendererObj, "RECORD VALUE", left + 16, 81, 0xFFB8BDC8);
        drawRect(left + 15, 90, right - 15, 110, 0xFF11141B);
        recordField.drawTextBox();

        List<String> records = settings.getRecords();
        int listTop = 151;
        int listBottom = bottom - 43;
        drawString(fontRendererObj, "RECORDS (" + records.size() + ")", left + 16, 143, 0xFFFFD866);
        drawRect(left + 15, listTop, right - 15, listBottom, 0x6614171E);

        int visible = Math.max(0, (listBottom - listTop - 8) / 14);
        int start = Math.min(scrollOffset, Math.max(0, records.size() - visible));
        if (records.isEmpty()) {
            drawCenteredString(fontRendererObj, "No records added.", width / 2, listTop + 12, 0xFF858D9B);
        } else {
            for (int i = start; i < records.size() && i < start + visible; i++) {
                String value = records.get(i);
                value = fontRendererObj.trimStringToWidth(value, panelWidth - 48);
                drawString(fontRendererObj, value, left + 22, listTop + 6 + (i - start) * 14,
                        0xFFE0E3E8);
            }
        }

        drawString(fontRendererObj, feedback, left + 16, bottom - 31, 0xFFFFD866);
        drawCenteredString(fontRendererObj,
                "No visual theme or Minecraft rendering changes are applied.",
                width / 2, height - 12, 0xFF858D9B);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private static String onOff(boolean value) {
        return value ? "On" : "Off";
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
