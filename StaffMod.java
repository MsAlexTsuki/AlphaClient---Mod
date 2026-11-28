package com.alpharip.staffmod;

import com.alpharip.alphaclient.AlphaClient;
import com.alpharip.staffmod.movement.BhopMode;

/**
 * Legacy compatibility bridge for older staffmod GUI classes.
 * New AlphaClient code must use {@link com.alpharip.alphaclient.AlphaClient} directly.
 * AlphaClient is the only Forge @Mod entry point.
 */
public final class StaffMod {
    public static final String MODID = AlphaClient.MOD_ID;
    public static final String NAME = AlphaClient.NAME;
    public static final String VERSION = AlphaClient.VERSION;

    public static AlphaClient instance;
    public static BhopMode bhopMode;

    private StaffMod() {
    }
}
