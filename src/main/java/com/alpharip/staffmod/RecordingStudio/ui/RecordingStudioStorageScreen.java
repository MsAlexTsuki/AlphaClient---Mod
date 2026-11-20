package com.alpharip.staffmod.RecordingStudio.ui;

import com.alpharip.staffmod.RecordingStudio.core.RecordingStudioStorageUsage;
import com.alpharip.staffmod.RecordingStudio.integration.RecordingStudioRuntime;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;

/** Read-only storage dashboard; it never deletes or modifies user recordings. */
public final class RecordingStudioStorageScreen extends GuiScreen {
    private final GuiScreen parent;
    private final RecordingStudioStorageUsage usage;
    private final RecordingStudioRuntime runtime;

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
        buttonList.add(new AlphaStudioButton(0, width / 2 - 105, height - 42, 100, 22, "Refresh"));
        buttonList.add(new AlphaStudioButton(1, width / 2 + 5, height - 42, 100, 22, "Back", true));
    }

    @Override protected void actionPerformed(net.minecraft.client.gui.GuiButton button) {
        if (button.id == 0) usage.refreshAsync();
        else if (button.id == 1) mc.displayGuiScreen(parent);
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
        drawCenteredString(fontRendererObj, "Read-only overview  |  No files are removed automatically",
                width / 2, 316, 0xFF888888);
        super.drawScreen(mouseX, mouseY, partialTicks);
        runtime.drawCameraPreview();
    }
}
