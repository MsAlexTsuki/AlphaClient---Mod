package com.alpharip.staffmod.RecordingStudio.ui;

import com.alpharip.staffmod.RecordingStudio.core.RecordingStudioController;
import com.alpharip.staffmod.RecordingStudio.export.RecordingStudioExportSettings;
import com.alpharip.staffmod.RecordingStudio.settings.RecordingStudioPreferences;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import java.io.IOException;

/** Compact, searchable Alpha-style preferences workspace for Recording Studio. */
public final class RecordingStudioSettingsScreen extends GuiScreen {
    private static final int GENERAL = 0, RECORDING = 1, EXPORT = 2, AUDIO = 3, DIAGNOSTICS = 4;
    private static final String[] CATEGORIES = { "General", "Recording", "Export", "Audio", "Diagnostics" };

    private final RecordingStudioScreen parent;
    private final RecordingStudioController controller;
    private final RecordingStudioPreferences preferences;
    private int selectedCategory = GENERAL;
    private GuiTextField searchField;
    private String search = "";

    public RecordingStudioSettingsScreen(RecordingStudioScreen parent, RecordingStudioController controller) {
        if (parent == null || controller == null) throw new IllegalArgumentException("Settings dependencies are required");
        this.parent = parent;
        this.controller = controller;
        this.preferences = RecordingStudioPreferences.load(net.minecraft.client.Minecraft.getMinecraft().mcDataDir);
        controller.getExportSettings().setResolution(preferences.getResolution());
        controller.getExportSettings().setQuality(preferences.getQuality());
    }

    @Override public boolean doesGuiPauseGame() { return false; }

    @Override
    public void initGui() {
        buttonList.clear();
        int sideX = 14;
        int sideY = 76;
        for (int i = 0; i < CATEGORIES.length; i++) {
            buttonList.add(new AlphaStudioButton(100 + i, sideX, sideY + i * 23, 112, 18,
                    CATEGORIES[i], i == selectedCategory));
        }
        buttonList.add(new AlphaStudioButton(10, width - 112, 18, 96, 17,
                preferences.isAdvancedMode() ? "ADVANCED" : "BASIC", preferences.isAdvancedMode()));
        buttonList.add(new AlphaStudioButton(11, width - 112, height - 31, 96, 17, "BACK"));
        searchField = new GuiTextField(50, fontRendererObj, 150, 49, Math.max(120, width - 410), 17);
        searchField.setMaxStringLength(48);
        searchField.setText(search);
        addSettingButtons();
    }

    private void addSettingButtons() {
        int contentX = 164;
        int colWidth = Math.max(320, width - 200);
        int controlRight = contentX + colWidth - 12;
        if (selectedCategory == RECORDING) {
            addSmall(200, controlRight - 80, 111, 38, "-100");
            addSmall(201, controlRight - 38, 111, 38, "+100");
            if (preferences.isAdvancedMode()) {
                addSmall(202, controlRight - 80, 165, 38, "-1s");
                addSmall(203, controlRight - 38, 165, 38, "+1s");
            }
        } else if (selectedCategory == EXPORT) {
            addSmall(210, controlRight - 104, 111, 104, controller.getExportSettings().getResolution().getLabel());
            addSmall(211, controlRight - 104, 165, 104, controller.getExportSettings().getQuality().getLabel());
        } else if (selectedCategory == AUDIO) {
            addSmall(220, controlRight - 104, 111, 104, "NEXT DEVICE");
            addSmall(221, controlRight - 104, 165, 104, "REFRESH");
        }
    }

    private void addSmall(int id, int x, int y, int w, String label) {
        buttonList.add(new AlphaStudioButton(id, x, y, w, 16, label));
    }

    private void persist() {
        RecordingStudioExportSettings settings = controller.getExportSettings();
        preferences.setResolution(settings.getResolution());
        preferences.setQuality(settings.getQuality());
        preferences.save(mc.mcDataDir);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id >= 100 && button.id < 100 + CATEGORIES.length) {
            selectedCategory = button.id - 100;
            initGui();
            return;
        }
        switch (button.id) {
            case 10:
                preferences.setAdvancedMode(!preferences.isAdvancedMode());
                persist();
                initGui();
                return;
            case 11:
                mc.displayGuiScreen(parent);
                return;
            case 200: controller.adjustCaptureInterval(-100L); break;
            case 201: controller.adjustCaptureInterval(100L); break;
            case 202: controller.adjustCaptureInterval(-1000L); break;
            case 203: controller.adjustCaptureInterval(1000L); break;
            case 210: controller.getExportSettings().cycleResolution(); persist(); break;
            case 211: controller.getExportSettings().cycleQuality(); persist(); break;
            case 220: controller.selectNextMicrophone(); break;
            case 221: controller.refreshMicrophones(); break;
            default: return;
        }
        initGui();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            mc.displayGuiScreen(parent);
            return;
        }
        if (searchField != null) {
            searchField.textboxKeyTyped(typedChar, keyCode);
            search = searchField.getText();
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (searchField != null) searchField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private boolean matches(String value) {
        return search == null || search.trim().isEmpty()
                || value.toLowerCase(java.util.Locale.ROOT).contains(search.trim().toLowerCase(java.util.Locale.ROOT));
    }

    private void applySearchVisibility() {
        for (Object item : buttonList) {
            GuiButton control = (GuiButton) item;
            if (control.id == 200 || control.id == 201) control.visible = matches("Capture interval");
            else if (control.id == 202 || control.id == 203) {
                control.visible = preferences.isAdvancedMode() && matches("Capture interval range");
            } else if (control.id == 210) control.visible = matches("Output resolution");
            else if (control.id == 211) control.visible = matches("Encoding quality");
            else if (control.id == 220 || control.id == 221) control.visible = matches("Microphone input");
        }
    }

    private String compactValue(String value) {
        if (value == null) return "";
        return value.length() <= 24 ? value : value.substring(0, 21) + "...";
    }

    private void section(String title, int x, int y, int width) {
        drawString(fontRendererObj, title.toUpperCase(java.util.Locale.ROOT), x, y, 0xFFFFC83D);
        Gui.drawRect(x, y + 12, x + width, y + 13, 0xFF414141);
    }

    private void setting(String title, String description, String value, int x, int y, int controlRight) {
        if (!matches(title) && !matches(description)) return;
        drawString(fontRendererObj, title, x, y, 0xFFE8E8E8);
        drawString(fontRendererObj, description, x, y + 11, 0xFF888888);
        if (value != null) {
            String compact = compactValue(value);
            int valueWidth = fontRendererObj.getStringWidth(compact);
            drawString(fontRendererObj, compact, controlRight - valueWidth, y, 0xFFFFD65A);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        Gui.drawRect(0, 0, width, height, 0xF00D0F12);
        Gui.drawRect(0, 0, width, 39, 0xFF111316);
        Gui.drawRect(0, 38, width, 40, 0xFFC88F20);
        Gui.drawRect(10, 48, 136, height - 15, 0xE817191D);
        Gui.drawRect(145, 45, width - 10, height - 15, 0xE817191D);
        Gui.drawRect(10, 48, 136, 50, 0xFFFFC83D);
        Gui.drawRect(145, 45, width - 10, 47, 0xFFFFC83D);

        applySearchVisibility();
        drawString(fontRendererObj, "ALPHA CLIENT", 16, 12, 0xFFAAAAAA);
        drawString(fontRendererObj, "/", 93, 12, 0xFF666666);
        drawString(fontRendererObj, "RECORDING STUDIO", 104, 12, 0xFFFFC83D);
        drawString(fontRendererObj, "SETTINGS", 16, 57, 0xFFFFC83D);
        drawString(fontRendererObj, "PREFERENCES", 154, 30, 0xFFEAEAEA);
        if (searchField != null) {
            searchField.drawTextBox();
            if (searchField.getText().isEmpty()) drawString(fontRendererObj, "Search settings...", 155, 54, 0xFF777777);
        }

        String mode = preferences.isAdvancedMode() ? "ADVANCED MODE" : "BASIC MODE";
        drawString(fontRendererObj, mode, width - 222, 23, 0xFF999999);

        int x = 164;
        int colWidth = Math.max(250, (width - 340) / 2);
        int controlRight = x + colWidth - 12;
        if (selectedCategory == GENERAL) {
            section("General", x, 82, colWidth);
            setting("Settings mode", "Choose how much configuration is shown.", 
                    preferences.isAdvancedMode() ? "Advanced" : "Basic", x, 108, controlRight);
            setting("Preference storage", "Saved locally for this Recording Studio installation.", 
                    "Local", x, 145, controlRight);
            setting("Project data", "Project-specific recording data remains separate from preferences.",
                    "Separate", x, 182, controlRight);
        } else if (selectedCategory == RECORDING) {
            section("Recording", x, 82, colWidth);
            setting("Capture interval", "Time between captured frames; lower values capture more often.",
                    controller.getCapture().getIntervalMillis() + " ms", x, 108, controlRight - 92);
            if (preferences.isAdvancedMode()) {
                setting("Capture interval range", "The current capture service accepts 100–60000 ms.",
                        "100–60000 ms", x, 162, controlRight);
                setting("Frame queue", "The capture worker uses a bounded queue; overflow is counted.",
                        "1 active + 8 queued", x, 199, controlRight);
            }
        } else if (selectedCategory == EXPORT) {
            section("Export", x, 82, colWidth);
            setting("Output resolution", "Downscales only and preserves the source aspect ratio.",
                    null, x, 108, controlRight - 118);
            setting("Encoding quality", "H.264 quality preset; lower CRF produces larger files.",
                    null, x, 162, controlRight - 118);
            if (preferences.isAdvancedMode()) {
                setting("Frame rate", "Uses the source capture interval; no forced conversion.",
                        "Source", x, 216, controlRight);
                setting("Encoder", "Current MP4 encoder profile.", "H.264 High Profile", x, 253, controlRight);
                setting("Background export", "Exports currently run on a worker but are cancelled when Studio shuts down.",
                        "Not available", x, 290, controlRight);
            }
        } else if (selectedCategory == AUDIO) {
            section("Audio", x, 82, colWidth);
            setting("Microphone input", "Selected input device for the current project.",
                    controller.getMicrophoneSelectionName(), x, 108, controlRight - 118);
            setting("Capture format", "Current microphone capture format.", "44.1 kHz · 16-bit · Mono",
                    x, 162, controlRight);
            if (preferences.isAdvancedMode()) {
                setting("Game audio", "Minecraft system/game audio capture is not implemented.",
                        "Not available", x, 216, controlRight);
                setting("Live monitoring", "Microphone playback monitoring is not implemented.",
                        "Not available", x, 253, controlRight);
            }
        } else if (selectedCategory == DIAGNOSTICS) {
            section("Diagnostics", x, 82, colWidth);
            setting("Saved frames", "Successfully written frame images.",
                    Long.toString(controller.getCapture().getFrameCount()), x, 108, controlRight);
            setting("Pending frames", "Frames waiting for disk writes.",
                    Integer.toString(controller.getCapture().getPendingFrameCount()), x, 145, controlRight);
            setting("Failed frames", "Frame writes that failed.", 
                    Long.toString(controller.getCapture().getFailedFrameCount()), x, 182, controlRight);
            setting("Export status", "Current export worker state.", controller.getExportStatus(), x, 219, controlRight);
            setting("Runtime status", "Recording Studio controller state.", controller.getStatus(), x, 256, controlRight);
        }

        drawString(fontRendererObj, "Changes are saved automatically.", 154, height - 29, 0xFF777777);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
