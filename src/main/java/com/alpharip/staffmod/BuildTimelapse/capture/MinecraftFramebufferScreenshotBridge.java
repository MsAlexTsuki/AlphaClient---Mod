package com.alpharip.staffmod.BuildTimelapse.capture;

import com.alpharip.staffmod.BuildTimelapse.integration.MinecraftScreenshotBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

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
        if (width < 1 || height < 1 || width != minecraft.displayWidth || height != minecraft.displayHeight) {
            throw new UnsupportedOperationException(
                    "The framebuffer adapter currently supports native display resolution only");
        }

        long byteCount = (long) width * (long) height * 4L;
        if (byteCount > Integer.MAX_VALUE) throw new IOException("Framebuffer dimensions exceed the supported image size");

        Framebuffer framebuffer = minecraft.getFramebuffer();
        if (framebuffer == null || framebuffer.framebufferWidth != width
                || framebuffer.framebufferHeight != height) {
            throw new IOException("Minecraft main framebuffer dimensions do not match the display");
        }

        ByteBuffer pixels = BufferUtils.createByteBuffer((int) byteCount);
        boolean framebufferSupported = OpenGlHelper.isFramebufferEnabled();
        int previousFramebuffer = framebufferSupported ? GL11.glGetInteger(0x8CA6) : 0;
        IntBuffer previousViewport = BufferUtils.createIntBuffer(4);
        GL11.glGetInteger(GL11.GL_VIEWPORT, previousViewport);
        try {
            framebuffer.bindFramebuffer(true);
            GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
            pixels.position(0);
        } finally {
            if (framebufferSupported) {
                OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, previousFramebuffer);
            }
            GlStateManager.viewport(previousViewport.get(0), previousViewport.get(1),
                    previousViewport.get(2), previousViewport.get(3));
        }

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
        File temporary = new File(parent == null ? new File(".") : parent, output.getName() + ".tmp");
        try {
            if (!ImageIO.write(image, "png", temporary)) {
                throw new IOException("No PNG writer is available");
            }
            try {
                Files.move(temporary.toPath(), output.toPath(),
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException unsupported) {
                Files.move(temporary.toPath(), output.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            if (temporary.exists() && !temporary.delete()) temporary.deleteOnExit();
        }
    }
}
