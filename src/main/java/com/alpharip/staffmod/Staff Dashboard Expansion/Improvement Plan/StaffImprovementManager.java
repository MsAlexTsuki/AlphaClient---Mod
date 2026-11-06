package com.alpharip.staffmod.staffdashboard.improvement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Local prototype manager. Data is intentionally memory-only. */
public class StaffImprovementManager {
    private final List<StaffImprovementPlan> plans = new ArrayList<StaffImprovementPlan>();
    private int nextPlanId = 1;
    private int nextActionId = 1;

    public StaffImprovementPlan createPlan(String staffMember, String title, String objective, String deadline) {
        StaffImprovementPlan plan = new StaffImprovementPlan(nextPlanId++, staffMember, title, objective, deadline);
        plans.add(plan);
        return plan;
    }

    public boolean removePlan(int planId) {
        for (int i = 0; i < plans.size(); i++) {
            if (plans.get(i).getId() == planId) { plans.remove(i); return true; }
        }
        return false;
    }

    public StaffImprovementPlan getPlan(int planId) {
        for (StaffImprovementPlan plan : plans) if (plan.getId() == planId) return plan;
        return null;
    }

    public List<StaffImprovementPlan> getPlans() {
        return Collections.unmodifiableList(new ArrayList<StaffImprovementPlan>(plans));
    }

    public StaffImprovementAction addAction(int planId, String description) {
        StaffImprovementPlan plan = getPlan(planId);
        if (plan == null) return null;
        StaffImprovementAction action = new StaffImprovementAction(nextActionId++, description);
        plan.addAction(action);
        return action;
    }

    public boolean removeAction(int planId, int actionId) {
        StaffImprovementPlan plan = getPlan(planId);
        return plan != null && plan.removeAction(actionId);
    }

    public boolean addEvaluation(int planId, String reviewer, String notes, int progressPercent) {
        StaffImprovementPlan plan = getPlan(planId);
        if (plan == null) return false;
        plan.addEvaluation(new StaffImprovementEvaluation(reviewer, notes, progressPercent));
        return true;
    }

    public int getTotalPlanCount() { return plans.size(); }
    public int getOpenPlanCount() {
        int count = 0;
        for (StaffImprovementPlan plan : plans) if (plan.getStatus() != StaffImprovementStatus.COMPLETED) count++;
        return count;
    }
    public void clear() { plans.clear(); }
}
