package com.alpharip.staffmod.staffops.ideas;


/** Holds two evidence references for a before/after comparison UI. */
public final class EvidenceBeforeAfterViewer {
    private String beforeReference = "";
    private String afterReference = "";

    public void setReferences(String beforeReference, String afterReference) {
        this.beforeReference = beforeReference == null ? "" : beforeReference;
        this.afterReference = afterReference == null ? "" : afterReference;
    }

    public String getBeforeReference() { return beforeReference; }
    public String getAfterReference() { return afterReference; }
    public boolean isReady() {
        return !beforeReference.trim().isEmpty() && !afterReference.trim().isEmpty();
    }
}