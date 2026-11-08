package com.alpharip.staffmod.singleplayer.treeexcavate;

import com.alpharip.alphaclient.AlphaKeyBindingSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/** Singleplayer tree excavation triggered by breaking a connected tree log. */
public final class TreeExcavate {
    private final KeyBinding excavateKey = new KeyBinding("AlphaClient: Excavate",
            AlphaKeyBindingSettings.getKeyCode("treeExcavate", Keyboard.KEY_GRAVE), "AlphaClient");
    private boolean keyVisible;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event == null || event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        syncKeyVisibility(mc);
        if (TreeExcavateAccess.isAvailable(mc)) {
            AlphaKeyBindingSettings.saveBindings(new String[]{"treeExcavate"}, new KeyBinding[]{excavateKey});
        }
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!TreeExcavateAccess.isAvailable(mc) || mc.currentScreen != null || !excavateKey.isPressed()) return;

        boolean enabled = !TreeExcavateSettings.isEnabled();
        TreeExcavateSettings.setEnabled(enabled);
        mc.thePlayer.addChatMessage(new ChatComponentText("§6[Tree Excavate] §f"
                + (enabled ? "Enabled." : "Disabled.")));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event == null || event.isCanceled() || event.world == null || event.world.isRemote
                || !(event.getPlayer() instanceof EntityPlayerMP)) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (!TreeExcavateAccess.isAvailable(mc) || !TreeExcavateSettings.isEnabled()) return;

        EntityPlayer player = event.getPlayer();
        World world = event.world;
        BlockPos origin = event.pos;
        if (origin == null || !world.isBlockLoaded(origin)) return;
        if (!world.getBlockState(origin).getBlock().isWood(world, origin)) return;

        List<BlockPos> connectedLogs = collectConnectedLogs(world, origin, TreeExcavateSettings.getMaxLogs());
        if (connectedLogs.size() <= 1 || !hasNearbyLeaves(world, connectedLogs)) return;

        boolean dropItems = !player.capabilities.isCreativeMode;
        for (BlockPos log : connectedLogs) {
            if (log.equals(origin) || !world.isBlockLoaded(log)) continue;
            world.destroyBlock(log, dropItems);
        }
    }

    private void syncKeyVisibility(Minecraft mc) {
        if (mc == null || mc.gameSettings == null || mc.gameSettings.keyBindings == null) return;
        boolean shouldBeVisible = TreeExcavateAccess.isAvailable(mc);
        if (shouldBeVisible == keyVisible) return;

        KeyBinding[] current = mc.gameSettings.keyBindings;
        List<KeyBinding> bindings = new ArrayList<KeyBinding>();
        boolean found = false;
        for (KeyBinding binding : current) {
            if (binding == excavateKey) {
                found = true;
                if (!shouldBeVisible) continue;
            }
            bindings.add(binding);
        }
        if (shouldBeVisible && !found) bindings.add(excavateKey);
        mc.gameSettings.keyBindings = bindings.toArray(new KeyBinding[bindings.size()]);
        KeyBinding.resetKeyBindingArrayAndHash();
        keyVisible = shouldBeVisible;
    }

    private static List<BlockPos> collectConnectedLogs(World world, BlockPos origin, int limit) {
        List<BlockPos> logs = new ArrayList<BlockPos>();
        Queue<BlockPos> queue = new ArrayDeque<BlockPos>();
        Set<BlockPos> visited = new HashSet<BlockPos>();
        queue.add(origin);
        visited.add(origin);

        while (!queue.isEmpty() && logs.size() < limit) {
            BlockPos current = queue.remove();
            if (!world.isBlockLoaded(current)) continue;
            if (!world.getBlockState(current).getBlock().isWood(world, current)) continue;
            logs.add(current);

            addNeighbor(world, current.add(1, 0, 0), origin, queue, visited);
            addNeighbor(world, current.add(-1, 0, 0), origin, queue, visited);
            addNeighbor(world, current.add(0, 1, 0), origin, queue, visited);
            addNeighbor(world, current.add(0, -1, 0), origin, queue, visited);
            addNeighbor(world, current.add(0, 0, 1), origin, queue, visited);
            addNeighbor(world, current.add(0, 0, -1), origin, queue, visited);
        }
        return logs;
    }

    private static void addNeighbor(World world, BlockPos next, BlockPos origin,
                                    Queue<BlockPos> queue, Set<BlockPos> visited) {
        if (visited.contains(next) || Math.abs(next.getX() - origin.getX()) > 8
                || Math.abs(next.getZ() - origin.getZ()) > 8
                || next.getY() < origin.getY() - 2 || next.getY() > origin.getY() + 20
                || !world.isBlockLoaded(next)) return;
        if (world.getBlockState(next).getBlock().isWood(world, next)) {
            visited.add(next);
            queue.add(next);
        }
    }

    private static boolean hasNearbyLeaves(World world, List<BlockPos> logs) {
        for (BlockPos log : logs) {
            for (int x = -2; x <= 2; x++) {
                for (int y = -2; y <= 2; y++) {
                    for (int z = -2; z <= 2; z++) {
                        BlockPos nearby = log.add(x, y, z);
                        if (!world.isBlockLoaded(nearby)) continue;
                        if (world.getBlockState(nearby).getBlock().isLeaves(world, nearby)) return true;
                    }
                }
            }
        }
        return false;
    }
}
