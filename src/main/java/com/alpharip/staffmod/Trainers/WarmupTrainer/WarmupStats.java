package com.alpharip.staffmod.Trainers.WarmupTrainer;

/** In-memory completion statistics; persistence is intentionally not included in this prototype. */
public final class WarmupStats {
    private int completedRoutines;
    private int currentStreak;
    private int bestStreak;
    private String lastCompletedDate;

    public synchronized void recordCompletion(String localDate) {
        if (localDate == null || localDate.trim().isEmpty()) throw new IllegalArgumentException("localDate must not be blank.");
        String date = localDate.trim();
        if (date.equals(lastCompletedDate)) return;
        completedRoutines++;
        currentStreak = isNextDay(lastCompletedDate, date) ? currentStreak + 1 : 1;
        if (currentStreak > bestStreak) bestStreak = currentStreak;
        lastCompletedDate = date;
    }

    private boolean isNextDay(String previous, String current) {
        if (previous == null) return false;
        try {
            java.time.LocalDate before = java.time.LocalDate.parse(previous);
            return before.plusDays(1).equals(java.time.LocalDate.parse(current));
        } catch (RuntimeException invalidDate) {
            return false;
        }
    }

    public synchronized int getCompletedRoutines() { return completedRoutines; }
    public synchronized int getCurrentStreak() { return currentStreak; }
    public synchronized int getBestStreak() { return bestStreak; }
    public synchronized String getLastCompletedDate() { return lastCompletedDate; }
}
