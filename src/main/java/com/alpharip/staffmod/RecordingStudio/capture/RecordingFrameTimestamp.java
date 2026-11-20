package com.alpharip.staffmod.RecordingStudio.capture;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/** Per-frame active-timeline timestamp sidecar, stored independently from the PNG. */
public final class RecordingFrameTimestamp {
    private RecordingFrameTimestamp() { }

    public static File sidecarFor(File frame) {
        String name = frame.getName();
        if (!name.endsWith(".png")) throw new IllegalArgumentException("Frame must be a PNG file");
        return new File(frame.getParentFile(), name.substring(0, name.length() - 4) + ".time");
    }

    public static void write(File frame, long timestampMillis) throws IOException {
        if (frame == null || timestampMillis < 0L) throw new IllegalArgumentException("Frame and non-negative timestamp are required");
        File destination = sidecarFor(frame);
        File temporary = new File(destination.getParentFile(), destination.getName() + ".tmp");
        try {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(temporary))) {
                writer.write(Long.toString(timestampMillis));
                writer.newLine();
            }
            try {
                Files.move(temporary.toPath(), destination.toPath(),
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException unsupported) {
                Files.move(temporary.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            if (temporary.exists() && !temporary.delete()) temporary.deleteOnExit();
        }
    }

    /** Legacy PNGs without a sidecar retain their original fixed-interval fallback. */
    public static long read(File frame, long fallbackMillis) {
        if (frame == null) return Math.max(0L, fallbackMillis);
        File sidecar = sidecarFor(frame);
        if (!sidecar.isFile()) return Math.max(0L, fallbackMillis);
        try (BufferedReader reader = new BufferedReader(new FileReader(sidecar))) {
            long value = Long.parseLong(reader.readLine());
            return Math.max(0L, value);
        } catch (Exception invalidOrUnreadable) {
            return Math.max(0L, fallbackMillis);
        }
    }
}
