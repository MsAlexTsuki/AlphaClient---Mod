package com.alpharip.staffmod.autosave;

import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Bridges Forge world-save and HUD-overlay events to the AutoSave prototype. */
final class AutoSaveEventHandler {
    private final AutoSave module;

    AutoSaveEventHandler(AutoSave module) {
        this.module = module;
    }

    @SubscribeEvent
    public void onWorldSave(WorldEvent.Save event) {
        if (event == null || event.world == null || event.world.isRemote) return;
        String worldName = event.world.getWorldInfo() == null
                ? "" : event.world.getWorldInfo().getWorldName();
        module.onWorldSaved(worldName);
    }

    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Text event) {
        module.getHud().onOverlay(event);
    }
}
