package com.alpharip.staffmod;

import com.alpharip.alphaclient.AlphaClient;
import com.alpharip.staffmod.modules.LongJumpMod;
import com.alpharip.staffmod.movement.BhopMode;

/**
 * Temporary source-compatibility bridge for inactive legacy GUI classes.
 * AlphaClient is the only Forge @Mod entry point.
 */
@Deprecated
public final class StaffMod {
    public static final String MODID = AlphaClient.MOD_ID;
    public static final String NAME = AlphaClient.NAME;
    public static final String VERSION = AlphaClient.VERSION;

    public static AlphaClient instance;
    public static BhopMode bhopMode;
    public static LongJumpMod longJumpMod;

    private StaffMod() {
    }
}