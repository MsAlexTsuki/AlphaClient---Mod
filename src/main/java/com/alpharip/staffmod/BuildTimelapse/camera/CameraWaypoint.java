package com.alpharip.staffmod.BuildTimelapse.camera;
public final class CameraWaypoint {private final String id;private final CameraPose pose;private final long duration,hold;private final String easing;
 public CameraWaypoint(String id,CameraPose p,long d,long h,String e){if(id==null||id.trim().isEmpty()||p==null||d<0||h<0)throw new IllegalArgumentException("Invalid waypoint");this.id=id;pose=p;duration=d;hold=h;easing=e==null?"SMOOTH":e;}
 public String getId(){return id;}public CameraPose getPose(){return pose;}public long getDuration(){return duration;}public long getHold(){return hold;}public String getEasing(){return easing;}
}
