package com.alpharip.staffmod.blockbreakring;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;

/** Renders a progress arc on the face of the block currently being mined. */
public final class BlockBreakRingRenderer {
    private final BlockBreakRingConfig config;

    public BlockBreakRingRenderer(BlockBreakRingConfig config) {
        this.config = config;
    }

    public void render(float partialTicks, float progress) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null || mc.objectMouseOver == null
                || mc.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK
                || progress <= 0.002F) return;

        BlockPos pos = mc.objectMouseOver.getBlockPos();
        EnumFacing face = mc.objectMouseOver.sideHit;
        if (pos == null || face == null) return;

        RenderManager rm = mc.getRenderManager();
        double x = pos.getX() + 0.5D - rm.viewerPosX + face.getFrontOffsetX() * 0.501D;
        double y = pos.getY() + 0.5D - rm.viewerPosY + face.getFrontOffsetY() * 0.501D;
        double z = pos.getZ() + 0.5D - rm.viewerPosZ + face.getFrontOffsetZ() * 0.501D;

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_LINE_BIT | GL11.GL_COLOR_BUFFER_BIT
                | GL11.GL_CURRENT_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glPushMatrix();
        try {
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glLineWidth(config.getLineWidth());
            GL11.glDepthMask(false);

            int rgb = config.getColorRgb();
            float red = ((rgb >> 16) & 255) / 255.0F;
            float green = ((rgb >> 8) & 255) / 255.0F;
            float blue = (rgb & 255) / 255.0F;
            float alpha = config.getOpacity();

            if (config.isShowBackground()) {
                GL11.glColor4f(red, green, blue, alpha * 0.22F);
                drawArc(face, x, y, z, config.getRadius(), 1.0F, 64);
            }
            GL11.glColor4f(red, green, blue, alpha);
            drawArc(face, x, y, z, config.getRadius(), progress, Math.max(2, (int) (64 * progress)));
        } finally {
            GL11.glDepthMask(true);
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }

    private void drawArc(EnumFacing face, double cx, double cy, double cz,
            float radius, float progress, int segments) {
        GL11.glBegin(GL11.GL_LINE_STRIP);
        for (int i = 0; i <= segments; i++) {
            double angle = -Math.PI / 2.0D + Math.PI * 2.0D * progress * i / segments;
            double a = Math.cos(angle) * radius;
            double b = Math.sin(angle) * radius;
            double x = cx, y = cy, z = cz;
            switch (face) {
                case UP:
                case DOWN:
                    x += a; z += b;
                    break;
                case NORTH:
                case SOUTH:
                    x += a; y += b;
                    break;
                case EAST:
                case WEST:
                    z += a; y += b;
                    break;
                default:
                    break;
            }
            GL11.glVertex3d(x, y, z);
        }
        GL11.glEnd();
    }
}
