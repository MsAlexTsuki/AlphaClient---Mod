package com.alpharip.staffmod.store.cosmetics;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Cosmetic browsing, category filtering, and bounded search. No purchase or unlock behavior. */
public final class CosmeticCatalog {
    private final CosmeticRegistry registry;
    private CosmeticCategory selectedCategory = CosmeticCategory.ALL;
    private String searchQuery = "";

    public CosmeticCatalog() { this(new CosmeticRegistry()); }

    public CosmeticCatalog(CosmeticRegistry registry) {
        if (registry == null) throw new IllegalArgumentException("Cosmetic registry is required.");
        this.registry = registry;
    }

    public CosmeticRegistry getRegistry() { return registry; }
    public synchronized CosmeticCategory getSelectedCategory() { return selectedCategory; }
    public synchronized void setSelectedCategory(CosmeticCategory category) {
        selectedCategory = category == null ? CosmeticCategory.ALL : category;
    }
    public synchronized String getSearchQuery() { return searchQuery; }
    public synchronized void setSearchQuery(String query) {
        String value = query == null ? "" : query.trim();
        searchQuery = value.length() > 80 ? value.substring(0, 80) : value;
    }

    public synchronized List<CosmeticEntry> getVisibleCosmetics() {
        String needle = searchQuery.toLowerCase(Locale.ROOT);
        List<CosmeticEntry> visible = new ArrayList<CosmeticEntry>();
        for (CosmeticEntry entry : registry.getAll()) {
            if (selectedCategory != CosmeticCategory.ALL
                    && entry.getCategory() != selectedCategory) continue;
            String searchable = (entry.getId() + " " + entry.getName() + " "
                    + entry.getDescription() + " " + entry.getSourceClass()).toLowerCase(Locale.ROOT);
            if (needle.isEmpty() || searchable.contains(needle)) visible.add(entry);
        }
        return visible;
    }

    public synchronized void clearSessionFilters() {
        selectedCategory = CosmeticCategory.ALL;
        searchQuery = "";
    }
}
