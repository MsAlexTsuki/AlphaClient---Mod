package com.alpharip.staffmod.resourcepackorganizer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Entry point and query layer for the folder-based resource-pack organizer prototype. */
public final class ResourcePackOrganizer {
    private final ResourcePackFileManager fileManager;

    public ResourcePackOrganizer(File minecraftDirectory) {
        if (minecraftDirectory == null) throw new IllegalArgumentException("minecraftDirectory");
        this.fileManager = new ResourcePackFileManager(new File(minecraftDirectory, "resourcepacks"));
    }

    public ResourcePackFileManager getFileManager() { return fileManager; }

    public List<ResourcePackEntry> getPacks(String category, String query) {
        String selectedCategory = category == null ? "" : category;
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        List<ResourcePackEntry> result = new ArrayList<ResourcePackEntry>();

        for (ResourcePackEntry pack : fileManager.scanPacks()) {
            if (!"*".equals(selectedCategory) && !selectedCategory.equals(pack.getCategory())) continue;
            if (normalizedQuery.length() > 0
                    && !pack.getName().toLowerCase(Locale.ROOT).contains(normalizedQuery)) continue;
            result.add(pack);
        }
        return Collections.unmodifiableList(result);
    }

    public List<String> getCategories() { return fileManager.getCategories(); }

    public boolean createCategory(String name) throws IOException {
        return fileManager.createCategory(name);
    }

    public boolean movePack(ResourcePackEntry pack, String category) throws IOException {
        return fileManager.movePack(pack, category);
    }
}
