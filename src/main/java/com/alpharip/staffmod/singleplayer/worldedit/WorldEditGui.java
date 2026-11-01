// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.singleplayer.worldedit;

import com.alpharip.staffmod.gui.AlphaButton;
import com.alpharip.staffmod.gui.AlphaGuiScreen;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.WorldServer;

import java.io.IOException;

/** Singleplayer-only WorldEdit interface. World changes are handled by WorldEditOperations. */
public final class WorldEditGui extends AlphaGuiScreen {
    private static final int BACK = 1, POS1 = 2, POS2 = 3, SET = 4, REPLACE = 5;
    private static final int WALLS = 6, OUTLINE = 7, HOLLOW = 8, COPY = 9, PASTE = 10;
    private static final int ACCENT = 0xFFFFD866;
    private static final int MAX_BLOCKS_PER_OPERATION = 32768;

    private final GuiScreen parent;
    private final WorldEditSelection selection = new WorldEditSelection();
    private final WorldEditClipboard clipboard = new WorldEditClipboard();
    private GuiTextField blockField;
    private GuiTextField replaceField;
    private String status = "Select the two corners of your region.";

    public WorldEditGui(GuiScreen parent) { this.parent = parent; }

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

        replaceField = new GuiTextField(21, fontRendererObj, left + 14 + half + gap, top + 76, half, 18);
        replaceField.setMaxStringLength(64);
        replaceField.setText("minecraft:dirt");
        replaceField.setTextColor(0xFFE7E9EE);
        replaceField.setDisabledTextColour(0xFF777D88);
        replaceField.setEnableBackgroundDrawing(true);

        buttonList.add(new AlphaButton(POS1, left + 14, top + 101, half, 20, "SET POSITION 1"));
        buttonList.add(new AlphaButton(POS2, left + 14 + half + gap, top + 101, half, 20, "SET POSITION 2"));
        int third = (innerWidth - gap * 2) / 3;
        buttonList.add(new AlphaButton(SET, left + 14, row, third, 22, "SET"));
        buttonList.add(new AlphaButton(REPLACE, left + 14 + third + gap, row, third, 22, "REPLACE"));
        buttonList.add(new AlphaButton(WALLS, left + 14 + (third + gap) * 2, row, third, 22, "WALLS"));
        row += 29;
        buttonList.add(new AlphaButton(OUTLINE, left + 14, row, third, 22, "OUTLINE"));
        buttonList.add(new AlphaButton(HOLLOW, left + 14 + third + gap, row, third, 22, "HOLLOW"));
        buttonList.add(new AlphaButton(COPY, left + 14 + (third + gap) * 2, row, third, 22, "COPY"));
        row += 29;
        buttonList.add(new AlphaButton(PASTE, left + 14, row, innerWidth, 22, "PASTE AT PLAYER"));
        buttonList.add(new AlphaButton(BACK, left + 14, top + 285, 90, 20, "BACK"));
    }

    private boolean isSingleplayerAvailable() {
        return mc != null && mc.isSingleplayer() && mc.thePlayer != null
                && mc.theWorld != null && mc.getIntegratedServer() != null;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == null) return;
        if (button.id == BACK) { mc.displayGuiScreen(parent); return; }
        if (!isSingleplayerAvailable()) {
            status = "WorldEdit is available only in a loaded singleplayer world.";
            return;
        }
        if (button.id == POS1 || button.id == POS2) {
            BlockPos selected = mc.thePlayer.getPosition();
            if (button.id == POS1) selection.setPos1(selected);
            else selection.setPos2(selected);
            status = (button.id == POS1 ? "Position 1: " : "Position 2: ") + format(selected);
            return;
        }
        if (button.id == PASTE) { pasteAtPlayer(); return; }
        if (!selection.hasSelection()) { status = "Set both positions before editing."; return; }

        final WorldEditSelection.Bounds bounds = selection.getBounds();
        if (!isValidVolume(bounds)) return;
        if (button.id == COPY) { copySelection(bounds); return; }

        final IBlockState selectedBlock = WorldEditBlockParser.parse(
                blockField == null ? "" : blockField.getText());
        if (selectedBlock == null) {
            status = "Enter a valid block ID (optional metadata: minecraft:stone:0).";
            return;
        }
        final IBlockState replacement = button.id == REPLACE
                ? WorldEditBlockParser.parse(replaceField == null ? "" : replaceField.getText()) : null;
        if (button.id == REPLACE && replacement == null) {
            status = "Enter a valid replacement block ID.";
            return;
        }
        if (button.id == SET || button.id == REPLACE) applyFill(bounds, selectedBlock, replacement);
        else if (button.id == WALLS || button.id == OUTLINE || button.id == HOLLOW)
            applyShape(bounds, selectedBlock, button.id);
    }

    private void applyFill(final WorldEditSelection.Bounds bounds,
                           final IBlockState state, final IBlockState filter) {
        runOnIntegratedServer(new WorldEditTask() {
            @Override public void run(WorldServer world) {
                if (!WorldEditOperations.regionLoaded(world, bounds)) {
                    status = "Selection contains unloaded chunks.";
                    return;
                }
                WorldEditOperations.fill(world, bounds, state, filter);
                status = filter == null ? "Region filled without using cheats." : "Matching blocks replaced.";
            }
        });
    }

    private void applyShape(final WorldEditSelection.Bounds bounds,
                            final IBlockState state, final int operation) {
        runOnIntegratedServer(new WorldEditTask() {
            @Override public void run(WorldServer world) {
                if (!WorldEditOperations.regionLoaded(world, bounds)) {
                    status = "Selection contains unloaded chunks.";
                    return;
                }
                WorldEditOperations.shape(world, bounds, state, operation, WALLS, OUTLINE, HOLLOW);
                status = operation == WALLS ? "Side walls built."
                        : operation == OUTLINE ? "Region outline built." : "Hollow region built.";
            }
        });
    }

    private void copySelection(final WorldEditSelection.Bounds bounds) {
        runOnIntegratedServer(new WorldEditTask() {
            @Override public void run(WorldServer world) {
                if (!WorldEditOperations.regionLoaded(world, bounds)) {
                    status = "Selection contains unloaded chunks.";
                    return;
                }
                clipboard.setBlocks(WorldEditOperations.copy(world, bounds));
                status = "Region copied to the WorldEdit clipboard.";
            }
        });
    }

    private void pasteAtPlayer() {
        if (!clipboard.hasBlocks()) { status = "Copy a region before pasting."; return; }
        final BlockPos destination = mc.thePlayer.getPosition();
        final IBlockState[][][] copied = clipboard.getBlocks();
        final WorldEditSelection.Bounds target = new WorldEditSelection.Bounds(
                destination.getX(), destination.getY(), destination.getZ(),
                destination.getX() + clipboard.getSizeX() - 1,
                destination.getY() + clipboard.getSizeY() - 1,
                destination.getZ() + clipboard.getSizeZ() - 1);
        if (!isValidVolume(target)) return;

        runOnIntegratedServer(new WorldEditTask() {
            @Override public void run(WorldServer world) {
                if (!WorldEditOperations.regionLoaded(world, target)) {
                    status = "Paste area contains unloaded chunks.";
                    return;
                }
                WorldEditOperations.paste(world, destination, copied);
                status = "Clipboard pasted at your position.";
            }
        });
    }

    private void runOnIntegratedServer(final WorldEditTask task) {
        final IntegratedServer server = mc.getIntegratedServer();
        if (server == null) { status = "Integrated server is unavailable."; return; }
        status = "Applying WorldEdit operation...";
        server.addScheduledTask(new Runnable() {
            @Override public void run() {
                WorldServer world = server.worldServerForDimension(mc.theWorld.provider.getDimensionId());
                if (world == null) { status = "Singleplayer world is unavailable."; return; }
                task.run(world);
            }
        });
    }

    private boolean isValidVolume(WorldEditSelection.Bounds bounds) {
        if (bounds.minY < 0 || bounds.maxY > 255) {
            status = "Selection must stay between Y=0 and Y=255.";
            return false;
        }
        if (bounds.volume() > MAX_BLOCKS_PER_OPERATION) {
            status = "Selection is too large. Maximum: " + MAX_BLOCKS_PER_OPERATION + " blocks.";
            return false;
        }
        return true;
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
        drawString(fontRendererObj, "REPLACE FILTER", left + panelWidth / 2 + 3, top + 64, 0xFFB8BDC8);
        if (blockField != null) blockField.drawTextBox();
        if (replaceField != null) replaceField.drawTextBox();
        drawString(fontRendererObj, "POS 1: " + (selection.getPos1() == null
                ? "Not set" : format(selection.getPos1())), left + 14, top + 157, 0xFFE7E9EE);
        drawString(fontRendererObj, "POS 2: " + (selection.getPos2() == null
                ? "Not set" : format(selection.getPos2())), left + 14, top + 169, 0xFFE7E9EE);
        drawString(fontRendererObj, "CLIPBOARD: " + (clipboard.hasBlocks() ? "Stored" : "Empty"),
                left + 14, top + 183, 0xFFB8BDC8);
        drawString(fontRendererObj, fit(status, panelWidth - 28), left + 14, top + 218, ACCENT);
        drawString(fontRendererObj, "Direct local-world editing. Cheats are not required.",
                left + 14, top + 245, 0xFF9298A4);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private String fit(String value, int maxWidth) {
        return fontRendererObj.getStringWidth(value) <= maxWidth ? value
                : fontRendererObj.trimStringToWidth(value, maxWidth - 12) + "...";
    }

    @Override public boolean doesGuiPauseGame() { return false; }

    private interface WorldEditTask { void run(WorldServer world); }
}