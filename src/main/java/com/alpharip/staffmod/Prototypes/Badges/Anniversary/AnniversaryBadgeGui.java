package com.alpharip.staffmod.badges.anniversary;

import com.alpharip.staffmod.badges.anniversary.birthday.BirthdayCelebrationGui;
import com.alpharip.staffmod.badges.anniversary.birthday.BirthdayCelebrationSettings;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

/**
 * Minecraft 1.8.9 GUI prototype for account anniversary badges.
 * Account creation date is supplied manually; live account lookup and badge
 * rendering outside this preview are intentionally not integrated.
 */
public final class AnniversaryBadgeGui extends GuiScreen {
    private static final int ENABLE_BUTTON = 1;
    private static final int SET_DATE_BUTTON = 2;
    private static final int CLEAR_DATE_BUTTON = 3;
    private static final int BACK_BUTTON = 4;
    private static final int BIRTHDAY_BUTTON = 5;
    private static final String DATE_PATTERN = "yyyy-MM-dd";

    private final GuiScreen parent;
    private final AnniversaryBadgeSettings settings;
    private final BirthdayCelebrationSettings birthdaySettings;
    private GuiTextField dateField;
    private String feedback = "";

    public AnniversaryBadgeGui(GuiScreen parent) {
        this(parent, new AnniversaryBadgeSettings(), new BirthdayCelebrationSettings());
    }

    public AnniversaryBadgeGui(GuiScreen parent, AnniversaryBadgeSettings settings) {
        this(parent, settings, new BirthdayCelebrationSettings());
    }

    public AnniversaryBadgeGui(GuiScreen parent, AnniversaryBadgeSettings settings,
                               BirthdayCelebrationSettings birthdaySettings) {
        if (settings == null) throw new IllegalArgumentException("Anniversary badge settings are required.");
        if (birthdaySettings == null) throw new IllegalArgumentException("Birthday settings are required.");
        this.parent = parent;
        this.settings = settings;
        this.birthdaySettings = birthdaySettings;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        buttonList.clear();
        int panelWidth = Math.min(360, Math.max(280, width - 24));
        int left = (width - panelWidth) / 2;
        int fieldWidth = panelWidth - 32;
        int halfWidth = fieldWidth / 2 - 3;

        dateField = new GuiTextField(0, fontRendererObj, left + 16, 91, fieldWidth, 18);
        dateField.setMaxStringLength(10);
        Date existing = settings.getAccountCreatedAt();
        if (existing != null) dateField.setText(formatDate(existing));

        buttonList.add(new GuiButton(ENABLE_BUTTON, left + 16, 52, halfWidth, 20,
                "Anniversary: " + (settings.isEnabled() ? "On" : "Off")));
        buttonList.add(new GuiButton(BIRTHDAY_BUTTON, left + 19 + fieldWidth / 2, 52,
                halfWidth, 20, "Birthday Celebration"));
        buttonList.add(new GuiButton(SET_DATE_BUTTON, left + 16, 116, halfWidth, 20, "Set Account Date"));
        buttonList.add(new GuiButton(CLEAR_DATE_BUTTON, left + 19 + fieldWidth / 2, 116,
                halfWidth, 20, "Clear Date"));
        buttonList.add(new GuiButton(BACK_BUTTON, left + panelWidth - 92, height - 30, 76, 20, "Back"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button.id == ENABLE_BUTTON) {
            settings.setEnabled(!settings.isEnabled());
            button.displayString = "Anniversary: " + (settings.isEnabled() ? "On" : "Off");
            feedback = "Badge display state updated.";
        } else if (button.id == BIRTHDAY_BUTTON) {
            mc.displayGuiScreen(new BirthdayCelebrationGui(this, birthdaySettings));
        } else if (button.id == SET_DATE_BUTTON) {
            Date parsed = parseDate(dateField.getText());
            if (parsed == null) {
                feedback = "Use a valid date in YYYY-MM-DD format.";
            } else if (parsed.after(new Date())) {
                feedback = "Account creation date cannot be in the future.";
            } else {
                settings.setAccountCreatedAt(parsed);
                feedback = "Account creation date saved locally.";
            }
        } else if (button.id == CLEAR_DATE_BUTTON) {
            settings.clearAccountCreatedAt();
            dateField.setText("");
            feedback = "Account creation date cleared.";
        } else if (button.id == BACK_BUTTON) {
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (dateField.textboxKeyTyped(typedChar, keyCode)) return;
        if (keyCode == Keyboard.KEY_ESCAPE) mc.displayGuiScreen(parent);
        else super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        dateField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void updateScreen() {
        dateField.updateCursorCounter();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int panelWidth = Math.min(360, Math.max(280, width - 24));
        int panelHeight = Math.min(height - 20, 300);
        int left = (width - panelWidth) / 2;
        int top = (height - panelHeight) / 2;
        int right = left + panelWidth;
        int bottom = top + panelHeight;

        drawRect(left, top, right, bottom, 0xF00B0D12);
        drawRect(left, top, right, top + 2, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "ACCOUNT ANNIVERSARY", width / 2, top + 12, 0xFFF2F3F5);
        drawCenteredString(fontRendererObj, "Badge milestone preview", width / 2, top + 28, 0xFFB8BDC8);

        drawString(fontRendererObj, "MODULE STATE", left + 16, 43, 0xFFB8BDC8);
        drawString(fontRendererObj, "ACCOUNT CREATION DATE (YYYY-MM-DD)", left + 16, 81, 0xFFB8BDC8);
        drawRect(left + 15, 90, right - 15, 110, 0xFF11141B);
        dateField.drawTextBox();

        List<AnniversaryBadgeStatus> statuses = settings.hasAccountCreatedAt()
                ? AnniversaryBadgeCalculator.calculate(settings.getAccountCreatedAt(), new Date())
                : AnniversaryBadgeCalculator.calculate(null, new Date());

        int listTop = 151;
        drawString(fontRendererObj, "ANNIVERSARY BADGES", left + 16, 143, 0xFFFFD866);
        drawRect(left + 15, listTop, right - 15, bottom - 44, 0x6614171E);
        if (!settings.hasAccountCreatedAt()) {
            drawCenteredString(fontRendererObj, "Enter an account creation date to preview badges.",
                    width / 2, listTop + 20, 0xFF858D9B);
        } else {
            for (int i = 0; i < statuses.size(); i++) {
                AnniversaryBadgeStatus status = statuses.get(i);
                String state = status.isEarned() ? "EARNED" : "LOCKED";
                int color = status.isEarned() ? 0xFFFFD866 : 0xFF858D9B;
                int rowY = listTop + 8 + i * 17;
                drawString(fontRendererObj, status.getTier().getDisplayName(), left + 23, rowY, 0xFFE0E3E8);
                drawString(fontRendererObj, state, right - 67, rowY, color);
            }
        }

        drawString(fontRendererObj, feedback, left + 16, bottom - 31, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "Local prototype only; no account service or Supabase connection.",
                width / 2, height - 12, 0xFF858D9B);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private static Date parseDate(String value) {
        if (value == null) return null;
        SimpleDateFormat format = new SimpleDateFormat(DATE_PATTERN, Locale.ROOT);
        format.setLenient(false);
        try {
            Date parsed = format.parse(value.trim());
            return value.trim().equals(format.format(parsed)) ? parsed : null;
        } catch (ParseException exception) {
            return null;
        }
    }

    private static String formatDate(Date date) {
        return new SimpleDateFormat(DATE_PATTERN, Locale.ROOT).format(date);
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
