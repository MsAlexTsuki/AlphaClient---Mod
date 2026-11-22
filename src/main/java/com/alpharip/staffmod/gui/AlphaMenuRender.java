// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

import org.lwjgl.opengl.GL11;

/**
 * Rendering primitives used only by the AlphaClient Mod Menu.
 * All OpenGL attributes are restored after each shape to avoid leaking state
 * into the Home menu, HUD editor, or other Minecraft screens.
 */
public final class AlphaMenuRender {
    private AlphaMenuRender() {}

    public static void roundedRect(float left, float top, float right, float bottom, float radius, int argb) {
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
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glColor4f(red, green, blue, alpha);
        // A single convex polygon avoids the radial triangle-fan seams that
        // can show through translucent rounded surfaces.
        GL11.glBegin(GL11.GL_POLYGON);
        addRoundedVertices(left, top, right, bottom, r);
        GL11.glEnd();
        GL11.glPopAttrib();
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
        addRoundedVertices(left, top, right, bottom, r);
        GL11.glEnd();
        GL11.glPopAttrib();
    }

    private static void addRoundedVertices(float left, float top, float right, float bottom, float radius) {
        if (radius <= 0.5F) {
            GL11.glVertex2f(left, top);
            GL11.glVertex2f(right, top);
            GL11.glVertex2f(right, bottom);
            GL11.glVertex2f(left, bottom);
            return;
        }
        addCorner(right - radius, top + radius, radius, -90.0F);
        addCorner(right - radius, bottom - radius, radius, 0.0F);
        addCorner(left + radius, bottom - radius, radius, 90.0F);
        addCorner(left + radius, top + radius, radius, 180.0F);
        GL11.glVertex2f(left + radius, top);
    }

    private static void addCorner(float centerX, float centerY, float radius, float startDegrees) {
        for (int step = 0; step <= 6; step++) {
            double angle = Math.toRadians(startDegrees + step * 15.0D);
            GL11.glVertex2f(centerX + (float) Math.cos(angle) * radius,
                    centerY + (float) Math.sin(angle) * radius);
        }
    }
}
