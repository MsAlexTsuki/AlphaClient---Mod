package com.alpharip.staffmod.LootFeed;

import net.minecraftforge.common.MinecraftForge;

/**
 * Client-side entry point for the Loot Feed prototype.
 *
 * Call register() from the client's initialization sequence to enable it.
 */
public final class LootFeed {
    private final LootFeedTracker tracker = new LootFeedTracker();
    private final LootFeedHud hud = new LootFeedHud(tracker);

    public void register() {
        MinecraftForge.EVENT_BUS.register(tracker);
        MinecraftForge.EVENT_BUS.register(hud);
    }
}
