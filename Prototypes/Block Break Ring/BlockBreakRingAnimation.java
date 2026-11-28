package com.alpharip.staffmod.blockbreakring;

/** Smooths block damage progress without depending on the Alpha Client Core. */
public final class BlockBreakRingAnimation {
    private float progress;

    public void update(float target, boolean smooth) {
        target = clamp(target);
        if (!smooth) {
            progress = target;
            return;
        }
        progress += (target - progress) * 0.38F;
        if (Math.abs(target - progress) < 0.0025F) progress = target;
    }

    public void setProgress(float value) { progress = clamp(value); }
    public float getProgress() { return progress; }
    public void reset() { progress = 0.0F; }

    private static float clamp(float value) {
        if (Float.isNaN(value) || Float.isInfinite(value)) return 0.0F;
        return Math.max(0.0F, Math.min(1.0F, value));
    }
}
