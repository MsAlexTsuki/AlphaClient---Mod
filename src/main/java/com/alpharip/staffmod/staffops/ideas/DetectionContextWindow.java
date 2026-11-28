package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/** Keeps a bounded sequence of caller-supplied context entries. */
public class DetectionContextWindow {
    private final Deque<String> entries = new ArrayDeque<String>();
    private int capacity;

    public DetectionContextWindow(int capacity) {
        setCapacity(capacity);
    }

    public void setCapacity(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be at least 1");
        }
        this.capacity = capacity;
        trimToCapacity();
    }

    public int getCapacity() {
        return capacity;
    }

    public void add(String entry) {
        if (entry == null) {
            return;
        }
        entries.addLast(entry);
        trimToCapacity();
    }

    public List<String> getEntries() {
        return Collections.unmodifiableList(new ArrayList<String>(entries));
    }

    public void clear() {
        entries.clear();
    }

    private void trimToCapacity() {
        while (entries.size() > capacity) {
            entries.removeFirst();
        }
    }
}
