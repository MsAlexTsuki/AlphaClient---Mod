package com.alpharip.staffmod.hitbox;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;

/**
 * Renders player bounding boxes without changing Minecraft collision geometry.
 */
public final class HitboxRenderer {
    public void render(RenderWorldLastEvent event, HitboxSettings settings) {
        Minecraft mc = Minecraft.getMinecraft();
        if (event == null || settings == null || !settings.isEnabled()
                || mc == null || mc.thePlayer == null || mc.theWorld == null
                || mc.getRenderViewEntity() == null) {
            return;
        }

        Entity renderView = mc.getRenderViewEntity();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();
        try {
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glLineWidth(settings.getLineWidth());
            GL11.glColor4f(settings.getRed(), settings.getGreen(),
                    settings.getBlue(), settings.getAlpha());

            if (settings.isThroughWalls()) {
                GL11.glDisable(GL11.GL_DEPTH_TEST);
            } else {
                GL11.glEnable(GL11.GL_DEPTH_TEST);
            }
            GL11.glDepthMask(false);

            GL11.glBegin(GL11.GL_LINES);
            try {
                for (Object object : mc.theWorld.loadedEntityList) {
                    if (!(object instanceof EntityPlayer)) {
                        continue;
                    }

                    EntityPlayer player = (EntityPlayer) object;
                    if (player == mc.thePlayer || player.isDead || !player.isEntityAlive()) {
                        continue;
                    }
                    if (!settings.isShowInvisiblePlayers() && player.isInvisible()) {
                        continue;
                    }
                    if (mc.thePlayer.getDistanceToEntity(player) > settings.getRange()) {
                        continue;
                    }

                    AxisAlignedBB box = HitboxGeometry.getInterpolatedRelativeBox(
                            player, renderView, event.partialTicks);
                    if (box != null) {
                        drawBox(box);
                    }
                }
            } finally {
                GL11.glEnd();
            }
        } finally {
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }

    private static void drawBox(AxisAlignedBB box) {
        double x1 = box.minX;
        double y1 = box.minY;
        double z1 = box.minZ;
        double x2 = box.maxX;
        double y2 = box.maxY;
        double z2 = box.maxZ;

        // Bottom rectangle
        edge(x1, y1, z1, x2, y1, z1);
        edge(x2, y1, z1, x2, y1, z2);
        edge(x2, y1, z2, x1, y1, z2);
        edge(x1, y1, z2, x1, y1, z1);

        // Top rectangle
        edge(x1, y2, z1, x2, y2, z1);
        edge(x2, y2, z1, x2, y2, z2);
        edge(x2, y2, z2, x1, y2, z2);
        edge(x1, y2, z2, x1, y2, z1);

        // Vertical edges
        edge(x1, y1, z1, x1, y2, z1);
        edge(x2, y1, z1, x2, y2, z1);
        edge(x2, y1, z2, x2, y2, z2);
        edge(x1, y1, z2, x1, y2, z2);
    }

    private static void edge(double x1, double y1, double z1,
                             double x2, double y2, double z2) {
        GL11.glVertex3d(x1, y1, z1);
        GL11.glVertex3d(x2, y2, z2);
    }
}
