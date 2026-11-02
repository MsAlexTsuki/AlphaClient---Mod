package com.alpharip.staffmod.Minimap;

import com.alpharip.staffmod.gui.AlphaGuiScreen;
import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;
import org.lwjgl.input.Mouse;

/** Large view of the live AlphaClient minimap. */
public final class FullscreenMinimapGui extends AlphaGuiScreen {
    private final GuiScreen parent;
    private boolean dragging;
    private int lastMouseX, lastMouseY;
    private double panRemainderX, panRemainderZ;

    public FullscreenMinimapGui(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0xE6080A0F);
        drawRect(0, 0, width, 2, 0xFFFFC83D);
        drawCenteredString(fontRendererObj, "ALPHA WORLD MAP", width / 2, 12, 0xFFFFD866);
        AlphaMinimap.getInstance().drawFullscreenMap(width, height);
        drawCenteredString(fontRendererObj, "Scroll to zoom  |  Drag to pan  |  ESC to close", width / 2, height - 18, 0xFFB8BEC8);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private int mapSize() {
        return Math.max(112, Math.min(320, Math.min(width - 48, height - 92)));
    }

    private boolean insideMap(int x, int y) {
        int size = mapSize();
        int left = (width - size) / 2;
        int top = (height - size) / 2 + 8;
        return x >= left && x <= left + size && y >= top && y <= top + size;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == 0 && insideMap(mouseX, mouseY)) {
            dragging = true;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            panRemainderX = 0.0D;
            panRemainderZ = 0.0D;
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        if (!dragging || clickedMouseButton != 0) return;
        int size = mapSize();
        double radius = Math.max(1.0D, size / 2.0D - 5.0D);
        int range = MinimapSettings.fullscreenRange();
        int deltaX = mouseX - lastMouseX;
        int deltaY = mouseY - lastMouseY;
        double screenWorldX = -deltaX * range / radius;
        double screenWorldZ = -deltaY * range / radius;
        double angle = Math.toRadians(180.0D - mc.thePlayer.rotationYaw);
        double cos = Math.cos(angle), sin = Math.sin(angle);
        panRemainderX += screenWorldX * cos + screenWorldZ * sin;
        panRemainderZ += -screenWorldX * sin + screenWorldZ * cos;
        int wholeX = (int) panRemainderX;
        int wholeZ = (int) panRemainderZ;
        if (wholeX != 0 || wholeZ != 0) {
            MinimapSettings.setPan(MinimapSettings.panX() + wholeX, MinimapSettings.panZ() + wholeZ);
            panRemainderX -= wholeX;
            panRemainderZ -= wholeZ;
        }
        lastMouseX = mouseX;
        lastMouseY = mouseY;
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        if (state == 0) dragging = false;
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel == 0) return;
        int mouseX = Mouse.getEventX() * width / mc.displayWidth;
        int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;
        if (!insideMap(mouseX, mouseY)) return;
        int current = MinimapSettings.fullscreenRange();
        MinimapSettings.setFullscreenRange(wheel > 0
                ? Math.max(16, (int)Math.round(current * 0.85D))
                : Math.min(512, (int)Math.round(current / 0.85D)));
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            mc.displayGuiScreen(parent);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
