// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;

import java.lang.reflect.Field;
import java.util.List;

/** Adds the native-style Alpha Settings entry to Minecraft's standard options screen. */
public final class AlphaOptionsHooks {
    private static final int ALPHA_SETTINGS_BUTTON_ID = 1989;
    private static final String OPTIONS_CLASS = "net.minecraft.client.gui.GuiOptions";

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

            int targetBottom = screen.height / 6 + 126;
            for (GuiButton button : buttons) {
                String label = button.displayString == null ? "" : button.displayString.toLowerCase();
                if (label.contains("fov") || label.contains("field of view")
                        || label.contains("difficulty") || label.contains("moeilijkheid")) {
                    targetBottom = Math.max(targetBottom, button.yPosition + button.height);
                }
            }

            int x = screen.width / 2 - 100;
            int y = targetBottom + 3;
            int doneY = screen.height / 6 + 168;
            for (GuiButton button : buttons) {
                if (button.id == 200 && y + 20 > button.yPosition) {
                    button.yPosition = Math.min(screen.height - 25, y + 24);
                }
            }
            if (y + 20 > screen.height - 24) y = screen.height - 46;
            buttons.add(new GuiButton(ALPHA_SETTINGS_BUTTON_ID, x, y, 200, 20, "Alpha Settings"));
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
