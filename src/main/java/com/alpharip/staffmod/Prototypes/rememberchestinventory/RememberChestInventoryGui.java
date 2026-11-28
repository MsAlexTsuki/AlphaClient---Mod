package com.alpharip.staffmod.rememberchestinventory;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

/** Searchable viewer for the locally remembered chest snapshots. */
public final class RememberChestInventoryGui extends GuiScreen {
    private final GuiScreen parent;
    private final RememberChestInventory module;
    private GuiTextField searchField;
    private List<ChestMemoryEntry> visibleEntries;
    private int scrollOffset;

    public RememberChestInventoryGui(GuiScreen parent, RememberChestInventory module) {
        this.parent = parent;
        this.module = module;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        searchField = new GuiTextField(0, fontRendererObj, width / 2 - 120, 34, 240, 20);
        buttonList.add(new GuiButton(1, width / 2 - 100, height - 30, 200, 20, "Done"));
        buttonList.add(new GuiButton(2, width / 2 + 125, 34, 65, 20, "Clear"));
        refreshResults();
    }

    private void refreshResults() {
        visibleEntries = module.getManager().search(searchField == null ? "" : searchField.getText());
        if (scrollOffset > Math.max(0, visibleEntries.size() - 1)) scrollOffset = 0;
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 1) {
            module.save();
            mc.displayGuiScreen(parent);
        } else if (button.id == 2) {
            module.getManager().clear();
            module.save();
            refreshResults();
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (searchField != null && searchField.textboxKeyTyped(typedChar, keyCode)) {
            scrollOffset = 0;
            refreshResults();
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (searchField != null) searchField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = org.lwjgl.input.Mouse.getEventDWheel();
        if (wheel != 0) {
            scrollOffset = Math.max(0, Math.min(Math.max(0, visibleEntries.size() - 1),
                    scrollOffset + (wheel < 0 ? 1 : -1)));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "Remember Chest Inventory", width / 2, 12, 0xFFFFFF);
        searchField.drawTextBox();
        drawString(fontRendererObj, "Search chest names, coordinates, or remembered items",
                width / 2 - 120, 58, 0xAAAAAA);

        int y = 82;
        int end = Math.min(visibleEntries.size(), scrollOffset + 8);
        for (int i = scrollOffset; i < end; i++) {
            ChestMemoryEntry entry = visibleEntries.get(i);
            String line = entry.getChestName() + "  [" + entry.getX() + ", "
                    + entry.getY() + ", " + entry.getZ() + "]  "
                    + entry.getItems().size() + " item types";
            drawString(fontRendererObj, line, width / 2 - 145, y, 0xFFFFFF);
            String date = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                    .format(new Date(entry.getObservedAt()));
            drawString(fontRendererObj, "Dimension " + entry.getDimensionId() + " · " + date,
                    width / 2 - 135, y + 11, 0x888888);
            y += 27;
        }
        if (visibleEntries.isEmpty()) {
            drawCenteredString(fontRendererObj, "No remembered chests found",
                    width / 2, height / 2, 0xAAAAAA);
        }
        drawCenteredString(fontRendererObj, visibleEntries.size() + " remembered chest(s)",
                width / 2, height - 42, 0xAAAAAA);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}