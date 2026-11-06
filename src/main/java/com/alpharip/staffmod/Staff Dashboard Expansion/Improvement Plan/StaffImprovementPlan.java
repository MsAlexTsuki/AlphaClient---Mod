package com.alpharip.staffmod.staffdashboard.improvement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StaffImprovementPlan {
    private final int id;
    private final long createdAt;
    private final String staffMember;
    private String title;
    private String objective;
    private String deadline;
    private StaffImprovementStatus status;
    private int progressPercent;
    private final List<StaffImprovementAction> actions = new ArrayList<StaffImprovementAction>();
    private final List<StaffImprovementEvaluation> evaluations = new ArrayList<StaffImprovementEvaluation>();

    public StaffImprovementPlan(int id, String staffMember, String title, String objective, String deadline) {
        if (staffMember == null || staffMember.trim().isEmpty()) throw new IllegalArgumentException("Staff member is required.");
        if (title == null || title.trim().isEmpty()) throw new IllegalArgumentException("Plan title is required.");
        if (objective == null || objective.trim().isEmpty()) throw new IllegalArgumentException("Objective is required.");
        this.id = id;
        this.createdAt = System.currentTimeMillis();
        this.staffMember = staffMember.trim();
        this.title = title.trim();
        this.objective = objective.trim();
        this.deadline = deadline == null ? "" : deadline.trim();
        this.status = StaffImprovementStatus.OPEN;
    }

    public int getId() { return id; }
    public long getCreatedAt() { return createdAt; }
    public String getStaffMember() { return staffMember; }
    public String getTitle() { return title; }
    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) throw new IllegalArgumentException("Plan title is required.");
        this.title = title.trim();
    }
    public String getObjective() { return objective; }
    public void setObjective(String objective) {
        if (objective == null || objective.trim().isEmpty()) throw new IllegalArgumentException("Objective is required.");
        this.objective = objective.trim();
    }
    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline == null ? "" : deadline.trim(); }
    public StaffImprovementStatus getStatus() { return status; }
    public void setStatus(StaffImprovementStatus status) {
        if (status == null) throw new IllegalArgumentException("Status is required.");
        this.status = status;
    }
    public int getProgressPercent() { return progressPercent; }
    public void setProgressPercent(int progressPercent) {
        if (progressPercent < 0 || progressPercent > 100) throw new IllegalArgumentException("Progress must be between 0 and 100.");
        this.progressPercent = progressPercent;
    }
    public List<StaffImprovementAction> getActions() {
        return Collections.unmodifiableList(new ArrayList<StaffImprovementAction>(actions));
    }
    public List<StaffImprovementEvaluation> getEvaluations() {
        return Collections.unmodifiableList(new ArrayList<StaffImprovementEvaluation>(evaluations));
    }
    public void addAction(StaffImprovementAction action) {
        if (action == null) throw new IllegalArgumentException("Action is required.");
        actions.add(action);
    }
    public boolean removeAction(int actionId) {
        for (int i = 0; i < actions.size(); i++) {
            if (actions.get(i).getId() == actionId) { actions.remove(i); return true; }
        }
        return false;
    }
    public void addEvaluation(StaffImprovementEvaluation evaluation) {
        if (evaluation == null) throw new IllegalArgumentException("Evaluation is required.");
        evaluations.add(evaluation);
        progressPercent = evaluation.getProgressPercent();
    }
    public int getCompletedActionCount() {
        int count = 0;
        for (StaffImprovementAction action : actions) if (action.isCompleted()) count++;
        return count;
    }
    public int getActionCount() { return actions.size(); }
}
