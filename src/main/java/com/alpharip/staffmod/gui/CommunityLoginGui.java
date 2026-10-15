package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.permissions.PermissionService;
import com.alpharip.staffmod.permissions.SupabaseCommunityService;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

import java.io.IOException;

/** In-client sign-in so the launcher does not need to be modified. */
public final class CommunityLoginGui extends GuiScreen {
    private static final int SIGN_IN = 1;
    private static final int SIGN_OUT = 2;
    private static final int BACK = 3;

    private final GuiScreen parent;
    private GuiTextField emailField;
    private GuiTextField passwordField;
    private volatile String status = "Sign in with your existing Alpha community account.";
    private volatile boolean busy = false;

    public CommunityLoginGui(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        int center = width / 2;
        int fieldWidth = Math.min(270, width - 48);
        int fieldLeft = center - fieldWidth / 2;
        emailField = new GuiTextField(10, fontRendererObj, fieldLeft, 122, fieldWidth, 20);
        emailField.setTextColor(0xFFF4F5F8);
        emailField.setDisabledTextColour(0xFFB8BDC8);
        emailField.setMaxStringLength(254);
        passwordField = new GuiTextField(11, fontRendererObj, fieldLeft, 164, fieldWidth, 20);
        passwordField.setTextColor(0xFFF4F5F8);
        passwordField.setDisabledTextColour(0xFFB8BDC8);
        passwordField.setMaxStringLength(256);
        buttonList.clear();
        int buttonWidth = (fieldWidth - 8) / 2;
        buttonList.add(new AlphaButton(SIGN_IN, fieldLeft, 199, buttonWidth, 22, "SIGN IN", true));
        buttonList.add(new AlphaButton(SIGN_OUT, fieldLeft + buttonWidth + 8, 199, buttonWidth, 22, "SIGN OUT"));
        buttonList.add(new AlphaButton(BACK, 10, height - 25, 76, 18, "BACK"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawGradientRect(0, 0, width, height, 0xFF181B22, 0xFF090A0E);

        int panelWidth = Math.min(340, width - 24);
        int left = width / 2 - panelWidth / 2;
        int right = left + panelWidth;
        drawRect(left, 38, right, Math.min(height - 32, 286), 0xFF14171D);
        drawRect(left, 38, right, 39, 0xFF343944);
        drawRect(left, 38, left + 3, Math.min(height - 32, 286), 0xFFFFD866);

        drawString(fontRendererObj, "ALPHA COMMUNITY", left + 14, 51, 0xFFFFD866);
        drawString(fontRendererObj, "Sign in to verify your community rank", left + 14, 67, 0xFFF4F5F8);
        drawString(fontRendererObj, "Your verified rank controls module access.", left + 14, 81, 0xFFB8BDC8);
        drawRect(left + 14, 96, right - 14, 97, 0xFF292D36);

        drawString(fontRendererObj, "EMAIL ADDRESS", width / 2 - Math.min(270, width - 48) / 2, 109, 0xFFB8BDC8);
        drawString(fontRendererObj, "PASSWORD", width / 2 - Math.min(270, width - 48) / 2, 151, 0xFFB8BDC8);
        emailField.drawTextBox();

        // Draw a masked copy without persisting or writing the password to disk.
        String raw = passwordField.getText();
        int cursor = passwordField.getCursorPosition();
        int selection = passwordField.getSelectionEnd();
        StringBuilder mask = new StringBuilder();
        for (int i = 0; i < raw.length(); i++) mask.append('*');
        passwordField.setText(mask.toString());
        passwordField.setCursorPosition(Math.min(cursor, mask.length()));
        passwordField.setSelectionPos(Math.min(selection, mask.length()));
        passwordField.drawTextBox();
        passwordField.setText(raw);
        passwordField.setCursorPosition(Math.min(cursor, raw.length()));
        passwordField.setSelectionPos(Math.min(selection, raw.length()));

        drawRect(left + 14, 232, right - 14, 233, 0xFF292D36);
        drawCenteredString(fontRendererObj, status, width / 2, 241, 0xFFB8BDC8);
        if (SupabaseCommunityService.isSignedIn()) {
            drawCenteredString(fontRendererObj, SupabaseCommunityService.getSignedInRankLabel(),
                    width / 2, 258, 0xFFFFD866);
        } else {
            drawCenteredString(fontRendererObj, "RANK NOT VERIFIED",
                    width / 2, 258, 0xFFFFD866);
        }
        if (SupabaseCommunityService.isSignedIn()) {
            drawCenteredString(fontRendererObj, "Signed in as " + SupabaseCommunityService.getSignedInEmail(),
                    width / 2, 275, 0xFF8DDBA4);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            mc.displayGuiScreen(parent);
            return;
        }
        if (emailField != null && emailField.isFocused()) {
            emailField.textboxKeyTyped(typedChar, keyCode);
        } else if (passwordField != null && passwordField.isFocused()) {
            passwordField.textboxKeyTyped(typedChar, keyCode);
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        emailField.mouseClicked(mouseX, mouseY, mouseButton);
        passwordField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button.id == BACK) {
            mc.displayGuiScreen(parent);
            return;
        }
        if (button.id == SIGN_OUT) {
            SupabaseCommunityService.signOut();
            status = "Signed out. Online rank access has been cleared.";
            return;
        }
        if (button.id == SIGN_IN) {
            if (busy) return;
            final String email = emailField.getText().trim();
            final String password = passwordField.getText();
            passwordField.setText("");
            busy = true;
            status = "Connecting to Alpha community...";
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        SupabaseCommunityService.signIn(email, password);
                        status = "Signed in. Community rank verified.";
                    } catch (Exception e) {
                        status = e.getMessage() == null ? "Sign-in failed." : e.getMessage();
                    } finally {
                        busy = false;
                    }
                }
            }, "AlphaClient-Community-Login").start();
        }
    }

    @Override
    public void updateScreen() {
        if (emailField != null) emailField.updateCursorCounter();
        if (passwordField != null) passwordField.updateCursorCounter();
        if (buttonList != null && !buttonList.isEmpty()) {
            buttonList.get(0).enabled = !busy;
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}