package com.alpharip.staffmod.inventorysearch;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.lang.reflect.Field;

/** Adds a clickable search box and client-only highlights to supported containers. */
public final class InventorySearchMod {
    private static final Minecraft MC = Minecraft.getMinecraft();
    private static GuiScreen owner;
    private static GuiTextField field;
    private static String query = "";

    private static boolean supports(GuiScreen screen) {
        if (!(screen instanceof GuiContainer)) {
            return false;
        }

        GuiContainer container = (GuiContainer) screen;
        return InventorySearchInventory.supports(container)
                || (screen instanceof GuiChest
                && (InventorySearchChest.supports((GuiChest) screen)
                || InventorySearchEnderChest.supports((GuiChest) screen)));
    }

    private static void init(GuiScreen screen) {
        if (owner == screen && field != null) {
            return;
        }

        owner = screen;
        GuiContainer container = (GuiContainer) screen;
        int left = read(container, "guiLeft", "field_147003_i");
        int top = read(container, "guiTop", "field_147009_r");
        field = new GuiTextField(
                9231, MC.fontRendererObj, left + 72, top + 4, 96, 12
        );
        field.setMaxStringLength(64);
        field.setText(query);
    }

    @SubscribeEvent
    public void onInit(GuiScreenEvent.InitGuiEvent.Post event) {
        if (InventorySearchSettings.isEnabled() && supports(event.gui)) {
            init(event.gui);
        } else if (owner != event.gui) {
            owner = null;
            field = null;
        }
    }

    @SubscribeEvent
    public void onMouse(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (!InventorySearchSettings.isEnabled() || !supports(event.gui)) {
            return;
        }

        init(event.gui);
        if (!Mouse.getEventButtonState()) {
            return;
        }

        int x = Mouse.getEventX() * event.gui.width / MC.displayWidth;
        int y = event.gui.height - Mouse.getEventY() * event.gui.height / MC.displayHeight - 1;
        if (x >= field.xPosition && x < field.xPosition + field.width
                && y >= field.yPosition && y < field.yPosition + field.height) {
            field.mouseClicked(x, y, Mouse.getEventButton());
            event.setCanceled(true);
        } else {
            field.setFocused(false);
        }
    }

    @SubscribeEvent
    public void onKey(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        if (!InventorySearchSettings.isEnabled() || owner != event.gui
                || field == null || !field.isFocused()) {
            return;
        }

        if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
            field.setFocused(false);
            event.setCanceled(true);
            return;
        }

        field.textboxKeyTyped(Keyboard.getEventCharacter(), Keyboard.getEventKey());
        query = field.getText();
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onDraw(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!InventorySearchSettings.isEnabled() || !supports(event.gui)) {
            return;
        }

        init(event.gui);
        field.drawTextBox();
        String searchText = field.getText().trim();
        if (searchText.isEmpty()) {
            MC.fontRendererObj.drawStringWithShadow(
                    "Search items...", field.xPosition + 5, field.yPosition + 5, 0xFFAAAAAA
            );
            return;
        }

        GuiContainer container = (GuiContainer) event.gui;
        int left = read(container, "guiLeft", "field_147003_i");
        int top = read(container, "guiTop", "field_147009_r");
        int hits = 0;
        int total = 0;

        for (Object object : container.inventorySlots.inventorySlots) {
            if (!(object instanceof Slot)) {
                continue;
            }

            Slot slot = (Slot) object;
            if (!slot.getHasStack()) {
                continue;
            }

            total++;
            boolean matches = InventorySearchMatcher.matches(slot.getStack(), searchText);
            if (matches) {
                hits++;
            }

            int x = left + slot.xDisplayPosition;
            int y = top + slot.yDisplayPosition;
            Gui.drawRect(x, y, x + 16, y + 16, matches ? 0x5533FF55 : 0x99000000);
        }

        MC.fontRendererObj.drawStringWithShadow(
                hits + "/" + total, field.xPosition + field.width + 5, field.yPosition + 5, 0xFFFFFFFF
        );
    }

    private static int read(GuiContainer container, String... names) {
        for (String name : names) {
            try {
                Field reflected = GuiContainer.class.getDeclaredField(name);
                reflected.setAccessible(true);
                return reflected.getInt(container);
            } catch (Exception ignored) {
                // Try the next mapped/obfuscated field name.
            }
        }
        return 0;
    }
}
