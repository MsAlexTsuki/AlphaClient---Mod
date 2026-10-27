package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Records supplied knockback response measurements; it does not evaluate live combat. */
public final class KnockbackAnalyzer {
    public static final class Response {
        private final long timestampMillis;
        private final double expectedDistance;
        private final double observedDistance;

        public Response(long timestampMillis, double expectedDistance, double observedDistance) {
            this.timestampMillis = timestampMillis;
            this.expectedDistance = expectedDistance;
            this.observedDistance = observedDistance;
        }
        public long getTimestampMillis() { return timestampMillis; }
        public double getExpectedDistance() { return expectedDistance; }
        public double getObservedDistance() { return observedDistance; }
        public double getDifference() { return observedDistance - expectedDistance; }
    }

    private final List<Response> responses = new ArrayList<Response>();
    public synchronized void add(Response response) { if (response != null) responses.add(response); }
    public synchronized List<Response> getResponses() {
        return Collections.unmodifiableList(new ArrayList<Response>(responses));
    }
    public synchronized void clear() { responses.clear(); }
}