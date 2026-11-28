package com.alpharip.staffmod.RiskMitigationBoard;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/** Aggregate for one risk, including mitigation actions and an internal note history. */
public final class RiskRecord {
    private final String id; private String title,description,owner;
    private RiskCategory category; private RiskSeverity severity; private RiskStatus status;
    private final long createdAt; private long updatedAt,dueAt,closedAt;
    private final List<RiskMitigationAction> actions=new ArrayList<RiskMitigationAction>();
    private final List<RiskNote> notes=new ArrayList<RiskNote>();
    public RiskRecord(String id,String title,String description,RiskCategory category,RiskSeverity severity,String owner,long dueAt,long now){
        if(id==null||id.trim().isEmpty())throw new IllegalArgumentException("Risk ID is required");
        this.id=id;this.createdAt=Math.max(0L,now);this.updatedAt=this.createdAt;
        revise(title,description,category,severity,owner,dueAt,now);this.status=RiskStatus.OPEN;
    }
    public String getId(){return id;} public String getTitle(){return title;} public String getDescription(){return description;}
    public String getOwner(){return owner;} public RiskCategory getCategory(){return category;}
    public RiskSeverity getSeverity(){return severity;} public RiskStatus getStatus(){return status;}
    public long getCreatedAt(){return createdAt;} public long getUpdatedAt(){return updatedAt;}
    public long getDueAt(){return dueAt;} public long getClosedAt(){return closedAt;}
    public List<RiskMitigationAction> getActions(){return Collections.unmodifiableList(new ArrayList<RiskMitigationAction>(actions));}
    public List<RiskNote> getNotes(){return Collections.unmodifiableList(new ArrayList<RiskNote>(notes));}
    public void revise(String t,String d,RiskCategory c,RiskSeverity s,String o,long due,long now){
        if(t==null||t.trim().isEmpty()||d==null||d.trim().isEmpty())throw new IllegalArgumentException("Title and description are required");
        if(c==null||s==null)throw new IllegalArgumentException("Category and severity are required");
        title=t.trim();description=d.trim();category=c;severity=s;owner=o==null?"":o.trim();dueAt=Math.max(0L,due);updatedAt=Math.max(0L,now);
    }
    public void changeStatus(RiskStatus next,long now){
        if(next==null)throw new IllegalArgumentException("Status is required");
        if(status==RiskStatus.CLOSED&&next!=RiskStatus.CLOSED)closedAt=0L;
        status=next;if(next==RiskStatus.CLOSED)closedAt=Math.max(0L,now);updatedAt=Math.max(0L,now);
    }
    public void addAction(RiskMitigationAction a){if(a==null)throw new IllegalArgumentException("Action is required");actions.add(a);touch();}
    public boolean removeAction(String id){for(int i=0;i<actions.size();i++)if(actions.get(i).getId().equals(id)){actions.remove(i);touch();return true;}return false;}
    public void addNote(RiskNote n){if(n==null)throw new IllegalArgumentException("Note is required");notes.add(n);touch();}
    private void touch(){updatedAt=System.currentTimeMillis();}
}
