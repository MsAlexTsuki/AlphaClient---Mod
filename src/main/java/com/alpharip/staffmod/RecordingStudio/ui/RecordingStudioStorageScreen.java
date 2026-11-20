package com.alpharip.staffmod.RecordingStudio.ui;

import com.alpharip.staffmod.RecordingStudio.core.RecordingStudioStorageUsage;
import com.alpharip.staffmod.RecordingStudio.core.RecordingStudioCleanupService;
import java.io.File;
import com.alpharip.staffmod.RecordingStudio.integration.RecordingStudioRuntime;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;

/** Storage dashboard with preview-first manual cleanup; it never deletes files automatically. */
public final class RecordingStudioStorageScreen extends GuiScreen {
    private final GuiScreen parent;
    private final RecordingStudioStorageUsage usage;
    private final RecordingStudioRuntime runtime;
    private int retentionDays;
    private String cleanupStatus = "Cleanup is manual and disabled until a retention period is selected";

    public RecordingStudioStorageScreen(GuiScreen parent, RecordingStudioStorageUsage usage,
                                         RecordingStudioRuntime runtime) {
        if (parent == null || usage == null || runtime == null) throw new IllegalArgumentException();
        this.parent = parent;
        this.usage = usage;
        this.runtime = runtime;
    }

    @Override public boolean doesGuiPauseGame() { return false; }

    @Override public void initGui() {
        buttonList.clear();
        int left = width / 2 - 160;
        buttonList.add(new AlphaStudioButton(0, left + 4, height - 42, 72, 22, "Refresh"));
        buttonList.add(new AlphaStudioButton(1, left + 80, height - 42, 82, 22, retentionDays == 0 ? "Retention: OFF" : retentionDays + " days"));
        buttonList.add(new AlphaStudioButton(2, left + 166, height - 42, 92, 22, "Preview cleanup"));
        buttonList.add(new AlphaStudioButton(3, left + 262, height - 42, 58, 22, "Back", true));
    }

    @Override protected void actionPerformed(net.minecraft.client.gui.GuiButton button) {
        if (button.id == 0) usage.refreshAsync();
        else if (button.id == 1) {
            int[] periods = {0, 7, 30, 90, 180, 365};
            int index = 0;
            for (int i = 0; i < periods.length; i++) if (periods[i] == retentionDays) index = i;
            retentionDays = periods[(index + 1) % periods.length];
            initGui();
        } else if (button.id == 2) previewCleanup();
        else if (button.id == 3) mc.displayGuiScreen(parent);
    }

    private void previewCleanup() {
        if (retentionDays <= 0) {
            cleanupStatus = "Choose a retention period before previewing cleanup";
            return;
        }
        try {
            RecordingStudioCleanupService.CleanupPlan plan =
                    RecordingStudioCleanupService.preview(new File(usage.getRootPath()),
                            retentionDays, System.currentTimeMillis());
            if (plan.getFileCount() == 0) {
                cleanupStatus = "No eligible media files older than " + retentionDays + " days";
                return;
            }
            mc.displayGuiScreen(new RecordingStudioCleanupConfirmScreen(this, usage, plan));
        } catch (IOException | RuntimeException failure) {
            cleanupStatus = "Cleanup preview failed: " + failure.getMessage();
        }
    }

    public void setCleanupStatus(String value) {
        cleanupStatus = value == null ? "" : value;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            mc.displayGuiScreen(parent);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    private void row(String label, long bytes, int count, int y) {
        int left = width / 2 - 145;
        Gui.drawRect(left, y - 5, left + 290, y + 24, 0xFF242424);
        drawString(fontRendererObj, label, left + 10, y + 3, 0xFFE7E7E7);
        String detail = RecordingStudioStorageUsage.formatBytes(bytes) + "  |  " + count + " files";
        drawString(fontRendererObj, detail, left + 150, y + 3, 0xFFB9B9B9);
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int left = width / 2 - 160;
        int top = 22;
        Gui.drawRect(left, top, left + 320, height - 72, 0xE8101010);
        Gui.drawRect(left, top, left + 320, top + 2, 0xFFFFC83D);
        drawCenteredString(fontRendererObj, "RECORDING STUDIO  /  STORAGE", width / 2, 34, 0xFFFFC83D);
        drawCenteredString(fontRendererObj, RecordingStudioStorageUsage.formatBytes(usage.getTotalBytes()),
                width / 2, 62, 0xFFFFFFFF);
        drawCenteredString(fontRendererObj, "TRACKED STUDIO DATA", width / 2, 75, 0xFFAAAAAA);
        row("Projects", usage.getProjectBytes(), usage.getProjectCount(), 108);
        row("Video frames", usage.getFrameBytes(), usage.getFrameCount(), 143);
        row("Microphone audio", usage.getAudioBytes(), usage.getAudioCount(), 178);
        row("Game audio", usage.getGameAudioBytes(), usage.getGameAudioCount(), 213);
        row("Exports", usage.getExportBytes(), usage.getExportCount(), 248);
        drawCenteredString(fontRendererObj, usage.isScanning() ? "Scanning storage..."
                : usage.getScanError() != null ? "Storage scan failed: " + usage.getScanError()
                : usage.isReadable() ? "Storage scan complete" : "Some folders could not be read",
                width / 2, 283, usage.isReadable() ? 0xFFB9B9B9 : 0xFFFFB45C);
        drawCenteredString(fontRendererObj, "Location: " + usage.getRootPath(),
                width / 2, 299, 0xFF888888);
        drawCenteredString(fontRendererObj, "Manual cleanup only  |  No automatic deletion",
                width / 2, 316, 0xFF888888);
        drawCenteredString(fontRendererObj, cleanupStatus,
                width / 2, 333, 0xFFFFD65A);
        drawCenteredString(fontRendererObj, "Only old media is eligible; project metadata and active projects are protected",
                width / 2, 348, 0xFF888888);
        super.drawScreen(mouseX, mouseY, partialTicks);
        runtime.drawCameraPreview();
    }
}
