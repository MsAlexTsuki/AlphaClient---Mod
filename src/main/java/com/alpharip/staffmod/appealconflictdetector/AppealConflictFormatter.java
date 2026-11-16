package com.alpharip.staffmod.appealconflictdetector;

public final class AppealConflictFormatter {
    private AppealConflictFormatter() { }

    public static String format(AppealConflictReport report) {
        if (report == null) return "No report available";
        if (report.getStatus() == AppealConflictReport.Status.NO_REVIEWER_ASSIGNED) {
            return "Case " + report.getCaseId() + ": no reviewer assigned";
        }
        if (!report.hasConflicts()) {
            return "Case " + report.getCaseId() + ": no configured conflict found";
        }
        StringBuilder text = new StringBuilder();
        text.append("Case ").append(report.getCaseId())
                .append(": ").append(report.getFindings().size()).append(" conflict(s)");
        for (AppealConflictFinding finding : report.getFindings()) {
            text.append("\n- ").append(finding.getRuleDescription())
                    .append(" [").append(finding.getInvolvementType()).append("]");
            if (!finding.getReference().isEmpty()) {
                text.append(" (").append(finding.getReference()).append(")");
            }
        }
        return text.toString();
    }
}
