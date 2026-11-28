package com.alpharip.staffmod.timing.blockplacement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Local prototype for measuring intervals between consecutive block placements.
 * Placement events must currently be supplied by the caller.
 */
public class BlockPlacementTiming {
    public static final String NAME = "Block Placement Timing";
    public static final String CATEGORY = "Timing";
    public static final boolean REQUIRES_SUPABASE = false;
    public static final boolean REQUIRES_SERVER_SUPPORT = false;

    private final List<BlockPlacementSample> samples = new ArrayList<BlockPlacementSample>();
    private BlockPlacementTimingConfig config;
    private long previousPlacementTimestamp = -1L;

    public BlockPlacementTiming() {
        this(new BlockPlacementTimingConfig());
    }

    public BlockPlacementTiming(BlockPlacementTimingConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config cannot be null");
        }
        this.config = config;
    }

    /** Records a measured interval in milliseconds; retained for compatibility. */
    public void record(long sample) {
        if (sample < 0L) {
            return;
        }
        addSample(new BlockPlacementSample(sample, -1L));
    }

    /**
     * Records a placement timestamp in milliseconds and derives the interval
     * from the previous placement. The first placement establishes the baseline.
     *
     * @return the measured interval, or -1 when this is the first placement
     */
    public long recordPlacement(long timestampMillis) {
        if (timestampMillis < 0L) {
            return -1L;
        }
        if (previousPlacementTimestamp < 0L) {
            previousPlacementTimestamp = timestampMillis;
            return -1L;
        }
        if (timestampMillis < previousPlacementTimestamp) {
            return -1L;
        }

        long interval = timestampMillis - previousPlacementTimestamp;
        previousPlacementTimestamp = timestampMillis;
        addSample(new BlockPlacementSample(interval, timestampMillis));
        return interval;
    }

    private void addSample(BlockPlacementSample sample) {
        samples.add(sample);
        while (samples.size() > config.getMaxSamples()) {
            samples.remove(0);
        }
    }

    public int getCount() {
        return samples.size();
    }

    public List<Long> getSamples() {
        List<Long> values = new ArrayList<Long>();
        for (BlockPlacementSample sample : samples) {
            values.add(sample.getIntervalMillis());
        }
        return Collections.unmodifiableList(values);
    }

    public List<BlockPlacementSample> getPlacementSamples() {
        return Collections.unmodifiableList(new ArrayList<BlockPlacementSample>(samples));
    }

    public double getAverage() {
        if (samples.isEmpty()) {
            return 0.0D;
        }
        double total = 0.0D;
        for (BlockPlacementSample sample : samples) {
            total += sample.getIntervalMillis();
        }
        return total / samples.size();
    }

    public BlockPlacementTimingStats getStats() {
        return new BlockPlacementTimingStats(samples, config.getSlowPlacementThresholdMillis());
    }

    public BlockPlacementTimingConfig getConfig() {
        return config;
    }

    public void setConfig(BlockPlacementTimingConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config cannot be null");
        }
        this.config = config;
        while (samples.size() > config.getMaxSamples()) {
            samples.remove(0);
        }
    }

    public void clear() {
        samples.clear();
        previousPlacementTimestamp = -1L;
    }
}
