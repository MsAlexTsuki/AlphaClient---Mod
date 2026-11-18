package com.alpharip.staffmod.RecordingStudio.capture;
import java.io.*;import java.util.Locale;
/** Standalone tick-driven capture scheduler. It does not depend on Build Timelapse. */
public final class RecordingCaptureService {
 public interface FrameWriter{void capture(File output)throws Exception;}
 private final FrameWriter writer;private final RecordingCaptureSettings settings;private final File directory;private long nextAt,index;private boolean recording,paused,closed;private String lastError;
 public RecordingCaptureService(FrameWriter w,RecordingCaptureSettings s,File d){if(w==null||s==null||d==null)throw new IllegalArgumentException();writer=w;settings=s;directory=d;}
 public synchronized void start(long now)throws IOException{ensureOpen();if((!directory.exists()&&!directory.mkdirs())||!directory.isDirectory())throw new IOException("Cannot create recording output directory");recording=true;paused=false;nextAt=now;lastError=null;}
 public synchronized void pause(){if(recording)paused=true;}public synchronized void resume(long now){ensureOpen();if(recording&&paused){paused=false;nextAt=now;}}
 public void tick(long now){final File output;synchronized(this){if(closed||!recording||paused||now<nextAt)return;long interval=settings.getIntervalMillis();nextAt=now>Long.MAX_VALUE-interval?Long.MAX_VALUE:now+interval;output=new File(directory,String.format(Locale.ROOT,"frame-%08d.png",index++));}try{writer.capture(output);synchronized(this){lastError=null;}}catch(Exception e){synchronized(this){lastError=e.toString();}}}
 public synchronized void stop(){recording=false;paused=false;}public synchronized boolean isRecording(){return recording&&!paused;}public synchronized boolean isPaused(){return recording&&paused;}public synchronized long getFrameCount(){return index;}public synchronized String getLastError(){return lastError;}public synchronized void shutdown(){if(closed)return;stop();closed=true;}private void ensureOpen(){if(closed)throw new IllegalStateException("Recording capture service is shut down");}
}