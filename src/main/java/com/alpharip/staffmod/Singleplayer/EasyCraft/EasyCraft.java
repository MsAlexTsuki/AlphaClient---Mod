package com.alpharip.staffmod.Singleplayer.EasyCraft;

import net.minecraft.client.Minecraft;

/** Entry point and local state owner for the Easy Craft prototype. */
public final class EasyCraft {
    private final EasyCraftRecipeCatalog catalog;
    private final EasyCraftFavorites favorites;

    public EasyCraft() {
        this.catalog = new EasyCraftRecipeCatalog();
        this.favorites = new EasyCraftFavorites(Minecraft.getMinecraft().mcDataDir);
    }

    public EasyCraftRecipeCatalog getCatalog() {
        return catalog;
    }

    public EasyCraftFavorites getFavorites() {
        return favorites;
    }

    public void refreshRecipes() {
        catalog.refresh();
    }
}
