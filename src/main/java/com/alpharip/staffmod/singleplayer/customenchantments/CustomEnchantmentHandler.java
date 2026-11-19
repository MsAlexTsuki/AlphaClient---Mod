package com.alpharip.staffmod.singleplayer.customenchantments;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockSand;
import net.minecraft.block.BlockGravel;
import net.minecraft.block.BlockClay;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.BlockOre;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.entity.item.EntityItem;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerPickupXpEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import java.util.Random;

/** Singleplayer-only runtime effects for the first combat, armour and soulbound enchantments. */
public final class CustomEnchantmentHandler {
    private static final Random RANDOM = new Random();
    private static final String SOULBOUND_KEY = "AlphaClientSoulboundItems";
    private static final ThreadLocal<Boolean> BULK_BREAK = new ThreadLocal<Boolean>() {
        @Override protected Boolean initialValue() { return Boolean.FALSE; }
    };

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (event == null || event.entityLiving == null || event.entityLiving.worldObj.isRemote || !CustomEnchantmentAccess.isAvailable()) return;
        Entity source = event.source == null ? null : event.source.getEntity();

        if (source instanceof EntityPlayer && event.source != null && event.source.isProjectile()) {
            Entity projectile = event.source.getSourceOfDamage();
            if (projectile instanceof EntityArrow) {
                NBTTagCompound arrowData = projectile.getEntityData();
                int venomShot = arrowData.getInteger("AlphaVenomShot");
                if (venomShot > 0)
                    event.entityLiving.addPotionEffect(new PotionEffect(Potion.poison.id,
                            40 + venomShot * 30, Math.max(0, (venomShot - 3) / 2)));
                int flame = arrowData.getInteger("AlphaFlame");
                if (flame > 0 && !(event.entityLiving instanceof net.minecraft.entity.monster.EntityEnderman))
                    event.entityLiving.setFire(5 * flame);
                int explosive = arrowData.getInteger("AlphaExplosive");
                if (explosive > 0 && !arrowData.getBoolean("AlphaExplosiveTriggered")) {
                    arrowData.setBoolean("AlphaExplosiveTriggered", true);
                    float radius = 1.0F + explosive * .5F;
                    event.entityLiving.worldObj.newExplosion((Entity) source, event.entityLiving.posX,
                            event.entityLiving.posY, event.entityLiving.posZ, radius, false, false);
                }
            }
        }

        if (source instanceof EntityPlayer && (event.source == null || !event.source.isProjectile())) {
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
            if (dodge > 0) {
                NBTTagCompound dodgeData = defender.getEntityData();
                long now = defender.worldObj.getTotalWorldTime();
                long lastDodge = dodgeData.getLong("AlphaDodgeLast");
                if (now - lastDodge >= 40L && RANDOM.nextFloat() < .08F * dodge) {
                    dodgeData.setLong("AlphaDodgeLast", now);
                    event.setCanceled(true);
                    return;
                }
            }
            int thorns = maxArmorLevel(defender, CustomEnchantments.THORNS_PLUS);
            boolean reflectedDamage = source instanceof EntityLivingBase
                    && source.getEntityData().getBoolean("AlphaThornsPlusProc");
            if (thorns > 0 && source instanceof EntityLivingBase && source != defender
                    && !reflectedDamage && RANDOM.nextFloat() < .15F * thorns) {
                NBTTagCompound defenderData = defender.getEntityData();
                defenderData.setBoolean("AlphaThornsPlusProc", true);
                ((EntityLivingBase) source).attackEntityFrom(
                        DamageSource.causeThornsDamage(defender), thorns * 2.0F);
                defenderData.removeTag("AlphaThornsPlusProc");
            }
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
    public void onPickupExperience(PlayerPickupXpEvent event) {
        if (event == null || event.entityPlayer == null || event.orb == null
                || !CustomEnchantmentAccess.isAvailable()) return;
        int level = level(event.entityPlayer.getHeldItem(), CustomEnchantments.EXPERIENCE);
        if (level > 0) {
            NBTTagCompound data = event.entityPlayer.getEntityData();
            float remainder = data.getFloat("AlphaExperienceRemainder")
                    + event.orb.xpValue * (level * .20F);
            int bonus = (int) remainder;
            data.setFloat("AlphaExperienceRemainder", remainder - bonus);
            event.orb.xpValue += bonus;
        }
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
        Minecraft minecraft = Minecraft.getMinecraft();
        if (event == null || event.original == null || event.entityPlayer == null
                || !event.wasDeath || minecraft == null || !minecraft.isSingleplayer()) return;
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
    public void onArrowLoose(ArrowLooseEvent event) {
        if (event == null || event.entityPlayer == null || event.bow == null
                || event.entityPlayer.worldObj.isRemote || !CustomEnchantmentAccess.isAvailable()) return;
        int level = level(event.bow, CustomEnchantments.MULTISHOT);
        if (level <= 0) return;
        float charge = event.charge / 20.0F;
        charge = (charge * charge + charge * 2.0F) / 3.0F;
        if (charge < .1F) return;
        float velocity = Math.min(charge, 1.0F) * 2.0F;
        int totalArrows = level == 1 ? 3 : level == 2 ? 5 : 7;
        float spread = .12F;
        for (int i = 0; i < totalArrows - 1; i++) {
            EntityArrow extra = new EntityArrow(event.entityPlayer.worldObj, event.entityPlayer, velocity);
            extra.canBePickedUp = 0;
            int power = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, event.bow);
            if (power > 0) extra.setDamage(extra.getDamage() + power * .5D + .5D);
            int punch = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, event.bow);
            if (punch > 0) extra.setKnockbackStrength(punch);
            int flame = EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, event.bow);
            if (flame > 0) extra.setFire(100);
            if (charge >= 1.0F) extra.setIsCritical(true);
            double offset = (i - (totalArrows - 2) / 2.0D) * spread;
            float yaw = event.entityPlayer.rotationYaw * (float)Math.PI / 180.0F;
            extra.motionX += -Math.cos(yaw) * offset;
            extra.motionZ += -Math.sin(yaw) * offset;
            event.entityPlayer.worldObj.spawnEntityInWorld(extra);
        }
    }

    @SubscribeEvent
    public void onArrowSpawn(EntityJoinWorldEvent event) {
        if (event == null || !CustomEnchantmentAccess.isAvailable()) return;
        if (event.entity instanceof EntityItem) {
            if (event.world.isRemote) return;
            EntityItem item = (EntityItem) event.entity;
            ItemStack stack = item.getEntityItem();
            if (stack != null && stack.hasTagCompound()
                    && stack.getTagCompound().getBoolean("AlphaMagneticDrop")) {
                item.getEntityData().setBoolean("AlphaMagneticDrop", true);
                stack.getTagCompound().removeTag("AlphaMagneticDrop");
            }
            return;
        }
        if (!(event.entity instanceof EntityArrow)) return;
        EntityArrow arrow = (EntityArrow) event.entity;
        if (!(arrow.shootingEntity instanceof EntityPlayer)) return;
        EntityPlayer shooter = (EntityPlayer) arrow.shootingEntity;
        ItemStack bow = shooter.getHeldItem();
        if (bow == null) return;
        NBTTagCompound data = arrow.getEntityData();
        data.setInteger("AlphaVenomShot", level(bow, CustomEnchantments.VENOM_SHOT));
        data.setInteger("AlphaExplosive", level(bow, CustomEnchantments.EXPLOSIVE));
        data.setInteger("AlphaPiercing", level(bow, CustomEnchantments.PIERCING));
        data.setInteger("AlphaFlame", EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, bow));
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event == null || event.isCanceled() || event.world == null || event.world.isRemote
                || BULK_BREAK.get() || !CustomEnchantmentAccess.isAvailable()) return;
        EntityPlayer player = event.getPlayer();
        if (player == null || player.capabilities.isCreativeMode) return;
        ItemStack tool = player.getHeldItem();
        if (tool == null || !(tool.getItem() instanceof ItemTool)) return;

        int experience = level(tool, CustomEnchantments.EXPERIENCE);
        if (experience > 0 && event.state.getBlock() instanceof BlockOre)
            event.setExpToDrop(event.getExpToDrop() + experience);

        int vein = level(tool, CustomEnchantments.VEIN_MINER);
        int tree = level(tool, CustomEnchantments.TREE_FELLER);
        int excavator = level(tool, CustomEnchantments.EXCAVATOR);
        BlockPos origin = event.pos;
        Block block = event.state.getBlock();
        if (vein > 0 && block instanceof BlockOre) {
            int limit = vein == 1 ? 4 : vein == 2 ? 8 : 16;
            breakConnected(event.world, player, tool, origin, block,
                    block.getMetaFromState(event.state), limit, false);
        } else if (tree > 0 && block.isWood(event.world, origin)) {
            int limit = tree == 1 ? 15 : tree == 2 ? 31 : 63;
            breakConnected(event.world, player, tool, origin, block, -1, limit, true);
        } else if (excavator > 0 && isExcavatorBlock(block, event.world, origin)) {
            int width = excavator == 1 ? 3 : excavator == 2 ? 5 : 7;
            breakExcavatorPlane(event.world, player, tool, origin, width);
        }
    }

    private static void breakConnected(World world, EntityPlayer player, ItemStack tool,
                                       BlockPos origin, Block originBlock, int metadata,
                                       int limit, boolean wood) {
        BULK_BREAK.set(Boolean.TRUE);
        try {
            Queue<BlockPos> queue = new ArrayDeque<BlockPos>();
            Set<BlockPos> visited = new HashSet<BlockPos>();
            queue.add(origin);
            visited.add(origin);
            int broken = 0;
            while (!queue.isEmpty() && broken < limit) {
                BlockPos current = queue.remove();
                if (current.equals(origin) || !world.isBlockLoaded(current)) continue;
                net.minecraft.block.state.IBlockState state = world.getBlockState(current);
                Block block = state.getBlock();
                boolean matches = wood ? block.isWood(world, current)
                        : block == originBlock && block.getMetaFromState(state) == metadata;
                if (!matches || world.getTileEntity(current) != null) continue;
                if (world.destroyBlock(current, true)) {
                    damageTool(world, player, tool, block, current);
                    broken++;
                    addNeighbors(queue, visited, current);
                }
            }
        } finally {
            BULK_BREAK.set(Boolean.FALSE);
        }
    }

    private static void breakExcavatorPlane(World world, EntityPlayer player, ItemStack tool,
                                             BlockPos origin, int width) {
        BULK_BREAK.set(Boolean.TRUE);
        try {
            EnumFacing facing = player.getHorizontalFacing();
            int half = width / 2;
            for (int a = -half; a <= half; a++) {
                for (int b = -half; b <= half; b++) {
                    if (a == 0 && b == 0) continue;
                    BlockPos pos = facing.getAxis() == EnumFacing.Axis.Z
                            ? origin.add(a, b, 0) : origin.add(0, b, a);
                    if (!world.isBlockLoaded(pos) || world.getTileEntity(pos) != null) continue;
                    Block block = world.getBlockState(pos).getBlock();
                    if (!isExcavatorBlock(block, world, pos)) continue;
                    if (world.destroyBlock(pos, true)) damageTool(world, player, tool, block, pos);
                }
            }
        } finally {
            BULK_BREAK.set(Boolean.FALSE);
        }
    }

    private static boolean isExcavatorBlock(Block block, World world, BlockPos pos) {
        return block != null && world.getTileEntity(pos) == null
                && !(block instanceof BlockOre) && !(block instanceof BlockContainer)
                && (block instanceof BlockDirt || block instanceof BlockSand
                || block instanceof BlockGravel || block instanceof BlockClay
                || block instanceof BlockSnow || block instanceof BlockGrass);
    }

    private static void addNeighbors(Queue<BlockPos> queue, Set<BlockPos> visited, BlockPos pos) {
        BlockPos[] neighbors = {pos.add(1,0,0), pos.add(-1,0,0), pos.add(0,1,0),
                pos.add(0,-1,0), pos.add(0,0,1), pos.add(0,0,-1)};
        for (BlockPos next : neighbors) if (visited.add(next)) queue.add(next);
    }

    private static void damageTool(World world, EntityPlayer player, ItemStack tool, Block block, BlockPos pos) {
        if (tool == null || tool.stackSize <= 0 || block.getBlockHardness(world, pos) < 0.0F) return;
        tool.onBlockDestroyed(world, block, pos, player);
        if (tool.stackSize <= 0) player.destroyCurrentEquippedItem();
    }

    @SubscribeEvent
    public void onHarvestDrops(BlockEvent.HarvestDropsEvent event) {
        if (event == null || event.harvester == null || event.drops == null
                || !CustomEnchantmentAccess.isAvailable() || event.world.isRemote) return;
        EntityPlayer player = event.harvester;
        ItemStack tool = player.getHeldItem();
        int autoSmelt = level(tool, CustomEnchantments.AUTO_SMELT);
        int telepathy = level(tool, CustomEnchantments.TELEPATHY);
        int magnetic = level(tool, CustomEnchantments.MAGNETIC);
        if (autoSmelt <= 0 && telepathy <= 0 && magnetic <= 0) return;

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

        if (magnetic > 0) {
            for (ItemStack drop : event.drops) {
                if (drop == null) continue;
                if (!drop.hasTagCompound()) drop.setTagCompound(new NBTTagCompound());
                drop.getTagCompound().setBoolean("AlphaMagneticDrop", true);
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
        int range = Math.max(rangeForLevel(magnetic),
                Math.max(rangeForLevel(magnet), rangeForLevel(autoPickup)));
        if (range <= 0) return;
        java.util.List<EntityItem> items = player.worldObj.getEntitiesWithinAABB(EntityItem.class,
                player.getEntityBoundingBox().expand(range, range, range));
        for (EntityItem item : items) {
            if (item == null || item.isDead) continue;
            if (magnetic > 0 && magnet <= 0 && autoPickup <= 0
                    && !item.getEntityData().getBoolean("AlphaMagneticDrop")) continue;
            double dx = player.posX - item.posX;
            double dy = player.posY + player.getEyeHeight() * .5D - item.posY;
            double dz = player.posZ - item.posZ;
            double distanceSq = dx * dx + dy * dy + dz * dz;
            if (distanceSq < .25D) continue;
            double scale = .08D / Math.max(.5D, Math.sqrt(distanceSq));
            item.motionX += dx * scale;
            item.motionY += dy * scale;
            item.motionZ += dz * scale;
            if (autoPickup > 0 && distanceSq <= 2.25D) item.onCollideWithPlayer(player);
        }
    }

    private static int rangeForLevel(int level) {
        return level <= 0 ? 0 : level == 1 ? 4 : level == 2 ? 8 : 16;
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