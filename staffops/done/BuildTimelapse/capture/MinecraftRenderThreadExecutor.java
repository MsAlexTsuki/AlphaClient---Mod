package com.alpharip.staffmod.BuildTimelapse.capture;
import net.minecraft.client.Minecraft;import java.util.concurrent.*;
/** Executes framebuffer work on Minecraft 1.8.9's client/render thread. */
public final class MinecraftRenderThreadExecutor implements RenderThreadExecutor {
 private final Minecraft minecraft;public MinecraftRenderThreadExecutor(Minecraft mc){if(mc==null)throw new IllegalArgumentException("minecraft");minecraft=mc;}
 public <T>T call(Callable<T> task)throws Exception{if(task==null)throw new IllegalArgumentException("task");if(minecraft.isCallingFromMinecraftThread())return task.call();try{return minecraft.addScheduledTask(task).get();}catch(InterruptedException e){Thread.currentThread().interrupt();throw e;}catch(ExecutionException e){Throwable cause=e.getCause();if(cause instanceof Exception)throw(Exception)cause;if(cause instanceof Error)throw(Error)cause;throw new RuntimeException(cause);}}
}