// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.pvp;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;

/**
 * Loads the narrowly scoped renderer transformers used by AlphaClient.
 */
@IFMLLoadingPlugin.Name("AlphaClientLegacyPvp")
@IFMLLoadingPlugin.MCVersion("1.8.9")
@IFMLLoadingPlugin.SortingIndex(1001)
public final class LegacyPvpCorePlugin implements IFMLLoadingPlugin {
    @Override
    public String[] getASMTransformerClass() {
        return new String[] {
                LegacyPvpTransformer.class.getName(),
                com.alpharip.staffmod.gui.AlphaOptionsTransformer.class.getName(),
                com.alpharip.staffmod.gui.AlphaChatAutocompleteTransformer.class.getName()
        };
    }

    @Override public String getModContainerClass() { return null; }
    @Override public String getSetupClass() { return null; }
    @Override public void injectData(Map<String, Object> data) { }
    @Override public String getAccessTransformerClass() { return null; }
}
