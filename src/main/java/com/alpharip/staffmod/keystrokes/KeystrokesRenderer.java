// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.keystrokes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

/** Compact key-grid renderer shared by the live HUD and HUD Editor preview. */
public final class KeystrokesRenderer {
    public static final int BASE_WIDTH = 70;
    public static final int BASE_HEIGHT = 98;
    private KeystrokesRenderer() { }

    public static void render(Minecraft mc, int x, int y, float scale) {
        if (mc == null || mc.fontRendererObj == null) return;
        KeystrokesSettings.load();
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        GlStateManager.scale(scale, scale, 1);
        if (KeystrokesSettings.showMovement()) {
            key(mc, 25, 0, 20, 20, "W", KeystrokesInput.isForward());
            key(mc, 2, 22, 20, 20, "A", KeystrokesInput.isLeft());
            key(mc, 25, 22, 20, 20, "S", KeystrokesInput.isBack());
            key(mc, 48, 22, 20, 20, "D", KeystrokesInput.isRight());
        }
        if (KeystrokesSettings.showMouse()) {
            mouse(mc, 2, 45, "LMB", KeystrokesInput.isLeftMouseDown(), KeystrokesInput.getLeftCps());
            mouse(mc, 36, 45, "RMB", KeystrokesInput.isRightMouseDown(), KeystrokesInput.getRightCps());
        }
        if (KeystrokesSettings.showSpace()) {
            key(mc, 2, 76, 66, 20, "SPACE", KeystrokesInput.isJump());
        }
        GlStateManager.popMatrix();
    }

    private static void mouse(Minecraft mc, int x, int y, String label, boolean down, int cps) {
        box(x, y, 32, 27, down);
        int labelWidth = mc.fontRendererObj.getStringWidth(label);
        mc.fontRendererObj.drawString(label, x + (32 - labelWidth) / 2, y + 3, KeystrokesSettings.getTextColor(), false);
        if (KeystrokesSettings.showCps()) {
            String value = String.valueOf(cps);
            GlStateManager.pushMatrix();
            GlStateManager.translate(x + 16, y + 15, 0);
            GlStateManager.scale(0.72F, 0.72F, 1F);
            mc.fontRendererObj.drawString(value, -mc.fontRendererObj.getStringWidth(value) / 2, 0, KeystrokesSettings.getTextColor(), false);
            GlStateManager.popMatrix();
        }
    }

    private static void key(Minecraft mc, int x, int y, int w, int h, String label, boolean down) {
        box(x, y, w, h, down);
        int labelWidth = mc.fontRendererObj.getStringWidth(label);
        mc.fontRendererObj.drawString(label, x + (w - labelWidth) / 2, y + (h - 8) / 2, KeystrokesSettings.getTextColor(), false);
    }

    private static void box(int x, int y, int w, int h, boolean active) {
        int fill = active ? KeystrokesSettings.getActiveColor() : KeystrokesSettings.getInactiveColor();
        if (KeystrokesSettings.hasBackground()) {
            int alpha = KeystrokesSettings.getBackgroundAlpha();
            int bg = (alpha << 24) | 0x000A0C11;
            if (KeystrokesSettings.isRounded()) {
                roundedRect(x, y, x + w, y + h, 4, bg);
            } else Gui.drawRect(x, y, x + w, y + h, bg);
        }
        if (KeystrokesSettings.isRounded()) roundedRect(x + 1, y + 1, x + w - 1, y + h - 1, 3, fill);
        else Gui.drawRect(x + 1, y + 1, x + w - 1, y + h - 1, fill);
    }

    private static void roundedRect(float left, float top, float right, float bottom, float radius, int color) {
        float a = ((color >> 24) & 255) / 255F, r = ((color >> 16) & 255) / 255F;
        float g = ((color >> 8) & 255) / 255F, b = (color & 255) / 255F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GL11.glColor4f(r, g, b, a);
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2f((left + right) / 2F, (top + bottom) / 2F);
        float[][] centers = {{right-radius,top+radius},{right-radius,bottom-radius},{left+radius,bottom-radius},{left+radius,top+radius}};
        for (int corner=0; corner<4; corner++) {
            float start = -90 + corner * 90;
            for (int step=0; step<=6; step++) {
                double angle = Math.toRadians(start + step * 15);
                GL11.glVertex2d(centers[corner][0] + Math.cos(angle)*radius, centers[corner][1] + Math.sin(angle)*radius);
            }
        }
        GL11.glEnd();
        GL11.glColor4f(1F,1F,1F,1F);
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
    }
}
