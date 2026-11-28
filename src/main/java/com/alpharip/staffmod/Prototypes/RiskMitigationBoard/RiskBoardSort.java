package com.alpharip.staffmod.RiskMitigationBoard;
import java.util.Collections;import java.util.Comparator;import java.util.List;
public final class RiskBoardSort {
    public enum Order { SEVERITY, UPDATED_NEWEST, DUE_DATE }
    private RiskBoardSort(){}
    public static void sort(List<RiskRecord> values,final Order order){
        if(values==null||order==null)return;
        Collections.sort(values,new Comparator<RiskRecord>(){public int compare(RiskRecord a,RiskRecord b){
            if(order==Order.SEVERITY)return Integer.compare(b.getSeverity().ordinal(),a.getSeverity().ordinal());
            if(order==Order.DUE_DATE){long ad=a.getDueAt()==0L?Long.MAX_VALUE:a.getDueAt(),bd=b.getDueAt()==0L?Long.MAX_VALUE:b.getDueAt();return Long.compare(ad,bd);}
            return Long.compare(b.getUpdatedAt(),a.getUpdatedAt());
        }});
    }
}
