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
        emailField = new GuiTextField(10, fontRendererObj, center - 145, 145, 290, 22);
        emailField.setMaxStringLength(254);
        passwordField = new GuiTextField(11, fontRendererObj, center - 145, 190, 290, 22);
        passwordField.setMaxStringLength(256);
        buttonList.clear();
        buttonList.add(new AlphaButton(SIGN_IN, center - 145, 230, 140, 24, "SIGN IN"));
        buttonList.add(new AlphaButton(SIGN_OUT, center + 5, 230, 140, 24, "SIGN OUT"));
        buttonList.add(new AlphaButton(BACK, 14, height - 32, 100, 20, "BACK"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawGradientRect(0, 0, width, height, 0xFF17191F, 0xFF0B0C10);
        drawCenteredString(fontRendererObj, "ALPHA COMMUNITY", width / 2, 65, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "Sign in to verify your community rank", width / 2, 86, 0xFFE8E9ED);
        drawString(fontRendererObj, "Email", width / 2 - 145, 130, 0xFFB8BBC4);
        drawString(fontRendererObj, "Password", width / 2 - 145, 175, 0xFFB8BBC4);
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

        drawCenteredString(fontRendererObj, status, width / 2, 276, 0xFFB8BBC4);
        drawCenteredString(fontRendererObj, "Access: " + PermissionService.getStatusText(),
                width / 2, 294, 0xFFFFD866);
        if (SupabaseCommunityService.isSignedIn()) {
            drawCenteredString(fontRendererObj, "Signed in as " + SupabaseCommunityService.getSignedInEmail(),
                    width / 2, 313, 0xFF8DDBA4);
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