package com.alpharip.staffmod.staffops.ideas;


/** Presentation model for two evidence references displayed side by side. */
public final class EvidenceSideBySide {
    private String leftReference = "";
    private String rightReference = "";
    private String leftLabel = "Left";
    private String rightLabel = "Right";

    public void configure(String leftReference, String rightReference,
                          String leftLabel, String rightLabel) {
        this.leftReference = leftReference == null ? "" : leftReference;
        this.rightReference = rightReference == null ? "" : rightReference;
        this.leftLabel = leftLabel == null ? "Left" : leftLabel;
        this.rightLabel = rightLabel == null ? "Right" : rightLabel;
    }

    public String getLeftReference() { return leftReference; }
    public String getRightReference() { return rightReference; }
    public String getLeftLabel() { return leftLabel; }
    public String getRightLabel() { return rightLabel; }
}