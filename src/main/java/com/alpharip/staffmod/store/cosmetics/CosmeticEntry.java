package com.alpharip.staffmod.store.cosmetics;

/** Immutable descriptive metadata for one cosmetic catalogue item. */
public final class CosmeticEntry {
    private final String id;
    private final String name;
    private final String description;
    private final String sourceClass;
    private final CosmeticCategory category;

    public CosmeticEntry(String id, String name, String description,
                         CosmeticCategory category, String sourceClass) {
        if (blank(id) || blank(name) || category == null)
            throw new IllegalArgumentException("Cosmetic ID, name, and category are required.");
        this.id = id.trim();
        this.name = name.trim();
        this.description = description == null ? "" : description.trim();
        this.category = category;
        this.sourceClass = sourceClass == null ? "" : sourceClass.trim();
    }

    private static boolean blank(String value) { return value == null || value.trim().isEmpty(); }
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public CosmeticCategory getCategory() { return category; }
    public String getSourceClass() { return sourceClass; }
}
