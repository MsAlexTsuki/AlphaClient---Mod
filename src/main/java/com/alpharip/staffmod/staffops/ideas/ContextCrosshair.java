package com.alpharip.staffmod.staffops.ideas;
/** Crosshair context state calculation; target-state acquisition and crosshair rendering are pending. */
public final class ContextCrosshair {
    public enum State { NO_TARGET, OUT_OF_REACH, IN_REACH, BLOCKING, EATING }
    public State determineState(boolean hasTarget, boolean inReach, boolean blocking, boolean eating) {
        if (!hasTarget) return State.NO_TARGET;
        if (eating) return State.EATING;
        if (blocking) return State.BLOCKING;
        return inReach ? State.IN_REACH : State.OUT_OF_REACH;
    }
}
