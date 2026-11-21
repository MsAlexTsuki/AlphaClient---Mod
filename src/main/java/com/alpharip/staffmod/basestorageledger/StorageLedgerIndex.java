package com.alpharip.staffmod.basestorageledger;

import com.alpharip.staffmod.rememberchestinventory.ChestItemSnapshot;
import com.alpharip.staffmod.rememberchestinventory.ChestMemoryEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Builds a cross-chest stock index without scanning containers a second time. */
public final class StorageLedgerIndex {
    private final Map<String, MutableItem> items = new LinkedHashMap<String, MutableItem>();

    public synchronized void rebuild(List<ChestMemoryEntry> snapshots) {
        items.clear();
        if (snapshots == null) return;

        for (ChestMemoryEntry chest : snapshots) {
            if (chest == null) continue;
            Set<String> countedInThisChest = new LinkedHashSet<String>();

            for (ChestItemSnapshot stack : chest.getItems()) {
                if (stack == null || stack.getCount() <= 0) continue;
                String itemId = stack.getItemId() == null ? "" : stack.getItemId();
                String key = itemId + "#" + stack.getMetadata();
                MutableItem item = items.get(key);
                if (item == null) {
                    item = new MutableItem(itemId, stack.getMetadata(), stack.getDisplayName());
                    items.put(key, item);
                }
                item.totalCount += stack.getCount();
                item.lastObservedAt = Math.max(item.lastObservedAt, chest.getObservedAt());
                if (countedInThisChest.add(key)) item.chestCount++;
            }
        }
    }

    public synchronized List<StorageLedgerItem> getItems() {
        List<StorageLedgerItem> result = new ArrayList<StorageLedgerItem>();
        for (MutableItem item : items.values()) {
            result.add(item.toImmutable());
        }
        Collections.sort(result, new Comparator<StorageLedgerItem>() {
            @Override
            public int compare(StorageLedgerItem left, StorageLedgerItem right) {
                int countOrder = Integer.compare(right.getTotalCount(), left.getTotalCount());
                return countOrder != 0 ? countOrder
                        : left.getDisplayName().compareToIgnoreCase(right.getDisplayName());
            }
        });
        return Collections.unmodifiableList(result);
    }

    public synchronized List<StorageLedgerItem> search(String query) {
        String needle = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        if (needle.isEmpty()) return getItems();

        List<StorageLedgerItem> matches = new ArrayList<StorageLedgerItem>();
        for (StorageLedgerItem item : getItems()) {
            if (contains(item.getDisplayName(), needle) || contains(item.getItemId(), needle)) {
                matches.add(item);
            }
        }
        return Collections.unmodifiableList(matches);
    }

    public synchronized int getTotalItemCount() {
        int total = 0;
        for (MutableItem item : items.values()) total += item.totalCount;
        return total;
    }

    public synchronized int size() { return items.size(); }

    private boolean contains(String value, String needle) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(needle);
    }

    private static final class MutableItem {
        private final String itemId;
        private final int metadata;
        private final String displayName;
        private int totalCount;
        private int chestCount;
        private long lastObservedAt;

        private MutableItem(String itemId, int metadata, String displayName) {
            this.itemId = itemId;
            this.metadata = metadata;
            this.displayName = displayName == null ? itemId : displayName;
        }

        private StorageLedgerItem toImmutable() {
            return new StorageLedgerItem(itemId, metadata, displayName, totalCount,
                    chestCount, lastObservedAt);
        }
    }
}
