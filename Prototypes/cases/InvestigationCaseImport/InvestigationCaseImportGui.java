package com.alpharip.staffmod.cases.investigationcaseimport;

import net.minecraft.client.gui.*;
import java.io.IOException;
import java.util.Map;

public final class InvestigationCaseImportGui extends GuiScreen {
    private final GuiScreen parent;
    private final InvestigationCaseImport importer;
    private final GuiTextField[] lines = new GuiTextField[8];
    private String preview = "";
    private String notice = "";

    public InvestigationCaseImportGui(GuiScreen parent, InvestigationCaseImport importer) {
        this.parent = parent;
        this.importer = importer;
    }

    public void initGui() {
        buttonList.clear();
        int x = Math.max(8, width / 2 - 180);
        for (int i = 0; i < lines.length; i++) {
            lines[i] = new GuiTextField(i + 1, fontRendererObj, x, 48 + i * 22, 360, 18);
            lines[i].setMaxStringLength(180);
        }
        buttonList.add(new GuiButton(1, x, 235, 100, 20, "Preview import"));
        buttonList.add(new GuiButton(2, x + 105, 235, 75, 20, "Clear"));
        buttonList.add(new GuiButton(3, x + 185, 235, 75, 20, "Close"));
    }

    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 3) { mc.displayGuiScreen(parent); return; }
        if (button.id == 2) {
            for (GuiTextField line : lines) line.setText("");
            preview = "";
            notice = "Input cleared.";
        } else if (button.id == 1) {
            StringBuilder input = new StringBuilder();
            for (GuiTextField line : lines) {
                if (!line.getText().trim().isEmpty()) input.append(line.getText()).append('\n');
            }
            Map<String, String> parsed = importer.parse(input.toString());
            preview = importer.formatPreview(parsed);
            notice = parsed.isEmpty() ? "No valid key:value fields found."
                    : parsed.size() + " field(s) parsed. Nothing was applied to a case.";
        }
    }

    protected void keyTyped(char character, int keyCode) throws IOException {
        for (GuiTextField line : lines) line.textboxKeyTyped(character, keyCode);
        super.keyTyped(character, keyCode);
    }

    protected void mouseClicked(int x, int y, int button) throws IOException {
        super.mouseClicked(x, y, button);
        for (GuiTextField line : lines) line.mouseClicked(x, y, button);
    }

    public void updateScreen() { for (GuiTextField line : lines) line.updateCursorCounter(); }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int x = Math.max(8, width / 2 - 180);
        drawCenteredString(fontRendererObj, "INVESTIGATION CASE IMPORT", width / 2, 18, 0xFFFFC83D);
        drawString(fontRendererObj, "Enter one key:value pair per line", x, 34, 0xFFFFFFFF);
        for (int i = 0; i < lines.length; i++) {
            drawRect(x, 48 + i * 22, x + 361, 67 + i * 22, 0xAA20242C);
            lines[i].drawTextBox();
        }
        drawString(fontRendererObj, "PARSED PREVIEW", x, 270, 0xFFFFC83D);
        int y = 284;
        for (String line : preview.split("\\n")) {
            if (line.isEmpty()) continue;
            drawString(fontRendererObj, fontRendererObj.trimStringToWidth(line, width - 2 * x),
                    x, y, 0xFFE5E7EB);
            y += 12;
            if (y > height - 48) break;
        }
        if (!notice.isEmpty()) drawCenteredString(fontRendererObj, notice, width / 2, height - 35, 0xFFFFC83D);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public boolean doesGuiPauseGame() { return false; }
}