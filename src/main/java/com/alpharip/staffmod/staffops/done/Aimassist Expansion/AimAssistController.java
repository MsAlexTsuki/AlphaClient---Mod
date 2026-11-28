package com.alpharip.staffmod.aimassistexpansion;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import java.util.List;

/** Coordinates target selection and mode calculations; contains no attack or packet actions. */
public final class AimAssistController {
    private final AimTargetSelector selector = new AimTargetSelector();
    private final AimRotationCalculator calculator = new AimRotationCalculator();
    private final AimSmoothingController smoothing = new AimSmoothingController();

    public void update(Minecraft mc, AimAssistSettings settings) {
        if (mc == null || mc.thePlayer == null || mc.theWorld == null || settings == null
                || !settings.isEnabled() || mc.currentScreen != null) return;
        List<AimTarget> targets = selector.collect(mc, settings.getRange());
        AimTarget best = chooseTarget(mc, targets, settings);
        if (best == null) return;

        double tx = best.getX(), ty = best.getY(), tz = best.getZ();
        if (settings.getMode() == AimAssistMode.PREDICTIVE) {
            float ticks = settings.getPredictionTicks();
            tx += best.getVelocityX() * ticks;
            ty += best.getVelocityY() * ticks;
            tz += best.getVelocityZ() * ticks;
        }
        float[] delta = calculator.calculate(mc.thePlayer.posX,
                mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ,
                mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, tx, ty, tz);
        if (Math.sqrt(delta[0] * delta[0] + delta[1] * delta[1]) > settings.getMaximumAngle()) return;
        float[] step = smoothing.apply(settings.getMode(), delta[0], delta[1],
                settings.getSpeed(), settings.getMaximumStep());
        mc.thePlayer.rotationYaw += step[0];
        mc.thePlayer.rotationPitch = Math.max(-90.0F,
                Math.min(90.0F, mc.thePlayer.rotationPitch + step[1]));
    }

    private AimTarget chooseTarget(Minecraft mc, List<AimTarget> targets, AimAssistSettings settings) {
        AimTarget best = null;
        double bestScore = Double.MAX_VALUE;
        for (AimTarget target : targets) {
            float[] delta = calculator.calculate(mc.thePlayer.posX,
                    mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ,
                    mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch,
                    target.getX(), target.getY(), target.getZ());
            double angle = Math.sqrt(delta[0] * delta[0] + delta[1] * delta[1]);
            if (angle > settings.getMaximumAngle()) continue;
            double score = settings.getPriority() == AimTargetSelector.Priority.CLOSEST
                    ? target.getDistance() : angle;
            if (score < bestScore) { bestScore = score; best = target; }
        }
        return best;
    }
}
