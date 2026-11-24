package com.alpharip.staffmod.togglesneak;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

/** User-configurable activation bindings for the two toggle modules. */
public final class ToggleMovementKeyBindings {
    public static final KeyBinding TOGGLE_SNEAK = new KeyBinding(
            "key.alphaclient.togglesneak",
            Keyboard.KEY_NONE,
            "key.categories.alphaclient.movement"
    );

    public static final KeyBinding TOGGLE_SPRINT = new KeyBinding(
            "key.alphaclient.togglesprint",
            Keyboard.KEY_NONE,
            "key.categories.alphaclient.movement"
    );

    private static boolean registered;

    private ToggleMovementKeyBindings() { }

    public static void register() {
        if (registered) return;
        ClientRegistry.registerKeyBinding(TOGGLE_SNEAK);
        ClientRegistry.registerKeyBinding(TOGGLE_SPRINT);
        registered = true;
    }

    public static boolean consumeSneakPress() {
        return consumePress(TOGGLE_SNEAK);
    }

    public static boolean consumeSprintPress() {
        return consumePress(TOGGLE_SPRINT);
    }

    private static boolean consumePress(KeyBinding binding) {
        boolean pressed = false;
        while (binding.isPressed()) pressed = true;
        return pressed;
    }
}
