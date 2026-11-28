package com.alpharip.staffmod.AuditFindingsBoard;
import java.util.*;
/** Board facade retaining the original enabled flag and string-record API. */
public final class AuditFindingsBoard {
 private boolean enabled=true;private final AuditFindingsRegistry registry=new AuditFindingsRegistry();private final AuditFindingsService service=new AuditFindingsService(registry);
 public boolean isEnabled(){return enabled;}public void setEnabled(boolean v){enabled=v;}public AuditFindingsRegistry getRegistry(){return registry;}public AuditFindingsService getService(){return service;}
 public void addRecord(String record){if(record!=null&&!record.trim().isEmpty())service.create("Imported audit finding",record.trim(),AuditFindingCategory.OTHER,AuditFindingSeverity.MEDIUM,"",0L,System.currentTimeMillis());}
 public List<String> getRecords(){List<String> out=new ArrayList<String>();for(AuditFinding f:registry.getAll())out.add(f.getDescription());return Collections.unmodifiableList(out);}
 public void clearRecords(){for(AuditFinding f:registry.getAll())registry.remove(f.getId());}
}
