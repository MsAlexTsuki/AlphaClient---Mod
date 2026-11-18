package com.alpharip.staffmod.RecordingStudio.integration;

import com.alpharip.staffmod.RecordingStudio.capture.RecordingCaptureService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Reads OpenGL pixels on the Minecraft client thread and encodes/writes PNGs
 * on RecordingCaptureService's background worker.
 */
public final class MinecraftFramebufferFrameWriter implements RecordingCaptureService.FrameWriter {
    private final Minecraft minecraft;

    public MinecraftFramebufferFrameWriter(Minecraft minecraft) {
        if (minecraft == null) throw new IllegalArgumentException("minecraft");
        this.minecraft = minecraft;
    }

    public RecordingCaptureService.FrameData captureFrame() throws Exception {
        if (!minecraft.isCallingFromMinecraftThread()) {
            throw new IllegalStateException("Framebuffer capture must run on the Minecraft client thread");
        }
        int width = minecraft.displayWidth;
        int height = minecraft.displayHeight;
        Framebuffer framebuffer = minecraft.getFramebuffer();
        long byteCount = (long) width * (long) height * 4L;
        if (width < 1 || height < 1 || byteCount > Integer.MAX_VALUE || framebuffer == null
                || framebuffer.framebufferWidth != width || framebuffer.framebufferHeight != height) {
            throw new IOException("Minecraft framebuffer dimensions are unavailable or inconsistent");
        }

        ByteBuffer pixels = BufferUtils.createByteBuffer((int) byteCount);
        byte[] rgba = new byte[(int) byteCount];
        int previousFramebuffer = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
        java.nio.IntBuffer previousViewport = BufferUtils.createIntBuffer(4);
        GL11.glGetInteger(GL11.GL_VIEWPORT, previousViewport);
        framebuffer.bindFramebuffer(true);
        try {
            GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
            pixels.position(0);
            pixels.get(rgba);
        } finally {
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, previousFramebuffer);
            GL11.glViewport(previousViewport.get(0), previousViewport.get(1),
                    previousViewport.get(2), previousViewport.get(3));
        }
        return new RecordingCaptureService.FrameData(width, height, rgba);
    }

    public void writeFrame(RecordingCaptureService.FrameData frame, File output) throws Exception {
        if (frame == null || output == null) throw new IllegalArgumentException("Frame and output are required");
        int width = frame.getWidth();
        int height = frame.getHeight();
        byte[] rgba = frame.getRgba();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int[] row = new int[width];
        for (int y = 0; y < height; y++) {
            int sourceY = height - 1 - y;
            int offset = sourceY * width * 4;
            for (int x = 0; x < width; x++) {
                int pixel = offset + x * 4;
                int red = rgba[pixel] & 255;
                int green = rgba[pixel + 1] & 255;
                int blue = rgba[pixel + 2] & 255;
                int alpha = rgba[pixel + 3] & 255;
                row[x] = (alpha << 24) | (red << 16) | (green << 8) | blue;
            }
            image.setRGB(0, y, width, 1, row, 0, width);
        }

        File parent = output.getParentFile();
        if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
            throw new IOException("Cannot create frame output directory");
        }
        File temporary = new File(parent == null ? new File(".") : parent, output.getName() + ".tmp");
        try {
            if (!ImageIO.write(image, "png", temporary)) throw new IOException("PNG writer is unavailable");
            try {
                Files.move(temporary.toPath(), output.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException unsupported) {
                Files.move(temporary.toPath(), output.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            if (temporary.exists() && !temporary.delete()) temporary.deleteOnExit();
        }
    }
}
