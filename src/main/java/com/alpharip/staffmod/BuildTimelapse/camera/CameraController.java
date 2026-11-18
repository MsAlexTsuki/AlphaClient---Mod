package com.alpharip.staffmod.BuildTimelapse.camera;
public final class CameraController {
 private final CameraRuntimePort runtime;private final CameraPathInterpolator interpolator=new CameraPathInterpolator();private CameraPose pose=CameraPose.origin();private CameraMode mode=CameraMode.FREE;private CameraPath path;private long pathStart,pathPausedAt;private boolean active,poseConfigured;
 public CameraController(CameraRuntimePort r){if(r==null)throw new IllegalArgumentException();runtime=r;CameraPose initial=r.getInitialPose();if(initial!=null)pose=initial;}
 public synchronized void activate()throws Exception{if(active)return;if(!runtime.isAvailable())throw new IllegalStateException("Camera bridge missing");if(!poseConfigured){CameraPose initial=runtime.getInitialPose();if(initial!=null)pose=initial;}try{runtime.activate(pose);active=true;}catch(Throwable failure){try{runtime.deactivate();}catch(Throwable cleanup){failure.addSuppressed(cleanup);}if(failure instanceof Exception)throw(Exception)failure;if(failure instanceof Error)throw(Error)failure;throw new RuntimeException(failure);}}
 public synchronized void setPose(CameraPose p)throws Exception{if(p==null)throw new IllegalArgumentException();if(active)runtime.apply(p);pose=p;poseConfigured=true;}
 public synchronized CameraPose getPose(){return pose;}public synchronized void setMode(CameraMode m){if(m==null)throw new IllegalArgumentException();if(m!=CameraMode.PATH&&path!=null)stopPath();mode=m;}public synchronized CameraMode getMode(){return mode;}
 public synchronized void playPath(CameraPath p,long now){if(p==null||p.getWaypoints().size()<2||p.duration()<=0)throw new IllegalArgumentException("Path needs two timed points");path=p;pathStart=now;pathPausedAt=0;mode=CameraMode.PATH;}
 public synchronized void pausePath(long now){if(path!=null&&pathPausedAt==0)pathPausedAt=now;}
 public synchronized void resumePath(long now){if(path!=null&&pathPausedAt!=0){long paused=Math.max(0,now-pathPausedAt);pathStart=pathStart>Long.MAX_VALUE-paused?Long.MAX_VALUE:pathStart+paused;pathPausedAt=0;}}
 public synchronized void tick(long now)throws Exception{if(!active||path==null||pathPausedAt!=0)return;long elapsed=now<=pathStart?0:now-pathStart;setPose(interpolator.sampleAt(path,elapsed));if(elapsed>=path.duration()){path=null;mode=CameraMode.FREE;}}
 public synchronized void stopPath(){path=null;pathPausedAt=0;if(mode==CameraMode.PATH)mode=CameraMode.FREE;}
 public synchronized void deactivate()throws Exception{try{if(active)runtime.deactivate();}finally{active=false;stopPath();}}
 public synchronized boolean isActive(){return active;}
}