package com.alpharip.staffmod.trainers.bridge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Local, in-memory controller for Bridge Speed Trainer runs.
 *
 * This prototype exposes methods for a future Forge event adapter to call.
 * It does not detect course markers, place blocks, or read Minecraft events.
 */
public final class BridgeSpeedTrainer {
    public static final String NAME = "Bridge Speed Trainer";
    public static final String CATEGORY = "Practice";
    public static final String STATUS = "PROTOTYPE";
    public static final boolean REQUIRES_SUPABASE = false;
    public static final boolean REQUIRES_SERVER_SUPPORT = false;

    private final BridgeTrainerConfig config;
    private final List<BridgeRunResult> history = new ArrayList<BridgeRunResult>();
    private final BridgePersonalBest personalBest = new BridgePersonalBest();
    private BridgeRun activeRun;

    public BridgeSpeedTrainer() {
        this(new BridgeTrainerConfig());
    }

    public BridgeSpeedTrainer(BridgeTrainerConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config must not be null.");
        }
        this.config = config;
    }

    /** Starts a run. A second run cannot start while another run is active. */
    public synchronized BridgeRun startRun() {
        if (activeRun != null && !activeRun.isFinished()) {
            throw new IllegalStateException("A bridge run is already active.");
        }
        activeRun = new BridgeRun();
        return activeRun;
    }

    /**
     * Records a block placement supplied by a future client-side event adapter.
     *
     * @param misplaced true when the placement is classified as a misplacement
     */
    public synchronized void recordBlockPlacement(boolean misplaced) {
        requireActiveRun().recordBlockPlacement(misplaced);
    }

    /** Finishes the active run, stores its result, and updates the personal best. */
    public synchronized BridgeRunResult finishRun() {
        BridgeRun run = requireActiveRun();
        BridgeRunResult result = run.finish();
        history.add(result);
        trimHistory();
        personalBest.consider(result);
        activeRun = null;
        return result;
    }

    /** Cancels the active run without adding it to history. */
    public synchronized boolean cancelRun() {
        if (activeRun == null) {
            return false;
        }
        activeRun = null;
        return true;
    }

    public synchronized boolean hasActiveRun() {
        return activeRun != null && !activeRun.isFinished();
    }

    public synchronized BridgeRun getActiveRun() {
        return activeRun;
    }

    public synchronized List<BridgeRunResult> getHistory() {
        return Collections.unmodifiableList(
                new ArrayList<BridgeRunResult>(history));
    }

    public synchronized BridgeRunResult getPersonalBest() {
        return personalBest.getResult();
    }

    public BridgeTrainerConfig getConfig() {
        return config;
    }

    private BridgeRun requireActiveRun() {
        if (activeRun == null || activeRun.isFinished()) {
            throw new IllegalStateException("There is no active bridge run.");
        }
        return activeRun;
    }

    private void trimHistory() {
        while (history.size() > config.getMaximumHistoryEntries()) {
            history.remove(0);
        }
    }
}
