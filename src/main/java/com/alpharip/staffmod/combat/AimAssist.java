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

/**
 * Founder-only smooth aim correction.
 *
 * This module only adjusts client-side view rotation for a visible player whose
 * full body is already close to the crosshair. It never attacks, changes
 * hitboxes, sends packets, or acquires targets outside the small angle gate.
 */
public final class AimAssist {
    private static final double MAX_RANGE = 3.2D;
    private static final float MAX_TARGET_ANGLE = 10.0F;
    private static final float SMOOTHING = 0.10F;
    private static final float MAX_ROTATION_STEP = 1.5F;
    private static final double[] BODY_HEIGHT_SAMPLES = {0.10D, 0.30D, 0.50D, 0.70D, 0.90D};

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.theWorld == null || mc.currentScreen != null) return;

        // Do not rely on rank inheritance or offline-test rank fallbacks for this module.
        if (!SupabaseCommunityService.isSignedInFounder()) return;

        AlphaModule module = AlphaModuleRegistry.find("aim-assist");
        if (module == null || !module.isImplemented() || !module.isUnlocked() || !module.isEnabled()) return;

        TargetAim targetAim = findCrosshairAdjacentTarget(mc);
        if (targetAim == null) return;

        double dx = targetAim.player.posX - mc.thePlayer.posX;
        double dz = targetAim.player.posZ - mc.thePlayer.posZ;
        double dy = targetAim.aimY - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

        float targetYaw = (float) (Math.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F;
        float targetPitch = (float) (-(Math.atan2(dy, horizontalDistance) * 180.0D / Math.PI));
        float yawDelta = MathHelper.wrapAngleTo180_float(targetYaw - mc.thePlayer.rotationYaw);
        float pitchDelta = MathHelper.wrapAngleTo180_float(targetPitch - mc.thePlayer.rotationPitch);

        mc.thePlayer.rotationYaw += clamp(yawDelta * SMOOTHING, -MAX_ROTATION_STEP, MAX_ROTATION_STEP);
        mc.thePlayer.rotationPitch = MathHelper.clamp_float(
                mc.thePlayer.rotationPitch
                        + clamp(pitchDelta * SMOOTHING, -MAX_ROTATION_STEP, MAX_ROTATION_STEP),
                -90.0F,
                90.0F
        );
    }

    private TargetAim findCrosshairAdjacentTarget(Minecraft mc) {
        TargetAim bestTarget = null;
        float bestAngle = MAX_TARGET_ANGLE;

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

    /**
     * Tests points distributed from the lower body to the top of the player's
     * bounding box, so aim correction can activate on the legs, torso, or head.
     */
    private TargetAim findClosestBodyAimPoint(EntityPlayer viewer, EntityPlayer target) {
        AxisAlignedBB box = target.getEntityBoundingBox();
        double centerX = (box.minX + box.maxX) * 0.5D;
        double centerZ = (box.minZ + box.maxZ) * 0.5D;
        double bodyHeight = box.maxY - box.minY;

        TargetAim bestAim = null;
        float bestAngle = MAX_TARGET_ANGLE;

        for (double heightFraction : BODY_HEIGHT_SAMPLES) {
            double aimY = box.minY + bodyHeight * heightFraction;
            float angle = getAimAngle(viewer, centerX, aimY, centerZ);
            if (angle < bestAngle) {
                bestAngle = angle;
                bestAim = new TargetAim(target, aimY, angle);
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

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static final class TargetAim {
        private final EntityPlayer player;
        private final double aimY;
        private final float angle;

        private TargetAim(EntityPlayer player, double aimY, float angle) {
            this.player = player;
            this.aimY = aimY;
            this.angle = angle;
        }
    }
}
