package com.alpharip.staffmod.MinecraftWindowAlphaClient;

import org.lwjgl.opengl.Display;

/** Minecraft 1.8.9 / LWJGL 2 adapter for the native game-window title. */
public final class LwjglWindowTitleTarget implements MinecraftWindowTitleTarget {
    @Override
    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) return;
        if (Display.isCreated()) {
            Display.setTitle(title);
        }
    }
}
