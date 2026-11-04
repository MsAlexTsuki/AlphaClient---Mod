package com.alpharip.staffmod.singleplayer.autotool;

import com.alpharip.staffmod.gui.AlphaButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

/** Multi-page local settings and item-management screen for AutoTool. */
public final class AutoToolGui extends GuiScreen {
    private final GuiScreen parent;
    private int page;
    private int left;
    private int top;
    private int panelWidth;
    private int panelHeight;

    public AutoToolGui(GuiScreen parent) { this(parent, 0); }

    private AutoToolGui(GuiScreen parent, int page) {
        this.parent = parent;
        this.page = Math.max(0, Math.min(3, page));
    }

    @Override
    public void initGui() {
        buttonList.clear();
        panelWidth = Math.max(1, Math.min(360, width - 16));
        panelHeight = page == 3 ? 292 : 204;
        left = (width - panelWidth) / 2;
        top = (height - panelHeight) / 2;
        int buttonWidth = Math.max(1, panelWidth - 32);

        if (page == 3) {
            Set<String> shownItems = new HashSet<String>();
            int row = 0;
            for (int slot = 0; slot < 9; slot++) {
                ItemStack stack = mc.thePlayer.inventory.getStackInSlot(slot);
                if (stack == null || stack.getItem() == null) continue;
                ResourceLocation itemLocation = (ResourceLocation) Item.itemRegistry.getNameForObject(stack.getItem());
                if (itemLocation == null) continue;
                String itemId = itemLocation.toString();
                if (!shownItems.add(itemId)) continue;

                boolean excluded = AutoToolSettings.isItemIgnored(itemId);
                String action = excluded ? "Include" : "Exclude";
                String itemName = truncate(stack.getDisplayName(), 24);
                String label = "Slot " + (slot + 1) + ": " + itemName + " - " + action;
                buttonList.add(new AlphaButton(100 + slot, left + 16, top + 58 + row * 22,
                        buttonWidth, 20, label));
                row++;
            }
        } else {
            String[] options = optionsForPage();
            for (int index = 0; index < options.length; index++) {
                buttonList.add(new AlphaButton(index + 1, left + 16, top + 43 + index * 24,
                        buttonWidth, 20, labelFor(options[index])));
            }
        }
        int footerY = top + panelHeight - 25;
        if (page > 0) {
            buttonList.add(new AlphaButton(91, left + 16, footerY, 68, 18, "Back"));
            buttonList.add(new AlphaButton(92, left + 90, footerY, 68, 18, "Menu"));
        } else {
            buttonList.add(new AlphaButton(92, left + 16, footerY, 76, 18, "Menu"));
        }
        if (page < 3) buttonList.add(new AlphaButton(90, left + panelWidth - 92, footerY, 76, 18, "More"));
        else buttonList.add(new AlphaButton(90, left + panelWidth - 92, footerY, 76, 18, "Done"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (!AutoToolAccess.isAvailable(mc)) { closeToParent(); return; }
        if (button == null) return;
        if (button.id == 90) {
            if (page < 3) {
                mc.displayGuiScreen(new AutoToolGui(parent, page + 1));
            } else {
                closeToParent();
            }
            return;
        } else if (button.id == 91) {
            mc.displayGuiScreen(new AutoToolGui(parent, page - 1));
            return;
        } else if (button.id == 92) {
            closeToParent();
            return;
        } else if (page == 3 && button.id >= 100 && button.id <= 108) {
            int slot = button.id - 100;
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(slot);
            if (stack != null && stack.getItem() != null) {
                ResourceLocation itemLocation = (ResourceLocation) Item.itemRegistry.getNameForObject(stack.getItem());
                if (itemLocation != null) {
                    String itemId = itemLocation.toString();
                    AutoToolSettings.setItemIgnored(itemId, !AutoToolSettings.isItemIgnored(itemId));
                }
            }
            mc.displayGuiScreen(new AutoToolGui(parent, page));
            return;
        } else if (button.id >= 1 && button.id <= 5) {
            String[] options = optionsForPage();
            int index = button.id - 1;
            if (index < options.length) change(options[index]);
        }
        initGui();
    }

    private String[] optionsForPage() {
        if (page == 0) return new String[]{"enabled", "swapback", "delay", "feedback", "efficiency"};
        if (page == 1) return new String[]{"durability", "tieBreak", "pauseUse", "pauseSneak", "pauseMode"};
        return new String[]{"pauseSurvival", "pauseCreative", "pauseAdventure"};
    }

    private String labelFor(String option) {
        if ("enabled".equals(option)) return "AutoTool: " + onOff(AutoToolSettings.isEnabled());
        if ("swapback".equals(option)) return "Switch back: " + AutoToolSettings.getSwapBackMode();
        if ("delay".equals(option)) return "Switch-back delay: " + AutoToolSettings.getSwapBackDelay() + " ticks";
        if ("feedback".equals(option)) return "Selection feedback: " + onOff(AutoToolSettings.isFeedbackEnabled());
        if ("efficiency".equals(option)) return "Efficiency enchantments: " + onOff(AutoToolSettings.isEfficiencyAware());
        if ("durability".equals(option)) return "Minimum durability: " + AutoToolSettings.getMinimumDurability() + "%";
        if ("tieBreak".equals(option)) return "Prefer higher durability on ties: " + onOff(AutoToolSettings.isDurabilityTieBreakEnabled());
        if ("pauseUse".equals(option)) return "Pause while using an item: " + onOff(AutoToolSettings.isPauseWhenUsingItem());
        if ("pauseSneak".equals(option)) return "Pause while sneaking: " + onOff(AutoToolSettings.isPauseWhenSneaking());
        if ("pauseMode".equals(option)) return "Pause key mode: " + (AutoToolSettings.isPauseToggleMode() ? "Toggle" : "Hold");
        if ("pauseSurvival".equals(option)) return "Pause in Survival: " + onOff(AutoToolSettings.isPauseInSurvival());
        if ("pauseCreative".equals(option)) return "Pause in Creative: " + onOff(AutoToolSettings.isPauseInCreative());
        return "Pause in Adventure: " + onOff(AutoToolSettings.isPauseInAdventure());
    }

    private void change(String option) {
        if ("enabled".equals(option)) AutoToolSettings.setEnabled(!AutoToolSettings.isEnabled());
        else if ("swapback".equals(option)) {
            String mode = AutoToolSettings.getSwapBackMode();
            AutoToolSettings.setSwapBackMode("NEVER".equals(mode) ? "RELEASE" : "RELEASE".equals(mode) ? "BROKEN" : "NEVER");
        } else if ("delay".equals(option)) {
            int delay = AutoToolSettings.getSwapBackDelay();
            AutoToolSettings.setSwapBackDelay(delay == 0 ? 4 : delay == 4 ? 10 : delay == 10 ? 20 : 0);
        } else if ("feedback".equals(option)) AutoToolSettings.setFeedbackEnabled(!AutoToolSettings.isFeedbackEnabled());
        else if ("efficiency".equals(option)) AutoToolSettings.setEfficiencyAware(!AutoToolSettings.isEfficiencyAware());
        else if ("durability".equals(option)) {
            int value = AutoToolSettings.getMinimumDurability();
            AutoToolSettings.setMinimumDurability(value == 0 ? 10 : value == 10 ? 25 : value == 25 ? 50 : value == 50 ? 75 : 0);
        } else if ("tieBreak".equals(option)) AutoToolSettings.setDurabilityTieBreakEnabled(!AutoToolSettings.isDurabilityTieBreakEnabled());
        else if ("pauseUse".equals(option)) AutoToolSettings.setPauseWhenUsingItem(!AutoToolSettings.isPauseWhenUsingItem());
        else if ("pauseSneak".equals(option)) AutoToolSettings.setPauseWhenSneaking(!AutoToolSettings.isPauseWhenSneaking());
        else if ("pauseMode".equals(option)) AutoToolSettings.setPauseToggleMode(!AutoToolSettings.isPauseToggleMode());
        else if ("pauseSurvival".equals(option)) AutoToolSettings.setPauseInSurvival(!AutoToolSettings.isPauseInSurvival());
        else if ("pauseCreative".equals(option)) AutoToolSettings.setPauseInCreative(!AutoToolSettings.isPauseInCreative());
        else if ("pauseAdventure".equals(option)) AutoToolSettings.setPauseInAdventure(!AutoToolSettings.isPauseInAdventure());
    }

    private static String truncate(String value, int maxLength) {
        if (value == null) return "Unknown item";
        if (value.length() <= maxLength) return value;
        return value.substring(0, Math.max(0, maxLength - 3)) + "...";
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (!AutoToolAccess.isAvailable(mc)) { closeToParent(); return; }
        drawRect(0, 0, width, height, 0x99000000);
        drawRect(left, top, left + panelWidth, top + panelHeight, 0xF00B0D12);
        drawRect(left, top, left + panelWidth, top + 2, 0xFFFFC83D);
        drawString(fontRendererObj, "AUTOTOOL  •  " + (page + 1) + "/4", left + 16, top + 12, 0xFFF2F3F5);
        drawString(fontRendererObj, AutoToolSettings.getProfileDescription(), left + 16, top + 27, 0xFFB8BDC8);
        if (page == 2) {
            drawString(fontRendererObj, "AutoTool automatically selects the best tool for each block.",
                    left + 16, top + 126, 0xFFB8BDC8);
            drawString(fontRendererObj, "Advanced command: /autotool prefer <block> <item|clear>",
                    left + 16, top + 140, 0xFFB8BDC8);
        } else if (page == 3) {
            drawString(fontRendererObj, "Select an item from your hotbar to exclude or include it.",
                    left + 16, top + 42, 0xFFB8BDC8);
            boolean hasItems = false;
            for (int slot = 0; slot < 9; slot++) {
                ItemStack stack = mc.thePlayer.inventory.getStackInSlot(slot);
                if (stack != null && stack.getItem() != null) { hasItems = true; break; }
            }
            if (!hasItems) {
                drawString(fontRendererObj, "Your hotbar is empty.", left + 16, top + 65, 0xFFB8BDC8);
            }
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void closeToParent() {
        if (mc != null) mc.displayGuiScreen(parent);
    }

    private static String onOff(boolean value) { return value ? "On" : "Off"; }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}