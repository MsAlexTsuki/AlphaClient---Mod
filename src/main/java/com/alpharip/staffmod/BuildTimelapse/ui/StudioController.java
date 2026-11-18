package com.alpharip.staffmod.BuildTimelapse.ui;
import com.alpharip.staffmod.BuildTimelapse.core.BuildTimelapseController;
public final class StudioController {
 private final BuildTimelapseController c;private final StudioScreenModel m;
 public StudioController(BuildTimelapseController c,StudioScreenModel m){if(c==null||m==null)throw new IllegalArgumentException();this.c=c;this.m=m;}
 public void start(){try{c.start();m.setStatus("Recording");}catch(RuntimeException e){m.setStatus("Cannot start: "+e.getMessage());}}
 public void pause(){c.pause();m.setStatus("Paused");}public void resume(){try{c.resume();m.setStatus("Recording");}catch(RuntimeException e){m.setStatus("Cannot resume: "+e.getMessage());}}
 public void stop(){c.stop();m.setStatus(c.getLastFailure()==null?"Stopped":"Stopped with error: "+c.getLastFailure());}
 public void save()throws java.io.IOException{c.save();m.setStatus("Saved");}
}

