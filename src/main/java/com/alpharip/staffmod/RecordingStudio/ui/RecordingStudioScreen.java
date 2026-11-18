package com.alpharip.staffmod.RecordingStudio.ui;

import com.alpharip.staffmod.RecordingStudio.core.RecordingStudioController;
import com.alpharip.staffmod.RecordingStudio.integration.RecordingStudioRuntime;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;

/** Standalone manual recording dashboard with AlphaClient dark-and-gold styling. */
public final class RecordingStudioScreen extends GuiScreen {
    private final RecordingStudioController controller;
    private final RecordingStudioRuntime runtime;
    private boolean navigating;

    public RecordingStudioScreen(RecordingStudioController controller, RecordingStudioRuntime runtime) {
        if (controller == null || runtime == null) throw new IllegalArgumentException();
        this.controller = controller;
        this.runtime = runtime;
    }

    @Override public boolean doesGuiPauseGame() { return false; }

    @Override public void initGui() {
        navigating = false;
        buttonList.clear();
        int center = width / 2;
        int left = center - 130;
        int right = center + 6;
        int y = 82;
        add(0, left, y, 124, "Start Recording", true);
        add(1, right, y, 124, "Pause");
        y += 24;
        add(2, left, y, 124, "Resume");
        add(3, right, y, 124, "Stop");
        y += 24;
        add(4, left, y, 124, "Timeline");
        add(5, right, y, 124, "Save Project");
        y += 24;
        add(6, left, y, 260, "Camera Editor / View Camera");
        y += 24;
        add(7, left, y, 124, "Interval - 100 ms");
        add(8, right, y, 124, "Interval + 100 ms");
        y += 24;
        add(9, left, y, 124, "Start Audio");
        add(10, right, y, 124, "Stop Audio");
        y += 24;
        add(11, left, y, 124, "Export MP4", true);
        add(12, right, y, 124, "Export GIF");
        y += 24;
        add(13, left, y, 124, "Cancel Export");
        add(14, right, y, 124, "Storage Usage");
    }

    private void add(int id, int x, int y, int w, String label) {
        add(id, x, y, w, label, false);
    }

    private void add(int id, int x, int y, int w, String label, boolean accent) {
        buttonList.add(new AlphaStudioButton(id, x, y, w, 20, label, accent));
    }

    @Override protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 0: controller.start(); break;
            case 1: controller.pause(); break;
            case 2: controller.resume(); break;
            case 3: controller.stop(); break;
            case 4:
                navigating = true;
                mc.displayGuiScreen(new TimelineEditorScreen(this, controller.getTimeline()));
                break;
            case 5: controller.save(); break;
            case 6:
                navigating = true;
                mc.displayGuiScreen(new CameraEditorScreen(this, runtime.getCameraController(), mc.mcDataDir));
                break;
            case 7: controller.adjustCaptureInterval(-100L); break;
            case 8: controller.adjustCaptureInterval(100L); break;
            case 9: controller.startAudioCapture(); break;
            case 10: controller.stopAudioCapture(); break;
            case 11: controller.startExport(com.alpharip.staffmod.RecordingStudio.export.RecordingStudioExportService.Format.MP4); break;
            case 12: controller.startExport(com.alpharip.staffmod.RecordingStudio.export.RecordingStudioExportService.Format.GIF); break;
            case 13: controller.cancelExport(); break;
            case 14:
                navigating = true;
                controller.getStorageUsage().refreshAsync();
                mc.displayGuiScreen(new RecordingStudioStorageScreen(this, controller.getStorageUsage()));
                break;
            default: break;
        }
    }

    @Override protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) { mc.displayGuiScreen(null); return; }
        super.keyTyped(typedChar, keyCode);
    }

    @Override public void onGuiClosed() {
        if (!navigating) runtime.shutdown();
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int left = width / 2 - 160;
        int top = 16;
        Gui.drawRect(left, top, left + 320, height - 14, 0xE8101010);
        Gui.drawRect(left, top, left + 320, top + 2, 0xFFFFC83D);
        drawCenteredString(fontRendererObj, "ALPHA CLIENT", width / 2, 27, 0xFFAAAAAA);
        drawCenteredString(fontRendererObj, "RECORDING STUDIO", width / 2, 42, 0xFFFFC83D);
        drawCenteredString(fontRendererObj, controller.getProject().getName(), width / 2, 59, 0xFFFFFFFF);

        drawCenteredString(fontRendererObj, "SESSION  " + controller.getStatus(),
                width / 2, height - 111, 0xFFE0E0E0);
        drawCenteredString(fontRendererObj, "Frames  " + controller.getCapture().getFrameCount()
                        + " saved  /  " + controller.getCapture().getPendingFrameCount()
                        + " pending  /  " + controller.getCapture().getFailedFrameCount() + " failed",
                width / 2, height - 96, 0xFFB8B8B8);
        drawCenteredString(fontRendererObj, "Timeline  " + controller.getTimeline().duration()
                        + " ms     Capture interval  " + controller.getCapture().getIntervalMillis() + " ms",
                width / 2, height - 81, 0xFFB8B8B8);
        drawCenteredString(fontRendererObj, controller.getAudioStatus(), width / 2, height - 66, 0xFFB8B8B8);
        drawCenteredString(fontRendererObj, controller.getExportStatus(), width / 2, height - 51, 0xFFFFC83D);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
