// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.rankviewer;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import com.alpharip.staffmod.permissions.SupabaseCommunityService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;
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

            // Keep the panel almost flush with the badge and name: one pixel of
            // outer padding and a low-opacity border, so it does not dominate the nametag.
            int left = cursor - 1;
            int right = cursor + totalWidth + 1;
            int top = -1;
            int bottom = BADGE_SIZE + 1;
            Gui.drawRect(left, top, right, bottom, 0x10000000);
            Gui.drawRect(left, top, right, top + 1, 0x18000000);
            Gui.drawRect(left, bottom - 1, right, bottom, 0x18000000);
            Gui.drawRect(left, top, left + 1, bottom, 0x18000000);
            Gui.drawRect(right - 1, top, right, bottom, 0x18000000);

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

        Team targetTeam = target.getTeam();
        Team ownTeam = minecraft.thePlayer.getTeam();
        ScorePlayerTeam targetScoreTeam = targetTeam instanceof ScorePlayerTeam
                ? (ScorePlayerTeam) targetTeam : null;
        ScorePlayerTeam ownScoreTeam = ownTeam instanceof ScorePlayerTeam
                ? (ScorePlayerTeam) ownTeam : null;
        if (target.getUniqueID().equals(minecraft.thePlayer.getUniqueID())) {
            EnumChatFormatting format = ownScoreTeam == null ? null : ownScoreTeam.getChatFormat();
            if (format != null && format.getColorIndex() >= 0) {
                return 0xFF000000 | getFormattingColor(format);
            }
            return rank == null ? 0xFFFFFFFF : rank.color();
        }
        if (targetTeam != null && ownTeam != null && targetTeam.isSameTeam(ownTeam)) {
            EnumChatFormatting format = targetScoreTeam == null ? null : targetScoreTeam.getChatFormat();
            if (format != null && format.getColorIndex() >= 0) {
                return 0xFF000000 | getFormattingColor(format);
            }
        }
        if (targetScoreTeam != null) {
            EnumChatFormatting format = targetScoreTeam.getChatFormat();
            if (format != null && format.getColorIndex() >= 0) {
                return 0xFF000000 | getFormattingColor(format);
            }
        }
        return 0xFFFFFFFF;
    }

    private static int getFormattingColor(EnumChatFormatting format) {
        if (format == null || format.getColorIndex() < 0) return 0xFFFFFF;
        int index = format.getColorIndex();
        int[] colors = {
                0x000000, 0x0000AA, 0x00AA00, 0x00AAAA,
                0xAA0000, 0xAA00AA, 0xFFAA00, 0xAAAAAA,
                0x555555, 0x5555FF, 0x55FF55, 0x55FFFF,
                0xFF5555, 0xFF55FF, 0xFFFF55, 0xFFFFFF
        };
        return index >= 0 && index < colors.length ? colors[index] : 0xFFFFFF;
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