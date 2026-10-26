package com.alpharip.staffmod.staffops.ideas;


import java.util.List;

/** Formats supplied timeline rows as plain text; file writing is intentionally external. */
public final class EvidenceTimelineExport {
    public String toPlainText(List<String> rows) {
        StringBuilder output = new StringBuilder();
        if (rows == null) return "";
        for (String row : rows) {
            if (output.length() > 0) output.append('\n');
            output.append(row == null ? "" : row);
        }
        return output.toString();
    }
}