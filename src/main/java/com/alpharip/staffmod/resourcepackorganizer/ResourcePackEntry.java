package com.alpharip.staffmod.resourcepackorganizer;

import java.io.File;

/** A resource pack discovered in the root folder or one organizer category. */
public final class ResourcePackEntry {
    private final File file;
    private final String category;

    public ResourcePackEntry(File file, String category) {
        if (file == null) throw new IllegalArgumentException("file");
        this.file = file;
        this.category = category == null ? "" : category;
    }

    public File getFile() { return file; }
    public String getName() { return file.getName(); }
    public String getCategory() { return category; }
    public boolean isArchive() { return file.isFile() && file.getName().toLowerCase().endsWith(".zip"); }
    public boolean isDirectoryPack() { return file.isDirectory() && new File(file, "pack.mcmeta").isFile(); }
    public String getTypeLabel() { return isArchive() ? "ZIP" : "FOLDER"; }
}
