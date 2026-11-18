package com.alpharip.staffmod.BuildTimelapse.capture;

import com.alpharip.staffmod.BuildTimelapse.integration.MinecraftScreenshotBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Minecraft 1.8.9 framebuffer adapter.
 *
 * Captures the already-rendered main framebuffer on the client/render thread.
 * HUD visibility is controlled through GameSettings.hideGUI. Hand suppression
 * requires a renderer hook and is deliberately rejected instead of being
 * reported as successful when no such hook is installed.
 */
public final class MinecraftFramebufferScreenshotBridge implements MinecraftScreenshotBridge {
    private final Minecraft minecraft;

    public MinecraftFramebufferScreenshotBridge(Minecraft minecraft) {
        if (minecraft == null) throw new IllegalArgumentException("minecraft");
        this.minecraft = minecraft;
    }

    private void requireRenderThread() {
        if (!minecraft.isCallingFromMinecraftThread()) {
            throw new IllegalStateException("Framebuffer access must run on the Minecraft client thread");
        }
    }

    @Override
    public boolean isHudHidden() {
        requireRenderThread();
        return minecraft.gameSettings.hideGUI;
    }

    @Override
    public boolean isHandHidden() {
        requireRenderThread();
        return false;
    }

    @Override
    public void setHudHidden(boolean hidden) {
        requireRenderThread();
        minecraft.gameSettings.hideGUI = hidden;
    }

    @Override
    public void setHandHidden(boolean hidden) {
        requireRenderThread();
        if (hidden) {
            throw new UnsupportedOperationException(
                    "Hiding the first-person hand requires a dedicated renderer hook");
        }
    }

    @Override
    public int getDisplayWidth() {
        requireRenderThread();
        return minecraft.displayWidth;
    }

    @Override
    public int getDisplayHeight() {
        requireRenderThread();
        return minecraft.displayHeight;
    }

    @Override
    public void captureFramebuffer(File output, int width, int height) throws Exception {
        requireRenderThread();
        if (output == null) throw new IllegalArgumentException("output");
        if (width != minecraft.displayWidth || height != minecraft.displayHeight) {
            throw new UnsupportedOperationException(
                    "The framebuffer adapter currently supports native display resolution only");
        }

        Framebuffer framebuffer = minecraft.getFramebuffer();
        if (framebuffer == null || framebuffer.framebufferWidth != width
                || framebuffer.framebufferHeight != height) {
            throw new IOException("Minecraft main framebuffer dimensions do not match the display");
        }

        ByteBuffer pixels = BufferUtils.createByteBuffer(width * height * 4);
        framebuffer.bindFramebuffer(true);
        GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            int sourceY = height - 1 - y;
            for (int x = 0; x < width; x++) {
                int offset = (sourceY * width + x) * 4;
                int red = pixels.get(offset) & 255;
                int green = pixels.get(offset + 1) & 255;
                int blue = pixels.get(offset + 2) & 255;
                int alpha = pixels.get(offset + 3) & 255;
                image.setRGB(x, y, (alpha << 24) | (red << 16) | (green << 8) | blue);
            }
        }

        File parent = output.getParentFile();
        if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
            throw new IOException("Cannot create screenshot directory: " + parent);
        }
        if (!ImageIO.write(image, "png", output)) {
            throw new IOException("No PNG writer is available");
        }
    }
}
