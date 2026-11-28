package com.alpharip.staffmod.chunkborders;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Forge adapter that delegates world rendering to the standalone module. */
final class ChunkBordersEventHandler {
    private final ChunkBorders module;

    ChunkBordersEventHandler(ChunkBorders module) {
        if (module == null) throw new IllegalArgumentException("module");
        this.module = module;
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (event == null || !module.isRunning() || !module.isEnabled()) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) return;
        int chunkX = ((int) Math.floor(mc.thePlayer.posX)) >> 4;
        int chunkZ = ((int) Math.floor(mc.thePlayer.posZ)) >> 4;
        ChunkBordersSnapshot snapshot = module.createSnapshot(chunkX, chunkZ);
        if (snapshot != null) ChunkBordersRenderer.render(snapshot);
    }
}
