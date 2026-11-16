package com.alpharip.staffmod.blindcasereview;

import java.util.Map;

public final class BlindCaseReviewFormatter {
    private BlindCaseReviewFormatter() { }

    public static String formatDecision(BlindReviewDecision decision) {
        if (decision == null) return "No decision submitted";
        return decision.getOutcome().name() + " - " + decision.getRationale();
    }

    public static String formatContext(Map<String, String> context) {
        if (context == null || context.isEmpty()) return "No restricted context supplied";
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, String> entry : context.entrySet()) {
            if (result.length() > 0) result.append(" | ");
            result.append(entry.getKey()).append(": ").append(entry.getValue());
        }
        return result.toString();
    }
}
