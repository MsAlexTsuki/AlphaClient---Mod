// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.movement;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import com.alpharip.staffmod.permissions.SupabaseCommunityService;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovementInput;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Founder-only movement assistance that steers active movement around the
 * nearest visible player while maintaining a configurable orbit distance.
 */
public final class AutoStrafe {
    private static final double TARGET_DISTANCE_EPSILON = 0.001D;

    @SubscribeEvent
    public void onInputUpdate(InputUpdateEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || event == null || event.entityPlayer == null
                || event.entityPlayer != mc.thePlayer || mc.thePlayer == null
                || mc.theWorld == null || mc.currentScreen != null
                || !SupabaseCommunityService.isSignedInFounder()) {
            return;
        }

        AlphaModule module = AlphaModuleRegistry.find("auto-strafe");
        if (module == null || !module.isImplemented() || !module.isUnlocked() || !module.isEnabled()) {
            return;
        }

        MovementInput input = event.movementInput;
        if (input == null) return;

        double inputForward = input.moveForward;
        double inputStrafe = input.moveStrafe;
        double inputMagnitude = Math.sqrt(inputForward * inputForward + inputStrafe * inputStrafe);
        if (inputMagnitude < 0.01D) return;

        EntityPlayer target = findTarget(mc);
        if (target == null) return;

        double dx = target.posX - mc.thePlayer.posX;
        double dz = target.posZ - mc.thePlayer.posZ;
        double distance = Math.sqrt(dx * dx + dz * dz);
        if (distance < TARGET_DISTANCE_EPSILON) return;

        double towardX = dx / distance;
        double towardZ = dz / distance;
        double tangentX;
        double tangentZ;
        if (AutoStrafeSettings.getDirection() == AutoStrafeSettings.Direction.LEFT) {
            tangentX = -towardZ;
            tangentZ = towardX;
        } else {
            tangentX = towardZ;
            tangentZ = -towardX;
        }

        double distanceError = distance - AutoStrafeSettings.getOrbitDistance();
        double radialStrength = clamp(distanceError * 0.65D, -1.0D, 1.0D);
        double tangentStrength = AutoStrafeSettings.getSpeed() / 100.0D;

        double worldX = towardX * radialStrength + tangentX * tangentStrength;
        double worldZ = towardZ * radialStrength + tangentZ * tangentStrength;
        double outputMagnitude = Math.sqrt(worldX * worldX + worldZ * worldZ);
        if (outputMagnitude < TARGET_DISTANCE_EPSILON) return;
        if (outputMagnitude > 1.0D) {
            worldX /= outputMagnitude;
            worldZ /= outputMagnitude;
        }

        // Convert the desired world-space vector back into Minecraft's local
        // forward/strafe axes without changing the player's requested input strength.
        double yaw = Math.toRadians(mc.thePlayer.rotationYaw);
        double sinYaw = Math.sin(yaw);
        double cosYaw = Math.cos(yaw);
        double localStrafe = worldX * cosYaw + worldZ * sinYaw;
        double localForward = -worldX * sinYaw + worldZ * cosYaw;
        double scale = Math.min(1.0D, inputMagnitude);

        input.moveForward = (float) clamp(localForward * scale, -1.0D, 1.0D);
        input.moveStrafe = (float) clamp(localStrafe * scale, -1.0D, 1.0D);
    }

    private EntityPlayer findTarget(Minecraft mc) {
        EntityPlayer best = null;
        double bestDistance = AutoStrafeSettings.getTargetRange();
        for (Object object : mc.theWorld.playerEntities) {
            if (!(object instanceof EntityPlayer)) continue;
            EntityPlayer candidate = (EntityPlayer) object;
            if (candidate == mc.thePlayer || candidate.isDead || !candidate.isEntityAlive()
                    || candidate.isInvisible() || !mc.thePlayer.canEntityBeSeen(candidate)) {
                continue;
            }
            double dx = candidate.posX - mc.thePlayer.posX;
            double dz = candidate.posZ - mc.thePlayer.posZ;
            double distance = Math.sqrt(dx * dx + dz * dz);
            if (distance <= bestDistance) {
                bestDistance = distance;
                best = candidate;
            }
        }
        return best;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
