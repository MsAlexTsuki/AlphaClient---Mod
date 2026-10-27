// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.pvp;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;

/**
 * Loads the narrowly scoped ItemRenderer transformer used by Legacy PvP.
 */
@IFMLLoadingPlugin.Name("AlphaClientLegacyPvp")
@IFMLLoadingPlugin.MCVersion("1.8.9")
@IFMLLoadingPlugin.SortingIndex(1001)
public final class LegacyPvpCorePlugin implements IFMLLoadingPlugin {
    @Override
    public String[] getASMTransformerClass() {
        return new String[] { LegacyPvpTransformer.class.getName() };
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        // No launch-time data is required.
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
