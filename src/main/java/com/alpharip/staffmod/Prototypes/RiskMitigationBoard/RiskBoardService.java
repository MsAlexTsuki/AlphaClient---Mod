package com.alpharip.staffmod.RiskMitigationBoard;
import java.util.ArrayList;import java.util.List;import java.util.UUID;
/** Validated use cases and dashboard calculations. */
public final class RiskBoardService {
    private final RiskBoard board;
    public RiskBoardService(RiskBoard board){if(board==null)throw new IllegalArgumentException("Board is required");this.board=board;}
    public RiskRecord create(String title,String description,RiskCategory category,RiskSeverity severity,String owner,long dueAt,long now){
        RiskRecord r=new RiskRecord(UUID.randomUUID().toString(),title,description,category,severity,owner,dueAt,now);board.add(r);return r;
    }
    public void update(String id,String title,String description,RiskCategory category,RiskSeverity severity,String owner,long dueAt,long now){
        RiskRecord r=require(id);if(r.getStatus()==RiskStatus.CLOSED)throw new IllegalStateException("Closed risks cannot be edited");
        r.revise(title,description,category,severity,owner,dueAt,now);
    }
    public void setStatus(String id,RiskStatus status,long now){require(id).changeStatus(status,now);}
    public void addAction(String id,String text,String assignee,long now){require(id).addAction(new RiskMitigationAction(UUID.randomUUID().toString(),text,assignee,now));}
    public void addNote(String id,String author,String text,long now){require(id).addNote(new RiskNote(UUID.randomUUID().toString(),author,text,now));}
    public boolean remove(String id){return board.remove(id);}
    public List<RiskRecord> find(RiskBoardFilter filter){List<RiskRecord> out=new ArrayList<RiskRecord>();for(RiskRecord r:board.getAll())if(filter==null||filter.matches(r))out.add(r);return out;}
    public RiskBoardSummary summarize(long now){
        int o=0,p=0,m=0,c=0,cr=0,due=0,actions=0;
        for(RiskRecord r:board.getAll()){
            switch(r.getStatus()){case OPEN:o++;break;case IN_PROGRESS:p++;break;case MITIGATED:m++;break;case CLOSED:c++;break;}
            if(r.getSeverity()==RiskSeverity.CRITICAL&&r.getStatus()!=RiskStatus.CLOSED)cr++;
            if(r.getDueAt()>0L&&r.getDueAt()<now&&r.getStatus()!=RiskStatus.CLOSED)due++;
            for(RiskMitigationAction a:r.getActions())if(!a.isCompleted())actions++;
        }
        return new RiskBoardSummary(board.size(),o,p,m,c,cr,due,actions);
    }
    private RiskRecord require(String id){RiskRecord r=board.get(id);if(r==null)throw new IllegalArgumentException("Risk not found: "+id);return r;}
}
