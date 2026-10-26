package com.alpharip.staffmod.staffops.ideas;
/** Normalized chat widget position, size and scale preferences. */
public class MovableScalableChat {private float x=.02f,y=.55f,width=.45f,height=.4f,scale=1f;
 public void setPosition(float x,float y){this.x=clamp(x);this.y=clamp(y);}public void setSize(float w,float h){if(w<=0||h<=0)throw new IllegalArgumentException();width=clamp(w);height=clamp(h);}
 public void setScale(float s){if(Float.isNaN(s)||s<.5f||s>2f)throw new IllegalArgumentException();scale=s;}
 public float getX(){return x;}public float getY(){return y;}public float getWidth(){return width;}public float getHeight(){return height;}public float getScale(){return scale;}
 private static float clamp(float v){if(Float.isNaN(v))throw new IllegalArgumentException();return Math.max(0,Math.min(1,v));}
}