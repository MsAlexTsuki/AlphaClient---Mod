package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Foundation for configurable Hypixel Quick Play shortcuts.
 * Server detection, command dispatch, and menu integration are not connected yet.
 */
public final class HypixelQuickPlay {
    public static final String FEATURE_NAME = "Hypixel Quick Play";

    public static final class Shortcut {
        private final String name;
        private final String command;

        public Shortcut(String name, String command) {
            this.name = name == null ? "" : name.trim();
            this.command = normalizeCommand(command);
        }

        public String getName() { return name; }
        public String getCommand() { return command; }
        public boolean isValid() {
            return !name.isEmpty() && command.startsWith("/") && command.length() > 1
                    && command.indexOf('\n') < 0 && command.indexOf('\r') < 0;
        }

        private static String normalizeCommand(String value) {
            if (value == null) return "";
            String command = value.trim();
            if (!command.isEmpty() && !command.startsWith("/")) command = "/" + command;
            return command;
        }
    }

    private boolean enabled = false;
    private final List<Shortcut> shortcuts = new ArrayList<Shortcut>();

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean value) { enabled = value; }

    public void addShortcut(Shortcut shortcut) {
        if (shortcut != null && shortcut.isValid()) shortcuts.add(shortcut);
    }

    public boolean removeShortcut(String name) {
        if (name == null) return false;
        for (int i = 0; i < shortcuts.size(); i++) {
            if (name.equalsIgnoreCase(shortcuts.get(i).getName())) {
                shortcuts.remove(i);
                return true;
            }
        }
        return false;
    }

    public List<Shortcut> getShortcuts() {
        return Collections.unmodifiableList(new ArrayList<Shortcut>(shortcuts));
    }
}
