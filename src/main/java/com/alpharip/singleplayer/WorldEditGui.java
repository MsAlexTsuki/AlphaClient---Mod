// --- Made By MsAlexTsuki ---
package com.alpharip.singleplayer;

import com.alpharip.staffmod.gui.AlphaButton;
import com.alpharip.staffmod.gui.AlphaGuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.BlockPos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleplayer-only WorldEdit control screen.
 *
 * Editing is sent to the integrated server through vanilla /fill and /clone
 * commands. No command is ever sent while connected to a multiplayer server.
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
    private final GuiScreen parent;

    private GuiTextField blockField;
    private GuiTextField replaceField;
    private BlockPos pos1;
    private BlockPos pos2;
    private BlockPos clipboardMin;
    private BlockPos clipboardMax;
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
        return mc != null && mc.isSingleplayer() && mc.thePlayer != null && mc.theWorld != null;
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

        if (!hasSelection()) {
            status = "Set both positions before editing.";
            return;
        }

        String block = normalizeBlock(blockField == null ? "" : blockField.getText());
        String replacement = normalizeBlock(replaceField == null ? "" : replaceField.getText());
        if (button.id == SET) {
            if (block == null) { status = "Enter a valid block ID."; return; }
            send(fillCommand(region(), block));
            status = "Set region to " + block + ".";
        } else if (button.id == REPLACE) {
            if (block == null || replacement == null) { status = "Enter valid block IDs."; return; }
            send(fillCommand(region(), block) + " replace " + replacement);
            status = "Replaced " + replacement + " with " + block + ".";
        } else if (button.id == WALLS || button.id == OUTLINE || button.id == HOLLOW) {
            if (block == null) { status = "Enter a valid block ID."; return; }
            if (button.id == WALLS) runWalls(block); else runShell(block, button.id == HOLLOW);
            status = button.id == WALLS ? "Built the four side walls."
                    : (button.id == OUTLINE ? "Built the region outline." : "Built a hollow region.");
        } else if (button.id == COPY) {
            clipboardMin = min(pos1, pos2);
            clipboardMax = max(pos1, pos2);
            status = "Region copied. Use PASTE AT PLAYER to place it.";
        } else if (button.id == PASTE) {
            if (clipboardMin == null || clipboardMax == null) {
                status = "Copy a region before pasting.";
                return;
            }
            BlockPos destination = mc.thePlayer.getPosition();
            send("/clone " + coords(clipboardMin) + " " + coords(clipboardMax) + " "
                    + destination.getX() + " " + destination.getY() + " " + destination.getZ());
            status = "Pasting copied region at your position.";
        }
    }

    private boolean hasSelection() {
        return pos1 != null && pos2 != null;
    }

    private String[] region() {
        BlockPos low = min(pos1, pos2);
        BlockPos high = max(pos1, pos2);
        return new String[] { coords(low), coords(high) };
    }

    private String fillCommand(String[] bounds, String block) {
        return "/fill " + bounds[0] + " " + bounds[1] + " " + block;
    }

    private void runWalls(String block) {
        BlockPos low = min(pos1, pos2);
        BlockPos high = max(pos1, pos2);
        send("/fill " + low.getX() + " " + low.getY() + " " + low.getZ()
                + " " + low.getX() + " " + high.getY() + " " + high.getZ() + " " + block);
        send("/fill " + high.getX() + " " + low.getY() + " " + low.getZ()
                + " " + high.getX() + " " + high.getY() + " " + high.getZ() + " " + block);
        send("/fill " + low.getX() + " " + low.getY() + " " + low.getZ()
                + " " + high.getX() + " " + high.getY() + " " + low.getZ() + " " + block);
        send("/fill " + low.getX() + " " + low.getY() + " " + high.getZ()
                + " " + high.getX() + " " + high.getY() + " " + high.getZ() + " " + block);
    }

    private void runShell(String block, boolean hollow) {
        BlockPos low = min(pos1, pos2);
        BlockPos high = max(pos1, pos2);
        List<String> commands = new ArrayList<String>();
        commands.add("/fill " + low.getX() + " " + low.getY() + " " + low.getZ()
                + " " + high.getX() + " " + low.getY() + " " + high.getZ() + " " + block);
        commands.add("/fill " + low.getX() + " " + high.getY() + " " + low.getZ()
                + " " + high.getX() + " " + high.getY() + " " + high.getZ() + " " + block);
        commands.add("/fill " + low.getX() + " " + low.getY() + " " + low.getZ()
                + " " + low.getX() + " " + high.getY() + " " + high.getZ() + " " + block);
        commands.add("/fill " + high.getX() + " " + low.getY() + " " + low.getZ()
                + " " + high.getX() + " " + high.getY() + " " + high.getZ() + " " + block);
        commands.add("/fill " + low.getX() + " " + low.getY() + " " + low.getZ()
                + " " + high.getX() + " " + high.getY() + " " + low.getZ() + " " + block);
        commands.add("/fill " + low.getX() + " " + low.getY() + " " + high.getZ()
                + " " + high.getX() + " " + high.getY() + " " + high.getZ() + " " + block);

        if (hollow) {
            int innerMinX = low.getX() + 1, innerMinY = low.getY() + 1, innerMinZ = low.getZ() + 1;
            int innerMaxX = high.getX() - 1, innerMaxY = high.getY() - 1, innerMaxZ = high.getZ() - 1;
            if (innerMinX <= innerMaxX && innerMinY <= innerMaxY && innerMinZ <= innerMaxZ) {
                commands.add("/fill " + innerMinX + " " + innerMinY + " " + innerMinZ
                        + " " + innerMaxX + " " + innerMaxY + " " + innerMaxZ + " minecraft:air");
            }
        }
        for (String command : commands) send(command);
    }

    private void send(String command) {
        if (isSingleplayerAvailable()) mc.thePlayer.sendChatMessage(command);
    }

    private String normalizeBlock(String value) {
        if (value == null) return null;
        String block = value.trim().toLowerCase(java.util.Locale.ROOT);
        if (block.isEmpty() || !block.matches("[a-z0-9_]+(:[a-z0-9_]+)?")) return null;
        return block.indexOf(':') >= 0 ? block : "minecraft:" + block;
    }

    private BlockPos min(BlockPos a, BlockPos b) {
        return new BlockPos(Math.min(a.getX(), b.getX()), Math.min(a.getY(), b.getY()),
                Math.min(a.getZ(), b.getZ()));
    }

    private BlockPos max(BlockPos a, BlockPos b) {
        return new BlockPos(Math.max(a.getX(), b.getX()), Math.max(a.getY(), b.getY()),
                Math.max(a.getZ(), b.getZ()));
    }

    private String coords(BlockPos pos) {
        return pos.getX() + " " + pos.getY() + " " + pos.getZ();
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
        drawString(fontRendererObj, "COPY: " + (clipboardMin == null ? "Empty" : "Region stored"),
                left + 14, top + 183, 0xFFB8BDC8);
        drawString(fontRendererObj, fit(status, panelWidth - 28), left + 14,
                top + 218, ACCENT);
        drawString(fontRendererObj, "Requires singleplayer cheats (vanilla /fill and /clone).",
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
}
