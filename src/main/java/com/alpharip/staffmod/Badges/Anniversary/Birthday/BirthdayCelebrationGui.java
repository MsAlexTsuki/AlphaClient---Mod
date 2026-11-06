package com.alpharip.staffmod.badges.anniversary.birthday;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

/** Local configuration screen for the birthday celebration extension. */
public final class BirthdayCelebrationGui extends GuiScreen {
    private static final int ENABLE_BUTTON = 1;
    private static final int SAVE_BUTTON = 2;
    private static final int CLEAR_BUTTON = 3;
    private static final int BACK_BUTTON = 4;

    private final GuiScreen parent;
    private final BirthdayCelebrationSettings settings;
    private GuiTextField birthdayField;
    private String feedback = "";

    public BirthdayCelebrationGui(GuiScreen parent, BirthdayCelebrationSettings settings) {
        if (settings == null) throw new IllegalArgumentException("Birthday settings are required.");
        this.parent = parent;
        this.settings = settings;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        buttonList.clear();

        int panelWidth = Math.min(340, Math.max(280, width - 24));
        int left = (width - panelWidth) / 2;
        int controlWidth = panelWidth - 32;

        birthdayField = new GuiTextField(0, fontRendererObj, left + 16, 104, controlWidth, 18);
        birthdayField.setMaxStringLength(10);
        if (settings.hasBirthday()) birthdayField.setText(settings.getBirthday().toString());

        buttonList.add(new GuiButton(ENABLE_BUTTON, left + 16, 64, controlWidth, 20,
                "Birthday Confetti: " + (settings.isEnabled() ? "On" : "Off")));
        buttonList.add(new GuiButton(SAVE_BUTTON, left + 16, 132, controlWidth / 2 - 3, 20, "Save Birthday"));
        buttonList.add(new GuiButton(CLEAR_BUTTON, left + 19 + controlWidth / 2, 132,
                controlWidth / 2 - 3, 20, "Clear"));
        buttonList.add(new GuiButton(BACK_BUTTON, width / 2 - 38, height - 34, 76, 20, "Back"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;

        if (button.id == ENABLE_BUTTON) {
            settings.setEnabled(!settings.isEnabled());
            button.displayString = "Birthday Confetti: " + (settings.isEnabled() ? "On" : "Off");
            feedback = "Birthday celebration setting updated.";
        } else if (button.id == SAVE_BUTTON) {
            try {
                LocalDate birthday = LocalDate.parse(birthdayField.getText().trim());
                if (birthday.isAfter(LocalDate.now())) {
                    feedback = "Birthday cannot be in the future.";
                } else {
                    settings.setBirthday(birthday);
                    feedback = "Birthday saved locally.";
                }
            } catch (DateTimeParseException exception) {
                feedback = "Use a valid date in YYYY-MM-DD format.";
            }
        } else if (button.id == CLEAR_BUTTON) {
            settings.clearBirthday();
            birthdayField.setText("");
            feedback = "Birthday cleared.";
        } else if (button.id == BACK_BUTTON) {
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (birthdayField.textboxKeyTyped(typedChar, keyCode)) return;
        if (keyCode == Keyboard.KEY_ESCAPE) mc.displayGuiScreen(parent);
        else super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        birthdayField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void updateScreen() {
        birthdayField.updateCursorCounter();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int panelWidth = Math.min(340, Math.max(280, width - 24));
        int panelHeight = 230;
        int left = (width - panelWidth) / 2;
        int top = (height - panelHeight) / 2;
        int right = left + panelWidth;
        int bottom = top + panelHeight;

        drawRect(left, top, right, bottom, 0xF00B0D12);
        drawRect(left, top, right, top + 2, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "ACCOUNT BIRTHDAY", width / 2, top + 14, 0xFFF2F3F5);
        drawCenteredString(fontRendererObj, "Anniversary celebration settings", width / 2, top + 31, 0xFFB8BDC8);
        drawString(fontRendererObj, "CELEBRATION", left + 16, 55, 0xFFB8BDC8);
        drawString(fontRendererObj, "BIRTHDAY (YYYY-MM-DD)", left + 16, 94, 0xFFB8BDC8);
        drawRect(left + 15, 103, right - 15, 123, 0xFF11141B);
        birthdayField.drawTextBox();

        String status = !settings.isEnabled() ? "Celebration is disabled."
                : !settings.hasBirthday() ? "Enter a birthday to configure the celebration."
                : settings.isBirthdayToday(LocalDate.now()) ? "Today is the configured birthday."
                : "Birthday saved; confetti will trigger on that calendar date.";
        drawString(fontRendererObj, status, left + 16, 169, 0xFFE0E3E8);
        drawString(fontRendererObj, feedback, left + 16, 187, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "Local prototype only; particle renderer integration is pending.",
                width / 2, height - 12, 0xFF858D9B);
        super.drawScreen(mouseX, mouseY, partialTicks);
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
