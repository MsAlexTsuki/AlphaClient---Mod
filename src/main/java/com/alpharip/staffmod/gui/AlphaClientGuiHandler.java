package com.alpharip.staffmod.gui;

import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * The vanilla Minecraft title screen is intentionally left untouched.
 * AlphaClient's in-game home overlay is opened using the AlphaClient keybind.
 */
public final class AlphaClientGuiHandler {
    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        // Deliberately do not replace GuiMainMenu.
    }
}