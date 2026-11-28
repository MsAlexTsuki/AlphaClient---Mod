package com.alpharip.staffmod.Singleplayer.EasyCraft;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Reads the recipes already registered by Minecraft and Forge. */
public final class EasyCraftRecipeCatalog {
    private final List<EasyCraftRecipe> recipes = new ArrayList<EasyCraftRecipe>();

    public EasyCraftRecipeCatalog() {
        refresh();
    }

    public synchronized void refresh() {
        recipes.clear();
        for (IRecipe recipe : CraftingManager.getInstance().getRecipeList()) {
            try {
                ItemStack output = recipe == null ? null : recipe.getRecipeOutput();
                if (output != null && output.getItem() != null) recipes.add(new EasyCraftRecipe(recipe));
            } catch (RuntimeException ignored) {
                // One malformed third-party recipe must not prevent the recipe book from opening.
            }
        }
    }

    public synchronized List<EasyCraftRecipe> getAll() {
        return Collections.unmodifiableList(new ArrayList<EasyCraftRecipe>(recipes));
    }

    public synchronized List<EasyCraftRecipe> find(String query, EasyCraftCategory category,
                                                     boolean favoritesOnly, EasyCraftFavorites favorites) {
        String normalized = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        List<EasyCraftRecipe> result = new ArrayList<EasyCraftRecipe>();
        for (EasyCraftRecipe recipe : recipes) {
            if (category != null && category != EasyCraftCategory.ALL
                    && recipe.getCategory() != category) continue;
            if (favoritesOnly && (favorites == null || !favorites.contains(recipe.getKey()))) continue;
            if (!normalized.isEmpty() && !recipe.getDisplayName().toLowerCase(Locale.ROOT).contains(normalized))
                continue;
            result.add(recipe);
        }
        return result;
    }
}
