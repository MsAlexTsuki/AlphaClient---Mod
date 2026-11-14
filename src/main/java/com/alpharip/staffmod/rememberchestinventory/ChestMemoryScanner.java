package com.alpharip.staffmod.rememberchestinventory;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts an observed chest inventory into a detached snapshot.
 *
 * The Minecraft 1.8.9/Forge event adapter is intentionally not wired yet.
 * A future adapter should read the open chest container and call scan().
 */
public final class ChestMemoryScanner {
    public ChestMemoryEntry scan(int dimensionId, int x, int y, int z, String chestName,
                                 List<ChestItemSnapshot> observedItems, long observedAt) {
        List<ChestItemSnapshot> copy = new ArrayList<ChestItemSnapshot>();
        if (observedItems != null) {
            for (ChestItemSnapshot item : observedItems) {
                if (item != null && item.getCount() > 0) copy.add(item);
            }
        }
        return new ChestMemoryEntry(dimensionId, x, y, z, chestName, observedAt, copy);
    }
}