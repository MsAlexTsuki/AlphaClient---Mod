package com.alpharip.staffmod.Singleplayer.EasyCraft;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Immutable display snapshot for one recipe registered in Minecraft 1.8.9. */
public final class EasyCraftRecipe {
    private final IRecipe recipe;
    private final ItemStack output;
    private final List<ItemStack> ingredients;
    private final EasyCraftCategory category;
    private final String key;

    public EasyCraftRecipe(IRecipe recipe) {
        if (recipe == null) throw new IllegalArgumentException("recipe cannot be null");
        ItemStack result = recipe.getRecipeOutput();
        if (result == null || result.getItem() == null) {
            throw new IllegalArgumentException("recipe output cannot be empty");
        }

        this.recipe = recipe;
        this.output = result.copy();
        this.ingredients = Collections.unmodifiableList(readIngredients(recipe));
        this.category = categorize(output);
        this.key = createKey(output, ingredients);
    }

    public IRecipe getVanillaRecipe() {
        return recipe;
    }

    public ItemStack getOutput() {
        return output.copy();
    }

    public List<ItemStack> getIngredients() {
        List<ItemStack> copy = new ArrayList<ItemStack>();
        for (ItemStack stack : ingredients) copy.add(stack == null ? null : stack.copy());
        return Collections.unmodifiableList(copy);
    }

    public EasyCraftCategory getCategory() {
        return category;
    }

    public String getKey() {
        return key;
    }

    public String getDisplayName() {
        return output.getDisplayName();
    }

    private static List<ItemStack> readIngredients(IRecipe recipe) {
        List<ItemStack> result = new ArrayList<ItemStack>();
        if (recipe instanceof ShapedRecipes) {
            ItemStack[] source = ((ShapedRecipes) recipe).recipeItems;
            for (ItemStack stack : source) addCopy(result, stack);
        } else if (recipe instanceof ShapelessRecipes) {
            List<ItemStack> source = ((ShapelessRecipes) recipe).recipeItems;
            for (ItemStack stack : source) addCopy(result, stack);
        }
        return result;
    }

    private static void addCopy(List<ItemStack> target, ItemStack stack) {
        if (stack != null && stack.getItem() != null) target.add(stack.copy());
    }

    private static EasyCraftCategory categorize(ItemStack stack) {
        ResourceLocation location = (ResourceLocation) Item.itemRegistry.getNameForObject(stack.getItem());
        String name = location == null ? "" : location.getResourcePath().toLowerCase();

        if (name.contains("sword") || name.contains("bow") || name.contains("arrow")
                || name.contains("armor") || name.contains("helmet") || name.contains("chestplate")
                || name.contains("leggings") || name.contains("boots")) return EasyCraftCategory.COMBAT;
        if (name.contains("pickaxe") || name.contains("axe") || name.contains("shovel")
                || name.contains("hoe") || name.contains("shears") || name.contains("flint_and_steel"))
            return EasyCraftCategory.TOOLS;
        if (name.contains("bread") || name.contains("cake") || name.contains("cookie")
                || name.contains("pie") || name.contains("stew") || name.contains("food")
                || name.contains("apple") || name.contains("carrot") || name.contains("fish")
                || name.contains("beef") || name.contains("pork") || name.contains("chicken"))
            return EasyCraftCategory.FOOD;
        if (name.contains("redstone") || name.contains("repeater") || name.contains("comparator")
                || name.contains("piston") || name.contains("hopper") || name.contains("dispenser")
                || name.contains("dropper") || name.contains("daylight_detector"))
            return EasyCraftCategory.REDSTONE;
        if (name.contains("planks") || name.contains("slab") || name.contains("stairs")
                || name.contains("wall") || name.contains("fence") || name.contains("door")
                || name.contains("brick") || name.contains("stone") || name.contains("glass")
                || name.contains("wool") || name.contains("carpet") || name.contains("block"))
            return EasyCraftCategory.BUILDING;
        return EasyCraftCategory.MISC;
    }

    private static String createKey(ItemStack output, List<ItemStack> ingredients) {
        ResourceLocation location = (ResourceLocation) Item.itemRegistry.getNameForObject(output.getItem());
        StringBuilder key = new StringBuilder(location == null ? "unknown" : location.toString());
        key.append('#').append(output.getMetadata());
        for (ItemStack ingredient : ingredients) {
            ResourceLocation ingredientName = (ResourceLocation) Item.itemRegistry.getNameForObject(ingredient.getItem());
            key.append('|').append(ingredientName == null ? "unknown" : ingredientName.toString())
                    .append('#').append(ingredient.getMetadata());
        }
        return key.toString();
    }
}
