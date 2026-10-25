package com.alpharip.staffmod.movement;

import com.alpharip.staffmod.permissions.PermissionService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;


/** Client-side W-Tap helper. Settings are shared with the W-Tap GUI. */
public final class WTapAssistance {
    private static boolean enabled;
    private static boolean requireW = true;
    private static int delay = 2;
    private static int cooldown = 4;
    private static int mode;
    private static int trigger;
    private static int target;

    private int pendingDelay = -1;
    private int releaseTicks;
    private int cooldownTicks;

    public static boolean isEnabled() { return enabled; }

    private static boolean hasFounderAccess() {
        return PermissionService.hasAccess(PermissionService.Rank.FOUNDER)
                || PermissionService.hasModAccess("w-tap-assistance")
                || PermissionService.hasCommandPermission("command.wtap", false);
    }

    public static void setEnabled(boolean value) {
        if (hasFounderAccess()) enabled = value;
    }
    public static boolean isRequireW() { return requireW; }
    public static void setRequireW(boolean value) {
        if (hasFounderAccess()) requireW = value;
    }
    public static int getDelay() { return delay; }
    public static void setDelay(int value) {
        if (hasFounderAccess()) delay = Math.max(0, Math.min(10, value));
    }
    public static int getCooldown() { return cooldown; }
    public static void setCooldown(int value) {
        if (hasFounderAccess()) cooldown = Math.max(0, Math.min(20, value));
    }
    public static int getMode() { return mode; }
    public static void setMode(int value) {
        if (hasFounderAccess()) mode = Math.max(0, Math.min(3, value));
    }
    public static int getTrigger() { return trigger; }
    public static void setTrigger(int value) {
        if (hasFounderAccess()) trigger = Math.max(0, Math.min(1, value));
    }
    public static int getTarget() { return target; }
    public static void setTarget(int value) {
        if (hasFounderAccess()) target = Math.max(0, Math.min(1, value));
    }
    @SubscribeEvent
    public void onAttackEntity(net.minecraftforge.event.entity.player.AttackEntityEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!enabled || mc == null || mc.thePlayer == null || event.entityPlayer != mc.thePlayer
                || mc.currentScreen != null) return;
        if (!hasFounderAccess()) return;
        Entity victim = event.target;
        if (!(victim instanceof EntityLivingBase)) return;
        if (target == 0 && !(victim instanceof EntityPlayer)) return;

        if (mode == 3 || (requireW && !Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode()))) return;
        if (cooldownTicks > 0 || releaseTicks > 0 || pendingDelay >= 0) return;
        pendingDelay = delay;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.gameSettings == null) {
            pendingDelay = -1;
            releaseTicks = 0;
            return;
        }
        int keyCode = mc.gameSettings.keyBindForward.getKeyCode();
        boolean physicalW = Keyboard.isKeyDown(keyCode);

        if (mc.currentScreen != null) {
            pendingDelay = -1;
            releaseTicks = 0;
            KeyBinding.setKeyBindState(keyCode, false);
            return;
        }

        if (!enabled || !hasFounderAccess() || mode == 3) {
            restoreForwardKey(mc);
            pendingDelay = -1;
            releaseTicks = 0;
            return;
        }

        // Outside an active release, keep Minecraft's forward state synchronized with the real key.
        // This restores movement on the next client tick even if the physical key never generated
        // another key-down event after W-Tap temporarily released it.
        if (releaseTicks <= 0 && pendingDelay < 0) {
            KeyBinding.setKeyBindState(keyCode, physicalW);
        }

        if (cooldownTicks > 0) cooldownTicks--;
        if (pendingDelay > 0) {
            pendingDelay--;
            return;
        }
        if (pendingDelay == 0) {
            pendingDelay = -1;
            if (!requireW || physicalW) {
                KeyBinding.setKeyBindState(keyCode, false);
                releaseTicks = 1 + mode;
                cooldownTicks = cooldown;
            }
            return;
        }
        if (releaseTicks > 0) {
            // Reassert the release for the whole tap; physical W key events must not cancel it early.
            KeyBinding.setKeyBindState(keyCode, false);
            releaseTicks--;
            if (releaseTicks == 0) restoreForwardKey(mc);
            return;
        }
    }

    private static void restoreForwardKey(Minecraft mc) {
        if (mc == null || mc.gameSettings == null) return;
        int keyCode = mc.gameSettings.keyBindForward.getKeyCode();
        KeyBinding.setKeyBindState(keyCode, Keyboard.isKeyDown(keyCode));
    }
}
