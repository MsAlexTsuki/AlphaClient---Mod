package com.alpharip.staffmod.actingrankassignment;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Minecraft 1.8.9 preview screen for creating and reviewing local acting-rank records. */
public final class ActingRankAssignmentGui extends GuiScreen {
    private final GuiScreen parent;
    private final ActingRankService service;
    private final String currentStaffName;
    private GuiTextField staffField;
    private GuiTextField currentRankField;
    private GuiTextField actingRankField;
    private GuiTextField durationField;
    private GuiTextField reasonField;
    private int selectedIndex;
    private int scrollOffset;
    private String message = "";

    public ActingRankAssignmentGui(GuiScreen parent, ActingRankService service, String currentStaffName) {
        if (service == null || currentStaffName == null || currentStaffName.trim().isEmpty()) {
            throw new IllegalArgumentException("Service and current staff name are required.");
        }
        this.parent = parent;
        this.service = service;
        this.currentStaffName = currentStaffName.trim();
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int left = Math.max(8, width / 2 - 190);
        int fieldWidth = Math.max(70, Math.min(150, (width - 40) / 4));
        int y = 58;
        staffField = field(10, left, y, fieldWidth, 18, 32);
        currentRankField = field(11, left + fieldWidth + 6, y, fieldWidth, 18, 32);
        actingRankField = field(12, left + (fieldWidth + 6) * 2, y, fieldWidth, 18, 32);
        durationField = field(13, left + (fieldWidth + 6) * 3, y, 48, 18, 6);
        reasonField = field(14, left, y + 38, Math.min(250, width - 100), 18, 120);
        buttonList.add(new GuiButton(0, left + Math.min(250, width - 100) + 8, y + 37, 74, 20, "Assign"));
        buttonList.add(new GuiButton(1, width / 2 - 80, height - 29, 74, 20, "Revoke"));
        buttonList.add(new GuiButton(2, width / 2 + 6, height - 29, 74, 20, "Back"));
    }

    private GuiTextField field(int id, int x, int y, int w, int h, int max) {
        GuiTextField field = new GuiTextField(id, fontRendererObj, x, y, w, h);
        field.setMaxStringLength(max);
        return field;
    }

    @Override
    public void updateScreen() {
        staffField.updateCursorCounter();
        currentRankField.updateCursorCounter();
        actingRankField.updateCursorCounter();
        durationField.updateCursorCounter();
        reasonField.updateCursorCounter();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (staffField.textboxKeyTyped(typedChar, keyCode)
                || currentRankField.textboxKeyTyped(typedChar, keyCode)
                || actingRankField.textboxKeyTyped(typedChar, keyCode)
                || durationField.textboxKeyTyped(typedChar, keyCode)
                || reasonField.textboxKeyTyped(typedChar, keyCode)) return;
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        staffField.mouseClicked(mouseX, mouseY, mouseButton);
        currentRankField.mouseClicked(mouseX, mouseY, mouseButton);
        actingRankField.mouseClicked(mouseX, mouseY, mouseButton);
        durationField.mouseClicked(mouseX, mouseY, mouseButton);
        reasonField.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == 0 && mouseY >= 142 && mouseY < height - 36) {
            int row = (mouseY - 142) / 32;
            selectedIndex = scrollOffset + row;
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            try {
                long hours = Long.parseLong(durationField.getText().trim());
                if (hours < 1L || hours > 8760L) throw new NumberFormatException();
                service.create(staffField.getText(), currentRankField.getText(),
                        actingRankField.getText(), currentStaffName, reasonField.getText(),
                        hours * 60L * 60L * 1000L, System.currentTimeMillis());
                message = "Acting-rank record created.";
                staffField.setText("");
                currentRankField.setText("");
                actingRankField.setText("");
                reasonField.setText("");
                selectedIndex = 0;
            } catch (RuntimeException ex) {
                message = ex.getMessage() == null ? "Invalid assignment details." : ex.getMessage();
            }
        } else if (button.id == 1) {
            List<ActingRankEntry> entries = getEntries();
            if (selectedIndex >= 0 && selectedIndex < entries.size()) {
                boolean revoked = service.revoke(entries.get(selectedIndex).getId(),
                        currentStaffName, System.currentTimeMillis());
                message = revoked ? "Assignment revoked." : "Selected assignment cannot be revoked.";
            }
        } else if (button.id == 2) {
            mc.displayGuiScreen(parent);
        }
    }

    private List<ActingRankEntry> getEntries() {
        return service.search("", service.getSettings().isShowExpired(), System.currentTimeMillis());
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = org.lwjgl.input.Mouse.getEventDWheel();
        if (wheel != 0) {
            int max = Math.max(0, getEntries().size() - Math.max(1, (height - 170) / 32));
            scrollOffset = Math.max(0, Math.min(max, scrollOffset + (wheel > 0 ? -1 : 1)));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "ACTING RANK ASSIGNMENT", width / 2, 14, 0xFFFFFFFF);
        drawCenteredString(fontRendererObj, "Temporary tracking only - does not grant rank permissions",
                width / 2, 29, 0xFFFFD866);
        int left = Math.max(8, width / 2 - 190);
        drawString(fontRendererObj, "STAFF", left, 45, 0xFFB8BDC8);
        drawString(fontRendererObj, "CURRENT RANK", left + staffField.width + 6, 45, 0xFFB8BDC8);
        drawString(fontRendererObj, "ACTING RANK", left + (staffField.width + 6) * 2, 45, 0xFFB8BDC8);
        drawString(fontRendererObj, "HOURS", left + (staffField.width + 6) * 3, 45, 0xFFB8BDC8);
        drawField(staffField);
        drawField(currentRankField);
        drawField(actingRankField);
        drawField(durationField);
        drawString(fontRendererObj, "REASON", left, 98, 0xFFB8BDC8);
        drawField(reasonField);

        List<ActingRankEntry> entries = getEntries();
        drawString(fontRendererObj, "ASSIGNMENTS: " + entries.size()
                + "  ACTIVE: " + service.getRegistry().countActive(System.currentTimeMillis()),
                left, 122, 0xFFB8BDC8);
        int top = 142;
        int rowHeight = 32;
        int rows = Math.max(0, (height - top - 38) / rowHeight);
        for (int i = 0; i < rows && scrollOffset + i < entries.size(); i++) {
            int index = scrollOffset + i;
            ActingRankEntry entry = entries.get(index);
            int y = top + i * rowHeight;
            drawRect(left, y, width - left, y + 29,
                    index == selectedIndex ? 0xAA665522 : 0x88303030);
            drawString(fontRendererObj, fit(entry.getStaffName() + "  "
                    + entry.getCurrentRank() + " -> " + entry.getActingRank(), width - left * 2 - 8),
                    left + 6, y + 3, 0xFFFFFFFF);
            String status = entry.getEffectiveStatus(System.currentTimeMillis()).name();
            String expiry = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.ENGLISH)
                    .format(new Date(entry.getExpiresAtMillis()));
            drawString(fontRendererObj, status + " | by " + entry.getAssignedBy()
                    + " | expires " + expiry, left + 6, y + 16, 0xFFB8BDC8);
        }
        if (entries.isEmpty()) drawCenteredString(fontRendererObj, "No acting-rank assignments yet",
                width / 2, top + 20, 0xFFB8BDC8);
        drawString(fontRendererObj, fit(message, width - 24), 12, height - 42, 0xFFFFD866);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawField(GuiTextField field) {
        drawRect(field.xPosition - 1, field.yPosition - 1,
                field.xPosition + field.width + 1, field.yPosition + field.height + 1, 0xFF454B56);
        drawRect(field.xPosition, field.yPosition,
                field.xPosition + field.width, field.yPosition + field.height, 0xFF11141B);
        field.drawTextBox();
    }

    private String fit(String value, int widthLimit) {
        return fontRendererObj.trimStringToWidth(value, Math.max(0, widthLimit));
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
