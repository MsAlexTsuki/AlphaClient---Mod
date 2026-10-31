package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.profile.ModuleProfileManager;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Alpha's Staffing Mods
 *
 * Mod color configuration.
 *
 * De kleuren worden opgeslagen in:
 *
 * config/alphastaffmod/bhop_profiles.json
 *
 * Per profiel.
 *
 * Author: MsAlexTsuki
 */
public class ModColorSettingsGui extends ModernGuiScreen {

    private static final int BUTTON_SAVE = 1;
    private static final int BUTTON_BACK = 2;

    private static final int ROW_HEIGHT = 38;
    private static final int ROW_GAP = 6;

    private static final int FIELD_WIDTH = 150;
    private static final int FIELD_HEIGHT = 22;

    private static final int PREVIEW_SIZE = 22;

    /*
     * =========================
     * LAYOUT
     * =========================
     *
     * Exact dezelfde GUI-afmetingen
     * als de HUD Editor.
     */
    private static final int ROW_START_OFFSET = 62;

    private static final int SCROLL_AMOUNT = 24;

    private int scrollOffset = 0;

    private static final int COLOR_HEADER_OFFSET = 48;

    private static final int BUTTON_WIDTH = 160;
    private static final int BUTTON_HEIGHT = 32;
    private static final int BUTTON_GAP = 20;

    private static final String DEFAULT_COLOR =
            "#F2EE4D";

    private final GuiScreen parentScreen;

    private final List<String> modNames =
            new ArrayList<String>();

    private final List<GuiTextField> colorFields =
            new ArrayList<GuiTextField>();


    /*
     * =========================================================
     * CONSTRUCTOR
     * =========================================================
     */

    public ModColorSettingsGui(
            GuiScreen parentScreen) {

        this.parentScreen =
                parentScreen;

        ModuleProfileManager.load();

        /*
         * Bestaande standaardmods.
         */
        registerMod(
                "CPS",
                DEFAULT_COLOR
        );

        registerMod(
                "BHOP_PROFILE_NAME",
                DEFAULT_COLOR
        );

        registerMod(
                "BHOP_SPEED",
                DEFAULT_COLOR
        );

        registerMod(
                "KNOCKBACK",
                DEFAULT_COLOR
        );

        rebuildModList();
    }


    /*
     * =========================================================
     * REGISTER MOD
     * =========================================================
     */

    public static void registerMod(
            String modName,
            String defaultColor) {

        if (modName == null
                || modName.trim().isEmpty()) {

            return;
        }

        ModuleProfileManager.ensureSelectedModColor(
                modName,
                defaultColor
        );
    }


    /*
     * =========================================================
     * GET COLOR
     * =========================================================
     */

    public static String getColor(
            String modName) {

        return ModuleProfileManager.getSelectedModColor(
                modName,
                DEFAULT_COLOR
        );
    }


    /*
     * =========================================================
     * GET COLOR AS RGB INT
     * =========================================================
     */

    public static int getColorInt(
            String modName) {

        String color =
                getColor(
                        modName
                );

        if (color.startsWith("#")) {

            color =
                    color.substring(1);
        }

        try {

            return Integer.parseInt(
                    color,
                    16
            );

        } catch (Exception e) {

            return Integer.parseInt(
                    DEFAULT_COLOR.substring(1),
                    16
            );
        }
    }


    /*
     * =========================================================
     * MOD LIST
     * =========================================================
     */

    private void rebuildModList() {

        modNames.clear();

        /*
         * Vaste volgorde.
         */

        addModToList(
                "CPS"
        );

        addModToList(
                "BHOP_PROFILE_NAME"
        );

        addModToList(
                "BHOP_SPEED"
        );

        addModToList(
                "KNOCKBACK"
        );

        /*
         * Eventuele toekomstige mods
         * uit het actieve profiel.
         */

        JsonObject profileColors =
                ModuleProfileManager
                        .getSelectedModColors();

        for (Map.Entry<String, JsonElement> entry :
                profileColors.entrySet()) {

            String name =
                    entry.getKey();

            if (name == null
                    || name.trim().isEmpty()) {

                continue;
            }

            if ("CPS".equalsIgnoreCase(name)) {
                continue;
            }

            if ("BHOP".equalsIgnoreCase(name)) {
                continue;
            }

            if ("BHOP_PROFILE_NAME"
                    .equalsIgnoreCase(name)) {

                continue;
            }

            if ("BHOP_SPEED"
                    .equalsIgnoreCase(name)) {

                continue;
            }

            if ("KNOCKBACK"
                    .equalsIgnoreCase(name)) {

                continue;
            }

            addModToList(
                    name
            );
        }
    }


    /*
     * =========================================================
     * ADD MOD TO LIST
     * =========================================================
     */

    private void addModToList(
            String name) {

        if (name == null
                || name.trim().isEmpty()) {

            return;
        }

        if (!modNames.contains(name)) {

            modNames.add(
                    name
            );
        }
    }


    /*
     * =========================================================
     * DISPLAY NAME
     * =========================================================
     */

    private String getDisplayName(
            String modName) {

        if ("BHOP_PROFILE_NAME".equals(
                modName)) {

            return "BHOP PROFILE NAME";
        }

        if ("BHOP_SPEED".equals(
                modName)) {

            return "BHOP SPEED";
        }

        return modName;
    }


    /*
     * =========================================================
     * FIELD X
     * =========================================================
     */

    private int getFieldX() {

        int groupWidth =
                PREVIEW_SIZE
                        + 8
                        + FIELD_WIDTH;

        int groupLeft =
                getGuiCenter()
                        + 100
                        - groupWidth / 2;

        return groupLeft
                + PREVIEW_SIZE
                + 8;
    }


    /*
     * =========================================================
     * COLOR AREA CENTER
     * =========================================================
     */

    private int getColorAreaCenter() {

        return getGuiCenter()
                + 100;
    }


    /*
     * =========================================================
     * ROW Y
     * =========================================================
     */

    private int getRowY(
            int index) {

        return getGuiTop()
                + ROW_START_OFFSET
                + index
                * (
                ROW_HEIGHT
                        + ROW_GAP
        )
                - scrollOffset;
    }


    /*
     * =========================================================
     * SCROLL LIST BOUNDS
     * =========================================================
     */

    private int getListTop() {

        return getGuiTop()
                + ROW_START_OFFSET
                - 2;
    }


    private int getListBottom() {

        return getGuiBottom()
                - 82;
    }


    private boolean isRowVisible(
            int rowY) {

        return rowY + ROW_HEIGHT > getListTop()
                && rowY < getListBottom();
    }


    /*
     * =========================================================
     * CLEAR ALL FIELD FOCUS
     * =========================================================
     */

    private void clearAllFieldFocus() {

        for (GuiTextField field :
                colorFields) {

            if (field != null) {

                field.setFocused(
                        false
                );
            }
        }
    }


    /*
     * =========================================================
     * FOCUS ONE FIELD
     * =========================================================
     */

    private void focusField(
            GuiTextField selectedField) {

        for (GuiTextField field :
                colorFields) {

            if (field == null) {
                continue;
            }

            field.setFocused(
                    field == selectedField
            );
        }
    }


    /*
     * =========================================================
     * INIT GUI
     * =========================================================
     */

    @Override
    public void initGui() {

        this.buttonList.clear();

        colorFields.clear();

        rebuildModList();

        clampScroll();

        int centerX =
                getGuiCenter();

        int fieldX =
                getFieldX();

        /*
         * =========================
         * COLOR FIELDS
         * =========================
         */

        for (int i = 0;
             i < modNames.size();
             i++) {

            String modName =
                    modNames.get(i);

            int rowY =
                    getRowY(
                            i
                    );

            GuiTextField field =
                    new GuiTextField(
                            100 + i,
                            this.fontRendererObj,
                            fieldX,
                            rowY + 8,
                            FIELD_WIDTH,
                            FIELD_HEIGHT
                    );

            field.setMaxStringLength(
                    7
            );

            field.setEnableBackgroundDrawing(
                    false
            );

            field.setText(
                    getColor(
                            modName
                    )
            );

            field.setFocused(
                    false
            );

            colorFields.add(
                    field
            );
        }

        /*
         * =========================
         * SAVE
         * =========================
         *
         * Zelfde onderpositie als de
         * HUD Editor.
         */

        this.buttonList.add(
                new ModernButton(
                        BUTTON_SAVE,
                        centerX
                                - BUTTON_WIDTH
                                - BUTTON_GAP / 2,
                        getGuiBottom()
                                - 42,
                        BUTTON_WIDTH,
                        BUTTON_HEIGHT,
                        "SAVE"
                )
        );

        /*
         * =========================
         * BACK
         * =========================
         */

        this.buttonList.add(
                new ModernButton(
                        BUTTON_BACK,
                        centerX
                                + BUTTON_GAP / 2,
                        getGuiBottom()
                                - 42,
                        BUTTON_WIDTH,
                        BUTTON_HEIGHT,
                        "BACK"
                )
        );
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

        drawModernBackground();

        int centerX =
                getGuiCenter();

        int guiTop =
                getGuiTop();

        int guiBottom =
                getGuiBottom();

        /*
         * =========================
         * MAIN PANEL
         * =========================
         *
         * Exact dezelfde 520x300
         * afmetingen als de HUD Editor.
         */

        drawTransparentPanel(
                getGuiLeft(),
                guiTop,
                GUI_WIDTH,
                GUI_HEIGHT
        );

        /*
         * =========================
         * TITLE
         * =========================
         */

        drawModTitle(
                "MOD COLORS",
                centerX,
                guiTop + 18
        );

        /*
         * =========================
         * SUBTITLE
         * =========================
         */

        drawCenteredString(
                this.fontRendererObj,
                "Configure the accent color of each mod.",
                centerX,
                guiTop + 39,
                COLOR_GRAY
        );

        /*
         * =========================
         * COLOR CODE HEADER
         * =========================
         *
         * Bewust hoger gezet.
         */

        drawCenteredString(
                this.fontRendererObj,
                "COLOR CODE",
                getColorAreaCenter(),
                guiTop + COLOR_HEADER_OFFSET,
                COLOR_GOLD
        );

        /*
         * =========================
         * MOD ROWS
         * =========================
         */

        for (int i = 0;
             i < modNames.size();
             i++) {

            String modName =
                    modNames.get(i);

            int rowY =
                    getRowY(
                            i
                    );

            if (!isRowVisible(rowY)) {

                continue;
            }

            boolean hovered =
                    mouseX >= getGuiLeft() + 8
                            && mouseX <= getGuiRight() - 8
                            && mouseY >= rowY - 2
                            && mouseY <= rowY
                            + ROW_HEIGHT
                            - 2;

            int background =
                    hovered
                            ? 0x55202020
                            : 0x38101010;

            int outline =
                    hovered
                            ? 0x58E8E8E8
                            : 0x30E8E8E8;

            /*
             * Rij achtergrond.
             */

            drawRect(
                    getGuiLeft() + 8,
                    rowY - 2,
                    getGuiRight() - 8,
                    rowY
                            + ROW_HEIGHT
                            - 2,
                    background
            );

            /*
             * Outline boven.
             */

            drawRect(
                    getGuiLeft() + 8,
                    rowY - 2,
                    getGuiRight() - 8,
                    rowY - 1,
                    outline
            );

            /*
             * Outline onder.
             */

            drawRect(
                    getGuiLeft() + 8,
                    rowY
                            + ROW_HEIGHT
                            - 3,
                    getGuiRight() - 8,
                    rowY
                            + ROW_HEIGHT
                            - 2,
                    outline
            );

            /*
             * Outline links.
             */

            drawRect(
                    getGuiLeft() + 8,
                    rowY - 2,
                    getGuiLeft() + 9,
                    rowY
                            + ROW_HEIGHT
                            - 2,
                    outline
            );

            /*
             * Outline rechts.
             */

            drawRect(
                    getGuiRight() - 9,
                    rowY - 2,
                    getGuiRight() - 8,
                    rowY
                            + ROW_HEIGHT
                            - 2,
                    outline
            );

            /*
             * Modnaam.
             */

            drawString(
                    this.fontRendererObj,
                    getDisplayName(
                            modName
                    ),
                    getGuiLeft() + 18,
                    rowY + 10,
                    COLOR_WHITE
            );

            /*
             * =========================
             * COLOR GROUP
             * =========================
             */

            int groupWidth =
                    PREVIEW_SIZE
                            + 8
                            + FIELD_WIDTH;

            int groupLeft =
                    getColorAreaCenter()
                            - groupWidth / 2;

            int previewX =
                    groupLeft;

            int previewY =
                    rowY + 5;

            int fieldX =
                    previewX
                            + PREVIEW_SIZE
                            + 8;

            /*
             * =========================
             * COLOR PREVIEW
             * =========================
             */

            int colorInt =
                    getColorInt(
                            modName
                    );

            drawRect(
                    previewX,
                    previewY,
                    previewX
                            + PREVIEW_SIZE,
                    previewY
                            + PREVIEW_SIZE,
                    0xFF000000
                            | colorInt
            );

            /*
             * =========================
             * TEXT FIELD
             * =========================
             */

            if (i < colorFields.size()) {

                GuiTextField field =
                        colorFields.get(i);

                if (field != null) {

                    field.xPosition =
                            fieldX;

                    field.yPosition =
                            rowY + 8;

                    field.drawTextBox();
                }
            }
        }

        /*
         * =========================
         * HINT
         * =========================
         */

        drawCenteredString(
                this.fontRendererObj,
                "Use HEX format: #RRGGBB",
                centerX,
                guiBottom - 62,
                COLOR_GRAY
        );

        /*
         * =========================
         * BUTTONS
         * =========================
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
     */

    private void drawTransparentPanel(
            int x,
            int y,
            int width,
            int height) {

        drawRect(
                x,
                y,
                x + width,
                y + height,
                0x45101010
        );

        drawRect(
                x,
                y,
                x + width,
                y + 1,
                0x30E8E8E8
        );

        drawRect(
                x,
                y + height - 1,
                x + width,
                y + height,
                0x30E8E8E8
        );

        drawRect(
                x,
                y,
                x + 1,
                y + height,
                0x30E8E8E8
        );

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
     * KEYBOARD
     * =========================================================
     */

    @Override
    protected void keyTyped(
            char typedChar,
            int keyCode)
            throws IOException {

        if (keyCode ==
                Keyboard.KEY_ESCAPE) {

            clearAllFieldFocus();

            this.mc.displayGuiScreen(
                    parentScreen
            );

            return;
        }

        if (keyCode ==
                Keyboard.KEY_RETURN
                || keyCode ==
                Keyboard.KEY_NUMPADENTER) {

            saveColors();

            clearAllFieldFocus();

            return;
        }

        /*
         * Alleen het actieve veld ontvangt
         * toetsenbordinput.
         */

        for (GuiTextField field :
                colorFields) {

            if (field == null
                    || !field.isFocused()) {

                continue;
            }

            field.textboxKeyTyped(
                    typedChar,
                    keyCode
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
     * MOUSE
     * =========================================================
     */

    @Override
    protected void mouseClicked(
            int mouseX,
            int mouseY,
            int mouseButton)
            throws IOException {

        int guiMouseX =
                getScaledMouseX(
                        mouseX
                );

        int guiMouseY =
                getScaledMouseY(
                        mouseY
                );

        /*
         * =========================
         * COLOR ROWS
         * =========================
         */

        for (int i = 0;
             i < colorFields.size();
             i++) {

            GuiTextField field =
                    colorFields.get(i);

            if (field == null) {
                continue;
            }

            int rowY =
                    getRowY(
                            i
                    );

            if (!isRowVisible(rowY)) {

                continue;
            }

            int groupWidth =
                    PREVIEW_SIZE
                            + 8
                            + FIELD_WIDTH;

            int groupLeft =
                    getColorAreaCenter()
                            - groupWidth / 2;

            field.xPosition =
                    groupLeft
                            + PREVIEW_SIZE
                            + 8;

            field.yPosition =
                    rowY + 8;

            boolean insideRow =
                    guiMouseX >= getGuiLeft() + 8
                            && guiMouseX <= getGuiRight() - 8
                            && guiMouseY >= rowY - 2
                            && guiMouseY <= rowY
                            + ROW_HEIGHT
                            - 2;

            if (!insideRow) {
                continue;
            }

            /*
             * Eén actief veld tegelijk.
             */

            if (mouseButton == 0) {

                focusField(
                        field
                );
            }

            field.mouseClicked(
                    guiMouseX,
                    guiMouseY,
                    mouseButton
            );

            /*
             * mouseClicked() kan intern
             * opnieuw focus aanpassen.
             *
             * Daarom zetten we achteraf
             * opnieuw exact één veld actief.
             */

            if (mouseButton == 0) {

                focusField(
                        field
                );

                int fieldX =
                        field.xPosition;

                int fieldY =
                        field.yPosition;

                boolean insideField =
                        guiMouseX >= fieldX
                                && guiMouseX <=
                                fieldX + FIELD_WIDTH
                                && guiMouseY >= fieldY
                                && guiMouseY <=
                                fieldY + FIELD_HEIGHT;

                if (!insideField) {

                    field.setCursorPositionEnd();
                }
            }

            return;
        }

        /*
         * Klik buiten een kleurregel:
         * alle velden verliezen focus.
         */

        if (mouseButton == 0) {

            clearAllFieldFocus();
        }

        super.mouseClicked(
                mouseX,
                mouseY,
                mouseButton
        );
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
                    getScaledMouseX(
                            Mouse.getEventX()
                    );

            int mouseY =
                    getScaledMouseY(
                            Mouse.getEventY()
                    );

            handleScroll(
                    mouseX,
                    mouseY,
                    wheel
            );
        }

        super.handleMouseInput();
    }


    /*
     * =========================================================
     * SCROLL
     * =========================================================
     */

    private void handleScroll(
            int mouseX,
            int mouseY,
            int wheel) {

        if (!isInside(
                mouseX,
                mouseY,
                getGuiLeft() + 8,
                getListTop(),
                GUI_WIDTH - 16,
                getListBottom() - getListTop()
        )) {

            return;
        }

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

        int listHeight =
                getListBottom()
                        - getListTop();

        int contentHeight =
                modNames.size()
                        * ROW_HEIGHT
                        + Math.max(
                        0,
                        modNames.size() - 1
                ) * ROW_GAP;

        int maxScroll =
                contentHeight
                        - listHeight
                        + 4;

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
     * UPDATE
     * =========================================================
     */

    @Override
    public void updateScreen() {

        /*
         * Alleen het actieve veld
         * laat zijn cursor knipperen.
         */

        for (GuiTextField field :
                colorFields) {

            if (field == null
                    || !field.isFocused()) {

                continue;
            }

            field.updateCursorCounter();
        }

        super.updateScreen();
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

        if (button.id ==
                BUTTON_SAVE) {

            saveColors();

            clearAllFieldFocus();

            return;
        }

        if (button.id ==
                BUTTON_BACK) {

            clearAllFieldFocus();

            this.mc.displayGuiScreen(
                    parentScreen
            );
        }
    }


    /*
     * =========================================================
     * SAVE COLORS FROM FIELDS
     * =========================================================
     */

    private void saveColors() {

        Map<String, String> updatedColors =
                new LinkedHashMap<String, String>();

        for (int i = 0;
             i < modNames.size();
             i++) {

            if (i >= colorFields.size()) {

                continue;
            }

            String modName =
                    modNames.get(i);

            GuiTextField field =
                    colorFields.get(i);

            if (field == null) {

                continue;
            }

            String value =
                    normalizeColor(
                            field.getText()
                    );

            if (value == null) {

                /*
                 * Ongeldige kleur:
                 * huidige profielkleur behouden.
                 */

                field.setText(
                        getColor(
                                modName
                        )
                );

                continue;
            }

            updatedColors.put(
                    modName,
                    value
            );

            field.setText(
                    value
            );
        }

        /*
         * Alles in één keer opslaan
         * in het geselecteerde profiel.
         */

        ModuleProfileManager.setSelectedModColors(
                updatedColors
        );

        System.out.println(
                "[AlphaStaffMod] Profile mod colors updated: "
                        + ModuleProfileManager
                        .getSelectedProfileName()
        );
    }


    /*
     * =========================================================
     * NORMALIZE COLOR
     * =========================================================
     */

    private String normalizeColor(
            String value) {

        if (value == null) {

            return null;
        }

        String color =
                value.trim()
                        .toUpperCase();

        if (color.isEmpty()) {

            return null;
        }

        if (!color.startsWith("#")) {

            color =
                    "#" + color;
        }

        if (color.length() != 7) {

            return null;
        }

        for (int i = 1;
             i < color.length();
             i++) {

            char c =
                    color.charAt(i);

            boolean valid =
                    (c >= '0'
                            && c <= '9')
                            || (c >= 'A'
                            && c <= 'F');

            if (!valid) {

                return null;
            }
        }

        return color;
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
     * NO GAME PAUSE
     * =========================================================
     */

    @Override
    public boolean doesGuiPauseGame() {

        return false;
    }
}