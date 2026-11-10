package com.alpharip.staffmod.kitmaphcf.factions.ally;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.List;

/**
 * Simple two-column GUI for adding and reviewing notes grouped by faction.
 * The screen does not pause singleplayer.
 */
public final class AllyFactionNotesScreen extends GuiScreen {
    private static final int VISIBLE_ROWS = 8;

    private final GuiScreen parent;
    private final AllyFactionNotesStore store;

    private GuiTextField factionField;
    private GuiTextField noteField;
    private String selectedFaction;
    private String status = "Select a faction or enter a name to add its first note.";
    private int factionScroll;
    private int noteScroll;

    public AllyFactionNotesScreen(GuiScreen parent, AllyFactionNotesStore store) {
        this.parent = parent;
        this.store = store;
    }

    @Override
    public void initGui() {
        buttonList.clear();

        int fieldWidth = Math.min(220, width - 40);
        int fieldX = width / 2 - fieldWidth / 2;
        factionField = new GuiTextField(1, fontRendererObj, fieldX, 44, fieldWidth, 18);
        factionField.setMaxStringLength(32);
        noteField = new GuiTextField(2, fontRendererObj, fieldX, 82, fieldWidth, 18);
        noteField.setMaxStringLength(160);

        int buttonWidth = 100;
        int buttonY = 108;
        buttonList.add(new GuiButton(0, width / 2 - 154, buttonY,
                buttonWidth, 20, "Save note"));
        buttonList.add(new GuiButton(1, width / 2 - 50, buttonY,
                buttonWidth, 20, "Clear faction"));
        buttonList.add(new GuiButton(2, width / 2 + 54, buttonY,
                buttonWidth, 20, "Done"));

        if (selectedFaction != null) {
            factionField.setText(selectedFaction);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            saveNote();
        } else if (button.id == 1) {
            clearFaction();
        } else if (button.id == 2) {
            mc.displayGuiScreen(parent);
        }
    }

    private void saveNote() {
        String faction = factionField.getText().trim();
        String note = noteField.getText().trim();
        if (faction.isEmpty()) {
            status = "Enter a faction name first.";
            return;
        }
        if (note.isEmpty()) {
            status = "Enter a note before saving.";
            return;
        }

        try {
            store.addNote(faction, note);
            selectedFaction = faction;
            noteField.setText("");
            factionField.setText(faction);
            factionScroll = Math.max(0, store.getFactionNames().size() - VISIBLE_ROWS);
            noteScroll = 0;
            status = "Note saved locally for " + faction + ".";
        } catch (IllegalArgumentException exception) {
            status = exception.getMessage();
        }
    }

    private void clearFaction() {
        String faction = selectedFaction;
        if (faction == null || faction.trim().isEmpty()) {
            faction = factionField.getText().trim();
        }
        if (faction.isEmpty()) {
            status = "Select or enter a faction first.";
            return;
        }

        store.clearFaction(faction);
        selectedFaction = null;
        factionField.setText("");
        noteScroll = 0;
        factionScroll = 0;
        status = "All notes for " + faction + " were cleared.";
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (factionField != null) {
            factionField.mouseClicked(mouseX, mouseY, mouseButton);
            noteField.mouseClicked(mouseX, mouseY, mouseButton);
        }

        int panelTop = 140;
        int rowHeight = 18;
        int left = width / 2 - 205;
        int right = width / 2 + 5;

        if (mouseButton == 0 && mouseY >= panelTop + 18) {
            int row = (mouseY - panelTop - 18) / rowHeight;
            if (row < 0 || row >= VISIBLE_ROWS) {
                return;
            }

            if (mouseX >= left && mouseX < left + 195) {
                List<String> factions = store.getFactionNames();
                int index = factionScroll + row;
                if (index >= 0 && index < factions.size()) {
                    selectedFaction = factions.get(index);
                    factionField.setText(selectedFaction);
                    noteScroll = 0;
                    status = "Showing notes for " + selectedFaction + ".";
                }
            } else if (mouseX >= right && mouseX < right + 195) {
                // Notes are intentionally read-only in this first prototype.
            }
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel == 0) {
            return;
        }

        int mouseX = Mouse.getEventX() * width / mc.displayWidth;
        int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;
        int left = width / 2 - 205;
        int right = width / 2 + 5;
        if (mouseY < 163) {
            return;
        }

        if (mouseX >= left && mouseX < left + 195) {
            int max = Math.max(0, store.getFactionNames().size() - VISIBLE_ROWS);
            factionScroll = clamp(factionScroll + (wheel < 0 ? 1 : -1), 0, max);
        } else if (mouseX >= right && mouseX < right + 195 && selectedFaction != null) {
            int max = Math.max(0, store.getNotes(selectedFaction).size() - VISIBLE_ROWS);
            noteScroll = clamp(noteScroll + (wheel < 0 ? 1 : -1), 0, max);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            mc.displayGuiScreen(parent);
            return;
        }

        if (keyCode == 15) {
            boolean focusFaction = !factionField.isFocused();
            factionField.setFocused(focusFaction);
            noteField.setFocused(!focusFaction);
            return;
        }

        if (factionField.isFocused()) {
            factionField.textboxKeyTyped(typedChar, keyCode);
            return;
        }
        if (noteField.isFocused()) {
            noteField.textboxKeyTyped(typedChar, keyCode);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void updateScreen() {
        factionField.updateCursorCounter();
        noteField.updateCursorCounter();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        drawCenteredString(fontRendererObj, "ALLY FACTION NOTES", width / 2, 8, 0xFFFFFF);
        drawCenteredString(fontRendererObj, "Faction notes are stored locally on this Minecraft installation.",
                width / 2, 21, 0xAAAAAA);

        drawString(fontRendererObj, "Faction name", factionField.xPosition, 34, 0xDDDDDD);
        factionField.drawTextBox();
        drawString(fontRendererObj, "New note", noteField.xPosition, 72, 0xDDDDDD);
        noteField.drawTextBox();

        super.drawScreen(mouseX, mouseY, partialTicks);

        int left = width / 2 - 205;
        int right = width / 2 + 5;
        int panelTop = 145;
        int panelBottom = panelTop + 18 + VISIBLE_ROWS * 18 + 4;

        Gui.drawRect(left - 4, panelTop, left + 195, panelBottom, 0xAA101010);
        Gui.drawRect(right - 4, panelTop, right + 195, panelBottom, 0xAA101010);
        drawString(fontRendererObj, "FACTIONS", left, panelTop + 5, 0xFFFFC83D);
        drawString(fontRendererObj, "NOTES", right, panelTop + 5, 0xFFFFC83D);

        List<String> factions = store.getFactionNames();
        for (int row = 0; row < VISIBLE_ROWS; row++) {
            int index = factionScroll + row;
            int y = panelTop + 21 + row * 18;
            if (index >= factions.size()) {
                break;
            }
            String faction = factions.get(index);
            boolean selected = faction.equals(selectedFaction);
            if (selected) {
                Gui.drawRect(left - 2, y - 2, left + 191, y + 14, 0x884C3B12);
            }
            drawString(fontRendererObj, trim(faction, 27), left + 2, y, 
                    selected ? 0xFFFFC83D : 0xFFE0E0E0);
        }

        if (selectedFaction != null) {
            List<String> notes = store.getNotes(selectedFaction);
            for (int row = 0; row < VISIBLE_ROWS; row++) {
                int index = noteScroll + row;
                if (index >= notes.size()) {
                    break;
                }
                int y = panelTop + 21 + row * 18;
                drawString(fontRendererObj, trim((index + 1) + ". " + notes.get(index), 30),
                        right + 2, y, 0xFFE0E0E0);
            }
        } else {
            drawString(fontRendererObj, "Choose a faction", right + 2, panelTop + 24, 0xFF888888);
        }

        drawCenteredString(fontRendererObj, status, width / 2, panelBottom + 8, 0xFFBBBBBB);
        drawCenteredString(fontRendererObj, "Scroll inside either panel to see more entries.",
                width / 2, panelBottom + 20, 0xFF777777);
    }

    private static String trim(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value == null ? "" : value;
        }
        return value.substring(0, maxLength - 3) + "...";
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
