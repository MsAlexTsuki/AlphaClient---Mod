package com.alpharip.staffmod.singleplayer.customenchantments;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.entity.item.EntityItem;
import net.minecraftforge.event.world.BlockEvent;
import java.util.Iterator;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import java.util.Random;

/** Singleplayer-only runtime effects for the first combat, armour and soulbound enchantments. */
public final class CustomEnchantmentHandler {
    private static final Random RANDOM = new Random();
    private static final String SOULBOUND_KEY = "AlphaClientSoulboundItems";

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (event == null || event.entityLiving == null || !CustomEnchantmentAccess.isAvailable()) return;
        Entity source = event.source == null ? null : event.source.getEntity();

        if (source instanceof EntityPlayer) {
            EntityPlayer attacker = (EntityPlayer) source;
            NBTTagCompound data = attacker.getEntityData();
            if (data.getBoolean("AlphaDoubleStrikeFollowup")) {
                data.removeTag("AlphaDoubleStrikeFollowup");
                event.amount *= .75F;
                return;
            }

            ItemStack weapon = attacker.getHeldItem();
            int lifesteal = level(weapon, CustomEnchantments.LIFESTEAL);
            if (lifesteal > 0) {
                float[] heal = {0F, .10F, .15F, .20F, .25F, .35F};
                attacker.heal(event.amount * heal[Math.min(lifesteal, 5)]);
            }
            int execute = level(weapon, CustomEnchantments.EXECUTE);
            if (execute > 0 && event.entityLiving.getHealth() <= event.entityLiving.getMaxHealth() * .30F)
                event.amount *= 1.0F + .20F * execute;
            int venom = level(weapon, CustomEnchantments.VENOM);
            if (venom > 0)
                event.entityLiving.addPotionEffect(new PotionEffect(Potion.poison.id, 40 + venom * 30,
                        Math.max(0, (venom - 3) / 2)));

            int rage = level(weapon, CustomEnchantments.RAGE);
            if (rage > 0) {
                long now = attacker.worldObj.getTotalWorldTime();
                long last = data.getLong("AlphaRageLastHit");
                int stacks = last > 0 && now - last <= 100L ? data.getInteger("AlphaRageStacks") : 0;
                stacks = Math.min(10, stacks + 1);
                data.setInteger("AlphaRageStacks", stacks);
                data.setLong("AlphaRageLastHit", now);
                event.amount *= 1.0F + Math.min(1.0F, stacks * .05F * rage);
            }

            int doubleStrike = level(weapon, CustomEnchantments.DOUBLE_STRIKE);
            if (doubleStrike > 0 && RANDOM.nextFloat() < .15F * doubleStrike
                    && event.entityLiving.isEntityAlive()) {
                data.setBoolean("AlphaDoubleStrikeFollowup", true);
                attacker.attackTargetEntityWithCurrentItem(event.entityLiving);
                data.removeTag("AlphaDoubleStrikeFollowup");
            }
        }

        if (event.entityLiving instanceof EntityPlayer) {
            EntityPlayer defender = (EntityPlayer) event.entityLiving;
            int dodge = maxArmorLevel(defender, CustomEnchantments.DODGE);
            if (dodge > 0 && RANDOM.nextFloat() < .08F * dodge) {
                event.setCanceled(true);
                return;
            }
            int thorns = maxArmorLevel(defender, CustomEnchantments.THORNS_PLUS);
            if (thorns > 0 && source instanceof EntityLivingBase && source != defender
                    && RANDOM.nextFloat() < .15F * thorns)
                ((EntityLivingBase) source).attackEntityFrom(DamageSource.causeThornsDamage(defender), thorns * 2.0F);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event == null || event.phase != TickEvent.Phase.END || event.player == null
                || event.player.worldObj.isRemote || !CustomEnchantmentAccess.isAvailable()) return;
        EntityPlayer player = event.player;
        if (player.ticksExisted % 20 == 0) {
            int regen = maxArmorLevel(player, CustomEnchantments.REJUVENATION);
            if (regen > 0 && player.getHealth() < player.getMaxHealth())
                player.heal(regen >= 5 ? 1.0F : regen * .2F);
        }
        applyPotion(player, Potion.moveSpeed, level(player.getCurrentArmor(0), CustomEnchantments.SPEED), 40);
        applyPotion(player, Potion.nightVision, level(player.getCurrentArmor(3), CustomEnchantments.NIGHT_VISION), 260);
        applyPotion(player, Potion.digSpeed, level(player.getHeldItem(), CustomEnchantments.HASTE), 40);
    }

    @SubscribeEvent
    public void onPlayerDrops(PlayerDropsEvent event) {
        if (event == null || event.entityPlayer == null || !CustomEnchantmentAccess.isAvailable()) return;
        NBTTagCompound saved = new NBTTagCompound();
        int count = 0;
        for (int i = event.drops.size() - 1; i >= 0; i--) {
            net.minecraft.entity.item.EntityItem drop = event.drops.get(i);
            if (drop == null || drop.getEntityItem() == null
                    || level(drop.getEntityItem(), CustomEnchantments.SOULBOUND) <= 0) continue;
            NBTTagCompound item = new NBTTagCompound();
            drop.getEntityItem().writeToNBT(item);
            saved.setTag("Item" + count++, item);
            drop.setDead();
            event.drops.remove(i);
        }
        if (count > 0) {
            saved.setInteger("Count", count);
            event.entityPlayer.getEntityData().setTag(SOULBOUND_KEY, saved);
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        if (event == null || event.original == null || event.entityPlayer == null
                || !event.wasDeath || !CustomEnchantmentAccess.isAvailable()) return;
        NBTTagCompound data = event.original.getEntityData();
        if (!data.hasKey(SOULBOUND_KEY)) return;
        NBTTagCompound saved = data.getCompoundTag(SOULBOUND_KEY);
        for (int i = 0; i < saved.getInteger("Count"); i++) {
            ItemStack stack = ItemStack.loadItemStackFromNBT(saved.getCompoundTag("Item" + i));
            if (stack != null && !event.entityPlayer.inventory.addItemStackToInventory(stack))
                event.entityPlayer.entityDropItem(stack, 0.0F);
        }
        data.removeTag(SOULBOUND_KEY);
    }


    @SubscribeEvent
    public void onHarvestDrops(BlockEvent.HarvestDropsEvent event) {
        if (event == null || event.harvester == null || event.drops == null
                || !CustomEnchantmentAccess.isAvailable() || event.world.isRemote) return;
        EntityPlayer player = event.harvester;
        ItemStack tool = player.getHeldItem();
        int autoSmelt = level(tool, CustomEnchantments.AUTO_SMELT);
        int telepathy = level(tool, CustomEnchantments.TELEPATHY);
        if (autoSmelt <= 0 && telepathy <= 0) return;

        if (autoSmelt > 0 && !event.isSilkTouching) {
            for (int i = 0; i < event.drops.size(); i++) {
                ItemStack drop = event.drops.get(i);
                if (drop == null) continue;
                ItemStack smelted = FurnaceRecipes.instance().getSmeltingResult(drop);
                if (smelted == null) continue;
                ItemStack replacement = smelted.copy();
                replacement.stackSize = smelted.stackSize * drop.stackSize;
                event.drops.set(i, replacement);
            }
        }

        if (telepathy > 0) {
            for (int i = event.drops.size() - 1; i >= 0; i--) {
                ItemStack drop = event.drops.get(i);
                if (drop == null) { event.drops.remove(i); continue; }
                ItemStack remainder = drop.copy();
                player.inventory.addItemStackToInventory(remainder);
                if (remainder.stackSize <= 0) event.drops.remove(i);
                else event.drops.set(i, remainder);
            }
        }
    }

    @SubscribeEvent
    public void attractDroppedItems(TickEvent.PlayerTickEvent event) {
        if (event == null || event.phase != TickEvent.Phase.END || event.player == null
                || event.player.worldObj.isRemote || !CustomEnchantmentAccess.isAvailable()) return;
        EntityPlayer player = event.player;
        int magnetic = level(player.getHeldItem(), CustomEnchantments.MAGNETIC);
        int magnet = level(player.getCurrentArmor(0), CustomEnchantments.MAGNET);
        int autoPickup = level(player.getCurrentArmor(0), CustomEnchantments.AUTO_PICKUP);
        int range = Math.max(magnetic > 0 ? magnetic * 4 : 0,
                Math.max(magnet > 0 ? magnet * 4 : 0, autoPickup > 0 ? autoPickup * 4 : 0));
        if (range <= 0) return;
        java.util.List<EntityItem> items = player.worldObj.getEntitiesWithinAABB(EntityItem.class,
                player.getEntityBoundingBox().expand(range, range, range));
        for (EntityItem item : items) {
            if (item == null || item.isDead) continue;
            double dx = player.posX - item.posX;
            double dy = player.posY + player.getEyeHeight() * .5D - item.posY;
            double dz = player.posZ - item.posZ;
            double distanceSq = dx * dx + dy * dy + dz * dz;
            if (distanceSq < .25D) continue;
            double scale = .08D / Math.max(.5D, Math.sqrt(distanceSq));
            item.motionX += dx * scale;
            item.motionY += dy * scale;
            item.motionZ += dz * scale;
        }
    }

    private static int level(ItemStack stack, net.minecraft.enchantment.Enchantment enchantment) {
        return stack == null || enchantment == null ? 0
                : EnchantmentHelper.getEnchantmentLevel(enchantment.effectId, stack);
    }
    private static int maxArmorLevel(EntityPlayer player, net.minecraft.enchantment.Enchantment enchantment) {
        int max = 0;
        for (int i = 0; i < 4; i++) max = Math.max(max, level(player.getCurrentArmor(i), enchantment));
        return max;
    }
    private static void applyPotion(EntityPlayer player, Potion potion, int level, int duration) {
        if (level > 0 && potion != null)
            player.addPotionEffect(new PotionEffect(potion.id, duration, Math.min(2, level - 1), true, false));
    }
}