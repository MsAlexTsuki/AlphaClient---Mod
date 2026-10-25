package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.movement.BhopMode;
import com.alpharip.staffmod.profile.Profile;
import com.alpharip.staffmod.profile.ProfileManager;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

import java.io.IOException;
import java.util.List;

public class ProfileSettingsGui extends ModernGuiScreen {

    private static final int BUTTON_PREVIOUS = 1;
    private static final int BUTTON_NEXT = 2;
    private static final int BUTTON_LOAD = 3;
    private static final int BUTTON_SAVE = 4;
    private static final int BUTTON_DELETE = 5;
    private static final int BUTTON_CREATE = 6;
    private static final int BUTTON_BACK = 7;

    private final GuiScreen parentScreen;

    private GuiTextField profileField;
    private GuiTextField nameField;
    private GuiTextField colorField;

    public ProfileSettingsGui(
            GuiScreen parentScreen) {

        this.parentScreen =
                parentScreen;

        ProfileManager.getInstance().load();
    }

    @Override
    public void initGui() {

        this.buttonList.clear();

        int centerX =
                this.width / 2;

        /*
         * =========================
         * TEXT FIELDS
         * =========================
         */

        profileField =
                new GuiTextField(
                        10,
                        this.fontRendererObj,
                        centerX - 125,
                        130,
                        250,
                        22
                );

        nameField =
                new GuiTextField(
                        11,
                        this.fontRendererObj,
                        centerX - 125,
                        195,
                        250,
                        22
                );

        colorField =
                new GuiTextField(
                        12,
                        this.fontRendererObj,
                        centerX - 125,
                        260,
                        250,
                        22
                );

        profileField.setTextColor(0xFFF4F5F8);
        profileField.setDisabledTextColour(0xFFB8BDC8);
        nameField.setTextColor(0xFFF4F5F8);
        nameField.setDisabledTextColour(0xFFB8BDC8);
        colorField.setTextColor(0xFFF4F5F8);
        colorField.setDisabledTextColour(0xFFB8BDC8);

        profileField.setMaxStringLength(32);
        nameField.setMaxStringLength(32);
        colorField.setMaxStringLength(16);

        profileField.setEnableBackgroundDrawing(false);
        nameField.setEnableBackgroundDrawing(false);
        colorField.setEnableBackgroundDrawing(false);

        loadSelectedProfileIntoFields();

        /*
         * =========================
         * PROFILE NAVIGATION
         * =========================
         */

        this.buttonList.add(
                new ModernButton(
                        BUTTON_PREVIOUS,
                        centerX - 155,
                        130,
                        25,
                        22,
                        "<"
                )
        );

        this.buttonList.add(
                new ModernButton(
                        BUTTON_NEXT,
                        centerX + 130,
                        130,
                        25,
                        22,
                        ">"
                )
        );

        /*
         * =========================
         * LOAD / SAVE / DELETE
         * =========================
         */

        this.buttonList.add(
                new ModernButton(
                        BUTTON_LOAD,
                        centerX - 160,
                        165,
                        95,
                        24,
                        "LOAD"
                )
        );

        this.buttonList.add(
                new ModernButton(
                        BUTTON_SAVE,
                        centerX - 47,
                        165,
                        95,
                        24,
                        "SAVE"
                )
        );

        this.buttonList.add(
                new ModernButton(
                        BUTTON_DELETE,
                        centerX + 66,
                        165,
                        95,
                        24,
                        "DELETE"
                )
        );

        /*
         * =========================
         * CREATE
         * =========================
         */

        this.buttonList.add(
                new ModernButton(
                        BUTTON_CREATE,
                        centerX - 125,
                        310,
                        250,
                        28,
                        "CREATE PROFILE"
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
                        this.height - 55,
                        250,
                        25,
                        "BACK"
                )
        );
    }

    /*
     * =========================
     * LOAD SELECTED PROFILE
     * =========================
     */

    private void loadSelectedProfileIntoFields() {

        Profile profile =
                ProfileManager
                        .getInstance()
                        .getSelectedProfile();

        if (profile == null) {
            return;
        }

        profileField.setText(
                profile.getProfileName()
        );

        nameField.setText(
                profile.getBhopName()
        );

        colorField.setText(
                profile.getBhopColor()
        );
    }

    /*
     * =========================
     * LOAD PROFILE
     * =========================
     */

    private void loadProfile() {

        String profileName =
                profileField.getText();

        if (profileName == null
                || profileName.trim().isEmpty()) {

            return;
        }

        ProfileManager manager =
                ProfileManager.getInstance();

        if (!manager.selectProfile(
                profileName.trim()
        )) {

            return;
        }

        loadSelectedProfileIntoFields();
    }

    /*
     * =========================
     * SAVE PROFILE
     * =========================
     */

    private void saveProfile() {

        String profileName =
                profileField.getText();

        String bhopName =
                nameField.getText();

        String color =
                colorField.getText();

        if (profileName == null
                || profileName.trim().isEmpty()) {

            return;
        }

        ProfileManager manager =
                ProfileManager.getInstance();

        Profile selectedProfile =
                manager.getSelectedProfile();

        if (selectedProfile == null) {
            return;
        }

        /*
         * =========================
         * PROFILE NAME
         * =========================
         */

        selectedProfile.setProfileName(
                profileName.trim()
        );

        /*
         * =========================
         * BHOP
         * =========================
         */

        selectedProfile.setBhopName(
                bhopName
        );

        selectedProfile.setBhopColor(
                color
        );

        selectedProfile.setBhopEnabled(
                BhopMode.isEnabled()
        );

        selectedProfile.setShiftUpEnabled(
                BhopMode.isShiftUpEnabled()
        );

        selectedProfile.setSpeedLevel(
                BhopMode.getSpeedLevel()
        );

        /*
         * =========================
         * SAVE
         * =========================
         */

        manager.save();

        /*
         * Apply the profile again so
         * the runtime state and GUI
         * remain synchronized.
         */

        manager.applySelectedProfile();

        loadSelectedProfileIntoFields();
    }

    /*
     * =========================
     * DELETE PROFILE
     * =========================
     */

    private void deleteProfile() {

        String profileName =
                profileField.getText();

        if (profileName == null) {
            return;
        }

        profileName =
                profileName.trim();

        /*
         * Default mag niet verwijderd worden.
         */

        if (profileName
                .equalsIgnoreCase("Default")) {

            return;
        }

        ProfileManager manager =
                ProfileManager.getInstance();

        if (!manager.deleteProfile(
                profileName
        )) {

            return;
        }

        loadSelectedProfileIntoFields();
    }

    /*
     * =========================
     * CREATE PROFILE
     * =========================
     */

    private void createProfile() {

        ProfileManager manager =
                ProfileManager.getInstance();

        String newProfileName =
                getNewProfileName();

        Profile profile =
                manager.createProfile(
                        newProfileName
                );

        if (profile == null) {
            return;
        }

        /*
         * Nieuwe profielen beginnen
         * standaard uitgeschakeld.
         */

        profile.setBhopEnabled(false);
        profile.setShiftUpEnabled(false);
        profile.setSpeedLevel(2);

        profile.setBhopName("BHOP");
        profile.setBhopColor("#55FF55");

profile.setModule(
                "NOFALL",
                false
        );

        manager.save();

        /*
         * Activeer het nieuwe profiel.
         */

        manager.selectProfile(
                newProfileName
        );

        loadSelectedProfileIntoFields();
    }

    /*
     * =========================
     * NEW PROFILE NAME
     * =========================
     */

    private String getNewProfileName() {

        ProfileManager manager =
                ProfileManager.getInstance();

        List<Profile> profiles =
                manager.getProfiles();

        int number = 1;

        while (true) {

            String name =
                    "Profile " + number;

            boolean exists = false;

            for (Profile profile :
                    profiles) {

                if (profile
                        .getProfileName()
                        .equalsIgnoreCase(name)) {

                    exists = true;

                    break;
                }
            }

            if (!exists) {

                return name;
            }

            number++;
        }
    }

    /*
     * =========================
     * PREVIOUS PROFILE
     * =========================
     */

    private void previousProfile() {

        ProfileManager manager =
                ProfileManager.getInstance();

        List<Profile> profiles =
                manager.getProfiles();

        if (profiles.isEmpty()) {
            return;
        }

        String selected =
                manager
                        .getSelectedProfileName();

        int index = 0;

        for (int i = 0;
             i < profiles.size();
             i++) {

            if (profiles.get(i)
                    .getProfileName()
                    .equalsIgnoreCase(selected)) {

                index = i;

                break;
            }
        }

        index--;

        if (index < 0) {

            index =
                    profiles.size() - 1;
        }

        Profile profile =
                profiles.get(index);

        manager.selectProfile(
                profile.getProfileName()
        );

        loadSelectedProfileIntoFields();
    }

    /*
     * =========================
     * NEXT PROFILE
     * =========================
     */

    private void nextProfile() {

        ProfileManager manager =
                ProfileManager.getInstance();

        List<Profile> profiles =
                manager.getProfiles();

        if (profiles.isEmpty()) {
            return;
        }

        String selected =
                manager
                        .getSelectedProfileName();

        int index = 0;

        for (int i = 0;
             i < profiles.size();
             i++) {

            if (profiles.get(i)
                    .getProfileName()
                    .equalsIgnoreCase(selected)) {

                index = i;

                break;
            }
        }

        index++;

        if (index >= profiles.size()) {

            index = 0;
        }

        Profile profile =
                profiles.get(index);

        manager.selectProfile(
                profile.getProfileName()
        );

        loadSelectedProfileIntoFields();
    }

    /*
     * =========================
     * BUTTON ACTIONS
     * =========================
     */

    @Override
    protected void actionPerformed(
            GuiButton button)
            throws IOException {

        if (button.id == BUTTON_PREVIOUS) {

            previousProfile();

            return;
        }

        if (button.id == BUTTON_NEXT) {

            nextProfile();

            return;
        }

        if (button.id == BUTTON_LOAD) {

            loadProfile();

            return;
        }

        if (button.id == BUTTON_SAVE) {

            saveProfile();

            return;
        }

        if (button.id == BUTTON_DELETE) {

            deleteProfile();

            return;
        }

        if (button.id == BUTTON_CREATE) {

            createProfile();

            return;
        }

        if (button.id == BUTTON_BACK) {

            this.mc.displayGuiScreen(
                    parentScreen
            );
        }
    }

    /*
     * =========================
     * KEYBOARD
     * =========================
     */

    @Override
    protected void keyTyped(
            char typedChar,
            int keyCode)
            throws IOException {

        if (profileField.textboxKeyTyped(
                typedChar,
                keyCode
        )) {

            return;
        }

        if (nameField.textboxKeyTyped(
                typedChar,
                keyCode
        )) {

            return;
        }

        if (colorField.textboxKeyTyped(
                typedChar,
                keyCode
        )) {

            return;
        }

        super.keyTyped(
                typedChar,
                keyCode
        );
    }

    /*
     * =========================
     * MOUSE
     * =========================
     */

    @Override
    protected void mouseClicked(
            int mouseX,
            int mouseY,
            int mouseButton)
            throws IOException {

        super.mouseClicked(
                mouseX,
                mouseY,
                mouseButton
        );

        profileField.mouseClicked(
                mouseX,
                mouseY,
                mouseButton
        );

        nameField.mouseClicked(
                mouseX,
                mouseY,
                mouseButton
        );

        colorField.mouseClicked(
                mouseX,
                mouseY,
                mouseButton
        );
    }

    @Override
    public void updateScreen() {

        profileField.updateCursorCounter();
        nameField.updateCursorCounter();
        colorField.updateCursorCounter();

        super.updateScreen();
    }

    /*
     * =========================
     * TEXT FIELD BACKGROUND
     * =========================
     */

    private void drawTextFieldBackground(
            int x,
            int y,
            int width) {

        drawRect(
                x - 3,
                y - 3,
                x + width + 3,
                y + 25,
                0x99151515
        );
    }

    /*
     * =========================
     * DRAW SCREEN
     * =========================
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
         * Header.
         */

        drawHeader(
                "PROFILES",
                "Manage your BHOP profiles"
        );

        /*
         * =========================
         * TRANSPARENT MAIN AREA
         * =========================
         */

        int panelLeft =
                this.width / 2 - 300;

        int panelRight =
                this.width / 2 + 300;

        drawRect(
                panelLeft,
                105,
                panelRight,
                345,
                0x99151515
        );

        /*
         * =========================
         * CURRENT PROFILE
         * =========================
         */

        drawRowLabel(
                "CURRENT PROFILE",
                centerX - 125,
                112
        );

        this.fontRendererObj.drawStringWithShadow(
                "Select a saved profile.",
                centerX - 125,
                122,
                0xFFB8BDC8
        );

        drawTextFieldBackground(
                centerX - 125,
                130,
                250
        );

        /*
         * =========================
         * ACTIONS
         * =========================
         */

        this.fontRendererObj.drawStringWithShadow(
                "PROFILE ACTIONS",
                panelLeft + 25,
                180,
                0x55FF55
        );

        /*
         * =========================
         * BHOP NAME
         * =========================
         */

        drawRowLabel(
                "BHOP NAME",
                panelLeft + 25,
                225
        );

        this.fontRendererObj.drawStringWithShadow(
                "Name shown for this BHOP profile.",
                panelLeft + 25,
                240,
                0xFFB8BDC8
        );

        drawTextFieldBackground(
                centerX - 125,
                195,
                250
        );

        /*
         * =========================
         * COLOR
         * =========================
         */

        drawRowLabel(
                "COLOR",
                panelRight - 275,
                225
        );

        this.fontRendererObj.drawStringWithShadow(
                "Profile display color.",
                panelRight - 275,
                240,
                0xFFB8BDC8
        );

        drawTextFieldBackground(
                panelRight - 275,
                260,
                250
        );

        /*
         * =========================
         * CURRENT SETTINGS
         * =========================
         */

        drawRowLabel(
                "CURRENT SETTINGS",
                panelLeft + 25,
                305
        );

        this.fontRendererObj.drawStringWithShadow(
                "Speed: "
                        + BhopMode.getSpeedLevelName(),
                panelLeft + 25,
                323,
                0xFFD0D3DB
        );

        this.fontRendererObj.drawStringWithShadow(
                "BHOP: "
                        + (BhopMode.isEnabled()
                        ? "ON"
                        : "OFF"),
                panelLeft + 180,
                323,
                BhopMode.isEnabled()
                        ? 0x55FF55
                        : 0xFF5555
        );

        this.fontRendererObj.drawStringWithShadow(
                "Jump Boost: "
                        + (BhopMode.isShiftUpEnabled()
                        ? "ON"
                        : "OFF"),
                panelLeft + 300,
                323,
                BhopMode.isShiftUpEnabled()
                        ? 0x55FF55
                        : 0xFF5555
        );

        /*
         * Text fields.
         */

        profileField.drawTextBox();
        nameField.drawTextBox();
        colorField.drawTextBox();

        /*
         * Buttons.
         */

        super.drawScreen(
                mouseX,
                mouseY,
                partialTicks
        );
    }

    @Override
    public boolean doesGuiPauseGame() {

        return false;
    }
}