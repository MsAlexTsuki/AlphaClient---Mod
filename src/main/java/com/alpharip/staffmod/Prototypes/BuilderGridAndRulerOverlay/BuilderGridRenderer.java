package com.alpharip.staffmod.buildergridandruleroverlay;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderManager;
import org.lwjgl.opengl.GL11;

/** Camera-relative world rendering for the build grid and ruler axes. */
public final class BuilderGridRenderer {
    private BuilderGridRenderer() { }

    public static void render(BuilderGridSnapshot snapshot) {
        Minecraft mc = Minecraft.getMinecraft();
        if (snapshot == null || mc.thePlayer == null || mc.theWorld == null) return;
        RenderManager manager = mc.getRenderManager();
        double viewerX = manager.viewerPosX;
        double viewerY = manager.viewerPosY;
        double viewerZ = manager.viewerPosZ;
        int radius = snapshot.getAreaRadius();
        double planeY = snapshot.getAnchorY() + 0.035D - viewerY;

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_LINE_BIT
                | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_CURRENT_BIT
                | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glPushMatrix();
        try {
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glDepthMask(false);
            GL11.glLineWidth(1.25F);

            if (snapshot.isGridEnabled()) {
                drawGrid(snapshot, radius, planeY, viewerX, viewerZ);
            }
            if (snapshot.isRulerEnabled()) {
                drawRuler(snapshot, planeY + 0.025D, viewerX, viewerZ);
            }
        } finally {
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }

    private static void drawGrid(BuilderGridSnapshot snapshot, int radius,
            double planeY, double viewerX, double viewerZ) {
        int minX = snapshot.getAnchorX() - radius;
        int maxX = snapshot.getAnchorX() + radius;
        int minZ = snapshot.getAnchorZ() - radius;
        int maxZ = snapshot.getAnchorZ() + radius;
        List<Integer> xLines = BuilderGridCalculator.gridLines(
                minX, maxX, snapshot.getGridSpacing());
        List<Integer> zLines = BuilderGridCalculator.gridLines(
                minZ, maxZ, snapshot.getGridSpacing());

        GL11.glColor4f(0.35F, 0.78F, 1.0F, 0.62F);
        GL11.glBegin(GL11.GL_LINES);
        for (Integer x : xLines) {
            double relativeX = x - viewerX;
            GL11.glVertex3d(relativeX, planeY, minZ - viewerZ);
            GL11.glVertex3d(relativeX, planeY, maxZ - viewerZ);
        }
        for (Integer z : zLines) {
            double relativeZ = z - viewerZ;
            GL11.glVertex3d(minX - viewerX, planeY, relativeZ);
            GL11.glVertex3d(maxX - viewerX, planeY, relativeZ);
        }
        GL11.glEnd();

        // Mark the saved origin with a brighter cross.
        GL11.glLineWidth(2.0F);
        GL11.glColor4f(1.0F, 0.78F, 0.24F, 0.95F);
        double anchorX = snapshot.getAnchorX() - viewerX;
        double anchorZ = snapshot.getAnchorZ() - viewerZ;
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3d(anchorX - 0.45D, planeY + 0.01D, anchorZ);
        GL11.glVertex3d(anchorX + 0.45D, planeY + 0.01D, anchorZ);
        GL11.glVertex3d(anchorX, planeY + 0.01D, anchorZ - 0.45D);
        GL11.glVertex3d(anchorX, planeY + 0.01D, anchorZ + 0.45D);
        GL11.glEnd();
    }

    private static void drawRuler(BuilderGridSnapshot snapshot, double y,
            double viewerX, double viewerZ) {
        int anchorX = snapshot.getAnchorX();
        int anchorZ = snapshot.getAnchorZ();
        int playerX = snapshot.getPlayerX();
        int playerZ = snapshot.getPlayerZ();
        int spacing = snapshot.getGridSpacing();

        GL11.glLineWidth(2.0F);
        GL11.glColor4f(1.0F, 0.72F, 0.20F, 0.95F);
        GL11.glBegin(GL11.GL_LINES);
        // X measurement axis.
        GL11.glVertex3d(anchorX + 0.5D - viewerX, y, anchorZ + 0.5D - viewerZ);
        GL11.glVertex3d(playerX + 0.5D - viewerX, y, anchorZ + 0.5D - viewerZ);
        // Z measurement axis.
        GL11.glVertex3d(playerX + 0.5D - viewerX, y, anchorZ + 0.5D - viewerZ);
        GL11.glVertex3d(playerX + 0.5D - viewerX, y, playerZ + 0.5D - viewerZ);
        GL11.glEnd();

        drawTicks(anchorX, playerX, anchorZ, true, spacing, y, viewerX, viewerZ);
        drawTicks(anchorZ, playerZ, playerX, false, spacing, y, viewerX, viewerZ);
    }

    private static void drawTicks(int start, int end, int fixedCoordinate,
            boolean xAxis, int spacing, double y, double viewerX, double viewerZ) {
        int direction = end >= start ? 1 : -1;
        GL11.glBegin(GL11.GL_LINES);
        for (int coordinate = start; ; coordinate += direction) {
            boolean major = Math.floorMod(coordinate - start, spacing) == 0;
            double length = major ? 0.28D : 0.14D;
            if (xAxis) {
                double x = coordinate + 0.5D - viewerX;
                double z = fixedCoordinate + 0.5D - viewerZ;
                GL11.glVertex3d(x, y, z - length);
                GL11.glVertex3d(x, y, z + length);
            } else {
                double x = fixedCoordinate + 0.5D - viewerX;
                double z = coordinate + 0.5D - viewerZ;
                GL11.glVertex3d(x - length, y, z);
                GL11.glVertex3d(x + length, y, z);
            }
            if (coordinate == end) break;
        }
        GL11.glEnd();
    }
}
