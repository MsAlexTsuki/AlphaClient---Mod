package com.alpharip.staffmod.BuildTimelapse.capture;
import com.alpharip.staffmod.BuildTimelapse.integration.MinecraftScreenshotBridge;import java.io.*;
/** Marshals framebuffer capture to the client thread and restores HUD/hand state even after failure. */
public final class MinecraftScreenshotCaptureBackend implements CaptureBackend {
 private final MinecraftScreenshotBridge bridge;private final RenderThreadExecutor executor;
 public MinecraftScreenshotCaptureBackend(MinecraftScreenshotBridge b,RenderThreadExecutor e){if(b==null||e==null)throw new IllegalArgumentException();bridge=b;executor=e;}
 public CaptureFrame capture(final long index,final long timestamp,final File output,final CaptureSettings settings)throws Exception{
  if(output==null||settings==null)throw new IllegalArgumentException();return executor.call(new java.util.concurrent.Callable<CaptureFrame>(){public CaptureFrame call()throws Exception{
   boolean oldHud=bridge.isHudHidden(),oldHand=bridge.isHandHidden();int width=settings.getWidth()==0?bridge.getDisplayWidth():settings.getWidth();int height=settings.getHeight()==0?bridge.getDisplayHeight():settings.getHeight();if(width<1||height<1)throw new IOException("Minecraft returned an invalid framebuffer size");
   File parent=output.getParentFile();if(parent!=null&&!parent.exists()&&!parent.mkdirs())throw new IOException("Cannot create frame directory");
   try{bridge.setHudHidden(settings.isHideHud());bridge.setHandHidden(settings.isHideHand());bridge.captureFramebuffer(output,width,height);if(!output.isFile()||output.length()==0)throw new IOException("Framebuffer adapter did not write a PNG frame");return new CaptureFrame(index,timestamp,output.getAbsolutePath(),width,height,output.length());}
   finally{try{bridge.setHandHidden(oldHand);}finally{bridge.setHudHidden(oldHud);}}
  }});
 }
}