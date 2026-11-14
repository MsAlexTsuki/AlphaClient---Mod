package com.alpharip.staffmod.store.partners;

/** Immutable directory record for a person who promotes, helps, or collaborates with AlphaClient. */
public final class PartnerEntry {
    private final String id;
    private final String name;
    private final String description;
    private final PartnerType type;
    private final String platform;
    private final String profileUrl;

    public PartnerEntry(String id, String name, String description, PartnerType type,
                        String platform, String profileUrl) {
        if (blank(id) || blank(name) || type == null || type == PartnerType.ALL)
            throw new IllegalArgumentException("Partner ID, name, and a concrete relationship type are required.");
        this.id = id.trim();
        this.name = name.trim();
        this.description = description == null ? "" : description.trim();
        this.type = type;
        this.platform = platform == null ? "" : platform.trim();
        this.profileUrl = profileUrl == null ? "" : profileUrl.trim();
    }

    private static boolean blank(String value) { return value == null || value.trim().isEmpty(); }
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public PartnerType getType() { return type; }
    public String getPlatform() { return platform; }
    public String getProfileUrl() { return profileUrl; }
}
