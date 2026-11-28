package com.alpharip.staffmod.remembervillagers;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiMerchant;
import net.minecraft.village.IMerchant;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Field;

/** Captures merchant offers when the villager trading screen is initialized. */
final class VillagerTradeEventHandler {
    private final VillagerTradeCapture capture;

    VillagerTradeEventHandler(VillagerTradeCapture capture) {
        this.capture = capture;
    }

    @SubscribeEvent
    public void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
        GuiScreen screen = event.gui;
        if (!(screen instanceof GuiMerchant)) return;
        IMerchant merchant = findMerchant(screen);
        if (merchant != null && capture.capture(merchant)) {
            remembervillagertrade.getInstance().save();
        }
    }

    private IMerchant findMerchant(Object screen) {
        Class<?> type = screen.getClass();
        while (type != null) {
            Field[] fields = type.getDeclaredFields();
            for (Field field : fields) {
                if (!IMerchant.class.isAssignableFrom(field.getType())) continue;
                try {
                    field.setAccessible(true);
                    Object value = field.get(screen);
                    if (value instanceof IMerchant) return (IMerchant) value;
                } catch (IllegalAccessException | SecurityException ignored) {
                    // Try the next field; an inaccessible merchant is simply not captured.
                }
            }
            type = type.getSuperclass();
        }
        return null;
    }
}
