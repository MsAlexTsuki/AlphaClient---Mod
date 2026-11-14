package com.alpharip.staffmod.store.partners;

/** Relationship types for people connected to AlphaClient through promotion or collaboration. */
public enum PartnerType {
    ALL("All"),
    PROMOTER("Promoter"),
    HELPER("Helper"),
    COLLABORATOR("Collaborator");

    private final String displayName;
    PartnerType(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
}
