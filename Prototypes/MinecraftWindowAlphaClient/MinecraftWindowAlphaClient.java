package com.alpharip.staffmod.MinecraftWindowAlphaClient;

/**
 * Standalone entry point for the AlphaClient window-title prototype.
 *
 * Call refreshTitle after the Minecraft display has been created. This prototype
 * is deliberately not registered with AlphaClient Core or the live module menu.
 */
public final class MinecraftWindowAlphaClient {
    private final MinecraftWindowTitleController controller;
    private final AlphaClientJarVersionResolver versionResolver;

    public MinecraftWindowAlphaClient(MinecraftWindowTitleTarget target,
                                      MinecraftWindowTitleSettings settings) {
        if (target == null) throw new IllegalArgumentException("A window title target is required");
        if (settings == null) throw new IllegalArgumentException("Window title settings are required");
        this.controller = new MinecraftWindowTitleController(target, settings);
        this.versionResolver = new AlphaClientJarVersionResolver();
    }

    /** Resolves the installed JAR version and applies the requested title when enabled. */
    public String refreshTitle() {
        String title = MinecraftWindowTitleFormatter.format(versionResolver.resolve());
        controller.apply(title);
        return title;
    }

    public MinecraftWindowTitleSettings getSettings() {
        return controller.getSettings();
    }
}
