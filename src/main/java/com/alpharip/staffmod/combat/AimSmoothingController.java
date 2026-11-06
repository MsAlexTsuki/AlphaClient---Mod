package com.alpharip.staffmod.combat;

/** Applies the selected correction profile and bounds every rotation step. */
public final class AimSmoothingController {
    public float[] apply(AimAssistSettings.Mode mode, float yawDelta, float pitchDelta,
                         float yawSpeed, float pitchSpeed, float maximumStep) {
        float yawFactor = clamp(yawSpeed, 0.01F, 1.0F);
        float pitchFactor = clamp(pitchSpeed, 0.01F, 1.0F);
        switch (mode) {
            case SNAP:
                yawFactor = pitchFactor = 1.0F;
                break;
            case DYNAMIC:
                yawFactor = pitchFactor = clamp(0.08F + Math.max(Math.abs(yawDelta),
                        Math.abs(pitchDelta)) / 45.0F, 0.08F, 0.75F);
                break;
            case PREDICTIVE:
                break;
            case HORIZONTAL:
                pitchDelta = 0.0F;
                break;
            case VERTICAL:
                yawDelta = 0.0F;
                break;
            case ADAPTIVE:
                yawFactor = clamp(yawFactor * (1.0F + Math.abs(yawDelta) / 90.0F), 0.01F, 0.85F);
                pitchFactor = clamp(pitchFactor * (1.0F + Math.abs(pitchDelta) / 90.0F), 0.01F, 0.85F);
                break;
            case SMOOTH:
            default:
                break;
        }
        return new float[] {
            clamp(yawDelta * yawFactor, -maximumStep, maximumStep),
            clamp(pitchDelta * pitchFactor, -maximumStep, maximumStep)
        };
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
