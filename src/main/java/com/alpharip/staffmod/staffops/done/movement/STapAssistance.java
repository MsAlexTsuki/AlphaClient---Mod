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

/**
 * Founder-only S-Tap helper.
 * Mirrors WTapAssistance, but briefly releases the S/back key after an attack.
 */
public final class STapAssistance {
    private static boolean enabled;
    private static boolean requireS = true;
    private static int delay = 2;
    private static int cooldown = 4;
    private static int mode;
    private static int trigger;
    private static int target;

    private int pendingDelay = -1;
    private int releaseTicks;
    private int cooldownTicks;

    private static boolean hasFounderAccess() {
        return PermissionService.hasAccess(PermissionService.Rank.FOUNDER);
    }

    public static boolean isEnabled() {
        return enabled && hasFounderAccess();
    }

    public static void setEnabled(boolean value) {
        if (hasFounderAccess()) enabled = value;
        else enabled = false;
    }

    public static boolean isRequireS() { return requireS; }
    public static void setRequireS(boolean value) {
        if (hasFounderAccess()) requireS = value;
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
        if (!isEnabled() || mc == null || mc.thePlayer == null
                || event.entityPlayer != mc.thePlayer || mc.currentScreen != null) return;

        Entity victim = event.target;
        if (!(victim instanceof EntityLivingBase)) return;
        if (target == 0 && !(victim instanceof EntityPlayer)) return;
        if (mode == 3 || (requireS
                && !Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode()))) return;
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

        int keyCode = mc.gameSettings.keyBindBack.getKeyCode();
        boolean physicalS = Keyboard.isKeyDown(keyCode);

        if (mc.currentScreen != null) {
            pendingDelay = -1;
            releaseTicks = 0;
            KeyBinding.setKeyBindState(keyCode, false);
            return;
        }

        if (!isEnabled() || mode == 3) {
            restoreBackKey(mc);
            pendingDelay = -1;
            releaseTicks = 0;
            return;
        }

        if (releaseTicks <= 0 && pendingDelay < 0) {
            KeyBinding.setKeyBindState(keyCode, physicalS);
        }

        if (cooldownTicks > 0) cooldownTicks--;
        if (pendingDelay > 0) {
            pendingDelay--;
            return;
        }
        if (pendingDelay == 0) {
            pendingDelay = -1;
            if (!requireS || physicalS) {
                KeyBinding.setKeyBindState(keyCode, false);
                releaseTicks = 1 + mode;
                cooldownTicks = cooldown;
            }
            return;
        }
        if (releaseTicks > 0) {
            KeyBinding.setKeyBindState(keyCode, false);
            releaseTicks--;
            if (releaseTicks == 0) restoreBackKey(mc);
        }
    }

    private static void restoreBackKey(Minecraft mc) {
        if (mc == null || mc.gameSettings == null) return;
        int keyCode = mc.gameSettings.keyBindBack.getKeyCode();
        KeyBinding.setKeyBindState(keyCode, Keyboard.isKeyDown(keyCode));
    }
}
