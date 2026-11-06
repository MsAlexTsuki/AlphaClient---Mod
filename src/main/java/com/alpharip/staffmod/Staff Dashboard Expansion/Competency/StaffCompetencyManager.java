package com.alpharip.staffdashboardexpansion.competency;

import java.util.List;
import java.util.Locale;

/**
 * Operations for defining competencies and recording staff assessments.
 *
 * TODO: Add authenticated staff identity and rank-based access checks when
 * this prototype is integrated into an approved production host.
 * TODO: Add shared persistence only after the storage design is approved.
 */
public final class StaffCompetencyManager {
    private final StaffCompetencyMatrix matrix;
    private long nextCompetencyId = 1L;

    public StaffCompetencyManager() {
        this(new StaffCompetencyMatrix());
    }

    public StaffCompetencyManager(StaffCompetencyMatrix matrix) {
        if (matrix == null) throw new IllegalArgumentException("matrix must not be null.");
        this.matrix = matrix;
    }

    public synchronized StaffCompetency createCompetency(String name, String category,
                                                          String description) {
        String safeName = StaffCompetencyMatrix.requireText(name, "name");
        String safeCategory = StaffCompetencyMatrix.requireText(category, "category");
        String safeDescription = description == null ? "" : description.trim();
        for (StaffCompetency existing : matrix.getCompetencies()) {
            if (existing.getName().equalsIgnoreCase(safeName)) {
                throw new IllegalArgumentException("A competency with this name already exists.");
            }
        }
        String id;
        do {
            id = "competency-" + nextCompetencyId++;
        } while (matrix.getCompetency(id) != null);
        StaffCompetency competency =
            new StaffCompetency(id, safeName, safeCategory, safeDescription);
        matrix.addCompetency(competency);
        return competency;
    }

    public synchronized boolean removeCompetency(String id) {
        return matrix.removeCompetency(StaffCompetencyMatrix.requireText(id, "id"));
    }

    public synchronized StaffCompetencyAssessment assess(String staffName,
            String competencyId, StaffCompetencyLevel level, String notes) {
        String staff = StaffCompetencyMatrix.requireText(staffName, "staffName");
        String id = StaffCompetencyMatrix.requireText(competencyId, "competencyId");
        if (matrix.getCompetency(id) == null) {
            throw new IllegalArgumentException("Unknown competency: " + id);
        }
        if (level == null || level == StaffCompetencyLevel.NOT_ASSESSED) {
            throw new IllegalArgumentException("Choose an assessed competency level.");
        }
        String safeNotes = notes == null ? "" : notes.trim();
        StaffCompetencyAssessment assessment = matrix.getAssessment(staff, id);
        if (assessment == null) {
            assessment = new StaffCompetencyAssessment(staff, id, level, safeNotes);
            matrix.putAssessment(assessment);
        } else {
            assessment.update(level, safeNotes);
        }
        return assessment;
    }

    public synchronized boolean removeAssessment(String staffName, String competencyId) {
        return matrix.removeAssessment(staffName, competencyId);
    }

    public StaffCompetencyMatrix getMatrix() { return matrix; }
    public List<StaffCompetency> getCompetencies() { return matrix.getCompetencies(); }
    public List<StaffCompetencyAssessment> getAssessments() { return matrix.getAssessments(); }
    public List<StaffCompetencyAssessment> getAssessments(String staffName) {
        return matrix.getAssessments(staffName);
    }

    /** Convenience lookup for a staff member's level in a named competency. */
    public StaffCompetencyLevel getLevel(String staffName, String competencyName) {
        for (StaffCompetency competency : matrix.getCompetencies()) {
            if (competency.getName().toLowerCase(Locale.ROOT)
                    .equals(StaffCompetencyMatrix.requireText(competencyName, "competencyName")
                        .toLowerCase(Locale.ROOT))) {
                StaffCompetencyAssessment assessment =
                    matrix.getAssessment(staffName, competency.getId());
                return assessment == null ? StaffCompetencyLevel.NOT_ASSESSED
                    : assessment.getLevel();
            }
        }
        return StaffCompetencyLevel.NOT_ASSESSED;
    }
}
