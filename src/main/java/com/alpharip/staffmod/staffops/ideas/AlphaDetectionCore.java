package com.alpharip.staffmod.staffops.ideas;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** Registry foundation for detection components; does not run detection itself. */
public class AlphaDetectionCore {
    private final Set<String> registeredComponents = new HashSet<String>();

    public boolean register(String component) {
        return component != null && !component.trim().isEmpty()
                && registeredComponents.add(component.trim());
    }
    public boolean unregister(String component) {
        return component != null && registeredComponents.remove(component.trim());
    }
    public Set<String> getRegisteredComponents() {
        return Collections.unmodifiableSet(new HashSet<String>(registeredComponents));
    }
    public void clear() { registeredComponents.clear(); }
}
