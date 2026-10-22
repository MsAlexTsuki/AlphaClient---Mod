package com.alpharip.staffmod.rankviewer;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import com.alpharip.staffmod.permissions.SupabaseCommunityService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

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
        boolean renderingLocalPlayer = minecraft.thePlayer != null
                && minecraft.thePlayer.getUniqueID().equals(player.getUniqueID());
        if (player.isInvisible()) return;

        AlphaRank rank;
        if (renderingLocalPlayer) {
            // The local nameplate must render during the player's own entity pass so its
            // position and billboard rotation follow the F5 camera naturally.
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

        // Specials.Pre runs with the entity transform applied, so the nameplate stays
        // attached to the player as the camera and cursor move in third person.
        drawNameWithBadge(minecraft, rank, player.getName(), 0.0D,
                player.height + (renderingLocalPlayer ? 0.5D : 0.72D), 0.0D,
                minecraft.getRenderManager().playerViewY,
                minecraft.getRenderManager().playerViewX,
                renderingLocalPlayer ? 0.026666668F : 0.02F);
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
        boolean blendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
        int blendSrcRgb = GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB);
        int blendDstRgb = GL11.glGetInteger(GL14.GL_BLEND_DST_RGB);
        int blendSrcAlpha = GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA);
        int blendDstAlpha = GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
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
            // Rank textures contain transparent pixels; blend them instead of relying on
            // whatever blend state the previous world renderer left active.
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
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
            GlStateManager.depthMask(depthMask);
            restoreCapability(GL11.GL_DEPTH_TEST, depthEnabled);
            restoreCapability(GL11.GL_LIGHTING, lightingEnabled);
            restoreCapability(GL11.GL_TEXTURE_2D, textureEnabled);
            GlStateManager.tryBlendFuncSeparate(blendSrcRgb, blendDstRgb, blendSrcAlpha, blendDstAlpha);
            restoreCapability(GL11.GL_BLEND, blendEnabled);
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
            case GL11.GL_BLEND:
                if (enabled) GlStateManager.enableBlend();
                else GlStateManager.disableBlend();
                break;
            default:
                throw new IllegalArgumentException("Unsupported OpenGL capability: " + capability);
        }
    }
}