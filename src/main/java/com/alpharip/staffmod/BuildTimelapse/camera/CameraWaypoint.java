package com.alpharip.staffmod.BuildTimelapse.camera;
public final class CameraWaypoint {
 private final String id;private final CameraPose pose;private final long duration,hold;private final String easing;
 public CameraWaypoint(String id,CameraPose p,long d,long h,String e){if(id==null||id.trim().isEmpty()||p==null||d<0||h<0)throw new IllegalArgumentException("Invalid waypoint");this.id=id.trim();pose=p;duration=d;hold=h;String mode=e==null?"SMOOTH":e.trim().toUpperCase(java.util.Locale.ROOT);if(!("LINEAR".equals(mode)||"EASE_IN".equals(mode)||"EASE_OUT".equals(mode)||"EASE_IN_OUT".equals(mode)||"SMOOTH".equals(mode)))throw new IllegalArgumentException("Unsupported easing: "+mode);easing=mode;}
 public String getId(){return id;}public CameraPose getPose(){return pose;}public long getDuration(){return duration;}public long getHold(){return hold;}public String getEasing(){return easing;}
}