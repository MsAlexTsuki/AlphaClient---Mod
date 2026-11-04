package com.alpharip.staffmod.waypoints;

import com.alpharip.staffmod.gui.AlphaButton;
import com.alpharip.staffmod.gui.AlphaGuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.MathHelper;
import java.io.IOException;

/** Focused Lunar-style screen for creating a waypoint at custom coordinates. */
public final class CreateWaypointGui extends AlphaGuiScreen {
    private static final int GOLD = 0xFFFFD866;
    private static final int[] COLORS = {
            0xFFFF5555, 0xFFFFAA00, 0xFFFFFF55, 0xFF55FF55, 0xFF55FFFF,
            0xFF5555FF, 0xFFAA55FF, 0xFFFF55FF, 0xFFFFFFFF, 0xFFFFD866
    };
    private final GuiScreen parent;
    private GuiTextField nameField, xField, yField, zField;
    private int selectedColor = GOLD;
    private int left, top, panelWidth, panelHeight, paletteTop;
    private String validationError = "";

    public CreateWaypointGui(GuiScreen parent) { this.parent = parent; }

    @Override
    public void initGui() {
        buttonList.clear();
        panelWidth = Math.min(360, width - 24);
        panelHeight = Math.min(260, height - 24);
        left = (width - panelWidth) / 2;
        top = (height - panelHeight) / 2;

        nameField = new GuiTextField(20, fontRendererObj, left + 18, top + 62, panelWidth - 36, 20);
        nameField.setMaxStringLength(40);
        nameField.setFocused(true);

        int coordinateWidth = (panelWidth - 48) / 3;
        int coordinateY = top + 105;
        xField = new GuiTextField(21, fontRendererObj, left + 18, coordinateY, coordinateWidth, 20);
        yField = new GuiTextField(22, fontRendererObj, left + 24 + coordinateWidth, coordinateY, coordinateWidth, 20);
        zField = new GuiTextField(23, fontRendererObj, left + 30 + coordinateWidth * 2, coordinateY, coordinateWidth, 20);
        xField.setMaxStringLength(16); yField.setMaxStringLength(16); zField.setMaxStringLength(16);
        if (mc.thePlayer != null) {
            xField.setText(String.valueOf(MathHelper.floor_double(mc.thePlayer.posX)));
            yField.setText(String.valueOf(MathHelper.floor_double(mc.thePlayer.posY)));
            zField.setText(String.valueOf(MathHelper.floor_double(mc.thePlayer.posZ)));
        }

        paletteTop = top + 151;
        int buttonY = top + panelHeight - 32;
        int buttonWidth = (panelWidth - 44) / 2;
        buttonList.add(new AlphaButton(1, left + 18, buttonY, buttonWidth, 22, "Create Waypoint", true));
        buttonList.add(new AlphaButton(2, left + 26 + buttonWidth, buttonY, buttonWidth, 22, "Cancel"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == null) return;
        if (button.id == 1) createWaypoint();
        else if (button.id == 2) mc.displayGuiScreen(parent);
    }

    private void createWaypoint() {
        if (mc.thePlayer == null) return;
        String name = nameField.getText().trim();
        if (name.isEmpty()) name = "Waypoint " + (WaypointManager.getInstance().getForCurrentServer().size() + 1);
        final double x, y, z;
        try {
            x = Double.parseDouble(xField.getText().trim());
            y = Double.parseDouble(yField.getText().trim());
            z = Double.parseDouble(zField.getText().trim());
            if (Double.isNaN(x) || Double.isInfinite(x) || Double.isNaN(y) || Double.isInfinite(y)
                    || Double.isNaN(z) || Double.isInfinite(z)) throw new NumberFormatException();
        } catch (NumberFormatException invalidCoordinates) {
            validationError = "Enter valid X, Y and Z coordinates.";
            return;
        }

        WaypointManager manager = WaypointManager.getInstance();
        final StaffWaypoint point;
        try {
            point = manager.add(name, x, y, z);
        } catch (IllegalStateException worldNotReady) {
            validationError = "World data is not ready yet. Please try again.";
            return;
        }

        point.setColor(selectedColor);
        point.setIconId(WaypointSettings.getString("defaultIcon", "pin"));
        manager.select(point.getId());
        manager.saveChanges();
        mc.displayGuiScreen(parent);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) { mc.displayGuiScreen(parent); return; }
        if (keyCode == 28) { createWaypoint(); return; }
        if (nameField != null && nameField.isFocused()) nameField.textboxKeyTyped(typedChar, keyCode);
        else if (xField != null && xField.isFocused()) xField.textboxKeyTyped(typedChar, keyCode);
        else if (yField != null && yField.isFocused()) yField.textboxKeyTyped(typedChar, keyCode);
        else if (zField != null && zField.isFocused()) zField.textboxKeyTyped(typedChar, keyCode);
        else super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        nameField.mouseClicked(mouseX, mouseY, mouseButton);
        xField.mouseClicked(mouseX, mouseY, mouseButton);
        yField.mouseClicked(mouseX, mouseY, mouseButton);
        zField.mouseClicked(mouseX, mouseY, mouseButton);
        boolean inName = mouseX >= left + 18 && mouseX < left + panelWidth - 18
                && mouseY >= top + 62 && mouseY < top + 82;
        int coordinateWidth = (panelWidth - 48) / 3;
        int coordinateY = top + 105;
        boolean inCoordinates = mouseY >= coordinateY && mouseY < coordinateY + 20
                && mouseX >= left + 18 && mouseX < left + panelWidth - 18;
        nameField.setFocused(inName);
        xField.setFocused(inCoordinates && mouseX < left + 18 + coordinateWidth);
        yField.setFocused(inCoordinates && mouseX >= left + 24 + coordinateWidth
                && mouseX < left + 24 + coordinateWidth * 2);
        zField.setFocused(inCoordinates && mouseX >= left + 30 + coordinateWidth * 2);
        validationError = "";
        for (int i = 0; i < COLORS.length; i++) {
            int x = left + 20 + i * 29;
            if (mouseX >= x && mouseX < x + 19 && mouseY >= paletteTop && mouseY < paletteTop + 19) {
                selectedColor = COLORS[i]; return;
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
        drawString(fontRendererObj, "Choose a name and destination coordinates", left + 18, top + 34, 0xFFB8BDC8);
        drawString(fontRendererObj, "Waypoint name", left + 18, top + 51, 0xFFE6E8ED);
        nameField.drawTextBox();
        int coordinateWidth = (panelWidth - 48) / 3;
        drawString(fontRendererObj, "X", left + 18, top + 92, 0xFFE6E8ED);
        drawString(fontRendererObj, "Y", left + 24 + coordinateWidth, top + 92, 0xFFE6E8ED);
        drawString(fontRendererObj, "Z", left + 30 + coordinateWidth * 2, top + 92, 0xFFE6E8ED);
        xField.drawTextBox(); yField.drawTextBox(); zField.drawTextBox();
        drawString(fontRendererObj, "Marker color", left + 18, top + 132, 0xFFE6E8ED);
        for (int i = 0; i < COLORS.length; i++) {
            int x = left + 20 + i * 29;
            if (COLORS[i] == selectedColor) drawRect(x - 2, paletteTop - 2, x + 21, paletteTop + 21, 0xFFFFFFFF);
            drawRect(x, paletteTop, x + 17, paletteTop + 17, COLORS[i]);
        }
        if (!validationError.isEmpty())
            drawString(fontRendererObj, validationError, left + 18, top + panelHeight - 48, 0xFFFF6666);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
