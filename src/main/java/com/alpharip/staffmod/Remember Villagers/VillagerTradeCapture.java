package com.alpharip.staffmod.remembervillagers;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.village.IMerchant;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;

import java.util.ArrayList;
import java.util.List;

/** Reads the currently open merchant GUI and converts its offers into local snapshots. */
final class VillagerTradeCapture {
    private final VillagerTradeRegistry registry;

    VillagerTradeCapture(VillagerTradeRegistry registry) {
        this.registry = registry;
    }

    boolean capture(Object merchantObject) {
        if (!(merchantObject instanceof IMerchant)) return false;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null) return false;

        IMerchant merchant = (IMerchant) merchantObject;
        MerchantRecipeList recipes;
        try {
            recipes = merchant.getRecipes(mc.thePlayer);
        } catch (RuntimeException ignored) {
            return false;
        }
        if (recipes == null || recipes.isEmpty()) return false;

        Entity entity = merchantObject instanceof Entity ? (Entity) merchantObject : null;
        String id = entity == null ? merchantObject.getClass().getName()
                : entity.getUniqueID().toString();
        String name = entity == null ? "Villager" : entity.getDisplayName().getUnformattedText();
        int dimension = mc.theWorld == null ? 0 : mc.theWorld.provider.getDimensionId();
        int x = entity == null ? 0 : entity.getPosition().getX();
        int y = entity == null ? 0 : entity.getPosition().getY();
        int z = entity == null ? 0 : entity.getPosition().getZ();

        List<VillagerTradeEntry> entries = new ArrayList<VillagerTradeEntry>();
        for (MerchantRecipe recipe : recipes) {
            if (recipe == null) continue;
            entries.add(new VillagerTradeEntry(
                    itemName(recipe.getItemToBuy()), itemCount(recipe.getItemToBuy()),
                    itemName(recipe.getSecondItemToBuy()), itemCount(recipe.getSecondItemToBuy()),
                    itemName(recipe.getItemToSell()), itemCount(recipe.getItemToSell())));
        }
        if (entries.isEmpty()) return false;

        VillagerTradeRecord record = new VillagerTradeRecord(
                id, name, dimension, x, y, z, System.currentTimeMillis(), entries);
        return registry.remember(record);
    }

    private String itemName(ItemStack stack) {
        return stack == null ? "" : stack.getDisplayName();
    }

    private int itemCount(ItemStack stack) {
        return stack == null ? 0 : stack.stackSize;
    }
}
