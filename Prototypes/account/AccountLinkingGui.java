package com.alpharip.staffmod.account;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

/** Local-only account-reference management preview for Minecraft 1.8.9. */
public final class AccountLinkingGui extends GuiScreen {
    private final GuiScreen parent;
    private final AccountLinkingService service;
    private GuiTextField providerField;
    private GuiTextField identifierField;
    private GuiTextField displayNameField;
    private String feedback = "";

    public AccountLinkingGui(GuiScreen parent, AccountLinkingService service) {
        if (service == null) throw new IllegalArgumentException("service is required");
        this.parent = parent;
        this.service = service;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int center = width / 2;
        providerField = new GuiTextField(0, fontRendererObj, center - 100, 42, 200, 18);
        identifierField = new GuiTextField(1, fontRendererObj, center - 100, 66, 200, 18);
        displayNameField = new GuiTextField(2, fontRendererObj, center - 100, 90, 200, 18);
        providerField.setMaxStringLength(32);
        identifierField.setMaxStringLength(128);
        displayNameField.setMaxStringLength(32);
        buttonList.add(new GuiButton(0, center - 100, 114, 200, 20, "Add Local Entry"));
        buttonList.add(new GuiButton(1, center - 100, height - 74, 96, 20, "Disconnect"));
        buttonList.add(new GuiButton(2, center + 4, height - 74, 96, 20, "Remove"));
        buttonList.add(new GuiButton(3, center - 100, height - 50, 96, 20,
                service.getAccountLinking().getSettings().isEnabled() ? "Disable" : "Enable"));
        buttonList.add(new GuiButton(4, center + 4, height - 50, 96, 20, "Back"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            try {
                service.createLocalEntry(providerField.getText(), identifierField.getText(), displayNameField.getText());
                feedback = "Local entry added. Account ownership was not verified.";
                providerField.setText("");
                identifierField.setText("");
                displayNameField.setText("");
            } catch (IllegalArgumentException exception) {
                feedback = exception.getMessage();
            }
        } else if (button.id == 1) {
            feedback = service.disconnectLocalEntry(selectedId()) ? "Local entry disconnected." : "Select an active local entry.";
        } else if (button.id == 2) {
            feedback = service.removeLocalEntry(selectedId()) ? "Local entry removed." : "Select an entry first.";
        } else if (button.id == 3) {
            AccountLinkingSettings settings = service.getAccountLinking().getSettings();
            settings.setEnabled(!settings.isEnabled());
            button.displayString = settings.isEnabled() ? "Disable" : "Enable";
            feedback = "Prototype " + (settings.isEnabled() ? "enabled." : "disabled.");
        } else if (button.id == 4) {
            mc.displayGuiScreen(parent);
        }
    }

    private UUID selectedId() {
        return service.getAccountLinking().getSettings().getSelectedEntryId();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        providerField.mouseClicked(mouseX, mouseY, mouseButton);
        identifierField.mouseClicked(mouseX, mouseY, mouseButton);
        displayNameField.mouseClicked(mouseX, mouseY, mouseButton);
        int firstRow = 145;
        List<AccountLink> entries = service.getAccountLinking().getRegistry().getEntries();
        for (int i = 0; i < entries.size(); i++) {
            int rowY = firstRow + i * 13;
            if (mouseY >= rowY && mouseY < rowY + 12) {
                service.select(entries.get(i).getId());
                break;
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (providerField.textboxKeyTyped(typedChar, keyCode)
                || identifierField.textboxKeyTyped(typedChar, keyCode)
                || displayNameField.textboxKeyTyped(typedChar, keyCode)) return;
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "Account Linking", width / 2, 16, 0xFFFFFF);
        drawCenteredString(fontRendererObj, "Local prototype only - no authentication or account verification", width / 2, 29, 0xFFCC66);
        drawString(fontRendererObj, "Provider", width / 2 - 100, 32, 0xAAAAAA);
        drawString(fontRendererObj, "Account identifier", width / 2 - 100, 56, 0xAAAAAA);
        drawString(fontRendererObj, "Display name", width / 2 - 100, 80, 0xAAAAAA);
        providerField.drawTextBox();
        identifierField.drawTextBox();
        displayNameField.drawTextBox();
        drawString(fontRendererObj, "Local entries (click a row to select):", width / 2 - 100, 137, 0xFFFFFF);
        List<AccountLink> entries = service.getAccountLinking().getRegistry().getEntries();
        int y = 151;
        for (AccountLink entry : entries) {
            String marker = entry.getId().equals(selectedId()) ? "> " : "  ";
            String line = marker + entry.getDisplayName() + " [" + entry.getProvider() + "] - "
                    + (entry.isLocallyConnected() ? "LOCAL" : "DISCONNECTED");
            drawString(fontRendererObj, line, width / 2 - 100, y, 0xDDDDDD);
            y += 13;
            if (y > height - 82) break;
        }
        if (!feedback.isEmpty()) drawCenteredString(fontRendererObj, feedback, width / 2, height - 91, 0xFFCC66);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
