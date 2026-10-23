package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.movement.BhopMode;
import com.alpharip.staffmod.profile.ModuleProfileManager;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

import org.lwjgl.input.Keyboard;

import java.io.IOException;

/**
 * Alpha's Staffing Mods
 *
 * Author: MsAlexTsuki
 */
public class ProfileRenameGui extends ModernGuiScreen {

    private static final int BUTTON_SAVE = 1;
    private static final int BUTTON_CANCEL = 2;

    private final GuiScreen parentScreen;

    private GuiTextField profileNameField;

    /*
     * =========================================================
     * CONSTRUCTOR
     * =========================================================
     */

    public ProfileRenameGui(
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

        this.buttonList.clear();

        int centerX =
                this.width / 2;

        /*
         * =====================================================
         * CURRENT PROFILE NAME
         * =====================================================
         */

        String currentName =
                ModuleProfileManager
                        .getSelectedProfileName();

        if (currentName == null
                || currentName.trim().isEmpty()) {

            currentName =
                    "Default";
        }

        /*
         * =====================================================
         * PROFILE NAME FIELD
         * =====================================================
         */

        profileNameField =
                new GuiTextField(
                        0,
                        this.fontRendererObj,
                        centerX - 120,
                        145,
                        240,
                        24
                );

        profileNameField.setMaxStringLength(
                32
        );

        profileNameField.setText(
                currentName
        );

        profileNameField.setFocused(
                true
        );

        profileNameField.setCursorPositionEnd();

        Keyboard.enableRepeatEvents(
                true
        );

        /*
         * =====================================================
         * SAVE PROFILE
         * =====================================================
         */

        this.buttonList.add(
                new ModernButton(
                        BUTTON_SAVE,
                        centerX - 120,
                        180,
                        240,
                        28,
                        "SAVE PROFILE"
                )
        );

        /*
         * =====================================================
         * CANCEL
         * =====================================================
         */

        this.buttonList.add(
                new ModernButton(
                        BUTTON_CANCEL,
                        centerX - 120,
                        215,
                        240,
                        28,
                        "CANCEL"
                )
        );
    }

    /*
     * =========================================================
     * GUI CLOSED
     * =========================================================
     */

    @Override
    public void onGuiClosed() {

        Keyboard.enableRepeatEvents(
                false
        );
    }

    /*
     * =========================================================
     * UPDATE
     * =========================================================
     */

    @Override
    public void updateScreen() {

        if (profileNameField != null) {

            profileNameField.updateCursorCounter();
        }
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

        /*
         * ESC = CANCEL
         */

        if (keyCode ==
                Keyboard.KEY_ESCAPE) {

            this.mc.displayGuiScreen(
                    parentScreen
            );

            return;
        }

        /*
         * ENTER = SAVE PROFILE
         */

        if (keyCode ==
                Keyboard.KEY_RETURN
                || keyCode ==
                Keyboard.KEY_NUMPADENTER) {

            saveProfile();

            return;
        }

        /*
         * TEXT FIELD
         */

        if (profileNameField != null) {

            profileNameField.textboxKeyTyped(
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

        if (profileNameField != null) {

            profileNameField.mouseClicked(
                    mouseX,
                    mouseY,
                    mouseButton
            );
        }

        super.mouseClicked(
                mouseX,
                mouseY,
                mouseButton
        );
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
         * SAVE PROFILE
         * =====================================================
         */

        if (button.id ==
                BUTTON_SAVE) {

            saveProfile();

            return;
        }

        /*
         * =====================================================
         * CANCEL
         * =====================================================
         */

        if (button.id ==
                BUTTON_CANCEL) {

            this.mc.displayGuiScreen(
                    parentScreen
            );
        }
    }

    /*
     * =========================================================
     * SAVE PROFILE
     * =========================================================
     */

    private void saveProfile() {

        if (profileNameField == null) {

            return;
        }

        String newName =
                profileNameField
                        .getText()
                        .trim();

        /*
         * Geen lege profielnaam.
         */

        if (newName.isEmpty()) {

            return;
        }

        /*
         * =====================================================
         * SAVE EVERYTHING
         * =====================================================
         */

        ModuleProfileManager.saveProfile(
                newName,
                BhopMode.getBhopName(),
                BhopMode.getColor(),
                BhopMode.isEnabled(),
                BhopMode.isShiftUpEnabled(),
                BhopMode.getSpeedLevel()
        );

        /*
         * =====================================================
         * LOAD SAVED PROFILE
         * =====================================================
         */

        BhopMode.loadSelectedProfile();

        /*
         * =====================================================
         * RETURN TO PREVIOUS GUI
         * =====================================================
         */

        this.mc.displayGuiScreen(
                parentScreen
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

        /*
         * Transparante Minecraft-wereld.
         */
        drawModernBackground();

        int centerX =
                this.width / 2;

        /*
         * =====================================================
         * ALPHA LOGO
         * =====================================================
         */

        drawAlphaLogo(
                "ALPHA'S STAFFING MODS",
                centerX,
                45,
                2
        );

        /*
         * =====================================================
         * TITLE
         * =====================================================
         */

        drawCenteredString(
                this.fontRendererObj,
                "RENAME PROFILE",
                centerX,
                105,
                COLOR_WHITE
        );

        /*
         * =====================================================
         * PROFILE NAME FIELD
         * =====================================================
         */

        if (profileNameField != null) {

            profileNameField.drawTextBox();
        }

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
     * GAME DOES NOT PAUSE
     * =========================================================
     */

    @Override
    public boolean doesGuiPauseGame() {

        return false;
    }
}