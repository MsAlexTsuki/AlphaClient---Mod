package com.alpharip.staffmod.AccessReviewReminder;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Compact, scrollable Alpha-styled screen for managing staff access reviews. */
public final class AccessReviewReminderGui extends GuiScreen {
    private static final int ADD = 1;
    private static final int ROW_BASE = 1000;
    private static final int ROW_HEIGHT = 38;

    private final GuiScreen parent;
    private final AccessReviewReminder manager = AccessReviewReminder.getInstance();

    private GuiTextField nameField;
    private GuiTextField rankField;
    private GuiTextField intervalField;

    private int panelLeft, panelRight, panelTop, panelBottom;
    private int listTop, listBottom, visibleRows, scrollOffset;
    private String message = "";
    private int messageColor = 0xFFB8BDC8;
    private List<AccessReviewEntry> visibleEntries;

    public AccessReviewReminderGui(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        manager.load();
        buttonList.clear();

        int panelWidth = Math.min(460, Math.max(280, width - 20));
        int panelHeight = Math.min(340, Math.max(230, height - 20));
        panelLeft = (width - panelWidth) / 2;
        panelRight = panelLeft + panelWidth;
        panelTop = (height - panelHeight) / 2;
        panelBottom = panelTop + panelHeight;

        int innerWidth = panelWidth - 24;
        int addWidth = 54;
        int intervalWidth = 42;
        int rankWidth = 82;
        int nameWidth = Math.max(70, innerWidth - addWidth - intervalWidth - rankWidth - 18);
        int fieldX = panelLeft + 12;
        int fieldY = panelTop + 61;

        nameField = new GuiTextField(10, fontRendererObj, fieldX, fieldY, nameWidth, 18);
        rankField = new GuiTextField(11, fontRendererObj, fieldX + nameWidth + 6,
                fieldY, rankWidth, 18);
        intervalField = new GuiTextField(12, fontRendererObj,
                fieldX + nameWidth + rankWidth + 12, fieldY, intervalWidth, 18);
        nameField.setMaxStringLength(32);
        rankField.setMaxStringLength(24);
        intervalField.setMaxStringLength(4);
        intervalField.setText("30");

        int addX = fieldX + nameWidth + rankWidth + intervalWidth + 18;
        buttonList.add(new AlphaActionButton(ADD, addX, fieldY - 1, addWidth, 20, "ADD"));

        listTop = panelTop + 105;
        listBottom = panelBottom - 44;
        visibleRows = Math.max(1, (listBottom - listTop) / ROW_HEIGHT);
        rebuildRowButtons();
    }

    private void rebuildRowButtons() {
        for (int i = buttonList.size() - 1; i >= 0; i--) {
            if (buttonList.get(i).id >= ROW_BASE) buttonList.remove(i);
        }
        List<AccessReviewEntry> entries = manager.getEntries();
        if (scrollOffset > Math.max(0, entries.size() - visibleRows)) {
            scrollOffset = Math.max(0, entries.size() - visibleRows);
        }
        visibleEntries = entries;
        int rowRight = panelRight - 12;
        int first = scrollOffset;
        int last = Math.min(entries.size(), first + visibleRows);
        for (int index = first; index < last; index++) {
            int row = index - first;
            int y = listTop + row * ROW_HEIGHT + 9;
            buttonList.add(new AlphaActionButton(ROW_BASE + row * 2,
                    rowRight - 112, y, 58, 19, "REVIEW"));
            buttonList.add(new AlphaActionButton(ROW_BASE + row * 2 + 1,
                    rowRight - 49, y, 49, 19, "REMOVE"));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0xA6000000);
        drawRect(panelLeft, panelTop, panelRight, panelBottom, 0xF00A0C11);
        drawRect(panelLeft, panelTop, panelRight, panelTop + 2, 0xFFFFD866);

        drawCenteredString(fontRendererObj, "ACCESS REVIEW REMINDER",
                width / 2, panelTop + 13, 0xFFF3F4F6);
        drawCenteredString(fontRendererObj, "STAFF ACCESS OVERVIEW",
                width / 2, panelTop + 29, 0xFFFFD866);

        drawString(fontRendererObj, "STAFF NAME", nameField.xPosition, panelTop + 47, 0xFFB8BDC8);
        drawString(fontRendererObj, "RANK", rankField.xPosition, panelTop + 47, 0xFFB8BDC8);
        drawString(fontRendererObj, "DAYS", intervalField.xPosition, panelTop + 47, 0xFFB8BDC8);

        drawField(nameField);
        drawField(rankField);
        drawField(intervalField);

        long now = System.currentTimeMillis();
        List<AccessReviewEntry> entries = manager.getEntries();
        drawString(fontRendererObj, entries.size() + " STAFF",
                panelLeft + 12, panelTop + 88, 0xFFB8BDC8);
        drawString(fontRendererObj, manager.countDue(now) + " DUE",
                panelLeft + 92, panelTop + 88, 0xFFFF8A80);
        drawString(fontRendererObj, manager.countUpcoming(now) + " UPCOMING",
                panelLeft + 158, panelTop + 88, 0xFFFFD866);

        drawRect(panelLeft + 10, listTop - 5, panelRight - 10, listBottom,
                0x6614171E);
        if (entries.isEmpty()) {
            drawCenteredString(fontRendererObj, "No staff members added yet",
                    width / 2, listTop + 24, 0xFFB8BDC8);
            drawCenteredString(fontRendererObj, "Add a name above to start tracking reviews",
                    width / 2, listTop + 39, 0xFF858D9B);
        } else {
            drawRows(mouseX, mouseY, now, entries);
            drawScrollbar(entries.size());
        }

        drawString(fontRendererObj, message, panelLeft + 12,
                panelBottom - 29, messageColor);
        drawString(fontRendererObj, "SCROLL TO VIEW MORE",
                panelRight - 126, panelBottom - 29, 0xFF777F8D);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawRows(int mouseX, int mouseY, long now, List<AccessReviewEntry> entries) {
        ScaledResolution resolution = new ScaledResolution(mc);
        int scale = resolution.getScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(panelLeft * scale, mc.displayHeight - listBottom * scale,
                (panelRight - panelLeft) * scale, (listBottom - listTop) * scale);

        int first = scrollOffset;
        int last = Math.min(entries.size(), first + visibleRows);
        for (int index = first; index < last; index++) {
            AccessReviewEntry entry = entries.get(index);
            int row = index - first;
            int y = listTop + row * ROW_HEIGHT;
            int rowLeft = panelLeft + 14;
            int rowRight = panelRight - 14;
            drawRect(rowLeft, y, rowRight, y + ROW_HEIGHT - 2, 0xAA191D25);
            drawRect(rowLeft, y, rowRight, y + 1, 0x554B515D);

            String title = entry.getStaffName();
            if (!entry.getRank().isEmpty()) title += "  [" + entry.getRank() + "]";
            drawString(fontRendererObj, fit(title, rowRight - rowLeft - 126),
                    rowLeft + 7, y + 5, 0xFFF3F4F6);

            String lastReview = entry.getLastReviewedAt() <= 0L
                    ? "Last review: Never"
                    : "Last review: " + formatDate(entry.getLastReviewedAt());
            drawString(fontRendererObj, fit(lastReview, rowRight - rowLeft - 126),
                    rowLeft + 7, y + 20, 0xFFADB4C0);

            AccessReviewEntry.ReviewStatus status = entry.getStatus(now);
            String statusText;
            int statusColor;
            if (status == AccessReviewEntry.ReviewStatus.DUE) {
                statusText = "DUE";
                statusColor = 0xFFFF8A80;
            } else if (status == AccessReviewEntry.ReviewStatus.UPCOMING) {
                statusText = "UPCOMING";
                statusColor = 0xFFFFD866;
            } else {
                statusText = "CURRENT";
                statusColor = 0xFF9CE2AC;
            }
            drawString(fontRendererObj, statusText, rowRight - 111, y - 1, statusColor);

            long nextReview = entry.getNextReviewAt();
            String nextText = nextReview < 0L ? "Review required"
                    : "Next: " + formatDate(nextReview);
            drawString(fontRendererObj, fit(nextText, rowRight - rowLeft - 126),
                    rowLeft + 7, y + 30, 0xFF858D9B);
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    private void drawScrollbar(int totalEntries) {
        int trackX = panelRight - 8;
        int trackTop = listTop;
        int trackBottom = listBottom;
        int trackHeight = trackBottom - trackTop;
        drawRect(trackX, trackTop, trackX + 2, trackBottom, 0x554B515D);
        if (totalEntries <= visibleRows) return;

        int thumbHeight = Math.max(18, trackHeight * visibleRows / totalEntries);
        int maxScroll = totalEntries - visibleRows;
        int thumbY = trackTop + (trackHeight - thumbHeight) * scrollOffset / maxScroll;
        drawRect(trackX - 1, thumbY, trackX + 3, thumbY + thumbHeight, 0xFFFFD866);
    }

    private void drawField(GuiTextField field) {
        drawRect(field.xPosition - 1, field.yPosition - 1,
                field.xPosition + field.width + 1, field.yPosition + field.height + 1,
                field.isFocused() ? 0xFFFFD866 : 0xFF454B56);
        drawRect(field.xPosition, field.yPosition,
                field.xPosition + field.width, field.yPosition + field.height, 0xFF11141B);
        field.drawTextBox();
    }

    private String fit(String value, int maxWidth) {
        return fontRendererObj.trimStringToWidth(value, Math.max(0, maxWidth));
    }

    private String formatDate(long timestamp) {
        return new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(new Date(timestamp));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button.id == ADD) {
            addStaff();
            return;
        }
        if (button.id >= ROW_BASE) {
            int relative = button.id - ROW_BASE;
            int row = relative / 2;
            int action = relative % 2;
            int index = scrollOffset + row;
            List<AccessReviewEntry> entries = manager.getEntries();
            if (index < 0 || index >= entries.size()) return;
            AccessReviewEntry selected = entries.get(index);
            if (action == 0) {
                manager.markReviewed(selected.getId());
                setMessage("Review saved for " + selected.getStaffName(), 0xFF9CE2AC);
            } else {
                manager.removeStaff(selected.getId());
                setMessage("Removed " + selected.getStaffName(), 0xFFB8BDC8);
            }
            rebuildRowButtons();
        }
    }

    private void addStaff() {
        try {
            String name = nameField.getText();
            String rank = rankField.getText();
            int days = Integer.parseInt(intervalField.getText().trim());
            manager.addStaff(name, rank, days);
            nameField.setText("");
            rankField.setText("");
            intervalField.setText("30");
            scrollOffset = Math.max(0, manager.getEntries().size() - visibleRows);
            setMessage("Staff member added", 0xFF9CE2AC);
            rebuildRowButtons();
        } catch (NumberFormatException exception) {
            setMessage("Enter a valid interval in days", 0xFFFF8A80);
        } catch (IllegalArgumentException exception) {
            setMessage(exception.getMessage(), 0xFFFF8A80);
        }
    }

    private void setMessage(String text, int color) {
        message = fit(text, panelRight - panelLeft - 145);
        messageColor = color;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        nameField.mouseClicked(mouseX, mouseY, mouseButton);
        rankField.mouseClicked(mouseX, mouseY, mouseButton);
        intervalField.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            mc.displayGuiScreen(parent);
            return;
        }
        nameField.textboxKeyTyped(typedChar, keyCode);
        rankField.textboxKeyTyped(typedChar, keyCode);
        intervalField.textboxKeyTyped(typedChar, keyCode);
        if (keyCode == 28) addStaff();
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel == 0) return;
        int mouseX = Mouse.getEventX() * width / mc.displayWidth;
        int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;
        if (mouseX >= panelLeft && mouseX <= panelRight
                && mouseY >= listTop && mouseY <= listBottom) {
            int maxScroll = Math.max(0, manager.getEntries().size() - visibleRows);
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset + (wheel > 0 ? -1 : 1)));
            rebuildRowButtons();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    /** Alpha-style button kept local so this module does not modify shared GUI classes. */
    private static final class AlphaActionButton extends GuiButton {
        AlphaActionButton(int id, int x, int y, int width, int height, String label) {
            super(id, x, y, width, height, label);
        }

        @Override
        public void drawButton(net.minecraft.client.Minecraft minecraft, int mouseX, int mouseY) {
            if (!visible) return;
            hovered = mouseX >= xPosition && mouseY >= yPosition
                    && mouseX < xPosition + width && mouseY < yPosition + height;
            int background = hovered ? 0xFF292D36 : 0xFF171A21;
            int border = hovered ? 0xFFFFD866 : 0x664B515D;
            int foreground = hovered ? 0xFFFFD866 : 0xFFE6E8ED;
            drawRect(xPosition, yPosition, xPosition + width, yPosition + height, background);
            drawRect(xPosition, yPosition, xPosition + width, yPosition + 1, border);
            drawRect(xPosition, yPosition + height - 1, xPosition + width, yPosition + height,
                    0xAA090A0E);
            drawCenteredString(minecraft.fontRendererObj, displayString,
                    xPosition + width / 2, yPosition + (height - 8) / 2, foreground);
        }
    }
}