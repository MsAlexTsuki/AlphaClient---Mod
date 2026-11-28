package com.alpharip.staffmod.timing.brewing;

import java.lang.reflect.Field;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;

/**
 * Isolated 1.8.9 adapter for reading the vanilla Brewing Stand brew-time field.
 *
 * ContainerBrewingStand keeps its backing IInventory private. This adapter
 * searches that container's fields instead of depending on a guessed field
 * name. It returns -1 when the backing inventory cannot be identified.
 */
final class BrewingStandReader {
    private static final int BREW_TIME_FIELD = 0;

    int readRemainingTicks(Container container) {
        if (container == null) return -1;

        Class<?> type = container.getClass();
        while (type != null) {
            Field[] fields = type.getDeclaredFields();
            for (Field field : fields) {
                if (!IInventory.class.isAssignableFrom(field.getType())) continue;

                try {
                    field.setAccessible(true);
                    Object value = field.get(container);
                    if (!(value instanceof IInventory)) continue;

                    IInventory inventory = (IInventory) value;
                    if (inventory.getFieldCount() <= BREW_TIME_FIELD) continue;

                    int ticks = inventory.getField(BREW_TIME_FIELD);
                    return Math.max(0, ticks);
                } catch (IllegalAccessException | SecurityException ignored) {
                    // Try another candidate field; unavailable data is not guessed.
                }
            }
            type = type.getSuperclass();
        }
        return -1;
    }
}
