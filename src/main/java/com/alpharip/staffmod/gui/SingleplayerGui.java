// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.singleplayer.worldedit.WorldEditGui;
import com.alpharip.staffmod.singleplayer.autotool.AutoToolGui;
import com.alpharip.staffmod.singleplayer.autotool.AutoToolSettings;
import com.alpharip.staffmod.singleplayer.treeexcavate.TreeExcavateGui;
import com.alpharip.staffmod.singleplayer.autoreplant.AutoReplantGui;
import com.alpharip.staffmod.singleplayer.alphahomes.AlphaHomesGui;
import com.alpharip.staffmod.singleplayer.privatevault.AlphaPrivateVaultsGui;
import com.alpharip.staffmod.singleplayer.inventorysorter.InventorySorterGui;
import com.alpharip.staffmod.singleplayer.treeexcavate.TreeExcavateSettings;
import com.alpharip.staffmod.singleplayer.autoreplant.AutoReplantSettings;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleFeature;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public final class SingleplayerGui extends AlphaGuiScreen {
    private static final int BACK = 1;
    private static final int WORLD_EDIT = 2;
    private static final int AUTO_TOOL = 3;
    private static final int ALPHA_HOMES = 4;
    private static final int PRIVATE_VAULTS = 5;
    private static final int TREE_EXCAVATE = 6;
    private static final int AUTO_REPLANT = 7;
    private static final int INVENTORY_SORTER = 8;
    private static final int ACCENT = 0xFFFFD866;

    private final GuiScreen parent;
    private int panelLeft, panelRight, panelTop, panelBottom;
    private int tileWidth;

    public SingleplayerGui(GuiScreen parent) {
        this.parent = parent;
    }

    private boolean hasActiveSingleplayerWorld() {
        Minecraft minecraft = Minecraft.getMinecraft();
        return minecraft != null && minecraft.isSingleplayer()
                && minecraft.thePlayer != null && minecraft.theWorld != null
                && minecraft.getIntegratedServer() != null;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        if (!hasActiveSingleplayerWorld()) {
            if (mc != null) mc.displayGuiScreen(parent);
            return;
        }

        panelLeft = Math.max(12, width / 2 - Math.min(230, width / 2 - 12));
        panelRight = Math.min(width - 12, panelLeft + 460);
        panelTop = Math.max(12, height / 2 - Math.min(150, height / 2 - 12));
        panelBottom = Math.min(height - 12, panelTop + 300);

        int gap = 8;
        int columns = 4;
        tileWidth = (panelRight - panelLeft - 24 - gap * (columns - 1)) / columns;
        int tileHeight = 36;
        int tileY = panelTop + 68;
        int firstTileX = panelLeft + 12;

        buttonList.add(new AlphaButton(BACK, panelLeft + 10, panelBottom - 29, 90, 20, "BACK"));
        AlphaModule worldEdit = AlphaModuleRegistry.find("world-edit");
        boolean worldEditEnabled = worldEdit != null && worldEdit.isEnabled();
        buttonList.add(new FeatureTileButton(WORLD_EDIT, firstTileX, tileY,
                tileWidth, tileHeight, "World Edit", "◆",
                worldEditEnabled ? "ON" : "OFF", worldEditEnabled, true));
        buttonList.add(new FeatureTileButton(AUTO_TOOL, firstTileX + tileWidth + gap, tileY,
                tileWidth, tileHeight, "AutoTool", "◆",
                AutoToolSettings.isEnabled() ? "ON" : "OFF", AutoToolSettings.isEnabled(), true));
        buttonList.add(new FeatureTileButton(ALPHA_HOMES, firstTileX + (tileWidth + gap) * 2, tileY,
                tileWidth, tileHeight, "AlphaHomes", "◆", null, false, true));
        buttonList.add(new FeatureTileButton(PRIVATE_VAULTS, firstTileX + (tileWidth + gap) * 3, tileY,
                tileWidth, tileHeight, "Private Vaults", "◆", null, false, true));
        boolean treeExcavateEnabled = TreeExcavateSettings.isEnabled();
        buttonList.add(new FeatureTileButton(TREE_EXCAVATE, firstTileX, tileY + tileHeight + gap,
                tileWidth, tileHeight, "Tree Excavate", "◆",
                treeExcavateEnabled ? "ON" : "OFF", treeExcavateEnabled, true));
        boolean autoReplantEnabled = AutoReplantSettings.isEnabled();
        buttonList.add(new FeatureTileButton(AUTO_REPLANT, firstTileX + tileWidth + gap,
                tileY + tileHeight + gap, tileWidth, tileHeight, "Auto Replant", "◆",
                autoReplantEnabled ? "ON" : "OFF", autoReplantEnabled, true));
        AlphaModuleFeature inventorySorter = AlphaModuleRegistry.findFeature("inventory-sorter");
        boolean inventorySorterEnabled = inventorySorter != null && inventorySorter.isEnabled();
        buttonList.add(new FeatureTileButton(INVENTORY_SORTER, firstTileX + (tileWidth + gap) * 2,
                tileY + tileHeight + gap, tileWidth, tileHeight, "Inventory Sorter", "◆",
                inventorySorterEnabled ? "ON" : "OFF", inventorySorterEnabled, true));
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (!hasActiveSingleplayerWorld() && mc != null) {
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == null) return;
        if (button instanceof FeatureTileButton
                && ((FeatureTileButton) button).wasSwitchClicked()) {
            if (button.id == WORLD_EDIT) {
                AlphaModule worldEdit = AlphaModuleRegistry.find("world-edit");
                if (worldEdit != null) worldEdit.setEnabled(!worldEdit.isEnabled());
            } else if (button.id == AUTO_TOOL) {
                AutoToolSettings.setEnabled(!AutoToolSettings.isEnabled());
            } else if (button.id == TREE_EXCAVATE) {
                TreeExcavateSettings.setEnabled(!TreeExcavateSettings.isEnabled());
            } else if (button.id == AUTO_REPLANT) {
                AutoReplantSettings.setEnabled(!AutoReplantSettings.isEnabled());
            } else if (button.id == INVENTORY_SORTER) {
                AlphaModuleFeature inventorySorter = AlphaModuleRegistry.findFeature("inventory-sorter");
                if (inventorySorter != null) inventorySorter.setEnabled(!inventorySorter.isEnabled());
            }
            initGui();
            return;
        }
        if (button.id == BACK) {
            mc.displayGuiScreen(parent);
        } else if (button.id == WORLD_EDIT) {
            if (hasActiveSingleplayerWorld()) mc.displayGuiScreen(new WorldEditGui(this));
        } else if (button.id == AUTO_TOOL) {
            if (hasActiveSingleplayerWorld()) mc.displayGuiScreen(new AutoToolGui(this));
        } else if (button.id == ALPHA_HOMES) {
            if (hasActiveSingleplayerWorld()) mc.displayGuiScreen(new AlphaHomesGui(this));
        } else if (button.id == PRIVATE_VAULTS) {
            if (hasActiveSingleplayerWorld()) mc.displayGuiScreen(new AlphaPrivateVaultsGui(this));
        } else if (button.id == TREE_EXCAVATE) {
            if (hasActiveSingleplayerWorld()) mc.displayGuiScreen(new TreeExcavateGui(this));
        } else if (button.id == AUTO_REPLANT) {
            if (hasActiveSingleplayerWorld()) mc.displayGuiScreen(new AutoReplantGui(this));
        } else if (button.id == INVENTORY_SORTER) {
            if (hasActiveSingleplayerWorld()) mc.displayGuiScreen(new InventorySorterGui(this));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x18000000);
        drawRect(panelLeft, panelTop, panelRight, panelBottom, 0xD90A0C11);
        drawRect(panelLeft, panelTop, panelRight, panelTop + 2, ACCENT);
        drawCenteredString(fontRendererObj, "SINGLEPLAYER", width / 2,
                panelTop + 13, 0xFFF3F4F6);
        drawCenteredString(fontRendererObj, "SINGLEPLAYER MODS", width / 2, panelTop + 30, ACCENT);
        drawString(fontRendererObj, "Singleplayer-only tools", panelLeft + 12,
                panelTop + 49, 0xFFB8BDC8);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
