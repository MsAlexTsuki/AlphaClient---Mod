package com.alpharip.staffmod.RecordingStudio.integration;

import com.alpharip.staffmod.RecordingStudio.capture.RecordingCaptureService;
import com.alpharip.staffmod.RecordingStudio.scene.RecordingStudioScene;
import com.alpharip.staffmod.RecordingStudio.scene.RecordingStudioSource;
import com.alpharip.staffmod.RecordingStudio.timeline.Timeline;
import net.minecraft.client.Minecraft;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** Captures and composites enabled visual sources in scene-list (back-to-front) order. */
public final class RecordingStudioFrameWriter implements RecordingCaptureService.FrameWriter, AutoCloseable {
    private final MinecraftFramebufferFrameWriter minecraftWriter;
    private final Timeline timeline;
    private final Map<String, RecordingStudioWebcamCapture> webcams = new HashMap<String, RecordingStudioWebcamCapture>();

    public RecordingStudioFrameWriter(Minecraft minecraft, Timeline timeline) {
        if (minecraft == null || timeline == null) throw new IllegalArgumentException("Minecraft and timeline are required");
        minecraftWriter = new MinecraftFramebufferFrameWriter(minecraft);
        this.timeline = timeline;
    }

    public RecordingCaptureService.FrameData captureFrame() throws Exception {
        RecordingStudioScene scene = timeline.getActiveScene();
        if (scene == null) throw new IOException("No active Recording Studio scene");
        java.util.List<Layer> layers = new java.util.ArrayList<Layer>();
        int width = 0, height = 0;
        boolean hasNonWebcamCanvas = false;
        java.util.Set<String> activeWebcams = new java.util.HashSet<String>();
        for (RecordingStudioSource source : scene.getSources()) {
            if (!source.isEnabled() || !isVisual(source.getType())) continue;
            RecordingCaptureService.FrameData frame;
            switch (source.getType()) {
                case GAME_CAPTURE: frame = minecraftWriter.captureFrame(); break;
                case DESKTOP: frame = DesktopScreenFrameWriter.capturePrimary(); break;
                case DISPLAY: frame = DesktopScreenFrameWriter.captureDisplay(source.getScreenDeviceId()); break;
                case WEBCAM:
                    activeWebcams.add(source.getId());
                    RecordingStudioWebcamCapture webcam = webcams.get(source.getId());
                    if (webcam == null) { webcam = new RecordingStudioWebcamCapture("ffmpeg"); webcams.put(source.getId(), webcam); }
                    frame = webcam.capture(source.getWebcamDeviceName());
                    break;
                default: continue;
            }
            if (width == 0 || (source.getType() != RecordingStudioSource.Type.WEBCAM && !hasNonWebcamCanvas)) {
                width = frame.getWidth(); height = frame.getHeight();
                if (source.getType() != RecordingStudioSource.Type.WEBCAM) hasNonWebcamCanvas = true;
            }
            layers.add(new Layer(source, frame));
        }
        java.util.Iterator<Map.Entry<String, RecordingStudioWebcamCapture>> webcamIterator = webcams.entrySet().iterator();
        while (webcamIterator.hasNext()) {
            Map.Entry<String, RecordingStudioWebcamCapture> entry = webcamIterator.next();
            if (!activeWebcams.contains(entry.getKey())) { entry.getValue().close(); webcamIterator.remove(); }
        }
        if (layers.isEmpty()) throw new IOException("Enable at least one visual source in the active scene");
        if (layers.size() == 1) return layers.get(0).frame;
        return composite(layers, width, height);
    }

    private static boolean isVisual(RecordingStudioSource.Type type) {
        return type == RecordingStudioSource.Type.GAME_CAPTURE || type == RecordingStudioSource.Type.DESKTOP
                || type == RecordingStudioSource.Type.DISPLAY || type == RecordingStudioSource.Type.WEBCAM;
    }

    private static RecordingCaptureService.FrameData composite(java.util.List<Layer> layers, int width, int height) {
        BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = canvas.createGraphics();
        try {
            graphics.setColor(java.awt.Color.BLACK);
            graphics.fillRect(0, 0, width, height);
            for (int layerIndex = layers.size() - 1; layerIndex >= 0; layerIndex--) {
                Layer layer = layers.get(layerIndex);
                RecordingStudioSource s = layer.source;
                BufferedImage image = toImage(layer.frame);
                int left = s.getCropLeftPercent() * image.getWidth() / 100;
                int top = s.getCropTopPercent() * image.getHeight() / 100;
                int right = s.getCropRightPercent() * image.getWidth() / 100;
                int bottom = s.getCropBottomPercent() * image.getHeight() / 100;
                int sw = Math.max(1, image.getWidth() - left - right);
                int sh = Math.max(1, image.getHeight() - top - bottom);
                int x = s.getXPercent() * width / 100, y = s.getYPercent() * height / 100;
                int w = Math.max(1, s.getWidthPercent() * width / 100);
                int h = Math.max(1, s.getHeightPercent() * height / 100);
                graphics.drawImage(image, x, y, x + w, y + h, left, top, left + sw, top + sh, null);
            }
        } finally { graphics.dispose(); }
        byte[] rgba = new byte[width * height * 4];
        int offset = 0;
        for (int y = height - 1; y >= 0; y--) for (int x = 0; x < width; x++) {
            int argb = canvas.getRGB(x, y);
            rgba[offset++] = (byte) (argb >> 16); rgba[offset++] = (byte) (argb >> 8);
            rgba[offset++] = (byte) argb; rgba[offset++] = (byte) (argb >>> 24);
        }
        return new RecordingCaptureService.FrameData(width, height, rgba);
    }

    private static BufferedImage toImage(RecordingCaptureService.FrameData frame) {
        int width = frame.getWidth(), height = frame.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        byte[] rgba = frame.getRgba();
        int[] row = new int[width];
        for (int y = 0; y < height; y++) {
            int offset = (height - 1 - y) * width * 4;
            for (int x = 0; x < width; x++) {
                int p = offset + x * 4;
                row[x] = ((rgba[p + 3] & 255) << 24) | ((rgba[p] & 255) << 16)
                        | ((rgba[p + 1] & 255) << 8) | (rgba[p + 2] & 255);
            }
            image.setRGB(0, y, width, 1, row, 0, width);
        }
        return image;
    }

    public void writeFrame(RecordingCaptureService.FrameData frame, File output) throws Exception {
        minecraftWriter.writeFrame(frame, output);
    }
    public synchronized void onCaptureStopped() { stopWebcams(); }
    public synchronized void close() { stopWebcams(); }
    private void stopWebcams() {
        for (RecordingStudioWebcamCapture webcam : webcams.values()) webcam.close();
        webcams.clear();
    }
    private static final class Layer {
        final RecordingStudioSource source; final RecordingCaptureService.FrameData frame;
        Layer(RecordingStudioSource source, RecordingCaptureService.FrameData frame) { this.source = source; this.frame = frame; }
    }
}
