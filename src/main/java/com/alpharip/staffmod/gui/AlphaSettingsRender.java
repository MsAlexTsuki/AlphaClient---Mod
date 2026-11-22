// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

/** Rounded panel rendering for non-StaffMods AlphaClient screens. */
public final class AlphaSettingsRender {
    private AlphaSettingsRender() {}

    public static void drawPanel(int left, int top, int right, int bottom) {
        AlphaMenuRender.roundedRect(left, top, right, bottom, 9.0F, 0xD90A0C11);
        AlphaMenuRender.roundedOutline(left, top, right, bottom, 9.0F, 0x66515A68);
        AlphaMenuRender.roundedRect(left + 1, top + 1, right - 1, top + 3, 1.0F, 0xFFFFD866);
    }
}
