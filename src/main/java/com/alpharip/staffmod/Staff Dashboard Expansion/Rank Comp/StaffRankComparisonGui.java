package com.alpharip.staffdashboardexpansion.rankcomp;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;
/**
 * Standalone Minecraft 1.8.9 comparison screen. All role and permission data
 * is entered locally; this GUI is not connected to the staff permission system.
 */
public final class StaffRankComparisonGui extends GuiScreen {
    private final GuiScreen parent;
    private final StaffRankComparisonModule module;
    private GuiTextField firstRole, firstPermissions, secondRole, secondPermissions;
    private StaffRankComparisonResult result;
    private String feedback = "";
    private int resultScroll;
    public StaffRankComparisonGui(GuiScreen parent) { this(parent, new StaffRankComparisonModule()); }
    public StaffRankComparisonGui(GuiScreen parent, StaffRankComparisonModule module) {
        if (module == null) throw new IllegalArgumentException("Module is required.");
        this.parent = parent; this.module = module;
    }
    @Override public void initGui() {
        Keyboard.enableRepeatEvents(true); buttonList.clear();
        int left = width / 2 - 190;
        firstRole = new GuiTextField(0, fontRendererObj, left, 53, 180, 18);
        firstPermissions = new GuiTextField(1, fontRendererObj, left, 91, 180, 36);
        secondRole = new GuiTextField(2, fontRendererObj, left + 200, 53, 180, 18);
        secondPermissions = new GuiTextField(3, fontRendererObj, left + 200, 91, 180, 36);
        firstRole.setMaxStringLength(32); secondRole.setMaxStringLength(32);
        firstPermissions.setMaxStringLength(256); secondPermissions.setMaxStringLength(256);
        buttonList.add(new GuiButton(0, width / 2 - 90, 137, 180, 20, "Compare Roles"));
        buttonList.add(new GuiButton(1, width / 2 - 45, height - 28, 90, 20, "Back"));
    }
    @Override protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            try {
                result = module.compare(firstRole.getText(), firstPermissions.getText(),
                        secondRole.getText(), secondPermissions.getText());
                resultScroll = 0; feedback = "Comparison updated. Labels only; no access changes are made.";
            } catch (IllegalArgumentException exception) { result = null; feedback = exception.getMessage(); }
        } else if (button.id == 1) mc.displayGuiScreen(parent);
    }
    @Override protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (firstRole.textboxKeyTyped(typedChar, keyCode) || firstPermissions.textboxKeyTyped(typedChar, keyCode)
                || secondRole.textboxKeyTyped(typedChar, keyCode)
                || secondPermissions.textboxKeyTyped(typedChar, keyCode)) return;
        if (keyCode == Keyboard.KEY_ESCAPE) mc.displayGuiScreen(parent);
        else if (keyCode == Keyboard.KEY_UP) resultScroll = Math.max(0, resultScroll - 1);
        else if (keyCode == Keyboard.KEY_DOWN) resultScroll++;
        else super.keyTyped(typedChar, keyCode);
    }
    @Override protected void mouseClicked(int x, int y, int button) throws IOException {
        super.mouseClicked(x, y, button);
        firstRole.mouseClicked(x, y, button); firstPermissions.mouseClicked(x, y, button);
        secondRole.mouseClicked(x, y, button); secondPermissions.mouseClicked(x, y, button);
    }
    @Override public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = org.lwjgl.input.Mouse.getEventDWheel();
        if (wheel != 0) resultScroll = Math.max(0, resultScroll + (wheel > 0 ? -1 : 1));
    }
    @Override public void updateScreen() {
        firstRole.updateCursorCounter(); firstPermissions.updateCursorCounter();
        secondRole.updateCursorCounter(); secondPermissions.updateCursorCounter();
    }
    private List<String> asList(Set<String> values) { return new ArrayList<String>(values); }
    private void drawColumn(String heading, List<String> values, int x, int y, int color) {
        drawString(fontRendererObj, heading + " (" + values.size() + ")", x, y, color);
        int visible = Math.max(1, (height - y - 52) / 12);
        int offset = Math.min(resultScroll, Math.max(0, values.size() - visible));
        for (int i = offset; i < values.size() && i < offset + visible; i++) {
            String value = values.get(i);
            if (value.length() > 27) value = value.substring(0, 24) + "...";
            drawString(fontRendererObj, "- " + value, x, y + 14 + (i - offset) * 12, 0xFFE0E0E0);
        }
    }
    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "Staff Rank Comparison", width / 2, 12, 0xFFFFFFFF);
        drawCenteredString(fontRendererObj, "Compare manually supplied permission labels for two roles",
                width / 2, 27, 0xFFAAAAAA);
        int left = width / 2 - 190;
        drawString(fontRendererObj, "Role A", left, 41, 0xFFFFC83D);
        drawString(fontRendererObj, "Role B", left + 200, 41, 0xFF55CCFF);
        firstRole.drawTextBox(); firstPermissions.drawTextBox();
        secondRole.drawTextBox(); secondPermissions.drawTextBox();
        drawString(fontRendererObj, "Permissions (comma-separated)", left, 79, 0xFFAAAAAA);
        drawString(fontRendererObj, "Permissions (comma-separated)", left + 200, 79, 0xFFAAAAAA);
        if (result != null) {
            int top = 170;
            drawColumn("Shared", asList(result.getShared()), 18, top, 0xFF55CC88);
            drawColumn("Only " + firstRole.getText().trim(), asList(result.getFirstOnly()),
                    width / 2 - 125, top, 0xFFFFC83D);
            drawColumn("Only " + secondRole.getText().trim(), asList(result.getSecondOnly()),
                    width - 145, top, 0xFF55CCFF);
        } else {
            drawCenteredString(fontRendererObj, "Enter both roles and their permission labels, then select Compare Roles.",
                    width / 2, 190, 0xFFAAAAAA);
        }
        if (!feedback.isEmpty()) drawCenteredString(fontRendererObj, feedback, width / 2, height - 43, 0xFFFFFF55);
        drawCenteredString(fontRendererObj,
                "Local prototype data only — this screen does not grant or change permissions.",
                width / 2, height - 15, 0xFF888888);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    @Override public void onGuiClosed() { Keyboard.enableRepeatEvents(false); }
    @Override public boolean doesGuiPauseGame() { return false; }
}
