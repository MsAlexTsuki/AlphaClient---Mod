package com.alpharip.staffmod.staffops.ideas;

/** Foundation for tracking texture-memory cleanup requests. Actual resource disposal must be wired to Minecraft's renderer. */
public class TextureMemoryCleanupTool {
 private long estimatedBytes; private boolean cleanupRequested;
 public long getEstimatedBytes(){return estimatedBytes;} public void setEstimatedBytes(long v){estimatedBytes=Math.max(0L,v);}
 public void requestCleanup(){cleanupRequested=true;} public boolean isCleanupRequested(){return cleanupRequested;} public void clearRequest(){cleanupRequested=false;}
}
