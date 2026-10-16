package com.alpharip.staffmod.rankviewer;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Draws a compact, colored Alpha rank badge above player nameplates. */
public final class RankViewerRenderer {
    @SubscribeEvent
    public void onRenderPlayerSpecials(RenderPlayerEvent.Specials.Pre event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || event.entityPlayer == null || minecraft.theWorld == null) return;
        EntityPlayer player = event.entityPlayer;
        boolean renderingLocalPlayer = minecraft.thePlayer != null
                && minecraft.thePlayer.getUniqueID().equals(player.getUniqueID());
        // The local player's name is drawn in RenderWorldLastEvent.
        if (renderingLocalPlayer || player.isInvisible()) return;

        AlphaRank rank = RankCache.INSTANCE.get(player.getUniqueID());
        // Only render a custom badge for players with a rank record. Their
        // normal Minecraft nametag remains handled by vanilla rendering.
        if (rank == null) return;

        String label = rank.label().toUpperCase(java.util.Locale.ROOT) + "  |  " + player.getName();
        String emblem = rank.emblem();
        int labelWidth = minecraft.fontRendererObj.getStringWidth(label);
        int emblemWidth = minecraft.fontRendererObj.getStringWidth(emblem);
        int contentWidth = emblemWidth + (emblem.isEmpty() ? 0 : 5) + labelWidth;
        int left = -contentWidth / 2;

        GlStateManager.pushMatrix();
        try {
            GlStateManager.translate(event.x, event.y + player.height + 0.72D, event.z);
            GlStateManager.rotate(-minecraft.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(minecraft.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
            GlStateManager.scale(-0.02F, -0.02F, 0.02F);
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.depthMask(false);

            int rankColor = rank.color();
            int cursor = left;
            if (!emblem.isEmpty()) {
                minecraft.fontRendererObj.drawStringWithShadow(emblem, cursor, 0, rankColor);
                cursor += emblemWidth + 5;
            }
            minecraft.fontRendererObj.drawStringWithShadow(label, cursor, 0, rankColor);
        } finally {
            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
            GlStateManager.enableLighting();
            GlStateManager.popMatrix();
        }
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.thePlayer == null || minecraft.theWorld == null
                || minecraft.gameSettings == null || minecraft.fontRendererObj == null) return;
        if (minecraft.gameSettings.thirdPersonView == 0) return;

        AlphaModule module = AlphaModuleRegistry.find("name-viewer");
        if (module == null || !module.isEnabled() || !module.isUnlocked()) return;

        EntityPlayer player = minecraft.thePlayer;
        if (player.isInvisible()) return;

        double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.partialTicks;
        double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.partialTicks;
        double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.partialTicks;
        net.minecraft.client.renderer.entity.RenderManager renderManager = minecraft.getRenderManager();
        x -= renderManager.viewerPosX;
        y -= renderManager.viewerPosY;
        z -= renderManager.viewerPosZ;

        AlphaRank rank = RankCache.INSTANCE.get(player.getUniqueID());
        String name = player.getName();
        String rankLabel = rank == null ? "" : rank.label().toUpperCase(java.util.Locale.ROOT) + "  |  ";
        String label = rankLabel + name;
        String emblem = rank == null ? "" : rank.emblem();
        int rankColor = rank == null ? 0xFFFFD866 : rank.color();
        int emblemWidth = minecraft.fontRendererObj.getStringWidth(emblem);
        int labelWidth = minecraft.fontRendererObj.getStringWidth(label);
        int totalWidth = labelWidth + (emblem.isEmpty() ? 0 : emblemWidth + 5);

        GlStateManager.pushMatrix();
        try {
            GlStateManager.translate(x, y + player.height + 0.5D, z);
            GlStateManager.rotate(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
            GlStateManager.scale(-0.026666668F, -0.026666668F, 0.026666668F);
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.depthMask(false);

            int cursor = -totalWidth / 2;
            if (!emblem.isEmpty()) {
                minecraft.fontRendererObj.drawStringWithShadow(emblem, cursor, 1, rankColor);
                cursor += emblemWidth + 5;
            }
            minecraft.fontRendererObj.drawStringWithShadow(label, cursor, 1, rankColor);
        } finally {
            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
            GlStateManager.enableLighting();
            GlStateManager.popMatrix();
        }
    }
}
