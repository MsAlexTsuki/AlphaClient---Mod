package com.alpharip.staffmod.RecordingStudio.capture;
/** Settings for standalone manual Minecraft frame recording. */
public final class RecordingCaptureSettings {private long intervalMillis=1000L;public synchronized long getIntervalMillis(){return intervalMillis;}public synchronized void setIntervalMillis(long v){if(v<100L||v>60000L)throw new IllegalArgumentException("Capture interval must be 100..60000 ms");intervalMillis=v;}}
