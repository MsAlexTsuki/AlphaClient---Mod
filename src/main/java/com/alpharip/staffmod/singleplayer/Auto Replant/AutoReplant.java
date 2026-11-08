package com.alpharip.staffmod.singleplayer.autoreplant;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockCocoa;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockNetherWart;
import net.minecraft.block.BlockStem;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Replants harvested vanilla Minecraft 1.8.9 crops in an active singleplayer world.
 * PixelGarden and other modded blocks are deliberately excluded.
 */
public final class AutoReplant {
    private final Map<World, List<PendingReplant>> pending = new IdentityHashMap<World, List<PendingReplant>>();

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event == null || event.phase != TickEvent.Phase.END) return;
        AutoReplantSettings.loadForCurrentWorld(Minecraft.getMinecraft());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event == null || event.isCanceled() || event.world == null || event.world.isRemote
                || !(event.getPlayer() instanceof EntityPlayerMP)
                || !AutoReplantSettings.isEnabled()) return;

        EntityPlayerMP player = (EntityPlayerMP) event.getPlayer();
        if (player.capabilities.isCreativeMode || player.isDead) return;

        CropPlan plan = getCropPlan(event.state);
        if (plan == null) return;

        synchronized (pending) {
            List<PendingReplant> worldQueue = pending.get(event.world);
            if (worldQueue == null) {
                worldQueue = new ArrayList<PendingReplant>();
                pending.put(event.world, worldQueue);
            }
            worldQueue.add(new PendingReplant(event.pos, player, plan));
        }
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event == null || event.phase != TickEvent.Phase.END || event.world == null || event.world.isRemote) return;

        List<PendingReplant> work;
        synchronized (pending) {
            work = pending.remove(event.world);
        }
        if (work == null || work.isEmpty()) return;

        for (PendingReplant task : work) {
            replant(event.world, task);
        }
    }

    private static void replant(World world, PendingReplant task) {
        if (world == null || task == null || task.player == null || task.player.isDead
                || task.player.worldObj != world || !world.isBlockLoaded(task.position)
                || !world.isAirBlock(task.position)) return;

        if (task.plan.block instanceof BlockBush) {
            if (!((BlockBush) task.plan.block).canBlockStay(world, task.position, task.plan.plantedState)) return;
        } else if (task.plan.block instanceof BlockCocoa) {
            if (!task.plan.block.canPlaceBlockAt(world, task.position)) return;
        }

        EntityItem harvestedSeed = findNearbyDrop(world, task.position,
                task.plan.seed, task.plan.seedMetadata);
        ItemStack inventorySeed = harvestedSeed == null
                ? findSeed(task.player, task.plan.seed, task.plan.seedMetadata) : null;
        if (harvestedSeed == null && inventorySeed == null) return;
        if (!world.setBlockState(task.position, task.plan.plantedState, 3)) return;

        if (harvestedSeed != null) {
            ItemStack dropStack = harvestedSeed.getEntityItem();
            if (dropStack != null) {
                dropStack.stackSize--;
                if (dropStack.stackSize <= 0) harvestedSeed.setDead();
            }
        } else if (inventorySeed != null) {
            inventorySeed.stackSize--;
            if (inventorySeed.stackSize <= 0) {
                for (int slot = 0; slot < 36; slot++) {
                    if (task.player.inventory.getStackInSlot(slot) == inventorySeed) {
                        task.player.inventory.setInventorySlotContents(slot, null);
                        break;
                    }
                }
            }
            if (task.player.inventoryContainer != null) {
                task.player.inventoryContainer.detectAndSendChanges();
            }
        }
    }

    private static EntityItem findNearbyDrop(World world, BlockPos position, Item item, int metadata) {
        if (world == null || position == null || item == null) return null;
        AxisAlignedBB searchArea = new AxisAlignedBB(
                position.getX() - 0.25D, position.getY() - 0.25D, position.getZ() - 0.25D,
                position.getX() + 1.25D, position.getY() + 1.25D, position.getZ() + 1.25D);
        List<EntityItem> nearbyDrops = world.getEntitiesWithinAABB(EntityItem.class, searchArea);
        for (EntityItem entity : nearbyDrops) {
            if (entity == null || entity.isDead || entity.ticksExisted > 2) continue;
            ItemStack stack = entity.getEntityItem();
            if (stack != null && stack.stackSize > 0 && stack.getItem() == item
                    && stack.getItemDamage() == metadata) return entity;
        }
        return null;
    }

    private static ItemStack findSeed(EntityPlayer player, Item item, int metadata) {
        if (player == null || item == null) return null;
        for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = player.inventory.getStackInSlot(slot);
            if (stack != null && stack.stackSize > 0 && stack.getItem() == item
                    && stack.getItemDamage() == metadata) return stack;
        }
        return null;
    }

    private static CropPlan getCropPlan(IBlockState state) {
        if (state == null || state.getBlock() == null) return null;
        Block block = state.getBlock();

        if (block == Blocks.wheat && state.getValue(BlockCrops.AGE) == 7) {
            return new CropPlan(Blocks.wheat, Blocks.wheat.getDefaultState(),
                    Items.wheat_seeds, 0);
        }
        if (block == Blocks.carrots && state.getValue(BlockCrops.AGE) == 7) {
            return new CropPlan(Blocks.carrots, Blocks.carrots.getDefaultState(),
                    Items.carrot, 0);
        }
        if (block == Blocks.potatoes && state.getValue(BlockCrops.AGE) == 7) {
            return new CropPlan(Blocks.potatoes, Blocks.potatoes.getDefaultState(),
                    Items.potato, 0);
        }
        if (block == Blocks.nether_wart && state.getValue(BlockNetherWart.AGE) == 3) {
            return new CropPlan(Blocks.nether_wart, Blocks.nether_wart.getDefaultState(),
                    Items.nether_wart, 0);
        }
        if (block == Blocks.cocoa && state.getValue(BlockCocoa.AGE) == 2) {
            IBlockState planted = state.withProperty(BlockCocoa.AGE, Integer.valueOf(0));
            return new CropPlan(Blocks.cocoa, planted, Items.dye, 3);
        }
        if (block == Blocks.pumpkin_stem && state.getValue(BlockStem.AGE) == 7) {
            return new CropPlan(Blocks.pumpkin_stem, Blocks.pumpkin_stem.getDefaultState(),
                    Items.pumpkin_seeds, 0);
        }
        if (block == Blocks.melon_stem && state.getValue(BlockStem.AGE) == 7) {
            return new CropPlan(Blocks.melon_stem, Blocks.melon_stem.getDefaultState(),
                    Items.melon_seeds, 0);
        }
        return null;
    }

    private static final class CropPlan {
        private final Block block;
        private final IBlockState plantedState;
        private final Item seed;
        private final int seedMetadata;

        private CropPlan(Block block, IBlockState plantedState, Item seed, int seedMetadata) {
            this.block = block;
            this.plantedState = plantedState;
            this.seed = seed;
            this.seedMetadata = seedMetadata;
        }
    }

    private static final class PendingReplant {
        private final BlockPos position;
        private final EntityPlayerMP player;
        private final CropPlan plan;

        private PendingReplant(BlockPos position, EntityPlayerMP player, CropPlan plan) {
            this.position = position;
            this.player = player;
            this.plan = plan;
        }
    }
}
