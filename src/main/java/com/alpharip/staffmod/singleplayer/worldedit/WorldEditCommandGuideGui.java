// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.singleplayer.worldedit;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Read-only command guide for the AlphaClient Singleplayer World Edit commands. */
public final class WorldEditCommandGuideGui extends GuiScreen {
    private static final int ROW_HEIGHT = 34;
    private static final int HEADER_HEIGHT = 20;

    private final GuiScreen parent;
    private final List<GuideRow> rows = new ArrayList<GuideRow>();
    private int scrollOffset;
    private int listTop;
    private int listBottom;

    public WorldEditCommandGuideGui(GuiScreen parent) {
        this.parent = parent;
        addSection("GENERAL");
        addCommand("//acwe help", "Open this command guide.");
        addCommand("//acwe logs", "Open the local seven-day audit log of Alpha World Edit changes.");

        addSection("NAVIGATION");
        addCommand("//acwe unstuck", "If embedded in blocks, move upward to the nearest clear position.");
        addCommand("//acwe !", "Alias for unstuck.");
        addCommand("//acwe ascend [levels]", "Move to one or more safe floors above your current position.");
        addCommand("//acwe asc [levels]", "Alias for ascend.");
        addCommand("//acwe descend [levels]", "Move to one or more safe floors below your current position.");
        addCommand("//acwe desc [levels]", "Alias for descend.");
        addCommand("//acwe thru", "Move through a wall in the direction you are looking, when a clear destination is found.");
        addCommand("//acwe jumpto", "Teleport above the block you are looking at.");
        addCommand("//acwe j", "Alias for jumpto.");
        addCommand("//acwe up [distance]", "Move vertically upward and place a glass support block if needed.");
        addCommand("//acwe ceil [clearance]", "Move above the highest block at your position with a glass support block if needed.");

        addSection("SELECTION");
        addCommand("//acwe wand", "Receive the dedicated World Edit Wand. Left-click a block for position 1; right-click for position 2.");
        addCommand("//acwe toggleeditwand", "Enable or disable selection clicks from the dedicated World Edit Wand.");
        addCommand("//acwe pos1", "Set position 1 to your current player position.");
        addCommand("//acwe pos2", "Set position 2 to your current player position.");
        addCommand("//acwe desel", "Clear the current selection.");
        addCommand("//acwe deselect", "Alias for //acwe desel.");

        addSection("REGION");
        addCommand("//acwe set <block>", "Fill the selected region with the specified block.");
        addCommand("//acwe replace <from> <to>", "Replace matching blocks in the selected region.");
        addCommand("//acwe walls <block>", "Build the four side walls of the selected region.");
        addCommand("//acwe outline <block>", "Build the outline of the selected region.");
        addCommand("//acwe hollow <block>", "Build a hollow shell around the selected region.");

        addSection("CLIPBOARD");
        addCommand("//acwe copy", "Copy the selected region to the Alpha World Edit clipboard.");
        addCommand("//acwe paste", "Paste the clipboard at your current player position.");
        addCommand("//acwe clearclipboard", "Clear the Alpha World Edit clipboard.");
        addCommand("//acwe cut", "Copy the selected region and clear its original blocks.");
        addCommand("//acwe rotate <90|180|270>", "Rotate the clipboard around the vertical axis.");
        addCommand("//acwe flip <x|y|z>", "Flip the clipboard on the selected axis.");
        addCommand("//acwe schematic save <name>", "Save the clipboard in AlphaClient's compressed .acwe format.");
        addCommand("//acwe schematic load <name>", "Load an AlphaClient .acwe or classic Minecraft 1.8.9 .schematic file into the clipboard.");
        addCommand("//acwe schematic delete <name>", "Delete a saved AlphaClient schematic.");
        addCommand("//acwe schematic list", "List saved AlphaClient schematics.");
        addCommand("//acwe schematic formats", "Show supported formats: classic MCEdit/WorldEdit 1.8.9 .schematic and AlphaClient .acwe.");


        addSection("SELECTION TOOLS");
        addCommand("//acwe hpos1 / hpos2", "Set a selection corner to the block you are looking at.");
        addCommand("//acwe chunk", "Select the chunk containing your player position.");
        addCommand("//acwe size", "Show the selected region dimensions and volume.");
        addCommand("//acwe count <block>", "Count matching blocks in the selected region.");
        addCommand("//acwe distr [page]", "Show block types in the selected region, sorted by frequency, with 10 entries per page.");
        addCommand("//acwe expand <amount> [direction]", "Expand the selection in a direction.");
        addCommand("//acwe contract <amount> [direction]", "Contract the selection in a direction.");
        addCommand("//acwe shift <amount> [direction]", "Move the selection without moving blocks.");
        addCommand("//acwe outset [amount]", "Expand the selection on all sides.");
        addCommand("//acwe inset [amount]", "Shrink the selection on all sides.");

        addSection("BIOME");
        addCommand("//acwe biomelist", "List available registered biomes.");
        addCommand("//acwe biomeinfo", "Show the biome at your target block or player position.");
        addCommand("//acwe setbiome <name>", "Change the biome of the chunk containing your player.");

        addSection("REGION OPERATIONS");
        addCommand("//acwe line <block>", "Draw a line between the two selected corners.");
        addCommand("//acwe center <block>", "Place a block at the center of the selection.");
        addCommand("//acwe overlay <block>", "Place a surface layer across the selected columns.");
        addCommand("//acwe naturalize", "Apply grass and dirt to the top layers of selected terrain.");
        addCommand("//acwe smooth [iterations]", "Smooth the selected terrain (1-5 iterations).");
        addCommand("//acwe move <x> <y> <z>", "Move the selected blocks by an offset.");
        addCommand("//acwe stack <count> [dx dy dz]", "Repeat the selected region in an offset direction.");
        addCommand("//acwe faces <block>", "Alias for the selected-region outline operation.");

        addSection("GENERATION");
        addCommand("//acwe sphere <block> <radius>", "Generate a solid sphere around your player.");
        addCommand("//acwe hsphere <block> <radius>", "Generate a hollow sphere around your player.");
        addCommand("//acwe cyl <block> <radius> [height]", "Generate a solid vertical cylinder.");
        addCommand("//acwe hcyl <block> <radius> [height]", "Generate a hollow vertical cylinder.");
        addCommand("//acwe pyramid <block> <size>", "Generate a solid pyramid.");
        addCommand("//acwe hpyramid <block> <size>", "Generate a hollow pyramid.");

        addSection("GENERAL");
        addCommand("//acwe limit <blocks>", "Set the maximum permitted region volume.");
        addCommand("//acwe gmask <block|off>", "Set or clear the global block mask used by set.");
        addCommand("//acwe searchitem <text>", "Search registered item identifiers.");
        addCommand("//acwe calc <a><operator><b>", "Calculate a basic integer expression.");
        addCommand("//acwe ceil", "Move above the highest block at your current position.");
        addCommand("//acwe generatebiome <name>", "Set the biome across the selected columns.");

        addSection("FILL UTILITIES");
        addCommand("//acwe fill <block> [depth]", "Fill air downward from the block above your target.");
        addCommand("//acwe fillr <block> [radius]", "Flood-fill connected air within a bounded radius.");

        addSection("CHUNKS");
        addCommand("//acwe listchunks", "List chunk coordinates around you or inside the current selection, with loaded status.");
        addCommand("//acwe delchunks", "Clear all blocks in your current chunk; the chunk remains loaded.");

        addSection("ADVANCED REGION");
        addCommand("//acwe curve <block>", "Draw a quadratic curve from pos1 to pos2 using your position as the control point.");
        addCommand("//acwe deform <amplitude> [frequency]", "Apply bounded sinusoidal terrain deformation to the selection.");
        addCommand("//acwe regen", "Rebuild the selected area as generated rolling terrain.");
        addCommand("//acwe forest [density]", "Generate trees in the selected area.");
        addCommand("//acwe flora [count]", "Scatter grass and flowers in the selected area.");

        addSection("GENERATION");
        addCommand("//acwe forestgen <radius> [tree-count]", "Generate a bounded forest around your position.");
        addCommand("//acwe pumpkins [count]", "Scatter pumpkins on suitable surfaces in the selection.");
        addCommand("//acwe generate <block> <expression>", "Generate blocks where an arithmetic x/y/z expression evaluates to 0 or less.");

        addSection("SCRIPTING");
        addCommand("//acwe cs <name>", "Create a local .acs script template under AlphaWorldEdit/scripts.");
        addCommand("//acwe .s <name>", "Execute up to 64 Alpha World Edit commands from a local script.");

        addSection("BRUSH");
        addCommand("//acwe brush <type> [block] [radius]", "Configure a right-click brush. Types: sphere, cylinder, clipboard, smooth, ex, gravity, butcher, deform, set, paint, apply, forest, raise, lower, none.");
        addCommand("//acwe br <type> [block] [radius]", "Alias for brush.");
        addCommand("//acwe superpickaxe <single|area|recursive>", "Configure left-click block removal modes.");
        addCommand("//acwe sp <single|area|recursive>", "Alias for superpickaxe.");
        addCommand("//acwe superpickaxe none", "Disable the active Super Pickaxe mode.");

        addSection("TOOLS");
        addCommand("//acwe tool <none|info|tree|repl|cycler|floodfill|deltree|farwand>", "Assign a click tool to your interactions.");
        addCommand("//acwe tool repl <from> <to>", "Replace matching nearby blocks on right-click.");
        addCommand("//acwe tool floodfill <block>", "Fill connected air from the clicked block.");
        addCommand("//acwe tool lrbuild <left-block> <right-block>", "Place one block on left-click and another on right-click.");
        addCommand("//acwe tool none", "Disable the currently selected tool.");

        addSection("BRUSH SETTINGS");
        addCommand("//acwe mat <block>", "Set the material used by compatible brushes.");
        addCommand("//acwe material <block>", "Alias for mat.");
        addCommand("//acwe range <1-16>", "Set the active brush radius.");
        addCommand("//acwe toggleplace", "Toggle brush placement between the targeted block and the adjacent block.");

        addSection("SNAPSHOTS");
        addCommand("//acwe snapshot before <name>", "Save the selected region as a separate named before snapshot.");
        addCommand("//acwe snapshot after <name>", "Save the selected region as a separate named after snapshot.");
        addCommand("//acwe snapshot list", "List saved snapshots.");
        addCommand("//acwe snapshot use <name>", "Load a snapshot into the clipboard.");
        addCommand("//acwe snapshot sel <name>", "Restore the selection bounds stored in a snapshot.");
        addCommand("//acwe restore <name>", "Restore a saved snapshot to its original coordinates.");

        addSection("HISTORY");
        addCommand("//acwe undo", "Restore the previous recorded block state.");
        addCommand("//acwe redo", "Reapply the last undone block state.");
        addCommand("//acwe clearhistory", "Clear the in-memory undo and redo history.");

        addSection("LOCAL UTILITIES");
        addCommand("//acwe drain [radius]", "Remove liquid blocks in a bounded radius around you.");
        addCommand("//acwe extinguish [radius]", "Remove fire blocks in a bounded radius around you.");
        addCommand("//acwe removeabove [distance]", "Clear blocks above your position in a bounded column area.");
        addCommand("//acwe removebelow [distance]", "Clear blocks below your position in a bounded column area.");
        addCommand("//acwe removenear <block> [radius]", "Remove matching blocks near your position.");
        addCommand("//acwe replacenear <from> <to> [radius]", "Replace matching nearby blocks.");
        addCommand("//acwe snow", "Add snow layers to exposed selected surfaces.");
        addCommand("//acwe thaw", "Remove snow layers and melt ice in the selected area.");
        addCommand("//acwe green", "Convert exposed dirt surfaces in the selected area to grass.");
        addCommand("//acwe fixwater", "Convert flowing water in the selection to source water.");
        addCommand("//acwe fixlava", "Convert flowing lava in the selection to source lava.");
        addCommand("//acwe remove", "Clear all blocks in the selected region.");
        addCommand("//acwe butcher [-a] [radius]", "Remove hostile mobs nearby; -a also includes passive animals.");
    }

    private void addSection(String title) {
        rows.add(new GuideRow(title, null, true));
    }

    private void addCommand(String syntax, String description) {
        rows.add(new GuideRow(null, syntax, description, false));
    }

    @Override
    public void initGui() {
        buttonList.clear();
        buttonList.add(new GuiButton(1, 8, height - 28, 70, 20, "Back"));
        listTop = 48;
        listBottom = height - 36;
        clampScroll();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button != null && button.id == 1) {
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            mc.displayGuiScreen(parent);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            scrollOffset += wheel > 0 ? -2 : 2;
            clampScroll();
        }
    }

    private void clampScroll() {
        int availableHeight = Math.max(1, listBottom - listTop);
        int maximumOffset = 0;
        for (int offset = 0; offset < rows.size(); offset++) {
            int remainingHeight = 0;
            for (int i = offset; i < rows.size(); i++) {
                remainingHeight += rows.get(i).section ? HEADER_HEIGHT : ROW_HEIGHT;
            }
            if (remainingHeight <= availableHeight) {
                maximumOffset = offset;
                break;
            }
            maximumOffset = offset + 1;
        }
        scrollOffset = Math.max(0, Math.min(scrollOffset, maximumOffset));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        int panelWidth = Math.min(520, width - 24);
        int left = (width - panelWidth) / 2;
        int right = left + panelWidth;
        listTop = 48;
        listBottom = height - 36;

        drawRect(left, 12, right, listBottom + 4, 0xE00A0C11);
        drawRect(left, 12, right, 14, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "SINGLEPLAYER WORLD EDIT", width / 2, 20, 0xFFF3F4F6);
        drawCenteredString(fontRendererObj, "AlphaClient command guide", width / 2, 34, 0xFFB8B8B8);
        drawRect(left + 8, listTop - 3, right - 8, listBottom, 0xAA111318);

        int y = listTop;
        int skipped = 0;
        for (GuideRow row : rows) {
            int rowHeight = row.section ? HEADER_HEIGHT : ROW_HEIGHT;
            if (skipped < scrollOffset) {
                skipped++;
                continue;
            }
            if (y + rowHeight > listBottom) break;

            if (row.section) {
                drawRect(left + 9, y, right - 9, y + HEADER_HEIGHT - 1, 0x553A3020);
                drawString(fontRendererObj, row.title, left + 16, y + 6, 0xFFFFD866);
            } else {
                drawRect(left + 9, y, right - 9, y + ROW_HEIGHT - 1, 0x33222222);
                drawString(fontRendererObj, row.syntax, left + 16, y + 4, 0xFFFFFFFF);
                drawString(fontRendererObj, trim(row.description, panelWidth - 34), left + 16, y + 18, 0xFFBDBDBD);
            }
            y += rowHeight;
        }

        drawString(fontRendererObj, "Scroll to view commands. ESC or Back to close.", 88, height - 22, 0xFFAAAAAA);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private String trim(String value, int maxPixels) {
        String result = value == null ? "" : value;
        while (!result.isEmpty() && fontRendererObj.getStringWidth(result) > maxPixels) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    private static final class GuideRow {
        private final String title;
        private final String syntax;
        private final String description;
        private final boolean section;

        private GuideRow(String title, String syntax, boolean section) {
            this(title, syntax, null, section);
        }

        private GuideRow(String title, String syntax, String description, boolean section) {
            this.title = title;
            this.syntax = syntax;
            this.description = description;
            this.section = section;
        }
    }
}
