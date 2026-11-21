package com.alpharip.staffmod.resourcepackorganizer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Standalone prototype screen. It is intentionally not registered in AlphaClient
 * or injected into Minecraft's vanilla resource-pack screen yet.
 */
public final class ResourcePackOrganizerGui extends GuiScreen {
    private static final int CATEGORY_BUTTON_BASE = 10;
    private static final int PACK_BUTTON_BASE = 100;
    private static final int CREATE_CATEGORY = 200;
    private static final int MOVE_TO_CATEGORY = 201;
    private static final int MOVE_TO_ROOT = 202;
    private static final int PREVIOUS_PAGE = 203;
    private static final int NEXT_PAGE = 204;
    private static final int BACK = 205;

    private final GuiScreen parent;
    private final ResourcePackOrganizer organizer;
    private final List<String> visibleCategories = new ArrayList<String>();
    private final List<ResourcePackEntry> visiblePacks = new ArrayList<ResourcePackEntry>();
    private GuiTextField searchField;
    private GuiTextField categoryField;
    private String selectedCategory = "*";
    private int selectedPack = -1;
    private int page;
    private String status = "Select a pack and a destination category.";
    private boolean searchFocused;
    private boolean categoryFocused;

    public ResourcePackOrganizerGui(GuiScreen parent) {
        this.parent = parent;
        this.organizer = new ResourcePackOrganizer(Minecraft.getMinecraft().mcDataDir);
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int left = Math.max(8, width / 2 - 230);
        int top = Math.max(8, height / 2 - 145);

        searchField = new GuiTextField(1, fontRendererObj, left + 130, top + 27, 180, 18);
        searchField.setMaxStringLength(64);
        searchField.setFocused(searchFocused);
        searchField.setTextColor(0xFFE8E8E8);
        searchField.setEnableBackgroundDrawing(true);

        categoryField = new GuiTextField(2, fontRendererObj, left + 10, top + 247, 150, 18);
        categoryField.setMaxStringLength(32);
        categoryField.setFocused(categoryFocused);
        categoryField.setTextColor(0xFFE8E8E8);
        categoryField.setEnableBackgroundDrawing(true);

        buttonList.add(new GuiButton(CREATE_CATEGORY, left + 166, top + 247, 104, 20, "Create group"));
        buttonList.add(new GuiButton(MOVE_TO_CATEGORY, left + 276, top + 247, 108, 20, "Move to group"));
        buttonList.add(new GuiButton(MOVE_TO_ROOT, left + 276, top + 270, 108, 20, "Move to root"));
        buttonList.add(new GuiButton(PREVIOUS_PAGE, left + 10, top + 270, 72, 20, "< Previous"));
        buttonList.add(new GuiButton(NEXT_PAGE, left + 86, top + 270, 72, 20, "Next >"));
        buttonList.add(new GuiButton(BACK, left + 166, top + 270, 104, 20, "Back"));

        refreshLists();
        rebuildListButtons(left, top);
    }

    private void refreshLists() {
        visibleCategories.clear();
        visibleCategories.add("*");
        visibleCategories.addAll(organizer.getCategories());
        visiblePacks.clear();
        visiblePacks.addAll(organizer.getPacks(selectedCategory, searchField == null ? "" : searchField.getText()));
        int maxPage = Math.max(0, (visiblePacks.size() - 1) / 8);
        page = Math.max(0, Math.min(page, maxPage));
        if (selectedPack >= visiblePacks.size()) selectedPack = -1;
    }

    private void rebuildListButtons(int left, int top) {
        buttonList.removeIf(button -> button.id >= CATEGORY_BUTTON_BASE && button.id < PACK_BUTTON_BASE
                || button.id >= PACK_BUTTON_BASE && button.id < CREATE_CATEGORY);

        int categoryY = top + 58;
        int categoryCount = Math.min(7, visibleCategories.size());
        for (int i = 0; i < categoryCount; i++) {
            String category = visibleCategories.get(i);
            String label = "*".equals(category) ? "All packs" : category;
            buttonList.add(new GuiButton(CATEGORY_BUTTON_BASE + i, left + 10, categoryY, 105, 20, label));
            categoryY += 23;
        }

        int start = page * 8;
        int end = Math.min(visiblePacks.size(), start + 8);
        int packY = top + 58;
        for (int i = start; i < end; i++) {
            ResourcePackEntry pack = visiblePacks.get(i);
            String label = pack.getName() + "  [" + (pack.getCategory().length() == 0 ? "Root" : pack.getCategory()) + "]";
            if (label.length() > 48) label = label.substring(0, 45) + "...";
            buttonList.add(new GuiButton(PACK_BUTTON_BASE + (i - start), left + 130, packY, 254, 20, label));
            packY += 23;
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (!button.enabled) return;
        int left = Math.max(8, width / 2 - 230);
        int top = Math.max(8, height / 2 - 145);

        if (button.id >= CATEGORY_BUTTON_BASE && button.id < CATEGORY_BUTTON_BASE + 7) {
            int index = button.id - CATEGORY_BUTTON_BASE;
            if (index < visibleCategories.size()) {
                selectedCategory = visibleCategories.get(index);
                selectedPack = -1;
                page = 0;
                refreshLists();
                rebuildListButtons(left, top);
            }
            return;
        }
        if (button.id >= PACK_BUTTON_BASE && button.id < PACK_BUTTON_BASE + 8) {
            int index = page * 8 + button.id - PACK_BUTTON_BASE;
            selectedPack = index < visiblePacks.size() ? index : -1;
            status = selectedPack >= 0 ? "Selected: " + visiblePacks.get(selectedPack).getName() : status;
            return;
        }

        switch (button.id) {
            case CREATE_CATEGORY:
                categoryFocused = false;
                searchFocused = false;
                if (organizer.createCategory(categoryField.getText())) {
                    status = "Group created.";
                    categoryField.setText("");
                    refreshLists();
                    rebuildListButtons(left, top);
                } else {
                    status = "Group name is invalid or already exists.";
                }
                break;
            case MOVE_TO_CATEGORY:
            case MOVE_TO_ROOT:
                if (selectedPack < 0 || selectedPack >= visiblePacks.size()) {
                    status = "Select a pack first.";
                    break;
                }
                String destination = button.id == MOVE_TO_ROOT ? "" : selectedCategory;
                if ("*".equals(destination)) {
                    status = "Choose a specific group before moving a pack.";
                    break;
                }
                try {
                    if (organizer.movePack(visiblePacks.get(selectedPack), destination)) {
                        status = "Pack moved.";
                        selectedPack = -1;
                        refreshLists();
                        rebuildListButtons(left, top);
                    } else {
                        status = "Pack could not be moved. Check its location and destination.";
                    }
                } catch (IOException exception) {
                    status = "Move failed: " + exception.getMessage();
                }
                break;
            case PREVIOUS_PAGE:
                page = Math.max(0, page - 1);
                refreshLists();
                rebuildListButtons(left, top);
                break;
            case NEXT_PAGE:
                page = Math.min(Math.max(0, (visiblePacks.size() - 1) / 8), page + 1);
                refreshLists();
                rebuildListButtons(left, top);
                break;
            case BACK:
                mc.displayGuiScreen(parent);
                break;
            default:
                break;
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (searchField.textboxKeyTyped(typedChar, keyCode)) {
            page = 0;
            refreshLists();
            rebuildListButtons(Math.max(8, width / 2 - 230), Math.max(8, height / 2 - 145));
        } else if (categoryField.textboxKeyTyped(typedChar, keyCode)) {
            // Category name is entered separately from the pack search.
        } else if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(parent);
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        searchField.mouseClicked(mouseX, mouseY, mouseButton);
        categoryField.mouseClicked(mouseX, mouseY, mouseButton);
        searchFocused = searchField.isFocused();
        categoryFocused = categoryField.isFocused();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int left = Math.max(8, width / 2 - 230);
        int top = Math.max(8, height / 2 - 145);
        drawRect(left, top, left + 400, top + 300, 0xE0101010);
        drawRect(left, top, left + 400, top + 1, 0xFFFFD866);
        drawString(fontRendererObj, "RESOURCE PACK ORGANIZER", left + 10, top + 10, 0xFFFFD866);
        drawString(fontRendererObj, "Search", left + 130, top + 17, 0xFFAAAAAA);
        searchField.drawTextBox();
        drawString(fontRendererObj, "GROUPS", left + 10, top + 43, 0xFFFFD866);
        drawString(fontRendererObj, "PACKS", left + 130, top + 43, 0xFFFFD866);
        drawString(fontRendererObj, "New group", left + 10, top + 237, 0xFFAAAAAA);
        categoryField.drawTextBox();
        drawString(fontRendererObj, status, left + 10, top + 224, 0xFFDDDDDD);
        drawString(fontRendererObj, "Page " + (page + 1) + " / " + Math.max(1, (visiblePacks.size() + 7) / 8),
                left + 170, top + 277, 0xFFAAAAAA);
        drawString(fontRendererObj, "Note: nested packs need a Minecraft loader bridge to appear in the vanilla pack screen.",
                left + 10, top + 293, 0xFFFFC56B);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
