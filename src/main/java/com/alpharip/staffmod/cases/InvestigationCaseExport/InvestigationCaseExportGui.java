package com.alpharip.staffmod.cases.investigationcaseexport;

import net.minecraft.client.gui.*;
import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

public final class InvestigationCaseExportGui extends GuiScreen {
    private final GuiScreen parent;
    private final InvestigationCaseExport exporter;
    private final GuiTextField[] keys = new GuiTextField[6];
    private final GuiTextField[] values = new GuiTextField[6];
    private String output = "";
    private String notice = "";

    public InvestigationCaseExportGui(GuiScreen parent, InvestigationCaseExport exporter) {
        this.parent = parent;
        this.exporter = exporter;
    }

    public void initGui() {
        buttonList.clear();
        int x = Math.max(8, width / 2 - 195);
        for (int i = 0; i < keys.length; i++) {
            keys[i] = new GuiTextField(i + 1, fontRendererObj, x, 50 + i * 24, 125, 18);
            values[i] = new GuiTextField(i + 20, fontRendererObj, x + 130, 50 + i * 24, 260, 18);
            keys[i].setMaxStringLength(80);
            values[i].setMaxStringLength(180);
        }
        buttonList.add(new GuiButton(1, x, 205, 100, 20, "Generate"));
        buttonList.add(new GuiButton(2, x + 105, 205, 100, 20, "Save .txt"));
        buttonList.add(new GuiButton(3, x + 210, 205, 75, 20, "Clear"));
        buttonList.add(new GuiButton(4, x + 290, 205, 100, 20, "Close"));
    }

    private Map<String, String> collect() {
        Map<String, String> fields = new LinkedHashMap<String, String>();
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i].getText().trim();
            if (!key.isEmpty()) fields.put(key, values[i].getText());
        }
        return fields;
    }

    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 4) { mc.displayGuiScreen(parent); return; }
        if (button.id == 3) {
            for (int i = 0; i < keys.length; i++) { keys[i].setText(""); values[i].setText(""); }
            output = "";
            notice = "Fields cleared.";
        } else if (button.id == 1 || button.id == 2) {
            output = exporter.format(collect());
            if (button.id == 1) {
                notice = output.isEmpty() ? "Enter at least one field." : "Export text generated.";
            } else if (output.isEmpty()) {
                notice = "Enter at least one field before saving.";
            } else {
                File target = new File(mc.mcDataDir, "config/alphaclient/investigation-case-export.txt");
                File folder = target.getParentFile();
                if (folder != null && !folder.exists() && !folder.mkdirs()) {
                    notice = "Could not create export directory.";
                } else {
                    try (FileWriter writer = new FileWriter(target)) {
                        writer.write(output);
                        notice = "Saved: " + target.getName();
                    } catch (IOException exception) {
                        notice = "Export save failed: " + exception.getMessage();
                    }
                }
            }
        }
    }

    protected void keyTyped(char character, int keyCode) throws IOException {
        for (int i = 0; i < keys.length; i++) { keys[i].textboxKeyTyped(character, keyCode); values[i].textboxKeyTyped(character, keyCode); }
        super.keyTyped(character, keyCode);
    }

    protected void mouseClicked(int x, int y, int button) throws IOException {
        super.mouseClicked(x, y, button);
        for (int i = 0; i < keys.length; i++) { keys[i].mouseClicked(x, y, button); values[i].mouseClicked(x, y, button); }
    }

    public void updateScreen() {
        for (int i = 0; i < keys.length; i++) { keys[i].updateCursorCounter(); values[i].updateCursorCounter(); }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int x = Math.max(8, width / 2 - 195);
        drawCenteredString(fontRendererObj, "INVESTIGATION CASE EXPORT", width / 2, 18, 0xFFFFC83D);
        drawString(fontRendererObj, "Field key", x, 36, 0xFFFFFFFF);
        drawString(fontRendererObj, "Field value", x + 130, 36, 0xFFFFFFFF);
        for (int i = 0; i < keys.length; i++) {
            drawRect(x, 50 + i * 24, x + 126, 69 + i * 24, 0xAA20242C);
            drawRect(x + 130, 50 + i * 24, x + 391, 69 + i * 24, 0xAA20242C);
            keys[i].drawTextBox();
            values[i].drawTextBox();
        }
        drawString(fontRendererObj, "OUTPUT PREVIEW", x, 238, 0xFFFFC83D);
        int y = 252;
        for (String line : output.split("\\n")) {
            if (line.isEmpty()) continue;
            drawString(fontRendererObj, fontRendererObj.trimStringToWidth(line, width - 2 * x),
                    x, y, 0xFFE5E7EB);
            y += 12;
            if (y > height - 42) break;
        }
        if (!notice.isEmpty()) drawCenteredString(fontRendererObj, notice, width / 2, height - 25, 0xFFFFC83D);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public boolean doesGuiPauseGame() { return false; }
}