package com.alpharip.staffmod.RiskMitigationBoard;
/** Immutable aggregate counts for the dashboard. */
public final class RiskBoardSummary {
    private final int total,open,inProgress,mitigated,closed,critical,overdue,incompleteActions;
    RiskBoardSummary(int t,int o,int p,int m,int c,int cr,int due,int actions){total=t;open=o;inProgress=p;mitigated=m;closed=c;critical=cr;overdue=due;incompleteActions=actions;}
    public int getTotal(){return total;} public int getOpen(){return open;} public int getInProgress(){return inProgress;}
    public int getMitigated(){return mitigated;} public int getClosed(){return closed;} public int getCritical(){return critical;}
    public int getOverdue(){return overdue;} public int getIncompleteActions(){return incompleteActions;}
}
