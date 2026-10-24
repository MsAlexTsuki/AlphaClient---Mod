package com.alpharip.staffmod.rankviewer;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import com.alpharip.staffmod.permissions.SupabaseCommunityService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

/** Renders the rank's image badge immediately before the player's name. */
public final class RankViewerRenderer {
    private static final int BADGE_SIZE = 12;
    private static final int BADGE_GAP = 3;

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

        // RenderPlayerEvent.Specials.Pre already runs with the entity transform applied.
        // Use local entity coordinates here; event.x/y/z would apply the position twice.
        drawNameWithBadge(minecraft, rank, player.getName(), 0.0D,
                player.height + 0.72D, 0.0D,
                minecraft.getRenderManager().playerViewY,
                minecraft.getRenderManager().playerViewX, 0.02F);
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.thePlayer == null || minecraft.theWorld == null
                || minecraft.gameSettings == null || minecraft.fontRendererObj == null) return;
        // Name Viewer controls both the local player's name and rank badge in F5.
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

        // The local badge follows the currently signed-in Alpha account, not an arbitrary
        // Minecraft-UUID association (one UUID may be linked to multiple Alpha accounts).
        AlphaRank rank = SupabaseCommunityService.getSignedInDisplayRank();
        if (rank == null) rank = RankCache.INSTANCE.get(player.getUniqueID());
        // Keep the badge slot visible while account rank data is unavailable; a valid
        // signed-in rank or cached Minecraft UUID rank always takes precedence.
        if (rank == null) rank = AlphaRank.MEMBER;
        // Name Viewer displays the player's name together with their rank badge.
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
        int nameColor = getRelationshipNameColor(minecraft, name, rank);

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

            // Barely visible glass panel fitted tightly around badge and name.
            int left = cursor - 2;
            int right = cursor + totalWidth + 2;
            int top = -2;
            int bottom = BADGE_SIZE + 3;
            Gui.drawRect(left, top, right, bottom, 0x18000000);
            Gui.drawRect(left, top, right, top + 1, 0x26000000);
            Gui.drawRect(left, bottom - 1, right, bottom, 0x26000000);
            Gui.drawRect(left, top, left + 1, bottom, 0x26000000);
            Gui.drawRect(right - 1, top, right, bottom, 0x26000000);

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
            minecraft.fontRendererObj.drawStringWithShadow(name, cursor, 1, nameColor);
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


    /**
     * Uses server scoreboard colors when available. Vanilla scoreboard data
     * cannot reliably distinguish faction allies from enemies unless the server
     * exposes that relationship separately.
     */
    private int getRelationshipNameColor(Minecraft minecraft, String name, AlphaRank rank) {
        if (minecraft == null || minecraft.thePlayer == null || minecraft.theWorld == null) {
            return rank == null ? 0xFFFFFFFF : rank.color();
        }

        EntityPlayer target = null;
        for (Object object : minecraft.theWorld.playerEntities) {
            if (object instanceof EntityPlayer) {
                EntityPlayer candidate = (EntityPlayer) object;
                if (candidate.getName().equalsIgnoreCase(name)) {
                    target = candidate;
                    break;
                }
            }
        }
        if (target == null) return rank == null ? 0xFFFFFFFF : rank.color();

        ScorePlayerTeam targetTeam = target.getTeam();
        ScorePlayerTeam ownTeam = minecraft.thePlayer.getTeam();
        if (target.getUniqueID().equals(minecraft.thePlayer.getUniqueID())) {
            if (ownTeam != null && ownTeam.getChatFormat() != null
                    && ownTeam.getChatFormat().getColor() != null) {
                return 0xFF000000 | ownTeam.getChatFormat().getColor();
            }
            return rank == null ? 0xFFFFFFFF : rank.color();
        }
        if (targetTeam != null && ownTeam != null && targetTeam.isSameTeam(ownTeam)) {
            EnumChatFormatting format = targetTeam.getChatFormat();
            if (format != null && format.getColor() != null) {
                return 0xFF000000 | format.getColor();
            }
        }
        if (targetTeam != null) {
            EnumChatFormatting format = targetTeam.getChatFormat();
            if (format != null && format.getColor() != null) {
                return 0xFF000000 | format.getColor();
            }
        }
        return 0xFFFFFFFF;
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