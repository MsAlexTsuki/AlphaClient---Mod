package com.alpharip.staffmod.BuildTimelapse.capture;
import java.io.*;import java.nio.charset.StandardCharsets;
/** Append-only local CSV manifest for captured frames and failures. */
public final class CaptureManifest implements CaptureListener {
 private final File file;public CaptureManifest(File f)throws IOException{if(f==null)throw new IllegalArgumentException();file=f;File parent=f.getParentFile();if(parent!=null&&!parent.exists()&&!parent.mkdirs())throw new IOException("Cannot create manifest directory");if(file.exists()&&!file.isFile())throw new IOException("Manifest path is not a file");if(!file.exists())append("type,index,timestamp,path,width,height,bytes,message");}
 public synchronized void onCaptured(CaptureFrame f){if(f==null){onFailure(-1,new IllegalArgumentException("Captured frame is null"));return;}try{append("FRAME,"+f.getIndex()+","+f.getTime()+","+csv(f.getPath())+","+f.getWidth()+","+f.getHeight()+","+f.getBytes()+",");}catch(IOException e){throw new IllegalStateException("Cannot write capture manifest",e);}}
 public synchronized void onFailure(long index,Throwable error){try{append("ERROR,"+index+","+System.currentTimeMillis()+",,,,,"+csv(error==null?"Unknown capture error":error.toString()));}catch(IOException e){throw new IllegalStateException("Cannot write capture manifest",e);}}
 private void append(String line)throws IOException{try(Writer w=new OutputStreamWriter(new FileOutputStream(file,true),StandardCharsets.UTF_8)){w.write(line);w.write("\n");}}
 private String csv(String s){if(s==null)return "\"\"";return "\""+s.replace("\"","\"\"")+"\"";}
}