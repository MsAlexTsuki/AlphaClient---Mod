// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.Session;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.io.IOException;
import java.util.List;

/**
 * In-game Microsoft account switcher.
 * Account authentication is performed by the running Alpha Launcher.
 */
public final class AccountInfoGui extends AlphaGuiScreen {
    private static final int BACK = 0;
    private static final int ACCOUNT_BUTTON_START = 10;

    private final GuiScreen parent;
    private List<AccountSwitchClient.SavedAccount> accounts;
    private boolean switching;
    private String statusMessage;
    private int panelTop;

    public AccountInfoGui(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        accounts = AccountSwitchClient.getSavedAccounts();

        int rowHeight = 27;
        int panelHeight = 106 + Math.max(1, accounts.size()) * rowHeight;
        panelTop = Math.max(18, (height - panelHeight) / 2);

        int buttonWidth = Math.min(340, Math.max(190, width - 48));
        int buttonLeft = (width - buttonWidth) / 2;
        int firstButtonY = panelTop + 66;

        for (int i = 0; i < accounts.size(); i++) {
            AccountSwitchClient.SavedAccount account = accounts.get(i);
            String label = account.getName();
            if (isCurrentAccount(account)) label = label + "  [CURRENT]";
            buttonList.add(new AlphaButton(ACCOUNT_BUTTON_START + i, buttonLeft,
                    firstButtonY + i * rowHeight, buttonWidth, 23, label, !switching));
        }

        buttonList.add(new AlphaButton(BACK, width / 2 - 72,
                Math.min(height - 34, firstButtonY + Math.max(1, accounts.size()) * rowHeight + 8),
                144, 22, "BACK", true));
    }

    private boolean isCurrentAccount(AccountSwitchClient.SavedAccount account) {
        Minecraft minecraft = Minecraft.getMinecraft();
        return minecraft != null && minecraft.getSession() != null
                && account.getName().equalsIgnoreCase(minecraft.getSession().getUsername());
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        int panelWidth = Math.min(390, width - 24);
        int left = (width - panelWidth) / 2;
        int bottom = Math.min(height - 12, panelTop + 66 + Math.max(1, accounts.size()) * 27 + 43);
        drawRect(left, panelTop, left + panelWidth, bottom, 0xE814171D);
        drawRect(left, panelTop, left + panelWidth, panelTop + 2, 0xFFFFD866);
        drawRect(left, panelTop, left + 3, bottom, 0xFFFFD866);

        drawCenteredString(fontRendererObj, "ACCOUNT SWITCHER", width / 2, panelTop + 14, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "Select a saved Microsoft account", width / 2,
                panelTop + 37, 0xFFF0F1F4);

        if (accounts.isEmpty()) {
            drawCenteredString(fontRendererObj, "No saved accounts were provided by Alpha Launcher.",
                    width / 2, panelTop + 83, 0xFFADB3C0);
            drawCenteredString(fontRendererObj, "Add an account in the Launcher, then restart the game.",
                    width / 2, panelTop + 97, 0xFFADB3C0);
        }

        if (statusMessage != null) {
            drawCenteredString(fontRendererObj, statusMessage, width / 2,
                    Math.min(height - 48, bottom - 17), switching ? 0xFFFFD866 : 0xFFE4E8EF);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;

        if (button.id == BACK) {
            mc.displayGuiScreen(parent);
            return;
        }

        int accountIndex = button.id - ACCOUNT_BUTTON_START;
        if (accountIndex < 0 || accountIndex >= accounts.size() || switching) return;

        final AccountSwitchClient.SavedAccount selected = accounts.get(accountIndex);
        if (isCurrentAccount(selected)) {
            statusMessage = "This account is already active.";
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.theWorld != null || minecraft.thePlayer != null) {
            statusMessage = "Disconnect and return to the main menu before switching accounts.";
            return;
        }

        switching = true;
        statusMessage = "Waiting for Microsoft sign-in...";
        initGui();

        Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final AccountSwitchClient.SwitchResult result =
                            AccountSwitchClient.switchAccount(selected.getEmail());

                    minecraft.addScheduledTask(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Session newSession = new Session(
                                        result.getName(),
                                        result.getUuid(),
                                        result.getAccessToken(),
                                        "mojang"
                                );
                                ReflectionHelper.setPrivateValue(Minecraft.class, minecraft, newSession,
                                        "session", "field_71449_j");
                                statusMessage = "Switched to " + result.getName() + ".";
                            } catch (Exception exception) {
                                statusMessage = "The Minecraft session could not be updated: "
                                        + safeMessage(exception);
                            }
                            switching = false;
                            initGui();
                        }
                    });
                } catch (final Exception exception) {
                    minecraft.addScheduledTask(new Runnable() {
                        @Override
                        public void run() {
                            switching = false;
                            statusMessage = safeMessage(exception);
                            initGui();
                        }
                    });
                }
            }
        }, "AlphaClient-AccountSwitch");
        worker.setDaemon(true);
        worker.start();
    }

    private static String safeMessage(Exception exception) {
        String message = exception.getMessage();
        return message == null || message.trim().isEmpty()
                ? "Account switching failed. Check Alpha Launcher and try again."
                : message;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1 && !switching) {
            mc.displayGuiScreen(parent);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
