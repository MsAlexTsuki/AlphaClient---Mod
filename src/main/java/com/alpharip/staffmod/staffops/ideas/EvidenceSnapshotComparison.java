package com.alpharip.staffmod.staffops.ideas;


/** Holds references to two registered snapshots for a future comparison view. */
public final class EvidenceSnapshotComparison {
    private String firstReference = "";
    private String secondReference = "";

    public void setSnapshots(String firstReference, String secondReference) {
        this.firstReference = firstReference == null ? "" : firstReference;
        this.secondReference = secondReference == null ? "" : secondReference;
    }

    public String getFirstReference() { return firstReference; }
    public String getSecondReference() { return secondReference; }
    public boolean canCompare() {
        return !firstReference.trim().isEmpty()
                && !secondReference.trim().isEmpty()
                && !firstReference.equals(secondReference);
    }
}