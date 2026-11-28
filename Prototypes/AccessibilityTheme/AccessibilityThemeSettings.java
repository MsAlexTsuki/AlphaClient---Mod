package com.alpharip.staffmod.accessibilitytheme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Local prototype model based on the AccessibilityTheme idea source.
 * The source currently defines only an enabled flag and a generic record list.
 */
public final class AccessibilityThemeSettings {
    private boolean enabled = true;
    private final List<String> records = new ArrayList<String>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void addRecord(String record) {
        if (record != null && !record.trim().isEmpty()) {
            records.add(record.trim());
        }
    }

    public List<String> getRecords() {
        return Collections.unmodifiableList(new ArrayList<String>(records));
    }

    public void clearRecords() {
        records.clear();
    }

    // TODO: Define the actual accessibility-theme options before implementing visual changes.
    // TODO(Supabase): Only add remote settings storage if a future approved specification requires it.
}
