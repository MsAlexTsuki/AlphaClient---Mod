// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.movement;

/**
 * Backwards-compatible alias for the general module profile manager.
 *
 * New AlphaClient code should use
 * {@link com.alpharip.staffmod.profile.ModuleProfileManager}.
 * This bridge remains so legacy BHOP classes and older call sites keep working.
 */
@Deprecated
public final class BhopProfileManager
        extends com.alpharip.staffmod.profile.ModuleProfileManager {

    private BhopProfileManager() {
    }
}
