package com.alpharip.staffmod.togglesneak;

import net.minecraft.client.settings.KeyBinding;

/**
 * Applies a toggle to one vanilla key and remembers the key code it last controlled.
 * This allows a remapped vanilla key to be released cleanly.
 */
public final class ToggleMovementKeyState {
    private static final int NO_KEY = Integer.MIN_VALUE;
    private int lastKeyCode = NO_KEY;

    public void apply(KeyBinding vanillaBinding, boolean toggled) {
        if (vanillaBinding == null) {
            release();
            return;
        }

        int currentKeyCode = vanillaBinding.getKeyCode();
        if (lastKeyCode != NO_KEY && lastKeyCode != currentKeyCode) {
            KeyBinding.setKeyBindState(lastKeyCode, false);
        }

        boolean physicallyDown = ToggleMovementInput.isKeyDown(currentKeyCode);
        KeyBinding.setKeyBindState(currentKeyCode, toggled || physicallyDown);
        lastKeyCode = currentKeyCode;
    }

    public void release() {
        if (lastKeyCode != NO_KEY) {
            KeyBinding.setKeyBindState(lastKeyCode, false);
            lastKeyCode = NO_KEY;
        }
    }
}
