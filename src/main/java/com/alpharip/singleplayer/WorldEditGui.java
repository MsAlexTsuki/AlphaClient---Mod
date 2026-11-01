// --- Made By MsAlexTsuki ---
package com.alpharip.singleplayer;

import com.alpharip.staffmod.gui.AlphaButton;
import com.alpharip.staffmod.gui.AlphaGuiScreen;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.WorldServer;

import java.io.IOException;

/**
 * WorldEdit-style region editor for local singleplayer worlds.
 * All changes are applied directly to the integrated server world; commands
 * and cheats are not used.
 */
public final class WorldEditGui extends AlphaGuiScreen {
    private static final int BACK = 1;
    private static final int POS1 = 2;
    private static final int POS2 = 3;
    private static final int SET = 4;
    private static final int REPLACE = 5;
    private static final int WALLS = 6;
    private static final int OUTLINE = 7;
    private static final int HOLLOW = 8;
    private static final int COPY = 9;
    private static final int PASTE = 10;

    private static final int ACCENT = 0xFFFFD866;
    private static final int MAX_BLOCKS_PER_OPERATION = 32768;

    private final GuiScreen parent;
    private GuiTextField blockField;
    private GuiTextField replaceField;
    private BlockPos pos1;
    private BlockPos pos2;
    private IBlockState[][][] clipboard;
    private int clipboardX;
    private int clipboardY;
    private int clipboardZ;
    private String status = "Select the two corners of your region.";

    public WorldEditGui(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        if (!isSingleplayerAvailable()) {
            buttonList.add(new AlphaButton(BACK, width / 2 - 50, height - 35, 100, 20, "BACK"));
            return;
        }

        int panelWidth = Math.min(500, width - 20);
        int left = (width - panelWidth) / 2;
        int top = Math.max(10, (height - 320) / 2);
        int innerWidth = panelWidth - 28;
        int gap = 6;
        int half = (innerWidth - gap) / 2;
        int row = top + 126;

        blockField = new GuiTextField(20, fontRendererObj, left + 14, top + 76, half, 18);
        blockField.setMaxStringLength(64);
        blockField.setText("minecraft:stone");
        blockField.setTextColor(0xFFE7E9EE);
        blockField.setDisabledTextColour(0xFF777D88);
        blockField.setEnableBackgroundDrawing(true);

        replaceField = new GuiTextField(21, fontRendererObj, left + 14 + half + gap,
                top + 76, half, 18);
        replaceField.setMaxStringLength(64);
        replaceField.setText("minecraft:dirt");
        replaceField.setTextColor(0xFFE7E9EE);
        replaceField.setDisabledTextColour(0xFF777D88);
        replaceField.setEnableBackgroundDrawing(true);

        buttonList.add(new AlphaButton(POS1, left + 14, top + 101, half, 20, "SET POSITION 1"));
        buttonList.add(new AlphaButton(POS2, left + 14 + half + gap, top + 101,
                half, 20, "SET POSITION 2"));

        int third = (innerWidth - gap * 2) / 3;
        buttonList.add(new AlphaButton(SET, left + 14, row, third, 22, "SET"));
        buttonList.add(new AlphaButton(REPLACE, left + 14 + third + gap, row,
                third, 22, "REPLACE"));
        buttonList.add(new AlphaButton(WALLS, left + 14 + (third + gap) * 2, row,
                third, 22, "WALLS"));

        row += 29;
        buttonList.add(new AlphaButton(OUTLINE, left + 14, row, third, 22, "OUTLINE"));
        buttonList.add(new AlphaButton(HOLLOW, left + 14 + third + gap, row,
                third, 22, "HOLLOW"));
        buttonList.add(new AlphaButton(COPY, left + 14 + (third + gap) * 2, row,
                third, 22, "COPY"));

        row += 29;
        buttonList.add(new AlphaButton(PASTE, left + 14, row, innerWidth, 22, "PASTE AT PLAYER"));
        buttonList.add(new AlphaButton(BACK, left + 14, top + 285, 90, 20, "BACK"));
    }

    private boolean isSingleplayerAvailable() {
        return mc != null && mc.isSingleplayer() && mc.thePlayer != null && mc.theWorld != null
                && mc.getIntegratedServer() != null;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == null) return;
        if (button.id == BACK) {
            mc.displayGuiScreen(parent);
            return;
        }
        if (!isSingleplayerAvailable()) {
            status = "WorldEdit is available only in a loaded singleplayer world.";
            return;
        }

        if (button.id == POS1 || button.id == POS2) {
            BlockPos selected = mc.thePlayer.getPosition();
            if (button.id == POS1) pos1 = selected;
            else pos2 = selected;
            status = (button.id == POS1 ? "Position 1: " : "Position 2: ") + format(selected);
            return;
        }

        if (button.id == PASTE) {
            pasteAtPlayer();
            return;
        }

        if (!hasSelection()) {
            status = "Set both positions before editing.";
            return;
        }

        final Bounds bounds = getBounds();
        if (!isValidVolume(bounds)) return;

        if (button.id == COPY) {
            copySelection(bounds);
            return;
        }

        final IBlockState selectedBlock = parseBlock(blockField == null ? "" : blockField.getText());
        if (selectedBlock == null) {
            status = "Enter a valid block ID (optional metadata: minecraft:stone:0).";
            return;
        }

        final IBlockState replacement = button.id == REPLACE
                ? parseBlock(replaceField == null ? "" : replaceField.getText()) : null;
        if (button.id == REPLACE && replacement == null) {
            status = "Enter a valid replacement block ID.";
            return;
        }

        if (button.id == SET || button.id == REPLACE) {
            applyFill(bounds, selectedBlock, replacement);
        } else if (button.id == WALLS || button.id == OUTLINE || button.id == HOLLOW) {
            applyShape(bounds, selectedBlock, button.id);
        }
    }

    private void applyFill(final Bounds bounds, final IBlockState state, final IBlockState filter) {
        runOnIntegratedServer(new WorldEditTask() {
            @Override
            public void run(WorldServer world) {
                if (!regionLoaded(world, bounds)) { status = "Selection contains unloaded chunks."; return; }
                for (int x = bounds.minX; x <= bounds.maxX; x++)
                    for (int y = bounds.minY; y <= bounds.maxY; y++)
                        for (int z = bounds.minZ; z <= bounds.maxZ; z++) {
                            BlockPos p = new BlockPos(x, y, z);
                            if (filter == null || world.getBlockState(p).getBlock() == filter.getBlock())
                                world.setBlockState(p, state, 2);
                        }
                status = filter == null ? "Region filled without using cheats." : "Matching blocks replaced.";
            }
        });
    }

    private void applyShape(final Bounds b, final IBlockState state, final int operation) {
        runOnIntegratedServer(new WorldEditTask() {
            @Override
            public void run(WorldServer world) {
                if (!regionLoaded(world, b)) { status = "Selection contains unloaded chunks."; return; }
                for (int x = b.minX; x <= b.maxX; x++)
                    for (int y = b.minY; y <= b.maxY; y++)
                        for (int z = b.minZ; z <= b.maxZ; z++) {
                            boolean xEdge = x == b.minX || x == b.maxX;
                            boolean yEdge = y == b.minY || y == b.maxY;
                            boolean zEdge = z == b.minZ || z == b.maxZ;
                            boolean edge;
                            if (operation == WALLS) edge = xEdge || zEdge;
                            else if (operation == OUTLINE) edge = xEdge || yEdge || zEdge;
                            else edge = xEdge || yEdge || zEdge;
                            if (!edge && operation == HOLLOW) {
                                world.setBlockState(new BlockPos(x, y, z),
                                        net.minecraft.init.Blocks.air.getDefaultState(), 2);
                            } else if (edge) {
                                world.setBlockState(new BlockPos(x, y, z), state, 2);
                            }
                        }
                status = operation == WALLS ? "Side walls built."
                        : operation == OUTLINE ? "Region outline built." : "Hollow region built.";
            }
        });
    }

    private void copySelection(final Bounds b) {
        runOnIntegratedServer(new WorldEditTask() {
            @Override
            public void run(WorldServer world) {
                if (!regionLoaded(world, b)) { status = "Selection contains unloaded chunks."; return; }
                int sx = b.sizeX(), sy = b.sizeY(), sz = b.sizeZ();
                IBlockState[][][] copied = new IBlockState[sx][sy][sz];
                for (int x = 0; x < sx; x++)
                    for (int y = 0; y < sy; y++)
                        for (int z = 0; z < sz; z++)
                            copied[x][y][z] = world.getBlockState(
                                    new BlockPos(b.minX + x, b.minY + y, b.minZ + z));
                clipboard = copied;
                clipboardX = sx;
                clipboardY = sy;
                clipboardZ = sz;
                status = "Region copied to the WorldEdit clipboard.";
            }
        });
    }

    private void pasteAtPlayer() {
        if (clipboard == null) { status = "Copy a region before pasting."; return; }
        final BlockPos destination = mc.thePlayer.getPosition();
        final IBlockState[][][] copied = clipboard;
        final int sx = clipboardX, sy = clipboardY, sz = clipboardZ;
        final Bounds target = new Bounds(destination.getX(), destination.getY(), destination.getZ(),
                destination.getX() + sx - 1, destination.getY() + sy - 1, destination.getZ() + sz - 1);
        if (!isValidVolume(target)) return;

        runOnIntegratedServer(new WorldEditTask() {
            @Override
            public void run(WorldServer world) {
                if (!regionLoaded(world, target)) { status = "Paste area contains unloaded chunks."; return; }
                for (int x = 0; x < sx; x++)
                    for (int y = 0; y < sy; y++)
                        for (int z = 0; z < sz; z++)
                            world.setBlockState(new BlockPos(destination.getX() + x,
                                    destination.getY() + y, destination.getZ() + z), copied[x][y][z], 2);
                status = "Clipboard pasted at your position.";
            }
        });
    }

    private void runOnIntegratedServer(final WorldEditTask task) {
        final IntegratedServer server = mc.getIntegratedServer();
        if (server == null) { status = "Integrated server is unavailable."; return; }
        status = "Applying WorldEdit operation...";
        server.addScheduledTask(new Runnable() {
            @Override
            public void run() {
                WorldServer world = server.worldServerForDimension(mc.theWorld.provider.getDimensionId());
                if (world == null) { status = "Singleplayer world is unavailable."; return; }
                task.run(world);
            }
        });
    }

    private boolean regionLoaded(WorldServer world, Bounds b) {
        for (int x = b.minX >> 4; x <= b.maxX >> 4; x++)
            for (int z = b.minZ >> 4; z <= b.maxZ >> 4; z++)
                if (!world.getChunkProvider().chunkExists(x, z)) return false;
        return true;
    }

    private boolean isValidVolume(Bounds b) {
        if (b.minY < 0 || b.maxY > 255) { status = "Selection must stay between Y=0 and Y=255."; return false; }
        long volume = (long) b.sizeX() * b.sizeY() * b.sizeZ();
        if (volume > MAX_BLOCKS_PER_OPERATION) {
            status = "Selection is too large. Maximum: " + MAX_BLOCKS_PER_OPERATION + " blocks.";
            return false;
        }
        return true;
    }

    private IBlockState parseBlock(String input) {
        if (input == null) return null;
        String value = input.trim().toLowerCase(java.util.Locale.ROOT);
        if (!value.matches("[a-z0-9_]+(:[a-z0-9_]+)?(:[0-9]{1,2})?")) return null;
        String[] parts = value.split(":");
        String name = parts.length == 1 ? "minecraft:" + parts[0]
                : parts[0] + ":" + parts[1];
        Block block = Block.getBlockFromName(name);
        if (block == null) return null;
        int meta = parts.length == 3 ? Integer.parseInt(parts[2]) : 0;
        if (meta < 0 || meta > 15) return null;
        return block.getStateFromMeta(meta);
    }

    private boolean hasSelection() {
        return pos1 != null && pos2 != null;
    }

    private Bounds getBounds() {
        return new Bounds(Math.min(pos1.getX(), pos2.getX()), Math.min(pos1.getY(), pos2.getY()),
                Math.min(pos1.getZ(), pos2.getZ()), Math.max(pos1.getX(), pos2.getX()),
                Math.max(pos1.getY(), pos2.getY()), Math.max(pos1.getZ(), pos2.getZ()));
    }

    private String format(BlockPos pos) {
        return pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (blockField != null && blockField.isFocused()) {
            blockField.textboxKeyTyped(typedChar, keyCode);
            return;
        }
        if (replaceField != null && replaceField.isFocused()) {
            replaceField.textboxKeyTyped(typedChar, keyCode);
            return;
        }
        if (keyCode == org.lwjgl.input.Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(parent);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (blockField != null) blockField.mouseClicked(mouseX, mouseY, mouseButton);
        if (replaceField != null) replaceField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x22000000);
        int panelWidth = Math.min(500, width - 20);
        int left = (width - panelWidth) / 2;
        int top = Math.max(10, (height - 320) / 2);
        int right = left + panelWidth;
        int bottom = Math.min(height - 10, top + 320);

        drawRect(left, top, right, bottom, 0xE00A0C11);
        drawRect(left, top, right, top + 2, ACCENT);
        drawCenteredString(fontRendererObj, "WORLDEDIT", width / 2, top + 13, 0xFFF3F4F6);
        drawCenteredString(fontRendererObj, "SINGLEPLAYER EDITOR", width / 2, top + 29, ACCENT);

        if (!isSingleplayerAvailable()) {
            drawCenteredString(fontRendererObj, "Open a singleplayer world to use WorldEdit.",
                    width / 2, top + 90, 0xFFFFD866);
            super.drawScreen(mouseX, mouseY, partialTicks);
            return;
        }

        drawString(fontRendererObj, "BLOCK", left + 14, top + 64, 0xFFB8BDC8);
        drawString(fontRendererObj, "REPLACE FILTER", left + panelWidth / 2 + 3,
                top + 64, 0xFFB8BDC8);
        if (blockField != null) blockField.drawTextBox();
        if (replaceField != null) replaceField.drawTextBox();

        drawString(fontRendererObj, "POS 1: " + (pos1 == null ? "Not set" : format(pos1)),
                left + 14, top + 157, 0xFFE7E9EE);
        drawString(fontRendererObj, "POS 2: " + (pos2 == null ? "Not set" : format(pos2)),
                left + 14, top + 169, 0xFFE7E9EE);
        drawString(fontRendererObj, "CLIPBOARD: " + (clipboard == null ? "Empty" : "Stored"),
                left + 14, top + 183, 0xFFB8BDC8);
        drawString(fontRendererObj, fit(status, panelWidth - 28), left + 14,
                top + 218, ACCENT);
        drawString(fontRendererObj, "Direct local-world editing. Cheats are not required.",
                left + 14, top + 245, 0xFF9298A4);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private String fit(String value, int maxWidth) {
        return fontRendererObj.getStringWidth(value) <= maxWidth ? value
                : fontRendererObj.trimStringToWidth(value, maxWidth - 12) + "...";
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private interface WorldEditTask {
        void run(WorldServer world);
    }

    private static final class Bounds {
        final int minX, minY, minZ, maxX, maxY, maxZ;
        Bounds(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
            this.minX = minX; this.minY = minY; this.minZ = minZ;
            this.maxX = maxX; this.maxY = maxY; this.maxZ = maxZ;
        }
        int sizeX() { return maxX - minX + 1; }
        int sizeY() { return maxY - minY + 1; }
        int sizeZ() { return maxZ - minZ + 1; }
    }
}
