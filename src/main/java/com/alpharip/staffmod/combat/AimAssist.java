--- Made By MsAlexTsuki ---
    
package com.alpharip.staffmod.combat;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import com.alpharip.staffmod.permissions.SupabaseCommunityService;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * Founder-only smooth aim correction.
 *
 * This module only adjusts client-side view rotation for a visible player that
 * is already close to the crosshair. It never attacks, changes hitboxes, or
 * sends packets, and it does not acquire targets outside the small angle gate.
 */
public final class AimAssist {
    private static final double MAX_RANGE = 4.5D;
    private static final float MAX_TARGET_ANGLE = 10.0F;
    private static final float SMOOTHING = 0.10F;
    private static final float MAX_ROTATION_STEP = 1.5F;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.theWorld == null || mc.currentScreen != null) return;

        // Do not rely on rank inheritance or offline-test rank fallbacks for this module.
        if (!SupabaseCommunityService.isSignedInFounder()) return;

        AlphaModule module = AlphaModuleRegistry.find("aim-assist");
        if (module == null || !module.isImplemented() || !module.isUnlocked() || !module.isEnabled()) return;

        EntityPlayer target = findCrosshairAdjacentTarget(mc);
        if (target == null) return;

        double dx = target.posX - mc.thePlayer.posX;
        double dz = target.posZ - mc.thePlayer.posZ;
        double dy = target.posY + target.getEyeHeight()
                - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
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

    private EntityPlayer findCrosshairAdjacentTarget(Minecraft mc) {
        EntityPlayer bestTarget = null;
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

            float angle = getAimAngle(mc.thePlayer, candidate);
            if (angle < bestAngle) {
                bestAngle = angle;
                bestTarget = candidate;
            }
        }

        return bestTarget;
    }

    private float getAimAngle(EntityPlayer viewer, EntityPlayer target) {
        double dx = target.posX - viewer.posX;
        double dz = target.posZ - viewer.posZ;
        double dy = target.posY + target.getEyeHeight()
                - (viewer.posY + viewer.getEyeHeight());
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
}
