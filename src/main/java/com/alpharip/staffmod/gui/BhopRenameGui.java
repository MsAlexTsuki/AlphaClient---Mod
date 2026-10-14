package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.movement.BhopMode;
import com.alpharip.staffmod.movement.BhopProfileManager;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

import org.lwjgl.input.Keyboard;

import java.io.IOException;

/**
 * Alpha's Staffing Mods
 *
 * Rename BHOP / HUD.
 */
public class BhopRenameGui extends ModernGuiScreen {

    private static final int BUTTON_SAVE = 1;
    private static final int BUTTON_RESET_NAME = 2;
    private static final int BUTTON_CANCEL = 3;

    private static final int BUTTON_RESET_YES = 10;
    private static final int BUTTON_RESET_NO = 11;

    private static final String DEFAULT_BHOP_NAME =
            "ALPHA'S BHOP";

    private final GuiScreen settingsScreen;
    private final GuiScreen editScreen;

    private GuiTextField bhopNameField;

    private boolean showResetConfirmation = false;

    public BhopRenameGui(
            GuiScreen settingsScreen,
            GuiScreen editScreen) {

        this.settingsScreen = settingsScreen;
        this.editScreen = editScreen;
    }

    @Override
    public void initGui() {

        this.buttonList.clear();

        int centerX = this.width / 2;

        String currentName = BhopMode.getBhopName();

        if (currentName == null
                || currentName.trim().isEmpty()
                || currentName.equalsIgnoreCase("BHOP")) {

            currentName = DEFAULT_BHOP_NAME;
        }

        this.bhopNameField = new GuiTextField(
                0,
                this.fontRendererObj,
                centerX - 120,
                145,
                240,
                24
        );

        this.bhopNameField.setMaxStringLength(32);
        this.bhopNameField.setText(currentName);
        this.bhopNameField.setFocused(true);
        this.bhopNameField.setCursorPositionEnd();

        this.buttonList.add(
                new ModernButton(
                        BUTTON_SAVE,
                        centerX - 120,
                        180,
                        240,
                        28,
                        "SAVE BHOP"
                )
        );

        this.buttonList.add(
                new ModernButton(
                        BUTTON_RESET_NAME,
                        centerX - 120,
                        215,
                        240,
                        28,
                        "RESET NAME"
                )
        );

        this.buttonList.add(
                new ModernButton(
                        BUTTON_CANCEL,
                        centerX - 120,
                        250,
                        240,
                        28,
                        "CANCEL"
                )
        );

        ResetConfirmationButton yesButton =
                new ResetConfirmationButton(
                        BUTTON_RESET_YES,
                        centerX - 95,
                        222,
                        85,
                        26,
                        "YES",
                        true
                );

        ResetConfirmationButton noButton =
                new ResetConfirmationButton(
                        BUTTON_RESET_NO,
                        centerX + 10,
                        222,
                        85,
                        26,
                        "NO",
                        false
                );

        yesButton.visible = false;
        noButton.visible = false;

        this.buttonList.add(yesButton);
        this.buttonList.add(noButton);

        Keyboard.enableRepeatEvents(true);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void updateScreen() {

        super.updateScreen();

        if (this.bhopNameField != null
                && !showResetConfirmation) {

            this.bhopNameField.updateCursorCounter();
        }
    }

    @Override
    protected void keyTyped(
            char typedChar,
            int keyCode)
            throws IOException {

        if (showResetConfirmation) {

            if (keyCode == Keyboard.KEY_ESCAPE) {
                closeResetConfirmation();
            }

            return;
        }

        if (keyCode == Keyboard.KEY_ESCAPE) {

            this.mc.displayGuiScreen(null);
            return;
        }

        if (keyCode == Keyboard.KEY_RETURN
                || keyCode == Keyboard.KEY_NUMPADENTER) {

            saveBhopName();
            return;
        }

        if (this.bhopNameField != null) {

            this.bhopNameField.textboxKeyTyped(
                    typedChar,
                    keyCode
            );
        }
    }

    @Override
    protected void mouseClicked(
            int mouseX,
            int mouseY,
            int mouseButton)
            throws IOException {

        /*
         * ModernGuiScreen gebruikt een eigen GUI-schaal.
         * De inkomende mouseX/mouseY zijn de ruwe Minecraft
         * GUI-coördinaten. Daarom zetten we ze hier één keer
         * om naar exact dezelfde coördinaten waarin de buttons
         * getekend zijn.
         */
        int scaledMouseX =
                getScaledMouseX(mouseX);

        int scaledMouseY =
                getScaledMouseY(mouseY);

        /*
         * Popup open:
         *
         * Laat alleen YES/NO door naar de normale button
         * verwerking. De onderliggende buttons mogen niet
         * worden aangeklikt.
         */
        if (showResetConfirmation) {

            super.mouseClicked(
                    mouseX,
                    mouseY,
                    mouseButton
            );

            return;
        }

        /*
         * Text field.
         */
        if (this.bhopNameField != null) {

            this.bhopNameField.mouseClicked(
                    scaledMouseX,
                    scaledMouseY,
                    mouseButton
            );
        }

        /*
         * =====================================================
         * FIX RESET NAME HITBOX
         * =====================================================
         *
         * We behandelen RESET NAME bewust handmatig.
         *
         * De ModernButton wordt nog steeds normaal getekend,
         * maar het klikken is niet langer afhankelijk van de
         * standaard GuiButton hit-test.
         *
         * Daardoor is de VOLLEDIGE knop klikbaar:
         *
         * x: centerX - 120 t/m centerX + 120
         * y: 215 t/m 243
         */
        int centerX = this.width / 2;

        int resetX = centerX - 120;
        int resetY = 215;
        int resetWidth = 240;
        int resetHeight = 28;

        if (scaledMouseX >= resetX
                && scaledMouseX < resetX + resetWidth
                && scaledMouseY >= resetY
                && scaledMouseY < resetY + resetHeight) {

            openResetConfirmation();

            return;
        }

        /*
         * SAVE en CANCEL blijven via ModernGuiScreen /
         * GuiButton lopen.
         */
        super.mouseClicked(
                mouseX,
                mouseY,
                mouseButton
        );
    }

    @Override
    protected void actionPerformed(
            GuiButton button)
            throws IOException {

        if (button == null) {
            return;
        }

        if (showResetConfirmation) {

            if (button.id == BUTTON_RESET_YES) {
                resetBhopName();
                return;
            }

            if (button.id == BUTTON_RESET_NO) {
                closeResetConfirmation();
                return;
            }

            return;
        }

        if (button.id == BUTTON_SAVE) {
            saveBhopName();
            return;
        }

        /*
         * RESET NAME wordt in mouseClicked() bewust
         * handmatig afgehandeld.
         */
        if (button.id == BUTTON_RESET_NAME) {
            openResetConfirmation();
            return;
        }

        if (button.id == BUTTON_CANCEL) {

            this.mc.displayGuiScreen(editScreen);
        }
    }

    private void openResetConfirmation() {

        showResetConfirmation = true;

        if (this.bhopNameField != null) {

            this.bhopNameField.setFocused(false);
        }

        setResetButtonsVisible(true);
    }

    private void closeResetConfirmation() {

        showResetConfirmation = false;

        setResetButtonsVisible(false);

        if (this.bhopNameField != null) {

            this.bhopNameField.setFocused(true);
            this.bhopNameField.setCursorPositionEnd();
        }
    }

    private void setResetButtonsVisible(
            boolean visible) {

        for (Object object : this.buttonList) {

            if (!(object instanceof GuiButton)) {
                continue;
            }

            GuiButton button = (GuiButton) object;

            if (button.id == BUTTON_RESET_YES
                    || button.id == BUTTON_RESET_NO) {

                button.visible = visible;
            }
        }
    }

    private void resetBhopName() {

        if (this.bhopNameField != null) {

            this.bhopNameField.setText(
                    DEFAULT_BHOP_NAME
            );

            this.bhopNameField.setCursorPositionEnd();
        }

        closeResetConfirmation();
    }

    private void saveBhopName() {

        if (this.bhopNameField == null) {
            return;
        }

        String newName =
                this.bhopNameField.getText();

        if (newName == null) {
            return;
        }

        newName = newName.trim();

        if (newName.isEmpty()) {
            return;
        }

        BhopMode.setBhopName(newName);

        String profileName =
                BhopProfileManager
                        .getSelectedProfileName();

        if (profileName == null
                || profileName.trim().isEmpty()) {

            profileName = "Default";
        }

        BhopProfileManager.saveProfile(
                profileName,
                BhopMode.getBhopName(),
                BhopMode.getColor(),
                BhopMode.isEnabled(),
                BhopMode.isShiftUpEnabled(),
                BhopMode.getSpeedLevel()
        );

        BhopMode.loadSelectedProfile();

        this.mc.displayGuiScreen(
                new BhopSettingsGui(settingsScreen)
        );
    }

    @Override
    public void drawScreen(
            int mouseX,
            int mouseY,
            float partialTicks) {

        drawModernBackground();

        int centerX = this.width / 2;

        drawModTitle(
                "ALPHA'S RENAME BHOP",
                centerX,
                105
        );

        if (this.bhopNameField != null) {
            this.bhopNameField.drawTextBox();
        }

        super.drawScreen(
                mouseX,
                mouseY,
                partialTicks
        );

        if (showResetConfirmation) {

            drawResetConfirmation(centerX);

            drawResetConfirmationButtons(
                    mouseX,
                    mouseY
            );
        }
    }

    private void drawResetConfirmationButtons(
            int mouseX,
            int mouseY) {

        /*
         * De popup-buttons moeten dezelfde schaal gebruiken
         * als de rest van de GUI.
         */
        int scaledMouseX =
                getScaledMouseX(mouseX);

        int scaledMouseY =
                getScaledMouseY(mouseY);

        for (Object object : this.buttonList) {

            if (!(object instanceof ResetConfirmationButton)) {
                continue;
            }

            ResetConfirmationButton button =
                    (ResetConfirmationButton) object;

            if (!button.visible) {
                continue;
            }

            button.drawButton(
                    this.mc,
                    scaledMouseX,
                    scaledMouseY
            );
        }
    }

    private void drawResetConfirmation(
            int centerX) {

        int popupWidth = 230;
        int popupHeight = 92;

        int popupLeft =
                centerX - popupWidth / 2;

        int popupTop = 170;

        int popupRight =
                popupLeft + popupWidth;

        int popupBottom =
                popupTop + popupHeight;

        drawRect(
                0,
                0,
                this.width,
                this.height,
                0x66000000
        );

        drawRect(
                popupLeft,
                popupTop,
                popupRight,
                popupBottom,
                0xEE151515
        );

        drawRect(
                popupLeft,
                popupTop,
                popupRight,
                popupTop + 1,
                0xFF555555
        );

        drawRect(
                popupLeft,
                popupBottom - 1,
                popupRight,
                popupBottom,
                0xFF555555
        );

        drawRect(
                popupLeft,
                popupTop,
                popupLeft + 1,
                popupBottom,
                0xFF555555
        );

        drawRect(
                popupRight - 1,
                popupTop,
                popupRight,
                popupBottom,
                0xFF555555
        );

        this.drawCenteredString(
                this.fontRendererObj,
                "RESET BHOP NAME?",
                centerX,
                popupTop + 12,
                0xFFFFFF
        );

        this.drawCenteredString(
                this.fontRendererObj,
                "Reset to ALPHA'S BHOP?",
                centerX,
                popupTop + 29,
                0xAAAAAA
        );
    }

    private static class ResetConfirmationButton
            extends GuiButton {

        private final boolean green;

        public ResetConfirmationButton(
                int buttonId,
                int x,
                int y,
                int width,
                int height,
                String text,
                boolean green) {

            super(
                    buttonId,
                    x,
                    y,
                    width,
                    height,
                    text
            );

            this.green = green;
        }

        @Override
        public void drawButton(
                net.minecraft.client.Minecraft mc,
                int mouseX,
                int mouseY) {

            if (!this.visible) {
                return;
            }

            boolean hovered =
                    mouseX >= this.xPosition
                            && mouseY >= this.yPosition
                            && mouseX < this.xPosition + this.width
                            && mouseY < this.yPosition + this.height;

            int background;

            if (green) {

                background =
                        hovered
                                ? 0xCC1F5A1F
                                : 0xAA153A15;

            } else {

                background =
                        hovered
                                ? 0xCC5A1F1F
                                : 0xAA3A1515;
            }

            drawRect(
                    this.xPosition,
                    this.yPosition,
                    this.xPosition + this.width,
                    this.yPosition + this.height,
                    background
            );

            int border;

            if (green) {

                border =
                        hovered
                                ? 0xFF55FF55
                                : 0xFF3F9F3F;

            } else {

                border =
                        hovered
                                ? 0xFFFF5555
                                : 0xFF9F3F3F;
            }

            drawRect(
                    this.xPosition,
                    this.yPosition,
                    this.xPosition + this.width,
                    this.yPosition + 1,
                    border
            );

            drawRect(
                    this.xPosition,
                    this.yPosition + this.height - 1,
                    this.xPosition + this.width,
                    this.yPosition + this.height,
                    border
            );

            drawRect(
                    this.xPosition,
                    this.yPosition,
                    this.xPosition + 1,
                    this.yPosition + this.height,
                    border
            );

            drawRect(
                    this.xPosition + this.width - 1,
                    this.yPosition,
                    this.xPosition + this.width,
                    this.yPosition + this.height,
                    border
            );

            drawCenteredString(
                    mc.fontRendererObj,
                    this.displayString,
                    this.xPosition + this.width / 2,
                    this.yPosition + (this.height - 8) / 2,
                    0xFFFFFF
            );
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
