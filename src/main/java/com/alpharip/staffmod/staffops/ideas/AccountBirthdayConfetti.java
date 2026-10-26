package com.alpharip.staffmod.staffops.ideas;

import java.time.LocalDate;
import java.time.MonthDay;

/**
 * Foundation for an account birthday celebration.
 * The Minecraft particle renderer and account-date source must be connected
 * by the client integration before any confetti is displayed in-game.
 */
public final class AccountBirthdayConfetti {
    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isBirthday(LocalDate dateOfBirth, LocalDate currentDate) {
        if (!enabled || dateOfBirth == null || currentDate == null) {
            return false;
        }
        return MonthDay.from(dateOfBirth).equals(MonthDay.from(currentDate));
    }

    public boolean shouldCelebrate(LocalDate dateOfBirth, LocalDate currentDate) {
        return isBirthday(dateOfBirth, currentDate);
    }
}
