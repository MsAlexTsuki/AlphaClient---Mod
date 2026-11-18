package com.alpharip.staffmod.BuildTimelapse.ui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;

public final class BuildTimelapseStudioScreen extends GuiScreen {
    private final StudioController controller;
    private final StudioScreenModel model;
    private final com.alpharip.staffmod.BuildTimelapse.integration.BuildTimelapseRuntime runtime;
    private boolean navigating;

    public BuildTimelapseStudioScreen(StudioController controller, StudioScreenModel model) {
        this(controller, model, null);
    }

    public BuildTimelapseStudioScreen(StudioController controller, StudioScreenModel model,
            com.alpharip.staffmod.BuildTimelapse.integration.BuildTimelapseRuntime runtime) {
        if (controller == null || model == null) throw new IllegalArgumentException();
        this.controller = controller;
        this.model = model;
        this.runtime = runtime;
    }

    @Override public boolean doesGuiPauseGame() { return false; }

    @Override
    public void initGui() {
        navigating = false;
        buttonList.clear();
        int center = width / 2;
        int top = height / 2 - 62;
        buttonList.add(new GuiButton(0, center - 125, top, 120, 20, "Start Recording"));
        buttonList.add(new GuiButton(1, center + 5, top, 120, 20, "Pause"));
        buttonList.add(new GuiButton(2, center - 125, top + 25, 120, 20, "Resume"));
        buttonList.add(new GuiButton(3, center + 5, top + 25, 120, 20, "Stop"));
        buttonList.add(new GuiButton(4, center - 125, top + 58, 80, 20, "Camera"));
        buttonList.add(new GuiButton(5, center - 40, top + 58, 80, 20, "Timeline"));
        buttonList.add(new GuiButton(6, center + 45, top + 58, 80, 20, "Capture"));
        buttonList.add(new GuiButton(7, center - 60, top + 86, 120, 20, "Save Project"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) controller.start();
        else if (button.id == 1) controller.pause();
        else if (button.id == 2) controller.resume();
        else if (button.id == 3) controller.stop();
        else if (button.id == 4) {
            navigating = true;
            mc.displayGuiScreen(new CameraEditorScreen(this, controller.getCamera()));
        } else if (button.id == 5) {
            navigating = true;
            mc.displayGuiScreen(new com.alpharip.staffmod.RecordingStudio.ui.TimelineEditorScreen(this, controller.getTimeline()));
        } else if (button.id == 6) {
            navigating = true;
            mc.displayGuiScreen(new CaptureSettingsScreen(this, controller.getCaptureSettings()));
        } else if (button.id == 7) controller.save();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) { mc.displayGuiScreen(null); return; }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void onGuiClosed() {
        if (runtime != null && !navigating) runtime.closeStudio();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "BUILD TIMELAPSE STUDIO", width / 2, 24, 0xFFC83D);
        drawCenteredString(fontRendererObj,
                model.getProject() == null ? "No project selected" : model.getProject().getName(),
                width / 2, 42, 0xFFFFFF);
        drawCenteredString(fontRendererObj, "Status: " + model.getStatus(),
                width / 2, height / 2 + 58, 0xBBBBBB);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
