package com.alpharip.staffmod.RecordingStudio.ui;

import com.alpharip.staffmod.RecordingStudio.core.RecordingStudioCleanupService;
import com.alpharip.staffmod.RecordingStudio.core.RecordingStudioStorageUsage;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;

/** Explicit second confirmation before old recording media is deleted. */
public final class RecordingStudioCleanupConfirmScreen extends GuiScreen {
    private final RecordingStudioStorageScreen parent;
    private final RecordingStudioStorageUsage usage;
    private final RecordingStudioCleanupService.CleanupPlan plan;
    private String status = "Review the exact cleanup scope before confirming.";

    public RecordingStudioCleanupConfirmScreen(RecordingStudioStorageScreen parent,
                                                RecordingStudioStorageUsage usage,
                                                RecordingStudioCleanupService.CleanupPlan plan) {
        if (parent == null || usage == null || plan == null) throw new IllegalArgumentException("Cleanup confirmation data is required");
        this.parent = parent;
        this.usage = usage;
        this.plan = plan;
    }

    @Override public boolean doesGuiPauseGame() { return false; }

    @Override public void initGui() {
        buttonList.clear();
        buttonList.add(new AlphaStudioButton(0, width / 2 - 112, height - 48, 104, 24, "DELETE MEDIA", true));
        buttonList.add(new AlphaStudioButton(1, width / 2 + 8, height - 48, 104, 24, "CANCEL"));
    }

    @Override protected void actionPerformed(GuiButton button) {
        if (button.id == 1) {
            mc.displayGuiScreen(parent);
            return;
        }
        if (button.id != 0) return;
        try {
            RecordingStudioCleanupService.CleanupResult result = RecordingStudioCleanupService.clean(plan);
            parent.setCleanupStatus("Deleted " + result.getDeletedFiles() + " files · "
                    + RecordingStudioStorageUsage.formatBytes(result.getDeletedBytes()));
            usage.refreshAsync();
            mc.displayGuiScreen(parent);
        } catch (IOException | RuntimeException failure) {
            status = "Cleanup failed: " + failure.getMessage();
        }
    }

    @Override protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            mc.displayGuiScreen(parent);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int left = width / 2 - 164;
        Gui.drawRect(left, 42, left + 328, height - 82, 0xE8101010);
        Gui.drawRect(left, 42, left + 328, 44, 0xFFFFC83D);
        drawCenteredString(fontRendererObj, "CONFIRM STORAGE CLEANUP", width / 2, 58, 0xFFFFC83D);
        drawCenteredString(fontRendererObj, plan.getFileCount() + " old media files selected", width / 2, 92, 0xFFFFFFFF);
        drawCenteredString(fontRendererObj, RecordingStudioStorageUsage.formatBytes(plan.getBytes())
                + " · older than " + plan.getRetentionDays() + " days", width / 2, 111, 0xFFE0E0E0);
        drawCenteredString(fontRendererObj, "Scope: recordings/ and exports/ only", width / 2, 151, 0xFFBBBBBB);
        drawCenteredString(fontRendererObj, "Active or unreadable projects are skipped.", width / 2, 171, 0xFFBBBBBB);
        drawCenteredString(fontRendererObj, "Project metadata, timelines and recovery markers are never deleted.", width / 2, 191, 0xFFBBBBBB);
        drawCenteredString(fontRendererObj, "This action cannot be undone.", width / 2, 225, 0xFFFFB45C);
        drawCenteredString(fontRendererObj, status, width / 2, 254, 0xFFBBBBBB);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
