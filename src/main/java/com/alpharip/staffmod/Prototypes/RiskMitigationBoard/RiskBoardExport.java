package com.alpharip.staffmod.RiskMitigationBoard;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
/** Writes a CSV snapshot for manual offline review. */
public final class RiskBoardExport {
    private RiskBoardExport(){}
    public static boolean exportCsv(File file,List<RiskRecord> records){
        if(file==null||records==null)return false;
        FileWriter writer=null;
        try{
            File parent=file.getParentFile();
            if(parent!=null&&!parent.exists()&&!parent.mkdirs())return false;
            writer=new FileWriter(file);
            writer.write("id,title,category,severity,status,owner,dueAt,createdAt,updatedAt\n");
            for(RiskRecord r:records){
                writer.write(csv(r.getId())+","+csv(r.getTitle())+","+csv(r.getCategory().name())+","
                    +csv(r.getSeverity().name())+","+csv(r.getStatus().name())+","+csv(r.getOwner())+","
                    +r.getDueAt()+","+r.getCreatedAt()+","+r.getUpdatedAt()+"\n");
            }
            writer.flush();
            return true;
        }catch(Exception e){return false;}
        finally{if(writer!=null)try{writer.close();}catch(Exception ignored){}}
    }
    private static String csv(String value){
        return "\""+(value==null?"":value).replace("\"","\\" )+"\"";
    }
}
