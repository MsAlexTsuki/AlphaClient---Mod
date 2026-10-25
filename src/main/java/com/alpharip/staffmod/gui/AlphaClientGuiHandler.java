package com.alpharip.staffmod.gui;

import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Replaces Minecraft's vanilla title screen with AlphaClient's own main menu.
 */
public final class AlphaClientGuiHandler {
    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (event.gui instanceof GuiMainMenu) {
            event.gui = new AlphaMainMenu();
        }
    }
}
