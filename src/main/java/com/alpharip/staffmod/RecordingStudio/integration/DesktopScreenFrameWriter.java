package com.alpharip.staffmod.RecordingStudio.integration;

import com.alpharip.staffmod.RecordingStudio.capture.RecordingCaptureService;
import java.awt.AWTException;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Captures a complete physical display using the Java AWT desktop capture API. */
public final class DesktopScreenFrameWriter {
    public static final class Display {
        private final String id;
        private final String name;
        private final boolean primary;

        private Display(GraphicsDevice device, boolean primary, int index) {
            this.id = device.getIDstring();
            this.name = "Display " + (index + 1) + (primary ? " (Primary)" : "");
            this.primary = primary;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public boolean isPrimary() { return primary; }
    }

    private DesktopScreenFrameWriter() { }

    public static List<Display> getDisplays() {
        try {
            GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice primary = environment.getDefaultScreenDevice();
            GraphicsDevice[] devices = environment.getScreenDevices();
            List<Display> result = new ArrayList<Display>();
            for (int i = 0; i < devices.length; i++) {
                result.add(new Display(devices[i], devices[i].equals(primary), i));
            }
            return Collections.unmodifiableList(result);
        } catch (RuntimeException unavailable) {
            return Collections.emptyList();
        }
    }

    public static RecordingCaptureService.FrameData capturePrimary() throws IOException {
        GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        return capture(environment.getDefaultScreenDevice());
    }

    /** Captures a specifically selected non-primary display. */
    public static RecordingCaptureService.FrameData captureDisplay(String deviceId) throws IOException {
        if (deviceId == null || deviceId.trim().isEmpty()) {
            throw new IOException("Select an additional display before recording");
        }
        GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice primary = environment.getDefaultScreenDevice();
        for (GraphicsDevice device : environment.getScreenDevices()) {
            if (deviceId.equals(device.getIDstring())) {
                if (device.equals(primary)) throw new IOException("Display source must target a non-primary display");
                return capture(device);
            }
        }
        throw new IOException("The selected display is no longer available");
    }

    private static RecordingCaptureService.FrameData capture(GraphicsDevice device) throws IOException {
        if (device == null) throw new IOException("No desktop display is available");
        try {
            Rectangle bounds = device.getDefaultConfiguration().getBounds();
            if (bounds == null || bounds.width < 1 || bounds.height < 1) {
                throw new IOException("Display dimensions are unavailable");
            }
            long bytes = (long) bounds.width * bounds.height * 4L;
            if (bytes > Integer.MAX_VALUE) throw new IOException("Display resolution is too large to capture");
            BufferedImage image = new Robot(device).createScreenCapture(bounds);
            byte[] rgba = new byte[(int) bytes];
            int offset = 0;
            // FrameData uses bottom-up rows to share the existing PNG writer with OpenGL captures.
            for (int y = image.getHeight() - 1; y >= 0; y--) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int argb = image.getRGB(x, y);
                    rgba[offset++] = (byte) ((argb >>> 16) & 255);
                    rgba[offset++] = (byte) ((argb >>> 8) & 255);
                    rgba[offset++] = (byte) (argb & 255);
                    rgba[offset++] = (byte) ((argb >>> 24) & 255);
                }
            }
            return new RecordingCaptureService.FrameData(image.getWidth(), image.getHeight(), rgba);
        } catch (AWTException | SecurityException | java.awt.HeadlessException failure) {
            throw new IOException("Desktop capture is unavailable: " + failure.getMessage(), failure);
        }
    }
}
