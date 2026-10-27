package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Foundation for user-defined server and game-mode shortcuts.
 * Screen integration and connection/command dispatch are not connected yet.
 */
public final class QuickJoinMenu {
    public static final String FEATURE_NAME = "Quick Join Menu";

    public static final class Entry {
        private final String label;
        private final String address;
        private final String command;

        public Entry(String label, String address, String command) {
            this.label = clean(label);
            this.address = clean(address);
            this.command = normalizeCommand(command);
        }

        public String getLabel() { return label; }
        public String getAddress() { return address; }
        public String getCommand() { return command; }

        public boolean isValid() {
            return !label.isEmpty() && (isValidAddress(address) || isValidCommand(command));
        }
    }

    private boolean enabled;
    private final List<Entry> entries = new ArrayList<Entry>();

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean value) { enabled = value; }

    public boolean addEntry(Entry entry) {
        if (entry == null || !entry.isValid() || find(entry.getLabel()) != null) return false;
        entries.add(entry);
        return true;
    }

    public boolean removeEntry(String label) {
        Entry existing = find(label);
        return existing != null && entries.remove(existing);
    }

    public List<Entry> getEntries() {
        return Collections.unmodifiableList(new ArrayList<Entry>(entries));
    }

    private Entry find(String label) {
        if (label == null) return null;
        for (Entry entry : entries) if (entry.getLabel().equalsIgnoreCase(label.trim())) return entry;
        return null;
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private static String normalizeCommand(String value) {
        String result = clean(value);
        if (!result.isEmpty() && !result.startsWith("/")) result = "/" + result;
        return result;
    }

    private static boolean isValidCommand(String value) {
        return value.length() > 1 && value.startsWith("/")
                && value.indexOf('\n') < 0 && value.indexOf('\r') < 0;
    }

    private static boolean isValidAddress(String value) {
        return value.matches("(?i)[a-z0-9.-]+(?::[0-9]{1,5})?");
    }
}
