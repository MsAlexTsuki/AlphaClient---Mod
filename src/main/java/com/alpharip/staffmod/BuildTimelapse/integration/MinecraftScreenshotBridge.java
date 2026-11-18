package com.alpharip.staffmod.BuildTimelapse.integration;

import java.io.File;

/** Client-thread framebuffer readback plus background-safe PNG output operations. */
public interface MinecraftScreenshotBridge {
    boolean isHudHidden();
    boolean isHandHidden();
    void setHudHidden(boolean hidden) throws Exception;
    void setHandHidden(boolean hidden) throws Exception;
    int getDisplayWidth();
    int getDisplayHeight();

    /** Must be called on the Minecraft render thread; returns bottom-up RGBA pixels. */
    byte[] readFramebufferPixels(int width, int height) throws Exception;

    /** Converts RGBA pixels and writes the PNG; safe to call from the capture worker thread. */
    void writePng(File output, int width, int height, byte[] rgbaPixels) throws Exception;

    void captureFramebuffer(File file, int width, int height) throws Exception;
}
