package com.alpharip.staffmod.trainers.bridge;

/** Tracks the highest blocks-per-second result observed in this session. */
public final class BridgePersonalBest {
    private BridgeRunResult result;

    synchronized boolean consider(BridgeRunResult candidate) {
        if (candidate == null || candidate.getBlocksPlaced() == 0) {
            return false;
        }
        if (result == null
                || candidate.getBlocksPerSecond() > result.getBlocksPerSecond()) {
            result = candidate;
            return true;
        }
        return false;
    }

    public synchronized BridgeRunResult getResult() {
        return result;
    }

    public synchronized boolean hasResult() {
        return result != null;
    }

    public synchronized void clear() {
        result = null;
    }
}
