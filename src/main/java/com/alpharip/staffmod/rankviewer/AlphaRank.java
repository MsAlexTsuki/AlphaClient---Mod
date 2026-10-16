package com.alpharip.staffmod.rankviewer;

import java.util.Locale;

/** Public community rank presentation. This is display-only, not an authorization source. */
public enum AlphaRank {
    FOUNDER("founder", "Founder", 0xFFFFD866),
    OWNER("owner", "Owner", 0xFFB8BDC8),
    MANAGEMENT("management", "Management", 0xFFB8BDC8),
    SENIOR_ADMIN("senior_admin", "Sr Admin", 0xFFB8BDC8),
    ADMIN("admin", "Admin", 0xFFB8BDC8),
    SENIOR_MODERATOR("senior_moderator", "Sr Mod", 0xFFB8BDC8),
    MODERATOR("moderator", "Moderator", 0xFFB8BDC8),
    TRIAL_MODERATOR("trial_moderator", "Trial Mod", 0xFF166534),
    BETA("beta", "Beta", 0xFFB8BDC8),
    ALPHA_PLUS("alpha_plus", "Alpha+", 0xFFB8BDC8),
    ALPHA("alpha", "Alpha", 0xFFB8BDC8),
    MEMBER("member", "Member", 0xFFB8BDC8);

    private final String key;
    private final String label;
    private final int color;

    AlphaRank(String key, String label, int color) {
        this.key = key;
        this.label = label;
        this.color = color;
    }

    public String key() { return key; }
    public String label() { return label; }
    public int color() { return color; }

    public static AlphaRank fromString(String value) {
        if (value == null || value.trim().isEmpty()) return MEMBER;
        String key = value.trim().toLowerCase(Locale.ROOT)
                .replace("+", "_plus")
                .replace('-', '_')
                .replace(' ', '_');
        if ("user".equals(key) || "public".equals(key)) return MEMBER;
        if ("sr_admin".equals(key)) return SENIOR_ADMIN;
        if ("sr_mod".equals(key) || "senior_mod".equals(key)) return SENIOR_MODERATOR;
        if ("trial_mod".equals(key) || "t_mod".equals(key)) return TRIAL_MODERATOR;
        try {
            return valueOf(key.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return MEMBER;
        }
    }
}
