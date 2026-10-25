package com.alpharip.staffmod.combat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;

/**
 * Shared first-step target selection for the planned combat modules.
 *
 * This class only selects a client-side candidate. It does not attack, change
 * rotation, modify hitboxes, or send packets. Later modules can consume the
 * same result instead of implementing competing target-selection rules.
 */
public final class CombatTargeting {
    public enum Priority {
        DISTANCE,
        CROSSHAIR
    }

    private CombatTargeting() {
    }

    /** Returns eligible living players within range, ordered by the requested priority. */
    public static List<EntityPlayer> getCandidates(final double range, final Priority priority) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.theWorld == null || range <= 0.0D) {
            return Collections.emptyList();
        }

        final EntityPlayer viewer = mc.thePlayer;
        List<EntityPlayer> candidates = new ArrayList<EntityPlayer>();
        for (Object object : mc.theWorld.playerEntities) {
            if (!(object instanceof EntityPlayer)) continue;
            EntityPlayer candidate = (EntityPlayer) object;
            if (candidate == viewer || candidate.isDead || !candidate.isEntityAlive()
                    || candidate.isInvisible() || viewer.getDistanceToEntity(candidate) > range) {
                continue;
            }
            candidates.add(candidate);
        }

        Collections.sort(candidates, new Comparator<EntityPlayer>() {
            @Override
            public int compare(EntityPlayer first, EntityPlayer second) {
                if (priority == Priority.CROSSHAIR) {
                    float firstAngle = getAngularDifference(viewer, first);
                    float secondAngle = getAngularDifference(viewer, second);
                    return Float.compare(firstAngle, secondAngle);
                }
                return Double.compare(viewer.getDistanceToEntity(first), viewer.getDistanceToEntity(second));
            }
        });
        return candidates;
    }

    /** Returns the best candidate, or null when no eligible target exists. */
    public static EntityPlayer getBestTarget(double range, Priority priority) {
        List<EntityPlayer> candidates = getCandidates(range, priority);
        return candidates.isEmpty() ? null : candidates.get(0);
    }

    private static float getAngularDifference(Entity viewer, Entity target) {
        double dx = target.posX - viewer.posX;
        double dz = target.posZ - viewer.posZ;
        double dy = target.posY + target.getEyeHeight()
                - (viewer.posY + viewer.getEyeHeight());
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        float targetYaw = (float) (Math.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F;
        float targetPitch = (float) (-(Math.atan2(dy, horizontal) * 180.0D / Math.PI));
        float yawDifference = MathHelper.wrapAngleTo180_float(targetYaw - viewer.rotationYaw);
        float pitchDifference = MathHelper.wrapAngleTo180_float(targetPitch - viewer.rotationPitch);
        return (float) Math.sqrt(yawDifference * yawDifference + pitchDifference * pitchDifference);
    }
}
