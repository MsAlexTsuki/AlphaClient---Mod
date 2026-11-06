package com.alpharip.staffmod.timing.combattimer;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Standalone Combat Timer prototype for Minecraft 1.8.9 / Forge.
 *
 * The timer can be started by a configurable server-chat regular expression
 * or by calling tagCombat() from a future, verified combat-signal adapter.
 * No server message format or server-specific combat duration is assumed.
 *
 * This class owns its Forge event hooks, HUD and settings screen. It does not
 * register with Alpha Client Core and does not access Supabase.
 */
public final class CombatTimer {
    public static final String NAME = "Combat Timer";
    public static final String CATEGORY = "Timing";
    public static final boolean REQUIRES_SUPABASE = false;

    private static final long DEFAULT_DURATION_MILLIS = 15000L;

    private final Minecraft mc = Minecraft.getMinecraft();
    private final Settings settings = new Settings();
    private final EventHandler eventHandler = new EventHandler();

    private boolean running;
    private long combatUntilMillis;

    /** Register this module's own Forge listener. Safe to call more than once. */
    public void start() {
        if (running) return;
        MinecraftForge.EVENT_BUS.register(eventHandler);
        running = true;
    }

    /** Unregister this module and clear the active timer. */
    public void stop() {
        if (!running) return;
        MinecraftForge.EVENT_BUS.unregister(eventHandler);
        combatUntilMillis = 0L;
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isEnabled() {
        return settings.enabled;
    }

    public void setEnabled(boolean enabled) {
        settings.enabled = enabled;
        if (!enabled) combatUntilMillis = 0L;
    }

    /**
     * Starts or refreshes the combat timer.
     * Call this only when a real combat-tag signal has been observed.
     */
    public void tagCombat() {
        tagCombat(System.currentTimeMillis());
    }

    /** Timestamp overload for deterministic local testing. */
    public void tagCombat(long nowMillis) {
        if (!settings.enabled || settings.durationMillis <= 0L) return;
        long candidate = nowMillis + settings.durationMillis;
        combatUntilMillis = Math.max(combatUntilMillis, candidate);
    }

    public long getRemainingMillis() {
        return getRemainingMillis(System.currentTimeMillis());
    }

    public long getRemainingMillis(long nowMillis) {
        return Math.max(0L, combatUntilMillis - nowMillis);
    }

    public boolean isInCombat() {
        return getRemainingMillis() > 0L;
    }

    public long getDurationMillis() {
        return settings.durationMillis;
    }

    public void setDurationSeconds(int seconds) {
        settings.durationMillis = Math.max(1, Math.min(600, seconds)) * 1000L;
    }

    public boolean isHudEnabled() {
        return settings.showHud;
    }

    public void setHudEnabled(boolean showHud) {
        settings.showHud = showHud;
    }

    /**
     * Comma-separated regular expressions used to recognize combat-tag chat
     * messages. Empty by default because server-specific text is unknown.
     */
    public String getChatPatterns() {
        return settings.chatPatterns;
    }

    public void setChatPatterns(String patterns) {
        settings.chatPatterns = patterns == null ? "" : patterns.trim();
    }

    public void openSettings(GuiScreen parent) {
        mc.displayGuiScreen(new SettingsScreen(parent));
    }

    private boolean matchesCombatMessage(String message) {
        if (message == null || settings.chatPatterns.trim().isEmpty()) return false;

        String[] expressions = settings.chatPatterns.split(",");
        for (String expression : expressions) {
            String candidate = expression.trim();
            if (candidate.isEmpty()) continue;
            try {
                if (Pattern.compile(candidate, Pattern.CASE_INSENSITIVE)
                        .matcher(message).find()) {
                    return true;
                }
            } catch (PatternSyntaxException ignored) {
                // Invalid patterns are ignored; the settings screen keeps the
                // user's text so it can be corrected without losing it.
            }
        }
        return false;
    }

    private String formatRemaining(long millis) {
        double seconds = Math.ceil(millis / 100.0D) / 10.0D;
        return String.format(java.util.Locale.ROOT, "%.1fs", seconds);
    }

    private final class EventHandler {
        @SubscribeEvent
        public void onChat(ClientChatReceivedEvent event) {
            if (!running || !settings.enabled || event == null || event.message == null) return;

            String plain = event.message.getUnformattedText();
            if (matchesCombatMessage(plain)) {
                tagCombat();
            }
        }

        @SubscribeEvent
        public void onOverlay(RenderGameOverlayEvent.Post event) {
            if (!running || !settings.enabled || !settings.showHud
                    || event.type != RenderGameOverlayEvent.ElementType.ALL
                    || mc.thePlayer == null || mc.theWorld == null) {
                return;
            }

            long remaining = getRemainingMillis();
            if (remaining <= 0L) return;

            String label = "Combat: " + formatRemaining(remaining);
            int color = remaining <= 3000L ? 0xFFFF5555 : 0xFFFFC83D;
            int x = 6;
            int y = 6;
            mc.fontRendererObj.drawStringWithShadow(label, x, y, color);
        }
    }

    private static final class Settings {
        private boolean enabled = true;
        private boolean showHud = true;
        private long durationMillis = DEFAULT_DURATION_MILLIS;

        // Intentionally blank: no server-specific message has been verified.
        private String chatPatterns = "";
    }

    /** Small module-owned settings screen; no central GUI/Core dependency. */
    private final class SettingsScreen extends GuiScreen {
        private final GuiScreen parent;
        private GuiTextField durationField;
        private GuiTextField patternsField;
        private String validationMessage = "";

        private SettingsScreen(GuiScreen parent) {
            this.parent = parent;
        }

        @Override
        public void initGui() {
            buttonList.clear();
            int center = width / 2;
            durationField = new GuiTextField(0, fontRendererObj, center - 100, 64, 200, 20);
            durationField.setMaxStringLength(3);
            durationField.setText(String.valueOf((int) (settings.durationMillis / 1000L)));

            patternsField = new GuiTextField(1, fontRendererObj, center - 140, 112, 280, 20);
            patternsField.setMaxStringLength(240);
            patternsField.setText(settings.chatPatterns);

            buttonList.add(new GuiButton(2, center - 100, 150, 200, 20,
                    "HUD: " + (settings.showHud ? "ON" : "OFF")));
            buttonList.add(new GuiButton(3, center - 100, 176, 200, 20,
                    "Module: " + (settings.enabled ? "ON" : "OFF")));
            buttonList.add(new GuiButton(4, center - 100, 210, 98, 20, "Save"));
            buttonList.add(new GuiButton(5, center + 2, 210, 98, 20, "Cancel"));
        }

        @Override
        protected void actionPerformed(GuiButton button) {
            if (button.id == 2) {
                settings.showHud = !settings.showHud;
                button.displayString = "HUD: " + (settings.showHud ? "ON" : "OFF");
            } else if (button.id == 3) {
                setEnabled(!settings.enabled);
                button.displayString = "Module: " + (settings.enabled ? "ON" : "OFF");
            } else if (button.id == 4) {
                saveSettings();
                if (validationMessage.isEmpty()) mc.displayGuiScreen(parent);
            } else if (button.id == 5) {
                mc.displayGuiScreen(parent);
            }
        }

        private void saveSettings() {
            validationMessage = "";
            String durationText = durationField.getText().trim();
            try {
                int seconds = Integer.parseInt(durationText);
                if (seconds < 1 || seconds > 600) {
                    validationMessage = "Duration must be between 1 and 600 seconds.";
                    return;
                }
                settings.durationMillis = seconds * 1000L;
            } catch (NumberFormatException exception) {
                validationMessage = "Enter a valid duration in seconds.";
                return;
            }

            settings.chatPatterns = patternsField.getText().trim();
            if (!settings.chatPatterns.isEmpty()) {
                for (String expression : settings.chatPatterns.split(",")) {
                    if (expression.trim().isEmpty()) continue;
                    try {
                        Pattern.compile(expression.trim(), Pattern.CASE_INSENSITIVE);
                    } catch (PatternSyntaxException exception) {
                        validationMessage = "Invalid chat pattern: " + exception.getDescription();
                        return;
                    }
                }
            }
        }

        @Override
        protected void keyTyped(char typedChar, int keyCode) {
            if (durationField.textboxKeyTyped(typedChar, keyCode)) return;
            if (patternsField.textboxKeyTyped(typedChar, keyCode)) return;
            super.keyTyped(typedChar, keyCode);
        }

        @Override
        protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
            super.mouseClicked(mouseX, mouseY, mouseButton);
            durationField.mouseClicked(mouseX, mouseY, mouseButton);
            patternsField.mouseClicked(mouseX, mouseY, mouseButton);
        }

        @Override
        public void updateScreen() {
            durationField.updateCursorCounter();
            patternsField.updateCursorCounter();
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            drawDefaultBackground();
            drawCenteredString(fontRendererObj, "Combat Timer Settings", width / 2,  twenty(), 0xFFFFFF);
            drawString(fontRendererObj, "Combat duration (seconds)", width / 2 - 100, 52, 0xCCCCCC);
            durationField.drawTextBox();
            drawString(fontRendererObj, "Chat trigger regex (comma-separated; blank = manual only)",
                    width / 2 - 140, 100, 0xCCCCCC);
            patternsField.drawTextBox();
            if (!validationMessage.isEmpty()) {
                drawCenteredString(fontRendererObj, validationMessage, width / 2, 238, 0xFFFF5555);
            }
            drawCenteredString(fontRendererObj, "Server-specific trigger text is not preconfigured.",
                    width / 2, 250, 0x999999);
            super.drawScreen(mouseX, mouseY, partialTicks);
        }

        private int twenty() {
            return 20;
        }
    }

    // TODO(Supabase): Keep settings local in this prototype. If account-synced
    // preferences are requested later, document the required schema and RLS
    // policies first; do not add Supabase calls here.
    // TODO(Integration): Register/start this module from an approved module
    // entry point when it is ported. No Alpha Client Core changes are included.
}
