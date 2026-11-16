package com.alpharip.staffmod.punishmentcommandpreflight;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

/** Final review screen. Evidence is a local note and is not sent to the server. */
public final class PunishmentCommandPreflightGui extends GuiScreen {
    private static final int CONFIRM = 1, CANCEL = 2;
    private static final int ACCENT = 0xFFFFC83D;
    private final GuiScreen parent;
    private final PunishmentCommandPreflight preflight;
    private final PunishmentCommandDraft draft;
    private GuiTextField evidenceField;
    private int left, top, panelWidth, panelHeight;

    public PunishmentCommandPreflightGui(GuiScreen parent, PunishmentCommandPreflight preflight, PunishmentCommandDraft draft) {
        this.parent = parent;
        this.preflight = preflight;
        this.draft = draft;
    }

    @Override
    public void initGui() {
        panelWidth = Math.min(500, Math.max(300, width - 24));
        panelHeight = Math.min(300, Math.max(240, height - 24));
        left = (width - panelWidth) / 2;
        top = (height - panelHeight) / 2;
        evidenceField = new GuiTextField(0, fontRendererObj, left + 16, top + 190, panelWidth - 32, 20);
        evidenceField.setMaxStringLength(240);
        buttonList.clear();
        buttonList.add(new GuiButton(CANCEL, left + 16, top + panelHeight - 34, 100, 20, "CANCEL"));
        buttonList.add(new GuiButton(CONFIRM, left + panelWidth - 126, top + panelHeight - 34, 110, 20, "CONFIRM & SEND"));
        Keyboard.enableRepeatEvents(true);
    }

    @Override
    public void onGuiClosed() { Keyboard.enableRepeatEvents(false); }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            preflight.cancel();
            mc.displayGuiScreen(parent);
            return;
        }
        evidenceField.textboxKeyTyped(typedChar, keyCode);
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        evidenceField.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button.id == CANCEL) {
            preflight.cancel();
            mc.displayGuiScreen(parent);
        } else if (button.id == CONFIRM) {
            preflight.confirm(draft.withEvidence(evidenceField.getText().trim()));
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawRect(left, top, left + panelWidth, top + panelHeight, 0xF00B0D12);
        drawRect(left, top, left + panelWidth, top + 2, ACCENT);
        drawString(fontRendererObj, "PUNISHMENT COMMAND REVIEW", left + 16, top + 14, 0xFFF2F3F5);
        drawString(fontRendererObj, "Command: /" + draft.getCommandName(), left + 16, top + 40, ACCENT);
        drawString(fontRendererObj, "Target: " + draft.getTarget(), left + 16, top + 62, 0xFFE5E7EB);
        drawString(fontRendererObj, "Duration: " + draft.getDuration(), left + 16, top + 80, 0xFFE5E7EB);
        drawString(fontRendererObj, "Reason:", left + 16, top + 101, 0xFF9AA3B2);
        drawSplitString(draft.getReason(), left + 16, top + 115, panelWidth - 32, 0xFFE5E7EB);
        drawString(fontRendererObj, "Evidence note (local only, optional):", left + 16, top + 174, 0xFF9AA3B2);
        evidenceField.drawTextBox();
        drawString(fontRendererObj, "Original command is sent unchanged only after confirmation.",
                left + 16, top + panelHeight - 52, 0xFF9AA3B2);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawSplitString(String text, int x, int y, int maxWidth, int color) {
        for (String line : fontRendererObj.listFormattedStringToWidth(text, maxWidth)) {
            drawString(fontRendererObj, line, x, y, color);
            y += fontRendererObj.FONT_HEIGHT + 2;
        }
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
