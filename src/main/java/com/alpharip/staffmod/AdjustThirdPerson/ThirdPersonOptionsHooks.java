package com.alpharip.staffmod.AdjustThirdPerson;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;

/**
 * Prototype hook for placing a Third Person entry on Minecraft's Options screen.
 * A transformer or equivalent Options-screen integration must call these methods.
 */
public final class ThirdPersonOptionsHooks {
    public static final int THIRD_PERSON_BUTTON_ID = 19890;

    private ThirdPersonOptionsHooks() { }

    @SuppressWarnings("unchecked")
    public static void addThirdPersonButton(Object screenObject) {
        if (!(screenObject instanceof GuiOptions)) return;
        GuiScreen screen = (GuiScreen) screenObject;

        try {
            Field field = findButtonListField();
            field.setAccessible(true);
            List<GuiButton> buttons = (List<GuiButton>) field.get(screen);

            for (GuiButton button : buttons) {
                if (button.id == THIRD_PERSON_BUTTON_ID) return;
            }

            int targetRow = -1;
            for (GuiButton button : buttons) {
                String label = button.displayString == null
                        ? "" : button.displayString.toLowerCase(Locale.ROOT);
                if (label.contains("fov") || label.contains("field of view")
                        || label.contains("gezichtsveld")) {
                    targetRow = Math.max(targetRow, button.yPosition);
                }
            }

            if (targetRow < 0) targetRow = screen.height / 6 + 48;
            int y = targetRow + 24;
            buttons.add(new GuiButton(THIRD_PERSON_BUTTON_ID,
                    screen.width / 2 - 100, y, 200, 20, "Third Person"));
        } catch (Exception error) {
            System.err.println("[AlphaClient] Could not add Third Person prototype option: " + error.getMessage());
        }
    }

    public static boolean handleThirdPersonButton(Object screenObject, Object buttonObject) {
        if (!(screenObject instanceof GuiScreen) || !(buttonObject instanceof GuiButton)) return false;
        GuiButton button = (GuiButton) buttonObject;
        if (button.id != THIRD_PERSON_BUTTON_ID) return false;

        Minecraft.getMinecraft().displayGuiScreen(new ThirdPersonOptionsGui((GuiScreen) screenObject));
        return true;
    }

    private static Field findButtonListField() throws NoSuchFieldException {
        try {
            return GuiScreen.class.getDeclaredField("buttonList");
        } catch (NoSuchFieldException ignored) {
            return GuiScreen.class.getDeclaredField("field_146292_n");
        }
    }
}
