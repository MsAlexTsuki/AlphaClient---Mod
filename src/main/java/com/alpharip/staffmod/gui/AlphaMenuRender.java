// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

import net.minecraft.client.gui.Gui;
import org.lwjgl.opengl.GL11;

/**
 * Rounded rendering primitives used only by the AlphaClient Mod Menu.
 * Filled shapes use rectangular strips plus four quarter-circles to avoid
 * triangle-fan seams and diagonal artifacts on translucent surfaces.
 */
public final class AlphaMenuRender {
    private AlphaMenuRender() {}

    public static void roundedRect(float left, float top, float right, float bottom, float radius, int argb) {
        if (right <= left || bottom <= top) return;
        float r = Math.max(0.0F, Math.min(radius, Math.min(right - left, bottom - top) / 2.0F));
        if (r <= 0.5F) {
            Gui.drawRect((int) left, (int) top, (int) right, (int) bottom, argb);
            return;
        }
        int color = argb;
        Gui.drawRect((int) (left + r), (int) top, (int) (right - r), (int) bottom, color);
        Gui.drawRect((int) left, (int) (top + r), (int) right, (int) (bottom - r), color);
        drawCorner(left + r, top + r, r, 180.0F, 270.0F, color);
        drawCorner(right - r, top + r, r, 270.0F, 360.0F, color);
        drawCorner(right - r, bottom - r, r, 0.0F, 90.0F, color);
        drawCorner(left + r, bottom - r, r, 90.0F, 180.0F, color);
    }

    public static void roundedOutline(float left, float top, float right, float bottom, float radius, int argb) {
        if (right <= left || bottom <= top) return;
        float r = Math.max(0.0F, Math.min(radius, Math.min(right - left, bottom - top) / 2.0F));
        float red = ((argb >> 16) & 255) / 255.0F;
        float green = ((argb >> 8) & 255) / 255.0F;
        float blue = (argb & 255) / 255.0F;
        float alpha = ((argb >>> 24) & 255) / 255.0F;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(1.0F);
        GL11.glColor4f(red, green, blue, alpha);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        addRoundedOutlineVertices(left, top, right, bottom, r);
        GL11.glEnd();
        GL11.glPopAttrib();
    }

    private static void drawCorner(float centerX, float centerY, float radius,
                                   float startDegrees, float endDegrees, int argb) {
        float red = ((argb >> 16) & 255) / 255.0F;
        float green = ((argb >> 8) & 255) / 255.0F;
        float blue = (argb & 255) / 255.0F;
        float alpha = ((argb >>> 24) & 255) / 255.0F;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(red, green, blue, alpha);
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2f(centerX, centerY);
        for (int step = 0; step <= 12; step++) {
            double angle = Math.toRadians(startDegrees + (endDegrees - startDegrees) * step / 12.0D);
            GL11.glVertex2f(centerX + (float) Math.cos(angle) * radius,
                    centerY + (float) Math.sin(angle) * radius);
        }
        GL11.glEnd();
        GL11.glPopAttrib();
    }

    private static void addRoundedOutlineVertices(float left, float top, float right, float bottom, float radius) {
        if (radius <= 0.5F) {
            GL11.glVertex2f(left, top);
            GL11.glVertex2f(right, top);
            GL11.glVertex2f(right, bottom);
            GL11.glVertex2f(left, bottom);
            return;
        }
        addOutlineCorner(right - radius, top + radius, radius, -90.0F);
        addOutlineCorner(right - radius, bottom - radius, radius, 0.0F);
        addOutlineCorner(left + radius, bottom - radius, radius, 90.0F);
        addOutlineCorner(left + radius, top + radius, radius, 180.0F);
        GL11.glVertex2f(left + radius, top);
    }

    private static void addOutlineCorner(float centerX, float centerY, float radius, float startDegrees) {
        for (int step = 0; step <= 6; step++) {
            double angle = Math.toRadians(startDegrees + step * 15.0D);
            GL11.glVertex2f(centerX + (float) Math.cos(angle) * radius,
                    centerY + (float) Math.sin(angle) * radius);
        }
    }
}
