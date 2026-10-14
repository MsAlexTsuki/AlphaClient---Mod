package com.alpharip.staffmod.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.input.Mouse;

import java.io.IOException;

public class UtilitiesGui extends ModernGuiScreen {

    private static final int BUTTON_BACK = 2;

    private final GuiScreen parentScreen;

    /*
     * =========================================================
     * LAYOUT
     * =========================================================
     */

    private static final int MAX_WIDTH = 570;

    private static final int SIDE_MARGIN = 12;

    private static final int TOP = 88;

    private static final int BOTTOM_OFFSET = 45;

    private static final int LIST_TOP_OFFSET = 48;

    private static final int LIST_PADDING = 8;

    private static final int ROW_HEIGHT = 30;

    private static final int ROW_GAP = 6;

    private static final int SCROLL_AMOUNT = 24;

    /*
     * =========================================================
     * SCROLL
     * =========================================================
     */

    private int scrollOffset = 0;

    /*
     * =========================================================
     * HOVER
     * =========================================================
     */

    private int hoveredUtility = -1;

    /*
     * =========================================================
     * CONSTRUCTOR
     * =========================================================
     */

    public UtilitiesGui(
            GuiScreen parentScreen) {

        this.parentScreen =
                parentScreen;
    }

    /*
     * =========================================================
     * INIT
     * =========================================================
     */

    @Override
    public void initGui() {

        super.initGui();

        scrollOffset = 0;

        hoveredUtility = -1;
    }

    /*
     * =========================================================
     * DRAW
     * =========================================================
     */

    @Override
    public void drawScreen(
            int mouseX,
            int mouseY,
            float partialTicks) {

        drawModernBackground();

        /*
         * Gebruik dezelfde geschaalde
         * muiscoördinaten als StaffModsGui.
         */

        int scaledMouseX =
                getScaledMouseX(mouseX);

        int scaledMouseY =
                getScaledMouseY(mouseY);

        /*
         * =====================================================
         * DYNAMISCHE BREEDTE
         * =====================================================
         */

        int availableWidth =
                this.width
                        - (SIDE_MARGIN * 2);

        int totalWidth =
                Math.min(
                        MAX_WIDTH,
                        availableWidth
                );

        if (totalWidth < 300) {

            totalWidth =
                    availableWidth;
        }

        int left =
                (this.width - totalWidth) / 2;

        int bottom =
                this.height
                        - BOTTOM_OFFSET;

        int panelHeight =
                bottom - TOP;

        /*
         * =====================================================
         * HEADER
         * =====================================================
         */

        drawModTitle(
                "UTILITIES",
                this.width / 2,
                18
        );

        drawCenteredString(
                this.fontRendererObj,
                "Select a utility",
                this.width / 2,
                67,
                COLOR_GOLD
        );

        /*
         * =====================================================
         * PANEL
         * =====================================================
         */

        drawTransparentPanel(
                left,
                TOP,
                totalWidth,
                panelHeight
        );

        /*
         * =====================================================
         * LIST TITLE
         * =====================================================
         */

        drawString(
                this.fontRendererObj,
                "Select a utility",
                left + 10,
                TOP + 12,
                COLOR_GOLD
        );

        /*
         * =====================================================
         * LIST AREA
         * =====================================================
         */

        int listTop =
                TOP
                        + LIST_TOP_OFFSET;

        int listBottom =
                bottom
                        - 50;

        /*
         * =====================================================
         * UTILITIES
         * =====================================================
         */

        drawUtilities(
                left,
                totalWidth,
                listTop,
                listBottom,
                scaledMouseX,
                scaledMouseY
        );

        /*
         * =====================================================
         * BACK
         * =====================================================
         */

        int actionY =
                bottom
                        - 38;

        drawActionButton(
                left,
                actionY,
                totalWidth,
                34,
                "BACK",
                scaledMouseX,
                scaledMouseY
        );

        /*
         * =====================================================
         * BUTTONS
         * =====================================================
         */

        super.drawScreen(
                mouseX,
                mouseY,
                partialTicks
        );
    }

    /*
     * =========================================================
     * UTILITIES
     * =========================================================
     */

    private String[] getUtilities() {

        return new String[] {
                "NAMETAGS",
                "NOFALL"
        };
    }

    /*
     * =========================================================
     * DRAW UTILITIES
     * =========================================================
     */

    private void drawUtilities(
            int left,
            int width,
            int listTop,
            int listBottom,
            int mouseX,
            int mouseY) {

        hoveredUtility = -1;

        int innerLeft =
                left + LIST_PADDING;

        int innerWidth =
                width
                        - (LIST_PADDING * 2);

        drawRect(
                innerLeft - 1,
                listTop - 1,
                innerLeft + innerWidth + 1,
                listBottom + 1,
                0x24101010
        );

        drawRect(
                innerLeft - 1,
                listTop - 1,
                innerLeft + innerWidth + 1,
                listTop,
                0x35E8E8E8
        );

        drawRect(
                innerLeft - 1,
                listBottom,
                innerLeft + innerWidth + 1,
                listBottom + 1,
                0x35E8E8E8
        );

        String[] utilities =
                getUtilities();

        for (int i = 0;
             i < utilities.length;
             i++) {

            String utility =
                    utilities[i];

            if (utility == null) {
                continue;
            }

            int y =
                    listTop
                            + i
                            * (
                            ROW_HEIGHT
                                    + ROW_GAP
                    )
                            - scrollOffset;

            /*
             * Buiten de zichtbare lijst.
             */

            if (y < listTop) {
                continue;
            }

            if (y + ROW_HEIGHT > listBottom) {
                continue;
            }

            boolean hovered =
                    isInside(
                            mouseX,
                            mouseY,
                            innerLeft,
                            y,
                            innerWidth,
                            ROW_HEIGHT
                    );

            if (hovered) {

                hoveredUtility = i;
            }

            drawListRow(
                    innerLeft,
                    y,
                    innerWidth,
                    ROW_HEIGHT,
                    utility,
                    hovered
            );
        }
    }

    /*
     * =========================================================
     * LIST ROW
     * =========================================================
     */

    private void drawListRow(
            int x,
            int y,
            int width,
            int height,
            String text,
            boolean hovered) {

        int background =
                hovered
                        ? 0x55202020
                        : 0x38101010;

        int outline =
                hovered
                        ? 0x58E8E8E8
                        : 0x30E8E8E8;

        /*
         * Achtergrond.
         */

        drawRect(
                x,
                y,
                x + width,
                y + height,
                background
        );

        /*
         * Bovenrand.
         */

        drawRect(
                x,
                y,
                x + width,
                y + 1,
                outline
        );

        /*
         * Onderrand.
         */

        drawRect(
                x,
                y + height - 1,
                x + width,
                y + height,
                outline
        );

        /*
         * Linkerrand.
         */

        drawRect(
                x,
                y,
                x + 1,
                y + height,
                outline
        );

        /*
         * Rechterrand.
         */

        drawRect(
                x + width - 1,
                y,
                x + width,
                y + height,
                outline
        );

        /*
         * Tekst.
         */

        drawString(
                this.fontRendererObj,
                text,
                x + 10,
                y + 10,
                COLOR_WHITE
        );
    }

    /*
     * =========================================================
     * TRANSPARENT PANEL
     * =========================================================
     */

    private void drawTransparentPanel(
            int x,
            int y,
            int width,
            int height) {

        /*
         * Transparante achtergrond.
         */

        drawRect(
                x,
                y,
                x + width,
                y + height,
                0x45101010
        );

        /*
         * Bovenrand.
         */

        drawRect(
                x,
                y,
                x + width,
                y + 1,
                0x30E8E8E8
        );

        /*
         * Onderrand.
         */

        drawRect(
                x,
                y + height - 1,
                x + width,
                y + height,
                0x30E8E8E8
        );

        /*
         * Linkerrand.
         */

        drawRect(
                x,
                y,
                x + 1,
                y + height,
                0x30E8E8E8
        );

        /*
         * Rechterrand.
         */

        drawRect(
                x + width - 1,
                y,
                x + width,
                y + height,
                0x30E8E8E8
        );
    }

    /*
     * =========================================================
     * ACTION BUTTON
     * =========================================================
     */

    private void drawActionButton(
            int x,
            int y,
            int width,
            int height,
            String text,
            int mouseX,
            int mouseY) {

        boolean hovered =
                isInside(
                        mouseX,
                        mouseY,
                        x,
                        y,
                        width,
                        height
                );

        int background =
                hovered
                        ? 0x55202020
                        : 0x38101010;

        int outline =
                hovered
                        ? 0x60E8E8E8
                        : 0x38E8E8E8;

        /*
         * Achtergrond.
         */

        drawRect(
                x,
                y,
                x + width,
                y + height,
                background
        );

        /*
         * Bovenrand.
         */

        drawRect(
                x,
                y,
                x + width,
                y + 1,
                outline
        );

        /*
         * Onderrand.
         */

        drawRect(
                x,
                y + height - 1,
                x + width,
                y + height,
                outline
        );

        /*
         * Linkerrand.
         */

        drawRect(
                x,
                y,
                x + 1,
                y + height,
                outline
        );

        /*
         * Rechterrand.
         */

        drawRect(
                x + width - 1,
                y,
                x + width,
                y + height,
                outline
        );

        /*
         * Tekst.
         */

        drawCenteredString(
                this.fontRendererObj,
                text,
                x + width / 2,
                y + 10,
                COLOR_WHITE
        );
    }

    /*
     * =========================================================
     * MOUSE CLICK
     * =========================================================
     */

    @Override
    protected void mouseClicked(
            int mouseX,
            int mouseY,
            int mouseButton)
            throws IOException {

        int scaledMouseX =
                getScaledMouseX(mouseX);

        int scaledMouseY =
                getScaledMouseY(mouseY);

        /*
         * =====================================================
         * LAYOUT
         * =====================================================
         */

        int availableWidth =
                this.width
                        - (SIDE_MARGIN * 2);

        int totalWidth =
                Math.min(
                        MAX_WIDTH,
                        availableWidth
                );

        if (totalWidth < 300) {

            totalWidth =
                    availableWidth;
        }

        int left =
                (this.width - totalWidth) / 2;

        int bottom =
                this.height
                        - BOTTOM_OFFSET;

        /*
         * =====================================================
         * BACK
         * =====================================================
         */

        int actionY =
                bottom
                        - 38;

        if (mouseButton == 0
                && isInside(
                scaledMouseX,
                scaledMouseY,
                left,
                actionY,
                totalWidth,
                34
        )) {

            Minecraft.getMinecraft()
                    .displayGuiScreen(
                            this.parentScreen
                    );

            return;
        }

        /*
         * =====================================================
         * UTILITY CLICK
         * =====================================================
         */

        if (mouseButton == 0) {

            int listTop =
                    TOP
                            + LIST_TOP_OFFSET;

            int listBottom =
                    bottom
                            - 50;

            int innerLeft =
                    left + LIST_PADDING;

            int innerWidth =
                    totalWidth
                            - (LIST_PADDING * 2);

            String[] utilities =
                    getUtilities();

            for (int i = 0;
                 i < utilities.length;
                 i++) {

                int y =
                        listTop
                                + i
                                * (
                                ROW_HEIGHT
                                        + ROW_GAP
                        )
                                - scrollOffset;

                /*
                 * Alleen zichtbare rijen.
                 */

                if (y < listTop) {
                    continue;
                }

                if (y + ROW_HEIGHT > listBottom) {
                    continue;
                }

                if (!isInside(
                        scaledMouseX,
                        scaledMouseY,
                        innerLeft,
                        y,
                        innerWidth,
                        ROW_HEIGHT
                )) {

                    continue;
                }

                openUtility(
                        utilities[i]
                );

                return;
            }
        }

        super.mouseClicked(
                mouseX,
                mouseY,
                mouseButton
        );
    }

    /*
     * =========================================================
     * OPEN UTILITY
     * =========================================================
     */

    private void openUtility(
            String utility) {

        if ("NAMETAGS".equals(utility)) {

            Minecraft.getMinecraft()
                    .displayGuiScreen(
                            new RankSettingsGui(
                                    this
                            )
                    );

            return;
        }

        if ("NOFALL".equals(utility)) {

            Minecraft.getMinecraft()
                    .displayGuiScreen(
                            new NoFallSettingsGui(
                                    this
                            )
                    );
        }
    }

    /*
     * =========================================================
     * MOUSE WHEEL
     * =========================================================
     */

    @Override
    public void handleMouseInput()
            throws IOException {

        int wheel =
                Mouse.getEventDWheel();

        if (wheel != 0) {

            int mouseX =
                    Mouse.getEventX()
                            * this.width
                            / Minecraft.getMinecraft().displayWidth;

            int mouseY =
                    this.height
                            - Mouse.getEventY()
                            * this.height
                            / Minecraft.getMinecraft().displayHeight
                            - 1;

            mouseX =
                    getScaledMouseX(
                            mouseX
                    );

            mouseY =
                    getScaledMouseY(
                            mouseY
                    );

            handleScroll(
                    mouseX,
                    mouseY,
                    wheel
            );
        }

        super.handleMouseInput();
    }

    private void handleScroll(
            int mouseX,
            int mouseY,
            int wheel) {

        int availableWidth =
                this.width
                        - (SIDE_MARGIN * 2);

        int totalWidth =
                Math.min(
                        MAX_WIDTH,
                        availableWidth
                );

        if (totalWidth < 300) {

            totalWidth =
                    availableWidth;
        }

        int left =
                (this.width - totalWidth) / 2;

        int bottom =
                this.height
                        - BOTTOM_OFFSET;

        int listTop =
                TOP
                        + LIST_TOP_OFFSET;

        int listBottom =
                bottom
                        - 50;

        /*
         * Alleen scrollen wanneer de
         * muis boven de utility-lijst staat.
         */

        if (!isInside(
                mouseX,
                mouseY,
                left,
                listTop,
                totalWidth,
                listBottom - listTop
        )) {

            return;
        }

        /*
         * Scrollrichting.
         */

        if (wheel < 0) {

            scrollOffset +=
                    SCROLL_AMOUNT;

        } else {

            scrollOffset -=
                    SCROLL_AMOUNT;
        }

        clampScroll();
    }

    /*
     * =========================================================
     * SCROLL CLAMP
     * =========================================================
     */

    private void clampScroll() {

        String[] utilities =
                getUtilities();

        int utilityCount =
                utilities == null
                        ? 0
                        : utilities.length;

        int bottom =
                this.height
                        - BOTTOM_OFFSET;

        int listTop =
                TOP
                        + LIST_TOP_OFFSET;

        int listBottom =
                bottom
                        - 50;

        int listHeight =
                listBottom
                        - listTop;

        int contentHeight;

        if (utilityCount <= 0) {

            contentHeight = 0;

        } else {

            contentHeight =
                    utilityCount
                            * ROW_HEIGHT
                            + (
                            utilityCount - 1
                    )
                            * ROW_GAP;
        }

        int maxScroll =
                contentHeight
                        - listHeight;

        if (maxScroll < 0) {

            maxScroll = 0;
        }

        if (scrollOffset < 0) {

            scrollOffset = 0;
        }

        if (scrollOffset > maxScroll) {

            scrollOffset = maxScroll;
        }
    }

    /*
     * =========================================================
     * HIT TEST
     * =========================================================
     */

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

    /*
     * =========================================================
     * ESC
     * =========================================================
     */

    @Override
    protected void keyTyped(
            char typedChar,
            int keyCode)
            throws IOException {

        if (keyCode == 1) {

            this.mc.displayGuiScreen(null);

            this.mc.setIngameFocus();

            return;
        }

        super.keyTyped(
                typedChar,
                keyCode
        );
    }

    /*
     * =========================================================
     * PAUSE
     * =========================================================
     */

    @Override
    public boolean doesGuiPauseGame() {

        return false;
    }
}