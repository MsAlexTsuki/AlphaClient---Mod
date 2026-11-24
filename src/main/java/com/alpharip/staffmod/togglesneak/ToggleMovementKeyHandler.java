package com.alpharip.staffmod.togglesneak;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/** Applies toggle state through Minecraft 1.8.9's existing movement key bindings. */
public final class ToggleMovementKeyHandler {
    private final ToggleSneak toggleSneak;
    private final ToggleSprint toggleSprint;

    public ToggleMovementKeyHandler(ToggleSneak toggleSneak, ToggleSprint toggleSprint) {
        if (toggleSneak == null || toggleSprint == null) {
            throw new IllegalArgumentException("Toggle modules must not be null.");
        }
        this.toggleSneak = toggleSneak;
        this.toggleSprint = toggleSprint;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.gameSettings == null) return;

        boolean sneakPressed = consumePress(ToggleMovementKeyBindings.TOGGLE_SNEAK);
        boolean sprintPressed = consumePress(ToggleMovementKeyBindings.TOGGLE_SPRINT);

        if (minecraft.thePlayer == null || minecraft.theWorld == null) {
            toggleSneak.reset();
            toggleSprint.reset();
            releaseMovementKeys();
            return;
        }

        if (minecraft.currentScreen != null || !minecraft.inGameHasFocus) {
            releaseMovementKeys();
            return;
        }

        if (sneakPressed) toggleSneak.toggle();
        if (sprintPressed) toggleSprint.toggle();

        applyMovementKey(minecraft.gameSettings.keyBindSneak, toggleSneak.isToggled());
        applyMovementKey(minecraft.gameSettings.keyBindSprint, toggleSprint.isToggled());
    }

    public void releaseMovementKeys() {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.gameSettings == null) return;

        KeyBinding.setKeyBindState(minecraft.gameSettings.keyBindSneak.getKeyCode(), false);
        KeyBinding.setKeyBindState(minecraft.gameSettings.keyBindSprint.getKeyCode(), false);
    }

    private static boolean consumePress(KeyBinding binding) {
        boolean pressed = false;
        while (binding.isPressed()) pressed = true;
        return pressed;
    }

    private static void applyMovementKey(KeyBinding vanillaBinding, boolean toggled) {
        boolean physicalInput = isPhysicalInputDown(vanillaBinding.getKeyCode());
        KeyBinding.setKeyBindState(vanillaBinding.getKeyCode(), toggled || physicalInput);
    }

    private static boolean isPhysicalInputDown(int keyCode) {
        if (keyCode == Keyboard.KEY_NONE) return false;
        if (keyCode < 0) return Mouse.isButtonDown(keyCode + 100);
        return Keyboard.isKeyDown(keyCode);
    }
}
