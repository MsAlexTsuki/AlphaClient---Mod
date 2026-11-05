package com.alpharip.staffmod.ComboCounter;

/** Provides the settings screen with the active Combo Counter instance. */
public final class ComboCounterSettingsAccess {
    private static ComboCounter instance;

    private ComboCounterSettingsAccess() {
    }

    public static void setInstance(ComboCounter counter) {
        instance = counter;
    }

    private static ComboCounter get() {
        if (instance == null) throw new IllegalStateException("Combo Counter has not been initialized.");
        return instance;
    }

    public static boolean isShowBackground() { return get().isShowBackground(); }
    public static void setShowBackground(boolean value) { get().setShowBackground(value); }
    public static String getColorName() { return get().getColorName(); }
    public static void cycleColor() { get().cycleColor(); }
}
