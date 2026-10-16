package com.alpharip.staffmod.rankviewer;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Renders the rank's image badge immediately before the player's name. */
public final class RankViewerRenderer {
    private static final int BADGE_SIZE = 8;
    private static final int BADGE_GAP = 3;

    @SubscribeEvent
    public void onRenderPlayer(RenderPlayerEvent.Pre event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.thePlayer == null || minecraft.gameSettings == null
                || event.entityPlayer == null) return;

        // In rear-facing F5, hide only the local player model when the camera has clipped
        // inside the player's hitbox. Keep the Name Viewer and rank badge rendering intact.
        if (minecraft.gameSettings.thirdPersonView != 1
                || !minecraft.thePlayer.getUniqueID().equals(event.entityPlayer.getUniqueID())) return;

        net.minecraft.util.AxisAlignedBB bounds = event.entityPlayer.getEntityBoundingBox();
        net.minecraft.client.renderer.entity.RenderManager renderManager = minecraft.getRenderManager();
        if (bounds != null && bounds.isVecInside(new net.minecraft.util.Vec3(
                renderManager.viewerPosX, renderManager.viewerPosY, renderManager.viewerPosZ))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRenderPlayerSpecials(RenderPlayerEvent.Specials.Pre event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || event.entityPlayer == null || minecraft.theWorld == null
                || minecraft.fontRendererObj == null) return;

        EntityPlayer player = event.entityPlayer;
        boolean renderingLocalPlayer = minecraft.thePlayer != null
                && minecraft.thePlayer.getUniqueID().equals(player.getUniqueID());
        // The local player's name is rendered by the Name Viewer below.
        if (renderingLocalPlayer || player.isInvisible()) return;

        AlphaRank rank = RankCache.INSTANCE.get(player.getUniqueID());
        if (rank == null) return;

        drawNameWithBadge(minecraft, rank, player.getName(), event.x,
                event.y + player.height + 0.72D, event.z,
                minecraft.getRenderManager().playerViewY,
                minecraft.getRenderManager().playerViewX, 0.02F);
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.thePlayer == null || minecraft.theWorld == null
                || minecraft.gameSettings == null || minecraft.fontRendererObj == null) return;
        // Render the local Name Viewer in both F5 views, but never in first-person.
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
        drawNameWithBadge(minecraft, rank, player.getName(), x,
                y + player.height + 0.5D, z,
                renderManager.playerViewY, renderManager.playerViewX, 0.026666668F);
    }

    private void drawNameWithBadge(Minecraft minecraft, AlphaRank rank, String name,
                                   double x, double y, double z,
                                   float playerViewY, float playerViewX, float scale) {
        int nameWidth = minecraft.fontRendererObj.getStringWidth(name);
        int badgeWidth = rank == null ? 0 : BADGE_SIZE + BADGE_GAP;
        int totalWidth = nameWidth + badgeWidth;

        GlStateManager.pushMatrix();
        try {
            GlStateManager.translate(x, y, z);
            GlStateManager.rotate(-playerViewY, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(playerViewX, 1.0F, 0.0F, 0.0F);
            GlStateManager.scale(-scale, -scale, scale);
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.depthMask(false);

            int cursor = -totalWidth / 2;
            if (rank != null) {
                GlStateManager.pushMatrix();
                try {
                    minecraft.getTextureManager().bindTexture(rank.texture());
                    GlStateManager.enableTexture2D();
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    Gui.drawModalRectWithCustomSizedTexture(
                            cursor, 0, 0.0F, 0.0F,
                            BADGE_SIZE, BADGE_SIZE, BADGE_SIZE, BADGE_SIZE);
                } finally {
                    GlStateManager.popMatrix();
                }
                cursor += badgeWidth;
            }

            // Keep the player's name; only the rank label/emblem text is replaced by the image.
            minecraft.fontRendererObj.drawStringWithShadow(name, cursor, 1, 0xFFFFFFFF);
        } finally {
            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
            GlStateManager.enableLighting();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();
        }
    }
}