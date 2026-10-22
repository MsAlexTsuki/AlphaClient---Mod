package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.profile.ModuleProfileManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

/** Small in-game dialog used to create or rename a profile. */
public final class ProfileNameGui extends AlphaGuiScreen {
    private static final int SAVE = 1;
    private static final int CANCEL = 2;

    private final GuiScreen parent;
    private final boolean editing;
    private final String initialName;
    private GuiTextField nameField;
    private String error = "";

    public ProfileNameGui(GuiScreen parent, boolean editing, String initialName) {
        this.parent = parent;
        this.editing = editing;
        this.initialName = initialName == null ? "" : initialName;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        Keyboard.enableRepeatEvents(true);
        int panelWidth = Math.min(250, width - 24);
        int left = (width - panelWidth) / 2;
        int top = height / 2 - 45;
        nameField = new GuiTextField(0, fontRendererObj, left + 10, top + 35, panelWidth - 20, 18);
        nameField.setMaxStringLength(32);
        nameField.setText(initialName);
        nameField.setFocused(true);
        buttonList.add(new AlphaButton(SAVE, left + 10, top + 65, (panelWidth - 25) / 2, 18, editing ? "SAVE" : "CREATE"));
        buttonList.add(new AlphaButton(CANCEL, left + 15 + (panelWidth - 25) / 2, top + 65,
                (panelWidth - 25) / 2, 18, "CANCEL"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x99000000);
        int panelWidth = Math.min(250, width - 24);
        int left = (width - panelWidth) / 2;
        int top = height / 2 - 45;
        drawRect(left, top, left + panelWidth, top + 94, 0xF00A0C11);
        drawRect(left, top, left + panelWidth, top + 2, 0xFFFFD866);
        drawString(fontRendererObj, editing ? "EDIT PROFILE" : "CREATE PROFILE", left + 10, top + 10, 0xFFFFD866);
        nameField.drawTextBox();
        if (!error.isEmpty()) drawString(fontRendererObj, error, left + 10, top + 56, 0xFFFF7777);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == CANCEL) {
            mc.displayGuiScreen(parent);
            return;
        }
        if (button.id == SAVE) {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                error = "Enter a profile name.";
                return;
            }
            boolean success = editing
                    ? ModuleProfileManager.renameSelectedProfile(name)
                    : ModuleProfileManager.createProfile(name);
            if (success) {
                mc.displayGuiScreen(parent);
            } else {
                error = "Name already exists or is invalid.";
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            Keyboard.enableRepeatEvents(false);
            mc.displayGuiScreen(parent);
            return;
        }
        if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
            for (Object item : buttonList) {
                GuiButton button = (GuiButton) item;
                if (button.id == SAVE) {
                    actionPerformed(button);
                    return;
                }
            }
        }
        nameField.textboxKeyTyped(typedChar, keyCode);
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        nameField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
