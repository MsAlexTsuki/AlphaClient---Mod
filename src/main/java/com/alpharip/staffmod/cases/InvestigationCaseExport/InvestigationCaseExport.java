package com.alpharip.staffmod.cases.investigationcaseexport;

import java.util.LinkedHashMap;
import java.util.Map;

/** Formats caller-supplied case fields as plain text; no file I/O is performed here. */
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

    public InvestigationCaseExportGui createGui(net.minecraft.client.gui.GuiScreen parent) {
        return new InvestigationCaseExportGui(parent, this);
    }
}