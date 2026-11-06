package com.alpharip.staffmod.store.addons;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import java.io.IOException;
import java.util.List;

/** Minecraft 1.8.9 UI preview. Catalogue data must be supplied by the caller. */
public final class AddonStoreGui extends GuiScreen {
    private final GuiScreen parent;
    private final AddonStore store;
    private GuiTextField searchField;
    private int selectedIndex = -1;
    private int scrollOffset;
    private String message = "";

    public AddonStoreGui(GuiScreen parent, AddonStore store) {
        if (store == null) throw new IllegalArgumentException("Addon store is required.");
        this.parent = parent;
        this.store = store;
    }

    @Override public void initGui() {
        buttonList.clear();
        searchField = new GuiTextField(10, fontRendererObj, width / 2 - 110, 39, 220, 18);
        searchField.setMaxStringLength(80);
        buttonList.add(new GuiButton(0, width / 2 - 112, height - 29, 70, 20, "Install"));
        buttonList.add(new GuiButton(1, width / 2 - 37, height - 29, 70, 20, "Disable"));
        buttonList.add(new GuiButton(2, width / 2 + 38, height - 29, 70, 20, "Back"));
    }

    @Override public void updateScreen() { searchField.updateCursorCounter(); }

    @Override protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (searchField.textboxKeyTyped(typedChar, keyCode)) {
            store.getSettings().setSearchQuery(searchField.getText());
            selectedIndex = -1;
            scrollOffset = 0;
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        searchField.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == 0 && mouseY >= 68 && mouseY < height - 38) {
            selectedIndex = scrollOffset + (mouseY - 68) / 34;
        }
    }

    @Override public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = org.lwjgl.input.Mouse.getEventDWheel();
        if (wheel != 0) {
            int max = Math.max(0, store.getVisibleAddons().size() - Math.max(1, (height - 112) / 34));
            scrollOffset = Math.max(0, Math.min(max, scrollOffset + (wheel > 0 ? -1 : 1)));
        }
    }

    @Override protected void actionPerformed(GuiButton button) throws IOException {
        List<AddonEntry> entries = store.getVisibleAddons();
        if (button.id == 0 && selectedIndex >= 0 && selectedIndex < entries.size()) {
            boolean changed = store.getManager().markInstalled(entries.get(selectedIndex).getId());
            message = changed ? "Local install state recorded; no files were downloaded."
                    : "This add-on cannot be marked as installed.";
        } else if (button.id == 1 && selectedIndex >= 0 && selectedIndex < entries.size()) {
            boolean changed = store.getManager().disable(entries.get(selectedIndex).getId());
            message = changed ? "Add-on marked as disabled locally." : "Select an installed add-on.";
        } else if (button.id == 2) {
            mc.displayGuiScreen(parent);
        }
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "ALPHA STORE - ADDONS", width / 2, 14, 0xFFFFFFFF);
        drawCenteredString(fontRendererObj, "Prototype catalogue | no downloads or external code execution",
                width / 2, 27, 0xFFFFD866);
        searchField.drawTextBox();
        drawString(fontRendererObj, "Search", width / 2 - 110, 30, 0xFFB8BDC8);
        List<AddonEntry> entries = store.getVisibleAddons();
        int rows = Math.max(0, (height - 112) / 34);
        for (int i = 0; i < rows && scrollOffset + i < entries.size(); i++) {
            int index = scrollOffset + i;
            AddonEntry entry = entries.get(index);
            int y = 68 + i * 34;
            drawRect(12, y, width - 12, y + 31, index == selectedIndex ? 0xAA665522 : 0x88303030);
            String title = entry.getName() + "  v" + entry.getVersion() + "  [" + entry.getCategory().getDisplayName() + "]";
            drawString(fontRendererObj, fontRendererObj.trimStringToWidth(title, width - 36), 18, y + 4, 0xFFFFFFFF);
            AddonInstallState state = store.getRegistry().getState(entry.getId());
            String details = entry.getAuthor() + " | " + state.name() + " | " + entry.getDescription();
            drawString(fontRendererObj, fontRendererObj.trimStringToWidth(details, width - 36), 18, y + 17, 0xFFB8BDC8);
        }
        if (entries.isEmpty()) drawCenteredString(fontRendererObj, "No matching add-ons. Register local entries first.",
                width / 2, height / 2, 0xFFB8BDC8);
        drawString(fontRendererObj, fontRendererObj.trimStringToWidth(message, width - 24),
                12, height - 43, 0xFFFFD866);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override public boolean doesGuiPauseGame() { return false; }
}
