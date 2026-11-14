package com.alpharip.staffmod.autosave;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Bridges singleplayer integrated-server save and HUD events to AutoSave.
 *
 * TODO: Verify the WorldEvent.Save timing and world identity against the project's
 * exact Forge 1.8.9 runtime before treating this event as a confirmed save indicator.
 */
final class AutoSaveEventHandler {
    private final AutoSave module;

    AutoSaveEventHandler(AutoSave module) {
        this.module = module;
    }

    @SubscribeEvent
    public void onWorldSave(WorldEvent.Save event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || !mc.isSingleplayer()) return;
        if (event == null || event.world == null || event.world.isRemote) return;
        String worldName = event.world.getWorldInfo() == null
                ? "" : event.world.getWorldInfo().getWorldName();
        module.onWorldSaved(worldName);
    }

    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Text event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || !mc.isSingleplayer()) return;
        module.getHud().onOverlay(event);
    }
}
