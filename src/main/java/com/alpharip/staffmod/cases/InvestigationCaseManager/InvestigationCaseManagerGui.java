package com.alpharip.staffmod.cases.investigationcasemanager;

import net.minecraft.client.gui.*;
import java.io.IOException;
import java.util.List;

public final class InvestigationCaseManagerGui extends GuiScreen {
    private final GuiScreen parent;
    private final InvestigationCaseManager module;
    private GuiTextField title;
    private GuiTextField summary;
    private long selectedCaseId = -1L;
    private String notice = "";

    public InvestigationCaseManagerGui(GuiScreen parent, InvestigationCaseManager module) {
        this.parent = parent;
        this.module = module;
    }

    public void initGui() {
        buttonList.clear();
        int x = Math.max(8, width / 2 - 190);
        title = new GuiTextField(1, fontRendererObj, x, 48, 380, 18);
        summary = new GuiTextField(2, fontRendererObj, x, 88, 380, 18);
        title.setMaxStringLength(100);
        summary.setMaxStringLength(240);
        buttonList.add(new GuiButton(1, x, 120, 90, 20, "Create case"));
        buttonList.add(new GuiButton(2, x + 95, 120, 75, 20, "Save"));
        buttonList.add(new GuiButton(3, x + 175, 120, 75, 20, "Reload"));
        buttonList.add(new GuiButton(4, x + 255, 120, 125, 20, "Remove selected"));
        buttonList.add(new GuiButton(5, x, height - 28, 90, 20, "Close"));
    }

    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 5) { mc.displayGuiScreen(parent); return; }
        if (button.id == 1) {
            try {
                module.create(title.getText(), summary.getText(), System.currentTimeMillis());
                title.setText("");
                summary.setText("");
                notice = "Case created locally.";
            } catch (IllegalArgumentException exception) {
                notice = "Enter a case title.";
            }
        } else if (button.id == 2) {
            notice = module.save() ? "Saved locally." : "Could not save local cases.";
        } else if (button.id == 3) {
            module.load();
            selectedCaseId = -1L;
            notice = "Local cases reloaded.";
        } else if (button.id == 4) {
            notice = selectedCaseId > 0L && module.remove(selectedCaseId)
                    ? "Case removed." : "Select a case first.";
            selectedCaseId = -1L;
        }
    }

    protected void keyTyped(char character, int keyCode) throws IOException {
        title.textboxKeyTyped(character, keyCode);
        summary.textboxKeyTyped(character, keyCode);
        super.keyTyped(character, keyCode);
    }

    protected void mouseClicked(int x, int y, int button) throws IOException {
        super.mouseClicked(x, y, button);
        title.mouseClicked(x, y, button);
        summary.mouseClicked(x, y, button);
        int listTop = 165;
        if (x >= Math.max(8, width / 2 - 190) && y >= listTop) {
            int row = (y - listTop) / 14;
            List<InvestigationCases.CaseEntry> entries = module.getCases().getCases();
            if (row >= 0 && row < entries.size()) selectedCaseId = entries.get(row).getId();
        }
    }

    public void updateScreen() { title.updateCursorCounter(); summary.updateCursorCounter(); }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int x = Math.max(8, width / 2 - 190);
        drawCenteredString(fontRendererObj, "INVESTIGATION CASE MANAGER", width / 2, 18, 0xFFFFC83D);
        drawString(fontRendererObj, "Case title", x, 36, 0xFFFFFFFF);
        drawRect(x, 48, x + 381, 67, 0xAA20242C); title.drawTextBox();
        drawString(fontRendererObj, "Summary", x, 76, 0xFFFFFFFF);
        drawRect(x, 88, x + 381, 107, 0xAA20242C); summary.drawTextBox();
        int y = 165;
        for (InvestigationCases.CaseEntry entry : module.getCases().getCases()) {
            String prefix = entry.getId() == selectedCaseId ? "> " : "  ";
            String line = prefix + "#" + entry.getId() + "  " + entry.getTitle() + "  -  " + entry.getSummary();
            drawString(fontRendererObj, fontRendererObj.trimStringToWidth(line, width - 24),
                    x, y, 0xFFE5E7EB);
            y += 14;
            if (y > height - 48) break;
        }
        if (!notice.isEmpty()) drawCenteredString(fontRendererObj, notice, width / 2, height - 43, 0xFFFFC83D);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public boolean doesGuiPauseGame() { return false; }
}