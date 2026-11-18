package com.alpharip.staffmod.MinecraftWindowAlphaClient;

/** Applies title changes without repeatedly writing an unchanged value. */
public final class MinecraftWindowTitleController {
    private final MinecraftWindowTitleTarget target;
    private final MinecraftWindowTitleSettings settings;
    private String lastAppliedTitle;

    public MinecraftWindowTitleController(MinecraftWindowTitleTarget target,
                                           MinecraftWindowTitleSettings settings) {
        if (target == null) throw new IllegalArgumentException("A window title target is required");
        if (settings == null) throw new IllegalArgumentException("Window title settings are required");
        this.target = target;
        this.settings = settings;
    }

    public synchronized boolean apply(String title) {
        if (!settings.isEnabled() || title == null || title.trim().isEmpty()) return false;
        if (title.equals(lastAppliedTitle)) return false;
        target.setTitle(title);
        lastAppliedTitle = title;
        return true;
    }

    public MinecraftWindowTitleSettings getSettings() {
        return settings;
    }
}
