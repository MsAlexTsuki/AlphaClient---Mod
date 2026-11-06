package com.alpharip.staffmod.staffdashboardexpansion.staffqueue;

public enum StaffQueuePriority {
    LOW(0),
    NORMAL(1),
    HIGH(2),
    URGENT(3);

    private final int weight;

    StaffQueuePriority(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }
}
