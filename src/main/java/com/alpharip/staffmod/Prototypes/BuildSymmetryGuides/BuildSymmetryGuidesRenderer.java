package com.alpharip.staffmod.buildsymmetryguides;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderManager;
import org.lwjgl.opengl.GL11;

/** Draws mirror destination outlines and horizontal reference-axis guides. */
public final class BuildSymmetryGuidesRenderer {
    private static final int[][] EDGES = {
            {0, 1}, {1, 2}, {2, 3}, {3, 0},
            {4, 5}, {5, 6}, {6, 7}, {7, 4},
            {0, 4}, {1, 5}, {2, 6}, {3, 7}
    };

    private BuildSymmetryGuidesRenderer() { }

    public static void render(SymmetryGuideSnapshot snapshot) {
        Minecraft mc = Minecraft.getMinecraft();
        if (snapshot == null || mc.thePlayer == null || mc.theWorld == null) return;
        RenderManager manager = mc.getRenderManager();
        double viewerX = manager.viewerPosX;
        double viewerY = manager.viewerPosY;
        double viewerZ = manager.viewerPosZ;
        int rgb = snapshot.getColorRgb();
        float red = ((rgb >> 16) & 255) / 255.0F;
        float green = ((rgb >> 8) & 255) / 255.0F;
        float blue = (rgb & 255) / 255.0F;

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_LINE_BIT
                | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_CURRENT_BIT
                | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glPushMatrix();
        try {
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glLineWidth(2.0F);
            GL11.glDepthMask(false);
            GL11.glColor4f(red, green, blue, snapshot.getOpacity());

            if (snapshot.isShowAxisLines()) {
                drawAxes(snapshot, mc.thePlayer.posY, viewerX, viewerY, viewerZ);
            }
            if (snapshot.isShowConnectionLines()) {
                drawConnections(snapshot, mc.thePlayer.posX, mc.thePlayer.posY,
                        mc.thePlayer.posZ, viewerX, viewerY, viewerZ);
            }
            if (snapshot.isShowMirrorPreview()) {
                drawMirrorOutlines(snapshot, mc.thePlayer.posX, mc.thePlayer.posY,
                        mc.thePlayer.posZ, viewerX, viewerY, viewerZ);
            }
        } finally {
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }

    private static void drawAxes(SymmetryGuideSnapshot snapshot, double playerY,
            double viewerX, double viewerY, double viewerZ) {
        BlockCoordinates origin = snapshot.getOrigin();
        double x = origin.getX() + 0.5D - viewerX;
        double z = origin.getZ() + 0.5D - viewerZ;
        double y = Math.floor(playerY) + 0.02D - viewerY;
        double distance = snapshot.getGuideDistance();
        GL11.glBegin(GL11.GL_LINES);
        if (snapshot.getAxis().reflectsX()) {
            GL11.glVertex3d(x, y, z - distance);
            GL11.glVertex3d(x, y, z + distance);
        }
        if (snapshot.getAxis().reflectsZ()) {
            GL11.glVertex3d(x - distance, y, z);
            GL11.glVertex3d(x + distance, y, z);
        }
        GL11.glEnd();
    }

    private static void drawConnections(SymmetryGuideSnapshot snapshot,
            double playerX, double playerY, double playerZ,
            double viewerX, double viewerY, double viewerZ) {
        BlockCoordinates source = snapshot.getSource();
        double sx = source.getX() + 0.5D - viewerX;
        double sy = source.getY() + 0.5D - viewerY;
        double sz = source.getZ() + 0.5D - viewerZ;
        double maxDistanceSq = (double) snapshot.getGuideDistance()
                * snapshot.getGuideDistance();
        GL11.glLineWidth(1.5F);
        GL11.glBegin(GL11.GL_LINES);
        for (SymmetryGuidePoint point : snapshot.getMirrorPoints()) {
            BlockCoordinates target = point.getPosition();
            double dx = target.getX() + 0.5D - playerX;
            double dy = target.getY() + 0.5D - playerY;
            double dz = target.getZ() + 0.5D - playerZ;
            if (dx * dx + dy * dy + dz * dz > maxDistanceSq) continue;
            GL11.glVertex3d(sx, sy, sz);
            GL11.glVertex3d(target.getX() + 0.5D - viewerX,
                    target.getY() + 0.5D - viewerY,
                    target.getZ() + 0.5D - viewerZ);
        }
        GL11.glEnd();
    }

    private static void drawMirrorOutlines(SymmetryGuideSnapshot snapshot,
            double playerX, double playerY, double playerZ,
            double viewerX, double viewerY, double viewerZ) {
        double maxDistanceSq = (double) snapshot.getGuideDistance()
                * snapshot.getGuideDistance();
        for (SymmetryGuidePoint point : snapshot.getMirrorPoints()) {
            BlockCoordinates pos = point.getPosition();
            double dx = pos.getX() + 0.5D - playerX;
            double dy = pos.getY() + 0.5D - playerY;
            double dz = pos.getZ() + 0.5D - playerZ;
            if (dx * dx + dy * dy + dz * dz > maxDistanceSq) continue;
            drawBlockOutline(pos, viewerX, viewerY, viewerZ);
        }
    }

    private static void drawBlockOutline(BlockCoordinates pos,
            double viewerX, double viewerY, double viewerZ) {
        double x = pos.getX() - viewerX + 0.02D;
        double y = pos.getY() - viewerY + 0.02D;
        double z = pos.getZ() - viewerZ + 0.02D;
        double[][] corners = {
                {x, y, z}, {x + 0.96D, y, z},
                {x + 0.96D, y, z + 0.96D}, {x, y, z + 0.96D},
                {x, y + 0.96D, z}, {x + 0.96D, y + 0.96D, z},
                {x + 0.96D, y + 0.96D, z + 0.96D},
                {x, y + 0.96D, z + 0.96D}
        };
        GL11.glBegin(GL11.GL_LINES);
        for (int[] edge : EDGES) {
            double[] first = corners[edge[0]];
            double[] second = corners[edge[1]];
            GL11.glVertex3d(first[0], first[1], first[2]);
            GL11.glVertex3d(second[0], second[1], second[2]);
        }
        GL11.glEnd();
    }
}
