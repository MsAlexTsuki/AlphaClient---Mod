package com.alpharip.staffmod.rankviewer;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Draws a compact community-rank label above player nameplates. */
public final class RankViewerRenderer {
    @SubscribeEvent
    public void onRenderPlayerSpecials(RenderPlayerEvent.Specials.Pre event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || event.entityPlayer == null || minecraft.theWorld == null) return;

        AlphaModule module = AlphaModuleRegistry.find("rank-viewer");
        if (module == null || !module.isEnabled() || !module.isUnlocked()) return;

        EntityPlayer player = event.entityPlayer;
        if (player == minecraft.thePlayer || player.isInvisible()) return;

        AlphaRank rank = RankCache.INSTANCE.get(player.getUniqueID());
        if (rank == null) return;

        String label = rank.label();
        int textWidth = minecraft.fontRendererObj.getStringWidth(label);
        float scale = 0.02F;

        GlStateManager.pushMatrix();
        GlStateManager.translate(event.x, event.y + player.height + 0.72D, event.z);
        GlStateManager.rotate(-minecraft.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(minecraft.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-scale, -scale, scale);
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        int left = -textWidth / 2;
        drawRect(minecraft, left - 3, -2, left + textWidth + 3,
                minecraft.fontRendererObj.FONT_HEIGHT + 2, 0x990A0C11);
        minecraft.fontRendererObj.drawStringWithShadow(label, left, 0, rank.color());
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    private static void drawRect(Minecraft minecraft, int left, int top, int right, int bottom, int color) {
        minecraft.ingameGUI.drawRect(left, top, right, bottom, color);
    }
}
