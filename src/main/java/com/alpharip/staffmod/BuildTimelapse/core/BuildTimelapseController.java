package com.alpharip.staffmod.BuildTimelapse.core;
import com.alpharip.staffmod.BuildTimelapse.camera.CameraController;import com.alpharip.staffmod.BuildTimelapse.capture.CaptureService;import com.alpharip.staffmod.BuildTimelapse.timeline.Timeline;import java.io.IOException;
public final class BuildTimelapseController {private final BuildTimelapseProject p;private final CameraController camera;private final CaptureService capture;private final Timeline timeline;private final ProjectStore store;private boolean active;
 public BuildTimelapseController(BuildTimelapseProject p,CameraController c,CaptureService s,Timeline t,ProjectStore st){if(p==null||c==null||s==null||t==null||st==null)throw new IllegalArgumentException();this.p=p;camera=c;capture=s;timeline=t;store=st;}
 public synchronized void start(){if(active)return;p.start(System.currentTimeMillis());capture.start();active=true;}public synchronized void pause(){if(active){capture.pause();p.pause(System.currentTimeMillis());active=false;}}
 public synchronized void resume(){if(!active){p.resume(System.currentTimeMillis());capture.resume();active=true;}}public synchronized void stop(){capture.stop();p.stop(System.currentTimeMillis());camera.stopPath();active=false;}
 public synchronized void save()throws IOException{store.save(p,timeline);}public synchronized boolean isActive(){return active;}
}
