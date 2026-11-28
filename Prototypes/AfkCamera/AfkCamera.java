package com.alpharip.staffmod.AfkCamera;

import net.minecraft.client.gui.GuiScreen;

/**
 * Main facade for the AFK Camera prototype.
 * Exposes local settings and an activity-driven state controller.
 * Actual Minecraft camera manipulation is intentionally not connected.
 */
public final class AfkCamera {
    private final AfkCameraSettings settings = new AfkCameraSettings();
    private final AfkActivityTracker activityTracker = new AfkActivityTracker();
    private final AfkCameraController controller =
            new AfkCameraController(settings, activityTracker);

    public AfkCameraSettings getSettings() { return settings; }
    public AfkActivityTracker getActivityTracker() { return activityTracker; }
    public AfkCameraController getController() { return controller; }

    public GuiScreen createSettingsGui(GuiScreen parent) {
        return new AfkCameraGui(parent, settings);
    }
}
