package com.alpharip.staffmod.AuditFindingsBoard;
public final class AuditFindingsFilter {private final AuditFindingStatus status;private final AuditFindingSeverity severity;private final AuditFindingCategory category;private final String owner,query;
 public AuditFindingsFilter(AuditFindingStatus s,AuditFindingSeverity v,AuditFindingCategory c,String o,String q){status=s;severity=v;category=c;owner=o==null?"":o.trim().toLowerCase();query=q==null?"":q.trim().toLowerCase();}
 public boolean matches(AuditFinding f){if(f==null)return false;if(status!=null&&f.getStatus()!=status)return false;if(severity!=null&&f.getSeverity()!=severity)return false;if(category!=null&&f.getCategory()!=category)return false;if(!owner.isEmpty()&&!f.getOwner().toLowerCase().contains(owner))return false;return query.isEmpty()||f.getTitle().toLowerCase().contains(query)||f.getDescription().toLowerCase().contains(query)||f.getOwner().toLowerCase().contains(query);}
}
