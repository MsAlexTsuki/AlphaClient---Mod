package com.alpharip.staffmod.remembervillagers;

/** Immutable snapshot of one villager trade. */
public final class VillagerTradeEntry {
    private final String firstInput;
    private final int firstInputCount;
    private final String secondInput;
    private final int secondInputCount;
    private final String output;
    private final int outputCount;

    public VillagerTradeEntry(String firstInput, int firstInputCount,
                              String secondInput, int secondInputCount,
                              String output, int outputCount) {
        this.firstInput = safe(firstInput);
        this.firstInputCount = Math.max(0, firstInputCount);
        this.secondInput = safe(secondInput);
        this.secondInputCount = Math.max(0, secondInputCount);
        this.output = safe(output);
        this.outputCount = Math.max(0, outputCount);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    public String getFirstInput() { return firstInput; }
    public int getFirstInputCount() { return firstInputCount; }
    public String getSecondInput() { return secondInput; }
    public int getSecondInputCount() { return secondInputCount; }
    public String getOutput() { return output; }
    public int getOutputCount() { return outputCount; }

    public String getDisplayText() {
        StringBuilder text = new StringBuilder();
        text.append(firstInputCount).append("x ").append(firstInput);
        if (!secondInput.isEmpty()) {
            text.append(" + ").append(secondInputCount).append("x ").append(secondInput);
        }
        text.append(" -> ").append(outputCount).append("x ").append(output);
        return text.toString();
    }

    public boolean sameAs(VillagerTradeEntry other) {
        return other != null
                && firstInput.equals(other.firstInput)
                && firstInputCount == other.firstInputCount
                && secondInput.equals(other.secondInput)
                && secondInputCount == other.secondInputCount
                && output.equals(other.output)
                && outputCount == other.outputCount;
    }
}
