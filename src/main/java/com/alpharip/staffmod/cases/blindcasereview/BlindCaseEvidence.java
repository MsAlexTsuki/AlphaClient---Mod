package com.alpharip.staffmod.blindcasereview;

public final class BlindCaseEvidence {
    private final String label;
    private final String content;

    public BlindCaseEvidence(String label, String content) {
        this.label = requireText(label, "label");
        this.content = requireText(content, "content");
    }

    public String getLabel() { return label; }
    public String getContent() { return content; }

    private static String requireText(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be empty");
        }
        return value.trim();
    }
}
