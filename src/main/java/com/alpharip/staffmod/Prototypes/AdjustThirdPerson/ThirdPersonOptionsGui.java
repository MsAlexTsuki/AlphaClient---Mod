package com.alpharip.staffmod.AdjustThirdPerson;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/**
 * Standalone Options -> Third Person prototype screen.
 * A future Options-menu hook can open this screen; this prototype does not install that hook.
 */
public final class ThirdPersonOptionsGui extends GuiScreen {
    private static final int DONE_BUTTON = 1;
    private static final int SLIDER_LEFT = 1;
    private static final int SLIDER_RIGHT = 2;

    private final GuiScreen parent;
    private int draggingSlider;

    public ThirdPersonOptionsGui(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int panelBottom = height / 2 + 72;
        buttonList.add(new GuiButton(DONE_BUTTON, width / 2 - 100, panelBottom, 200, 20, "Done"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null || button.id != DONE_BUTTON) return;
        AdjustableThirdPersonCameraDistance.save();
        Minecraft.getMinecraft().displayGuiScreen(parent);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        int panelWidth = 340;
        int panelHeight = 190;
        int left = width / 2 - panelWidth / 2;
        int top = height / 2 - panelHeight / 2;
        int right = left + panelWidth;
        int bottom = top + panelHeight;

        drawRect(left, top, right, bottom, 0xE6101218);
        drawRect(left, top, right, top + 2, 0xFFFFD866);
        drawRect(left, bottom - 1, right, bottom, 0x66555A63);

        drawCenteredString(fontRendererObj, "THIRD PERSON OPTIONS", width / 2, top + 13, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "Adjust each camera independently.", width / 2, top + 30, 0xFFB8BEC8);

        drawSlider("Rear Camera Distance", AdjustableThirdPersonCameraDistance.getRearDistance(),
                left + 26, top + 67, right - 26, SLIDER_LEFT, mouseX, mouseY);
        drawSlider("Front Camera Distance", AdjustableThirdPersonCameraDistance.getFrontDistance(),
                left + 26, top + 119, right - 26, SLIDER_RIGHT, mouseX, mouseY);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawSlider(String label, float value, int left, int top, int right,
                            int sliderId, int mouseX, int mouseY) {
        drawString(fontRendererObj, label, left, top - 16, 0xFFFFFFFF);
        String valueText = String.format(java.util.Locale.ROOT, "%.1f", value);
        drawString(fontRendererObj, valueText, right - fontRendererObj.getStringWidth(valueText), top - 16, 0xFFFFD866);

        int trackTop = top + 5;
        int trackBottom = trackTop + 4;
        int knobX = sliderX(value, left, right);
        drawRect(left, trackTop, right, trackBottom, 0xFF454A54);
        drawRect(left, trackTop, knobX, trackBottom, 0xFFFFD866);
        drawRect(knobX - 4, trackTop - 4, knobX + 5, trackBottom + 4, 0xFFFFD866);

        if (mouseX >= left && mouseX <= right && mouseY >= trackTop - 8 && mouseY <= trackBottom + 8) {
            drawRect(knobX - 5, trackTop - 5, knobX + 6, trackBottom + 5, 0x88FFFFFF);
        }
    }

    private int sliderX(float value, int left, int right) {
        float progress = (value - AdjustableThirdPersonCameraDistance.MIN_DISTANCE)
                / (AdjustableThirdPersonCameraDistance.MAX_DISTANCE - AdjustableThirdPersonCameraDistance.MIN_DISTANCE);
        return left + Math.round((right - left) * progress);
    }

    private void updateSlider(int mouseX, int sliderId) {
        int left = width / 2 - 144;
        int right = width / 2 + 144;
        float progress = Math.max(0.0F, Math.min(1.0F, (mouseX - left) / (float) (right - left)));
        float value = AdjustableThirdPersonCameraDistance.MIN_DISTANCE
                + progress * (AdjustableThirdPersonCameraDistance.MAX_DISTANCE
                - AdjustableThirdPersonCameraDistance.MIN_DISTANCE);
        value = Math.round(value * 10.0F) / 10.0F;

        if (sliderId == SLIDER_LEFT) {
            AdjustableThirdPersonCameraDistance.setRearDistance(value);
        } else if (sliderId == SLIDER_RIGHT) {
            AdjustableThirdPersonCameraDistance.setFrontDistance(value);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0) {
            int left = width / 2 - 144;
            int right = width / 2 + 144;
            int rearY = height / 2 - 23;
            int frontY = height / 2 + 29;

            if (mouseX >= left - 5 && mouseX <= right + 5
                    && mouseY >= rearY - 8 && mouseY <= rearY + 14) {
                draggingSlider = SLIDER_LEFT;
                updateSlider(mouseX, draggingSlider);
                return;
            }
            if (mouseX >= left - 5 && mouseX <= right + 5
                    && mouseY >= frontY - 8 && mouseY <= frontY + 14) {
                draggingSlider = SLIDER_RIGHT;
                updateSlider(mouseX, draggingSlider);
                return;
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (draggingSlider != 0 && clickedMouseButton == 0) {
            updateSlider(mouseX, draggingSlider);
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (draggingSlider != 0) {
            draggingSlider = 0;
            AdjustableThirdPersonCameraDistance.save();
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            AdjustableThirdPersonCameraDistance.save();
            Minecraft.getMinecraft().displayGuiScreen(parent);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
