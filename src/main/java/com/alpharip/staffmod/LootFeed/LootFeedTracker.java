package com.alpharip.staffmod.LootFeed;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Correlates item entities spawned near a mob killed by the local player.
 *
 * This is a client-side prototype. It uses a short spatial/time window because
 * vanilla 1.8.9 does not provide a direct client-side link from every item
 * entity drop back to the death event that caused it.
 */
public final class LootFeedTracker {
    private static final long DROP_WINDOW_MS = 2500L;
    private static final long DISPLAY_DURATION_MS = 8000L;
    private static final double DROP_RADIUS_SQUARED = 36.0D;

    private final Map<String, LootFeedEntry> entries =
            new LinkedHashMap<String, LootFeedEntry>();
    private final List<PendingKill> pendingKills = new ArrayList<PendingKill>();

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        EntityPlayer localPlayer = minecraft.thePlayer;

        if (localPlayer == null || minecraft.theWorld == null
                || !minecraft.theWorld.isRemote || event.entityLiving == null) {
            return;
        }

        Entity killer = event.source == null ? null : event.source.getEntity();
        if (killer != localPlayer) {
            return;
        }

        long now = System.currentTimeMillis();
        String entityName = event.entityLiving.getDisplayName().getUnformattedText();

        LootFeedEntry entry = entries.get(entityName);
        if (entry == null) {
            entry = new LootFeedEntry(entityName);
            entries.put(entityName, entry);
        }
        entry.addKill(now);

        pendingKills.add(new PendingKill(
                entityName,
                event.entityLiving.posX,
                event.entityLiving.posY,
                event.entityLiving.posZ,
                now
        ));

        prune(now);
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.world == null || !event.world.isRemote
                || !(event.entity instanceof EntityItem)) {
            return;
        }

        long now = System.currentTimeMillis();
        prune(now);

        EntityItem droppedItem = (EntityItem) event.entity;
        PendingKill nearestKill = findNearestKill(droppedItem, now);
        if (nearestKill == null) {
            return;
        }

        ItemStack stack = droppedItem.getEntityItem();
        if (stack == null || stack.stackSize <= 0) {
            return;
        }

        LootFeedEntry entry = entries.get(nearestKill.entityName);
        if (entry != null) {
            entry.addDrop(stack.getDisplayName(), stack.stackSize, now);
        }
    }

    private PendingKill findNearestKill(EntityItem item, long now) {
        PendingKill nearest = null;
        double nearestDistance = DROP_RADIUS_SQUARED;

        for (PendingKill candidate : pendingKills) {
            if (now - candidate.timestamp > DROP_WINDOW_MS) {
                continue;
            }

            double dx = item.posX - candidate.x;
            double dy = item.posY - candidate.y;
            double dz = item.posZ - candidate.z;
            double distance = dx * dx + dy * dy + dz * dz;

            if (distance <= nearestDistance) {
                nearestDistance = distance;
                nearest = candidate;
            }
        }

        return nearest;
    }

    private void prune(long now) {
        Iterator<PendingKill> killIterator = pendingKills.iterator();
        while (killIterator.hasNext()) {
            if (now - killIterator.next().timestamp > DROP_WINDOW_MS) {
                killIterator.remove();
            }
        }

        Iterator<Map.Entry<String, LootFeedEntry>> entryIterator =
                entries.entrySet().iterator();
        while (entryIterator.hasNext()) {
            LootFeedEntry entry = entryIterator.next().getValue();
            if (now - entry.getLastUpdated() > DISPLAY_DURATION_MS) {
                entryIterator.remove();
            }
        }
    }

    public List<LootFeedEntry> getVisibleEntries() {
        prune(System.currentTimeMillis());
        return Collections.unmodifiableList(
                new ArrayList<LootFeedEntry>(entries.values())
        );
    }

    private static final class PendingKill {
        private final String entityName;
        private final double x;
        private final double y;
        private final double z;
        private final long timestamp;

        private PendingKill(String entityName, double x, double y, double z,
                            long timestamp) {
            this.entityName = entityName;
            this.x = x;
            this.y = y;
            this.z = z;
            this.timestamp = timestamp;
        }
    }
}
