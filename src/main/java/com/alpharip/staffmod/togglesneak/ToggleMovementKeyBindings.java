package com.alpharip.staffmod.togglesneak;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

/** Configurable activation keys; both start unbound to avoid taking over existing controls. */
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
}
