package com.alpharip.staffmod.BuildTimelapse.camera;
public final class CameraController {
 private final CameraRuntimePort runtime;private final CameraPathInterpolator interpolator=new CameraPathInterpolator();private CameraPose pose=CameraPose.origin();private CameraMode mode=CameraMode.FREE;private CameraPath path;private long pathStart;private boolean active;
 public CameraController(CameraRuntimePort r){if(r==null)throw new IllegalArgumentException();runtime=r;}
 public synchronized void activate()throws Exception{if(active)return;if(!runtime.isAvailable())throw new IllegalStateException("Camera bridge missing");try{runtime.activate(pose);active=true;}catch(Exception failure){try{runtime.deactivate();}catch(Exception cleanup){failure.addSuppressed(cleanup);}throw failure;}}
 public synchronized void setPose(CameraPose p)throws Exception{if(p==null)throw new IllegalArgumentException();if(active)runtime.apply(p);pose=p;}
 public synchronized CameraPose getPose(){return pose;}public synchronized void setMode(CameraMode m){if(m==null)throw new IllegalArgumentException();mode=m;}public synchronized CameraMode getMode(){return mode;}
 public synchronized void playPath(CameraPath p,long now){if(p==null||p.getWaypoints().size()<2||p.duration()<=0)throw new IllegalArgumentException("Path needs two timed points");path=p;pathStart=now;mode=CameraMode.PATH;}
 public synchronized void tick(long now)throws Exception{if(!active||path==null)return;long elapsed=Math.max(0,now-pathStart);setPose(interpolator.sampleAt(path,elapsed));if(elapsed>=path.duration())path=null;}
 public synchronized void stopPath(){path=null;}
 public synchronized void deactivate()throws Exception{try{if(active)runtime.deactivate();}finally{active=false;path=null;}}
 public synchronized boolean isActive(){return active;}
}

