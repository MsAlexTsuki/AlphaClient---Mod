// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.movement;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import com.alpharip.staffmod.permissions.SupabaseCommunityService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/**
 * Founder-only movement assistance that steers active movement around the
 * nearest visible player while maintaining a configurable orbit distance.
 *
 * Forge 1.8.9 has no InputUpdateEvent, so this uses the client PlayerTickEvent
 * before the player processes movement input and restores physical key states
 * whenever AutoStrafe cannot take control.
 */
public final class AutoStrafe {
    private static final double TARGET_DISTANCE_EPSILON = 0.001D;
    private static final double INPUT_THRESHOLD = 0.25D;

    private boolean controllingMovement;
    private boolean lastForward, lastBack, lastLeft, lastRight;

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event == null || event.phase != TickEvent.Phase.START) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || event.player == null || event.player != mc.thePlayer
                || mc.thePlayer == null || mc.thePlayer.worldObj == null
                || !mc.thePlayer.worldObj.isRemote) {
            return;
        }

        boolean physicalForward = isPhysicalKeyDown(mc.gameSettings.keyBindForward);
        boolean physicalBack = isPhysicalKeyDown(mc.gameSettings.keyBindBack);
        boolean physicalLeft = isPhysicalKeyDown(mc.gameSettings.keyBindLeft);
        boolean physicalRight = isPhysicalKeyDown(mc.gameSettings.keyBindRight);

        // Preserve intentional key changes made by other modules such as W-Tap/S-Tap.
        if (hasExternalMovementOverride(mc, physicalForward, physicalBack, physicalLeft, physicalRight)) {
            controllingMovement = false;
            return;
        }

        AlphaModule module = AlphaModuleRegistry.find("auto-strafe");
        if (mc.currentScreen != null || !SupabaseCommunityService.isSignedInFounder()
                || module == null || !module.isImplemented()
                || !module.isUnlocked() || !module.isEnabled()) {
            restorePhysicalMovementKeys(mc, physicalForward, physicalBack, physicalLeft, physicalRight);
            return;
        }

        double inputForward = (physicalForward ? 1.0D : 0.0D) - (physicalBack ? 1.0D : 0.0D);
        double inputStrafe = (physicalLeft ? 1.0D : 0.0D) - (physicalRight ? 1.0D : 0.0D);
        if (Math.abs(inputForward) + Math.abs(inputStrafe) < 0.01D) {
            restorePhysicalMovementKeys(mc, physicalForward, physicalBack, physicalLeft, physicalRight);
            return;
        }

        EntityPlayer target = findTarget(mc);
        if (target == null) {
            restorePhysicalMovementKeys(mc, physicalForward, physicalBack, physicalLeft, physicalRight);
            return;
        }

        double dx = target.posX - mc.thePlayer.posX;
        double dz = target.posZ - mc.thePlayer.posZ;
        double distance = Math.sqrt(dx * dx + dz * dz);
        if (distance < TARGET_DISTANCE_EPSILON) {
            restorePhysicalMovementKeys(mc, physicalForward, physicalBack, physicalLeft, physicalRight);
            return;
        }

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
        if (outputMagnitude < TARGET_DISTANCE_EPSILON) {
            restorePhysicalMovementKeys(mc, physicalForward, physicalBack, physicalLeft, physicalRight);
            return;
        }
        if (outputMagnitude > 1.0D) {
            worldX /= outputMagnitude;
            worldZ /= outputMagnitude;
        }

        // Convert the desired world-space vector into Minecraft's local
        // forward/strafe axes. Movement bindings are digital in Minecraft 1.8.9.
        double yaw = Math.toRadians(mc.thePlayer.rotationYaw);
        double sinYaw = Math.sin(yaw);
        double cosYaw = Math.cos(yaw);
        double localStrafe = worldX * cosYaw + worldZ * sinYaw;
        double localForward = -worldX * sinYaw + worldZ * cosYaw;
        applyMovementKeys(mc, localForward, localStrafe);
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

    private boolean hasExternalMovementOverride(Minecraft mc, boolean physicalForward,
                                                boolean physicalBack, boolean physicalLeft,
                                                boolean physicalRight) {
        return isExternalOverride(mc.gameSettings.keyBindForward, physicalForward, lastForward)
                || isExternalOverride(mc.gameSettings.keyBindBack, physicalBack, lastBack)
                || isExternalOverride(mc.gameSettings.keyBindLeft, physicalLeft, lastLeft)
                || isExternalOverride(mc.gameSettings.keyBindRight, physicalRight, lastRight);
    }

    private boolean isExternalOverride(KeyBinding binding, boolean physical, boolean lastApplied) {
        boolean current = binding.isKeyDown();
        return controllingMovement ? current != lastApplied && current != physical : current != physical;
    }

    private void applyMovementKeys(Minecraft mc, double forward, double strafe) {
        boolean pressForward = forward > INPUT_THRESHOLD;
        boolean pressBack = forward < -INPUT_THRESHOLD;
        boolean pressLeft = strafe > INPUT_THRESHOLD;
        boolean pressRight = strafe < -INPUT_THRESHOLD;

        if (!pressForward && !pressBack && !pressLeft && !pressRight) {
            if (Math.abs(forward) >= Math.abs(strafe)) {
                pressForward = forward >= 0.0D;
                pressBack = !pressForward;
            } else {
                pressLeft = strafe >= 0.0D;
                pressRight = !pressLeft;
            }
        }

        setKey(mc.gameSettings.keyBindForward, pressForward);
        setKey(mc.gameSettings.keyBindBack, pressBack);
        setKey(mc.gameSettings.keyBindLeft, pressLeft);
        setKey(mc.gameSettings.keyBindRight, pressRight);
        lastForward = pressForward;
        lastBack = pressBack;
        lastLeft = pressLeft;
        lastRight = pressRight;
        controllingMovement = true;
    }

    private void restorePhysicalMovementKeys(Minecraft mc, boolean forward,
                                             boolean back, boolean left, boolean right) {
        if (!controllingMovement) return;
        setKey(mc.gameSettings.keyBindForward, forward);
        setKey(mc.gameSettings.keyBindBack, back);
        setKey(mc.gameSettings.keyBindLeft, left);
        setKey(mc.gameSettings.keyBindRight, right);
        controllingMovement = false;
    }

    private boolean isPhysicalKeyDown(KeyBinding binding) {
        int keyCode = binding.getKeyCode();
        if (keyCode == 0) return false;
        return keyCode < 0 ? Mouse.isButtonDown(keyCode + 100) : Keyboard.isKeyDown(keyCode);
    }

    private void setKey(KeyBinding binding, boolean pressed) {
        int keyCode = binding.getKeyCode();
        if (keyCode != 0) KeyBinding.setKeyBindState(keyCode, pressed);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
