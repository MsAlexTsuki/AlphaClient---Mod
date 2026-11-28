package com.alpharip.staffmod.Trainers.WarmupTrainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Ordered collection of warm-up exercises. */
public final class WarmupRoutine {
    private final String id;
    private final String name;
    private final List<WarmupStep> steps;

    public WarmupRoutine(String id, String name, List<WarmupStep> steps) {
        this.id = requireText(id, "id");
        this.name = requireText(name, "name");
        if (steps == null || steps.isEmpty()) throw new IllegalArgumentException("A routine needs at least one step.");
        List<WarmupStep> copy = new ArrayList<WarmupStep>(steps);
        Set<String> ids = new HashSet<String>();
        for (WarmupStep step : copy) {
            if (step == null) throw new IllegalArgumentException("Steps must not contain null.");
            if (!ids.add(step.getId())) throw new IllegalArgumentException("Duplicate step id: " + step.getId());
        }
        this.steps = Collections.unmodifiableList(copy);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public List<WarmupStep> getSteps() { return steps; }
    public int getTotalDurationSeconds() {
        int total = 0;
        for (WarmupStep step : steps) total += step.getDurationSeconds();
        return total;
    }

    /** Default routine based on the original WarmUpRoutineRunner idea. */
    public static WarmupRoutine createDefault() {
        List<WarmupStep> steps = new ArrayList<WarmupStep>();
        steps.add(new WarmupStep("aim", "Aim practice", "Warm up tracking and target placement.", 120));
        steps.add(new WarmupStep("strafing", "Strafing", "Practice controlled left and right movement.", 90));
        steps.add(new WarmupStep("bridging", "Bridging", "Practice consistent bridge placement.", 90));
        return new WarmupRoutine("default", "Standard warm-up", steps);
    }

    private static String requireText(String value, String name) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException(name + " must not be blank.");
        return value.trim();
    }
}
