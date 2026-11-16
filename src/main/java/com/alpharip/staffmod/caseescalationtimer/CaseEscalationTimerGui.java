package com.alpharip.staffmod.caseescalationtimer;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

import java.io.IOException;
import java.util.List;

/** Standalone case list and timer management screen for the prototype. */
public final class CaseEscalationTimerGui extends GuiScreen {
    private static final int ADD = 1, SAVE = 2, BACK = 3, ROW_BASE = 100;
    private final GuiScreen parent;
    private final CaseEscalationTimer module;
    private GuiTextField idField, titleField, hoursField;
    private String message = "";

    public CaseEscalationTimerGui(GuiScreen parent, CaseEscalationTimer module) {
        this.parent = parent;
        this.module = module;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int left = Math.max(8, (width - 440) / 2);
        idField = new GuiTextField(10, fontRendererObj, left + 10, 48, 90, 18);
        titleField = new GuiTextField(11, fontRendererObj, left + 106, 48, 170, 18);
        hoursField = new GuiTextField(12, fontRendererObj, left + 282, 48, 55, 18);
        idField.setMaxStringLength(32);
        titleField.setMaxStringLength(64);
        hoursField.setMaxStringLength(8);
        buttonList.add(new GuiButton(ADD, left + 342, 47, 80, 20, "Add case"));
        buttonList.add(new GuiButton(SAVE, left + 10, height - 30, 70, 20, "Save"));
        buttonList.add(new GuiButton(BACK, left + 86, height - 30, 70, 20, "Back"));
        rebuildRows();
    }

    private void rebuildRows() {
        for (int i = buttonList.size() - 1; i >= 0; i--)
            if (buttonList.get(i).id >= ROW_BASE) buttonList.remove(i);
        List<EscalationCase> cases = module.getTracker().getCases();
        int left = Math.max(8, (width - 440) / 2);
        int row = 0;
        for (EscalationCase item : cases) {
            int y = 91 + row * 22;
            if (y > height - 42) break;
            buttonList.add(new GuiButton(ROW_BASE + row, left + 342, y, 80, 18,
                    item.getState() == EscalationCaseState.OPEN ? "Pause" :
                            item.getState() == EscalationCaseState.PAUSED ? "Resume" : "Resolved"));
            row++;
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == BACK) { mc.displayGuiScreen(parent); return; }
        if (button.id == SAVE) { message = module.save() ? "Saved locally." : "Could not save."; return; }
        if (button.id == ADD) {
            try {
                long hours = Long.parseLong(hoursField.getText().trim());
                if (hours <= 0L || hours > 87600L) throw new NumberFormatException();
                long now = System.currentTimeMillis();
                long duration = hours * 60L * 60L * 1000L;
                EscalationCase item = new EscalationCase(idField.getText(), titleField.getText(), "",
                        now, EscalationDeadline.createDeadline(now, duration),
                        EscalationCaseState.OPEN, 0L, 0L);
                if (!module.getTracker().add(item)) { message = "Case ID already exists."; return; }
                idField.setText(""); titleField.setText(""); hoursField.setText("");
                message = "Case added. Save to persist.";
                rebuildRows();
            } catch (RuntimeException exception) {
                message = "Enter a unique ID, title, and positive deadline in hours.";
            }
            return;
        }
        if (button.id >= ROW_BASE) {
            int index = button.id - ROW_BASE;
            List<EscalationCase> cases = module.getTracker().getCases();
            if (index < 0 || index >= cases.size()) return;
            EscalationCase item = cases.get(index);
            long now = System.currentTimeMillis();
            if (item.getState() == EscalationCaseState.OPEN) module.getTracker().replace(item.pause(now));
            else if (item.getState() == EscalationCaseState.PAUSED) module.getTracker().replace(item.resume(now));
            else return;
            message = "Case timer updated. Save to persist.";
            rebuildRows();
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        idField.textboxKeyTyped(typedChar, keyCode);
        titleField.textboxKeyTyped(typedChar, keyCode);
        hoursField.textboxKeyTyped(typedChar, keyCode);
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        idField.mouseClicked(mouseX, mouseY, mouseButton);
        titleField.mouseClicked(mouseX, mouseY, mouseButton);
        hoursField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void updateScreen() {
        idField.updateCursorCounter(); titleField.updateCursorCounter(); hoursField.updateCursorCounter();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int left = Math.max(8, (width - 440) / 2);
        drawCenteredString(fontRendererObj, "CASE ESCALATION TIMER", width / 2, 18, 0xFFFFFF);
        drawString(fontRendererObj, "Case ID", left + 10, 36, 0xBBBBBB);
        drawString(fontRendererObj, "Title", left + 106, 36, 0xBBBBBB);
        drawString(fontRendererObj, "Deadline (hours)", left + 282, 36, 0xBBBBBB);
        drawRect(left + 9, 47, left + 101, 67, 0xAA20242C);
        drawRect(left + 105, 47, left + 277, 67, 0xAA20242C);
        drawRect(left + 281, 47, left + 338, 67, 0xAA20242C);
        idField.drawTextBox(); titleField.drawTextBox(); hoursField.drawTextBox();
        List<EscalationCase> cases = module.getTracker().getCases();
        int row = 0;
        for (EscalationCase item : cases) {
            int y = 91 + row * 22;
            if (y > height - 42) break;
            EscalationStatus status = EscalationFormatter.status(item, module.getPolicy(), System.currentTimeMillis());
            long remaining = EscalationDeadline.remainingMillis(item, System.currentTimeMillis());
            String line = item.getId() + " - " + item.getTitle() + " | " + status
                    + " | " + (status == EscalationStatus.NO_DEADLINE ? "No deadline" : EscalationFormatter.remaining(remaining));
            drawString(fontRendererObj, fontRendererObj.trimStringToWidth(line, 330), left + 10, y + 5, 0xE0E0E0);
            row++;
        }
        if (!message.isEmpty()) drawCenteredString(fontRendererObj, message, width / 2, height - 42, 0xFFD36A);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
