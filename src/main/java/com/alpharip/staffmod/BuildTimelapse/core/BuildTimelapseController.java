package com.alpharip.staffmod.BuildTimelapse.core;
import com.alpharip.staffmod.BuildTimelapse.camera.CameraController;import com.alpharip.staffmod.BuildTimelapse.capture.CaptureService;import com.alpharip.staffmod.BuildTimelapse.timeline.Timeline;import java.io.IOException;
/** Coordinates project, camera and interval capture. tick() must be called once per client tick. */
public final class BuildTimelapseController {
 private final BuildTimelapseProject p;private final CameraController camera;private final CaptureService capture;private final Timeline timeline;private final ProjectStore store;private boolean active;private volatile String lastFailure;
 public BuildTimelapseController(BuildTimelapseProject p,CameraController c,CaptureService s,Timeline t,ProjectStore st){if(p==null||c==null||s==null||t==null||st==null)throw new IllegalArgumentException();this.p=p;camera=c;capture=s;timeline=t;store=st;}
 public synchronized void start(){if(active)return;lastFailure=null;try{camera.activate();capture.start();p.start(System.currentTimeMillis());active=true;}catch(Exception failure){try{capture.stop();}finally{try{camera.deactivate();}catch(Exception cleanup){failure.addSuppressed(cleanup);}}throw new IllegalStateException("Could not start Build Timelapse; camera activation or capture setup failed",failure);}}
 public synchronized void pause(){if(active){capture.pause();p.pause(System.currentTimeMillis());active=false;}}
 public synchronized void resume(){if(!active){if(!camera.isActive())throw new IllegalStateException("Camera is not active; restart the recording session");p.resume(System.currentTimeMillis());capture.resume();active=true;}}
 public synchronized void tick(long now){if(!camera.isActive())return;try{camera.tick(now);if(active)capture.tick(now);}catch(Exception failure){lastFailure=failure.toString();try{stop();}catch(RuntimeException cleanup){lastFailure=lastFailure+"; cleanup: "+cleanup;}}}
 public synchronized void stop(){capture.stop();p.stop(System.currentTimeMillis());camera.stopPath();try{camera.deactivate();}catch(Exception e){lastFailure="Camera cleanup failed: "+e;}finally{active=false;}}
 public synchronized void save()throws IOException{store.save(p,timeline);}public synchronized boolean isActive(){return active;}public String getLastFailure(){return lastFailure;}
 public synchronized void shutdown(){try{stop();}finally{capture.shutdown();}}
}

