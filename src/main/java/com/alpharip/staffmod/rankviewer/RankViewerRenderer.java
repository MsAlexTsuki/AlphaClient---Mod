package com.alpharip.staffmod.rankviewer;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
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
        if (renderingLocalPlayer) {
            // The own nametag is intended for third-person view only (F5).
            if (minecraft.gameSettings == null || minecraft.gameSettings.thirdPersonView == 0) return;
            if (module == null || !module.isEnabled() || !module.isUnlocked()) return;
        }
        if (player.isInvisible()) return;
        AlphaRank rank = RankCache.INSTANCE.get(player.getUniqueID());
        // Other players only get a badge when rank data exists. The local player's
        // username is still rendered while rank data is loading or unavailable.
        if (!renderingLocalPlayer && rank == null) return;

        String label = renderingLocalPlayer
                ? (rank == null ? player.getName()
                        : rank.label().toUpperCase(java.util.Locale.ROOT) + "  |  " + player.getName())
                : rank.label().toUpperCase(java.util.Locale.ROOT);
        String emblem = renderingLocalPlayer && rank == null ? "" : rank.emblem();
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
                top + 2, renderingLocalPlayer ? 0xFFFFFFFF : rankColor);

        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    private static void drawRect(Minecraft minecraft, int left, int top, int right, int bottom, int color) {
        minecraft.ingameGUI.drawRect(left, top, right, bottom, color);
    }
}
