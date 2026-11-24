package com.alpharip.staffmod.togglesneak;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

/** Identifies which vanilla movement binding a toggle module controls. */
public enum ToggleMovementType {
    SNEAK,
    SPRINT;

    public KeyBinding getVanillaKeyBinding(Minecraft minecraft) {
        if (minecraft == null || minecraft.gameSettings == null) return null;
        switch (this) {
            case SNEAK:
                return minecraft.gameSettings.keyBindSneak;
            case SPRINT:
                return minecraft.gameSettings.keyBindSprint;
            default:
                throw new IllegalStateException("Unsupported movement type: " + this);
        }
    }

    public String getDisplayName() {
        switch (this) {
            case SNEAK: return "ToggleSneak";
            case SPRINT: return "ToggleSprint";
            default: return name();
        }
    }
}
