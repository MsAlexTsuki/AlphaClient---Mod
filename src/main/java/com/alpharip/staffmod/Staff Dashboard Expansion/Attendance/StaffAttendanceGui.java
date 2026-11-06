package com.alpharip.staffdashboardexpansion.attendance;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Standalone Minecraft 1.8.9 attendance screen.
 * TODO: Register/open from Staff Dashboard and apply the real staff permission layer.
 */
public final class StaffAttendanceGui extends GuiScreen {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final GuiScreen parent;
    private final StaffAttendanceManager manager;
    private GuiTextField staffField;
    private int scrollOffset;
    private boolean todayOnly = true;
    private String feedback = "";

    public StaffAttendanceGui(GuiScreen parent, StaffAttendanceManager manager) {
        if (manager == null) throw new IllegalArgumentException("manager must not be null.");
        this.parent = parent;
        this.manager = manager;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        buttonList.clear();
        staffField = new GuiTextField(0, fontRendererObj, width / 2 - 100, 34, 200, 18);
        staffField.setMaxStringLength(32);
        buttonList.add(new GuiButton(0, width / 2 - 100, 60, 98, 20, "Check In"));
        buttonList.add(new GuiButton(1, width / 2 + 2, 60, 98, 20, "Check Out"));
        buttonList.add(new GuiButton(2, width / 2 - 100, height - 28, 98, 20,
                todayOnly ? "View: Today" : "View: All"));
        buttonList.add(new GuiButton(3, width / 2 + 2, height - 28, 98, 20, "Back"));
        refreshRows();
    }

    private List<String> visibleRows() {
        List<String> rows = new ArrayList<String>();
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        for (Map.Entry<String, List<StaffAttendanceSession>> entry : manager.getAllSessions().entrySet()) {
            String name = entry.getKey();
            for (StaffAttendanceSession session : entry.getValue()) {
                if (todayOnly && !session.getCheckIn().toLocalDate().equals(today)
                        && !(session.isOpen() && session.getCheckIn().isBefore(today.atStartOfDay()))) continue;
                Duration duration = todayOnly ? manager.getForDate(name, today) : manager.getTotal(name);
                String state = session.isOpen() ? "OPEN" : "CLOSED";
                rows.add(name + " | " + state + " | " + session.getCheckIn().format(TIME)
                        + " | " + formatDuration(session.getDuration(now))
                        + " | total " + formatDuration(duration));
            }
        }
        return rows;
    }

    private void refreshRows() {
        while (buttonList.size() > 4) buttonList.remove(buttonList.size() - 1);
        List<String> rows = visibleRows();
        int visible = Math.max(0, Math.min(9, (height - 125) / 18));
        for (int i = scrollOffset; i < rows.size() && i < scrollOffset + visible; i++) {
            buttonList.add(new GuiButton(20 + i - scrollOffset, width / 2 - 150,
                    100 + (i - scrollOffset) * 18, 300, 17, rows.get(i)));
        }
    }

    private static String formatDuration(Duration duration) {
        long seconds = Math.max(0L, duration.getSeconds());
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        return String.format("%02d:%02d", hours, minutes);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (!button.enabled) return;
        String staff = staffField.getText().trim();
        try {
            if (button.id == 0 || button.id == 1) {
                if (staff.isEmpty()) { feedback = "Enter a staff name first."; return; }
                if (button.id == 0) manager.checkIn(staff); else manager.checkOut(staff);
                feedback = button.id == 0 ? "Check-in recorded." : "Check-out recorded.";
                scrollOffset = 0;
                refreshRows();
            } else if (button.id == 2) {
                todayOnly = !todayOnly;
                button.displayString = todayOnly ? "View: Today" : "View: All";
                scrollOffset = 0;
                refreshRows();
            } else if (button.id == 3) {
                mc.displayGuiScreen(parent);
            }
        } catch (IllegalArgumentException | IllegalStateException ex) {
            feedback = ex.getMessage();
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (staffField.textboxKeyTyped(typedChar, keyCode)) return;
        if (keyCode == Keyboard.KEY_ESCAPE) mc.displayGuiScreen(parent);
        else super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        staffField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = org.lwjgl.input.Mouse.getEventDWheel();
        if (wheel != 0) {
            int count = visibleRows().size();
            int visible = Math.max(1, Math.min(9, (height - 125) / 18));
            scrollOffset = Math.max(0, Math.min(Math.max(0, count - visible), scrollOffset + (wheel > 0 ? -1 : 1)));
            refreshRows();
        }
    }

    @Override
    public void updateScreen() { staffField.updateCursorCounter(); }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "Staff Attendance Overview", width / 2, 12, 0xFFFFFF);
        drawString(fontRendererObj, "Staff name", width / 2 - 100, 23, 0xAAAAAA);
        staffField.drawTextBox();
        drawString(fontRendererObj, "Sessions are local to this client session.", 8, height - 48, 0xAAAAAA);
        drawString(fontRendererObj, feedback, 8, height - 38, 0xDDDDDD);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void onGuiClosed() { Keyboard.enableRepeatEvents(false); }
    @Override
    public boolean doesGuiPauseGame() { return false; }
}
