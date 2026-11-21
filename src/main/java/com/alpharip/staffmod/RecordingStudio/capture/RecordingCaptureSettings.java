package com.alpharip.staffmod.RecordingStudio.capture;

/** Source frame pacing for direct video recording. */
public final class RecordingCaptureSettings {
    private long intervalMillis = 33L;

    public synchronized long getIntervalMillis() { return intervalMillis; }

    public synchronized void setIntervalMillis(long value) {
        if (value < 4L || value > 60000L) {
            throw new IllegalArgumentException("Capture interval must be 4..60000 ms");
        }
        intervalMillis = value;
    }
}
