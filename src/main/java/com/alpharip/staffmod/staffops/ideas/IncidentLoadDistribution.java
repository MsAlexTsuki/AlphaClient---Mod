package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tracks incident ownership and recommends the least-loaded candidate.
 *
 * This model does not dispatch incidents or validate real staff permissions.
 * TODO(Supabase): persist incident assignments, validate staff eligibility,
 * and enforce server-side access policies before operational use.
 * Compatible with Java 8.
 */
public class IncidentLoadDistribution {
    public static final class Incident {
        private final String id;
        private final int weight;
        private final String assigneeId;
        private final boolean resolved;

        private Incident(String id, int weight, String assigneeId, boolean resolved) {
            this.id = id;
            this.weight = weight;
            this.assigneeId = assigneeId;
            this.resolved = resolved;
        }

        public String getId() { return id; }
        public int getWeight() { return weight; }
        public String getAssigneeId() { return assigneeId; }
        public boolean isResolved() { return resolved; }
    }

    private final Map<String, Incident> incidents = new LinkedHashMap<String, Incident>();

    public void addIncident(String incidentId, int weight, String assigneeId) {
        String id = requireText(incidentId, "incidentId");
        if (weight < 1) {
            throw new IllegalArgumentException("weight must be at least 1.");
        }
        incidents.put(id, new Incident(id, weight,
                assigneeId == null || assigneeId.trim().isEmpty() ? null : assigneeId.trim(), false));
    }

    public void assign(String incidentId, String staffId) {
        Incident incident = requireIncident(incidentId);
        incidents.put(incident.getId(), new Incident(incident.getId(), incident.getWeight(),
                requireText(staffId, "staffId"), incident.isResolved()));
    }

    public void markResolved(String incidentId) {
        Incident incident = requireIncident(incidentId);
        incidents.put(incident.getId(), new Incident(incident.getId(), incident.getWeight(),
                incident.getAssigneeId(), true));
    }

    public String recommendAssignee(List<String> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        String best = null;
        int bestLoad = Integer.MAX_VALUE;
        for (String candidate : candidates) {
            if (candidate == null || candidate.trim().isEmpty()) {
                continue;
            }
            String id = candidate.trim();
            int load = getActiveLoad(id);
            if (load < bestLoad) {
                best = id;
                bestLoad = load;
            }
        }
        return best;
    }

    public int getActiveLoad(String staffId) {
        String id = requireText(staffId, "staffId");
        int load = 0;
        for (Incident incident : incidents.values()) {
            if (!incident.isResolved() && id.equals(incident.getAssigneeId())) {
                load += incident.getWeight();
            }
        }
        return load;
    }

    public List<Incident> getIncidents() {
        return Collections.unmodifiableList(new ArrayList<Incident>(incidents.values()));
    }

    public Map<String, Integer> getActiveLoads() {
        Map<String, Integer> loads = new LinkedHashMap<String, Integer>();
        for (Incident incident : incidents.values()) {
            if (!incident.isResolved() && incident.getAssigneeId() != null) {
                String id = incident.getAssigneeId();
                Integer current = loads.get(id);
                loads.put(id, (current == null ? 0 : current) + incident.getWeight());
            }
        }
        return Collections.unmodifiableMap(loads);
    }

    private Incident requireIncident(String incidentId) {
        Incident incident = incidents.get(requireText(incidentId, "incidentId"));
        if (incident == null) {
            throw new IllegalArgumentException("Unknown incident: " + incidentId);
        }
        return incident;
    }

    private static String requireText(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank.");
        }
        return value.trim();
    }
}
