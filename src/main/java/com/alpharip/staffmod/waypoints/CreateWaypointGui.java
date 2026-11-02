package com.alpharip.staffmod.waypoints;

import com.alpharip.staffmod.gui.AlphaButton;
import com.alpharip.staffmod.gui.AlphaGuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.MathHelper;
import java.io.IOException;

/** Focused Lunar-style screen for creating one waypoint. */
public final class CreateWaypointGui extends AlphaGuiScreen {
    private static final int GOLD = 0xFFFFD866;
    private static final int[] COLORS = {
            0xFFFF5555, 0xFFFFAA00, 0xFFFFFF55, 0xFF55FF55, 0xFF55FFFF,
            0xFF5555FF, 0xFFAA55FF, 0xFFFF55FF, 0xFFFFFFFF, 0xFFFFD866
    };

    private final GuiScreen parent;
    private GuiTextField nameField;
    private int selectedColor = GOLD;
    private int left, top, panelWidth, panelHeight, paletteTop;

    public CreateWaypointGui(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        panelWidth = Math.min(340, width - 24);
        panelHeight = Math.min(218, height - 24);
        left = (width - panelWidth) / 2;
        top = (height - panelHeight) / 2;

        nameField = new GuiTextField(20, fontRendererObj, left + 18, top + 62, panelWidth - 36, 20);
        nameField.setMaxStringLength(40);
        nameField.setFocused(true);

        paletteTop = top + 112;
        int buttonY = top + panelHeight - 34;
        buttonList.add(new AlphaButton(1, left + 18, buttonY, (panelWidth - 44) / 2, 22,
                "Create Waypoint", true));
        buttonList.add(new AlphaButton(2, left + 26 + (panelWidth - 44) / 2, buttonY,
                (panelWidth - 44) / 2, 22, "Cancel"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == null) return;
        if (button.id == 1) {
            createWaypoint();
        } else if (button.id == 2) {
            mc.displayGuiScreen(parent);
        }
    }

    private void createWaypoint() {
        if (mc.thePlayer == null) return;
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            name = "Waypoint " + (WaypointManager.getInstance().getForCurrentServer().size() + 1);
        }

        WaypointManager manager = WaypointManager.getInstance();
        StaffWaypoint point = manager.add(name, mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
        point.setColor(selectedColor);
        point.setIconId(WaypointSettings.getString("defaultIcon", "pin"));
        manager.select(point.getId());
        manager.saveChanges();
        mc.displayGuiScreen(parent);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            mc.displayGuiScreen(parent);
            return;
        }
        if (nameField != null && nameField.isFocused()) {
            if (keyCode == 28) {
                createWaypoint();
                return;
            }
            nameField.textboxKeyTyped(typedChar, keyCode);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (nameField != null) nameField.mouseClicked(mouseX, mouseY, mouseButton);

        int paletteX = left + 20;
        for (int i = 0; i < COLORS.length; i++) {
            int x = paletteX + i * 29;
            if (mouseX >= x && mouseX < x + 19 && mouseY >= paletteTop && mouseY < paletteTop + 19) {
                selectedColor = COLORS[i];
                return;
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x88000000);
        drawRect(left, top, left + panelWidth, top + panelHeight, 0xF00A0C11);
        drawRect(left, top, left + panelWidth, top + 2, GOLD);
        drawRect(left + 1, top + 1, left + 2, top + panelHeight - 1, 0x664B515D);

        drawString(fontRendererObj, "CREATE WAYPOINT", left + 18, top + 16, 0xFFF0F1F4);
        drawString(fontRendererObj, "Save your current location", left + 18, top + 34, 0xFFB8BDC8);
        drawString(fontRendererObj, "Waypoint name", left + 18, top + 51, 0xFFE6E8ED);
        nameField.drawTextBox();

        drawString(fontRendererObj, "Marker color", left + 18, top + 91, 0xFFE6E8ED);
        for (int i = 0; i < COLORS.length; i++) {
            int x = left + 20 + i * 29;
            if (COLORS[i] == selectedColor) {
                drawRect(x - 2, paletteTop - 2, x + 21, paletteTop + 21, 0xFFFFFFFF);
            }
            drawRect(x, paletteTop, x + 17, paletteTop + 17, COLORS[i]);
        }

        if (mc.thePlayer != null) {
            String coordinates = "X " + MathHelper.floor_double(mc.thePlayer.posX)
                    + "   Y " + MathHelper.floor_double(mc.thePlayer.posY)
                    + "   Z " + MathHelper.floor_double(mc.thePlayer.posZ);
            drawString(fontRendererObj, coordinates, left + 18, top + 143, 0xFFB8BDC8);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
