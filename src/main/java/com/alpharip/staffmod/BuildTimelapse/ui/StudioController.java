package com.alpharip.staffmod.BuildTimelapse.ui;
import com.alpharip.staffmod.BuildTimelapse.core.BuildTimelapseController;import com.alpharip.staffmod.BuildTimelapse.camera.CameraController;import com.alpharip.staffmod.BuildTimelapse.timeline.Timeline;import com.alpharip.staffmod.BuildTimelapse.capture.CaptureSettings;import com.alpharip.staffmod.BuildTimelapse.core.BuildTimelapseProject;
public final class StudioController {
 private final BuildTimelapseController c;private final StudioScreenModel m;
 public StudioController(BuildTimelapseController c,StudioScreenModel m){if(c==null||m==null)throw new IllegalArgumentException();this.c=c;this.m=m;m.setProject(c.getProject());}
 public void start(){try{c.start();m.setStatus("Recording");}catch(RuntimeException e){m.setStatus("Cannot start: "+e.getMessage());}}
 public void pause(){c.pause();m.setStatus("Paused");}public void resume(){try{c.resume();m.setStatus("Recording");}catch(RuntimeException e){m.setStatus("Cannot resume: "+e.getMessage());}}
 public void stop(){c.stop();m.setStatus(c.getLastFailure()==null?"Stopped":"Stopped with error: "+c.getLastFailure());}
 public void save()throws java.io.IOException{c.save();m.setStatus("Saved");}
 public CameraController getCamera(){return c.getCamera();}public Timeline getTimeline(){return c.getTimeline();}public CaptureSettings getCaptureSettings(){return c.getCapture().getSettings();}public BuildTimelapseProject getProject(){return c.getProject();}
}

