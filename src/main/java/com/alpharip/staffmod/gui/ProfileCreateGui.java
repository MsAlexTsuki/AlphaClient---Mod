package com.alpharip.staffmod.gui;

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
public class ProfileCreateGui extends ModernGuiScreen {

    private static final int BUTTON_CREATE = 1;
    private static final int BUTTON_CANCEL = 2;

    private final GuiScreen parentScreen;

    private GuiTextField profileNameField;

    public ProfileCreateGui(
            GuiScreen parentScreen) {

        this.parentScreen =
                parentScreen;
    }

    @Override
    public void initGui() {

        this.buttonList.clear();

        int centerX =
                this.width / 2;

        int formWidth =
                Math.min(
                        240,
                        Math.max(
                                120,
                                this.width - 24
                        )
                );

        int formLeft =
                centerX
                        - formWidth / 2;

        int fieldY =
                isSmallScreen()
                        ? 125
                        : 145;

        int buttonY =
                fieldY + 40;

        int gap = 6;

        int buttonWidth =
                Math.max(
                        1,
                        (formWidth - gap) / 2
                );

        profileNameField =
                new GuiTextField(
                        0,
                        this.fontRendererObj,
                        formLeft,
                        fieldY,
                        formWidth,
                        24
                );

        profileNameField.setMaxStringLength(
                32
        );

        profileNameField.setFocused(
                true
        );

        Keyboard.enableRepeatEvents(
                true
        );

        this.buttonList.add(
                new ModernButton(
                        BUTTON_CREATE,
                        formLeft,
                        buttonY,
                        buttonWidth,
                        30,
                        "CREATE"
                )
        );

        this.buttonList.add(
                new ModernButton(
                        BUTTON_CANCEL,
                        formLeft
                                + buttonWidth
                                + gap,
                        buttonY,
                        buttonWidth,
                        30,
                        "CANCEL"
                )
        );
    }

    @Override
    public void onGuiClosed() {

        Keyboard.enableRepeatEvents(
                false
        );
    }

    @Override
    public void updateScreen() {

        if (profileNameField != null) {

            profileNameField
                    .updateCursorCounter();
        }
    }

    @Override
    protected void keyTyped(
            char typedChar,
            int keyCode)
            throws IOException {

        if (keyCode ==
                Keyboard.KEY_ESCAPE) {

            this.mc.displayGuiScreen(
                    parentScreen
            );

            return;
        }

        if (keyCode ==
                Keyboard.KEY_RETURN
                || keyCode ==
                Keyboard.KEY_NUMPADENTER) {

            createProfile();

            return;
        }

        if (profileNameField != null
                && profileNameField
                .textboxKeyTyped(
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

    @Override
    protected void actionPerformed(
            GuiButton button)
            throws IOException {

        if (button == null) {
            return;
        }

        if (button.id ==
                BUTTON_CREATE) {

            createProfile();

            return;
        }

        if (button.id ==
                BUTTON_CANCEL) {

            this.mc.displayGuiScreen(
                    parentScreen
            );
        }
    }

    private void createProfile() {

        if (profileNameField == null) {
            return;
        }

        String name =
                profileNameField
                        .getText()
                        .trim();

        if (name.isEmpty()) {
            return;
        }

        boolean created =
                ModuleProfileManager.saveProfile(
                        name,
                        "ALPHA'S BHOP",
                        "#55FF55",
                        false,
                        false,
                        2
                );

        if (!created) {
            return;
        }

        this.mc.displayGuiScreen(
                parentScreen
        );
    }

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

        boolean small =
                isSmallScreen();

        /*
         * ALPHA LOGO
         */

        drawAlphaLogo(
                "ALPHA",
                centerX,
                small ? 25 : 45,
                small ? 2 : 3
        );

        /*
         * TITLE
         */

        drawCenteredString(
                this.fontRendererObj,
                "CREATE PROFILE",
                centerX,
                small ? 82 : 102,
                COLOR_WHITE
        );

        /*
         * DESCRIPTION
         */

        drawCenteredString(
                this.fontRendererObj,
                "Enter a name for your new profile.",
                centerX,
                small ? 98 : 118,
                COLOR_GRAY
        );

        /*
         * TEXT FIELD
         */

        if (profileNameField != null) {

            profileNameField.drawTextBox();
        }

        /*
         * BUTTONS
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