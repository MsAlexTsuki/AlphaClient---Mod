package com.alpharip.staffmod.RiskMitigationBoard;
/** Optional criteria for searching and filtering risk records. */
public final class RiskBoardFilter {
    private RiskStatus status;private RiskSeverity severity;private RiskCategory category;private String owner="",text="";
    public RiskBoardFilter status(RiskStatus v){status=v;return this;} public RiskBoardFilter severity(RiskSeverity v){severity=v;return this;}
    public RiskBoardFilter category(RiskCategory v){category=v;return this;} public RiskBoardFilter owner(String v){owner=clean(v);return this;}
    public RiskBoardFilter text(String v){text=clean(v);return this;}
    public boolean matches(RiskRecord r){
        if(r==null||(status!=null&&r.getStatus()!=status)||(severity!=null&&r.getSeverity()!=severity)||(category!=null&&r.getCategory()!=category))return false;
        if(!owner.isEmpty()&&!r.getOwner().toLowerCase().contains(owner.toLowerCase()))return false;
        return text.isEmpty()||(r.getTitle()+" "+r.getDescription()).toLowerCase().contains(text.toLowerCase());
    }
    private static String clean(String s){return s==null?"":s.trim();}
}
