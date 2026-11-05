package com.alpharip.staffmod.CommandPalette;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

/** Registers the command-palette shortcut and opens its screen during gameplay. */
public final class CommandPalette {
    private static final CommandPalette INSTANCE = new CommandPalette();
    private KeyBinding openKey;

    private CommandPalette() { }

    public static CommandPalette getInstance() {
        return INSTANCE;
    }

    public synchronized void register() {
        if (openKey != null) return;
        openKey = new KeyBinding("AlphaClient: Open Command Palette", Keyboard.KEY_P, "AlphaClient");
        ClientRegistry.registerKeyBinding(openKey);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (openKey == null) return;
        boolean pressed = false;
        while (openKey.isPressed()) pressed = true;
        if (!pressed) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.currentScreen != null) return;
        mc.displayGuiScreen(new CommandPaletteGui(null));
    }
}
