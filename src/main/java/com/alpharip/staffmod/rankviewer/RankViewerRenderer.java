package com.alpharip.staffmod.rankviewer;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import com.alpharip.staffmod.permissions.SupabaseCommunityService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

/** Renders the rank's image badge immediately before the player's name. */
public final class RankViewerRenderer {
    private static final int BADGE_SIZE = 8;
    private static final int BADGE_GAP = 3;

    @SubscribeEvent
    public void onRenderPlayerSpecials(RenderPlayerEvent.Specials.Pre event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || event.entityPlayer == null || minecraft.theWorld == null
                || minecraft.fontRendererObj == null) return;

        EntityPlayer player = event.entityPlayer;
        if (player.isInvisible()) return;

        boolean renderingLocalPlayer = minecraft.thePlayer != null
                && minecraft.thePlayer.getUniqueID().equals(player.getUniqueID());
        AlphaRank rank;
        if (renderingLocalPlayer) {
            // Render the local player's badge in the same world-space event as other players.
            // Name Viewer controls whether the local badge is shown in third-person.
            if (minecraft.gameSettings == null || minecraft.gameSettings.thirdPersonView == 0) return;
            AlphaModule module = AlphaModuleRegistry.find("name-viewer");
            if (module == null || !module.isEnabled() || !module.isUnlocked()) return;
            rank = SupabaseCommunityService.getSignedInDisplayRank();
            if (rank == null) rank = RankCache.INSTANCE.get(player.getUniqueID());
            if (rank == null) rank = AlphaRank.MEMBER;
        } else {
            rank = RankCache.INSTANCE.get(player.getUniqueID());
            if (rank == null) return;
        }

        drawNameWithBadge(minecraft, rank, player.getName(), event.x,
                event.y + player.height + 0.72D, event.z,
                minecraft.getRenderManager().playerViewY,
                minecraft.getRenderManager().playerViewX, 0.02F);
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        // The local player's badge is rendered from RenderPlayerEvent.Specials.Pre above,
        // using the same interpolated world-space coordinates as every other player's badge.
        // Do not render it a second time from RenderWorldLastEvent.
    }

    private void drawNameWithBadge(Minecraft minecraft, AlphaRank rank, String name,
                                   double x, double y, double z,
                                   float playerViewY, float playerViewX, float scale) {
        int nameWidth = minecraft.fontRendererObj.getStringWidth(name);
        int badgeWidth = rank == null ? 0 : BADGE_SIZE + BADGE_GAP;
        int totalWidth = nameWidth + badgeWidth;

        // Capture the state that this renderer changes. World rendering and the vanilla HUD
        // share the OpenGL context, so forcing states to "enabled" can leak into later renders.
        boolean depthEnabled = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean lightingEnabled = GL11.glIsEnabled(GL11.GL_LIGHTING);
        boolean textureEnabled = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
        boolean depthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        java.nio.FloatBuffer previousColor = org.lwjgl.BufferUtils.createFloatBuffer(16);
        GL11.glGetFloat(GL11.GL_CURRENT_COLOR, previousColor);
        float red = previousColor.get(0);
        float green = previousColor.get(1);
        float blue = previousColor.get(2);
        float alpha = previousColor.get(3);

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
                            BADGE_SIZE, BADGE_SIZE, 64, 64);
                } finally {
                    GlStateManager.popMatrix();
                }
                cursor += badgeWidth;
            }

            // Keep the player's name; only the rank label/emblem text is replaced by the image.
            minecraft.fontRendererObj.drawStringWithShadow(name, cursor, 1, 0xFFFFFFFF);
        } finally {
            GlStateManager.depthMask(depthMask);
            restoreCapability(GL11.GL_DEPTH_TEST, depthEnabled);
            restoreCapability(GL11.GL_LIGHTING, lightingEnabled);
            restoreCapability(GL11.GL_TEXTURE_2D, textureEnabled);
            GlStateManager.color(red, green, blue, alpha);
            GlStateManager.popMatrix();
        }
    }

    private static void restoreCapability(int capability, boolean enabled) {
        switch (capability) {
            case GL11.GL_DEPTH_TEST:
                if (enabled) GlStateManager.enableDepth();
                else GlStateManager.disableDepth();
                break;
            case GL11.GL_LIGHTING:
                if (enabled) GlStateManager.enableLighting();
                else GlStateManager.disableLighting();
                break;
            case GL11.GL_TEXTURE_2D:
                if (enabled) GlStateManager.enableTexture2D();
                else GlStateManager.disableTexture2D();
                break;
            default:
                throw new IllegalArgumentException("Unsupported OpenGL capability: " + capability);
        }
    }
}