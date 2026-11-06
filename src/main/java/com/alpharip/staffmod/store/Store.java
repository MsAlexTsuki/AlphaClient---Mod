package com.alpharip.staffmod.store;

import com.alpharip.staffmod.store.addons.AddonStore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Central Store core. Owns store-wide state and registered sections.
 * This prototype is local and does not download, install, or execute external code.
 */
public final class Store {
    private boolean enabled;
    private String selectedSectionId;
    private final Map<String, StoreSection> sections = new LinkedHashMap<String, StoreSection>();
    private final AddonStore addonStore;

    public Store() {
        this.enabled = true;
        this.selectedSectionId = "addons";
        this.addonStore = new AddonStore();
        registerSection(new StoreSection("addons", "Addons",
                "Browse and manage locally registered AlphaClient add-ons."));
    }

    public synchronized boolean isEnabled() { return enabled; }
    public synchronized void setEnabled(boolean enabled) { this.enabled = enabled; }

    public synchronized boolean registerSection(StoreSection section) {
        if (section == null || section.getId().trim().isEmpty()) return false;
        if (sections.containsKey(section.getId())) return false;
        sections.put(section.getId(), section);
        if (selectedSectionId == null) selectedSectionId = section.getId();
        return true;
    }

    public synchronized boolean selectSection(String sectionId) {
        if (sectionId == null || !sections.containsKey(sectionId)) return false;
        selectedSectionId = sectionId;
        return true;
    }

    public synchronized StoreSection getSelectedSection() {
        return sections.get(selectedSectionId);
    }

    public synchronized String getSelectedSectionId() { return selectedSectionId; }

    public synchronized List<StoreSection> getSections() {
        return Collections.unmodifiableList(new ArrayList<StoreSection>(sections.values()));
    }

    public AddonStore getAddonStore() { return addonStore; }

    public synchronized void clearSessionState() {
        selectedSectionId = sections.isEmpty() ? null : sections.keySet().iterator().next();
        addonStore.clear();
    }

    /** Immutable descriptor for one page/section in the central Store. */
    public static final class StoreSection {
        private final String id;
        private final String title;
        private final String description;

        public StoreSection(String id, String title, String description) {
            if (id == null || id.trim().isEmpty() || title == null || title.trim().isEmpty()) {
                throw new IllegalArgumentException("Section ID and title are required.");
            }
            this.id = id.trim();
            this.title = title.trim();
            this.description = description == null ? "" : description.trim();
        }
        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
    }
}
