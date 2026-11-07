// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.movement;

import com.alpharip.staffmod.combat.AimAssistController;
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
import java.util.UUID;

/**
 * Founder-only PvP movement assistance.
 * Consumes AimAssist's selected target and never changes camera rotation.
 * Manual backward input always takes priority.
 */
public final class AutoStrafe {
    private static final int MIN_STRAFE_HOLD_TICKS = 8;
    private static final double TARGET_LATERAL_SPEED_THRESHOLD = 0.015D;

    private boolean controllingMovement;
    private boolean lastForward, lastBack, lastLeft, lastRight;
    private boolean strafeLeft = false; // Start by holding D when the target is stationary.
    private int strafeHoldTicks;
    private int lastTargetLateralDirection;
    private UUID currentTargetId;

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

        // AimAssist is the sole target selector. Both modules therefore use the same player.
        EntityPlayer target = AimAssistController.getSelectedTarget();
        if (target == null || target == mc.thePlayer || target.isDead || !target.isEntityAlive()
                || target.isInvisible() || !mc.thePlayer.canEntityBeSeen(target)
                || mc.thePlayer.getDistanceToEntity(target) > AutoStrafeSettings.getTargetRange()) {
            resetStrafeState();
            restorePhysicalMovementKeys(mc, physicalForward, physicalBack, physicalLeft, physicalRight);
            return;
        }

        UUID targetId = target.getUniqueID();
        if (!targetId.equals(currentTargetId)) {
            currentTargetId = targetId;
            strafeLeft = false;
            strafeHoldTicks = 0;
            lastTargetLateralDirection = 0;
        }

        updateStrafeSide(mc.thePlayer, target);
        applyMovementKeys(mc, strafeLeft);
        strafeHoldTicks++;
    }

    /**
     * Changes the orbit direction only when the opponent changes lateral
     * movement direction. AimAssist owns yaw/pitch correction independently.
     */
    private void updateStrafeSide(EntityPlayer player, EntityPlayer target) {
        double yawRadians = Math.toRadians(player.rotationYaw);
        double rightX = Math.cos(yawRadians);
        double rightZ = Math.sin(yawRadians);
        double targetLateralVelocity =
                (target.posX - target.prevPosX) * rightX
                + (target.posZ - target.prevPosZ) * rightZ;

        int lateralDirection = targetLateralVelocity > TARGET_LATERAL_SPEED_THRESHOLD ? 1
                : targetLateralVelocity < -TARGET_LATERAL_SPEED_THRESHOLD ? -1 : 0;
        if (lateralDirection == 0) return;

        if (lastTargetLateralDirection == 0) {
            lastTargetLateralDirection = lateralDirection;
            strafeLeft = lateralDirection > 0;
            strafeHoldTicks = 0;
            return;
        }

        if (lateralDirection != lastTargetLateralDirection
                && strafeHoldTicks >= MIN_STRAFE_HOLD_TICKS) {
            lastTargetLateralDirection = lateralDirection;
            strafeLeft = lateralDirection > 0;
            strafeHoldTicks = 0;
        }
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
        lastTargetLateralDirection = 0;
        currentTargetId = null;
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