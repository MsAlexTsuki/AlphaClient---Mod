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
import java.util.List;

/**
 * Tracks item entities spawned near kills made by the local player.
 *
 * Vanilla 1.8.9 client events do not expose a reliable owner/cause reference
 * for every spawned item entity. Drop matching therefore remains a time and
 * distance based estimate.
 */
public final class LootFeedTracker {
    private static final long DROP_WINDOW_MS = 2500L;
    private static final long DISPLAY_DURATION_MS = 8000L;
    private static final long FADE_DURATION_MS = 350L;
    private static final int MAX_VISIBLE_GROUPS = 4;
    private static final double DROP_RADIUS_SQUARED = 36.0D;

    // Oldest first; newest last.
    private final List<LootFeedEntry> entries = new ArrayList<LootFeedEntry>();
    private final List<PendingKill> pendingKills = new ArrayList<PendingKill>();
    private final List<FadingEntry> fadingEntries = new ArrayList<FadingEntry>();

    // Only consecutive kills of the same non-player entity type share a group.
    private LootFeedEntry lastMobGroup;
    private Class<?> lastMobClass;

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
        Entity killedEntity = event.entityLiving;
        boolean playerKill = killedEntity instanceof EntityPlayer;
        String entityName = killedEntity.getDisplayName().getUnformattedText();

        LootFeedEntry entry;
        if (!playerKill && lastMobGroup != null
                && lastMobClass == killedEntity.getClass()
                && entries.contains(lastMobGroup)) {
            entry = lastMobGroup;
        } else {
            entry = new LootFeedEntry(entityName);
            if (entries.size() >= MAX_VISIBLE_GROUPS) {
                LootFeedEntry oldest = entries.remove(0);
                if (!oldest.getDrops().isEmpty()) {
                    fadingEntries.add(new FadingEntry(oldest, now));
                }
            }
            entries.add(entry);
        }

        entry.addKill(now);

        if (playerKill) {
            // Every player kill is an independent group and breaks mob streaks.
            lastMobGroup = null;
            lastMobClass = null;
        } else {
            lastMobGroup = entry;
            lastMobClass = killedEntity.getClass();
        }

        pendingKills.add(new PendingKill(
                entry,
                killedEntity.posX,
                killedEntity.posY,
                killedEntity.posZ,
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

        // Keep the exact kill-group reference; names are not unique identifiers.
        if (entries.contains(nearestKill.entry)) {
            nearestKill.entry.addDrop(stack.getDisplayName(), stack.stackSize, now);
        }
    }

    private PendingKill findNearestKill(EntityItem item, long now) {
        PendingKill nearest = null;
        double nearestDistance = DROP_RADIUS_SQUARED;

        for (PendingKill candidate : pendingKills) {
            if (now - candidate.timestamp > DROP_WINDOW_MS
                    || !entries.contains(candidate.entry)) {
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

        Iterator<LootFeedEntry> entryIterator = entries.iterator();
        while (entryIterator.hasNext()) {
            LootFeedEntry entry = entryIterator.next();
            if (now - entry.getLastUpdated() > DISPLAY_DURATION_MS) {
                entryIterator.remove();
                if (entry == lastMobGroup) {
                    lastMobGroup = null;
                    lastMobClass = null;
                }
            }
        }

        Iterator<FadingEntry> fadeIterator = fadingEntries.iterator();
        while (fadeIterator.hasNext()) {
            if (now - fadeIterator.next().startedAt >= FADE_DURATION_MS) {
                fadeIterator.remove();
            }
        }
    }

    public List<LootFeedEntry> getVisibleEntries() {
        prune(System.currentTimeMillis());

        List<LootFeedEntry> visible = new ArrayList<LootFeedEntry>();
        for (LootFeedEntry entry : entries) {
            if (!entry.getDrops().isEmpty()) {
                visible.add(entry);
            }
        }

        return Collections.unmodifiableList(visible);
    }

    public List<FadingEntry> getFadingEntries() {
        prune(System.currentTimeMillis());
        return Collections.unmodifiableList(
                new ArrayList<FadingEntry>(fadingEntries));
    }

    public static final class FadingEntry {
        private final LootFeedEntry entry;
        private final long startedAt;

        private FadingEntry(LootFeedEntry entry, long startedAt) {
            this.entry = entry;
            this.startedAt = startedAt;
        }

        public LootFeedEntry getEntry() {
            return entry;
        }

        public int getAlpha() {
            long elapsed = System.currentTimeMillis() - startedAt;
            if (elapsed >= FADE_DURATION_MS) {
                return 0;
            }
            return (int) (255L * (FADE_DURATION_MS - elapsed) / FADE_DURATION_MS);
        }
    }

    private static final class PendingKill {
        private final LootFeedEntry entry;
        private final double x;
        private final double y;
        private final double z;
        private final long timestamp;

        private PendingKill(LootFeedEntry entry, double x, double y, double z,
                            long timestamp) {
            this.entry = entry;
            this.x = x;
            this.y = y;
            this.z = z;
            this.timestamp = timestamp;
        }
    }
}
