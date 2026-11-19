package com.alpharip.staffmod.singleplayer.customenchantments;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Runtime effects for custom enchantments. All handlers are gated to an
 * integrated Singleplayer world; multiplayer state is never modified.
 */
public final class CustomEnchantmentHandler {
    private static final Random RANDOM = new Random();
    private static final String SOULBOUND_KEY = "AlphaClientSoulboundItems";

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (event == null || event.entityLiving == null || !CustomEnchantmentAccess.isAvailable()) return;
        Entity sourceEntity = event.source == null ? null : event.source.getEntity();

        if (sourceEntity instanceof EntityPlayer) {
            EntityPlayer attacker = (EntityPlayer) sourceEntity;
            NBTTagCompound attackerData = attacker.getEntityData();
            if (attackerData.getBoolean("AlphaDoubleStrikeFollowup")) {
                attackerData.removeTag("AlphaDoubleStrikeFollowup");
                event.amount *= 0.75F;
                return;
            }

            ItemStack weapon = attacker.getHeldItem();
            int lifesteal = level(weapon, CustomEnchantments.LIFESTEAL);
            if (lifesteal > 0) {
                float[] heal = {0F, .10F, .15F, .20F, .25F, .35F};
                attacker.heal(event.amount * heal[Math.min(lifesteal, 5)]);
            }

            int execute = level(weapon, CustomEnchantments.EXECUTE);
            if (execute > 0 && event.entityLiving.getHealth() <= event.entityLiving.getMaxHealth() * .30F) {
                event.amount *= 1.0F + (0.20F * execute);
            }

            int venom = level(weapon, CustomEnchantments.VENOM);
            if (venom > 0 && event.entityLiving.isEntityAlive()) {
                event.entityLiving.addPotionEffect(new PotionEffect(Potion.poison.id,
                        40 + venom * 30, Math.max(0, (venom - 3) / 2)));
            }

            int rage = level(weapon, CustomEnchantments.RAGE);
            if (rage > 0) {
                long now = attacker.worldObj.getTotalWorldTime();
                NBTTagCompound data = attacker.getEntityData();
                long previous = data.getLong("AlphaRageLastHit");
                int stacks = previous > 0 && now - previous <= 5L * 20L
                        ? data.getInteger("AlphaRageStacks") : 0;
                stacks = Math.min(10, stacks + 1);
                data.setInteger("AlphaRageStacks", stacks);
                data.setLong("AlphaRageLastHit", now);
                event.amount *= 1.0F + Math.min(1.0F, stacks * .05F * rage / 5.0F);
            }

            int doubleStrike = level(weapon, CustomEnchantments.DOUBLE_STRIKE);
            if (doubleStrike > 0 && RANDOM.nextFloat() < .15F * doubleStrike
                    && event.entityLiving.isEntityAlive()) {
                attackerData.setBoolean("AlphaDoubleStrikeFollowup", true);
                attacker.attackTargetEntityWithCurrentItem(event.entityLiving);
                attackerData.removeTag("AlphaDoubleStrikeFollowup");
            }
        }

        if (event.entityLiving instanceof EntityPlayer) {
            EntityPlayer defender = (EntityPlayer) event.entityLiving;
            int dodge = maxArmorLevel(defender, CustomEnchantments.DODGE);
            if (dodge > 0 && RANDOM.nextFloat() < .08F * dodge) {
                event.setCanceled(true);
                return;
            }

            int thornsPlus = maxArmorLevel(defender, CustomEnchantments.THORNS_PLUS);
            if (thornsPlus > 0 && sourceEntity instanceof EntityLivingBase
                    && sourceEntity != defender && RANDOM.nextFloat() < .15F * thornsPlus) {
                ((EntityLivingBase) sourceEntity).attackEntityFrom(
                        DamageSource.causeThornsDamage(defender), thornsPlus * 2.0F);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event == null || event.phase != TickEvent.Phase.END || event.player == null
                || !CustomEnchantmentAccess.isAvailable() || event.player.worldObj.isRemote) return;
        EntityPlayer player = event.player;

        if (player.ticksExisted % 20 == 0) {
            int rejuvenation = maxArmorLevel(player, CustomEnchantments.REJUVENATION);
            if (rejuvenation > 0 && player.getHealth() < player.getMaxHealth()) {
                player.heal(rejuvenation >= 5 ? 1.0F : rejuvenation * .2F);
            }
        }

        ItemStack boots = player.getCurrentArmor(0);
        int speed = level(boots, CustomEnchantments.SPEED);
        applyPotion(player, Potion.moveSpeed, speed, 40);

        ItemStack helmet = player.getCurrentArmor(3);
        int nightVision = level(helmet, CustomEnchantments.NIGHT_VISION);
        applyPotion(player, Potion.nightVision, nightVision, 260);

        int haste = level(player.getHeldItem(), CustomEnchantments.HASTE);
        applyPotion(player, Potion.digSpeed, haste, 40);
    }

    @SubscribeEvent
    public void onPlayerDrops(PlayerDropsEvent event) {
        if (event == null || event.entityPlayer == null || !CustomEnchantmentAccess.isAvailable()) return;
        EntityPlayer player = event.entityPlayer;
        NBTTagCompound saved = new NBTTagCompound();
        int index = 0;

        for (int i = event.drops.size() - 1; i >= 0; i--) {
            net.minecraft.entity.item.EntityItem drop = event.drops.get(i);
            if (drop == null || drop.getEntityItem() == null
                    || level(drop.getEntityItem(), CustomEnchantments.SOULBOUND) <= 0) continue;
            NBTTagCompound itemData = new NBTTagCompound();
            drop.getEntityItem().writeToNBT(itemData);
            saved.setTag("Item" + index++, itemData);
            drop.setDead();
            event.drops.remove(i);
        }

        if (index > 0) {
            saved.setInteger("Count", index);
            player.getEntityData().setTag(SOULBOUND_KEY, saved);
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        if (event == null || event.original == null || event.entityPlayer == null
                || !CustomEnchantmentAccess.isAvailable() || event.wasDeath == false) return;
        NBTTagCompound originalData = event.original.getEntityData();
        if (!originalData.hasKey(SOULBOUND_KEY)) return;
        NBTTagCompound saved = originalData.getCompoundTag(SOULBOUND_KEY);
        int count = saved.getInteger("Count");
        for (int i = 0; i < count; i++) {
            NBTTagCompound itemData = saved.getCompoundTag("Item" + i);
            ItemStack stack = ItemStack.loadItemStackFromNBT(itemData);
            if (stack == null) continue;
            if (!event.entityPlayer.inventory.addItemStackToInventory(stack)) {
                event.entityPlayer.entityDropItem(stack, 0.0F);
            }
        }
        originalData.removeTag(SOULBOUND_KEY);
    }

    private static int level(ItemStack stack, CustomEnchantment enchantment) {
        return stack == null || enchantment == null ? 0
                : EnchantmentHelper.getEnchantmentLevel(enchantment.effectId, stack);
    }

    private static int maxArmorLevel(EntityPlayer player, CustomEnchantment enchantment) {
        int maximum = 0;
        for (int slot = 0; slot < 4; slot++) {
            maximum = Math.max(maximum, level(player.getCurrentArmor(slot), enchantment));
        }
        return maximum;
    }

    private static void applyPotion(EntityPlayer player, Potion potion, int level, int duration) {
        if (level <= 0 || potion == null) return;
        int amplifier = Math.max(0, Math.min(level - 1, 2));
        player.addPotionEffect(new PotionEffect(potion.id, duration, amplifier, true, false));
    }
}
