package com.alpharip.staffmod.RiskMitigationBoard;
/** One concrete action intended to reduce a risk. */
public final class RiskMitigationAction {
    private final String id; private String description, assignee;
    private boolean completed; private final long createdAt; private long completedAt;
    public RiskMitigationAction(String id,String description,String assignee,long createdAt) {
        if(blank(id)||blank(description)) throw new IllegalArgumentException("Action ID and description are required");
        this.id=id;this.description=description.trim();this.assignee=clean(assignee);this.createdAt=Math.max(0L,createdAt);
    }
    public String getId(){return id;} public String getDescription(){return description;}
    public String getAssignee(){return assignee;} public boolean isCompleted(){return completed;}
    public long getCreatedAt(){return createdAt;} public long getCompletedAt(){return completedAt;}
    public void update(String text,String owner){if(blank(text))throw new IllegalArgumentException("Action description is required");description=text.trim();assignee=clean(owner);}
    public void setCompleted(boolean value,long now){completed=value;completedAt=value?Math.max(0L,now):0L;}
    private static boolean blank(String s){return s==null||s.trim().isEmpty();}
    private static String clean(String s){return s==null?"":s.trim();}
}
