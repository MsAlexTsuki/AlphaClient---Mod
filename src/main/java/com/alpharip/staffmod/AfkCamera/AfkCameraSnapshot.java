package com.alpharip.staffmod.AfkCamera;

/** Immutable view of the AFK decision and the selected camera preset. */
public final class AfkCameraSnapshot {
    private final boolean enabled;
    private final boolean activityKnown;
    private final boolean idle;
    private final boolean cameraBehaviorRequested;
    private final long idleDurationMillis;
    private final int idleDelaySeconds;
    private final AfkCameraStyle style;

    public AfkCameraSnapshot(boolean enabled, boolean activityKnown, boolean idle,
            boolean cameraBehaviorRequested, long idleDurationMillis,
            int idleDelaySeconds, AfkCameraStyle style) {
        this.enabled = enabled;
        this.activityKnown = activityKnown;
        this.idle = idle;
        this.cameraBehaviorRequested = cameraBehaviorRequested;
        this.idleDurationMillis = Math.max(0L, idleDurationMillis);
        this.idleDelaySeconds = idleDelaySeconds;
        this.style = style == null ? AfkCameraStyle.DEFAULT : style;
    }

    public boolean isEnabled() { return enabled; }
    public boolean isActivityKnown() { return activityKnown; }
    public boolean isIdle() { return idle; }
    /** True means a future camera adapter may activate the selected behavior. */
    public boolean isCameraBehaviorRequested() { return cameraBehaviorRequested; }
    public long getIdleDurationMillis() { return idleDurationMillis; }
    public int getIdleDelaySeconds() { return idleDelaySeconds; }
    public AfkCameraStyle getStyle() { return style; }
}
