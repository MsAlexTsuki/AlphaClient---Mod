package com.alpharip.staffmod.RecordingStudio.integration;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Lightweight playback for the PNG sequence produced by RecordingCaptureService.
 * Image decoding runs off the client thread; OpenGL texture updates stay on it.
 */
public final class RecordingStudioPlayback {
    private static final class DecodedFrame {
        final int index;
        final BufferedImage image;
        final String error;

        DecodedFrame(int index, BufferedImage image, String error) {
            this.index = index;
            this.image = image;
            this.error = error;
        }
    }

    private final File framesDirectory;
    private final ExecutorService decoder = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "Alpha-RecordingStudio-PlaybackDecoder");
        thread.setDaemon(true);
        return thread;
    });
    private final AtomicReference<DecodedFrame> decoded = new AtomicReference<DecodedFrame>();
    private File[] frames = new File[0];
    private int requestedFrame = -1;
    private boolean loading;
    private boolean playing;
    private boolean closed;
    private int frameIndex;
    private long lastFrameAt;
    private String status = "No captured frames yet";
    private DynamicTexture texture;
    private ResourceLocation textureLocation;
    private int textureWidth;
    private int textureHeight;

    public RecordingStudioPlayback(File framesDirectory) {
        if (framesDirectory == null) throw new IllegalArgumentException("Frames directory is required");
        this.framesDirectory = framesDirectory;
    }

    public synchronized void refreshFrames() {
        if (closed) return;
        if (!framesDirectory.isDirectory()) {
            frames = new File[0];
            frameIndex = 0;
            playing = false;
            status = "No captured frames yet";
            return;
        }
        File[] found = framesDirectory.listFiles((directory, name) ->
                name.matches("frame-[0-9]{8}\\\\.png") && new File(directory, name).isFile());
        if (found == null) {
            status = "Cannot read captured frame directory";
            return;
        }
        Arrays.sort(found, Comparator.comparing(File::getName));
        frames = found;
        if (frames.length == 0) {
            frameIndex = 0;
            playing = false;
            status = "No captured frames yet";
            return;
        }
        frameIndex = Math.min(frameIndex, frames.length - 1);
        status = frames.length + " captured frame(s)";
        requestFrame(frameIndex);
    }

    public synchronized void togglePlayback() {
        if (closed) return;
        if (frames.length == 0) refreshFrames();
        if (frames.length == 0) {
            playing = false;
            return;
        }
        playing = !playing;
        lastFrameAt = System.currentTimeMillis();
        if (playing) requestFrame(frameIndex);
        status = playing ? "Playing" : "Playback paused";
    }

    public synchronized void pause() {
        playing = false;
        if (frames.length > 0) status = "Playback paused";
    }

    public synchronized void step(int direction) {
        if (closed) return;
        if (frames.length == 0) refreshFrames();
        if (frames.length == 0) return;
        playing = false;
        frameIndex = Math.max(0, Math.min(frames.length - 1, frameIndex + direction));
        status = "Playback paused";
        requestFrame(frameIndex);
    }

    private synchronized void requestFrame(final int index) {
        if (closed || index < 0 || index >= frames.length) return;
        requestedFrame = index;
        if (loading) return;
        loading = true;
        decoder.execute(new Runnable() {
            public void run() {
                while (true) {
                    final int next;
                    final File file;
                    synchronized (RecordingStudioPlayback.this) {
                        next = requestedFrame;
                        requestedFrame = -1;
                        if (next < 0 || next >= frames.length || closed) {
                            loading = false;
                            return;
                        }
                        file = frames[next];
                    }
                    DecodedFrame result;
                    try {
                        BufferedImage image = ImageIO.read(file);
                        if (image == null) throw new IOException("Unsupported or invalid PNG frame");
                        result = new DecodedFrame(next, image, null);
                    } catch (Exception failure) {
                        result = new DecodedFrame(next, null, failure.getMessage());
                    }
                    decoded.set(result);
                    synchronized (RecordingStudioPlayback.this) {
                        if (requestedFrame < 0 || closed) {
                            loading = false;
                            return;
                        }
                    }
                }
            }
        });
    }

    public synchronized void draw(Minecraft minecraft, int x, int y, int width, int height,
                                  long frameIntervalMillis, int pendingWrites) {
        if (width <= 0 || height <= 0) return;
        if (playing && frames.length > 0) {
            long now = System.currentTimeMillis();
            long interval = Math.max(100L, frameIntervalMillis);
            if (now - lastFrameAt >= interval) {
                long steps = Math.max(1L, (now - lastFrameAt) / interval);
                frameIndex = (int) ((frameIndex + steps) % frames.length);
                lastFrameAt += steps * interval;
                requestFrame(frameIndex);
            }
        }

        DecodedFrame ready = decoded.getAndSet(null);
        if (ready != null) {
            if (ready.image == null) {
                status = "Frame " + (ready.index + 1) + " could not be decoded: " + ready.error;
            } else {
                updateTexture(minecraft, ready.image);
                status = (playing ? "Playing" : "Playback paused")
                        + " — Frame " + (ready.index + 1) + " / " + frames.length;
            }
        }

        Gui.drawRect(x, y, x + width, y + height, 0xFF080808);
        if (textureLocation == null) {
            String message = frames.length == 0
                    ? (pendingWrites > 0 ? "Waiting for captured frames to finish writing..." : "No captured frames yet")
                    : "Loading captured frame...";
            minecraft.fontRendererObj.drawString(message, x + 8, y + 8, 0xFFBBBBBB);
        } else {
            float scale = Math.min(width / (float) textureWidth, height / (float) textureHeight);
            int drawWidth = Math.max(1, Math.round(textureWidth * scale));
            int drawHeight = Math.max(1, Math.round(textureHeight * scale));
            int drawX = x + (width - drawWidth) / 2;
            int drawY = y + (height - drawHeight) / 2;
            minecraft.getTextureManager().bindTexture(textureLocation);
            Gui.drawModalRectWithCustomSizedTexture(drawX, drawY, 0.0F, 0.0F,
                    drawWidth, drawHeight, textureWidth, textureHeight);
        }
        minecraft.fontRendererObj.drawString(status, x + 8, y + height - 12, 0xFFFFD65A);
    }

    private void updateTexture(Minecraft minecraft, BufferedImage image) {
        if (texture == null || image.getWidth() != textureWidth || image.getHeight() != textureHeight) {
            if (textureLocation != null) minecraft.getTextureManager().deleteTexture(textureLocation);
            textureWidth = image.getWidth();
            textureHeight = image.getHeight();
            texture = new DynamicTexture(image);
            textureLocation = minecraft.getTextureManager().getDynamicTextureLocation(
                    "alpha-recording-studio-playback", texture);
            return;
        }
        image.getRGB(0, 0, textureWidth, textureHeight, texture.getTextureData(), 0, textureWidth);
        texture.updateDynamicTexture();
    }

    public synchronized boolean isPlaying() { return playing; }
    public synchronized int getFrameCount() { return frames.length; }

    public synchronized void close(Minecraft minecraft) {
        if (closed) return;
        closed = true;
        playing = false;
        decoder.shutdownNow();
        decoded.set(null);
        if (minecraft != null && textureLocation != null) {
            minecraft.getTextureManager().deleteTexture(textureLocation);
        }
        textureLocation = null;
        texture = null;
    }
}
