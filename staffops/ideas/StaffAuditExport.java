package com.alpharip.staffmod.staffops.ideas;

import java.util.List;

/**
 * Formats supplied local audit entries as plain text; it does not access files
 * or export authoritative/server audit data.
 * TODO(Supabase): only export shared records after access checks and an audit
 * export policy are defined.
 */
public final class StaffAuditExport {
    public static final String FEATURE_NAME = "Staff Audit Export";

    public String formatPlainText(List<String> entries) {
        if (entries == null || entries.isEmpty()) {
            return "";
        }
        StringBuilder output = new StringBuilder();
        for (String entry : entries) {
            if (entry != null && !entry.trim().isEmpty()) {
                if (output.length() > 0) {
                    output.append(System.lineSeparator());
                }
                output.append(entry.trim());
            }
        }
        return output.toString();
    }
}
