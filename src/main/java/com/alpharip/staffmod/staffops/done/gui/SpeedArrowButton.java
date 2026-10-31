package com.alpharip.staffmod.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

/**
 * Alpha's Staffing Mods
 *
 * Author: MsAlexTsuki
 */
public class SpeedArrowButton extends GuiButton {

    private boolean hoveredCustom;


    public SpeedArrowButton(
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
                text
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


        /*
         * =====================================================
         * HOVER
         * =====================================================
         */

        hoveredCustom =
                mouseX >= this.xPosition
                        && mouseY >= this.yPosition
                        && mouseX < this.xPosition + this.width
                        && mouseY < this.yPosition + this.height;


        /*
         * =====================================================
         * SUBTLE BACKGROUND
         * =====================================================
         *
         * Geen border.
         * Geen groene outline.
         *
         * De Minecraft wereld blijft zichtbaar
         * en de knop blendt met het paneel.
         */

        int backgroundColor =
                hoveredCustom
                        ? 0x55151515
                        : 0x33101010;


        drawRect(
                this.xPosition,
                this.yPosition,
                this.xPosition + this.width,
                this.yPosition + this.height,
                backgroundColor
        );


        /*
         * =====================================================
         * ARROW
         * =====================================================
         */

        int textColor =
                hoveredCustom
                        ? 0xFFFFFFFF
                        : 0xFFCCCCCC;


        drawCenteredString(
                mc.fontRendererObj,
                this.displayString,
                this.xPosition + this.width / 2,
                this.yPosition
                        + (this.height - 8) / 2,
                textColor
        );
    }
}