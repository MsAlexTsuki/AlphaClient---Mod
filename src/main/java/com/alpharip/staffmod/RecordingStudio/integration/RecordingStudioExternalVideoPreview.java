package com.alpharip.staffmod.RecordingStudio.integration;

import com.alpharip.staffmod.RecordingStudio.timeline.Timeline;
import com.alpharip.staffmod.RecordingStudio.timeline.TimelineClip;
import com.alpharip.staffmod.RecordingStudio.timeline.TimelineTrack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Extracts low-resolution preview frames from external video clips with FFmpeg. */
public final class RecordingStudioExternalVideoPreview {
    private static final class Request {
        final String key;
        final File source;
        final double sourceSeconds;
        Request(String key, File source, double sourceSeconds) {
            this.key = key; this.source = source; this.sourceSeconds = sourceSeconds;
        }
    }

    private final ExecutorService decoder = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "Alpha-RecordingStudio-ExternalVideoPreview");
        thread.setDaemon(true);
        return thread;
    });
    private Request pending;
    private String requestedKey = "";
    private boolean workerRunning;
    private boolean closed;
    private BufferedImage readyImage;
    private String status = "No external video at playhead";
    private volatile Process process;
    private DynamicTexture texture;
    private ResourceLocation textureLocation;
    private int textureWidth, textureHeight;

    public synchronized boolean update(Timeline timeline, long positionMillis) {
        if (closed) return false;
        TimelineClip selected = findActiveExternalClip(timeline, positionMillis);
        if (selected == null) {
            requestedKey = "";
            pending = null;
            readyImage = null;
            status = "No external video at playhead";
            return false;
        }
        long offsetMillis = Math.max(0L, positionMillis - selected.getStart());
        double sourceSeconds = offsetMillis * selected.getSpeed() / 1000.0D;
        long previewBucket = Math.max(0L, (long) Math.floor(sourceSeconds * 10.0D));
        String key = selected.getId() + "|" + selected.getSource() + "|" + previewBucket;
        if (!key.equals(requestedKey)) {
            requestedKey = key;
            readyImage = null;
            File source = new File(selected.getSource());
            if (!source.isFile()) {
                pending = null;
                status = "External video file not found: " + source.getName();
                return true;
            }
            pending = new Request(key, source, sourceSeconds);
            status = "Loading external video preview...";
            if (!workerRunning) {
                workerRunning = true;
                decoder.execute(new Runnable() {
                    public void run() { decodePendingRequests(); }
                });
            }
        }
        return true;
    }

    private void decodePendingRequests() {
        while (true) {
            final Request request;
            synchronized (this) {
                request = pending;
                pending = null;
                if (request == null || closed) { workerRunning = false; return; }
            }
            BufferedImage image = null;
            String resultStatus;
            try { image = decode(request); resultStatus = "External video preview"; }
            catch (Exception failure) { resultStatus = "External video preview failed: " + failure.getMessage(); }
            synchronized (this) {
                if (closed) { workerRunning = false; return; }
                if (request.key.equals(requestedKey)) {
                    readyImage = image;
                    status = resultStatus;
                }
            }
        }
    }

    private BufferedImage decode(Request request) throws Exception {
        File log = File.createTempFile("alpha-recording-preview-", ".log");
        Process activeProcess = null;
        try {
            String timestamp = String.format(Locale.ROOT, "%.3f", request.sourceSeconds);
            ProcessBuilder builder = new ProcessBuilder("ffmpeg", "-hide_banner", "-loglevel", "error",
                    "-ss", timestamp, "-i", request.source.getAbsolutePath(), "-frames:v", "1",
                    "-vf", "scale=1280:720:force_original_aspect_ratio=decrease",
                    "-f", "image2pipe", "-vcodec", "png", "pipe:1");
            builder.redirectError(log);
            activeProcess = builder.start();
            process = activeProcess;
            BufferedImage image = ImageIO.read(activeProcess.getInputStream());
            int exit = activeProcess.waitFor();
            if (exit != 0) throw new IOException("FFmpeg exited with code " + exit);
            if (image == null) throw new IOException("FFmpeg returned no preview frame");
            return image;
        } finally {
            process = null;
            if (activeProcess != null && activeProcess.isAlive()) {
                activeProcess.destroy();
                if (!activeProcess.waitFor(250L, java.util.concurrent.TimeUnit.MILLISECONDS))
                    activeProcess.destroyForcibly();
            }
            if (!log.delete()) log.deleteOnExit();
        }
    }

    private static TimelineClip findActiveExternalClip(Timeline timeline, long positionMillis) {
        if (timeline == null) return null;
        TimelineClip selected = null;
        for (TimelineTrack track : timeline.getTracks()) {
            if (!"video".equals(track.getId())) continue;
            for (TimelineClip clip : track.getClips()) {
                if ("@capture".equals(clip.getSource()) || clip.getDuration() <= 0L) continue;
                if (positionMillis >= clip.getStart() && positionMillis < clip.getEnd()
                        && (selected == null || clip.getStart() >= selected.getStart())) selected = clip;
            }
            break;
        }
        return selected;
    }

    public synchronized String getStatus() { return status; }

    public synchronized void draw(Minecraft minecraft, int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) return;
        Gui.drawRect(x, y, x + width, y + height, 0xFF080808);
        if (readyImage != null) { updateTexture(minecraft, readyImage); readyImage = null; }
        else if (!"External video preview".equals(status) && textureLocation != null) {
            minecraft.getTextureManager().deleteTexture(textureLocation);
            textureLocation = null; texture = null;
        }
        if (textureLocation == null) {
            minecraft.fontRendererObj.drawString(status, x + 8, y + 8, 0xFFFFD65A);
            return;
        }
        float scale = Math.min(width / (float) textureWidth, height / (float) textureHeight);
        int drawWidth = Math.max(1, Math.round(textureWidth * scale));
        int drawHeight = Math.max(1, Math.round(textureHeight * scale));
        int drawX = x + (width - drawWidth) / 2;
        int drawY = y + (height - drawHeight) / 2;
        minecraft.getTextureManager().bindTexture(textureLocation);
        Gui.drawModalRectWithCustomSizedTexture(drawX, drawY, 0.0F, 0.0F,
                drawWidth, drawHeight, textureWidth, textureHeight);
        minecraft.fontRendererObj.drawString(status, x + 8, y + height - 12, 0xFFFFD65A);
    }

    private void updateTexture(Minecraft minecraft, BufferedImage image) {
        if (texture == null || image.getWidth() != textureWidth || image.getHeight() != textureHeight) {
            if (textureLocation != null) minecraft.getTextureManager().deleteTexture(textureLocation);
            textureWidth = image.getWidth(); textureHeight = image.getHeight();
            texture = new DynamicTexture(image);
            textureLocation = minecraft.getTextureManager().getDynamicTextureLocation(
                    "alpha-recording-studio-external-preview", texture);
            return;
        }
        image.getRGB(0, 0, textureWidth, textureHeight, texture.getTextureData(), 0, textureWidth);
        texture.updateDynamicTexture();
    }

    public synchronized void close(Minecraft minecraft) {
        if (closed) return;
        closed = true; pending = null; decoder.shutdownNow();
        Process activeProcess = process;
        if (activeProcess != null) activeProcess.destroyForcibly();
        readyImage = null;
        if (minecraft != null && textureLocation != null) minecraft.getTextureManager().deleteTexture(textureLocation);
        textureLocation = null; texture = null;
    }
}
