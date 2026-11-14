package com.alpharip.staffmod.store.partners;

/** Directory categories for AlphaClient partners, staff-cape recipients, and collaborators. */
public enum PartnerType {
    ALL("All"),
    PARTNERS("Partners"),
    STAFF("Staff"),
    COLLABORATOR("Collaborator");

    private final String displayName;
    PartnerType(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
}
