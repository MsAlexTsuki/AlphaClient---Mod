package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/** Local last-in-first-out stack of investigation targets. */
public final class InvestigationTargetStack {
    private final Deque<String> stack = new ArrayDeque<String>();

    public synchronized void push(String targetId) {
        if (targetId == null || targetId.trim().isEmpty()) throw new IllegalArgumentException("target id is required");
        stack.push(targetId);
    }

    public synchronized String peek() { return stack.peek(); }
    public synchronized String pop() { return stack.poll(); }
    public synchronized int size() { return stack.size(); }
    public synchronized List<String> snapshot() {
        return Collections.unmodifiableList(new ArrayList<String>(stack));
    }
}