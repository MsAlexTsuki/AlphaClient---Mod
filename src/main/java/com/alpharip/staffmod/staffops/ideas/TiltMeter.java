package com.alpharip.staffmod.staffops.ideas;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
/** Compares the latest five fight scores with the session mean; score collection and warning UI are pending. */
public final class TiltMeter {
    private final List<Double> sessionScores = new ArrayList<Double>();
    private final Deque<Double> recentScores = new ArrayDeque<Double>();
    public void recordFight(double performanceScore) {
        if (Double.isNaN(performanceScore) || Double.isInfinite(performanceScore)) return;
        sessionScores.add(Double.valueOf(performanceScore));
        recentScores.addLast(Double.valueOf(performanceScore));
        while (recentScores.size() > 5) recentScores.removeFirst();
    }
    public double getSessionAverage() {
        if (sessionScores.isEmpty()) return 0D;
        double total=0D; for (Double score : sessionScores) total += score.doubleValue();
        return total / sessionScores.size();
    }
    public double getRecentAverage() {
        if (recentScores.isEmpty()) return 0D;
        double total=0D; for (Double score : recentScores) total += score.doubleValue();
        return total / recentScores.size();
    }
    public boolean isPerformanceDropping(double minimumDrop) {
        return recentScores.size() == 5 && getRecentAverage() < getSessionAverage() - Math.max(0D, minimumDrop);
    }
    public void resetSession() { sessionScores.clear(); recentScores.clear(); }
}
