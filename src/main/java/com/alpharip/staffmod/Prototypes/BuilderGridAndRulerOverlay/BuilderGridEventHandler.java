package com.alpharip.staffmod.buildergridandruleroverlay;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Forge event adapter for world rendering and the ruler HUD readout. */
final class BuilderGridEventHandler {
    private final BuilderGridAndRulerOverlay module;

    BuilderGridEventHandler(BuilderGridAndRulerOverlay module) {
        if (module == null) throw new IllegalArgumentException("module");
        this.module = module;
    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        if (event == null || !module.isRunning()) return;
        BuilderGridSnapshot snapshot = createSnapshot();
        if (snapshot != null) BuilderGridRenderer.render(snapshot);
    }

    @SubscribeEvent
    public void onHudRender(RenderGameOverlayEvent.Text event) {
        if (event == null || !module.isRunning()) return;
        BuilderGridSnapshot snapshot = createSnapshot();
        if (snapshot != null) BuilderGridRulerHudRenderer.render(snapshot, event);
    }

    private BuilderGridSnapshot createSnapshot() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) return null;
        int x = (int) Math.floor(mc.thePlayer.posX);
        int y = (int) Math.floor(mc.thePlayer.posY);
        int z = (int) Math.floor(mc.thePlayer.posZ);
        if (!module.getAnchor().isInitialized()) module.getAnchor().set(x, y, z);
        return module.createSnapshot(x, y, z);
    }
}
