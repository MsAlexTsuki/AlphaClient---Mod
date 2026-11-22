// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

/** Small OpenGL helpers for the rounded AlphaClient interface. */
public final class AlphaGuiDraw {
    private AlphaGuiDraw() {
    }

    public static void roundedRect(float x, float y, float width, float height,
                                   float radius, int argb) {
        if (width <= 0F || height <= 0F) return;
        float r = Math.max(0F, Math.min(radius, Math.min(width, height) / 2F));
        float alpha = ((argb >>> 24) & 255) / 255F;
        float red = ((argb >>> 16) & 255) / 255F;
        float green = ((argb >>> 8) & 255) / 255F;
        float blue = (argb & 255) / 255F;

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GL11.glColor4f(red, green, blue, alpha);
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2f(x + width / 2F, y + height / 2F);
        float[][] corners = {
                {x + width - r, y + r, -90F},
                {x + width - r, y + height - r, 0F},
                {x + r, y + height - r, 90F},
                {x + r, y + r, 180F}
        };
        for (float[] corner : corners) {
            for (int step = 0; step <= 6; step++) {
                double angle = Math.toRadians(corner[2] + step * 15F);
                GL11.glVertex2d(corner[0] + Math.cos(angle) * r,
                        corner[1] + Math.sin(angle) * r);
            }
        }
        GL11.glEnd();
        GL11.glColor4f(1F, 1F, 1F, 1F);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
}
