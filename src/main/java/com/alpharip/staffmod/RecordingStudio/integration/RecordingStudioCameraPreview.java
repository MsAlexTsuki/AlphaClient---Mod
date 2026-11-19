package com.alpharip.staffmod.RecordingStudio.integration;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

/**
 * Copies the current Minecraft render view into a reusable texture for the Studio viewport
 * and optional movable HUD preview. No second world-render pass is required.
 */
public final class RecordingStudioCameraPreview {
    private static final int HEADER_HEIGHT = 16;
    private static final int MIN_WIDTH = 128;
    private static final int MIN_HEIGHT = 88;

    private final RecordingStudioRuntime runtime;
    private int textureId = -1;
    private int textureWidth;
    private int textureHeight;
    private int x = -1;
    private int y = 52;
    private int width = 220;
    private int height = 140;
    private boolean visible;
    private boolean dragging;
    private boolean resizing;
    private int dragOffsetX;
    private int dragOffsetY;

    public RecordingStudioCameraPreview(RecordingStudioRuntime runtime) {
        if (runtime == null) throw new IllegalArgumentException("runtime");
        this.runtime = runtime;
    }

    public synchronized boolean isVisible() {
        return visible;
    }

    public synchronized void toggle() {
        visible = !visible;
        dragging = false;
        resizing = false;
    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        if (runtime.isClosed()) return;

        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.theWorld == null
                || minecraft.displayWidth < 1 || minecraft.displayHeight < 1) return;

        ensureTexture(minecraft.displayWidth, minecraft.displayHeight);
        if (textureId == -1) return;

        GL11.glPushAttrib(GL11.GL_TEXTURE_BIT | GL11.GL_ENABLE_BIT);
        try {
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
            GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0,
                    textureWidth, textureHeight);
        } finally {
            GL11.glPopAttrib();
        }
    }

    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type == RenderGameOverlayEvent.ElementType.ALL) {
            drawPreview();
        }
    }

    @SubscribeEvent
    public void onMouseInput(MouseEvent event) {
        if (!isVisible()) return;

        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.displayWidth < 1 || minecraft.displayHeight < 1) return;

        ScaledResolution resolution = new ScaledResolution(minecraft);
        int mouseX = event.x * resolution.getScaledWidth() / minecraft.displayWidth;
        int mouseY = resolution.getScaledHeight()
                - event.y * resolution.getScaledHeight() / minecraft.displayHeight - 1;

        if (event.button == 0 && event.buttonstate) {
            if (inside(mouseX, mouseY, x, y, width, height)) {
                if (event.isCancelable()) event.setCanceled(true);
                if (inside(mouseX, mouseY, x + width - 14, y + height - 14, 14, 14)) {
                    resizing = true;
                    dragging = false;
                    dragOffsetX = mouseX;
                    dragOffsetY = mouseY;
                } else if (inside(mouseX, mouseY, x, y, width, HEADER_HEIGHT)) {
                    dragging = true;
                    resizing = false;
                    dragOffsetX = mouseX - x;
                    dragOffsetY = mouseY - y;
                }
            }
            return;
        }

        if (event.button == 0 && !event.buttonstate) {
            if ((dragging || resizing) && event.isCancelable()) event.setCanceled(true);
            dragging = false;
            resizing = false;
            return;
        }

        if (!Mouse.isButtonDown(0)) {
            dragging = false;
            resizing = false;
            return;
        }

        if (dragging) {
            if (event.isCancelable()) event.setCanceled(true);
            x = clamp(mouseX - dragOffsetX, 0, Math.max(0, resolution.getScaledWidth() - width));
            y = clamp(mouseY - dragOffsetY, 0, Math.max(0, resolution.getScaledHeight() - height));
        } else if (resizing) {
            if (event.isCancelable()) event.setCanceled(true);
            int deltaX = mouseX - dragOffsetX;
            int deltaY = mouseY - dragOffsetY;
            width = clamp(width + deltaX, MIN_WIDTH, Math.max(MIN_WIDTH, resolution.getScaledWidth() - x));
            height = clamp(height + deltaY, MIN_HEIGHT, Math.max(MIN_HEIGHT, resolution.getScaledHeight() - y));
            dragOffsetX = mouseX;
            dragOffsetY = mouseY;
        }
    }

    /** Draws the current Minecraft view inside the main Studio viewport. */
    public void drawLiveViewport(int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) return;
        Gui.drawRect(x, y, x + width, y + height, 0xFF080808);
        if (textureId == -1 || textureWidth < 1 || textureHeight < 1) {
            Minecraft minecraft = Minecraft.getMinecraft();
            if (minecraft != null) {
                minecraft.fontRendererObj.drawString("Waiting for Minecraft frame...",
                        x + 8, y + 8, 0xFFBBBBBB);
            }
            return;
        }

        float scale = Math.min((float) width / (float) textureWidth,
                (float) height / (float) textureHeight);
        int drawWidth = Math.max(1, Math.round(textureWidth * scale));
        int drawHeight = Math.max(1, Math.round(textureHeight * scale));
        int drawX = x + (width - drawWidth) / 2;
        int drawY = y + (height - drawHeight) / 2;
        drawTexture(drawX, drawY, drawWidth, drawHeight);
    }

    private void drawTexture(int x, int y, int width, int height) {
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_TEXTURE_BIT
                | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_CURRENT_BIT);
        GL11.glPushMatrix();
        try {
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glTexCoord2f(0.0F, 0.0F); GL11.glVertex3f(x, y + height, 0.0F);
            GL11.glTexCoord2f(1.0F, 0.0F); GL11.glVertex3f(x + width, y + height, 0.0F);
            GL11.glTexCoord2f(1.0F, 1.0F); GL11.glVertex3f(x + width, y, 0.0F);
            GL11.glTexCoord2f(0.0F, 1.0F); GL11.glVertex3f(x, y, 0.0F);
            GL11.glEnd();
        } finally {
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }

    /** Called by the Studio screen after its own widgets so the preview stays on top. */
    public void drawPreview() {
        if (!isVisible() || textureId == -1) return;

        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null) return;
        ScaledResolution resolution = new ScaledResolution(minecraft);
        if (x < 0) x = Math.max(0, resolution.getScaledWidth() - width - 12);
        x = clamp(x, 0, Math.max(0, resolution.getScaledWidth() - width));
        y = clamp(y, 0, Math.max(0, resolution.getScaledHeight() - height));

        Gui.drawRect(x, y, x + width, y + height, 0xE8101010);
        Gui.drawRect(x, y, x + width, y + HEADER_HEIGHT, 0xF0202020);
        Gui.drawRect(x, y, x + width, y + 2, 0xFFFFC83D);
        Gui.drawRect(x, y + height - 1, x + width, y + height, 0xFF777777);
        Gui.drawRect(x, y, x + 1, y + height, 0xFF777777);
        Gui.drawRect(x + width - 1, y, x + width, y + height, 0xFF777777);
        minecraft.fontRendererObj.drawString("CAMERA PREVIEW  -  drag / resize",
                x + 5, y + 4, 0xFFFFC83D);

        int imageX = x + 3;
        int imageY = y + HEADER_HEIGHT + 3;
        int imageWidth = width - 6;
        int imageHeight = height - HEADER_HEIGHT - 6;

        drawTexture(imageX, imageY, imageWidth, imageHeight);
        Gui.drawRect(x + width - 11, y + height - 11, x + width - 3, y + height - 3, 0xFFFFC83D);
    }

    public synchronized void dispose() {
        if (textureId != -1) {
            GL11.glDeleteTextures(textureId);
            textureId = -1;
            textureWidth = 0;
            textureHeight = 0;
        }
    }

    private void ensureTexture(int requestedWidth, int requestedHeight) {
        if (textureId != -1 && textureWidth == requestedWidth && textureHeight == requestedHeight) return;
        dispose();
        GL11.glPushAttrib(GL11.GL_TEXTURE_BIT);
        try {
            textureId = GL11.glGenTextures();
            textureWidth = requestedWidth;
            textureHeight = requestedHeight;
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8,
                    textureWidth, textureHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (java.nio.ByteBuffer) null);
        } finally {
            GL11.glPopAttrib();
        }
    }

    private static boolean inside(int mouseX, int mouseY, int left, int top, int width, int height) {
        return mouseX >= left && mouseX <= left + width
                && mouseY >= top && mouseY <= top + height;
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
