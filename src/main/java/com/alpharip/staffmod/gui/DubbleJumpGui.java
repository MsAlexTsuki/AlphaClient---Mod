package com.alpharip.staffmod.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import com.alpharip.staffmod.modules.LongJumpMod;

import java.io.IOException;

/**
 * Alpha's Staffing Mods
 *
 * Dubble Jump settings page.
 *
 * The existing LongJumpMod class is intentionally kept as the
 * internal implementation so existing imports remain compatible.
 */
public class DubbleJumpGui extends ModernGuiScreen {

    private final GuiScreen parentScreen;

    private static final int PANEL_WIDTH = 440;
    private static final int PANEL_HEIGHT = 230;

    private static final int BUTTON_WIDTH = 240;
    private static final int BUTTON_HEIGHT = 34;

    public DubbleJumpGui(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    @Override
    public void initGui() {
        super.initGui();
    }

    @Override
    public void drawScreen(
            int mouseX,
            int mouseY,
            float partialTicks) {

        drawModernBackground();

        int scaledMouseX = getScaledMouseX(mouseX);
        int scaledMouseY = getScaledMouseY(mouseY);

        int left = (this.width - PANEL_WIDTH) / 2;
        int top = 70;

        drawModTitle(
                "Dubble Jump",
                this.width / 2,
                18
        );

        drawCenteredString(
                this.fontRendererObj,
                "Dubble Jump settings",
                this.width / 2,
                48,
                0xFFF2EE4D
        );

        /*
         * Transparent Alpha-style panel.
         *
         * The Minecraft world remains visible behind it,
         * just like the StaffModsGui panels.
         */
        drawRect(
                left,
                top,
                left + PANEL_WIDTH,
                top + PANEL_HEIGHT,
                0x28101010
        );

        /*
         * Subtle white/grey outline.
         */
        drawRect(
                left,
                top,
                left + PANEL_WIDTH,
                top + 1,
                0x38E8E8E8
        );

        drawRect(
                left,
                top + PANEL_HEIGHT - 1,
                left + PANEL_WIDTH,
                top + PANEL_HEIGHT,
                0x38E8E8E8
        );

        drawRect(
                left,
                top,
                left + 1,
                top + PANEL_HEIGHT,
                0x38E8E8E8
        );

        drawRect(
                left + PANEL_WIDTH - 1,
                top,
                left + PANEL_WIDTH,
                top + PANEL_HEIGHT,
                0x38E8E8E8
        );

        /*
         * Description.
         */
        drawCenteredString(
                this.fontRendererObj,
                "You can press your spacebar 5x per jump. Landing resets.",
                this.width / 2,
                top + 48,
                0xFFFFFFFF
        );

        LongJumpMod doubleJump =
                LongJumpMod.getInstance();

        /*
         * Dubble Jump toggle.
         */
        int toggleY = top + 92;

        drawButton(
                this.width / 2 - BUTTON_WIDTH / 2,
                toggleY,
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                doubleJump.isEnabled()
                        ? "DUBBLE JUMP: ON"
                        : "DUBBLE JUMP: OFF",
                scaledMouseX,
                scaledMouseY
        );

        /*
         * Back button.
         */
        int backY = top + 150;

        drawButton(
                this.width / 2 - BUTTON_WIDTH / 2,
                backY,
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                "BACK",
                scaledMouseX,
                scaledMouseY
        );

        super.drawScreen(
                mouseX,
                mouseY,
                partialTicks
        );
    }

    @Override
    protected void mouseClicked(
            int mouseX,
            int mouseY,
            int mouseButton) throws IOException {

        if (mouseButton != 0) {
            super.mouseClicked(mouseX, mouseY, mouseButton);
            return;
        }

        int scaledMouseX = getScaledMouseX(mouseX);
        int scaledMouseY = getScaledMouseY(mouseY);

        int buttonX =
                this.width / 2
                        - BUTTON_WIDTH / 2;

        /*
         * Dubble Jump toggle.
         */
        int toggleY = 70 + 92;

        if (isInside(
                scaledMouseX,
                scaledMouseY,
                buttonX,
                toggleY,
                BUTTON_WIDTH,
                BUTTON_HEIGHT)) {

            LongJumpMod.getInstance().toggle();
            return;
        }

        /*
         * Back button.
         */
        int backY = 70 + 150;

        if (isInside(
                scaledMouseX,
                scaledMouseY,
                buttonX,
                backY,
                BUTTON_WIDTH,
                BUTTON_HEIGHT)) {

            Minecraft.getMinecraft().displayGuiScreen(
                    parentScreen
            );
            return;
        }

        super.mouseClicked(
                mouseX,
                mouseY,
                mouseButton
        );
    }

    private void drawButton(
            int x,
            int y,
            int width,
            int height,
            String text,
            int mouseX,
            int mouseY) {

        boolean hovered = isInside(
                mouseX,
                mouseY,
                x,
                y,
                width,
                height
        );

        int background = hovered
                ? 0x55202020
                : 0x38101010;

        int outline = hovered
                ? 0x60E8E8E8
                : 0x38E8E8E8;

        drawRect(
                x,
                y,
                x + width,
                y + height,
                background
        );

        drawRect(
                x,
                y,
                x + width,
                y + 1,
                outline
        );

        drawRect(
                x,
                y + height - 1,
                x + width,
                y + height,
                outline
        );

        drawRect(
                x,
                y,
                x + 1,
                y + height,
                outline
        );

        drawRect(
                x + width - 1,
                y,
                x + width,
                y + height,
                outline
        );

        drawCenteredString(
                this.fontRendererObj,
                text,
                x + width / 2,
                y + 10,
                0xFFFFFFFF
        );
    }

    private boolean isInside(
            int mouseX,
            int mouseY,
            int x,
            int y,
            int width,
            int height) {

        return mouseX >= x
                && mouseX <= x + width
                && mouseY >= y
                && mouseY <= y + height;
    }
}