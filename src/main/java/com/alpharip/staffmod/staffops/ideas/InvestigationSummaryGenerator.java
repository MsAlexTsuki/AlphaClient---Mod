package com.alpharip.staffmod.staffops.ideas;

import java.util.List;

/** Creates a concise text summary from supplied notes; it does not infer missing facts. */
public final class InvestigationSummaryGenerator {
    public String generate(String title, List<String> keyPoints) {
        StringBuilder out = new StringBuilder();
        if (title != null && !title.trim().isEmpty()) out.append(title.trim()).append('\n');
        if (keyPoints != null) {
            for (String point : keyPoints) {
                if (point != null && !point.trim().isEmpty()) out.append("- ").append(point.trim()).append('\n');
            }
        }
        return out.toString();
    }
}