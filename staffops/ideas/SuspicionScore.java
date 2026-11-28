package com.alpharip.staffmod.staffops.ideas;

/** Holds a manually calculated suspicion score and its stated rationale. */
public class SuspicionScore {
    private double score;
    private String rationale = "";

    public double getScore() { return score; }

    public void setScore(double score) {
        if (!Double.isNaN(score) && !Double.isInfinite(score)) this.score = score;
    }

    public String getRationale() { return rationale; }

    public void setRationale(String rationale) {
        this.rationale = rationale == null ? "" : rationale;
    }
}
