package com.alpharip.staffmod.store.addons;

/** Immutable metadata for one locally catalogued add-on. */
public final class AddonEntry {
    private final String id, name, description, version, author;
    private final AddonCategory category;

    public AddonEntry(String id, String name, String description, String version,
                      String author, AddonCategory category) {
        if (blank(id) || blank(name) || blank(version) || blank(author) || category == null) {
            throw new IllegalArgumentException("Add-on ID, name, version, author, and category are required.");
        }
        this.id = id.trim();
        this.name = name.trim();
        this.description = description == null ? "" : description.trim();
        this.version = version.trim();
        this.author = author.trim();
        this.category = category;
    }

    private static boolean blank(String value) { return value == null || value.trim().isEmpty(); }
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getVersion() { return version; }
    public String getAuthor() { return author; }
    public AddonCategory getCategory() { return category; }
}
