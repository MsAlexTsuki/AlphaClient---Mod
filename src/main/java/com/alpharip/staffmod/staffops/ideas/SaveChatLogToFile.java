package com.alpharip.staffmod.staffops.ideas;

/** Foundation for Save Chat Log to File; configuration/state only, runtime integration is not implemented here. */
public class SaveChatLogToFile {
    public boolean enabled = true;
    public boolean includeTimestamps = true;
    public String fileNamePattern = "chat-{date}.log";
}
