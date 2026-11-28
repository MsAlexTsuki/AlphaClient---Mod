package com.alpharip.staffmod.hud;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Shows local block light level from 0 to 15. */
public final class LightLevelHud {
    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Text event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.theWorld == null || mc.fontRendererObj == null || mc.gameSettings.showDebugInfo) return;
        AlphaModule module = AlphaModuleRegistry.find("light-level-hud");
        if (module == null || !module.isEnabled() || !module.isUnlocked()) return;
        BlockPos pos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
        int light = mc.theWorld.getLight(pos);
        mc.fontRendererObj.drawStringWithShadow("Light: " + light + "/15", 6, 162, 0xFFFFFFFF);
    }
}
