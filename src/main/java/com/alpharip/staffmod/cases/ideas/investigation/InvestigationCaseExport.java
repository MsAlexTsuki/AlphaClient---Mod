package com.alpharip.staffmod.staffops.ideas;

import java.util.LinkedHashMap;
import java.util.Map;

/** Formats caller-supplied case fields as a simple text export; no file I/O is performed. */
public final class InvestigationCaseExport {
    public String format(Map<String, String> fields) {
        StringBuilder out = new StringBuilder();
        if (fields == null) return out.toString();
        for (Map.Entry<String, String> entry : new LinkedHashMap<String, String>(fields).entrySet()) {
            if (entry.getKey() == null) continue;
            out.append(entry.getKey()).append(": ")
                .append(entry.getValue() == null ? "" : entry.getValue()).append('\n');
        }
        return out.toString();
    }
}