package com.alpharip.staffmod.singleplayer.customenchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemBook;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.util.ResourceLocation;

/**
 * Base definition for AlphaClient's Singleplayer-only custom enchantments.
 * Enchantment levels are stored by Minecraft in the normal item enchantment NBT.
 */
public final class CustomEnchantment extends Enchantment {
    public enum Rarity {
        UNCOMMON(11), RARE(6), LEGENDARY(3);
        public final int weight;
        Rarity(int weight) { this.weight = weight; }
    }

    public enum Target {
        SWORD, BOW, TOOL, PICKAXE, AXE, SHOVEL, MINING_TOOL, COMBAT_AND_MINING, ARMOR, HELMET, BOOTS, CHESTPLATE_LEGGINGS, ANY_EQUIPMENT
    }

    private final String key;
    private final Target target;
    private final Rarity rarity;
    private final int maximumLevel;

    public CustomEnchantment(int id, String key, Target target, Rarity rarity, int maximumLevel) {
        super(id, new ResourceLocation("alphaclient", key), rarity.weight, typeFor(target));
        this.key = key;
        this.target = target;
        this.rarity = rarity;
        this.maximumLevel = maximumLevel;
        setName("alphaclient." + key);
    }

    private static EnumEnchantmentType typeFor(Target target) {
        switch (target) {
            case SWORD: return EnumEnchantmentType.WEAPON;
            case BOW: return EnumEnchantmentType.BOW;
            case TOOL: case PICKAXE: case AXE: case SHOVEL: case MINING_TOOL: return EnumEnchantmentType.DIGGER;
            case COMBAT_AND_MINING: return EnumEnchantmentType.ALL;
            case HELMET: return EnumEnchantmentType.ARMOR_HEAD;
            case BOOTS: return EnumEnchantmentType.ARMOR_FEET;
            case CHESTPLATE_LEGGINGS: return EnumEnchantmentType.ARMOR;
            case ARMOR: return EnumEnchantmentType.ARMOR;
            default: return EnumEnchantmentType.ALL;
        }
    }

    @Override
    public int getMinEnchantability(int level) {
        return 10 + (level - 1) * 10;
    }

    @Override
    public int getMaxEnchantability(int level) {
        return getMinEnchantability(level) + 20;
    }

    @Override
    public int getMaxLevel() {
        return maximumLevel;
    }

    @Override
    public boolean canApply(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return false;
        Item item = stack.getItem();
        if (item instanceof ItemBook) return true;
        switch (target) {
            case SWORD: return item instanceof ItemSword;
            case BOW: return item instanceof ItemBow;
            case TOOL: return item instanceof ItemTool;
            case PICKAXE: return item instanceof ItemPickaxe;
            case AXE: return item instanceof ItemAxe;
            case SHOVEL: return item instanceof ItemSpade;
            case MINING_TOOL: return item instanceof ItemPickaxe || item instanceof ItemAxe || item instanceof ItemSpade;
            case COMBAT_AND_MINING:
                return item instanceof ItemSword || item instanceof ItemBow
                        || item instanceof ItemPickaxe || item instanceof ItemAxe || item instanceof ItemSpade;
            case ARMOR: return item instanceof ItemArmor;
            case HELMET: return item instanceof ItemArmor && ((ItemArmor) item).armorType == 0;
            case BOOTS: return item instanceof ItemArmor && ((ItemArmor) item).armorType == 3;
            case CHESTPLATE_LEGGINGS:
                return item instanceof ItemArmor && (((ItemArmor) item).armorType == 1
                        || ((ItemArmor) item).armorType == 2);
            case ANY_EQUIPMENT:
                return item instanceof ItemSword || item instanceof ItemBow
                        || item instanceof ItemTool || item instanceof ItemArmor;
            default: return false;
        }
    }

    @Override
    public boolean canApplyTogether(Enchantment other) {
        if (other instanceof CustomEnchantment) {
            String otherKey = ((CustomEnchantment) other).getKey();
            if (("vein_miner".equals(key) && "excavator".equals(otherKey))
                    || ("excavator".equals(key) && "vein_miner".equals(otherKey))) return false;
        }
        return super.canApplyTogether(other);
    }

    public String getKey() { return key; }
    public Target getTarget() { return target; }
    public Rarity getRarity() { return rarity; }
}
