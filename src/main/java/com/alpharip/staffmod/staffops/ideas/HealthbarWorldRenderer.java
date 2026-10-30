package com.alpharip.staffmod.staffops.ideas;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;

/** World-space billboard renderer for the Healthbar module. */
final class HealthbarWorldRenderer {
    private HealthbarWorldRenderer() { }

    static void render(EntityPlayer target, float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.getRenderViewEntity() == null || target == null) return;

        double x = target.lastTickPosX + (target.posX - target.lastTickPosX) * partialTicks;
        double y = target.lastTickPosY + (target.posY - target.lastTickPosY) * partialTicks + target.height + 0.35D;
        double z = target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * partialTicks;

        net.minecraft.entity.Entity view = mc.getRenderViewEntity();
        double rx = view.lastTickPosX + (view.posX - view.lastTickPosX) * partialTicks;
        double ry = view.lastTickPosY + (view.posY - view.lastTickPosY) * partialTicks;
        double rz = view.lastTickPosZ + (view.posZ - view.lastTickPosZ) * partialTicks;

        float maxHealth = Math.max(1.0F, target.getMaxHealth());
        float health = Math.max(0.0F, Math.min(1.0F, target.getHealth() / maxHealth));
        float halfWidth = 18.0F;

        GlStateManager.pushMatrix();
        try {
            GlStateManager.translate(x - rx, y - ry, z - rz);
            GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(
                    mc.gameSettings.thirdPersonView == 2
                            ? -mc.getRenderManager().playerViewX
                            : mc.getRenderManager().playerViewX,
                    1.0F, 0.0F, 0.0F);
            GlStateManager.scale(-0.025F, -0.025F, 0.025F);

            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.depthMask(false);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(
                    GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA,
                    GL11.GL_ONE, GL11.GL_ZERO);
            GlStateManager.disableTexture2D();

            drawRect(-halfWidth, -2.0F, halfWidth, 2.0F, 0xB0000000);
            int fillColor = health > 0.5F
                    ? 0xFF55DD55
                    : (health > 0.25F ? 0xFFFFCC33 : 0xFFFF4444);
            drawRect(-halfWidth, -2.0F, -halfWidth + (halfWidth * 2.0F * health), 2.0F, fillColor);
        } finally {
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
            GlStateManager.enableLighting();
            GlStateManager.popMatrix();
        }
    }

    private static void drawRect(float left, float top, float right, float bottom, int color) {
        float a = (color >> 24 & 255) / 255.0F;
        float r = (color >> 16 & 255) / 255.0F;
        float g = (color >> 8 & 255) / 255.0F;
        float b = (color & 255) / 255.0F;

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer renderer = tessellator.getWorldRenderer();
        renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        renderer.pos(left, bottom, 0.0D).color(r, g, b, a).endVertex();
        renderer.pos(right, bottom, 0.0D).color(r, g, b, a).endVertex();
        renderer.pos(right, top, 0.0D).color(r, g, b, a).endVertex();
        renderer.pos(left, top, 0.0D).color(r, g, b, a).endVertex();
        tessellator.draw();
    }
}
