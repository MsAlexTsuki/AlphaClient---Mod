package com.alpharip.staffmod.store;

import com.alpharip.staffmod.store.addons.AddonStore;
import com.alpharip.staffmod.store.cosmetics.CosmeticCatalog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Central Store core. Each section owns its own catalogue and session state. */
public final class Store {
    private boolean enabled = true;
    private String selectedSectionId = "addons";
    private final Map<String, StoreSection> sections = new LinkedHashMap<String, StoreSection>();
    private final AddonStore addonStore;
    private final CosmeticCatalog cosmeticCatalog;

    public Store() {
        this(new AddonStore(), new CosmeticCatalog());
    }

    public Store(AddonStore addonStore) {
        this(addonStore, new CosmeticCatalog());
    }

    public Store(AddonStore addonStore, CosmeticCatalog cosmeticCatalog) {
        if (addonStore == null || cosmeticCatalog == null)
            throw new IllegalArgumentException("Store catalogues are required.");
        this.addonStore = addonStore;
        this.cosmeticCatalog = cosmeticCatalog;
        registerSection(new StoreSection("addons", "Addons",
                "Browse and manage AlphaClient add-ons."));
        registerSection(new StoreSection("cosmetics", "Cosmetics",
                "Browse cosmetic items and customization options."));
    }

    public synchronized boolean isEnabled() { return enabled; }
    public synchronized void setEnabled(boolean enabled) { this.enabled = enabled; }

    public synchronized boolean registerSection(StoreSection section) {
        if (section == null || sections.containsKey(section.getId())) return false;
        sections.put(section.getId(), section);
        if (selectedSectionId == null) selectedSectionId = section.getId();
        return true;
    }

    public synchronized boolean selectSection(String sectionId) {
        if (sectionId == null || !sections.containsKey(sectionId)) return false;
        selectedSectionId = sectionId;
        return true;
    }

    public synchronized StoreSection getSelectedSection() { return sections.get(selectedSectionId); }
    public synchronized String getSelectedSectionId() { return selectedSectionId; }
    public synchronized List<StoreSection> getSections() {
        return Collections.unmodifiableList(new ArrayList<StoreSection>(sections.values()));
    }
    public AddonStore getAddonStore() { return addonStore; }
    public CosmeticCatalog getCosmeticCatalog() { return cosmeticCatalog; }

    public synchronized void clearSessionState() {
        selectedSectionId = sections.isEmpty() ? null : sections.keySet().iterator().next();
        addonStore.clearSessionFilters();
        cosmeticCatalog.clearSessionFilters();
    }

    public static final class StoreSection {
        private final String id;
        private final String title;
        private final String description;

        public StoreSection(String id, String title, String description) {
            if (id == null || id.trim().isEmpty() || title == null || title.trim().isEmpty())
                throw new IllegalArgumentException("Section ID and title are required.");
            this.id = id.trim();
            this.title = title.trim();
            this.description = description == null ? "" : description.trim();
        }
        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
    }
}
