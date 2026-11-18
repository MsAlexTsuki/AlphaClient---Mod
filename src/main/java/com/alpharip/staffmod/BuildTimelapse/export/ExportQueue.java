package com.alpharip.staffmod.BuildTimelapse.export;
import java.util.concurrent.*;
public final class ExportQueue {
 private final ExecutorService executor=Executors.newSingleThreadExecutor(new ThreadFactory(){public Thread newThread(Runnable r){Thread t=new Thread(r,"Alpha-BuildTimelapse-Export");t.setDaemon(true);return t;}});
 private volatile boolean busy,closed;
 public synchronized Future<?> submit(final VideoEncoder e,final ExportJob j){if(e==null||j==null)throw new IllegalArgumentException("Encoder and job are required");if(closed)throw new IllegalStateException("Export queue is shut down");if(busy)throw new IllegalStateException("Export busy");busy=true;try{return executor.submit(new Runnable(){public void run(){try{e.encode(j);}catch(Exception x){throw new RuntimeException(x);}finally{busy=false;}}});}catch(RuntimeException rejected){busy=false;throw rejected;}}
 public boolean isBusy(){return busy;}public synchronized void shutdown(){if(closed)return;closed=true;executor.shutdown();}
}