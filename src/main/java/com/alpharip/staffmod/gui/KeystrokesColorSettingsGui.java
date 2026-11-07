// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.keystrokes.KeystrokesMod;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

/** Hex color controls for the Keystrokes HUD. Values use AARRGGBB format. */
public final class KeystrokesColorSettingsGui extends AlphaGuiScreen {
    private static final int SAVE = 1;
    private static final int BACK = 2;
    private final GuiScreen parent;
    private GuiTextField[] fields = new GuiTextField[5];
    private final String[] labels = {
            "BACKGROUND", "PRESSED BACKGROUND", "TEXT", "PRESSED TEXT", "BORDER"
    };
    private final String[] keys = {
            "background", "pressedBackground", "text", "pressedText", "border"
    };
    private int left;
    private int top;

    public KeystrokesColorSettingsGui(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        buttonList.clear();
        left = width / 2 - 145;
        top = Math.max(8, height / 2 - 135);
        int[] colors = {
                KeystrokesMod.getBackgroundColor(),
                KeystrokesMod.getPressedBackgroundColor(),
                KeystrokesMod.getTextColor(),
                KeystrokesMod.getPressedTextColor(),
                KeystrokesMod.getBorderColor()
        };
        for (int i = 0; i < fields.length; i++) {
            fields[i] = new GuiTextField(i, fontRendererObj, left + 150, top + 49 + i * 31, 128, 19);
            fields[i].setMaxStringLength(8);
            fields[i].setText(String.format("%08X", colors[i]));
        }
        buttonList.add(new AlphaButton(SAVE, left + 12, top + 225, 125, 20, "SAVE COLORS"));
        buttonList.add(new AlphaButton(BACK, left + 153, top + 225, 125, 20, "BACK"));
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(parent);
            return;
        }
        for (GuiTextField field : fields) {
            if (field != null && field.isFocused()) {
                field.textboxKeyTyped(typedChar, keyCode);
                return;
            }
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        for (GuiTextField field : fields) {
            if (field != null) field.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x30000000);
        drawRect(left, top, left + 290, top + 260, 0xE00A0C11);
        drawRect(left, top, left + 290, top + 2, 0xFFFFC83D);
        drawCenteredString(fontRendererObj, "KEYSTROKES COLORS", width / 2, top + 12, 0xFFF3F4F6);
        drawCenteredString(fontRendererObj, "Enter 8-digit AARRGGBB hex colors", width / 2, top + 29, 0xFFB8BDC8);
        for (int i = 0; i < fields.length; i++) {
            fontRendererObj.drawString(labels[i], left + 12, top + 54 + i * 31, 0xFFF3F4F6);
            fields[i].drawTextBox();
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button.id == BACK) {
            mc.displayGuiScreen(parent);
            return;
        }
        if (button.id != SAVE) return;
        try {
            int[] parsed = new int[fields.length];
            for (int i = 0; i < fields.length; i++) {
                String value = fields[i].getText().trim();
                if (value.startsWith("#")) value = value.substring(1);
                if (value.length() != 8) return;
                parsed[i] = (int) Long.parseLong(value, 16);
            }
            for (int i = 0; i < keys.length; i++) KeystrokesMod.setColor(keys[i], parsed[i]);
            mc.displayGuiScreen(parent);
        } catch (NumberFormatException ignored) {
            // Keep the editor open so invalid hex input can be corrected.
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
