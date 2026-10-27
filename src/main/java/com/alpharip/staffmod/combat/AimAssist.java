// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.combat;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import com.alpharip.staffmod.permissions.SupabaseCommunityService;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.UUID;

/**
 * Founder-only smooth aim correction.
 * It only corrects toward a visible player already inside the configured angle gate.
 */
public final class AimAssist {
    private static final double MAX_RANGE = 3.2D;
    private static final float MAX_ROTATION_STEP = 1.5F;
    private UUID pendingTargetId;
    private long pendingTargetSince;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.theWorld == null || mc.currentScreen != null) {
            resetPendingTarget();
            return;
        }

        if (!SupabaseCommunityService.isSignedInFounder()) {
            resetPendingTarget();
            return;
        }

        AlphaModule module = AlphaModuleRegistry.find("aim-assist");
        if (module == null || !module.isImplemented() || !module.isUnlocked() || !module.isEnabled()) {
            resetPendingTarget();
            return;
        }

        TargetAim targetAim = findCrosshairAdjacentTarget(mc);
        if (targetAim == null) {
            resetPendingTarget();
            return;
        }

        long now = System.currentTimeMillis();
        UUID targetId = targetAim.player.getUniqueID();
        if (!targetId.equals(pendingTargetId)) {
            pendingTargetId = targetId;
            pendingTargetSince = now;
        }
        if (now - pendingTargetSince < AimAssistSettings.getReactionDelayMs()) return;

        double dx = targetAim.x - mc.thePlayer.posX;
        double dz = targetAim.z - mc.thePlayer.posZ;
        double dy = targetAim.y - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

        float targetYaw = (float) (Math.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F;
        float targetPitch = (float) (-(Math.atan2(dy, horizontalDistance) * 180.0D / Math.PI));
        float yawDelta = MathHelper.wrapAngleTo180_float(targetYaw - mc.thePlayer.rotationYaw);
        float pitchDelta = MathHelper.wrapAngleTo180_float(targetPitch - mc.thePlayer.rotationPitch);

        float baseSpeed = AimAssistSettings.getCorrectionSpeed() / 100.0F;
        float yawSpeed = baseSpeed * AimAssistSettings.getHorizontalSpeed() / 10.0F;
        float pitchSpeed = baseSpeed * AimAssistSettings.getVerticalSpeed() / 10.0F;

        mc.thePlayer.rotationYaw += clamp(yawDelta * yawSpeed, -MAX_ROTATION_STEP, MAX_ROTATION_STEP);
        mc.thePlayer.rotationPitch = MathHelper.clamp_float(
                mc.thePlayer.rotationPitch
                        + clamp(pitchDelta * pitchSpeed, -MAX_ROTATION_STEP, MAX_ROTATION_STEP),
                -90.0F,
                90.0F
        );
    }

    private TargetAim findCrosshairAdjacentTarget(Minecraft mc) {
        TargetAim bestTarget = null;
        float bestAngle = AimAssistSettings.getMaximumAngleDegrees();

        for (Object object : mc.theWorld.playerEntities) {
            if (!(object instanceof EntityPlayer)) continue;
            EntityPlayer candidate = (EntityPlayer) object;
            if (candidate == mc.thePlayer || candidate.isDead || !candidate.isEntityAlive()
                    || candidate.isInvisible()
                    || mc.thePlayer.getDistanceToEntity(candidate) > MAX_RANGE
                    || !mc.thePlayer.canEntityBeSeen(candidate)) {
                continue;
            }

            TargetAim candidateAim = findClosestBodyAimPoint(mc.thePlayer, candidate);
            if (candidateAim != null && candidateAim.angle < bestAngle) {
                bestAngle = candidateAim.angle;
                bestTarget = candidateAim;
            }
        }
        return bestTarget;
    }

    private TargetAim findClosestBodyAimPoint(EntityPlayer viewer, EntityPlayer target) {
        AxisAlignedBB box = target.getEntityBoundingBox();
        double centerX = (box.minX + box.maxX) * 0.5D;
        double centerZ = (box.minZ + box.maxZ) * 0.5D;
        double bodyHeight = box.maxY - box.minY;
        TargetAim bestAim = null;
        float bestAngle = AimAssistSettings.getMaximumAngleDegrees();

        for (double heightFraction : AimAssistSettings.getTargetArea().getHeightSamples()) {
            double aimY = box.minY + bodyHeight * heightFraction;
            float angle = getAimAngle(viewer, centerX, aimY, centerZ);
            if (angle < bestAngle) {
                bestAngle = angle;
                bestAim = new TargetAim(target, centerX, aimY, centerZ, angle);
            }
        }
        return bestAim;
    }

    private float getAimAngle(EntityPlayer viewer, double targetX, double targetY, double targetZ) {
        double dx = targetX - viewer.posX;
        double dz = targetZ - viewer.posZ;
        double dy = targetY - (viewer.posY + viewer.getEyeHeight());
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        float targetYaw = (float) (Math.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F;
        float targetPitch = (float) (-(Math.atan2(dy, horizontalDistance) * 180.0D / Math.PI));
        float yawDelta = MathHelper.wrapAngleTo180_float(targetYaw - viewer.rotationYaw);
        float pitchDelta = MathHelper.wrapAngleTo180_float(targetPitch - viewer.rotationPitch);
        return (float) Math.sqrt(yawDelta * yawDelta + pitchDelta * pitchDelta);
    }

    private void resetPendingTarget() {
        pendingTargetId = null;
        pendingTargetSince = 0L;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static final class TargetAim {
        private final EntityPlayer player;
        private final double x, y, z;
        private final float angle;

        private TargetAim(EntityPlayer player, double x, double y, double z, float angle) {
            this.player = player;
            this.x = x;
            this.y = y;
            this.z = z;
            this.angle = angle;
        }
    }
}
