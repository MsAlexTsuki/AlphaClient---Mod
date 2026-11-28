package com.alpharip.staffmod.MinecraftWindowAlphaClient;

/** Formats the exact title requested for the Minecraft 1.8.9 client. */
public final class MinecraftWindowTitleFormatter {
    public static final String MINECRAFT_VERSION = "1.8.9";
    private static final String UNKNOWN_VERSION = "Unknown";

    private MinecraftWindowTitleFormatter() {
    }

    public static String format(String alphaClientJarVersion) {
        String version = alphaClientJarVersion == null ? "" : alphaClientJarVersion.trim();
        if (version.isEmpty()) version = UNKNOWN_VERSION;
        return "AlphaClient " + version + " + Forge " + MINECRAFT_VERSION;
    }
}
