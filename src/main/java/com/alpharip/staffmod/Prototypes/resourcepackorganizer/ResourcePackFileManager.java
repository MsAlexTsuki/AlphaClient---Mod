package com.alpharip.staffmod.resourcepackorganizer;

import java.io.File;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Handles real folder-based organization below .minecraft/resourcepacks.
 *
 * A category is a direct child directory that is not itself a resource pack
 * (that is, it does not contain pack.mcmeta). Packs are ZIP files or folders
 * containing pack.mcmeta.
 */
public final class ResourcePackFileManager {
    private final File resourcePacksDirectory;

    public ResourcePackFileManager(File resourcePacksDirectory) {
        if (resourcePacksDirectory == null) throw new IllegalArgumentException("resourcePacksDirectory");
        this.resourcePacksDirectory = resourcePacksDirectory;
    }

    public File getResourcePacksDirectory() { return resourcePacksDirectory; }

    public List<String> getCategories() {
        File[] children = resourcePacksDirectory.listFiles();
        if (children == null) return Collections.emptyList();

        List<String> categories = new ArrayList<String>();
        for (File child : children) {
            if (isCategoryDirectory(child)) categories.add(child.getName());
        }
        Collections.sort(categories, String.CASE_INSENSITIVE_ORDER);
        return Collections.unmodifiableList(categories);
    }

    public List<ResourcePackEntry> scanPacks() {
        List<ResourcePackEntry> packs = new ArrayList<ResourcePackEntry>();
        scanDirectory(resourcePacksDirectory, "", packs);

        for (String category : getCategories()) {
            scanDirectory(new File(resourcePacksDirectory, category), category, packs);
        }

        Collections.sort(packs, new Comparator<ResourcePackEntry>() {
            @Override public int compare(ResourcePackEntry left, ResourcePackEntry right) {
                int categoryOrder = left.getCategory().compareToIgnoreCase(right.getCategory());
                return categoryOrder != 0 ? categoryOrder : left.getName().compareToIgnoreCase(right.getName());
            }
        });
        return Collections.unmodifiableList(packs);
    }

    private void scanDirectory(File directory, String category, List<ResourcePackEntry> result) {
        File[] children = directory.listFiles();
        if (children == null) return;

        for (File child : children) {
            if (isPack(child)) result.add(new ResourcePackEntry(child, category));
        }
    }

    public boolean createCategory(String requestedName) throws IOException {
        String name = normalizeCategoryName(requestedName);
        if (name.length() == 0) return false;
        ensureRootExists();

        File category = new File(resourcePacksDirectory, name);
        if (category.exists()) return isCategoryDirectory(category);
        return category.mkdir();
    }

    public boolean movePack(ResourcePackEntry pack, String targetCategory) throws IOException {
        if (pack == null || !isPack(pack.getFile())) return false;
        ensureRootExists();

        File source = pack.getFile().getCanonicalFile();
        File root = resourcePacksDirectory.getCanonicalFile();
        File sourceParent = source.getParentFile();
        if (sourceParent == null) return false;

        boolean fromRoot = sourceParent.equals(root);
        boolean fromCategory = sourceParent.getParentFile() != null
                && sourceParent.getParentFile().equals(root)
                && isCategoryDirectory(sourceParent);
        if (!fromRoot && !fromCategory) return false;

        String category = targetCategory == null ? "" : targetCategory.trim();
        File destinationDirectory;
        if (category.length() == 0) {
            destinationDirectory = root;
        } else {
            if (!isSafeCategoryName(category)) return false;
            destinationDirectory = new File(root, category);
            if (!destinationDirectory.exists() && !destinationDirectory.mkdir()) return false;
            if (!isCategoryDirectory(destinationDirectory)) return false;
        }

        if (sourceParent.equals(destinationDirectory.getCanonicalFile())) return true;
        File destination = new File(destinationDirectory, source.getName());
        if (destination.exists()) return false;

        try {
            Files.move(source.toPath(), destination.toPath(), StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(source.toPath(), destination.toPath());
        }
        return true;
    }

    private void ensureRootExists() throws IOException {
        if (!resourcePacksDirectory.exists() && !resourcePacksDirectory.mkdirs()) {
            throw new IOException("Could not create resourcepacks directory: " + resourcePacksDirectory);
        }
        if (!resourcePacksDirectory.isDirectory()) {
            throw new IOException("Resourcepacks path is not a directory: " + resourcePacksDirectory);
        }
    }

    private boolean isCategoryDirectory(File file) {
        return file != null && file.isDirectory() && !new File(file, "pack.mcmeta").isFile();
    }

    private boolean isPack(File file) {
        if (file == null || !file.exists()) return false;
        if (file.isFile()) return file.getName().toLowerCase().endsWith(".zip");
        return file.isDirectory() && new File(file, "pack.mcmeta").isFile();
    }

    private String normalizeCategoryName(String value) {
        if (value == null) return "";
        String name = value.trim();
        if (!isSafeCategoryName(name)) return "";
        return name;
    }

    private boolean isSafeCategoryName(String name) {
        if (name == null || name.trim().length() == 0) return false;
        if (".".equals(name) || "..".equals(name)) return false;
        return name.indexOf('/') < 0 && name.indexOf('\\') < 0
                && name.indexOf(':') < 0 && name.indexOf(0) < 0;
    }
}
