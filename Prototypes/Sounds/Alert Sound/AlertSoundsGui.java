package com.alpharip.staffmod.sounds.alertsound;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/**
 * Standalone Minecraft 1.8.9 configuration screen for AlertSounds.
 * The screen edits local event-to-sound mappings and can preview a sound.
 */
public final class AlertSoundsGui extends GuiScreen {
    private static final int TOGGLE_BUTTON = 1;
    private static final int SAVE_BUTTON = 2;
    private static final int REMOVE_BUTTON = 3;
    private static final int TEST_BUTTON = 4;
    private static final int BACK_BUTTON = 5;

    private final GuiScreen parent;
    private final AlertSounds settings;

    private GuiTextField eventField;
    private GuiTextField soundField;
    private GuiTextField volumeField;
    private String selectedEvent;
    private String feedback = "";
    private int scrollOffset;

    public AlertSoundsGui(GuiScreen parent, AlertSounds settings) {
        if (settings == null) {
            throw new IllegalArgumentException("Alert Sounds settings are required.");
        }
        this.parent = parent;
        this.settings = settings;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        buttonList.clear();

        int panelWidth = Math.min(390, Math.max(300, width - 24));
        int panelHeight = Math.min(height - 18, 390);
        int left = (width - panelWidth) / 2;
        int top = (height - panelHeight) / 2;
        int fieldWidth = panelWidth - 32;

        eventField = new GuiTextField(0, fontRendererObj, left + 16, top + 72, fieldWidth, 18);
        eventField.setMaxStringLength(64);
        soundField = new GuiTextField(1, fontRendererObj, left + 16, top + 112, fieldWidth, 18);
        soundField.setMaxStringLength(128);
        volumeField = new GuiTextField(2, fontRendererObj, left + 16, top + 152, 64, 18);
        volumeField.setMaxStringLength(5);
        volumeField.setText(Float.toString(settings.getVolume()));

        buttonList.add(new GuiButton(TOGGLE_BUTTON, left + 16, top + 34, fieldWidth, 20,
                "Alert Sounds: " + onOff(settings.isEnabled())));
        buttonList.add(new GuiButton(SAVE_BUTTON, left + 16, top + 177, fieldWidth / 2 - 3, 20,
                "Save Mapping"));
        buttonList.add(new GuiButton(REMOVE_BUTTON, left + 19 + fieldWidth / 2, top + 177,
                fieldWidth / 2 - 3, 20, "Remove Mapping"));
        buttonList.add(new GuiButton(TEST_BUTTON, left + 16, top + 202, fieldWidth, 20,
                "Test Sound"));
        buttonList.add(new GuiButton(BACK_BUTTON, left + panelWidth - 92, top + panelHeight - 30,
                76, 20, "Back"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;

        if (button.id == TOGGLE_BUTTON) {
            settings.setEnabled(!settings.isEnabled());
            button.displayString = "Alert Sounds: " + onOff(settings.isEnabled());
            feedback = "Alert Sounds state saved.";
        } else if (button.id == SAVE_BUTTON) {
            String event = eventField.getText().trim();
            String sound = soundField.getText().trim();
            if (event.isEmpty() || sound.isEmpty()) {
                feedback = "Enter both an event name and a sound identifier.";
                return;
            }
            try {
                float volume = Float.parseFloat(volumeField.getText().trim());
                settings.setVolume(volume);
                settings.setSound(event, sound);
                selectedEvent = event;
                feedback = "Mapping saved.";
            } catch (NumberFormatException exception) {
                feedback = "Volume must be a number from 0.0 to 1.0.";
            } catch (IllegalArgumentException exception) {
                feedback = exception.getMessage();
            }
        } else if (button.id == REMOVE_BUTTON) {
            String event = eventField.getText().trim();
            if (event.isEmpty()) {
                feedback = "Select or enter an event name first.";
            } else if (settings.removeSound(event)) {
                selectedEvent = null;
                feedback = "Mapping removed.";
            } else {
                feedback = "No mapping exists for that event.";
            }
        } else if (button.id == TEST_BUTTON) {
            String sound = soundField.getText().trim();
            try {
                float volume = Float.parseFloat(volumeField.getText().trim());
                settings.setVolume(volume);
                boolean played = settings.testSound(sound, Minecraft.getMinecraft().thePlayer);
                feedback = played ? "Sound preview requested." : "Enter a sound identifier first.";
            } catch (NumberFormatException exception) {
                feedback = "Volume must be a number from 0.0 to 1.0.";
            } catch (IllegalArgumentException exception) {
                feedback = exception.getMessage();
            }
        } else if (button.id == BACK_BUTTON) {
            Minecraft.getMinecraft().displayGuiScreen(parent);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (eventField.textboxKeyTyped(typedChar, keyCode)
                || soundField.textboxKeyTyped(typedChar, keyCode)
                || volumeField.textboxKeyTyped(typedChar, keyCode)) {
            return;
        }
        if (keyCode == Keyboard.KEY_ESCAPE) {
            Minecraft.getMinecraft().displayGuiScreen(parent);
        } else if (keyCode == Keyboard.KEY_UP) {
            scrollOffset = Math.max(0, scrollOffset - 1);
        } else if (keyCode == Keyboard.KEY_DOWN) {
            scrollOffset = Math.min(maxScroll(), scrollOffset + 1);
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        eventField.mouseClicked(mouseX, mouseY, mouseButton);
        soundField.mouseClicked(mouseX, mouseY, mouseButton);
        volumeField.mouseClicked(mouseX, mouseY, mouseButton);

        int panelWidth = Math.min(390, Math.max(300, width - 24));
        int left = (width - panelWidth) / 2;
        int panelHeight = Math.min(height - 18, 390);
        int top = (height - panelHeight) / 2;
        int listTop = top + 251;
        int listBottom = top + panelHeight - 39;
        int rowHeight = 14;
        if (mouseX >= left + 16 && mouseX <= left + panelWidth - 16
                && mouseY >= listTop && mouseY < listBottom) {
            int row = (mouseY - listTop) / rowHeight;
            List<String> events = new ArrayList<String>(settings.getMappings().keySet());
            int index = scrollOffset + row;
            if (index >= 0 && index < events.size()) {
                selectedEvent = events.get(index);
                eventField.setText(selectedEvent);
                soundField.setText(settings.getSound(selectedEvent));
                feedback = "Selected mapping: " + selectedEvent;
            }
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            scrollOffset = Math.max(0, Math.min(maxScroll(), scrollOffset + (wheel > 0 ? -1 : 1)));
        }
    }

    private int maxScroll() {
        return Math.max(0, settings.getMappings().size() - visibleRows());
    }

    private int visibleRows() {
        int panelHeight = Math.min(height - 18, 390);
        return Math.max(1, (panelHeight - 296) / 14);
    }

    @Override
    public void updateScreen() {
        eventField.updateCursorCounter();
        soundField.updateCursorCounter();
        volumeField.updateCursorCounter();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        int panelWidth = Math.min(390, Math.max(300, width - 24));
        int panelHeight = Math.min(height - 18, 390);
        int left = (width - panelWidth) / 2;
        int top = (height - panelHeight) / 2;
        int right = left + panelWidth;
        int bottom = top + panelHeight;

        drawRect(left, top, right, bottom, 0xF00B0D12);
        drawRect(left, top, right, top + 2, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "ALERT SOUNDS", width / 2, top + 10, 0xFFF2F3F5);
        drawString(fontRendererObj, "EVENT NAME", left + 16, top + 62, 0xFFB8BDC8);
        drawRect(left + 15, top + 71, right - 15, top + 91, 0xFF11141B);
        eventField.drawTextBox();
        drawString(fontRendererObj, "SOUND IDENTIFIER", left + 16, top + 102, 0xFFB8BDC8);
        drawRect(left + 15, top + 111, right - 15, top + 131, 0xFF11141B);
        soundField.drawTextBox();
        drawString(fontRendererObj, "VOLUME (0.0 - 1.0)", left + 16, top + 142, 0xFFB8BDC8);
        drawRect(left + 15, top + 151, left + 87, top + 171, 0xFF11141B);
        volumeField.drawTextBox();

        int listTop = top + 251;
        int listBottom = bottom - 39;
        drawString(fontRendererObj, "EVENT MAPPINGS (" + settings.getMappings().size() + ")",
                left + 16, listTop - 13, 0xFFFFD866);
        drawRect(left + 15, listTop, right - 15, listBottom, 0x6614171E);

        List<Map.Entry<String, String>> entries =
                new ArrayList<Map.Entry<String, String>>(settings.getMappings().entrySet());
        int visible = Math.max(0, (listBottom - listTop - 6) / 14);
        int start = Math.min(scrollOffset, Math.max(0, entries.size() - visible));
        if (entries.isEmpty()) {
            drawCenteredString(fontRendererObj, "No event mappings configured.",
                    width / 2, listTop + 8, 0xFF858D9B);
        } else {
            for (int i = start; i < entries.size() && i < start + visible; i++) {
                Map.Entry<String, String> entry = entries.get(i);
                int y = listTop + 5 + (i - start) * 14;
                boolean selected = entry.getKey().equals(selectedEvent);
                if (selected) {
                    drawRect(left + 17, y - 2, right - 17, y + 11, 0x663D3420);
                }
                String line = entry.getKey() + "  ->  " + entry.getValue();
                line = fontRendererObj.trimStringToWidth(line, panelWidth - 48);
                drawString(fontRendererObj, line, left + 22, y,
                        selected ? 0xFFFFD866 : 0xFFE0E3E8);
            }
        }

        drawString(fontRendererObj, feedback, left + 16, bottom - 27, 0xFFFFD866);
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
