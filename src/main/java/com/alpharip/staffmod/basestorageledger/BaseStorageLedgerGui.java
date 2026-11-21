package com.alpharip.staffmod.basestorageledger;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

/** Searchable cross-chest stock overview and minimum-stock alert editor. */
public final class BaseStorageLedgerGui extends GuiScreen {
    private final GuiScreen parent;
    private final BaseStorageLedger module;
    private GuiTextField searchField;
    private List<StorageLedgerItem> visibleItems;
    private String selectedKey;
    private int scrollOffset;

    public BaseStorageLedgerGui(GuiScreen parent, BaseStorageLedger module) {
        this.parent = parent;
        this.module = module;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        searchField = new GuiTextField(0, fontRendererObj, width / 2 - 150, 34, 250, 20);
        buttonList.add(new GuiButton(0, width / 2 + 105, 34, 75, 20, "Refresh"));
        buttonList.add(new GuiButton(1, width / 2 - 150, height - 54, 88, 20, "Minimum -16"));
        buttonList.add(new GuiButton(2, width / 2 - 58, height - 54, 88, 20, "Minimum +16"));
        buttonList.add(new GuiButton(3, width / 2 + 34, height - 54, 88, 20, "Disable alert"));
        buttonList.add(new GuiButton(4, width / 2 + 126, height - 54, 54, 20, "Done"));
        refreshResults();
        updateAlertButtons();
    }

    private void refreshResults() {
        visibleItems = module.getIndex().search(searchField == null ? "" : searchField.getText());
        if (scrollOffset >= visibleItems.size()) scrollOffset = Math.max(0, visibleItems.size() - 1);
    }

    private StorageLedgerItem getSelectedItem() {
        if (selectedKey == null) return null;
        for (StorageLedgerItem item : module.getIndex().getItems()) {
            if (selectedKey.equals(item.getKey())) return item;
        }
        return null;
    }

    private void updateAlertButtons() {
        StorageLedgerItem selected = getSelectedItem();
        boolean enabled = selected != null && module.getLowAlert().hasAlert(selected.getKey());
        buttonList.get(1).enabled = selected != null;
        buttonList.get(2).enabled = selected != null;
        buttonList.get(3).enabled = enabled;
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        StorageLedgerItem selected = getSelectedItem();
        switch (button.id) {
            case 0:
                module.refresh();
                refreshResults();
                break;
            case 1:
                if (selected != null) {
                    int next = Math.max(1, module.getLowAlert().getMinimum(selected.getKey()) - 16);
                    module.getLowAlert().setMinimum(selected.getKey(), next);
                    module.saveSettings();
                }
                break;
            case 2:
                if (selected != null) {
                    int current = module.getLowAlert().getMinimum(selected.getKey());
                    module.getLowAlert().setMinimum(selected.getKey(), current + 16);
                    module.saveSettings();
                }
                break;
            case 3:
                if (selected != null) {
                    module.getLowAlert().remove(selected.getKey());
                    module.saveSettings();
                }
                break;
            case 4:
                module.saveSettings();
                mc.displayGuiScreen(parent);
                return;
            default:
                return;
        }
        updateAlertButtons();
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

        int firstY = 78;
        int rowHeight = 25;
        if (mouseX >= width / 2 - 165 && mouseX <= width / 2 + 165) {
            int row = (mouseY - firstY) / rowHeight;
            int rowY = mouseY - firstY;
            if (rowY >= 0 && row < 9 && scrollOffset + row < visibleItems.size()) {
                selectedKey = visibleItems.get(scrollOffset + row).getKey();
                updateAlertButtons();
            }
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            scrollOffset = Math.max(0, Math.min(Math.max(0, visibleItems.size() - 1),
                    scrollOffset + (wheel < 0 ? 1 : -1)));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "Base Storage Ledger", width / 2, 12, 0xFFFFFF);
        searchField.drawTextBox();
        drawString(fontRendererObj, "Search remembered items", width / 2 - 150, 59, 0xAAAAAA);

        int y = 78;
        int end = Math.min(visibleItems.size(), scrollOffset + 9);
        long now = System.currentTimeMillis();
        for (int i = scrollOffset; i < end; i++) {
            StorageLedgerItem item = visibleItems.get(i);
            int rowY = y + (i - scrollOffset) * 25;
            boolean selected = item.getKey().equals(selectedKey);
            if (selected) drawRect(width / 2 - 168, rowY - 2, width / 2 + 168,
                    rowY + 21, 0x553F78A8);

            boolean low = module.getLowAlert().isLow(item);
            String prefix = low ? "[LOW] " : "";
            String line = prefix + item.getDisplayName() + "  x" + item.getTotalCount()
                    + "  (" + item.getChestCount() + " chest(s))";
            drawString(fontRendererObj, line, width / 2 - 160, rowY,
                    low ? 0xFFFF6666 : 0xFFFFFFFF);

            String detail = "Minimum: " + module.getLowAlert().getMinimum(item.getKey())
                    + "  ·  " + (item.isStale(now, module.getStaleAfterMillis())
                    ? "Snapshot may be outdated" : "Updated ")
                    + (item.isStale(now, module.getStaleAfterMillis()) ? "" :
                    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                            .format(new Date(item.getLastObservedAt())));
            drawString(fontRendererObj, detail, width / 2 - 150, rowY + 11, 0xFFAAAAAA);
        }

        if (visibleItems.isEmpty()) {
            drawCenteredString(fontRendererObj, "No remembered stock found",
                    width / 2, height / 2, 0xFFAAAAAA);
        }

        drawCenteredString(fontRendererObj,
                module.getIndex().size() + " item type(s)  ·  "
                        + module.getIndex().getTotalItemCount() + " total item(s)",
                width / 2, height - 32, 0xFFAAAAAA);
        drawCenteredString(fontRendererObj,
                "Stock is based on the last remembered chest snapshots, not live contents.",
                width / 2, height - 18, 0xFF888888);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
