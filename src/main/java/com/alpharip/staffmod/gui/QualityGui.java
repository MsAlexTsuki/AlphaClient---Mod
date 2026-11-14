// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.BlockPlacementPreview.BlockPlacementPreviewSettings;
import com.alpharip.staffmod.hud.SessionTimerHud;
import com.alpharip.staffmod.hud.PlaytimeTrackerHud;
import com.alpharip.staffmod.hud.AfkTimerHud;
import com.alpharip.staffmod.quality.FullbrightMod;
import com.alpharip.staffmod.quality.AutoGGMod;
import com.alpharip.staffmod.quality.AutoReconnectMod;
import com.alpharip.staffmod.quality.anvilcost.AnvilCostMod;
import com.alpharip.staffmod.quality.enchantingname.EnchantingNameMod;
import com.alpharip.staffmod.inventorysearch.InventorySearchGui;
import com.alpharip.staffmod.inventorysearch.InventorySearchSettings;
import com.alpharip.staffmod.founderfun.eastereggs.EasterEggSettingsGui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.io.IOException;

/** Browser for AlphaClient Quality of Life features. */
public final class QualityGui extends AlphaGuiScreen {
    private static final int BACK = 1;
    private static final int AFK_TIMER = 2;
    private static final int FULLBRIGHT = 3;
    private static final int SESSION_TIMER = 4;
    private static final int PLAYTIME_TRACKER = 5;
    private static final int AUTO_GG = 6;
    private static final int AUTO_RECONNECT = 7;
    private static final int SEARCH_INV = 8;
    private static final int ANVIL_COST = 9;
    private static final int ENCHANTING_NAMES = 10;
    private static final int EASTER_EGGS = 11;
    private static final int BLOCK_PLACEMENT_PREVIEW = 12;
    private static final int AFK_TIMER_INDEX = 7;
    private static final int SESSION_TIMER_INDEX = 8;
    private static final int PLAYTIME_TRACKER_INDEX = 9;
    private static final int AUTO_RECONNECT_INDEX = 11;
    private static final int FULLBRIGHT_INDEX = 0;
    private static final int ANVIL_COST_INDEX = 19;
    private static final int ENCHANTING_NAMES_INDEX = 20;
    private static final int BLOCK_PLACEMENT_PREVIEW_INDEX = 22;
    private static final int ACCENT = 0xFFFFD866;
    private static final String[] FEATURES = {
            "Fullbright", "Alpha Module Manager", "Mod Profiles", "Auto GG", "Keybind Profiles",
            "Auto Respawn", "AlphaClient Core Experience", "AFK Timer", "Session Timer", "Playtime Tracker",
            "Config Manager", "Auto Reconnect", "Alpha System Monitor", "Alpha Crash Reporter",
            "Alpha Update Checker", "Alpha Control Center", "Keybind Manager", "Alpha Feature Flags", "Search Inv", "Anvil Cost", "Enchanting Names", "Easter Eggs", "Block Placement Preview"
    };

    private final GuiScreen parent;
    private int scrollOffset;
    private int visibleRows;
    private int panelLeft, panelRight, panelTop, panelBottom;

    public QualityGui(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        panelLeft = Math.max(12, width / 2 - Math.min(230, width / 2 - 12));
        panelRight = Math.min(width - 12, panelLeft + 460);
        panelTop = Math.max(12, height / 2 - Math.min(150, height / 2 - 12));
        panelBottom = Math.min(height - 12, panelTop + 300);
        int gap = 8;
        int columns = 3;
        int tileWidth = (panelRight - panelLeft - 24 - gap * (columns - 1)) / columns;
        int tileHeight = 36;
        int rowHeight = tileHeight + gap;
        visibleRows = Math.max(1, (panelBottom - panelTop - 100) / rowHeight);
        int totalRows = (FEATURES.length + columns - 1) / columns;
        int maxScroll = Math.max(0, totalRows - visibleRows);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
        buttonList.add(new AlphaButton(BACK, panelLeft + 10, panelBottom - 29, 90, 20, "BACK"));
        int startIndex = scrollOffset * columns;
        int endIndex = Math.min(FEATURES.length, startIndex + visibleRows * columns);
        for (int i = startIndex; i < endIndex; i++) {
            int local = i - startIndex;
            int x = panelLeft + 12 + (local % columns) * (tileWidth + gap);
            int y = panelTop + 68 + (local / columns) * rowHeight;
            if (i == PLAYTIME_TRACKER_INDEX) {
                boolean on = PlaytimeTrackerHud.isEnabled();
                buttonList.add(new FeatureTileButton(PLAYTIME_TRACKER, x, y,
                        tileWidth, tileHeight, "Playtime Tracker", "◆", on ? "ON" : "OFF", on, true));
            } else if (i == 3) {
                boolean on = AutoGGMod.isEnabled();
                buttonList.add(new FeatureTileButton(AUTO_GG, x, y, tileWidth, tileHeight,
                        "Auto GG", "◆", on ? "ON" : "OFF", on, true));
            } else if (i == AUTO_RECONNECT_INDEX) {
                boolean on = AutoReconnectMod.isEnabled();
                buttonList.add(new FeatureTileButton(AUTO_RECONNECT, x, y, tileWidth, tileHeight,
                        "Auto Reconnect", "◆", on ? "ON" : "OFF", on, true));
            } else if (i == FULLBRIGHT_INDEX) {
                boolean on = FullbrightMod.isEnabled();
                buttonList.add(new FeatureTileButton(FULLBRIGHT, x, y, tileWidth, tileHeight,
                        "Fullbright", "◆", on ? "ON" : "OFF", on, true));
            } else if (i == 18) {
                boolean on = InventorySearchSettings.isEnabled();
                buttonList.add(new FeatureTileButton(SEARCH_INV, x, y, tileWidth, tileHeight,
                        "Search Inv", "◆", on ? "ON" : "OFF", on, true));
            } else if (i == ANVIL_COST_INDEX) {
                boolean on = AnvilCostMod.isEnabled();
                buttonList.add(new FeatureTileButton(ANVIL_COST, x, y, tileWidth, tileHeight,
                        "Anvil Cost", "◆", on ? "ON" : "OFF", on, true));
            } else if (i == ENCHANTING_NAMES_INDEX) {
                boolean on = EnchantingNameMod.isEnabled();
                buttonList.add(new FeatureTileButton(ENCHANTING_NAMES, x, y, tileWidth, tileHeight,
                        "Enchanting Names", "◆", on ? "ON" : "OFF", on, true));
            } else if (i == BLOCK_PLACEMENT_PREVIEW_INDEX) {
                boolean on = BlockPlacementPreviewSettings.isEnabled();
                buttonList.add(new FeatureTileButton(BLOCK_PLACEMENT_PREVIEW, x, y,
                        tileWidth, tileHeight, "Block Placement Preview", "◆", on ? "ON" : "OFF", on, true));
            } else if (i == FEATURES.length - 2) {
                buttonList.add(new FeatureTileButton(EASTER_EGGS, x, y, tileWidth, tileHeight,
                        "Easter Eggs", "◆", null, false, true));
            } else if (i == AFK_TIMER_INDEX) {
                boolean on = AfkTimerHud.isEnabled();
                buttonList.add(new FeatureTileButton(AFK_TIMER, x, y, tileWidth, tileHeight,
                        "AFK Timer", "◆", on ? "ON" : "OFF", on, true));
            } else if (i == SESSION_TIMER_INDEX) {
                boolean on = SessionTimerHud.isEnabled();
                buttonList.add(new FeatureTileButton(SESSION_TIMER, x, y, tileWidth, tileHeight,
                        "Session Timer", "◆", on ? "ON" : "OFF", on, true));
            } else {
                FeatureTileButton planned = new FeatureTileButton(1000 + i, x, y,
                        tileWidth, tileHeight, FEATURES[i], "◆", "PLANNED", false, false);
                buttonList.add(planned);
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x18000000);
        drawRect(panelLeft, panelTop, panelRight, panelBottom, 0xD90A0C11);
        drawRect(panelLeft, panelTop, panelRight, panelTop + 2, ACCENT);
        drawCenteredString(fontRendererObj, "QUALITY", width / 2, panelTop + 13, 0xFFF3F4F6);
        drawCenteredString(fontRendererObj, "QUALITY OF LIFE FEATURES", width / 2, panelTop + 30, ACCENT);
        drawString(fontRendererObj, "Features and roadmap ideas", panelLeft + 12, panelTop + 49, 0xFFB8BDC8);

        // Feature rows are rendered by AlphaButton, matching StaffMods -> General.
        if (FEATURES.length > visibleRows) {
            drawString(fontRendererObj, "Scroll to view all " + FEATURES.length + " ideas", panelLeft + 112,
                    panelBottom - 23, 0xFF8E95A3);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button instanceof FeatureTileButton
                && ((FeatureTileButton) button).wasSwitchClicked()) {
            if (button.id == AUTO_GG) {
                AutoGGMod.setEnabled(!AutoGGMod.isEnabled());
            } else if (button.id == AUTO_RECONNECT) {
                AutoReconnectMod.setEnabled(!AutoReconnectMod.isEnabled());
            } else if (button.id == FULLBRIGHT) {
                FullbrightMod.setEnabled(!FullbrightMod.isEnabled());
            } else if (button.id == SEARCH_INV) {
                InventorySearchSettings.setEnabled(!InventorySearchSettings.isEnabled());
            } else if (button.id == ANVIL_COST) {
                AnvilCostMod.setEnabled(!AnvilCostMod.isEnabled());
            } else if (button.id == ENCHANTING_NAMES) {
                EnchantingNameMod.setEnabled(!EnchantingNameMod.isEnabled());
            } else if (button.id == SESSION_TIMER) {
                SessionTimerHud.setEnabled(!SessionTimerHud.isEnabled());
            } else if (button.id == AFK_TIMER) {
                AfkTimerHud.setEnabled(!AfkTimerHud.isEnabled());
            } else if (button.id == PLAYTIME_TRACKER) {
                PlaytimeTrackerHud.setEnabled(!PlaytimeTrackerHud.isEnabled());
            } else if (button.id == BLOCK_PLACEMENT_PREVIEW) {
                BlockPlacementPreviewSettings.setEnabled(!BlockPlacementPreviewSettings.isEnabled());
            }
            initGui();
            return;
        }
        if (button.id == BACK) {
            mc.displayGuiScreen(parent);
        } else if (button.id == AFK_TIMER) {
            mc.displayGuiScreen(new AfkTimerSettingsGui(this));
        } else if (button.id == SESSION_TIMER) {
            mc.displayGuiScreen(new SessionTimerSettingsGui(this));
        } else if (button.id == PLAYTIME_TRACKER) {
            mc.displayGuiScreen(new PlaytimeTrackerSettingsGui(this));
        } else if (button.id == AUTO_GG) {
            mc.displayGuiScreen(new AutoGGSettingsGui(this));
        } else if (button.id == AUTO_RECONNECT) {
            mc.displayGuiScreen(new AutoReconnectSettingsGui(this));
        } else if (button.id == FULLBRIGHT) {
            FullbrightMod.setEnabled(!FullbrightMod.isEnabled());
            initGui();
        } else if (button.id == SEARCH_INV) {
            mc.displayGuiScreen(new InventorySearchGui(this));
        } else if (button.id == ANVIL_COST) {
            mc.displayGuiScreen(new AnvilCostSettingsGui(this));
        } else if (button.id == ENCHANTING_NAMES) {
            mc.displayGuiScreen(new EnchantingNameSettingsGui(this));
        } else if (button.id == EASTER_EGGS) {
            mc.displayGuiScreen(new EasterEggSettingsGui(this));
        } else if (button.id == BLOCK_PLACEMENT_PREVIEW) {
            mc.displayGuiScreen(new BlockPlacementPreviewSettingsGui(this));
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            int columns = 3;
            int totalRows = (FEATURES.length + columns - 1) / columns;
            int maxScroll = Math.max(0, totalRows - visibleRows);
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset + (wheel < 0 ? 1 : -1)));
            initGui();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
