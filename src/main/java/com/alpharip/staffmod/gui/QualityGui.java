package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.hud.SessionTimerHud;
import com.alpharip.staffmod.hud.PlaytimeTrackerHud;
import com.alpharip.staffmod.quality.FullbrightMod;
import com.alpharip.staffmod.quality.AutoGGMod;
import com.alpharip.staffmod.quality.AutoReconnectMod;
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
    private static final int AFK_TIMER_INDEX = 7;
    private static final int SESSION_TIMER_INDEX = 8;
    private static final int PLAYTIME_TRACKER_INDEX = 9;
    private static final int AUTO_RECONNECT_INDEX = 11;
    private static final int FULLBRIGHT_INDEX = 0;
    private static final int ACCENT = 0xFFFFD866;
    private static final String[] FEATURES = {
            "Fullbright", "Alpha Module Manager", "Mod Profiles", "Auto GG", "Keybind Profiles",
            "Auto Respawn", "AlphaClient Core Experience", "AFK Timer", "Session Timer", "Playtime Tracker",
            "Config Manager", "Auto Reconnect", "Alpha System Monitor", "Alpha Crash Reporter",
            "Alpha Update Checker", "Alpha Control Center", "Keybind Manager", "Alpha Feature Flags"
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
        visibleRows = Math.max(1, (panelBottom - panelTop - 92) / 20);
        int maxScroll = Math.max(0, FEATURES.length - visibleRows);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
        buttonList.add(new GuiButton(BACK, panelLeft + 10, panelBottom - 29, 90, 20, "BACK"));
        for (int i = scrollOffset; i < FEATURES.length && i < scrollOffset + visibleRows; i++) {
            int y = panelTop + 68 + (i - scrollOffset) * 20;
            int buttonWidth = panelRight - panelLeft - 20;
            if (i == PLAYTIME_TRACKER_INDEX) {
                buttonList.add(new AlphaButton(PLAYTIME_TRACKER, panelLeft + 10, y,
                        buttonWidth, 17, "Playtime Tracker", true));
            } else if (i == 3) {
                buttonList.add(new AlphaButton(AUTO_GG, panelLeft + 10, y,
                        buttonWidth, 17, "Auto GG: " + (AutoGGMod.isEnabled() ? "ON" : "OFF"),
                        AutoGGMod.isEnabled()));
            } else if (i == AUTO_RECONNECT_INDEX) {
                buttonList.add(new AlphaButton(AUTO_RECONNECT, panelLeft + 10, y,
                        buttonWidth, 17, "Auto Reconnect: " + (AutoReconnectMod.isEnabled() ? "ON" : "OFF"),
                        AutoReconnectMod.isEnabled()));
            } else if (i == FULLBRIGHT_INDEX) {
                buttonList.add(new AlphaButton(FULLBRIGHT, panelLeft + 10, y,
                        buttonWidth, 17,
                        "Fullbright: " + (FullbrightMod.isEnabled() ? "ON" : "OFF"), true));
            } else if (i == AFK_TIMER_INDEX) {
                buttonList.add(new AlphaButton(AFK_TIMER, panelLeft + 10, y,
                        buttonWidth, 17, "AFK Timer", true));
            } else if (i == SESSION_TIMER_INDEX) {
                buttonList.add(new AlphaButton(SESSION_TIMER, panelLeft + 10, y,
                        buttonWidth, 17,
                        "Session Timer: " + (SessionTimerHud.isEnabled() ? "ON" : "OFF"),
                        SessionTimerHud.isEnabled()));
            } else {
                AlphaButton planned = new AlphaButton(1000 + i, panelLeft + 10, y,
                        buttonWidth, 17, FEATURES[i] + "  -  PLANNED");
                planned.enabled = false;
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
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            int maxScroll = Math.max(0, FEATURES.length - visibleRows);
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset + (wheel < 0 ? 2 : -2)));
            initGui();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
