package com.alpharip.staffmod.movement;

import com.alpharip.staffmod.permissions.PermissionService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import java.util.ArrayList;
import java.util.List;

/** Client-side W-Tap helper. Settings are shared with the W-Tap GUI. */
public final class WTapAssistance {
    private static boolean enabled;
    private static boolean requireW = true;
    private static int delay = 2;
    private static int cooldown = 4;
    private static int mode;
    private static int trigger;
    private static int target;
    private static final List<Integer> customPattern = new ArrayList<Integer>();
    private static boolean recording;
    private static boolean playing;
    private static boolean lastPhysicalW;
    private static int recordTicks;
    private static int playbackIndex;
    private static int playbackTicks;
    private int pendingDelay = -1;
    private int releaseTicks;
    private int cooldownTicks;

    public static boolean isEnabled() { return enabled; }
    public static void setEnabled(boolean value) { enabled = value; }
    public static boolean isRequireW() { return requireW; }
    public static void setRequireW(boolean value) { requireW = value; }
    public static int getDelay() { return delay; }
    public static void setDelay(int value) { delay = Math.max(0, Math.min(10, value)); }
    public static int getCooldown() { return cooldown; }
    public static void setCooldown(int value) { cooldown = Math.max(0, Math.min(20, value)); }
    public static int getMode() { return mode; }
    public static void setMode(int value) { mode = Math.max(0, Math.min(4, value)); }
    public static int getTrigger() { return trigger; }
    public static void setTrigger(int value) { trigger = Math.max(0, Math.min(1, value)); }
    public static int getTarget() { return target; }
    public static void setTarget(int value) { target = Math.max(0, Math.min(1, value)); }
    public static boolean isRecording() { return recording; }
    public static boolean isPlaying() { return playing; }
    public static boolean hasCustomPattern() { return !customPattern.isEmpty(); }

    public static void startRecording() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.gameSettings == null) return;
        customPattern.clear();
        recording = true;
        playing = false;
        recordTicks = 0;
        lastPhysicalW = Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode());
    }

    public static void stopRecording() {
        if (recording && recordTicks > 0) customPattern.add(recordTicks);
        recording = false;
        recordTicks = 0;
    }

    public static void startPlayback() {
        stopRecording();
        if (customPattern.isEmpty()) return;
        playing = true;
        playbackIndex = 0;
        playbackTicks = customPattern.get(0);
    }

    public static void stopPlayback() { playing = false; }

    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!enabled || mode >= 3 || mc == null || mc.thePlayer == null || event.entityPlayer != mc.thePlayer
                || mc.currentScreen != null) return;
        if (!PermissionService.hasAccess(PermissionService.Rank.FOUNDER)) return;
        Entity victim = event.target;
        if (!(victim instanceof EntityLivingBase)) return;
        if (target == 0 && !(victim instanceof EntityPlayer)) return;
        if (requireW && !Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode())) return;
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
            playing = false;
            return;
        }
        int keyCode = mc.gameSettings.keyBindForward.getKeyCode();
        boolean physicalW = Keyboard.isKeyDown(keyCode);

        if (recording && mc.currentScreen == null) {
            recordTicks++;
            if (physicalW != lastPhysicalW) {
                customPattern.add(recordTicks);
                recordTicks = 0;
                lastPhysicalW = physicalW;
            }
        }

        // Never propagate the physical W key into Minecraft's movement binding while a GUI/chat is open.
        if (mc.currentScreen != null) {
            KeyBinding.setKeyBindState(keyCode, false);
            pendingDelay = -1;
            releaseTicks = 0;
            if (playing) stopPlayback();
            return;
        }

        if (!enabled || !PermissionService.hasAccess(PermissionService.Rank.FOUNDER) || mode == 3) {
            restoreForwardKey(mc);
            pendingDelay = -1;
            releaseTicks = 0;
            if (playing) stopPlayback();
            return;
        }

        if (mode == 4 && playing) {
            if (playbackTicks > 0) playbackTicks--;
            if (playbackTicks <= 0) {
                playbackIndex++;
                if (playbackIndex >= customPattern.size()) {
                    stopPlayback();
                    restoreForwardKey(mc);
                } else {
                    playbackTicks = customPattern.get(playbackIndex);
                    if (playbackIndex % 2 == 0) KeyBinding.setKeyBindState(keyCode, physicalW);
                    else KeyBinding.setKeyBindState(keyCode, false);
                }
            }
            return;
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
            releaseTicks--;
            if (releaseTicks == 0) restoreForwardKey(mc);
        }
    }

    private void restoreForwardKey(Minecraft mc) {
        if (mc.gameSettings == null) return;
        int keyCode = mc.gameSettings.keyBindForward.getKeyCode();
        KeyBinding.setKeyBindState(keyCode, Keyboard.isKeyDown(keyCode));
    }
}