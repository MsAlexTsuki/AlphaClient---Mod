package com.alpharip.staffmod.hud;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Summarizes the chest currently targeted by the crosshair. */
public final class StaffChestViewer {
    private boolean enabled() {
        AlphaModule module = AlphaModuleRegistry.find("staff-chest-viewer");
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
        if (!(tile instanceof TileEntityChest)) return;
        IInventory inv = (IInventory) tile;
        int occupied = 0;
        for (int i = 0; i < inv.getSizeInventory(); i++) if (inv.getStackInSlot(i) != null) occupied++;
        mc.fontRendererObj.drawStringWithShadow("Chest: " + inv.getName(), 6, 6, 0xFFFFFFFF);
        mc.fontRendererObj.drawStringWithShadow("Occupied slots: " + occupied + "/" + inv.getSizeInventory(),
                6, 18, 0xFFFFFFFF);
        mc.fontRendererObj.drawStringWithShadow("Position: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ(),
                6, 30, 0xFFFFFFFF);
    }
}
