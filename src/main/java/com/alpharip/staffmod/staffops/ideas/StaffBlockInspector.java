package com.alpharip.staffmod.hud;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Inspects the block currently selected by the crosshair. */
public final class StaffBlockInspector {
    private boolean enabled() {
        AlphaModule module = AlphaModuleRegistry.find("staff-block-inspector");
        return module != null && module.isEnabled() && module.isUnlocked();
    }

    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Text event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT || !enabled()) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.theWorld == null || mc.fontRendererObj == null
                || mc.gameSettings.showDebugInfo || mc.objectMouseOver == null
                || mc.objectMouseOver.getBlockPos() == null) return;
        BlockPos pos = mc.objectMouseOver.getBlockPos();
        net.minecraft.block.state.IBlockState state = mc.theWorld.getBlockState(pos);
        net.minecraft.block.Block block = state.getBlock();
        String id = String.valueOf(block.getRegistryName());
        float hardness = block.getBlockHardness(mc.theWorld, pos);
        int light = mc.theWorld.getLightFromNeighbors(pos);
        mc.fontRendererObj.drawStringWithShadow("Block: " + id, 6, 6, 0xFFFFFFFF);
        mc.fontRendererObj.drawStringWithShadow("Pos: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()
                + " | meta " + block.getMetaFromState(state), 6, 18, 0xFFFFFFFF);
        mc.fontRendererObj.drawStringWithShadow("Hardness: " + String.format("%.2f", hardness)
                + " | Light: " + light + "/15", 6, 30, 0xFFFFFFFF);
    }
}
