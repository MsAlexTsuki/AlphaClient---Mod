package com.alpharip.staffmod.Viewer.ItemNbtViewer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Standalone expandable NBT tree with clipboard actions. */
public final class ItemNbtViewerGui extends GuiScreen {
    private static final int BACK = 1, COPY_SELECTED = 2, COPY_ALL = 3, REFRESH = 4;
    private static final int ROW_HEIGHT = 13;
    private final GuiScreen parent;
    private final ItemNbtViewer viewer;
    private final List<NbtTreeNode> visibleNodes = new ArrayList<NbtTreeNode>();
    private NbtTreeNode selectedNode;
    private int panelLeft, panelTop, panelRight, panelBottom, scrollOffset;

    public ItemNbtViewerGui(GuiScreen parent, ItemNbtViewer viewer) {
        this.parent = parent;
        this.viewer = viewer;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int panelWidth = Math.min(620, Math.max(340, width - 20));
        int panelHeight = Math.min(420, Math.max(240, height - 20));
        panelLeft = (width - panelWidth) / 2;
        panelRight = panelLeft + panelWidth;
        panelTop = (height - panelHeight) / 2;
        panelBottom = panelTop + panelHeight;
        buttonList.add(new ViewerButton(BACK, panelLeft + 12, panelBottom - 29, 68, 20, "BACK"));
        buttonList.add(new ViewerButton(COPY_SELECTED, panelLeft + 88, panelBottom - 29, 112, 20, "COPY SELECTED"));
        buttonList.add(new ViewerButton(COPY_ALL, panelLeft + 208, panelBottom - 29, 88, 20, "COPY ALL"));
        buttonList.add(new ViewerButton(REFRESH, panelRight - 88, panelBottom - 29, 76, 20, "REFRESH"));
        refreshTree();
    }

    private void refreshTree() {
        viewer.captureHeldItem();
        scrollOffset = 0;
        selectedNode = null;
        rebuildVisibleNodes();
    }

    @Override
    public void updateScreen() {
        viewer.captureHeldItem();
        rebuildVisibleNodes();
    }

    private void rebuildVisibleNodes() {
        visibleNodes.clear();
        NbtTreeNode root = viewer.getRoot();
        if (root != null) appendVisible(root);
        if (selectedNode != null && !visibleNodes.contains(selectedNode)) selectedNode = null;
        GuiButton copySelected = findButton(COPY_SELECTED);
        if (copySelected != null) copySelected.enabled = selectedNode != null;
        GuiButton copyAll = findButton(COPY_ALL);
        if (copyAll != null) copyAll.enabled = !viewer.getSerializedNbt().isEmpty();
        clampScroll();
    }

    private GuiButton findButton(int id) {
        for (Object entry : buttonList) {
            if (entry instanceof GuiButton && ((GuiButton) entry).id == id) return (GuiButton) entry;
        }
        return null;
    }

    private void appendVisible(NbtTreeNode node) {
        visibleNodes.add(node);
        if (node.isExpanded()) for (NbtTreeNode child : node.getChildren()) appendVisible(child);
    }
    private int listTop() { return panelTop + 75; }
    private int listBottom() { return panelBottom - 43; }
    private int visibleRowCount() { return Math.max(1, (listBottom() - listTop()) / ROW_HEIGHT); }
    private void clampScroll() {
        scrollOffset = Math.max(0, Math.min(scrollOffset,
                Math.max(0, visibleNodes.size() - visibleRowCount())));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawRect(panelLeft, panelTop, panelRight, panelBottom, 0xF00A0C11);
        drawRect(panelLeft, panelTop, panelRight, panelTop + 2, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "ITEM NBT VIEWER", width / 2, panelTop + 12, 0xFFF3F4F6);
        drawCenteredString(fontRendererObj, viewer.getItemDisplayName(), width / 2, panelTop + 28, 0xFFFFD866);
        drawString(fontRendererObj, "NAME / PATH", panelLeft + 16, panelTop + 49, 0xFF9AA3B2);
        drawString(fontRendererObj, "TYPE", panelRight - 164, panelTop + 49, 0xFF9AA3B2);
        drawString(fontRendererObj, "VALUE", panelRight - 112, panelTop + 49, 0xFF9AA3B2);
        drawRect(panelLeft + 12, panelTop + 62, panelRight - 12, panelTop + 63, 0x664B515D);

        if (viewer.getRoot() == null) {
            drawCenteredString(fontRendererObj, viewer.getItemDisplayName(), width / 2, panelTop + 100, 0xFFB8BDC8);
        } else {
            int rows = visibleRowCount();
            int end = Math.min(visibleNodes.size(), scrollOffset + rows);
            for (int i = scrollOffset; i < end; i++) {
                NbtTreeNode node = visibleNodes.get(i);
                int y = listTop() + (i - scrollOffset) * ROW_HEIGHT;
                if (node == selectedNode) drawRect(panelLeft + 12, y - 2, panelRight - 12, y + ROW_HEIGHT - 1, 0x88403A25);
                int depth = depthOf(node);
                int nameX = panelLeft + 17 + depth * 11;
                String marker = node.hasChildren() ? (node.isExpanded() ? "v " : "> ") : "  ";
                int nameWidth = Math.max(30, panelRight - nameX - 174);
                drawString(fontRendererObj, fontRendererObj.trimStringToWidth(marker + node.getName(), nameWidth),
                        nameX, y, node.hasChildren() ? 0xFFFFD866 : 0xFFE5E7EB);
                drawString(fontRendererObj, fontRendererObj.trimStringToWidth(node.getType(), 48),
                        panelRight - 164, y, 0xFF9AA3B2);
                drawString(fontRendererObj, fontRendererObj.trimStringToWidth(node.getValue(), 96),
                        panelRight - 112, y, 0xFFD5D9E0);
            }
            if (visibleNodes.size() > rows) {
                drawString(fontRendererObj, (scrollOffset + 1) + "-" + end + " / " + visibleNodes.size(),
                        panelRight - 94, panelBottom - 43, 0xFF858D9B);
            }
        }
        drawString(fontRendererObj, "Click a row to select it; click a branch to expand or collapse.",
                panelLeft + 14, panelBottom - 42, 0xFF858D9B);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private int depthOf(NbtTreeNode target) { return depthOf(viewer.getRoot(), target, 0); }
    private int depthOf(NbtTreeNode current, NbtTreeNode target, int depth) {
        if (current == null) return 0;
        if (current == target) return depth;
        if (current.isExpanded()) {
            for (NbtTreeNode child : current.getChildren()) {
                int found = depthOf(child, target, depth + 1);
                if (found > 0 || child == target) return found;
            }
        }
        return 0;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0 && mouseY >= listTop() && mouseY < listBottom()) {
            int index = scrollOffset + (mouseY - listTop()) / ROW_HEIGHT;
            if (index >= 0 && index < visibleNodes.size()) {
                selectedNode = visibleNodes.get(index);
                if (selectedNode.hasChildren()) selectedNode.toggleExpanded();
                rebuildVisibleNodes();
                return;
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
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

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button.id == BACK) mc.displayGuiScreen(parent);
        else if (button.id == COPY_SELECTED && selectedNode != null) GuiScreen.setClipboardString(selectedNode.getRawValue());
        else if (button.id == COPY_ALL) GuiScreen.setClipboardString(viewer.getSerializedNbt());
        else if (button.id == REFRESH) refreshTree();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) { mc.displayGuiScreen(parent); return; }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }

    private static final class ViewerButton extends GuiButton {
        ViewerButton(int id, int x, int y, int width, int height, String label) {
            super(id, x, y, width, height, label);
        }
        @Override
        public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
            if (!visible) return;
            hovered = mouseX >= xPosition && mouseY >= yPosition
                    && mouseX < xPosition + width && mouseY < yPosition + height;
            int background = !enabled ? 0xFF111318 : hovered ? 0xFF292D36 : 0xFF171A21;
            int accent = hovered && enabled ? 0xFFFFD866 : 0x664B515D;
            drawRect(xPosition, yPosition, xPosition + width, yPosition + height, background);
            drawRect(xPosition, yPosition, xPosition + width, yPosition + 1, accent);
            drawCenteredString(minecraft.fontRendererObj, displayString, xPosition + width / 2,
                    yPosition + (height - 8) / 2,
                    !enabled ? 0xFF666B75 : hovered ? 0xFFFFD866 : 0xFFE6E8ED);
        }
    }
}
