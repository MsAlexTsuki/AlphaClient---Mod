package com.alpharip.staffmod.staffops.ideas;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/** Configurable team-callout wheel model; keybind, radial UI and chat sending are pending. */
public final class CallWheel {
    private final List<String> callouts = new ArrayList<String>();
    public CallWheel() {
        callouts.add("Enemy at my coordinates");
        callouts.add("Need pots");
    }
    public void addCallout(String message) {
        if (message != null && !message.trim().isEmpty()) callouts.add(message.trim());
    }
    public void setCallout(int index, String message) {
        if (index < 0 || index >= callouts.size() || message == null || message.trim().isEmpty())
            throw new IllegalArgumentException("Invalid callout");
        callouts.set(index, message.trim());
    }
    public String getCallout(int index) { return callouts.get(index); }
    public List<String> getCallouts() { return Collections.unmodifiableList(new ArrayList<String>(callouts)); }
}
