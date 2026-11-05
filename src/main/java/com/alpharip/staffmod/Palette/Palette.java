package com.alpharip.staffmod.Palette;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

/** Owns the Command Palette key binding and opens the palette during gameplay. */
public final class Palette {
    private static final Palette INSTANCE = new Palette();

    private KeyBinding openKey;

    private Palette() {
    }

    public static Palette getInstance() {
        return INSTANCE;
    }

    public synchronized void register() {
        if (openKey != null) {
            return;
        }

        openKey = new KeyBinding(
                "AlphaClient: Open Command Palette",
                Keyboard.KEY_P,
                "AlphaClient"
        );
        ClientRegistry.registerKeyBinding(openKey);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (openKey == null) {
            return;
        }

        boolean pressed = false;
        while (openKey.isPressed()) {
            pressed = true;
        }
        if (!pressed) {
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.thePlayer == null || minecraft.currentScreen != null) {
            return;
        }

        minecraft.displayGuiScreen(new PaletteGui(null));
    }
}
