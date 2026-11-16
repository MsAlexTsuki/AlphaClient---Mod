package com.alpharip.staffmod.cases.investigationcaseimport;

import java.util.LinkedHashMap;
import java.util.Map;

/** Parses simple key:value text into a caller-owned local field map. */
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

    public String formatPreview(Map<String, String> fields) {
        StringBuilder output = new StringBuilder();
        if (fields == null) return "";
        for (Map.Entry<String, String> field : fields.entrySet()) {
            output.append(field.getKey()).append(" = ").append(field.getValue()).append('\n');
        }
        return output.toString();
    }

    public InvestigationCaseImportGui createGui(net.minecraft.client.gui.GuiScreen parent) {
        return new InvestigationCaseImportGui(parent, this);
    }
}