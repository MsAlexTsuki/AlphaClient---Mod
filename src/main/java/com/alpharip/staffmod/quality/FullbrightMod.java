package com.alpharip.staffmod.quality;

import com.alpharip.staffmod.profile.ModuleProfileManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

/**
 * Fullbright toggle for AlphaClient.
 *
 * The enabled state and key binding are stored in the active AlphaClient profile.
 * The key binding deliberately starts unassigned for profiles without a saved value.
 */
public final class FullbrightMod {
    private static final float FULLBRIGHT_GAMMA = 100.0F;

    private static boolean enabled;
    private static boolean originalGammaCaptured;
    private static float originalGamma;
    private static int lastObservedKeyCode = Integer.MIN_VALUE;
    private static boolean profileStatePending;
    private static boolean pendingEnabledState;

    private static KeyBinding toggleKey;

    public FullbrightMod() {
        toggleKey = new KeyBinding(
                "AlphaClient: Toggle Fullbright",
                Keyboard.KEY_NONE,
                "AlphaClient"
        );
        ClientRegistry.registerKeyBinding(toggleKey);
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static int getToggleKeyCode() {
        return toggleKey == null ? Keyboard.KEY_NONE : toggleKey.getKeyCode();
    }

    /** Apply the selected profile's Fullbright state and key binding. */
    public static void applyProfileSettings(boolean shouldEnable, int keyCode) {
        if (toggleKey != null) {
            toggleKey.setKeyCode(keyCode);
            KeyBinding.resetKeyBindingArrayAndHash();
            lastObservedKeyCode = keyCode;
        }

        // Profile loading can happen before Minecraft's GameSettings are ready.
        // Defer applying the saved enabled state until the first usable client tick.
        pendingEnabledState = shouldEnable;
        profileStatePending = true;
    }

    public static void setEnabled(boolean shouldEnable) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.gameSettings == null || enabled == shouldEnable) {
            return;
        }

        if (shouldEnable) {
            originalGamma = minecraft.gameSettings.gammaSetting;
            originalGammaCaptured = true;
            enabled = true;
            minecraft.gameSettings.gammaSetting = FULLBRIGHT_GAMMA;
        } else {
            enabled = false;
            restoreGamma(minecraft);
        }

        if (!ModuleProfileManager.isApplyingModuleStates()) {
            ModuleProfileManager.setSelectedFullbrightState(enabled);
        }
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (toggleKey != null && toggleKey.isPressed()) {
            setEnabled(!isEnabled());
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        if (profileStatePending && minecraft != null && minecraft.gameSettings != null) {
            boolean shouldEnable = pendingEnabledState;
            profileStatePending = false;
            setEnabled(shouldEnable);
        }

        int currentKeyCode = getToggleKeyCode();
        if (currentKeyCode != lastObservedKeyCode) {
            lastObservedKeyCode = currentKeyCode;
            if (!ModuleProfileManager.isApplyingModuleStates()) {
                ModuleProfileManager.setSelectedFullbrightKeyCode(currentKeyCode);
            }
        }

        if (!enabled) {
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.gameSettings == null) {
            return;
        }

        // Keep full brightness active if another client tick resets gamma.
        minecraft.gameSettings.gammaSetting = FULLBRIGHT_GAMMA;
    }

    private static void restoreGamma(Minecraft minecraft) {
        if (originalGammaCaptured && minecraft != null && minecraft.gameSettings != null) {
            minecraft.gameSettings.gammaSetting = originalGamma;
        }
        originalGammaCaptured = false;
    }
}
