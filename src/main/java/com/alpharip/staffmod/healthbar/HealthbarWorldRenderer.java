package com.alpharip.staffmod.healthbar;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
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
        double rx = mc.getRenderViewEntity().lastTickPosX
                + (mc.getRenderViewEntity().posX - mc.getRenderViewEntity().lastTickPosX) * partialTicks;
        double ry = mc.getRenderViewEntity().lastTickPosY
                + (mc.getRenderViewEntity().posY - mc.getRenderViewEntity().lastTickPosY) * partialTicks;
        double rz = mc.getRenderViewEntity().lastTickPosZ
                + (mc.getRenderViewEntity().posZ - mc.getRenderViewEntity().lastTickPosZ) * partialTicks;
        float health = Math.max(0.0F, Math.min(1.0F, target.getHealth() / Math.max(1.0F, target.getMaxHealth())));
        float width = 0.9F;
        GlStateManager.pushMatrix();
        GlStateManager.translate(x - rx, y - ry, z - rz);
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(mc.gameSettings.thirdPersonView == 2 ? -mc.getRenderManager().playerViewX : mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-0.025F, -0.025F, 0.025F);
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.disableTexture2D();
        drawRect(-width * 20.0F, -2.0F, width * 20.0F, 2.0F, 0xB0000000);
        drawRect(-width * 20.0F, -2.0F, -width * 20.0F + width * 40.0F * health, 2.0F, health > 0.5F ? 0xFF55DD55 : (health > 0.25F ? 0xFFFFCC33 : 0xFFFF4444));
        GlStateManager.enableTexture2D();
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    private static void drawRect(float left, float top, float right, float bottom, int color) {
        float a = (color >> 24 & 255) / 255.0F;
        float r = (color >> 16 & 255) / 255.0F;
        float g = (color >> 8 & 255) / 255.0F;
        float b = (color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer wr = tessellator.getWorldRenderer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(left, bottom, 0).color(r, g, b, a).endVertex();
        wr.pos(right, bottom, 0).color(r, g, b, a).endVertex();
        wr.pos(right, top, 0).color(r, g, b, a).endVertex();
        wr.pos(left, top, 0).color(r, g, b, a).endVertex();
        tessellator.draw();
    }
}
