package com.alpharip.staffmod.rankviewer;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Draws a compact, colored Alpha rank badge above player nameplates. */
public final class RankViewerRenderer {
    private static final int PANEL = 0xE8101116;
    private static final int EDGE_DARK = 0xFF08090D;

    @SubscribeEvent
    public void onRenderPlayerSpecials(RenderPlayerEvent.Specials.Pre event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || event.entityPlayer == null || minecraft.theWorld == null) return;
        EntityPlayer player = event.entityPlayer;
        AlphaModule module = AlphaModuleRegistry.find("name-viewer");
        boolean renderingLocalPlayer = minecraft.thePlayer != null
                && minecraft.thePlayer.getUniqueID().equals(player.getUniqueID());
        // The local player's name is drawn in RenderWorldLastEvent.
        if (renderingLocalPlayer) return;
        if (player.isInvisible()) return;
        AlphaRank rank = RankCache.INSTANCE.get(player.getUniqueID());
        // Other players only get a badge when rank data exists. The local player's
        // username is still rendered while rank data is loading or unavailable.
        if (!renderingLocalPlayer && rank == null) return;

        String label = rank.label().toUpperCase(java.util.Locale.ROOT);
        String emblem = rank.emblem();
        int labelWidth = minecraft.fontRendererObj.getStringWidth(label);
        int emblemWidth = minecraft.fontRendererObj.getStringWidth(emblem);
        int contentWidth = emblemWidth + (emblem.isEmpty() ? 0 : 8) + labelWidth;
        int left = -contentWidth / 2;
        int top = -2;
        int bottom = minecraft.fontRendererObj.FONT_HEIGHT + 4;

        GlStateManager.pushMatrix();
        GlStateManager.translate(event.x, event.y + player.height + 0.72D, event.z);
        GlStateManager.rotate(-minecraft.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(minecraft.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-0.02F, -0.02F, 0.02F);
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);

        drawRect(minecraft, left - 2, top - 2, left + contentWidth + 3, bottom + 2, EDGE_DARK);
        int rankColor = rank == null ? 0xFFFFD866 : rank.color();
        drawRect(minecraft, left, top, left + contentWidth + 1, bottom, rankColor);
        drawRect(minecraft, left + 2, top + 2, left + contentWidth - 1, bottom - 2, PANEL);
        if (!emblem.isEmpty()) {
            drawRect(minecraft, left + 2, top + 2, left + emblemWidth + 7, bottom - 2, rankColor);
            minecraft.fontRendererObj.drawStringWithShadow(emblem, left + 4, top + 2, 0xFFFFFFFF);
        }
        minecraft.fontRendererObj.drawStringWithShadow(label, left + (emblem.isEmpty() ? 4 : emblemWidth + 10),
                top + 2, rankColor);

        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
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

        String label = player.getName();
        int labelWidth = minecraft.fontRendererObj.getStringWidth(label);
        int labelHeight = minecraft.fontRendererObj.FONT_HEIGHT;
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y + player.height + 0.5D, z);
        GlStateManager.rotate(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-0.026666668F, -0.026666668F, 0.026666668F);
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);

        drawRect(minecraft, -labelWidth / 2 - 3, -2, labelWidth / 2 + 3, labelHeight + 2, EDGE_DARK);
        drawRect(minecraft, -labelWidth / 2 - 1, 0, labelWidth / 2 + 1, labelHeight, PANEL);
        minecraft.fontRendererObj.drawStringWithShadow(label, -labelWidth / 2, 1, 0xFFFFD866);

        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    private static void drawRect(Minecraft minecraft, int left, int top, int right, int bottom, int color) {
        minecraft.ingameGUI.drawRect(left, top, right, bottom, color);
    }
}
