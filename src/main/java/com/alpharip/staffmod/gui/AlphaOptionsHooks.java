// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;

/** Adds the native-style Alpha Settings entry to Minecraft's standard options screen. */
public final class AlphaOptionsHooks {
    private static final int ALPHA_SETTINGS_BUTTON_ID = 1989;
    private static final int BUTTON_HEIGHT = 20;
    private static final int ROW_SPACING = 24;

    private AlphaOptionsHooks() { }

    @SuppressWarnings("unchecked")
    public static void addAlphaSettingsButton(Object screenObject) {
        if (!(screenObject instanceof GuiOptions)) return;
        GuiScreen screen = (GuiScreen) screenObject;
        try {
            Field buttonListField = findField(GuiScreen.class, "buttonList", "field_146292_n");
            buttonListField.setAccessible(true);
            List<GuiButton> buttons = (List<GuiButton>) buttonListField.get(screen);
            for (GuiButton button : buttons) {
                if (button.id == ALPHA_SETTINGS_BUTTON_ID) return;
            }

            int targetRow = -1;
            for (GuiButton button : buttons) {
                String label = button.displayString == null
                        ? "" : button.displayString.toLowerCase(Locale.ROOT);
                if (label.contains("fov") || label.contains("field of view")
                        || label.contains("difficulty") || label.contains("moeilijkheid")
                        || label.contains("gezichtsveld")) {
                    targetRow = Math.max(targetRow, button.yPosition);
                }
            }

            // Vanilla 1.8.9 places the FOV/Difficulty controls in the upper half.
            // If the labels are localized, use the known row rather than the old
            // Resource Packs/Snooper row near the bottom of the options screen.
            if (targetRow < 0) targetRow = screen.height / 6 + 36;
            int y = targetRow + ROW_SPACING;
            int x = screen.width / 2 - 100;

            // Make room for a dedicated full-width row without overlapping Resource Packs,
            // Snooper Settings, or other vanilla controls below FOV/Difficulty.
            for (GuiButton button : buttons) {
                if (button.id != 200 && button.yPosition >= y) {
                    button.yPosition += ROW_SPACING;
                }
            }

            if (y + BUTTON_HEIGHT > screen.height - 30) {
                y = Math.max(targetRow + ROW_SPACING, screen.height - BUTTON_HEIGHT - 30);
            }
            buttons.add(new GuiButton(ALPHA_SETTINGS_BUTTON_ID, x, y, 200, BUTTON_HEIGHT, "Alpha Settings"));
        } catch (Exception error) {
            System.err.println("[AlphaClient] Could not add Alpha Settings to Minecraft options: " + error.getMessage());
        }
    }

    public static boolean handleAlphaSettingsButton(Object screenObject, Object buttonObject) {
        if (!(screenObject instanceof GuiScreen) || !(buttonObject instanceof GuiButton)) return false;
        GuiButton button = (GuiButton) buttonObject;
        if (button.id != ALPHA_SETTINGS_BUTTON_ID) return false;
        Minecraft.getMinecraft().displayGuiScreen(new AlphaSettingsMenu((GuiScreen) screenObject));
        return true;
    }

    private static Field findField(Class<?> owner, String... names) throws NoSuchFieldException {
        for (String name : names) {
            try {
                return owner.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) { }
        }
        throw new NoSuchFieldException(owner.getName() + " fields " + java.util.Arrays.toString(names));
    }
}
