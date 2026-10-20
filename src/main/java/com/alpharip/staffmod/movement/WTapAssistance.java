package com.alpharip.staffmod.movement;

import com.alpharip.staffmod.permissions.PermissionService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Collections;
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

    // Recorded values contain only W-release durations, in nanoseconds.
    private static final List<Long> customPattern = new ArrayList<Long>();
    private static final List<Long> playbackLatenessNanos = new ArrayList<Long>();
    private static boolean recording;
    private static boolean playing;
    private static boolean lastPhysicalW;
    private static long releaseStartedNanos;
    private static int playbackIndex;
    private static long playbackDeadlineNanos;
    private static boolean playbackInRelease;
    private static long renderFrame;
    private static long nextReleaseFrame;
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
        stopPlayback();
        customPattern.clear();
        recording = true;
        long now = System.nanoTime();
        lastPhysicalW = Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode());
        releaseStartedNanos = lastPhysicalW ? 0L : now;
    }

    public static void stopRecording() {
        if (recording && !lastPhysicalW) saveReleaseDuration(System.nanoTime());
        recording = false;
        releaseStartedNanos = 0L;
    }

    private static void saveReleaseDuration(long now) {
        if (lastPhysicalW || releaseStartedNanos <= 0L) return;
        long duration = now - releaseStartedNanos;
        if (duration > 0L) customPattern.add(duration);
        releaseStartedNanos = 0L;
    }

    private static void recordPhysicalTransition(boolean physicalW, long now) {
        if (!recording || physicalW == lastPhysicalW) return;
        if (!lastPhysicalW) {
            saveReleaseDuration(now);
        } else {
            releaseStartedNanos = now;
        }
        lastPhysicalW = physicalW;
    }

    public static void startPlayback() {
        stopRecording();
        if (customPattern.isEmpty()) return;
        playbackLatenessNanos.clear();
        playing = true;
        playbackIndex = 0;
        playbackInRelease = false;
        nextReleaseFrame = renderFrame;
        beginPlaybackRelease(System.nanoTime());
    }

    private static void beginPlaybackRelease(long now) {
        if (playbackIndex >= customPattern.size()) {
            finishPlayback();
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.gameSettings == null) {
            playing = false;
            return;
        }
        int keyCode = mc.gameSettings.keyBindForward.getKeyCode();
        KeyBinding.setKeyBindState(keyCode, false);
        playbackInRelease = true;
        playbackDeadlineNanos = now + Math.max(1L, customPattern.get(playbackIndex));
    }

    private void finishPlayback() {
        playing = false;
        playbackInRelease = false;
        restoreForwardKey(Minecraft.getMinecraft());
        reportPlaybackTiming();
    }

    public static void stopPlayback() {
        boolean wasPlaying = playing;
        playing = false;
        playbackInRelease = false;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null && mc.gameSettings != null) {
            int keyCode = mc.gameSettings.keyBindForward.getKeyCode();
            KeyBinding.setKeyBindState(keyCode, Keyboard.isKeyDown(keyCode));
        }
        if (wasPlaying) playbackLatenessNanos.clear();
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!recording || mc == null || mc.gameSettings == null || mc.currentScreen != null) return;
        int keyCode = mc.gameSettings.keyBindForward.getKeyCode();
        if (keyCode < 0 || Keyboard.getEventKey() != keyCode) return;
        recordPhysicalTransition(Keyboard.getEventKeyState(), System.nanoTime());
    }

    @SubscribeEvent
    public void onAttackEntity(net.minecraftforge.event.entity.player.AttackEntityEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!enabled || mc == null || mc.thePlayer == null || event.entityPlayer != mc.thePlayer
                || mc.currentScreen != null) return;
        if (!PermissionService.hasAccess(PermissionService.Rank.FOUNDER)) return;
        Entity victim = event.target;
        if (!(victim instanceof EntityLivingBase)) return;
        if (target == 0 && !(victim instanceof EntityPlayer)) return;

        // Custom mode automatically replays the saved W-release durations on eligible attacks.
        if (mode == 4) {
            if (!customPattern.isEmpty() && !playing
                    && (!requireW || Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode()))) {
                startPlayback();
            }
            return;
        }

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
            if (playing) stopPlayback();
            return;
        }
        int keyCode = mc.gameSettings.keyBindForward.getKeyCode();
        boolean physicalW = Keyboard.isKeyDown(keyCode);

        // Fallback synchronization handles focus changes while the recorder's GUI is open.
        if (recording && mc.currentScreen == null) {
            recordPhysicalTransition(physicalW, System.nanoTime());
        }

        if (mc.currentScreen != null) {
            pendingDelay = -1;
            releaseTicks = 0;
            if (playing) stopPlayback();
            else restoreForwardKey(mc);
            return;
        }

        if (!enabled || !PermissionService.hasAccess(PermissionService.Rank.FOUNDER) || mode == 3) {
            restoreForwardKey(mc);
            pendingDelay = -1;
            releaseTicks = 0;
            if (playing) stopPlayback();
            return;
        }

        // Custom playback is driven by RenderTickEvent and a monotonic clock, not game ticks.
        if (mode == 4 && playing) return;

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

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        renderFrame++;
        if (!playing) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.gameSettings == null || mc.currentScreen != null
                || !enabled || !PermissionService.hasAccess(PermissionService.Rank.FOUNDER) || mode != 4) {
            stopPlayback();
            return;
        }

        long now = System.nanoTime();
        if (playbackInRelease) {
            if (now < playbackDeadlineNanos) return;
            playbackLatenessNanos.add(now - playbackDeadlineNanos);
            restoreForwardKey(mc);
            playbackIndex++;
            playbackInRelease = false;
            nextReleaseFrame = renderFrame + 1L;
            if (playbackIndex >= customPattern.size()) {
                finishPlayback();
            }
            return;
        }

        // Ensure W is pressed for at least one rendered frame between recorded releases.
        if (renderFrame >= nextReleaseFrame) beginPlaybackRelease(now);
    }

    private void reportPlaybackTiming() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || playbackLatenessNanos.isEmpty()) return;
        List<Long> sorted = new ArrayList<Long>(playbackLatenessNanos);
        Collections.sort(sorted);
        long total = 0L;
        long max = 0L;
        for (Long lateness : sorted) {
            total += lateness;
            if (lateness > max) max = lateness;
        }
        long p95 = sorted.get(Math.min(sorted.size() - 1, (int) Math.ceil(sorted.size() * 0.95) - 1));
        mc.thePlayer.addChatMessage(new ChatComponentText(
                "§7[AlphaClient] W-Tap timing: §f" + sorted.size()
                        + " releases, avg +" + formatMillis(total / (double) sorted.size())
                        + " ms, P95 +" + formatMillis(p95)
                        + " ms, max +" + formatMillis(max) + " ms"));
        playbackLatenessNanos.clear();
    }

    private String formatMillis(double nanos) {
        return String.format(java.util.Locale.ROOT, "%.2f", nanos / 1000000.0);
    }

    private void restoreForwardKey(Minecraft mc) {
        if (mc == null || mc.gameSettings == null) return;
        int keyCode = mc.gameSettings.keyBindForward.getKeyCode();
        KeyBinding.setKeyBindState(keyCode, Keyboard.isKeyDown(keyCode));
    }
}
