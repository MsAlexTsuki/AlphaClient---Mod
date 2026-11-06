package com.alpharip.staffmod.blockbreakring;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/** Own Forge event adapter; registration is controlled by the module lifecycle. */
public final class BlockBreakRingEventHandler {
    private final BlockBreakProgressRing module;
    private final BlockBreakRingAnimation animation;
    private final BlockBreakRingRenderer renderer;

    public BlockBreakRingEventHandler(BlockBreakProgressRing module,
            BlockBreakRingAnimation animation, BlockBreakRingRenderer renderer) {
        this.module = module;
        this.animation = animation;
        this.renderer = renderer;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (!module.isEnabled() || mc.thePlayer == null || mc.theWorld == null
                || mc.currentScreen != null || mc.objectMouseOver == null
                || mc.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
            animation.update(0.0F, module.getConfig().isSmoothAnimation());
            return;
        }
        float target = mc.playerController == null ? 0.0F : mc.playerController.curBlockDamageMP;
        animation.update(target, module.getConfig().isSmoothAnimation());
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (!module.isEnabled() || !module.isRunning()) return;
        renderer.render(event.partialTicks, animation.getProgress());
    }
}
