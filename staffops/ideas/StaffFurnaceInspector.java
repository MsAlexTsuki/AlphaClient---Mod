package com.alpharip.staffmod.hud;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Displays furnace burn and cooking progress for the selected furnace. */
public final class StaffFurnaceInspector {
    private boolean enabled() {
        AlphaModule module = AlphaModuleRegistry.find("staff-furnace-inspector");
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
        TileEntity tile = mc.theWorld.getTileEntity(pos);
        if (!(tile instanceof TileEntityFurnace)) return;
        TileEntityFurnace furnace = (TileEntityFurnace) tile;
        int burn = furnace.getField(0);
        int burnTotal = furnace.getField(1);
        int cook = furnace.getField(2);
        int cookTotal = furnace.getField(3);
        mc.fontRendererObj.drawStringWithShadow("Furnace Inspector", 6, 6, 0xFFFFFFFF);
        mc.fontRendererObj.drawStringWithShadow("Burn: " + burn + "/" + burnTotal + " | Cook: "
                + cook + "/" + cookTotal, 6, 18, 0xFFFFFFFF);
        mc.fontRendererObj.drawStringWithShadow("Position: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ(),
                6, 30, 0xFFFFFFFF);
    }
}
