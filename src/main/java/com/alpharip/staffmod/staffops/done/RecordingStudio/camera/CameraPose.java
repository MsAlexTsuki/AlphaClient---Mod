package com.alpharip.staffmod.RecordingStudio.camera;
public final class CameraPose {
 public final double x,y,z;public final float yaw,pitch,roll,fov;
 public CameraPose(double x,double y,double z,float yaw,float pitch,float roll,float fov){if(!finite(x)||!finite(y)||!finite(z)||!finite(yaw)||!finite(pitch)||!finite(roll)||!finite(fov)||fov<1||fov>179)throw new IllegalArgumentException("Camera pose contains an invalid value");this.x=x;this.y=y;this.z=z;this.yaw=normalizeYaw(yaw);this.pitch=Math.max(-90,Math.min(90,pitch));this.roll=roll;this.fov=fov;}
 private static boolean finite(double n){return !Double.isNaN(n)&&!Double.isInfinite(n);}private static float normalizeYaw(float value){float result=value%360f;if(result>=180f)result-=360f;if(result< -180f)result+=360f;return result;}
 public static CameraPose origin(){return new CameraPose(0,0,0,0,0,0,70);}
}