package com.alpharip.staffmod.BuildTimelapse;
import com.alpharip.staffmod.BuildTimelapse.core.*;import com.alpharip.staffmod.BuildTimelapse.camera.*;import com.alpharip.staffmod.BuildTimelapse.capture.*;import com.alpharip.staffmod.BuildTimelapse.timeline.*;
/** Composition root; Minecraft runtime adapters are injected, never guessed. */
public final class BuildTimelapse {private final BuildTimelapseProject project;private final CameraController camera;private final CaptureService capture;private final Timeline timeline;private final BuildTimelapseController controller;
 public BuildTimelapse(BuildTimelapseProject p,CameraController c,CaptureService s,Timeline t,ProjectStore store){if(p==null||c==null||s==null||t==null||store==null)throw new IllegalArgumentException("Dependencies required");project=p;camera=c;capture=s;timeline=t;controller=new BuildTimelapseController(p,c,s,t,store);}
 public BuildTimelapseProject getProject(){return project;}public CameraController getCamera(){return camera;}public CaptureService getCapture(){return capture;}public Timeline getTimeline(){return timeline;}public BuildTimelapseController getController(){return controller;}
}
