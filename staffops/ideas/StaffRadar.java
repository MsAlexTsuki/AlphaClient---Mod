package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Data model for a local radar view of entities already visible to the client.
 * It must not be used to reveal hidden, unloaded, or server-only player locations.
 */
public final class StaffRadar {
    private final List<RadarContact> contacts = new ArrayList<RadarContact>();

    public void updateContact(String name, double x, double y, double z, long observedAtMillis) {
        if (name == null || name.trim().isEmpty()) {
            return;
        }
        for (int i = 0; i < contacts.size(); i++) {
            if (name.equalsIgnoreCase(contacts.get(i).getName())) {
                contacts.set(i, new RadarContact(name, x, y, z, observedAtMillis));
                return;
            }
        }
        contacts.add(new RadarContact(name, x, y, z, observedAtMillis));
    }

    public List<RadarContact> getContacts() {
        return Collections.unmodifiableList(new ArrayList<RadarContact>(contacts));
    }

    public void clear() { contacts.clear(); }

    public static final class RadarContact {
        private final String name;
        private final double x, y, z;
        private final long observedAtMillis;

        public RadarContact(String name, double x, double y, double z, long observedAtMillis) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.z = z;
            this.observedAtMillis = observedAtMillis;
        }

        public String getName() { return name; }
        public double getX() { return x; }
        public double getY() { return y; }
        public double getZ() { return z; }
        public long getObservedAtMillis() { return observedAtMillis; }
    }
}