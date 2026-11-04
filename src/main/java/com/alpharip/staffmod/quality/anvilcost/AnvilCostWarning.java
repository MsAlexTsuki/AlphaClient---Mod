package com.alpharip.staffmod.quality.anvilcost;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumChatFormatting;

/** Formats Anvil Cost warnings shown inside the normal item tooltip. */
public final class AnvilCostWarning {
    private AnvilCostWarning() {
    }

    public static String formatCost(AnvilCostCalculator.Preview preview, EntityPlayer player) {
        if (preview == null || player == null) {
            return "";
        }

        String cost = EnumChatFormatting.YELLOW + String.valueOf(preview.getLevels())
                + " levels";

        if (preview.getLevels() >= 40 && !player.capabilities.isCreativeMode) {
            cost += EnumChatFormatting.RED + " (Too Expensive)";
        }

        if (preview.getLevels() > player.experienceLevel) {
            int missingLevels = preview.getLevels() - player.experienceLevel;
            cost += EnumChatFormatting.RED + " (Need " + missingLevels + " more)";
        }

        return cost;
    }
}
