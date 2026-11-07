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
 * Founder-only PvP strafing assistance that maintains lateral movement while
 * keeping forward input active. The strafe side is held instead of pulsed and
 * only changes when the target moves clearly across the player's view.
 *
 * A minimum hold interval prevents rapid side changes. A dead zone around the
 * crosshair prevents jitter when the target is nearly centered. AutoStrafe
 * never synthesizes S; physical S input always returns control to the player.
 */
public final class AutoStrafe {
    private static final int MIN_STRAFE_HOLD_TICKS = 12;
    private static final float TARGET_SIDE_SWITCH_ANGLE = 24.0F;

    private boolean controllingMovement;
    private boolean lastForward, lastBack, lastLeft, lastRight;
    private boolean strafeLeft = false; // Start by holding D.
    private int strafeHoldTicks;

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

        // A manually pressed S always takes priority. AutoStrafe never presses it.
        if (physicalBack) {
            resetStrafeState();
            restorePhysicalMovementKeys(mc, physicalForward, physicalBack, physicalLeft, physicalRight);
            return;
        }

        // Preserve intentional key changes made by other modules such as W-Tap.
        if (hasExternalMovementOverride(mc, physicalForward, physicalBack, physicalLeft, physicalRight)) {
            resetStrafeState();
            controllingMovement = false;
            return;
        }

        AlphaModule module = AlphaModuleRegistry.find("auto-strafe");
        if (mc.currentScreen != null || !SupabaseCommunityService.isSignedInFounder()
                || module == null || !module.isImplemented()
                || !module.isUnlocked() || !module.isEnabled() || !physicalForward) {
            resetStrafeState();
            restorePhysicalMovementKeys(mc, physicalForward, physicalBack, physicalLeft, physicalRight);
            return;
        }

        EntityPlayer target = findTarget(mc);
        if (target == null) {
            resetStrafeState();
            restorePhysicalMovementKeys(mc, physicalForward, physicalBack, physicalLeft, physicalRight);
            return;
        }

        updateStrafeSide(mc.thePlayer, target);
        applyMovementKeys(mc, strafeLeft);
        strafeHoldTicks++;
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

    private void updateStrafeSide(EntityPlayer player, EntityPlayer target) {
        double dx = target.posX - player.posX;
        double dz = target.posZ - player.posZ;
        float targetYaw = (float) (Math.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F;
        float relativeYaw = wrapAngleTo180(targetYaw - player.rotationYaw);

        if (strafeHoldTicks < MIN_STRAFE_HOLD_TICKS) return;

        // Target clearly left of the crosshair: move left to recover the angle.
        // Target clearly right: move right. Inside the dead zone, keep the current
        // side so small aim fluctuations cannot cause rapid A/D switching.
        if (relativeYaw >= TARGET_SIDE_SWITCH_ANGLE && !strafeLeft) {
            strafeLeft = true;
            strafeHoldTicks = 0;
        } else if (relativeYaw <= -TARGET_SIDE_SWITCH_ANGLE && strafeLeft) {
            strafeLeft = false;
            strafeHoldTicks = 0;
        }
    }

    private float wrapAngleTo180(float angle) {
        angle %= 360.0F;
        if (angle >= 180.0F) angle -= 360.0F;
        if (angle < -180.0F) angle += 360.0F;
        return angle;
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

    private void applyMovementKeys(Minecraft mc, boolean left) {
        setKey(mc.gameSettings.keyBindForward, true);
        setKey(mc.gameSettings.keyBindBack, false);
        setKey(mc.gameSettings.keyBindLeft, left);
        setKey(mc.gameSettings.keyBindRight, !left);

        lastForward = true;
        lastBack = false;
        lastLeft = left;
        lastRight = !left;
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

    private void resetStrafeState() {
        strafeLeft = false;
        strafeHoldTicks = 0;
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
}
