package com.alpharip.staffmod.store.addons;

/** Session-local filters and preferences for the Addons store page. */
public final class AddonStoreSettings {
    private boolean enabled = true;
    private AddonCategory selectedCategory = AddonCategory.ALL;
    private String searchQuery = "";
    private boolean showInstalledOnly;
    private String style = "default";

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public AddonCategory getSelectedCategory() { return selectedCategory; }
    public void setSelectedCategory(AddonCategory category) {
        this.selectedCategory = category == null ? AddonCategory.ALL : category;
    }
    public String getSearchQuery() { return searchQuery; }
    public void setSearchQuery(String query) {
        String value = query == null ? "" : query.trim();
        this.searchQuery = value.length() > 80 ? value.substring(0, 80) : value;
    }
    public boolean isShowInstalledOnly() { return showInstalledOnly; }
    public void setShowInstalledOnly(boolean value) { showInstalledOnly = value; }
    public String getStyle() { return style; }
    public void setStyle(String style) {
        if (style != null && !style.trim().isEmpty()) this.style = style.trim();
    }
}
