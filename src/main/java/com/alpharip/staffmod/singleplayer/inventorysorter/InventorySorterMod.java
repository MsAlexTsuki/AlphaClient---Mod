// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.singleplayer.inventorysorter;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Manual chest and inventory sorting controls.
 *
 * No automatic sorting and no double-click behavior are registered here.
 * Middle-click sorts the section beneath the cursor.
 */
public final class InventorySorterMod {
    private static final int BUTTON_Y = 4;
    private static final int BUTTON_SIZE = 12;
    private static final int BUTTON_GAP = 3;
    private static final int QUICK_GRAB = 0;
    private static final int SORT_CHEST = 1;
    private static final int SORT_INVENTORY = 2;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onChestDraw(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!(event.gui instanceof GuiChest) || !InventorySorterAccess.isAvailable(Minecraft.getMinecraft())) {
            return;
        }

        int left = (event.gui.width - 176) / 2;
        int rightStart = left + 176 - 5 - (BUTTON_SIZE * 3 + BUTTON_GAP * 2);
        int mouseX = event.mouseX;
        int mouseY = event.mouseY;

        for (int i = 0; i < 3; i++) {
            int x = rightStart + i * (BUTTON_SIZE + BUTTON_GAP);
            boolean hovered = mouseX >= x && mouseX < x + BUTTON_SIZE
                    && mouseY >= getChestTop(event.gui) + BUTTON_Y
                    && mouseY < getChestTop(event.gui) + BUTTON_Y + BUTTON_SIZE;
            drawButton(x, getChestTop(event.gui) + BUTTON_Y, i, hovered);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onMouseInput(GuiScreenEvent.MouseInputEvent.Pre event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!InventorySorterAccess.isAvailable(mc) || mc.thePlayer == null
                || !(event.gui instanceof GuiChest || event.gui instanceof GuiInventory)
                || !Mouse.getEventButtonState()) {
            return;
        }

        int button = Mouse.getEventButton();
        int mouseX = Mouse.getEventX() * event.gui.width / mc.displayWidth;
        int mouseY = event.gui.height - Mouse.getEventY() * event.gui.height / mc.displayHeight - 1;

        if (event.gui instanceof GuiChest && button == 0) {
            int left = (event.gui.width - 176) / 2;
            int top = getChestTop(event.gui);
            int startX = left + 176 - 5 - (BUTTON_SIZE * 3 + BUTTON_GAP * 2);
            for (int i = 0; i < 3; i++) {
                int x = startX + i * (BUTTON_SIZE + BUTTON_GAP);
                if (inside(mouseX, mouseY, x, top + BUTTON_Y, BUTTON_SIZE, BUTTON_SIZE)) {
                    ContainerChest chest = (ContainerChest) mc.thePlayer.openContainer;
                    if (i == QUICK_GRAB) {
                        grabAll(mc, chest);
                    } else if (i == SORT_CHEST) {
                        sortChest(mc, chest);
                    } else {
                        sortPlayerInventory(mc, chest);
                    }
                    event.setCanceled(true);
                    return;
                }
            }
        }

        // Middle-click sorts the section under the cursor, including when the
        // clicked slot is empty. Chest slots and the player's main inventory
        // are separate sections; hotbar slots are deliberately excluded.
        if (button == 2) {
            Container container = mc.thePlayer.openContainer;
            Slot hoveredSlot = findSlotAt(event.gui, container, mouseX, mouseY);
            if (hoveredSlot == null) return;

            if (event.gui instanceof GuiChest) {
                ContainerChest chest = (ContainerChest) container;
                int chestSize = chest.getLowerChestInventory().getSizeInventory();
                if (hoveredSlot.slotNumber >= 0 && hoveredSlot.slotNumber < chestSize) {
                    sortChest(mc, chest);
                    event.setCanceled(true);
                    return;
                }
            }

            if (hoveredSlot.inventory == mc.thePlayer.inventory
                    && hoveredSlot.getSlotIndex() >= 9 && hoveredSlot.getSlotIndex() <= 35) {
                sortPlayerInventory(mc, container);
                event.setCanceled(true);
            }
        }
    }

    private static void drawButton(int x, int y, int type, boolean hovered) {
        int border = hovered ? 0xFFFFD866 : 0xFF555A64;
        int background = hovered ? 0xCC343943 : 0xAA11151B;
        Gui.drawRect(x, y, x + BUTTON_SIZE, y + BUTTON_SIZE, background);
        Gui.drawRect(x, y, x + BUTTON_SIZE, y + 1, border);
        Gui.drawRect(x, y + BUTTON_SIZE - 1, x + BUTTON_SIZE, y + BUTTON_SIZE, border);
        Gui.drawRect(x, y, x + 1, y + BUTTON_SIZE, border);
        Gui.drawRect(x + BUTTON_SIZE - 1, y, x + BUTTON_SIZE, y + BUTTON_SIZE, border);

        int icon = 0xFFE8EAF0;
        if (type == QUICK_GRAB) {
            Gui.drawRect(x + 5, y + 2, x + 7, y + 7, icon);
            Gui.drawRect(x + 3, y + 5, x + 5, y + 7, icon);
            Gui.drawRect(x + 7, y + 5, x + 9, y + 7, icon);
            Gui.drawRect(x + 2, y + 9, x + 10, y + 10, icon);
        } else if (type == SORT_CHEST) {
            Gui.drawRect(x + 2, y + 3, x + 10, y + 4, icon);
            Gui.drawRect(x + 2, y + 6, x + 8, y + 7, icon);
            Gui.drawRect(x + 2, y + 9, x + 6, y + 10, icon);
            Gui.drawRect(x + 9, y + 6, x + 10, y + 10, icon);
        } else {
            Gui.drawRect(x + 2, y + 2, x + 5, y + 5, icon);
            Gui.drawRect(x + 7, y + 2, x + 10, y + 5, icon);
            Gui.drawRect(x + 2, y + 7, x + 5, y + 10, icon);
            Gui.drawRect(x + 7, y + 7, x + 10, y + 10, icon);
        }
    }

    private static boolean inside(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private static int getChestTop(GuiScreen gui) {
        ContainerChest chest = (ContainerChest) Minecraft.getMinecraft().thePlayer.openContainer;
        int rows = chest.getLowerChestInventory().getSizeInventory() / 9;
        int guiHeight = 114 + rows * 18;
        return (gui.height - guiHeight) / 2;
    }

    private static Slot findSlotAt(GuiScreen gui, Container container, int mouseX, int mouseY) {
        int left;
        int top;
        if (gui instanceof GuiChest) {
            left = (gui.width - 176) / 2;
            top = getChestTop(gui);
        } else {
            left = (gui.width - 176) / 2;
            top = (gui.height - 166) / 2;
        }

        for (Object value : container.inventorySlots) {
            Slot slot = (Slot) value;
            if (inside(mouseX, mouseY, left + slot.xDisplayPosition,
                    top + slot.yDisplayPosition, 16, 16)) {
                return slot;
            }
        }
        return null;
    }

    private static void grabAll(Minecraft mc, ContainerChest chest) {
        int chestSize = chest.getLowerChestInventory().getSizeInventory();
        for (int slot = 0; slot < chestSize; slot++) {
            ItemStack before = chest.getSlot(slot).getStack();
            if (before == null) continue;
            int previousCount = before.stackSize;
            mc.playerController.windowClick(chest.windowId, slot, 0, 1, mc.thePlayer);
            ItemStack remaining = chest.getSlot(slot).getStack();
            if (remaining != null && remaining.stackSize >= previousCount) {
                break;
            }
        }
    }

    private static void sortChest(Minecraft mc, ContainerChest chest) {
        int chestSize = chest.getLowerChestInventory().getSizeInventory();
        List<Integer> slots = new ArrayList<Integer>();
        for (int i = 0; i < chestSize; i++) slots.add(i);
        sortSlots(mc, chest, slots);
    }

    private static void sortPlayerInventory(Minecraft mc, Container container) {
        List<Integer> slots = new ArrayList<Integer>();
        for (Object value : container.inventorySlots) {
            Slot slot = (Slot) value;
            if (slot.inventory == mc.thePlayer.inventory
                    && slot.getSlotIndex() >= 9 && slot.getSlotIndex() <= 35) {
                slots.add(slot.slotNumber);
            }
        }
        sortSlots(mc, container, slots);
    }

    private static void sortSlots(Minecraft mc, Container container, List<Integer> slots) {
        Collections.sort(slots, new Comparator<Integer>() {
            @Override
            public int compare(Integer first, Integer second) {
                return first.compareTo(second);
            }
        });

        for (int i = 0; i < slots.size(); i++) {
            int smallest = i;
            for (int j = i + 1; j < slots.size(); j++) {
                ItemStack candidate = container.getSlot(slots.get(j)).getStack();
                ItemStack current = container.getSlot(slots.get(smallest)).getStack();
                if (compareStacks(candidate, current) < 0) smallest = j;
            }
            if (smallest != i) {
                swapSlots(mc, container, slots.get(i), slots.get(smallest));
            }
        }
    }

    private static void swapSlots(Minecraft mc, Container container, int first, int second) {
        mc.playerController.windowClick(container.windowId, first, 0, 0, mc.thePlayer);
        mc.playerController.windowClick(container.windowId, second, 0, 0, mc.thePlayer);
        mc.playerController.windowClick(container.windowId, first, 0, 0, mc.thePlayer);
    }

    private static int compareStacks(ItemStack first, ItemStack second) {
        if (first == null) return second == null ? 0 : 1;
        if (second == null) return -1;

        String firstId = String.valueOf(Item.itemRegistry.getNameForObject(first.getItem()));
        String secondId = String.valueOf(Item.itemRegistry.getNameForObject(second.getItem()));
        int result = firstId.compareToIgnoreCase(secondId);
        if (result != 0) return result;

        result = Integer.compare(first.getItemDamage(), second.getItemDamage());
        if (result != 0) return result;

        result = first.getDisplayName().compareToIgnoreCase(second.getDisplayName());
        if (result != 0) return result;

        String firstTag = String.valueOf(first.getTagCompound());
        String secondTag = String.valueOf(second.getTagCompound());
        result = firstTag.compareTo(secondTag);
        if (result != 0) return result;

        return Integer.compare(second.stackSize, first.stackSize);
    }
}
