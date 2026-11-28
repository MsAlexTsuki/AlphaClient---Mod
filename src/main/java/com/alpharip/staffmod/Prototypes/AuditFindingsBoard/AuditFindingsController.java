package com.alpharip.staffmod.AuditFindingsBoard;
import java.io.File;
public final class AuditFindingsController {
 private final AuditFindingsRegistry registry;private final AuditFindingsService service;private final AuditFindingsAccess access;private final File file;
 public AuditFindingsController(AuditFindingsRegistry r,AuditFindingsAccess a,File f){if(r==null||a==null)throw new IllegalArgumentException("Registry and access adapter required");registry=r;service=new AuditFindingsService(r);access=a;file=f;}
 public AuditFindingsRegistry getRegistry(){view();return registry;}public AuditFindingsService getService(){manage();return service;}public AuditFindingsAccess getAccess(){return access;}
 public boolean load(){view();return file!=null&&AuditFindingsStorage.load(file,registry);}public boolean save(){manage();return file!=null&&AuditFindingsStorage.save(file,registry);}
 private void view(){if(!access.canViewFindings())throw new SecurityException("No permission to view audit findings");}private void manage(){if(!access.canManageFindings())throw new SecurityException("No permission to manage audit findings");}
}
