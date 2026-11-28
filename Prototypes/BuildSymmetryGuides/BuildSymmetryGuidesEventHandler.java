package com.alpharip.staffmod.buildsymmetryguides;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/** Forge adapter for target sampling and world-overlay rendering. */
public final class BuildSymmetryGuidesEventHandler {
    private final BuildSymmetryGuides module;

    public BuildSymmetryGuidesEventHandler(BuildSymmetryGuides module) {
        if (module == null) throw new IllegalArgumentException("module");
        this.module = module;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (!module.isRunning() || !module.isEnabled() || mc.thePlayer == null
                || mc.theWorld == null || mc.currentScreen != null
                || mc.objectMouseOver == null
                || mc.objectMouseOver.typeOfHit
                        != MovingObjectPosition.MovingObjectType.BLOCK) {
            module.clearTarget();
            return;
        }
        module.updateTarget(mc.objectMouseOver.getBlockPos());
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (!module.isRunning() || !module.isEnabled()) return;
        SymmetryGuideSnapshot snapshot = module.getSnapshot();
        if (snapshot != null) BuildSymmetryGuidesRenderer.render(snapshot);
    }
}
