package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.movement.BhopMode;
import com.alpharip.staffmod.profile.ModuleProfileManager;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/**
 * Alpha's Staffing Mods
 *
 * Author: MsAlexTsuki
 */
public class BhopSettingsGui extends ModernGuiScreen {

    private static final int BUTTON_TOGGLE = 1;
    private static final int BUTTON_SPEED_LEFT = 2;
    private static final int BUTTON_SPEED = 3;
    private static final int BUTTON_SPEED_RIGHT = 4;
    private static final int BUTTON_JUMP_BOOST = 5;
    private static final int BUTTON_RENAME = 6;
    private static final int BUTTON_SAVE = 7;
    private static final int BUTTON_BACK = 8;

    private final GuiScreen parentScreen;

    public BhopSettingsGui(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    @Override
    public void initGui() {

        this.buttonList.clear();

        int centerX =
                this.width / 2;

        int panelLeft =
                centerX - 300;


        /*
         * =========================
         * BHOP TOGGLE
         * =========================
         */

        this.buttonList.add(
                new ModernButton(
                        BUTTON_TOGGLE,
                        panelLeft + 400,
                        130,
                        120,
                        25,
                        getBhopToggleText()
                )
        );


        /*
         * =========================
         * SPEED LEFT
         * =========================
         */

        this.buttonList.add(
                new SpeedArrowButton(
                        BUTTON_SPEED_LEFT,
                        panelLeft + 350,
                        175,
                        40,
                        25,
                        "<"
                )
        );


        /*
         * =========================
         * SPEED
         * =========================
         */

        this.buttonList.add(
                new ModernButton(
                        BUTTON_SPEED,
                        panelLeft + 395,
                        175,
                        130,
                        25,
                        BhopMode.getSpeedLevelName()
                )
        );


        /*
         * =========================
         * SPEED RIGHT
         * =========================
         */

        this.buttonList.add(
                new SpeedArrowButton(
                        BUTTON_SPEED_RIGHT,
                        panelLeft + 530,
                        175,
                        40,
                        25,
                        ">"
                )
        );


        /*
         * =========================
         * JUMP BOOST
         * =========================
         */

        this.buttonList.add(
                new ModernButton(
                        BUTTON_JUMP_BOOST,
                        panelLeft + 400,
                        220,
                        120,
                        25,
                        getJumpBoostText()
                )
        );


        /*
         * =========================
         * RENAME BHOP
         * =========================
         *
         * Dit opent de aparte
         * BhopRenameGui.
         *
         * SAVE:
         * BhopRenameGui -> this
         *                  -> BhopSettingsGui
         *
         * CANCEL:
         * BhopRenameGui -> parentScreen
         *                  -> EditGui
         */

        this.buttonList.add(
                new ModernButton(
                        BUTTON_RENAME,
                        centerX - 125,
                        270,
                        170,
                        28,
                        "RENAME BHOP"
                )
        );


        /*
         * =========================
         * SAVE
         * =========================
         */

        this.buttonList.add(
                new ModernButton(
                        BUTTON_SAVE,
                        centerX + 50,
                        270,
                        120,
                        28,
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
                        centerX - 125,
                        this.height - 45,
                        250,
                        25,
                        "BACK"
                )
        );
    }


    /*
     * =========================================================
     * BHOP TOGGLE TEXT
     * =========================================================
     */

    private String getBhopToggleText() {

        return BhopMode.isEnabled()
                ? "ON"
                : "OFF";
    }


    /*
     * =========================================================
     * JUMP BOOST TEXT
     * =========================================================
     */

    private String getJumpBoostText() {

        return BhopMode.isShiftUpEnabled()
                ? "ON"
                : "OFF";
    }


    /*
     * =========================================================
     * PROFILE NAME
     * =========================================================
     */

    private String getProfileName() {

        String profileName =
                ModuleProfileManager
                        .getSelectedProfileName();

        if (profileName == null
                || profileName.trim().isEmpty()) {

            return "Default";
        }

        return profileName.trim();
    }


    /*
     * =========================================================
     * PREVIOUS SPEED LEVEL
     * =========================================================
     */

    private void previousSpeedLevel() {

        int current =
                BhopMode.getSpeedLevel();

        current--;

        if (current < 1) {

            current = 20;
        }

        BhopMode.setSpeedLevel(
                current
        );
    }


    /*
     * =========================================================
     * NEXT SPEED LEVEL
     * =========================================================
     */

    /**
     * Speed I -> Speed II -> ... -> Speed XX -> Speed I
     */
    private void nextSpeedLevel() {

        int current =
                BhopMode.getSpeedLevel();

        current++;

        if (current > 20) {

            current = 1;
        }

        BhopMode.setSpeedLevel(
                current
        );
    }


    /*
     * =========================================================
     * SAVE CURRENT PROFILE
     * =========================================================
     */

    /**
     * Saves the current BHOP settings
     * to the selected profile.
     */
    private void saveCurrentProfile() {

        String profileName =
                ModuleProfileManager
                        .getSelectedProfileName();

        if (profileName == null
                || profileName.trim().isEmpty()) {

            profileName = "Default";
        }

        ModuleProfileManager.saveProfile(
                profileName,
                BhopMode.getBhopName(),
                BhopMode.getColor(),
                BhopMode.isEnabled(),
                BhopMode.isShiftUpEnabled(),
                BhopMode.getSpeedLevel()
        );

        BhopMode.loadSelectedProfile();
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
         * =========================
         * BHOP TOGGLE
         * =========================
         */

        if (button.id ==
                BUTTON_TOGGLE) {

            BhopMode.toggle();

            button.displayString =
                    getBhopToggleText();

            return;
        }


        /*
         * =========================
         * SPEED LEFT
         * =========================
         */

        if (button.id ==
                BUTTON_SPEED_LEFT) {

            previousSpeedLevel();

            updateSpeedButton();

            return;
        }


        /*
         * =========================
         * SPEED
         * =========================
         */

        if (button.id ==
                BUTTON_SPEED) {

            nextSpeedLevel();

            updateSpeedButton();

            return;
        }


        /*
         * =========================
         * SPEED RIGHT
         * =========================
         */

        if (button.id ==
                BUTTON_SPEED_RIGHT) {

            nextSpeedLevel();

            updateSpeedButton();

            return;
        }


        /*
         * =========================
         * JUMP BOOST
         * =========================
         */

        if (button.id ==
                BUTTON_JUMP_BOOST) {

            BhopMode.toggleShiftUp();

            button.displayString =
                    getJumpBoostText();

            return;
        }


        /*
         * =========================
         * RENAME BHOP
         * =========================
         *
         * Open de aparte BHOP
         * rename GUI.
         *
         * Eerste parent:
         *      this
         *      = BhopSettingsGui
         *
         * Tweede parent:
         *      parentScreen
         *      = EditGui
         *
         * SAVE:
         *      BhopRenameGui
         *          -> BhopSettingsGui
         *
         * CANCEL:
         *      BhopRenameGui
         *          -> EditGui
         */

        if (button.id ==
                BUTTON_RENAME) {

            this.mc.displayGuiScreen(
                    new BhopRenameGui(
                            this,
                            parentScreen
                    )
            );

            return;
        }


        /*
         * =========================
         * SAVE
         * =========================
         */

        if (button.id ==
                BUTTON_SAVE) {

            saveCurrentProfile();

            return;
        }


        /*
         * =========================
         * BACK
         * =========================
         */

        if (button.id ==
                BUTTON_BACK) {

            this.mc.displayGuiScreen(
                    parentScreen
            );
        }
    }


    /*
     * =========================================================
     * UPDATE SPEED BUTTON
     * =========================================================
     */

    private void updateSpeedButton() {

        for (Object object :
                this.buttonList) {

            if (!(object instanceof GuiButton)) {

                continue;
            }

            GuiButton button =
                    (GuiButton) object;

            if (button.id ==
                    BUTTON_SPEED) {

                button.displayString =
                        BhopMode.getSpeedLevelName();

                return;
            }
        }
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
         * Minecraft world blijft zichtbaar.
         */

        drawModernBackground();

        int centerX =
                this.width / 2;


        /*
         * =========================
         * HEADER
         * =========================
         */

        drawModTitle(
                "BHOP",
                centerX,
                35
        );


        int panelLeft =
                centerX - 300;

        int panelRight =
                centerX + 300;


        /*
         * =========================
         * PANEL
         * =========================
         */

        drawPanel(
                panelLeft,
                105,
                panelRight,
                305
        );


        /*
         * =========================
         * PROFILE NAME
         * =========================
         *
         * Alleen informatie.
         *
         * De profielnaam wordt hier
         * NIET gewijzigd.
         */

        drawRowLabel(
                getProfileName(),
                panelLeft + 22,
                125
        );


        /*
         * =========================
         * STATUS
         * =========================
         */

        this.fontRendererObj.drawStringWithShadow(
                "Status:",
                panelLeft + 22,
                151,
                COLOR_WHITE
        );

        this.fontRendererObj.drawStringWithShadow(
                BhopMode.isEnabled()
                        ? "ENABLED"
                        : "DISABLED",
                panelLeft + 82,
                151,
                BhopMode.isEnabled()
                        ? COLOR_GREEN
                        : COLOR_RED
        );


        /*
         * =========================
         * SPEED
         * =========================
         */

        drawRowLabel(
                BhopMode.getSpeedLevelName(),
                panelLeft + 22,
                184
        );


        /*
         * =========================
         * JUMP BOOST
         * =========================
         */

        drawRowLabel(
                "JUMP BOOST",
                panelLeft + 22,
                221
        );

        this.fontRendererObj.drawStringWithShadow(
                "Hold SHIFT without movement for Jump Boost.",
                panelLeft + 22,
                240,
                COLOR_GRAY
        );

        this.fontRendererObj.drawStringWithShadow(
                "Save your current BHOP settings to this profile.",
                panelLeft + 22,
                260,
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
     * GAME DOES NOT PAUSE
     * =========================================================
     */

    @Override
    public boolean doesGuiPauseGame() {

        return false;
    }
}