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
import java.util.concurrent.ThreadLocalRandom;

/**
 * Shared W-key tap input handler. When both styles are enabled, each attack
 * randomly selects either the W-Tap settings or the S-Tap settings.
 * S-Tap also releases W; it never requires the physical S key.
 */
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
    private int wCooldownTicks;
    private int sCooldownTicks;
    private boolean activeUsesSTap;
    private int activeMode;
    private int activeCooldown;
    private boolean activeRequireW;

    // Keep one style for the whole engagement instead of rerolling on every hit.
    private Entity fightTarget;
    private boolean fightStyleChosen;
    private boolean fightUsesSTap;
    private int ticksSinceFightAttack;
    private static final int FIGHT_RESET_TICKS = 40;

    public static boolean isEnabled() { return enabled; }

    private static boolean hasFounderAccess() {
        return PermissionService.hasAccess(PermissionService.Rank.FOUNDER)
                || PermissionService.hasModAccess("w-tap-assistance")
                || PermissionService.hasCommandPermission("command.wtap", false);
    }

    private static boolean isWTapAvailable() {
        return enabled && hasFounderAccess() && mode != 3;
    }

    private static boolean isSTapAvailable() {
        return STapAssistance.isEnabled() && STapAssistance.getMode() != 3;
    }

    private boolean isSelectedStyleAvailable() {
        return activeUsesSTap ? isSTapAvailable() : isWTapAvailable();
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
        if (mc == null || mc.thePlayer == null || event.entityPlayer != mc.thePlayer
                || mc.currentScreen != null || mc.gameSettings == null) return;

        Entity victim = event.target;
        if (!(victim instanceof EntityLivingBase)) return;

        // A new target or a long pause starts a new engagement. Randomize once per
        // engagement so a fight uses S-Tap consistently, while the next fight can
        // use the regular W-Tap style instead.
        if (fightTarget != victim || !fightStyleChosen || ticksSinceFightAttack >= FIGHT_RESET_TICKS) {
            fightTarget = victim;
            fightUsesSTap = ThreadLocalRandom.current().nextBoolean();
            fightStyleChosen = true;
        }
        ticksSinceFightAttack = 0;

        boolean wAvailable = isWTapAvailable() && wCooldownTicks <= 0;
        boolean sAvailable = isSTapAvailable() && sCooldownTicks <= 0;
        if (!wAvailable && !sAvailable) return;

        // Preserve the engagement's selected style when both are available.
        // Fall back to the other style only if the selected one is unavailable.
        boolean useSTap = fightUsesSTap ? sAvailable : !wAvailable && sAvailable;
        if (!useSTap && !wAvailable) return;

        int selectedTarget = useSTap ? STapAssistance.getTarget() : target;
        boolean selectedRequireW = useSTap ? STapAssistance.isRequireW() : requireW;
        int selectedMode = useSTap ? STapAssistance.getMode() : mode;
        int selectedDelay = useSTap ? STapAssistance.getDelay() : delay;
        int selectedCooldown = useSTap ? STapAssistance.getCooldown() : cooldown;

        if (selectedTarget == 0 && !(victim instanceof EntityPlayer)) return;
        if (selectedMode == 3 || (selectedRequireW
                && !Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode()))) return;
        if (releaseTicks > 0 || pendingDelay >= 0) return;

        activeUsesSTap = useSTap;
        activeMode = selectedMode;
        activeCooldown = selectedCooldown;
        activeRequireW = selectedRequireW;
        pendingDelay = selectedDelay;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.gameSettings == null) {
            pendingDelay = -1;
            releaseTicks = 0;
            wCooldownTicks = 0;
            sCooldownTicks = 0;
            fightTarget = null;
            fightStyleChosen = false;
            ticksSinceFightAttack = 0;
            return;
        }

        if (ticksSinceFightAttack < FIGHT_RESET_TICKS) ticksSinceFightAttack++;

        int keyCode = mc.gameSettings.keyBindForward.getKeyCode();
        boolean physicalW = Keyboard.isKeyDown(keyCode);

        if (mc.currentScreen != null) {
            pendingDelay = -1;
            releaseTicks = 0;
            KeyBinding.setKeyBindState(keyCode, false);
            return;
        }

        if (!isWTapAvailable() && !isSTapAvailable()) {
            restoreForwardKey(mc);
            clearPendingTap();
            return;
        }

        if ((pendingDelay >= 0 || releaseTicks > 0) && !isSelectedStyleAvailable()) {
            clearPendingTap();
            restoreForwardKey(mc);
            return;
        }

        if (releaseTicks <= 0 && pendingDelay < 0) {
            KeyBinding.setKeyBindState(keyCode, physicalW);
        }

        if (wCooldownTicks > 0) wCooldownTicks--;
        if (sCooldownTicks > 0) sCooldownTicks--;

        if (pendingDelay > 0) {
            pendingDelay--;
            return;
        }
        if (pendingDelay == 0) {
            pendingDelay = -1;
            if (!activeRequireW || physicalW) {
                KeyBinding.setKeyBindState(keyCode, false);
                releaseTicks = 1 + activeMode;
                if (activeUsesSTap) sCooldownTicks = activeCooldown;
                else wCooldownTicks = activeCooldown;
            }
            return;
        }
        if (releaseTicks > 0) {
            KeyBinding.setKeyBindState(keyCode, false);
            releaseTicks--;
            if (releaseTicks == 0) restoreForwardKey(mc);
        }
    }

    private void clearPendingTap() {
        pendingDelay = -1;
        releaseTicks = 0;
    }

    private static void restoreForwardKey(Minecraft mc) {
        if (mc == null || mc.gameSettings == null) return;
        int keyCode = mc.gameSettings.keyBindForward.getKeyCode();
        KeyBinding.setKeyBindState(keyCode, Keyboard.isKeyDown(keyCode));
    }
}
