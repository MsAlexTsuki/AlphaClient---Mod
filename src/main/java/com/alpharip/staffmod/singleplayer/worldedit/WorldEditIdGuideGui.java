// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.singleplayer.worldedit;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Searchable reference guide for block/item identifiers and Alpha World Edit tools. */
public final class WorldEditIdGuideGui extends GuiScreen {
    private static final int ROW_HEIGHT = 34;
    private static final int FIRST_TAB_ID = 10;
    private static final String[] TAB_NAMES = {
            "Blocks", "Items", "Brushes", "Tools", "Pickaxe", "Commands"
    };

    private final GuiScreen parent;
    private final List<GuideEntry> allEntries = new ArrayList<GuideEntry>();
    private final List<GuideEntry> visibleEntries = new ArrayList<GuideEntry>();
    private GuiTextField searchField;
    private int selectedTab;
    private int scrollOffset;
    private int listTop;
    private int listBottom;
    private String status = "Click an entry to copy its identifier or mode.";

    public WorldEditIdGuideGui(GuiScreen parent) {
        this.parent = parent;
        rebuildEntries();
        filterEntries();
    }

    private void rebuildEntries() {
        allEntries.clear();
        if (selectedTab == 0) {
            for (Object key : Block.blockRegistry.getKeys()) {
                String id = String.valueOf(key);
                Block block = Block.getBlockFromName(id);
                if (block == null) continue;
                int numericId = Block.getIdFromBlock(block);
                allEntries.add(new GuideEntry(id,
                        "Numeric block ID: " + numericId + "  |  Parser metadata: 0-15",
                        id));
            }
            for (Object key : Block.blockRegistry.getKeys()) {
                String id = String.valueOf(key);
                Block block = Block.getBlockFromName(id);
                if (block != null) {
                    addBlockMetadataVariants(id, block, Block.getIdFromBlock(block));
                }
            }
            Collections.sort(allEntries, new Comparator<GuideEntry>() {
                @Override public int compare(GuideEntry a, GuideEntry b) {
                    return a.title.compareToIgnoreCase(b.title);
                }
            });
        } else if (selectedTab == 1) {
            for (Object key : Item.itemRegistry.getKeys()) {
                String id = String.valueOf(key);
                Item item = Item.itemRegistry.getObject((ResourceLocation) key);
                if (item == null) continue;
                int numericId = Item.getIdFromItem(item);
                allEntries.add(new GuideEntry(id, "Numeric item ID: " + numericId
                        + "  |  Item IDs are not block IDs", id));
            }
            Collections.sort(allEntries, new Comparator<GuideEntry>() {
                @Override public int compare(GuideEntry a, GuideEntry b) {
                    return a.title.compareToIgnoreCase(b.title);
                }
            });
        } else if (selectedTab == 2) {
            add("sphere", "Right-click brush; uses the configured block and radius.");
            add("cylinder", "Right-click brush; uses the configured block and radius.");
            add("clipboard", "Paste the current clipboard at the clicked position.");
            add("smooth", "Smooth terrain around the clicked position.");
            add("ex", "Extinguish fire around the clicked position.");
            add("gravity", "Apply the Alpha gravity operation around the clicked position.");
            add("butcher", "Remove nearby mobs.");
            add("deform", "Deform terrain around the clicked position.");
            add("set", "Place a sphere using the configured block.");
            add("paint", "Place a sphere using the configured block.");
            add("apply", "Place a sphere using the configured block.");
            add("forest", "Generate trees around the clicked position.");
            add("raise", "Raise terrain around the clicked position.");
            add("lower", "Lower terrain around the clicked position.");
            add("none", "Disable the active brush.");
        } else if (selectedTab == 3) {
            add("info", "Show the clicked block registry ID and metadata.");
            add("tree", "Generate a tree on right-click.");
            add("repl", "Replace nearby blocks; requires <from> and <to> block IDs.");
            add("cycler", "Cycle the clicked block's metadata state.");
            add("floodfill", "Fill connected air; optional block ID, defaults to stone.");
            add("deltree", "Remove nearby log and leaf blocks.");
            add("farwand", "Set selection positions with left/right-click.");
            add("lrbuild", "Place separate block IDs on left-click and right-click.");
            add("none", "Disable the active tool.");
        } else if (selectedTab == 4) {
            add("single", "Remove the single clicked block.");
            add("area", "Remove blocks in the configured area.");
            add("recursive", "Remove matching blocks recursively.");
            add("none", "Disable the Super Pickaxe.");
        } else {
            add("//acwe gmask <block|off>", "Set or clear the global block mask.");
            add("//acwe mask <block|off>", "Alias for gmask.");
            add("//acwe count <block>", "Count matching blocks in the selected region.");
            add("//acwe set <block>", "Fill the selected region with one block.");
            add("//acwe replace <from> <to>", "Replace one block ID with another.");
            add("//acwe walls <block>", "Build the four side walls.");
            add("//acwe outline <block>", "Build the region outline.");
            add("//acwe faces <block>", "Alias for outline.");
            add("//acwe hollow <block>", "Build a hollow shell.");
            add("//acwe line <block>", "Draw a line between the selected corners.");
            add("//acwe center <block>", "Place one block at the selection center.");
            add("//acwe overlay <block>", "Place a surface layer.");
            add("//acwe sphere <block> <radius>", "Generate a solid sphere.");
            add("//acwe hsphere <block> <radius>", "Generate a hollow sphere.");
            add("//acwe cyl <block> <radius> [height]", "Generate a solid cylinder.");
            add("//acwe hcyl <block> <radius> [height]", "Generate a hollow cylinder.");
            add("//acwe pyramid <block> <size>", "Generate a solid pyramid.");
            add("//acwe hpyramid <block> <size>", "Generate a hollow pyramid.");
            add("//acwe replacenear <from> <to> [radius]", "Replace nearby matching blocks.");
            add("//acwe fill <block> [depth]", "Fill air downward using a block.");
            add("//acwe fillr <block> [radius]", "Flood-fill connected air using a block.");
            add("//acwe removenear <block> [radius]", "Remove nearby matching blocks.");
            add("//acwe generate <block> <expression>", "Generate blocks using an arithmetic expression.");
            add("//acwe curve <block>", "Draw a curve between the selected corners.");
            add("//acwe brush <type> [block] [radius]", "Optional block argument for a configured brush.");
            add("//acwe tool floodfill [block]", "Optional fill block; defaults to stone.");
            add("//acwe tool repl <from> <to>", "Requires two block IDs.");
            add("//acwe tool lrbuild <left-block> <right-block>", "Requires two block IDs.");
        }
    }

    /**
     * Adds every distinct metadata-backed block state exposed by the 1.8.9 block.
     * The identifier is kept in the same block-id:metadata format accepted by the parser.
     */
    private void addBlockMetadataVariants(String blockId, Block block, int numericId) {
        Set<IBlockState> seenStates = new HashSet<IBlockState>();

        for (int metadata = 0; metadata <= 15; metadata++) {
            IBlockState state = block.getStateFromMeta(metadata);
            if (state == null || state.getPropertyNames().isEmpty() || !seenStates.add(state)) {
                continue;
            }

            StringBuilder properties = new StringBuilder();
            for (Object propertyObject : state.getPropertyNames()) {
                IProperty property = (IProperty) propertyObject;
                Comparable value = (Comparable) state.getValue(property);
                if (properties.length() > 0) properties.append(", ");
                properties.append(property.getName()).append("=")
                        .append(property.getName(value));
            }

            String identifier = blockId + ":" + metadata;
            String title = blockId + ":" + metadata + " (" + properties + ")";
            String description = "Numeric block ID: " + numericId + "  |  Metadata: " + metadata
                    + "  |  State: " + properties;
            allEntries.add(new GuideEntry(title, description, identifier));
        }
    }

    private void add(String title, String description) {
        allEntries.add(new GuideEntry(title, description, title));
    }

    private void filterEntries() {
        visibleEntries.clear();
        String query = searchField == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        for (GuideEntry entry : allEntries) {
            if (query.isEmpty() || entry.title.toLowerCase(Locale.ROOT).contains(query)
                    || entry.description.toLowerCase(Locale.ROOT).contains(query)) {
                visibleEntries.add(entry);
            }
        }
        scrollOffset = 0;
        clampScroll();
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int panelWidth = Math.min(760, width - 24);
        int left = (width - panelWidth) / 2;
        int tabWidth = 88;
        int totalTabsWidth = tabWidth * TAB_NAMES.length;
        int tabStart = (width - totalTabsWidth) / 2;
        for (int i = 0; i < TAB_NAMES.length; i++) {
            buttonList.add(new GuiButton(FIRST_TAB_ID + i, tabStart + i * tabWidth,
                    46, tabWidth - 3, 20, TAB_NAMES[i]));
        }
        buttonList.add(new GuiButton(1, left + 8, height - 28, 70, 20, "Back"));
        searchField = new GuiTextField(0, fontRendererObj, left + 12, 72, panelWidth - 24, 18);
        searchField.setMaxStringLength(80);
        searchField.setFocused(true);
        listTop = 96;
        listBottom = height - 36;
        filterEntries();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == null) return;
        if (button.id == 1) {
            mc.displayGuiScreen(parent);
        } else if (button.id >= FIRST_TAB_ID && button.id < FIRST_TAB_ID + TAB_NAMES.length) {
            selectedTab = button.id - FIRST_TAB_ID;
            status = "Click an entry to copy its identifier or mode.";
            rebuildEntries();
            filterEntries();
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            mc.displayGuiScreen(parent);
            return;
        }
        if (searchField != null && searchField.textboxKeyTyped(typedChar, keyCode)) {
            filterEntries();
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (searchField != null) searchField.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton != 0 || mouseY < listTop || mouseY >= listBottom) return;
        int index = scrollOffset + (mouseY - listTop) / ROW_HEIGHT;
        if (index >= 0 && index < visibleEntries.size()) {
            GuideEntry entry = visibleEntries.get(index);
            if (entry.copyValue != null && !entry.copyValue.isEmpty()) {
                setClipboardString(entry.copyValue);
                status = "Copied: " + entry.copyValue;
            }
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (searchField != null) searchField.updateCursorCounter();
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            scrollOffset += wheel > 0 ? -3 : 3;
            clampScroll();
        }
    }

    private void clampScroll() {
        int visibleRows = Math.max(1, (listBottom - listTop) / ROW_HEIGHT);
        int maximum = Math.max(0, visibleEntries.size() - visibleRows);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maximum));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int panelWidth = Math.min(760, width - 24);
        int left = (width - panelWidth) / 2;
        int right = left + panelWidth;
        listTop = 96;
        listBottom = height - 36;

        drawRect(left, 12, right, listBottom + 4, 0xE00A0C11);
        drawRect(left, 12, right, 14, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "SINGLEPLAYER WORLD EDIT", width / 2, 20, 0xFFF3F4F6);
        drawCenteredString(fontRendererObj, "Block IDs, item IDs and tools guide", width / 2, 34, 0xFFB8B8B8);
        drawRect(left + 8, listTop - 3, right - 8, listBottom, 0xAA111318);

        if (searchField != null) {
            searchField.drawTextBox();
            drawString(fontRendererObj, "Search", left + 12, 61, 0xFFBDBDBD);
        }

        int visibleRows = Math.max(1, (listBottom - listTop) / ROW_HEIGHT);
        int end = Math.min(visibleEntries.size(), scrollOffset + visibleRows);
        int y = listTop;
        for (int i = scrollOffset; i < end; i++) {
            GuideEntry entry = visibleEntries.get(i);
            drawRect(left + 9, y, right - 9, y + ROW_HEIGHT - 1, 0x33222222);
            drawString(fontRendererObj, trim(entry.title, panelWidth - 34),
                    left + 16, y + 4, 0xFFFFFFFF);
            drawString(fontRendererObj, trim(entry.description, panelWidth - 34),
                    left + 16, y + 18, 0xFFBDBDBD);
            y += ROW_HEIGHT;
        }
        if (visibleEntries.isEmpty()) {
            drawCenteredString(fontRendererObj, "No matching entries.", width / 2, listTop + 12, 0xFFBDBDBD);
        }

        drawString(fontRendererObj, trim(status, panelWidth - 100), left + 88,
                height - 22, 0xFFAAAAAA);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private String trim(String value, int maxPixels) {
        String result = value == null ? "" : value;
        while (!result.isEmpty() && fontRendererObj.getStringWidth(result) > maxPixels) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    private static final class GuideEntry {
        private final String title;
        private final String description;
        private final String copyValue;

        private GuideEntry(String title, String description, String copyValue) {
            this.title = title;
            this.description = description;
            this.copyValue = copyValue;
        }
    }
}
