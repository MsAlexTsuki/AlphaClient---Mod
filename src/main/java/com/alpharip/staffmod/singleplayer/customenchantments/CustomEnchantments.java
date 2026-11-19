package com.alpharip.staffmod.singleplayer.customenchantments;

import net.minecraft.enchantment.Enchantment;

/**
 * Central registry and rarity catalogue for AlphaClient custom enchantments.
 * IDs are kept in a dedicated range and namespaced to avoid vanilla collisions.
 */
public final class CustomEnchantments {
    private static final int FIRST_ID = 180;
    private static int nextId = FIRST_ID;
    private static boolean registered;

    public static CustomEnchantment LIFESTEAL, EXECUTE, VENOM, RAGE, DOUBLE_STRIKE;
    public static CustomEnchantment EXPLOSIVE, PIERCING, VENOM_SHOT, MULTISHOT;
    public static CustomEnchantment DODGE, REJUVENATION, SPEED, THORNS_PLUS, NIGHT_VISION;
    public static CustomEnchantment VEIN_MINER, AUTO_SMELT, TREE_FELLER, MAGNETIC, EXCAVATOR;
    public static CustomEnchantment TELEPATHY, EXPERIENCE, MAGNET, HASTE, AUTO_PICKUP, SOULBOUND;

    private CustomEnchantments() { }

    public static synchronized void register() {
        if (registered) return;
        LIFESTEAL = add("lifesteal", CustomEnchantment.Target.SWORD, CustomEnchantment.Rarity.RARE, 5);
        EXECUTE = add("execute", CustomEnchantment.Target.SWORD, CustomEnchantment.Rarity.LEGENDARY, 5);
        VENOM = add("venom", CustomEnchantment.Target.SWORD, CustomEnchantment.Rarity.RARE, 5);
        RAGE = add("rage", CustomEnchantment.Target.SWORD, CustomEnchantment.Rarity.LEGENDARY, 5);
        DOUBLE_STRIKE = add("double_strike", CustomEnchantment.Target.SWORD, CustomEnchantment.Rarity.RARE, 3);

        EXPLOSIVE = add("explosive", CustomEnchantment.Target.BOW, CustomEnchantment.Rarity.LEGENDARY, 5);
        PIERCING = add("piercing", CustomEnchantment.Target.BOW, CustomEnchantment.Rarity.RARE, 5);
        VENOM_SHOT = add("venom_shot", CustomEnchantment.Target.BOW, CustomEnchantment.Rarity.RARE, 5);
        MULTISHOT = add("multishot", CustomEnchantment.Target.BOW, CustomEnchantment.Rarity.LEGENDARY, 3);

        DODGE = add("dodge", CustomEnchantment.Target.CHESTPLATE_LEGGINGS, CustomEnchantment.Rarity.LEGENDARY, 5);
        REJUVENATION = add("rejuvenation", CustomEnchantment.Target.ARMOR, CustomEnchantment.Rarity.RARE, 5);
        SPEED = add("speed", CustomEnchantment.Target.BOOTS, CustomEnchantment.Rarity.RARE, 3);
        THORNS_PLUS = add("thorns_plus", CustomEnchantment.Target.ARMOR, CustomEnchantment.Rarity.RARE, 5);
        NIGHT_VISION = add("night_vision", CustomEnchantment.Target.HELMET, CustomEnchantment.Rarity.UNCOMMON, 1);

        VEIN_MINER = add("vein_miner", CustomEnchantment.Target.TOOL, CustomEnchantment.Rarity.RARE, 3);
        AUTO_SMELT = add("auto_smelt", CustomEnchantment.Target.TOOL, CustomEnchantment.Rarity.RARE, 1);
        TREE_FELLER = add("tree_feller", CustomEnchantment.Target.TOOL, CustomEnchantment.Rarity.RARE, 3);
        MAGNETIC = add("magnetic", CustomEnchantment.Target.TOOL, CustomEnchantment.Rarity.UNCOMMON, 3);
        EXCAVATOR = add("excavator", CustomEnchantment.Target.TOOL, CustomEnchantment.Rarity.LEGENDARY, 3);
        TELEPATHY = add("telepathy", CustomEnchantment.Target.TOOL, CustomEnchantment.Rarity.UNCOMMON, 1);
        EXPERIENCE = add("experience", CustomEnchantment.Target.ANY_EQUIPMENT, CustomEnchantment.Rarity.UNCOMMON, 5);
        MAGNET = add("magnet", CustomEnchantment.Target.BOOTS, CustomEnchantment.Rarity.UNCOMMON, 3);
        HASTE = add("haste", CustomEnchantment.Target.TOOL, CustomEnchantment.Rarity.RARE, 3);
        AUTO_PICKUP = add("auto_pickup", CustomEnchantment.Target.BOOTS, CustomEnchantment.Rarity.UNCOMMON, 3);
        SOULBOUND = add("soulbound", CustomEnchantment.Target.ANY_EQUIPMENT, CustomEnchantment.Rarity.LEGENDARY, 1);

        registered = true;
    }

    private static CustomEnchantment add(String key, CustomEnchantment.Target target,
                                         CustomEnchantment.Rarity rarity, int maxLevel) {
        while (nextId < 256 && Enchantment.getEnchantmentById(nextId) != null) nextId++;
        if (nextId >= 256) throw new IllegalStateException("No free enchantment IDs remain in the AlphaClient range.");
        int id = nextId++;
        CustomEnchantment enchantment = new CustomEnchantment(id, key, target, rarity, maxLevel);
        return enchantment;
    }

    public static CustomEnchantment[] getAll() {
        return new CustomEnchantment[] {
                LIFESTEAL, EXECUTE, VENOM, RAGE, DOUBLE_STRIKE,
                EXPLOSIVE, PIERCING, VENOM_SHOT, MULTISHOT,
                DODGE, REJUVENATION, SPEED, THORNS_PLUS, NIGHT_VISION,
                VEIN_MINER, AUTO_SMELT, TREE_FELLER, MAGNETIC, EXCAVATOR,
                TELEPATHY, EXPERIENCE, MAGNET, HASTE, AUTO_PICKUP, SOULBOUND
        };
    }

    public static boolean isCustom(Enchantment enchantment) {
        return enchantment instanceof CustomEnchantment;
    }

    public static boolean isRegistered() {
        return registered;
    }
}
