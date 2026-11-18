package com.alpharip.staffmod.RecordingStudio.ui;

import com.alpharip.staffmod.RecordingStudio.core.RecordingStudioController;
import com.alpharip.staffmod.RecordingStudio.export.RecordingStudioExportSettings;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/** Small settings screen for YouTube-oriented MP4 export. */
public final class RecordingStudioExportSettingsScreen extends GuiScreen {
    private final RecordingStudioScreen parent;
    private final RecordingStudioController controller;

    public RecordingStudioExportSettingsScreen(RecordingStudioScreen parent,
                                               RecordingStudioController controller) {
        if (parent == null || controller == null) throw new IllegalArgumentException();
        this.parent = parent;
        this.controller = controller;
    }

    @Override public boolean doesGuiPauseGame() { return false; }

    @Override
    public void initGui() {
        buttonList.clear();
        int center = width / 2;
        buttonList.add(new AlphaStudioButton(0, center - 100, height / 2 - 30, 200, 20,
                "Resolution: " + controller.getExportSettings().getResolution().getLabel(), true));
        buttonList.add(new AlphaStudioButton(1, center - 100, height / 2, 200, 20,
                "Quality: " + controller.getExportSettings().getQuality().getLabel()));
        buttonList.add(new AlphaStudioButton(2, center - 100, height / 2 + 42, 200, 20, "Back"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        RecordingStudioExportSettings settings = controller.getExportSettings();
        if (button.id == 0) settings.cycleResolution();
        else if (button.id == 1) settings.cycleQuality();
        else if (button.id == 2) {
            mc.displayGuiScreen(parent);
            return;
        }
        initGui();
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
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        Gui.drawRect(0, 0, width, height, 0xE8101010);
        drawCenteredString(fontRendererObj, "RECORDING STUDIO / EXPORT SETTINGS",
                width / 2, height / 2 - 100, 0xFFFFC83D);
        drawCenteredString(fontRendererObj, "YouTube-ready MP4 export",
                width / 2, height / 2 - 78, 0xFFE0E0E0);
        drawCenteredString(fontRendererObj, "Resolution limits only downscale; source aspect ratio is preserved.",
                width / 2, height / 2 + 78, 0xFFAAAAAA);
        drawCenteredString(fontRendererObj, "Frame rate follows the recording. No artificial 30/60 FPS conversion.",
                width / 2, height / 2 + 92, 0xFF888888);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
