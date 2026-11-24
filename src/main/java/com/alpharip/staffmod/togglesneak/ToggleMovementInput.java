package com.alpharip.staffmod.togglesneak;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/** Reads physical keyboard and mouse state without changing Minecraft bindings. */
public final class ToggleMovementInput {
    private ToggleMovementInput() { }

    public static boolean isKeyDown(int keyCode) {
        if (keyCode == Keyboard.KEY_NONE) return false;
        if (keyCode < 0) return Mouse.isButtonDown(keyCode + 100);
        return Keyboard.isKeyDown(keyCode);
    }
}
