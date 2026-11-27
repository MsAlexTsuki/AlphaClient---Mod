package com.alpharip.staffmod.store.partners;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Partner directory search and relationship filtering. No external account or social API calls. */
public final class PartnerCatalog {
    private final PartnerRegistry registry;
    private PartnerType selectedType = PartnerType.ALL;
    private String searchQuery = "";

    public PartnerCatalog() { this(new PartnerRegistry()); }

    public PartnerCatalog(PartnerRegistry registry) {
        if (registry == null) throw new IllegalArgumentException("Partner registry is required.");
        this.registry = registry;
    }

    public PartnerRegistry getRegistry() { return registry; }
    public synchronized PartnerType getSelectedType() { return selectedType; }
    public synchronized void setSelectedType(PartnerType type) {
        selectedType = type == null ? PartnerType.ALL : type;
    }
    public synchronized String getSearchQuery() { return searchQuery; }
    public synchronized void setSearchQuery(String query) {
        String value = query == null ? "" : query.trim();
        searchQuery = value.length() > 80 ? value.substring(0, 80) : value;
    }

    public synchronized List<PartnerEntry> getVisiblePartners() {
        String needle = searchQuery.toLowerCase(Locale.ROOT);
        List<PartnerEntry> visible = new ArrayList<PartnerEntry>();
        for (PartnerEntry entry : registry.getAll()) {
            if (selectedType != PartnerType.ALL && entry.getType() != selectedType) continue;
            String searchable = (entry.getId() + " " + entry.getName() + " "
                    + entry.getDescription() + " " + entry.getPlatform() + " "
                    + entry.getProfileUrl() + " " + entry.getType().getDisplayName()).toLowerCase(Locale.ROOT);
            if (needle.isEmpty() || searchable.contains(needle)) visible.add(entry);
        }
        return visible;
    }

    public synchronized void clearSessionFilters() {
        selectedType = PartnerType.ALL;
        searchQuery = "";
    }
}
