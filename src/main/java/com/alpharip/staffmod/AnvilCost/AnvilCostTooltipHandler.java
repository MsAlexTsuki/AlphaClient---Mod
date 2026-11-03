package com.alpharip.staffmod.AnvilCost;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

/** Adds compact, inventory-aware anvil previews to the normal item tooltip. */
public final class AnvilCostTooltipHandler {
    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        if (event == null || event.getItemStack() == null) {
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.thePlayer == null
                || !(minecraft.currentScreen instanceof GuiContainer)) {
            return;
        }

        EntityPlayer player = minecraft.thePlayer;
        ItemStack hoveredStack = event.getItemStack();
        List<AnvilCostCalculator.Preview> previews =
                AnvilCostCalculator.calculate(hoveredStack, player);

        if (previews.isEmpty()) {
            return;
        }

        List<String> tooltip = event.getToolTip();
        tooltip.add("");
        tooltip.add(EnumChatFormatting.GOLD + "Anvil Cost");

        for (AnvilCostCalculator.Preview preview : previews) {
            String cost = EnumChatFormatting.YELLOW + String.valueOf(preview.getLevels())
                    + " levels";
            if (preview.getLevels() >= 40 && !player.capabilities.isCreativeMode) {
                cost += EnumChatFormatting.RED + " (Too Expensive)";
            } else if (preview.getLevels() > player.experienceLevel) {
                int missingLevels = preview.getLevels() - player.experienceLevel;
                cost += EnumChatFormatting.RED + " (Need " + missingLevels + " more)";
            }

            tooltip.add(EnumChatFormatting.GRAY + preview.getOperation()
                    + EnumChatFormatting.DARK_GRAY + ": " + cost
                    + EnumChatFormatting.DARK_GRAY + " - "
                    + EnumChatFormatting.GRAY + preview.getMaterialCount()
                    + "x " + preview.getMaterialName());
        }
    }
}
