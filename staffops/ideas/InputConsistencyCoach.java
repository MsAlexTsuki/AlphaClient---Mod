package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/** Records local practice input samples and summarizes timing consistency. Runtime input hooks/UI are not wired. */
public final class InputConsistencyCoach {
    public static final String NAME = "Input Consistency Coach";
    public static final String CATEGORY = "Practice";
    private final Deque<Long> clickIntervals = new ArrayDeque<Long>();
    private final Deque<Double> movementSamples = new ArrayDeque<Double>();
    private int capacity = 200;

    public synchronized void setCapacity(int value) {
        if (value < 2 || value > 5000) throw new IllegalArgumentException("Capacity must be 2-5000");
        capacity = value; trim();
    }
    public synchronized void recordClickInterval(long millis) {
        if (millis <= 0 || millis > 10000) return;
        clickIntervals.addLast(millis); while (clickIntervals.size() > capacity) clickIntervals.removeFirst();
    }
    /** Records a normalized movement-input magnitude in the range 0..1. */
    public synchronized void recordMovement(double magnitude) {
        if (Double.isNaN(magnitude) || Double.isInfinite(magnitude)) return;
        movementSamples.addLast(Math.max(0.0, Math.min(1.0, magnitude))); trim();
    }
    private void trim() { while (clickIntervals.size() > capacity) clickIntervals.removeFirst(); while (movementSamples.size() > capacity) movementSamples.removeFirst(); }
    public synchronized List<Long> getClickIntervals() { return Collections.unmodifiableList(new ArrayList<Long>(clickIntervals)); }
    public synchronized double getAverageClickIntervalMillis() {
        if (clickIntervals.isEmpty()) return 0.0; double sum=0; for(Long n:clickIntervals) sum+=n; return sum/clickIntervals.size();
    }
    /** Coefficient of variation expressed as a percentage; lower means more even intervals. */
    public synchronized double getClickTimingVariationPercent() {
        if (clickIntervals.size()<2) return 0.0; double avg=getAverageClickIntervalMillis(); if(avg==0) return 0.0;
        double sum=0; for(Long n:clickIntervals){double d=n-avg;sum+=d*d;}
        return Math.sqrt(sum/clickIntervals.size())/avg*100.0;
    }
    public synchronized double getAverageMovementMagnitude() {
        if(movementSamples.isEmpty()) return 0.0; double sum=0; for(Double n:movementSamples) sum+=n; return sum/movementSamples.size();
    }
    public synchronized void reset(){clickIntervals.clear();movementSamples.clear();}
}