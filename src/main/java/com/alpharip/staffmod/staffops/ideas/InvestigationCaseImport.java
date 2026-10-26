package com.alpharip.staffmod.staffops.ideas;

import java.util.LinkedHashMap;
import java.util.Map;

/** Parses simple key:value text into local case fields; it does not update a case automatically. */
public final class InvestigationCaseImport {
    public Map<String, String> parse(String text) {
        Map<String, String> result = new LinkedHashMap<String, String>();
        if (text == null || text.isEmpty()) return result;
        String[] lines = text.split("\\r?\\n");
        for (String line : lines) {
            int separator = line.indexOf(':');
            if (separator <= 0) continue;
            String key = line.substring(0, separator).trim();
            if (!key.isEmpty()) result.put(key, line.substring(separator + 1).trim());
        }
        return result;
    }
}