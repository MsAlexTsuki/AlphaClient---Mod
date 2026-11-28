package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Builds an ordered, local investigation workflow definition. */
public final class InvestigationWorkflowBuilder {
    private final List<String> steps = new ArrayList<String>();

    public synchronized InvestigationWorkflowBuilder addStep(String step) {
        if (step == null || step.trim().isEmpty()) throw new IllegalArgumentException("step is required");
        steps.add(step.trim());
        return this;
    }

    public synchronized boolean removeStep(int index) {
        if (index < 0 || index >= steps.size()) return false;
        steps.remove(index);
        return true;
    }

    public synchronized void moveStep(int from, int to) {
        if (from < 0 || from >= steps.size() || to < 0 || to >= steps.size())
            throw new IndexOutOfBoundsException("step index out of range");
        String step = steps.remove(from);
        steps.add(to, step);
    }

    public synchronized List<String> build() {
        return Collections.unmodifiableList(new ArrayList<String>(steps));
    }

    public synchronized void clear() { steps.clear(); }
}