package com.alpharip.staffmod.RiskMitigationBoard;
import com.google.gson.Gson;import com.google.gson.GsonBuilder;
import java.io.File;import java.io.FileReader;import java.io.FileWriter;import java.util.ArrayList;import java.util.List;
/** Versioned, local JSON persistence. No network or Supabase access. */
public final class RiskBoardStorage {
    private static final Gson GSON=new GsonBuilder().setPrettyPrinting().create();
    private RiskBoardStorage(){}
    private static final class Snapshot {int version=1;List<RiskRecord> records=new ArrayList<RiskRecord>();}
    public static boolean save(File file,RiskBoard board){
        if(file==null||board==null)return false;File parent=file.getParentFile();
        if(parent!=null&&!parent.exists()&&!parent.mkdirs())return false;
        File temp=new File(file.getAbsolutePath()+".tmp");FileWriter writer=null;
        try{
            Snapshot s=new Snapshot();s.records=board.getAll();writer=new FileWriter(temp);GSON.toJson(s,writer);writer.flush();writer.close();writer=null;
            if(file.exists()&&!file.delete())return false;if(!temp.renameTo(file)){temp.delete();return false;}return true;
        }catch(Exception e){if(temp.exists())temp.delete();return false;}
        finally{if(writer!=null)try{writer.close();}catch(Exception ignored){}}
    }
    public static boolean load(File file,RiskBoard board){
        if(file==null||board==null||!file.isFile())return false;FileReader reader=null;
        try{reader=new FileReader(file);Snapshot s=GSON.fromJson(reader,Snapshot.class);if(s==null||s.version!=1||s.records==null)return false;board.replaceAll(s.records);return true;}
        catch(Exception e){return false;}finally{if(reader!=null)try{reader.close();}catch(Exception ignored){}}
    }
}
