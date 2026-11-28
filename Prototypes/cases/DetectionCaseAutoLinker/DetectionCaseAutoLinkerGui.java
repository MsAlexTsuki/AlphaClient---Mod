package com.alpharip.staffmod.cases.detectioncaseautolinker;
import net.minecraft.client.gui.*;
import java.io.IOException;
import java.util.*;

public final class DetectionCaseAutoLinkerGui extends GuiScreen {
    private final GuiScreen parent;
    private final DetectionCaseAutoLinker module;
    private GuiTextField id, label, key, value;
    private String notice = "";

    public DetectionCaseAutoLinkerGui(GuiScreen parent, DetectionCaseAutoLinker module) {
        this.parent = parent;
        this.module = module;
    }

    public void initGui() {
        buttonList.clear();
        int x = Math.max(8, width / 2 - 205);
        id = new GuiTextField(1, fontRendererObj, x, 45, 90, 18);
        label = new GuiTextField(2, fontRendererObj, x + 95, 45, 310, 18);
        key = new GuiTextField(3, fontRendererObj, x, 88, 120, 18);
        value = new GuiTextField(4, fontRendererObj, x + 125, 88, 180, 18);
        for (GuiTextField field : fields()) field.setMaxStringLength(100);

        buttonList.add(new GuiButton(1, x + 310, 87, 95, 20, "Add case"));
        buttonList.add(new GuiButton(2, x, height - 28, 90, 20, "Save"));
        buttonList.add(new GuiButton(3, x + 95, height - 28, 90, 20, "Close"));
        buttonList.add(new GuiButton(4, x + 190, height - 28, 215, 20, "Use key for matching"));
    }

    private List<GuiTextField> fields() {
        return Arrays.asList(id, label, key, value);
    }

    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 3) {
            mc.displayGuiScreen(parent);
            return;
        }
        if (button.id == 1) {
            String attributeKey = key.getText().trim();
            String attributeValue = value.getText().trim();
            if (attributeKey.isEmpty() || attributeValue.isEmpty()) {
                notice = "Enter an attribute key and value.";
                return;
            }
            try {
                Map<String, String> attributes = new LinkedHashMap<String, String>();
                attributes.put(attributeKey, attributeValue);
                if (!module.getService().addCase(new DetectionRecord(id.getText(), label.getText(), attributes))) {
                    notice = "Enter a unique case ID and label.";
                    return;
                }
                notice = "Case added.";
                id.setText("");
                label.setText("");
            } catch (IllegalArgumentException exception) {
                notice = "Enter a valid case ID and label.";
            }
        }
        if (button.id == 4) {
            String attributeKey = key.getText().trim();
            if (attributeKey.isEmpty()) {
                notice = "Enter an attribute key before configuring matching.";
                return;
            }
            Set<String> requiredKeys = new LinkedHashSet<String>(module.getService().getRequiredKeys());
            requiredKeys.add(attributeKey);
            module.getService().setRequiredKeys(requiredKeys);
            notice = "Matching key configured; cases re-evaluated.";
        }
        if (button.id == 2) notice = module.save() ? "Saved locally." : "Save failed.";
    }

    protected void keyTyped(char character, int keyCode) throws IOException {
        for (GuiTextField field : fields()) field.textboxKeyTyped(character, keyCode);
        super.keyTyped(character, keyCode);
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        for (GuiTextField field : fields()) field.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public void updateScreen() {
        for (GuiTextField field : fields()) field.updateCursorCounter();
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int x = Math.max(8, width / 2 - 205);
        drawCenteredString(fontRendererObj, "DETECTION CASE AUTO-LINKER", width / 2, 18, 0xFFFFC83D);
        drawString(fontRendererObj, "Case ID / label", x, 32, 0xFFFFFF);
        drawRect(x, 45, x + 91, 64, 0xAA20242C);
        drawRect(x + 95, 45, x + 406, 64, 0xAA20242C);
        id.drawTextBox();
        label.drawTextBox();
        drawString(fontRendererObj, "Matching attribute key / value", x, 75, 0xFFFFFF);
        drawRect(x, 88, x + 121, 107, 0xAA20242C);
        drawRect(x + 125, 88, x + 306, 107, 0xAA20242C);
        key.drawTextBox();
        value.drawTextBox();

        int y = 125;
        drawString(fontRendererObj, "Configured exact-match keys: " + module.getService().getRequiredKeys(), x, y, 0xFFAAAAAA);
        y += 20;
        drawString(fontRendererObj, "AUTO-LINKED CASE PAIRS", x, y, 0xFFFFC83D);
        y += 15;
        for (DetectionLink link : module.getService().getLinks()) {
            drawString(fontRendererObj, link.getFirstId() + " <-> " + link.getSecondId() + " | " + link.getMatchedKeys(), x, y, 0xFFE5E7EB);
            y += 14;
            if (y > height - 50) break;
        }
        if (module.getService().getLinks().isEmpty()) {
            drawString(fontRendererObj, "No matches; configure a key before automatic linking.", x, y, 0xFFAAAAAA);
        }
        if (!notice.isEmpty()) drawCenteredString(fontRendererObj, notice, width / 2, height - 43, 0xFFFFC83D);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public boolean doesGuiPauseGame() {
        return false;
    }
}