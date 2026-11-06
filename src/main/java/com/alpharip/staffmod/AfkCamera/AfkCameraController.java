package com.alpharip.staffmod.AfkCamera;

/** Evaluates AFK state without manipulating the Minecraft camera. */
public final class AfkCameraController {
    private final AfkCameraSettings settings;
    private final AfkActivityTracker activityTracker;

    public AfkCameraController(AfkCameraSettings settings, AfkActivityTracker activityTracker) {
        if (settings == null || activityTracker == null) {
            throw new IllegalArgumentException("settings and activityTracker are required");
        }
        this.settings = settings;
        this.activityTracker = activityTracker;
    }

    public void recordActivity(long timestampMillis) {
        activityTracker.recordActivity(timestampMillis);
    }

    public AfkCameraSnapshot snapshot(long nowMillis) {
        boolean known = activityTracker.hasActivity();
        long idleMillis = activityTracker.getIdleDurationMillis(nowMillis);
        int delaySeconds = settings.getIdleDelaySeconds();
        boolean idle = known && idleMillis >= delaySeconds * 1000L;
        boolean requested = settings.isEnabled() && idle;

        return new AfkCameraSnapshot(settings.isEnabled(), known, idle, requested,
                idleMillis, delaySeconds, settings.getStyle());
    }

    public void resetActivity() {
        activityTracker.reset();
    }
}
