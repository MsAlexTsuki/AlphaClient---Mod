package com.alpharip.staffmod.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.input.Mouse;

import java.io.IOException;

public class EditGui extends ModernGuiScreen {

    /*
     * =========================================================
     * BUTTONS
     * =========================================================
     */

    private static final int BUTTON_RENAME_PROFILE = 1;
    private static final int BUTTON_RENAME_BHOP = 2;
    private static final int BUTTON_COLORS = 3;
    private static final int BUTTON_BACK = 4;


    /*
     * =========================================================
     * PARENT SCREEN
     * =========================================================
     */

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

    private static final int ROW_HEIGHT = 34;

    private static final int ROW_GAP = 8;

    private static final int SCROLL_AMOUNT = 24;


    /*
     * =========================================================
     * SCROLL
     * =========================================================
     */

    private int scrollOffset = 0;


    /*
     * =========================================================
     * CONSTRUCTOR
     * =========================================================
     */

    public EditGui(
            GuiScreen parentScreen) {

        this.parentScreen =
                parentScreen;
    }


    /*
     * =========================================================
     * INIT GUI
     * =========================================================
     */

    @Override
    public void initGui() {

        super.initGui();

        this.buttonList.clear();

        scrollOffset = 0;

        int centerX =
                this.width / 2;


        /*
         * =====================================================
         * RENAME PROFILE
         * =====================================================
         */

        this.buttonList.add(
                new ModernButton(
                        BUTTON_RENAME_PROFILE,
                        centerX - 150,
                        0,
                        300,
                        ROW_HEIGHT,
                        "RENAME PROFILE"
                )
        );


        /*
         * =====================================================
         * RENAME BHOP
         * =====================================================
         */

        this.buttonList.add(
                new ModernButton(
                        BUTTON_RENAME_BHOP,
                        centerX - 150,
                        0,
                        300,
                        ROW_HEIGHT,
                        "RENAME BHOP"
                )
        );


        /*
         * =====================================================
         * COLORS
         * =====================================================
         */

        this.buttonList.add(
                new ModernButton(
                        BUTTON_COLORS,
                        centerX - 150,
                        0,
                        300,
                        ROW_HEIGHT,
                        "COLORS"
                )
        );


        /*
         * =====================================================
         * BACK
         * =====================================================
         *
         * BACK blijft altijd onderaan staan.
         */

        this.buttonList.add(
                new ModernButton(
                        BUTTON_BACK,
                        centerX - 150,
                        0,
                        300,
                        34,
                        "BACK"
                )
        );

        updateButtonPositions();
    }


    /*
     * =========================================================
     * GET SETTINGS CONTENT HEIGHT
     * =========================================================
     */

    private int getSettingsContentHeight() {

        /*
         * Alle buttons behalve BACK.
         */

        int settingsCount =
                this.buttonList.size() - 1;

        if (settingsCount <= 0) {

            return 0;
        }

        return settingsCount
                * (ROW_HEIGHT + ROW_GAP)
                - ROW_GAP;
    }


    /*
     * =========================================================
     * GET LIST BOTTOM
     * =========================================================
     */

    private int getListBottom() {

        int bottom =
                this.height - BOTTOM_OFFSET;

        /*
         * Ruimte reserveren voor
         * de vaste BACK button.
         */

        return bottom - 48;
    }


    /*
     * =========================================================
     * GET MAX SCROLL
     * =========================================================
     */

    private int getMaxScroll() {

        int listTop =
                TOP + LIST_TOP_OFFSET;

        int listBottom =
                getListBottom();

        int listHeight =
                listBottom - listTop;

        int contentHeight =
                getSettingsContentHeight();

        int maxScroll =
                contentHeight - listHeight;

        if (maxScroll < 0) {

            maxScroll = 0;
        }

        return maxScroll;
    }


    /*
     * =========================================================
     * CLAMP SCROLL
     * =========================================================
     */

    private void clampScroll() {

        int maxScroll =
                getMaxScroll();

        if (scrollOffset < 0) {

            scrollOffset = 0;
        }

        if (scrollOffset > maxScroll) {

            scrollOffset =
                    maxScroll;
        }
    }


    /*
     * =========================================================
     * UPDATE BUTTON POSITIONS
     * =========================================================
     */

    private void updateButtonPositions() {

        int centerX =
                this.width / 2;

        int buttonX =
                centerX - 150;

        int listTop =
                TOP + LIST_TOP_OFFSET;

        int listBottom =
                getListBottom();


        /*
         * =====================================================
         * SCROLLABLE SETTINGS
         * =====================================================
         */

        for (int i = 0;
             i < this.buttonList.size() - 1;
             i++) {

            GuiButton button =
                    this.buttonList.get(i);

            int y =
                    listTop
                            + i
                            * (ROW_HEIGHT + ROW_GAP)
                            - scrollOffset;

            button.xPosition =
                    buttonX;

            button.yPosition =
                    y;

            /*
             * Alleen buttons binnen
             * het zichtbare lijstgebied.
             */

            button.visible =
                    y >= listTop
                            && y + ROW_HEIGHT <= listBottom;
        }


        /*
         * =====================================================
         * FIXED BACK BUTTON
         * =====================================================
         */

        GuiButton backButton =
                this.buttonList.get(
                        this.buttonList.size() - 1
                );

        int bottom =
                this.height - BOTTOM_OFFSET;

        int backY =
                bottom - 34;

        backButton.xPosition =
                buttonX;

        backButton.yPosition =
                backY;

        backButton.visible = true;
    }


    /*
     * =========================================================
     * BUTTON ACTIONS
     * =========================================================
     */

    @Override
    protected void actionPerformed(
            GuiButton button)
            throws IOException {

        if (button == null) {

            return;
        }


        /*
         * =====================================================
         * RENAME PROFILE
         * =====================================================
         */

        if (button.id
                == BUTTON_RENAME_PROFILE) {

            this.mc.displayGuiScreen(
                    new ProfileRenameGui(
                            this
                    )
            );

            return;
        }


        /*
         * =====================================================
         * RENAME BHOP
         * =====================================================
         *
         * Bestaande navigatie behouden.
         */

        if (button.id
                == BUTTON_RENAME_BHOP) {

            this.mc.displayGuiScreen(
                    new BhopRenameGui(
                            new BhopSettingsGui(
                                    this
                            ),
                            this
                    )
            );

            return;
        }


        /*
         * =====================================================
         * COLORS
         * =====================================================
         */

        if (button.id
                == BUTTON_COLORS) {

            this.mc.displayGuiScreen(
                    new ModColorSettingsGui(
                            this
                    )
            );

            return;
        }


        /*
         * =====================================================
         * BACK
         * =====================================================
         */

        if (button.id
                == BUTTON_BACK) {

            this.mc.displayGuiScreen(
                    parentScreen
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

        super.handleMouseInput();

        int wheel =
                Mouse.getEventDWheel();

        if (wheel == 0) {

            return;
        }

        int mouseX =
                getScaledMouseX(
                        Mouse.getEventX()
                );

        int mouseY =
                getScaledMouseY(
                        Mouse.getEventY()
                );

        int listTop =
                TOP + LIST_TOP_OFFSET;

        int listBottom =
                getListBottom();


        /*
         * Alleen scrollen binnen
         * de settingslijst.
         */

        if (mouseX < 0
                || mouseX > this.width
                || mouseY < listTop
                || mouseY > listBottom) {

            return;
        }


        /*
         * Naar beneden.
         */

        if (wheel < 0) {

            scrollOffset +=
                    SCROLL_AMOUNT;

        } else {

            /*
             * Naar boven.
             */

            scrollOffset -=
                    SCROLL_AMOUNT;
        }

        clampScroll();

        updateButtonPositions();
    }


    /*
     * =========================================================
     * DRAW SCREEN
     * =========================================================
     */

    @Override
    public void drawScreen(
            int mouseX,
            int mouseY,
            float partialTicks) {

        /*
         * =====================================================
         * MODERN BACKGROUND
         * =====================================================
         */

        drawModernBackground();


        /*
         * =====================================================
         * TITLE
         * =====================================================
         *
         * BELANGRIJK:
         *
         * drawModTitle() voegt zelf
         * "ALPHA'S" toe.
         *
         * Daarom geven we alleen
         * "EDIT" mee.
         */

        drawModTitle(
                "EDIT",
                this.width / 2,
                18
        );


        /*
         * =====================================================
         * SUBTITLE
         * =====================================================
         */

        drawCenteredString(
                this.fontRendererObj,
                "Edit profile and BHOP settings",
                this.width / 2,
                67,
                COLOR_GOLD
        );


        /*
         * =====================================================
         * RESPONSIVE WIDTH
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


        /*
         * =====================================================
         * PANEL
         * =====================================================
         */

        int bottom =
                this.height
                        - BOTTOM_OFFSET;

        int panelHeight =
                bottom - TOP;

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
                "EDIT SETTINGS",
                left + 10,
                TOP + 12,
                COLOR_GOLD
        );


        /*
         * =====================================================
         * UPDATE SCROLL
         * =====================================================
         */

        clampScroll();

        updateButtonPositions();


        /*
         * =====================================================
         * SCROLLBAR
         * =====================================================
         */

        drawScrollBar(
                left,
                totalWidth
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
     * TRANSPARENT PANEL
     * =========================================================
     *
     * Deze methode hoort lokaal bij EditGui.
     *
     * ModernGuiScreen bevat deze methode niet.
     */

    private void drawTransparentPanel(
            int x,
            int y,
            int width,
            int height) {

        /*
         * Transparante donkere achtergrond.
         */

        drawRect(
                x,
                y,
                x + width,
                y + height,
                0x45101010
        );


        /*
         * Bovenste outline.
         */

        drawRect(
                x,
                y,
                x + width,
                y + 1,
                0x30E8E8E8
        );


        /*
         * Onderste outline.
         */

        drawRect(
                x,
                y + height - 1,
                x + width,
                y + height,
                0x30E8E8E8
        );


        /*
         * Linker outline.
         */

        drawRect(
                x,
                y,
                x + 1,
                y + height,
                0x30E8E8E8
        );


        /*
         * Rechter outline.
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
     * SCROLL BAR
     * =========================================================
     */

    private void drawScrollBar(
            int panelLeft,
            int panelWidth) {

        int contentHeight =
                getSettingsContentHeight();

        int listTop =
                TOP + LIST_TOP_OFFSET;

        int listBottom =
                getListBottom();

        int listHeight =
                listBottom - listTop;


        /*
         * Geen scrollbar nodig als
         * alles zichtbaar is.
         */

        if (contentHeight <= listHeight) {

            return;
        }


        /*
         * Scrollbar X positie.
         */

        int barX =
                panelLeft
                        + panelWidth
                        - 7;

        int barTop =
                listTop;

        int barBottom =
                listBottom;

        int barHeight =
                barBottom - barTop;


        /*
         * =====================================================
         * BAR BACKGROUND
         * =====================================================
         */

        drawRect(
                barX,
                barTop,
                barX + 2,
                barBottom,
                0x55333333
        );


        /*
         * =====================================================
         * HANDLE HEIGHT
         * =====================================================
         */

        int handleHeight =
                (int)
                        (
                                (
                                        (float) listHeight
                                                / (float) contentHeight
                                )
                                        * barHeight
                        );

        if (handleHeight < 20) {

            handleHeight = 20;
        }


        /*
         * =====================================================
         * HANDLE POSITION
         * =====================================================
         */

        int maxScroll =
                getMaxScroll();

        int handleTravel =
                barHeight
                        - handleHeight;

        int handleY =
                barTop;

        if (maxScroll > 0) {

            handleY =
                    barTop
                            + (int)
                            (
                                    (
                                            (float) scrollOffset
                                                    / (float) maxScroll
                                    )
                                            * handleTravel
                            );
        }


        /*
         * =====================================================
         * HANDLE
         * =====================================================
         */

        drawRect(
                barX,
                handleY,
                barX + 2,
                handleY + handleHeight,
                0xFFAAAAAA
        );
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

        /*
         * ESC gaat terug naar
         * het vorige scherm.
         */

        if (keyCode == 1) {

            this.mc.displayGuiScreen(
                    parentScreen
            );

            return;
        }

        super.keyTyped(
                typedChar,
                keyCode
        );
    }


    /*
     * =========================================================
     * GAME DOES NOT PAUSE
     * =========================================================
     */

    @Override
    public boolean doesGuiPauseGame() {

        return false;
    }
}