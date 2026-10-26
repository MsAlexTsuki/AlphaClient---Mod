package com.alpharip.staffmod.staffops.ideas;

import java.util.List;

/** Formats a supplied checklist as plain text; it does not write a file. */
public final class InvestigationChecklistExport {
    public String format(List<String> items) {
        StringBuilder out = new StringBuilder();
        if (items == null) return out.toString();
        for (String item : items) {
            if (item != null && !item.trim().isEmpty()) out.append("[ ] ").append(item.trim()).append('\n');
        }
        return out.toString();
    }
}