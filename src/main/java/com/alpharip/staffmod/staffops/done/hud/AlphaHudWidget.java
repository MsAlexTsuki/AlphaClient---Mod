// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.hud;

import net.minecraft.client.Minecraft;

/** A single independently rendered AlphaClient HUD element. */
public interface AlphaHudWidget {
    String getId();
    String getModuleId();
    void render(Minecraft minecraft, int screenWidth, int screenHeight, float partialTicks);
}
