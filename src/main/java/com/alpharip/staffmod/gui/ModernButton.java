// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public class ModernButton extends GuiButton {

    private static final int COLOR_ALPHA =
            0xFFFFD866;

    public ModernButton(
            int buttonId,
            int x,
            int y,
            int width,
            int height,
            String text) {

        super(
                buttonId,
                x,
                y,
                width,
                height,
                UiText.normalize(text)
        );
    }

    @Override
    public void drawButton(
            Minecraft mc,
            int mouseX,
            int mouseY) {

        if (!this.visible) {
            return;
        }

        boolean hovered =
                mouseX >= this.xPosition
                        && mouseY >= this.yPosition
                        && mouseX
                        < this.xPosition + this.width
                        && mouseY
                        < this.yPosition + this.height;

        /*
         * =========================
         * BUTTON BACKGROUND
         * =========================
         */

        int background;

        if (hovered) {

            background =
                    0xE02A2D35;

        } else {

            background =
                    0xC014161D;
        }

        drawRect(
                this.xPosition,
                this.yPosition,
                this.xPosition + this.width,
                this.yPosition + this.height,
                background
        );

        /*
         * =========================
         * BORDER
         * =========================
         */

        int border =
                hovered
                        ? COLOR_ALPHA
                        : 0x664B515D;

        /*
         * Top
         */
        drawRect(
                this.xPosition,
                this.yPosition,
                this.xPosition + this.width,
                this.yPosition + 1,
                border
        );

        /*
         * Bottom
         */
        drawRect(
                this.xPosition,
                this.yPosition + this.height - 1,
                this.xPosition + this.width,
                this.yPosition + this.height,
                border
        );

        /*
         * Left
         */
        drawRect(
                this.xPosition,
                this.yPosition,
                this.xPosition + 1,
                this.yPosition + this.height,
                border
        );

        /*
         * Right
         */
        drawRect(
                this.xPosition + this.width - 1,
                this.yPosition,
                this.xPosition + this.width,
                this.yPosition + this.height,
                border
        );

        /*
         * =========================
         * ALPHA BUTTON TEXT
         * =========================
         *
         * §l = bold
         *
         * De kleur wordt rechtstreeks
         * meegegeven zodat deze exact
         * overeenkomt met het ALPHA-logo.
         */

        String text =
                this.displayString;

        drawCenteredString(
                mc.fontRendererObj,
                text,
                this.xPosition
                        + this.width / 2,
                this.yPosition
                        + (this.height - 8) / 2,
                COLOR_ALPHA
        );
    }
}