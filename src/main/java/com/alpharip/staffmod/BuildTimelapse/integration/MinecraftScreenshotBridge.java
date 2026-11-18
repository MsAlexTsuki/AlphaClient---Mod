package com.alpharip.staffmod.BuildTimelapse.integration;
import java.io.File;
/** Implement with Minecraft 1.8.9 HUD/render settings and framebuffer/ScreenShotHelper APIs. */
public interface MinecraftScreenshotBridge {
 boolean isHudHidden();boolean isHandHidden();void setHudHidden(boolean hidden)throws Exception;void setHandHidden(boolean hidden)throws Exception;
 int getDisplayWidth();int getDisplayHeight();void captureFramebuffer(File file,int width,int height)throws Exception;
}