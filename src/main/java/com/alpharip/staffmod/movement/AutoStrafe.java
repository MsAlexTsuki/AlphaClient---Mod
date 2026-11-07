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
 * Founder-only movement assistance that strafes around the nearest visible player.
 *
 * Holding W activates the module. AutoStrafe keeps forward movement active and
 * adds exactly one configured lateral input (A or D). It never synthesizes S.
 * A physical S press immediately returns movement control to the player.
 *
 * Forge 1.8.9 has no InputUpdateEvent, so this uses the client PlayerTickEvent
 * before the player processes movement input and restores physical key states
 * whenever AutoStrafe cannot take control.
 */
public final class AutoStrafe {
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

        // A manually pressed S always takes priority. AutoStrafe never presses it.
        if (physicalBack) {
            restorePhysicalMovementKeys(mc, physicalForward, physicalBack, physicalLeft, physicalRight);
            return;
        }

        // Preserve intentional key changes made by other modules such as W-Tap.
        if (hasExternalMovementOverride(mc, physicalForward, physicalBack, physicalLeft, physicalRight)) {
            controllingMovement = false;
            return;
        }

        AlphaModule module = AlphaModuleRegistry.find("auto-strafe");
        if (mc.currentScreen != null || !SupabaseCommunityService.isSignedInFounder()
                || module == null || !module.isImplemented()
                || !module.isUnlocked() || !module.isEnabled() || !physicalForward) {
            restorePhysicalMovementKeys(mc, physicalForward, physicalBack, physicalLeft, physicalRight);
            return;
        }

        EntityPlayer target = findTarget(mc);
        if (target == null) {
            restorePhysicalMovementKeys(mc, physicalForward, physicalBack, physicalLeft, physicalRight);
            return;
        }

        // Direction is explicit and stable: no random side selection or orbit-vector
        // conversion that can unexpectedly turn the requested input into S.
        boolean strafeLeft = AutoStrafeSettings.getDirection() == AutoStrafeSettings.Direction.LEFT;
        applyMovementKeys(mc, strafeLeft);
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

    private void applyMovementKeys(Minecraft mc, boolean strafeLeft) {
        setKey(mc.gameSettings.keyBindForward, true);
        setKey(mc.gameSettings.keyBindBack, false);
        setKey(mc.gameSettings.keyBindLeft, strafeLeft);
        setKey(mc.gameSettings.keyBindRight, !strafeLeft);

        lastForward = true;
        lastBack = false;
        lastLeft = strafeLeft;
        lastRight = !strafeLeft;
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
}
