package com.alpharip.staffmod.badges.anniversary.birthday;

import java.time.LocalDate;
import java.time.MonthDay;

/** Local birthday settings for the Anniversary prototype; no account service is used. */
public final class BirthdayCelebrationSettings {
    private boolean enabled = true;
    private LocalDate birthday;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public LocalDate getBirthday() { return birthday; }
    public void setBirthday(LocalDate birthday) { this.birthday = birthday; }
    public void clearBirthday() { this.birthday = null; }
    public boolean hasBirthday() { return birthday != null; }

    public boolean isBirthdayToday(LocalDate currentDate) {
        return enabled && birthday != null && currentDate != null
                && MonthDay.from(birthday).equals(MonthDay.from(currentDate));
    }
}
