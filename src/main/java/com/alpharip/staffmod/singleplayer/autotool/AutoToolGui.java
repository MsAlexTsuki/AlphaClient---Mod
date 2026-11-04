package com.alpharip.staffmod.singleplayer.autotool;

import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.Locale;

/** Multi-page local settings and item-management screen for AutoTool. */
public final class AutoToolGui extends GuiScreen {
    private final GuiScreen parent;
    private int page;
    private int left;
    private int top;
    private int panelWidth;
    private int panelHeight;
    private GuiTextField blockIdField;
    private GuiTextField preferredItemField;
    private GuiTextField ignoredItemField;
    private String itemManagementStatus = "";

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
            int fieldWidth = Math.max(1, panelWidth - 32);
            blockIdField = new GuiTextField(101, fontRendererObj, left + 16, top + 53, fieldWidth, 18);
            preferredItemField = new GuiTextField(102, fontRendererObj, left + 16, top + 94, fieldWidth, 18);
            ignoredItemField = new GuiTextField(103, fontRendererObj, left + 16, top + 146, fieldWidth, 18);
            blockIdField.setMaxStringLength(128);
            preferredItemField.setMaxStringLength(128);
            ignoredItemField.setMaxStringLength(128);
            blockIdField.setFocused(true);
            buttonList.add(new GuiButton(70, left + 16, top + 177, 150, 20, "Save preference"));
            buttonList.add(new GuiButton(71, left + 174, top + 177, 150, 20, "Clear preference"));
            buttonList.add(new GuiButton(72, left + 16, top + 201, buttonWidth, 20, "Toggle ignored item"));
            buttonList.add(new GuiButton(73, left + 16, top + 225, 150, 20, "Copy overrides to global"));
            buttonList.add(new GuiButton(74, left + 174, top + 225, 150, 20, "Reset world overrides"));
        } else {
            String[] options = optionsForPage();
            for (int index = 0; index < options.length; index++) {
                buttonList.add(new GuiButton(index + 1, left + 16, top + 43 + index * 24,
                        buttonWidth, 20, labelFor(options[index])));
            }
        }
        int footerY = top + panelHeight - 25;
        if (page > 0) {
            buttonList.add(new GuiButton(91, left + 16, footerY, 68, 18, "Back"));
            buttonList.add(new GuiButton(92, left + 90, footerY, 68, 18, "Menu"));
        } else {
            buttonList.add(new GuiButton(92, left + 16, footerY, 76, 18, "Menu"));
        }
        if (page < 3) buttonList.add(new GuiButton(90, left + panelWidth - 92, footerY, 76, 18, "More"));
        else buttonList.add(new GuiButton(90, left + panelWidth - 92, footerY, 76, 18, "Done"));
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
        } else if (page == 3 && button.id >= 70 && button.id <= 72) {
            manageItems(button.id);
        } else if (page == 3 && button.id == 73) {
            itemManagementStatus = AutoToolSettings.copyWorldOverridesToGlobalDefaults()
                    ? "World overrides copied to global defaults."
                    : "No editable world profile, or settings could not be read.";
        } else if (page == 3 && button.id == 74) {
            itemManagementStatus = AutoToolSettings.resetCurrentWorldOverrides()
                    ? "World overrides reset to global defaults."
                    : "World profile could not be reset safely.";
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

    private void manageItems(int action) {
        if (action == 70 || action == 71) {
            ResourceLocation blockId = parseId(blockIdField.getText());
            if (blockId == null || !(Block.blockRegistry.getObject(blockId) instanceof Block)) {
                itemManagementStatus = "Enter a valid block registry ID.";
                return;
            }
            if (action == 71) {
                AutoToolSettings.setPreferredItem(blockId.toString(), null);
                itemManagementStatus = "Preference cleared for " + blockId + ".";
                return;
            }
            ResourceLocation itemId = parseId(preferredItemField.getText());
            if (itemId == null || !(Item.itemRegistry.getObject(itemId) instanceof Item)) {
                itemManagementStatus = "Enter a valid item registry ID.";
                return;
            }
            AutoToolSettings.setPreferredItem(blockId.toString(), itemId.toString());
            itemManagementStatus = "Preference saved for " + blockId + ".";
        } else {
            ResourceLocation itemId = parseId(ignoredItemField.getText());
            if (itemId == null || !(Item.itemRegistry.getObject(itemId) instanceof Item)) {
                itemManagementStatus = "Enter a valid item registry ID.";
                return;
            }
            boolean ignored = !AutoToolSettings.isItemIgnored(itemId.toString());
            AutoToolSettings.setItemIgnored(itemId.toString(), ignored);
            itemManagementStatus = ignored ? itemId + " added to ignore list." : itemId + " removed from ignore list.";
        }
    }

    private static ResourceLocation parseId(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try { return new ResourceLocation(value.trim().toLowerCase(Locale.ROOT)); }
        catch (RuntimeException invalid) { return null; }
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
            drawString(fontRendererObj, "Item preferences and ignore list: use page 4.", left + 16, top + 126, 0xFFB8BDC8);
            drawString(fontRendererObj, "/autotool prefer <block> <item|clear>", left + 16, top + 140, 0xFFB8BDC8);
            drawString(fontRendererObj, "/autotool ignore <add|remove> <item>", left + 16, top + 153, 0xFFB8BDC8);
        } else if (page == 3) {
            drawString(fontRendererObj, "Block registry ID", left + 16, top + 42, 0xFFB8BDC8);
            drawString(fontRendererObj, "Preferred item registry ID", left + 16, top + 83, 0xFFB8BDC8);
            drawString(fontRendererObj, "Item to add/remove from ignore list", left + 16, top + 135, 0xFFB8BDC8);
            blockIdField.drawTextBox();
            preferredItemField.drawTextBox();
            ignoredItemField.drawTextBox();
            if (!itemManagementStatus.isEmpty()) {
                drawString(fontRendererObj, itemManagementStatus, left + 16, top + 165, 0xFFFFC83D);
            }
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (page == 3) {
            blockIdField.mouseClicked(mouseX, mouseY, mouseButton);
            preferredItemField.mouseClicked(mouseX, mouseY, mouseButton);
            ignoredItemField.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) { closeToParent(); return; }
        if (page == 3) {
            blockIdField.textboxKeyTyped(typedChar, keyCode);
            preferredItemField.textboxKeyTyped(typedChar, keyCode);
            ignoredItemField.textboxKeyTyped(typedChar, keyCode);
        }
        super.keyTyped(typedChar, keyCode);
    }

    private void closeToParent() {
        if (mc != null) mc.displayGuiScreen(parent);
    }

    private static String onOff(boolean value) { return value ? "On" : "Off"; }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}