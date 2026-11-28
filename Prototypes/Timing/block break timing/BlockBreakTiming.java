package com.alpharip.staffmod.timing.blockbreak;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Local block-break timing collector. Forge event detection and HUD rendering are not included. */
public class BlockBreakTiming {
    public static final String NAME = "Block Break Timing";
    public static final String CATEGORY = "Timing";
    public static final boolean REQUIRES_SUPABASE = false;
    public static final boolean REQUIRES_SERVER_SUPPORT = false;

    private final BlockBreakTimingConfig config;
    private final List<BlockBreakSample> samples = new ArrayList<BlockBreakSample>();
    private long lastBreakTimestamp = -1L;

    public BlockBreakTiming() {
        this(new BlockBreakTimingConfig());
    }

    public BlockBreakTiming(BlockBreakTimingConfig config) {
        if (config == null) throw new IllegalArgumentException("config cannot be null");
        this.config = config;
    }

    /** Compatibility API: records an already calculated non-negative interval in milliseconds. */
    public void record(long sample) {
        if (sample >= 0L) {
            samples.add(new BlockBreakSample(sample, -1L));
            trimSamples();
        }
    }

    /** Records a break timestamp and derives the interval from the previous break. */
    public boolean recordBreak(long timestampMillis) {
        if (timestampMillis < 0L) return false;
        if (lastBreakTimestamp >= 0L && timestampMillis < lastBreakTimestamp) return false;
        if (lastBreakTimestamp < 0L) {
            lastBreakTimestamp = timestampMillis;
            return false;
        }
        long interval = timestampMillis - lastBreakTimestamp;
        lastBreakTimestamp = timestampMillis;
        samples.add(new BlockBreakSample(interval, timestampMillis));
        trimSamples();
        return true;
    }

    public int getCount() { return samples.size(); }

    /** Returns interval values in milliseconds, preserving the original API. */
    public List<Long> getSamples() {
        List<Long> values = new ArrayList<Long>();
        for (BlockBreakSample sample : samples) values.add(sample.getIntervalMillis());
        return Collections.unmodifiableList(values);
    }

    public List<BlockBreakSample> getDetailedSamples() {
        return Collections.unmodifiableList(new ArrayList<BlockBreakSample>(samples));
    }

    public double getAverage() { return getStats().getAverageMillis(); }

    public BlockBreakTimingStats getStats() {
        return new BlockBreakTimingStats(samples, config.getSlowThresholdMillis());
    }

    public BlockBreakTimingConfig getConfig() { return config; }

    public void clear() {
        samples.clear();
        lastBreakTimestamp = -1L;
    }

    private void trimSamples() {
        while (samples.size() > config.getMaximumSamples()) samples.remove(0);
    }
}
