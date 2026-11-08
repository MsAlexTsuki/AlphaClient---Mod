package com.alpharip.staffmod.singleplayer.inventorysorter;

import com.alpharip.staffmod.modules.AlphaModuleFeature;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Adds manual chest grabbing and sorting to vanilla chest/player inventory GUIs.
 * Every action is restricted to an active integrated singleplayer world.
 */
public final class InventorySorterMod {
    private static final int BUTTON_GRAB_ALL = 9411;
    private static final int BUTTON_SORT_CHEST = 9412;
    private static final int BUTTON_SORT_INVENTORY = 9413;

    private static SortMode nextSortMode = SortMode.ALPHABETICAL;

    private enum SortMode {
        ALPHABETICAL("Alphabetical"),
        TYPE_AND_NAME("Item type + name"),
        AMOUNT("Amount (most first)");

        private final String label;

        SortMode(String label) {
            this.label = label;
        }

        private SortMode next() {
            SortMode[] modes = values();
            return modes[(ordinal() + 1) % modes.length];
        }
    }

    private static AlphaModuleFeature profileFeature() {
        return AlphaModuleRegistry.findFeature("inventory-sorter");
    }

    private static boolean isEnabled() {
        AlphaModuleFeature feature = profileFeature();
        return feature != null && feature.isEnabled();
    }

    private static boolean isAvailable(Minecraft mc) {
        return isEnabled() && InventorySorterAccess.isAvailable(mc)
                && mc.playerController != null;
    }

    @SubscribeEvent
    public void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (event == null || !(event.gui instanceof GuiChest)
                || !isAvailable(mc)) {
            return;
        }

        GuiChest screen = (GuiChest) event.gui;
        if (!(screen.inventorySlots instanceof ContainerChest)) {
            return;
        }

        int left = readGuiPosition(screen, "guiLeft", "field_147003_i");
        int top = readGuiPosition(screen, "guiTop", "field_147009_r");
        int buttonY = Math.max(2, top - 20);
        int x = left + 1;

        // These controls sit directly above the chest window.
        event.buttonList.add(new GuiButton(BUTTON_GRAB_ALL, x, buttonY, 55, 18, "Grab All"));
        x += 57;
        event.buttonList.add(new GuiButton(BUTTON_SORT_CHEST, x, buttonY, 60, 18, "Sort Chest"));
        x += 62;
        event.buttonList.add(new GuiButton(BUTTON_SORT_INVENTORY, x, buttonY, 55, 18, "Sort Inv."));
    }

    @SubscribeEvent
    public void onButtonAction(GuiScreenEvent.ActionPerformedEvent.Post event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (event == null || !isAvailable(mc) || !(event.gui instanceof GuiChest)
                || !(event.gui instanceof GuiContainer)
                || !(((GuiContainer) event.gui).inventorySlots instanceof ContainerChest)
                || event.button == null) {
            return;
        }

        ContainerChest container = (ContainerChest) ((GuiContainer) event.gui).inventorySlots;
        int chestSize = container.getLowerChestInventory().getSizeInventory();

        if (event.button.id == BUTTON_GRAB_ALL) {
            grabChest(mc, container, chestSize);
        } else if (event.button.id == BUTTON_SORT_CHEST) {
            sortSlots(mc, container, 0, chestSize, "chest");
        } else if (event.button.id == BUTTON_SORT_INVENTORY) {
            sortSlots(mc, container, chestSize, 27, "inventory");
        }
    }

    @SubscribeEvent
    public void onMiddleMouseSort(GuiScreenEvent.MouseInputEvent.Pre event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (event == null || !isAvailable(mc) || !(event.gui instanceof GuiContainer)
                || !Mouse.getEventButtonState() || Mouse.getEventButton() != 2) {
            return;
        }

        GuiContainer screen = (GuiContainer) event.gui;
        Container container = screen.inventorySlots;
        int mouseX = Mouse.getEventX() * event.gui.width / mc.displayWidth;
        int mouseY = event.gui.height - Mouse.getEventY() * event.gui.height / mc.displayHeight - 1;
        int left = readGuiPosition(screen, "guiLeft", "field_147003_i");
        int top = readGuiPosition(screen, "guiTop", "field_147009_r");

        for (Object object : container.inventorySlots) {
            if (!(object instanceof Slot)) {
                continue;
            }

            Slot slot = (Slot) object;
            int slotX = left + slot.xDisplayPosition;
            int slotY = top + slot.yDisplayPosition;
            if (mouseX < slotX || mouseX >= slotX + 16
                    || mouseY < slotY || mouseY >= slotY + 16) {
                continue;
            }

            if (container instanceof ContainerChest && screen instanceof GuiChest) {
                int chestSize = ((ContainerChest) container).getLowerChestInventory().getSizeInventory();
                if (slot.slotNumber >= 0 && slot.slotNumber < chestSize) {
                    event.setCanceled(true);
                    sortSlots(mc, container, 0, chestSize, "chest");
                } else if (slot.slotNumber >= chestSize && slot.slotNumber < chestSize + 27) {
                    event.setCanceled(true);
                    sortSlots(mc, container, chestSize, 27, "inventory");
                }
                // The hotbar and every slot outside the two supported areas are untouched.
                return;
            }

            if (container instanceof ContainerPlayer && screen instanceof GuiInventory
                    && slot.slotNumber >= 9 && slot.slotNumber < 36) {
                event.setCanceled(true);
                sortSlots(mc, container, 9, 27, "inventory");
                return;
            }
            return;
        }
    }

    private static void grabChest(Minecraft mc, ContainerChest container, int chestSize) {
        if (mc.thePlayer.inventory.getItemStack() != null) {
            notifyPlayer(mc, "Place the item on your cursor before grabbing the chest.");
            return;
        }

        for (int slotId = 0; slotId < chestSize; slotId++) {
            Slot slot = container.getSlot(slotId);
            if (slot != null && slot.getHasStack()) {
                // Vanilla shift-click transfers the stack into available player inventory space.
                mc.playerController.windowClick(container.windowId, slotId, 0, 1, mc.thePlayer);
            }
        }

        notifyPlayer(mc, "Chest grab finished.");
    }

    private static void sortSlots(Minecraft mc, Container container, int firstSlot,
                                  int slotCount, String area) {
        if (mc.thePlayer.inventory.getItemStack() != null) {
            notifyPlayer(mc, "Place the item on your cursor before sorting.");
            return;
        }
        if (container == null || slotCount < 2 || firstSlot < 0
                || firstSlot + slotCount > container.inventorySlots.size()) {
            return;
        }

        List<ItemStack> current = new ArrayList<ItemStack>(slotCount);
        for (int offset = 0; offset < slotCount; offset++) {
            Slot slot = container.getSlot(firstSlot + offset);
            ItemStack stack = slot == null ? null : slot.getStack();
            current.add(stack == null ? null : stack.copy());
        }

        final SortMode mode = nextSortMode;
        List<ItemStack> sorted = new ArrayList<ItemStack>(current);
        Collections.sort(sorted, comparator(mode));

        for (int target = 0; target < slotCount; target++) {
            if (sameStack(current.get(target), sorted.get(target))) {
                continue;
            }

            int source = -1;
            for (int candidate = target + 1; candidate < slotCount; candidate++) {
                if (sameStack(current.get(candidate), sorted.get(target))) {
                    source = candidate;
                    break;
                }
            }
            if (source < 0) {
                continue;
            }

            int targetSlotId = firstSlot + target;
            int sourceSlotId = firstSlot + source;
            swapSlots(mc, container, targetSlotId, sourceSlotId);
            Collections.swap(current, target, source);
        }

        nextSortMode = nextSortMode.next();
        if (mc.playerController != null) {
            mc.playerController.updateController();
        }
        notifyPlayer(mc, "Sorted " + area + " using " + mode.label + ".");
    }

    private static void swapSlots(Minecraft mc, Container container, int first, int second) {
        Slot firstSlot = container.getSlot(first);
        Slot secondSlot = container.getSlot(second);
        if (firstSlot == null || secondSlot == null) {
            return;
        }

        boolean firstHasStack = firstSlot.getHasStack();
        boolean secondHasStack = secondSlot.getHasStack();
        if (firstHasStack && secondHasStack) {
            clickSlot(mc, container, first);
            clickSlot(mc, container, second);
            clickSlot(mc, container, first);
        } else if (firstHasStack) {
            clickSlot(mc, container, first);
            clickSlot(mc, container, second);
        } else if (secondHasStack) {
            clickSlot(mc, container, second);
            clickSlot(mc, container, first);
        }
    }

    private static void clickSlot(Minecraft mc, Container container, int slotId) {
        mc.playerController.windowClick(container.windowId, slotId, 0, 0, mc.thePlayer);
    }

    private static boolean sameStack(ItemStack first, ItemStack second) {
        if (first == null || second == null) {
            return first == second;
        }
        return ItemStack.areItemStacksEqual(first, second);
    }

    private static Comparator<ItemStack> comparator(final SortMode mode) {
        return new Comparator<ItemStack>() {
            @Override
            public int compare(ItemStack first, ItemStack second) {
                if (first == null) return second == null ? 0 : 1;
                if (second == null) return -1;

                if (mode == SortMode.AMOUNT) {
                    int amount = Integer.compare(second.stackSize, first.stackSize);
                    if (amount != 0) return amount;
                } else if (mode == SortMode.TYPE_AND_NAME) {
                    String firstType = first.getItem().getClass().getSimpleName();
                    String secondType = second.getItem().getClass().getSimpleName();
                    int type = firstType.compareToIgnoreCase(secondType);
                    if (type != 0) return type;
                }

                int name = first.getDisplayName().compareToIgnoreCase(second.getDisplayName());
                if (name != 0) return name;

                String firstId = String.valueOf(Item.itemRegistry.getNameForObject(first.getItem()));
                String secondId = String.valueOf(ItemStack.itemRegistry.getNameForObject(second.getItem()));
                return firstId.compareToIgnoreCase(secondId);
            }
        };
    }

    private static void notifyPlayer(Minecraft mc, String message) {
        if (mc != null && mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText("§6[Inventory Sorter] §f" + message));
        }
    }

    private static int readGuiPosition(GuiContainer screen, String... fieldNames) {
        for (String fieldName : fieldNames) {
            try {
                Field field = GuiContainer.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.getInt(screen);
            } catch (Exception ignored) {
                // Try the next mapped/obfuscated field name.
            }
        }
        return 0;
    }
}
