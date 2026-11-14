package com.alpharip.staffmod.store.cosmetics;

/**
 * Registers the 128 migrated Cosmetics source components in the Store catalogue.
 * These entries are explicitly marked as prototypes: the source files currently
 * provide data/configuration foundations, not complete live cosmetic behavior.
 */
public final class CosmeticPrototypeCatalog {
    private static final String[] COMPONENTS = {
        "AlphaBadgeChatCosmetic",
        "AlphaColoredName",
        "AlphaCosmeticsCore",
        "AlphaLogoKillEffect",
        "AlphaLogoVictoryEffect",
        "AlphaParticlesHitEffect",
        "AlphaTrail",
        "AnimatedName",
        "AnimatedPrefixes",
        "AnimationCustomizer",
        "BigName",
        "BowEmote",
        "CapeManager",
        "Capes",
        "CelebrateEmote",
        "ChatAppearanceCustomization",
        "ChatBadges",
        "ChatBubblesAbovePlayerHeads",
        "CoinsVictoryEffect",
        "ComboParticlesHitEffect",
        "ConfettiKillEffect",
        "ConfettiVictoryEffect",
        "CosmeticAssetManagement",
        "CosmeticAuditHistory",
        "CosmeticBundles",
        "CosmeticCoinShop",
        "CosmeticCollectionScreen",
        "CosmeticDatabase",
        "CosmeticLoadouts",
        "CosmeticManager",
        "CosmeticMixer",
        "CosmeticMysteryBoxes",
        "CosmeticPerformanceSettings",
        "CosmeticPetsLobby",
        "CosmeticPreview",
        "CosmeticSecurity",
        "CosmeticSetSynergy",
        "CosmeticShopPreview",
        "CosmeticStore",
        "CosmeticVisibility",
        "CosmeticVisibilitySettings",
        "CreatorBadgeChatCosmetic",
        "CriticalParticlesHitEffect",
        "CrossArmsEmote",
        "CrownVictoryEffect",
        "CursorSkins",
        "CustomCrosshairManager",
        "CustomGrassAndFoliageColors",
        "CustomHotbarSkin",
        "CustomHudFont",
        "CustomLoadingScreens",
        "CustomMenuButtonStyles",
        "CustomMenuSounds",
        "CustomMenuSoundscape",
        "CustomNametagBackgroundsAnimations",
        "CustomProfileBanners",
        "CustomRainSnowParticles",
        "CustomSkyAndStars",
        "CustomWeatherParticles",
        "CustomXpBarColor",
        "DabEmote",
        "DanceEmote",
        "DeveloperBadgeChatCosmetic",
        "EarlySupporterBadge",
        "EmoteManager",
        "EmoteWheel",
        "EmoteWheelCustomization",
        "EnergyBurstVictoryEffect",
        "EnergyTrail",
        "ExplosionVictoryEffect",
        "FireTrail",
        "FireworksVictoryEffect",
        "FounderBadgeChatCosmetic",
        "FriendNametagColors",
        "FriendNametags",
        "FriendsCosmeticIntegration",
        "GlowName",
        "GradientName",
        "HeartTrail",
        "LaughEmote",
        "LauncherCosmeticIntegration",
        "LightningTrail",
        "LightningVictoryEffect",
        "LimitedCosmeticsSystem",
        "LimitedTimeEventBadges",
        "LinkedAccountVerificationBadge",
        "LocalSkinPreviewSwitcher",
        "MenuOpeningAnimations",
        "MouseTrail",
        "MultipleAchievementBadges",
        "NameTagCustomizer",
        "OwnerEmotePack",
        "ParticleCustomizer",
        "PixelExplosionKillEffect",
        "PixelTrail",
        "PointEmote",
        "PortalVictoryEffect",
        "PremiumBadgeChatCosmetic",
        "RadialEmoteMenu",
        "RainbowName",
        "RainbowNametagForYourself",
        "RainbowTrail",
        "RankBasedCosmetics",
        "RotatingSkinPreviewInMenus",
        "SaluteEmote",
        "ScreenshotFilters",
        "SelectableMainMenuBackgrounds",
        "SilentNotificationSkin",
        "SitEmote",
        "SkinLayerToggle",
        "SkinManager",
        "SmokeKillEffect",
        "SmokeTrail",
        "SocialBadgeShowcase",
        "SoulKillEffect",
        "SpotifyNowPlayingBadge",
        "StaffBadgeChatCosmetic",
        "StarTrail",
        "StarsVictoryEffect",
        "SwingAnimationSpeed",
        "ThreeDSkins",
        "TrailEffects",
        "UnlockableGradientColoredNames",
        "UnlockableHotbarSkins",
        "ViewmodelCustomizer",
        "VoidTrail",
        "WaveEmote",
        "Wings"
    };

    private CosmeticPrototypeCatalog() { }

    public static int registerAll(CosmeticRegistry registry) {
        if (registry == null) throw new IllegalArgumentException("Cosmetic registry is required.");
        int registered = 0;
        for (String className : COMPONENTS) {
            String id = toId(className);
            String name = toDisplayName(className);
            String description = "Prototype component. Live rendering, ownership, and purchase behavior are not connected yet.";
            CosmeticEntry entry = new CosmeticEntry(id, name, description,
                    categoryFor(className), "com.alpharip.staffmod.store.cosmetics." + className);
            if (registry.register(entry)) registered++;
        }
        return registered;
    }

    public static int size() { return COMPONENTS.length; }

    private static CosmeticCategory categoryFor(String value) {
        if (value.contains("Trail")) return CosmeticCategory.TRAILS;
        if (value.contains("Emote")) return CosmeticCategory.EMOTES;
        if (value.contains("VictoryEffect") || value.contains("KillEffect")
                || value.contains("ParticlesHitEffect") || value.endsWith("Effect"))
            return CosmeticCategory.EFFECTS;
        if (value.contains("Cape")) return CosmeticCategory.CAPES;
        if (value.contains("Name") || value.contains("Nametag") || value.contains("Prefixes"))
            return CosmeticCategory.NAMES;
        if (value.contains("Badge") || value.contains("Chat") || value.contains("Social")
                || value.contains("Spotify") || value.contains("Achievement")
                || value.contains("Supporter") || value.contains("Verification"))
            return CosmeticCategory.CHAT;
        if (value.contains("Menu") || value.contains("Cursor") || value.contains("Crosshair")
                || value.contains("Hotbar") || value.contains("Hud") || value.contains("Profile")
                || value.contains("Loading") || value.contains("Notification")
                || value.contains("Skin") || value.contains("Screenshot")
                || value.contains("Viewmodel") || value.contains("Weather")
                || value.contains("Sky") || value.contains("XpBar")
                || value.contains("SwingAnimation") || value.contains("3DSkins"))
            return CosmeticCategory.UI;
        return CosmeticCategory.OTHER;
    }

    private static String toId(String value) {
        String separated = value.replaceAll("([a-z0-9])([A-Z])", "$1-$2");
        return separated.toLowerCase(java.util.Locale.ROOT);
    }

    private static String toDisplayName(String value) {
        String separated = value.replaceAll("([a-z0-9])([A-Z])", "$1 $2");
        return separated.replace("3 D", "3D").trim();
    }
}
