package com.alpharip.staffmod.staffops.ideas;

/** Foundation for Break Reminder After a Configurable Duration; configuration/state only, runtime integration is not implemented here. */
public class BreakReminderAfterConfigurableDuration {
    public boolean enabled = true;
    public int intervalSeconds = 60;
    public String schedule = "";
    public String filePath = "";
}
