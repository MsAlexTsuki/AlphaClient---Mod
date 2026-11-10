package com.alpharip.staffmod.kitmaphcf.factions.ally;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

/**
 * Standalone entry point for the Ally Faction Notes prototype.
 *
 * The AlphaClient core does not register or reference this class yet.
 * A future integration can call open() from the appropriate Kitmap/HCF menu.
 */
public final class AllyFactionNotesModule {
    public static final String MODULE_NAME = "Ally Faction Notes";

    private AllyFactionNotesModule() { }

    public static void open() {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null) {
            return;
        }

        AllyFactionNotesStore store = AllyFactionNotesStore.getInstance();
        store.load();

        GuiScreen parent = minecraft.currentScreen;
        minecraft.displayGuiScreen(new AllyFactionNotesScreen(parent, store));
    }
}
