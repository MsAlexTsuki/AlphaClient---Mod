package com.alpharip.staffmod.togglesneak;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/** Forge event adapter; movement decisions are delegated to the controller. */
public final class ToggleMovementKeyHandler {
    private final ToggleMovementController controller;

    public ToggleMovementKeyHandler(ToggleMovementController controller) {
        if (controller == null) {
            throw new IllegalArgumentException("Controller must not be null.");
        }
        this.controller = controller;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        controller.tick(Minecraft.getMinecraft());
    }

    public void stop() {
        controller.stop();
    }
}
