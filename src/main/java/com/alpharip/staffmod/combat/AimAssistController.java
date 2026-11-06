package com.alpharip.staffmod.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import java.util.List;
import java.util.UUID;

/** Coordinates target selection, optional prediction and mode-specific aim correction. */
public final class AimAssistController {
    private final AimTargetSelector selector = new AimTargetSelector();
    private final AimRotationCalculator calculator = new AimRotationCalculator();
    private final AimSmoothingController smoothing = new AimSmoothingController();
    private UUID pendingTargetId;
    private long pendingTargetSince;

    public void update(Minecraft mc) {
        if (mc == null || mc.thePlayer == null || mc.theWorld == null || mc.currentScreen != null) {
            resetPendingTarget();
            return;
        }
        List<AimTarget> targets = selector.collect(mc);
        AimTarget target = chooseTarget(targets);
        if (target == null) {
            resetPendingTarget();
            return;
        }
        EntityPlayer player = target.getPlayer();
        long now = System.currentTimeMillis();
        UUID targetId = player.getUniqueID();
        if (!targetId.equals(pendingTargetId)) {
            pendingTargetId = targetId;
            pendingTargetSince = now;
        }
        if (now - pendingTargetSince < AimAssistSettings.getReactionDelayMs()) return;

        double targetX = target.getX();
        double targetY = target.getY();
        double targetZ = target.getZ();
        if (AimAssistSettings.getMode() == AimAssistSettings.Mode.PREDICTIVE) {
            float ticks = AimAssistSettings.getPredictionTicks();
            targetX += target.getVelocityX() * ticks;
            targetY += target.getVelocityY() * ticks;
            targetZ += target.getVelocityZ() * ticks;
        }
        float[] delta = calculator.calculate(mc.thePlayer.posX,
                mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ,
                mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, targetX, targetY, targetZ);
        if (Math.sqrt(delta[0] * delta[0] + delta[1] * delta[1])
                > AimAssistSettings.getMaximumAngleDegrees()) return;

        float baseSpeed = AimAssistSettings.getCorrectionSpeed() / 100.0F;
        float yawSpeed = baseSpeed * AimAssistSettings.getHorizontalSpeed() / 10.0F;
        float pitchSpeed = baseSpeed * AimAssistSettings.getVerticalSpeed() / 10.0F;
        float[] step = smoothing.apply(AimAssistSettings.getMode(), delta[0], delta[1],
                yawSpeed, pitchSpeed, AimAssistSettings.getMaximumRotationStep());
        mc.thePlayer.rotationYaw += step[0];
        mc.thePlayer.rotationPitch = Math.max(-90.0F,
                Math.min(90.0F, mc.thePlayer.rotationPitch + step[1]));
    }

    private AimTarget chooseTarget(List<AimTarget> targets) {
        AimTarget best = null;
        double bestScore = Double.MAX_VALUE;
        for (AimTarget target : targets) {
            double score = AimAssistSettings.getTargetPriority() == AimAssistSettings.TargetPriority.CLOSEST
                    ? target.getDistance() : target.getAngle();
            if (score < bestScore) {
                bestScore = score;
                best = target;
            }
        }
        return best;
    }

    public void resetPendingTarget() {
        pendingTargetId = null;
        pendingTargetSince = 0L;
    }
}
