package com.alpharip.staffmod.togglesneak;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

/** Coordinates activation input, module state, and vanilla movement key state. */
public final class ToggleMovementController {
    private final ToggleSneak toggleSneak;
    private final ToggleSprint toggleSprint;
    private final ToggleMovementKeyState sneakKeyState = new ToggleMovementKeyState();
    private final ToggleMovementKeyState sprintKeyState = new ToggleMovementKeyState();

    public ToggleMovementController(ToggleSneak toggleSneak, ToggleSprint toggleSprint) {
        if (toggleSneak == null || toggleSprint == null) {
            throw new IllegalArgumentException("Toggle modules must not be null.");
        }
        this.toggleSneak = toggleSneak;
        this.toggleSprint = toggleSprint;
    }

    public void tick(Minecraft minecraft) {
        if (minecraft == null || minecraft.gameSettings == null) return;

        boolean sneakActivation = ToggleMovementKeyBindings.consumeSneakPress();
        boolean sprintActivation = ToggleMovementKeyBindings.consumeSprintPress();

        if (minecraft.thePlayer == null || minecraft.theWorld == null) {
            resetForMissingWorld();
            return;
        }

        if (minecraft.currentScreen != null || !minecraft.inGameHasFocus) {
            releaseMovementKeys();
            return;
        }

        if (sneakActivation) toggleSneak.toggle();
        if (sprintActivation) toggleSprint.toggle();

        KeyBinding sneakBinding = toggleSneak.getMovementType().getVanillaKeyBinding(minecraft);
        KeyBinding sprintBinding = toggleSprint.getMovementType().getVanillaKeyBinding(minecraft);

        sneakKeyState.apply(sneakBinding, toggleSneak.isToggled());
        sprintKeyState.apply(sprintBinding, toggleSprint.isToggled());
    }

    public void resetForMissingWorld() {
        toggleSneak.reset();
        toggleSprint.reset();
        releaseMovementKeys();
    }

    public void releaseMovementKeys() {
        sneakKeyState.release();
        sprintKeyState.release();
    }

    public void stop() {
        resetForMissingWorld();
    }

    public ToggleSneak getToggleSneak() {
        return toggleSneak;
    }

    public ToggleSprint getToggleSprint() {
        return toggleSprint;
    }
}
