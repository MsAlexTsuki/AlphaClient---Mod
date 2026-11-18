package com.alpharip.staffmod.RecordingStudio.core;
import com.alpharip.staffmod.RecordingStudio.capture.RecordingCaptureService;
import com.alpharip.staffmod.RecordingStudio.timeline.Timeline;import com.alpharip.staffmod.RecordingStudio.timeline.TimelineTrack;import java.io.*;
/** Owns a general-purpose recording session independently of any feature module. */
public final class RecordingStudioController {
 private final RecordingProject project;private final RecordingCaptureService capture;private final File projectDirectory;private final Timeline timeline=new Timeline();private boolean closed;private String status="Ready";
 public RecordingStudioController(RecordingProject p,RecordingCaptureService c,File d){if(p==null||c==null||d==null)throw new IllegalArgumentException();project=p;capture=c;projectDirectory=d;timeline.addTrack(new TimelineTrack("video","Video"));timeline.addTrack(new TimelineTrack("audio","Audio"));}
 public synchronized void start(){ensureOpen();try{long now=System.currentTimeMillis();capture.start(now);project.start(now);status="Recording";}catch(IOException e){status="Cannot start: "+e.getMessage();}}
 public synchronized void pause(){capture.pause();project.pause(System.currentTimeMillis());status="Paused";}
 public synchronized void resume(){ensureOpen();try{long now=System.currentTimeMillis();capture.resume(now);project.resume(now);status="Recording";}catch(RuntimeException e){status="Cannot resume: "+e.getMessage();}}
 public synchronized void stop(){capture.stop();project.stop(System.currentTimeMillis());status="Stopped";}
 public synchronized void tick(long now){if(!closed)capture.tick(now);}public synchronized void save()throws IOException{project.save(projectDirectory);status="Project saved";}
 public Timeline getTimeline(){return timeline;}public RecordingProject getProject(){return project;}public RecordingCaptureService getCapture(){return capture;}public synchronized String getStatus(){String e=capture.getLastError();return e==null?status:status+" — Capture error: "+e;}
 public synchronized void shutdown(){if(closed)return;stop();capture.shutdown();closed=true;}private void ensureOpen(){if(closed)throw new IllegalStateException("Recording Studio is shut down");}
}