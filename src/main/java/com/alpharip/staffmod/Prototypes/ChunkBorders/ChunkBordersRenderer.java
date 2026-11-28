package com.alpharip.staffmod.chunkborders;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderManager;
import org.lwjgl.opengl.GL11;

/** Draws camera-relative chunk boundary lines during the world render pass. */
public final class ChunkBordersRenderer {
    private static final int WORLD_MIN_Y = 0;
    private static final int WORLD_MAX_Y = 256;
    private static final int CHUNK_SIZE = 16;

    private ChunkBordersRenderer() { }

    public static void render(ChunkBordersSnapshot snapshot) {
        Minecraft mc = Minecraft.getMinecraft();
        if (snapshot == null || mc.thePlayer == null || mc.theWorld == null) return;

        RenderManager renderManager = mc.getRenderManager();
        double viewerX = renderManager.viewerPosX;
        double viewerY = renderManager.viewerPosY;
        double viewerZ = renderManager.viewerPosZ;
        int radius = snapshot.getRenderRadius();
        int minChunkX = snapshot.getPlayerChunkX() - radius;
        int maxChunkX = snapshot.getPlayerChunkX() + radius;
        int minChunkZ = snapshot.getPlayerChunkZ() - radius;
        int maxChunkZ = snapshot.getPlayerChunkZ() + radius;
        ChunkBorderStyle style = snapshot.getStyle();

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

            if (snapshot.isShowVerticalPillars()) {
                setColor(style.getVerticalRgb(), snapshot.getOpacity());
                GL11.glLineWidth(style.getLineWidth());
                drawVerticalPillars(minChunkX, maxChunkX, minChunkZ, maxChunkZ,
                        viewerX, viewerY, viewerZ);
            }
            if (snapshot.isShowHorizontalGrid()) {
                setColor(style.getHorizontalRgb(), snapshot.getOpacity());
                GL11.glLineWidth(style.getLineWidth());
                drawHorizontalGrid(minChunkX, maxChunkX, minChunkZ, maxChunkZ,
                        viewerX, viewerY, viewerZ);
            }
            if (snapshot.isHighlightCurrentChunk()) {
                drawCurrentChunk(snapshot, mc.thePlayer.posY, viewerX, viewerY, viewerZ);
            }
        } finally {
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }

    private static void drawVerticalPillars(int minChunkX, int maxChunkX,
            int minChunkZ, int maxChunkZ,
            double viewerX, double viewerY, double viewerZ) {
        GL11.glBegin(GL11.GL_LINES);
        for (int chunkX = minChunkX; chunkX <= maxChunkX + 1; chunkX++) {
            double x = ((double) chunkX * CHUNK_SIZE) - viewerX;
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ + 1; chunkZ++) {
                double z = ((double) chunkZ * CHUNK_SIZE) - viewerZ;
                GL11.glVertex3d(x, WORLD_MIN_Y - viewerY, z);
                GL11.glVertex3d(x, WORLD_MAX_Y - viewerY, z);
            }
        }
        GL11.glEnd();
    }

    private static void drawHorizontalGrid(int minChunkX, int maxChunkX,
            int minChunkZ, int maxChunkZ,
            double viewerX, double viewerY, double viewerZ) {
        GL11.glBegin(GL11.GL_LINES);
        for (int y = WORLD_MIN_Y; y <= WORLD_MAX_Y; y += CHUNK_SIZE) {
            double relativeY = y - viewerY;
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ + 1; chunkZ++) {
                double z = ((double) chunkZ * CHUNK_SIZE) - viewerZ;
                for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
                    double x1 = ((double) chunkX * CHUNK_SIZE) - viewerX;
                    double x2 = x1 + CHUNK_SIZE;
                    GL11.glVertex3d(x1, relativeY, z);
                    GL11.glVertex3d(x2, relativeY, z);
                }
            }
            for (int chunkX = minChunkX; chunkX <= maxChunkX + 1; chunkX++) {
                double x = ((double) chunkX * CHUNK_SIZE) - viewerX;
                for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                    double z1 = ((double) chunkZ * CHUNK_SIZE) - viewerZ;
                    double z2 = z1 + CHUNK_SIZE;
                    GL11.glVertex3d(x, relativeY, z1);
                    GL11.glVertex3d(x, relativeY, z2);
                }
            }
        }
        GL11.glEnd();
    }

    private static void drawCurrentChunk(ChunkBordersSnapshot snapshot,
            double playerY, double viewerX, double viewerY, double viewerZ) {
        double minX = ((double) snapshot.getPlayerChunkX() * CHUNK_SIZE) - viewerX;
        double maxX = minX + CHUNK_SIZE;
        double minZ = ((double) snapshot.getPlayerChunkZ() * CHUNK_SIZE) - viewerZ;
        double maxZ = minZ + CHUNK_SIZE;
        double y = Math.floor(playerY) + 0.04D - viewerY;
        setColor(0xFFFFFFFF, Math.min(1.0F, snapshot.getOpacity() + 0.20F));
        GL11.glLineWidth(Math.max(2.0F, snapshot.getStyle().getLineWidth()));
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex3d(minX, y, minZ);
        GL11.glVertex3d(maxX, y, minZ);
        GL11.glVertex3d(maxX, y, maxZ);
        GL11.glVertex3d(minX, y, maxZ);
        GL11.glEnd();
    }

    private static void setColor(int rgb, float alpha) {
        float red = ((rgb >> 16) & 255) / 255.0F;
        float green = ((rgb >> 8) & 255) / 255.0F;
        float blue = (rgb & 255) / 255.0F;
        GL11.glColor4f(red, green, blue, alpha);
    }
}
