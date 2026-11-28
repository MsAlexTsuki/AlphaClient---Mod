package com.alpharip.staffmod.staffops.ideas;

import java.util.List;

/** Produces a plain-text report from explicitly supplied investigation data. */
public final class InvestigationReportGenerator {
    public String generate(String title, List<String> sections) {
        StringBuilder out = new StringBuilder();
        if (title != null && !title.trim().isEmpty()) out.append(title.trim()).append('\n');
        out.append("=".equals("") ? "" : "--------------------").append('\n');
        if (sections != null) {
            for (String section : sections) {
                if (section == null || section.trim().isEmpty()) continue;
                out.append('\n').append(section.trim()).append('\n');
            }
        }
        return out.toString();
    }
}