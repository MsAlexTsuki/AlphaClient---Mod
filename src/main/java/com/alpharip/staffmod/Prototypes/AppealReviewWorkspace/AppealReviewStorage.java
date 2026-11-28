package com.alpharip.staffmod.AppealReviewWorkspace;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
/** Versioned local JSON persistence. Only base appeal cases are persisted in this prototype. */
public final class AppealReviewStorage {
 private static final Gson GSON=new GsonBuilder().setPrettyPrinting().create();private AppealReviewStorage(){}
 private static final class Snapshot {int version=1;List<AppealCase> cases=new ArrayList<AppealCase>();}
 public static boolean save(File file,AppealReviewBoard board){
  if(file==null||board==null)return false;File parent=file.getParentFile();if(parent!=null&&!parent.exists()&&!parent.mkdirs())return false;
  File temp=new File(file.getAbsolutePath()+".tmp"),bak=new File(file.getAbsolutePath()+".bak");Writer w=null;
  try{Snapshot s=new Snapshot();s.cases=board.getAll();w=new OutputStreamWriter(new FileOutputStream(temp),"UTF-8");GSON.toJson(s,w);w.flush();w.close();w=null;
   if(bak.exists()&&!bak.delete())return false;boolean had=file.exists();if(had&&!file.renameTo(bak))return false;
   if(!temp.renameTo(file)){if(had)bak.renameTo(file);return false;}if(had)bak.delete();return true;
  }catch(Exception e){temp.delete();return false;}finally{if(w!=null)try{w.close();}catch(Exception ignored){}}
 }
 public static boolean load(File file,AppealReviewBoard board){
  if(file==null||board==null||!file.isFile())return false;Reader r=null;
  try{r=new InputStreamReader(new FileInputStream(file),"UTF-8");Snapshot s=GSON.fromJson(r,Snapshot.class);if(s==null||s.version!=1||s.cases==null)return false;board.replaceCases(s.cases);return true;}
  catch(Exception e){return false;}finally{if(r!=null)try{r.close();}catch(Exception ignored){}}
 }
}