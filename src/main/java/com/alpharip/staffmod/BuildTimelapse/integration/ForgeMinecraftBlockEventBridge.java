package com.alpharip.staffmod.BuildTimelapse.integration;

import com.alpharip.staffmod.BuildTimelapse.analytics.BlockChangeEvent;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import java.util.List;

/** Forge 1.8.9 singleplayer block activity bridge. Server-side integrated-world events only. */
public final class ForgeMinecraftBlockEventBridge implements MinecraftBlockEventBridge {
    private volatile Listener listener;

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void register() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void unregister() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockEvent.PlaceEvent event) {
        if (event == null || event.isCanceled() || event.world == null || event.world.isRemote) return;
        if (event instanceof BlockEvent.MultiPlaceEvent) {
            List<BlockSnapshot> snapshots =
                    ((BlockEvent.MultiPlaceEvent) event).getReplacedBlockSnapshots();
            for (BlockSnapshot snapshot : snapshots) {
                if (snapshot != null) {
                    record(snapshot.pos, snapshot.getReplacedBlock(), snapshot.getCurrentBlock());
                }
            }
            return;
        }
        BlockSnapshot snapshot = event.blockSnapshot;
        if (snapshot != null) {
            record(snapshot.pos, snapshot.getReplacedBlock(), snapshot.getCurrentBlock());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event == null || event.isCanceled() || event.world == null || event.world.isRemote) return;
        record(event.pos, event.state, Blocks.air.getDefaultState());
    }

    private void record(BlockPos pos, IBlockState before, IBlockState after) {
        Listener current = listener;
        if (current == null || pos == null || before == null || after == null) return;
        current.onBlockChanged(new BlockChangeEvent(
                pos.getX(), pos.getY(), pos.getZ(),
                blockId(before.getBlock()), blockId(after.getBlock()),
                System.currentTimeMillis()
        ));
    }

    private static String blockId(Block block) {
        if (block == null) return "air";
        String registryName = block.getRegistryName();
        return registryName == null ? Integer.toString(Block.getIdFromBlock(block)) : registryName;
    }
}
