package com.alpharip.staffmod.rankviewer;

import net.minecraft.util.ResourceLocation;
import java.util.Locale;

/** Public community rank presentation. This is display-only, not an authorization source. */
public enum AlphaRank {
    FOUNDER("founder", "Founder", 0xFFFFD866, "F"),
    OWNER("owner", "Owner", 0xFFFF414D, "O"),
    MANAGEMENT("management", "Management", 0xFF9B4DFF, "M"),
    SENIOR_ADMIN("senior_admin", "Senior Admin", 0xFF159BFF, "S"),
    ADMIN("admin", "Admin", 0xFF00D9D9, "A"),
    SENIOR_MODERATOR("senior_moderator", "Senior Moderator", 0xFFFF35B8, "S"),
    MODERATOR("moderator", "Moderator", 0xFF39E85B, "M"),
    TRIAL_MODERATOR("trial_moderator", "Trial Moderator", 0xFF159BFF, "T"),
    DEVELOPER("developer", "Developer", 0xFF87CEFA, "D"),
    PARTNER("partner", "Partner", 0xFFFF69B4, "P"),
    FAMOUS("famous", "Famous", 0xFFFF69B4, "F"),
    MEDIA("media", "Media", 0xFFFF69B4, "M"),
    THE_NIBBLER("the_nibbler", "The Nibbler", 0xFFC3BFE0, "N"),
    ELITE("elite", "Elite", 0xFF00D9FF, "E"),
    ROGUE("rogue", "Rogue", 0xFFA855F7, "R"),
    SCOUT("scout", "Scout", 0xFFFFD700, "S"),
    BETA("beta", "Beta", 0xFFFF922E, "B"),
    ALPHA_PLUS("alpha_plus", "Alpha+", 0xFF00D9D9, "A+"),
    ALPHA("alpha", "Alpha", 0xFF777BFF, "A"),
    MEMBER("member", "Member", 0xFFB8BDC8, "•");

    private final String key;
    private final String label;
    private final int color;
    private final String emblem;
    private final ResourceLocation texture;

    AlphaRank(String key, String label, int color, String emblem) {
        this.key = key;
        this.label = label;
        this.color = color;
        this.emblem = emblem;
        String textureName = key;
        if ("elite".equals(key) || "rogue".equals(key) || "scout".equals(key)
                || "developer".equals(key) || "media".equals(key)) {
            textureName += "_64";
        }
        this.texture = new ResourceLocation(
                "alphastaffmod", "textures/ranks/" + textureName + ".png");
    }

    public String key() { return key; }
    public String label() { return label; }
    public int color() { return color; }
    public String emblem() { return emblem; }
    public ResourceLocation texture() { return texture; }

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