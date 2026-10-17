package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.config.HudPositionConfig;
import com.alpharip.staffmod.hud.BhopHud;
import com.alpharip.staffmod.profile.ConfigProfileManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.List;

/** UI for saving and applying named general HUD configuration profiles. */
public final class ConfigProfilesGui extends GuiScreen {
    private static final int BACK = 1;
    private static final int NAME_FIELD = 2;
    private static final int SAVE = 3;
    private static final int LOAD = 4;
    private static final int DELETE = 5;
    private static final int PROFILE_BASE = 100;

    private static final int ACCENT = 0xFFFFD866;

    private final GuiScreen parent;
    private GuiTextField nameField;
    private String selectedProfile;
    private String status = "";
    private int scrollOffset;
    private int visibleProfiles;

    public ConfigProfilesGui(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        Keyboard.enableRepeatEvents(true);

        int panelWidth = Math.min(360, width - 24);
        int left = (width - panelWidth) / 2;
        int top = Math.max(12, (height - Math.min(280, height - 24)) / 2);
        int bottom = Math.min(height - 12, top + 280);

        nameField = new GuiTextField(NAME_FIELD, fontRendererObj, left + 14, top + 54,
                panelWidth - 28, 18);
        nameField.setMaxStringLength(32);
        nameField.setFocused(true);

        int listTop = top + 84;
        int listBottom = bottom - 64;
        visibleProfiles = Math.max(1, (listBottom - listTop) / 21);

        List<String> profiles = ConfigProfileManager.getProfiles();
        if (selectedProfile == null || !profiles.contains(selectedProfile)) {
            selectedProfile = profiles.isEmpty() ? null : profiles.get(0);
        }
        scrollOffset = Math.max(0, Math.min(scrollOffset, Math.max(0, profiles.size() - visibleProfiles)));

        for (int i = 0; i < visibleProfiles && scrollOffset + i < profiles.size(); i++) {
            String profile = profiles.get(scrollOffset + i);
            buttonList.add(new GuiButton(PROFILE_BASE + i, left + 14, listTop + i * 21,
                    panelWidth - 28, 18, profile));
        }

        int actionY = bottom - 42;
        int gap = 5;
        int actionWidth = (panelWidth - 28 - gap * 2) / 3;
        buttonList.add(new GuiButton(SAVE, left + 14, actionY, actionWidth, 20, "SAVE CURRENT"));
        buttonList.add(new GuiButton(LOAD, left + 14 + actionWidth + gap, actionY,
                actionWidth, 20, "LOAD"));
        buttonList.add(new GuiButton(DELETE, left + 14 + (actionWidth + gap) * 2, actionY,
                actionWidth, 20, "DELETE"));
        buttonList.add(new GuiButton(BACK, left + 14, bottom - 20, 80, 16, "BACK"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        int panelWidth = Math.min(360, width - 24);
        int left = (width - panelWidth) / 2;
        int top = Math.max(12, (height - Math.min(280, height - 24)) / 2);
        int bottom = Math.min(height - 12, top + 280);

        drawRect(0, 0, width, height, 0x18000000);
        drawRect(left, top, left + panelWidth, bottom, 0xD90A0C11);
        drawRect(left, top, left + panelWidth, top + 2, ACCENT);
        drawCenteredString(fontRendererObj, "CONFIG PROFILES", width / 2, top + 12, 0xFFF3F4F6);
        drawCenteredString(fontRendererObj, "HUD layout and visual settings", width / 2, top + 29, ACCENT);
        drawString(fontRendererObj, "Profile name", left + 14, top + 43, 0xFFB8BDC8);
        nameField.drawTextBox();

        int listTop = top + 84;
        List<String> profiles = ConfigProfileManager.getProfiles();
        if (profiles.isEmpty()) {
            drawString(fontRendererObj, "No saved config profiles yet.", left + 14, listTop + 5, 0xFFB8BDC8);
        } else {
            drawString(fontRendererObj, "Saved profiles", left + 14, listTop - 11, 0xFFB8BDC8);
            if (profiles.size() > visibleProfiles) {
                String scroll = "Scroll " + (scrollOffset + 1) + "-"
                        + Math.min(profiles.size(), scrollOffset + visibleProfiles) + "/" + profiles.size();
                drawString(fontRendererObj, scroll, left + panelWidth - 14 - fontRendererObj.getStringWidth(scroll),
                        listTop - 11, 0xFF8E95A3);
            }
        }
        if (!status.isEmpty()) {
            drawString(fontRendererObj, status, left + 14, bottom - 57, 0xFFE7E9EE);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;

        if (button.id == BACK) {
            Keyboard.enableRepeatEvents(false);
            mc.displayGuiScreen(parent);
            return;
        }
        if (button.id == SAVE) {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                status = "Enter a profile name first.";
            } else if (ConfigProfileManager.saveCurrent(name)) {
                selectedProfile = name.replaceAll("[^A-Za-z0-9 _-]", "").trim();
                status = "Config profile saved.";
                initGui();
            } else {
                status = "Could not save. Check that HUD settings exist.";
            }
            return;
        }
        if (button.id == LOAD) {
            if (selectedProfile != null && ConfigProfileManager.loadProfile(selectedProfile)) {
                HudPositionConfig.reload();
                BhopHud.loadHudPosition();
                status = "Config profile loaded.";
            } else {
                status = "Select a profile to load.";
            }
            return;
        }
        if (button.id == DELETE) {
            if (selectedProfile != null && ConfigProfileManager.deleteProfile(selectedProfile)) {
                status = "Config profile deleted.";
                selectedProfile = null;
                initGui();
            } else {
                status = "Select a profile to delete.";
            }
            return;
        }
        if (button.id >= PROFILE_BASE && button.id < PROFILE_BASE + 100) {
            List<String> profiles = ConfigProfileManager.getProfiles();
            int index = scrollOffset + button.id - PROFILE_BASE;
            if (index >= 0 && index < profiles.size()) {
                selectedProfile = profiles.get(index);
                nameField.setText(selectedProfile);
                status = "Selected: " + selectedProfile;
                initGui();
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
        if (nameField != null && nameField.isFocused()) {
            nameField.textboxKeyTyped(typedChar, keyCode);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (nameField != null) nameField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            int count = ConfigProfileManager.getProfiles().size();
            scrollOffset = Math.max(0, Math.min(Math.max(0, count - visibleProfiles),
                    scrollOffset + (wheel < 0 ? 1 : -1)));
            initGui();
        }
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
