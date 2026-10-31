package com.alpharip.staffmod.hud;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Shows the closest other player within 32 blocks, without changing targeting or movement. */
public final class NearestEnemyHud {
    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Text event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.theWorld == null || mc.fontRendererObj == null || mc.gameSettings.showDebugInfo) return;
        AlphaModule module = AlphaModuleRegistry.find("nearest-enemy-hud");
        if (module == null || !module.isEnabled() || !module.isUnlocked()) return;

        EntityPlayer nearest = null;
        double nearestDistance = 32.0D;
        for (Object value : mc.theWorld.playerEntities) {
            if (!(value instanceof EntityPlayer)) continue;
            EntityPlayer player = (EntityPlayer) value;
            if (player == mc.thePlayer || player.isDead) continue;
            double distance = mc.thePlayer.getDistanceToEntity(player);
            if (distance < nearestDistance) {
                nearest = player;
                nearestDistance = distance;
            }
        }
        if (nearest != null) {
            mc.fontRendererObj.drawStringWithShadow(
                    String.format("Nearest player: %s (%.1fm)", nearest.getName(), nearestDistance),
                    6, 42, 0xFFFFFFFF);
        }
    }
}
