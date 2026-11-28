package com.alpharip.staffmod.staffops.ideas;

/** Basic opt-in filter for sensitive text supplied by callers. */
public class DetectionPrivacyFilter {
    private boolean enabled = true;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String filter(String value) {
        if (value == null || !enabled) {
            return value;
        }
        return value.replaceAll("(?i)(token|password|secret)=\\S+", "$1=[REDACTED]");
    }
}
