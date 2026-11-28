package com.alpharip.staffmod.AuditFindingsBoard;
import java.io.*;import java.util.List;
public final class AuditFindingsExport {private AuditFindingsExport(){}
 public static boolean writeCsv(File file,List<AuditFinding> findings){if(file==null||findings==null)return false;File parent=file.getParentFile();if(parent!=null&&!parent.exists()&&!parent.mkdirs())return false;Writer w=null;try{w=new OutputStreamWriter(new FileOutputStream(file),"UTF-8");w.write("id,title,category,severity,status,owner,dueAt,createdAt,updatedAt\n");for(AuditFinding f:findings)w.write(csv(f.getId())+","+csv(f.getTitle())+","+csv(f.getCategory().name())+","+csv(f.getSeverity().name())+","+csv(f.getStatus().name())+","+csv(f.getOwner())+","+f.getDueAt()+","+f.getCreatedAt()+","+f.getUpdatedAt()+"\n");w.flush();return true;}catch(Exception e){return false;}finally{if(w!=null)try{w.close();}catch(Exception ignored){}}}
 private static String csv(String v){String s=v==null?"":v;return "\""+s.replace("\"","\\\"")+"\"";}
}
