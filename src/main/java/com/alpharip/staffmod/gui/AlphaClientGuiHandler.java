package com.alpharip.staffmod.gui;

import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Replaces the vanilla main menu with AlphaClient's own menu. */
public final class AlphaClientGuiHandler {
    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (event == null || event.gui == null) {
            return;
        }
        if (event.gui instanceof GuiMainMenu && !(event.gui instanceof AlphaMainMenu)) {
            event.gui = new AlphaMainMenu();
        }
    }
}