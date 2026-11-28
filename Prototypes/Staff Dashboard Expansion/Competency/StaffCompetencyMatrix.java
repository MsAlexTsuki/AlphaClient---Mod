package com.alpharip.staffdashboardexpansion.competency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory competency matrix. Assessments are keyed by normalized staff name
 * and competency id; this class has no disk, network, or database dependency.
 */
public final class StaffCompetencyMatrix {
    private final Map<String, StaffCompetency> competencies =
        new LinkedHashMap<String, StaffCompetency>();
    private final Map<String, StaffCompetencyAssessment> assessments =
        new LinkedHashMap<String, StaffCompetencyAssessment>();

    synchronized void addCompetency(StaffCompetency competency) {
        competencies.put(competency.getId(), competency);
    }

    synchronized boolean removeCompetency(String id) {
        if (competencies.remove(requireText(id, "id")) == null) return false;
        List<String> remove = new ArrayList<String>();
        for (Map.Entry<String, StaffCompetencyAssessment> entry : assessments.entrySet()) {
            if (entry.getValue().getCompetencyId().equals(id)) remove.add(entry.getKey());
        }
        for (String key : remove) assessments.remove(key);
        return true;
    }

    synchronized void putAssessment(StaffCompetencyAssessment assessment) {
        assessments.put(key(assessment.getStaffName(), assessment.getCompetencyId()), assessment);
    }

    synchronized boolean removeAssessment(String staffName, String competencyId) {
        return assessments.remove(key(staffName, competencyId)) != null;
    }

    public synchronized StaffCompetency getCompetency(String id) {
        return competencies.get(requireText(id, "id"));
    }

    public synchronized StaffCompetencyAssessment getAssessment(String staffName,
                                                                  String competencyId) {
        return assessments.get(key(staffName, competencyId));
    }

    public synchronized List<StaffCompetency> getCompetencies() {
        return Collections.unmodifiableList(
            new ArrayList<StaffCompetency>(competencies.values()));
    }

    public synchronized List<StaffCompetencyAssessment> getAssessments() {
        return Collections.unmodifiableList(
            new ArrayList<StaffCompetencyAssessment>(assessments.values()));
    }

    public synchronized List<StaffCompetencyAssessment> getAssessments(String staffName) {
        String staff = requireText(staffName, "staffName");
        List<StaffCompetencyAssessment> result = new ArrayList<StaffCompetencyAssessment>();
        for (StaffCompetencyAssessment assessment : assessments.values()) {
            if (assessment.getStaffName().equalsIgnoreCase(staff)) result.add(assessment);
        }
        return Collections.unmodifiableList(result);
    }

    public synchronized int count(StaffCompetencyLevel level) {
        if (level == null) throw new IllegalArgumentException("level must not be null.");
        int count = 0;
        for (StaffCompetencyAssessment assessment : assessments.values()) {
            if (assessment.getLevel() == level) count++;
        }
        return count;
    }

    public synchronized int getCompetencyCount() { return competencies.size(); }
    public synchronized int getAssessmentCount() { return assessments.size(); }

    public synchronized void clear() {
        competencies.clear();
        assessments.clear();
    }

    static String requireText(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank.");
        }
        return value.trim();
    }

    static String key(String staffName, String competencyId) {
        return requireText(staffName, "staffName").toLowerCase(java.util.Locale.ROOT)
            + "::" + requireText(competencyId, "competencyId");
    }
}
