package com.alpharip.staffmod.BuildTimelapse.export;
import java.util.concurrent.*;
/** Single-job export queue with cooperative process cancellation through thread interruption. */
public final class ExportQueue {
 private final ExecutorService executor=Executors.newSingleThreadExecutor(new ThreadFactory(){public Thread newThread(Runnable r){Thread t=new Thread(r,"Alpha-BuildTimelapse-Export");t.setDaemon(true);return t;}});
 private volatile boolean busy,closed;private FutureTask<Void> current;
 public synchronized Future<?> submit(final VideoEncoder encoder,final ExportJob job){
  if(encoder==null||job==null)throw new IllegalArgumentException("Encoder and job are required");
  if(closed)throw new IllegalStateException("Export queue is shut down");
  if(busy)throw new IllegalStateException("Export busy");
  busy=true;
  current=new FutureTask<Void>(new Callable<Void>(){public Void call()throws Exception{encoder.encode(job);return null;}}){
   @Override protected void done(){synchronized(ExportQueue.this){busy=false;if(current==this)current=null;}}
  };
  try{executor.execute(current);return current;}catch(RuntimeException ex){current.cancel(false);throw ex;}
 }
 public synchronized boolean cancelCurrent(){return current!=null&&current.cancel(true);}
 public boolean isBusy(){return busy;}
 public synchronized void shutdown(){if(closed)return;closed=true;if(current!=null)current.cancel(true);executor.shutdownNow();}
}
