package com.alpharip.staffmod.BuildTimelapse.capture;
import java.io.*;import java.util.concurrent.*;import java.util.concurrent.atomic.AtomicLong;
/** Tick-driven interval scheduler with a bounded single-thread capture queue. Call tick on the client tick. */
public final class CaptureService {
 private final CaptureBackend backend;private final CaptureSettings settings;private final File dir;private final ThreadPoolExecutor worker;private final AtomicLong sequence=new AtomicLong();
 private volatile boolean running,paused,closed;private long nextAt;private volatile CaptureListener listener;
 public CaptureService(CaptureBackend b,CaptureSettings s,File d){if(b==null||s==null||d==null)throw new IllegalArgumentException();backend=b;settings=s;dir=d;worker=new ThreadPoolExecutor(1,1,0L,TimeUnit.MILLISECONDS,new ArrayBlockingQueue<Runnable>(2),new ThreadFactory(){public Thread newThread(Runnable r){Thread t=new Thread(r,"Alpha-BuildTimelapse-Capture");t.setDaemon(true);return t;}},new ThreadPoolExecutor.AbortPolicy());}
 public void setListener(CaptureListener l){listener=l;}
 public synchronized void start(){if(closed)throw new IllegalStateException("Capture service is shut down");if(running)return;if(!dir.exists()&&!dir.mkdirs())throw new IllegalStateException("Cannot create frame directory");running=true;paused=false;nextAt=System.currentTimeMillis();}
 public synchronized void pause(){paused=true;}public synchronized void resume(){if(running&&!closed){paused=false;nextAt=System.currentTimeMillis();}}
 public synchronized void stop(){running=false;paused=false;}
 public void tick(final long now){final long index;final CaptureSettings snapshot;synchronized(this){if(closed||!running||paused||now<nextAt)return;nextAt=now+settings.getInterval();index=sequence.getAndIncrement();snapshot=settings.copy();}
  try{worker.execute(new Runnable(){public void run(){try{File output=new File(dir,String.format(java.util.Locale.ROOT,"frame-%08d.png",index));CaptureFrame frame=backend.capture(index,now,output,snapshot);CaptureListener l=listener;if(l!=null)l.onCaptured(frame);}catch(Throwable error){CaptureListener l=listener;if(l!=null)l.onFailure(index,error);}}});}
  catch(RejectedExecutionException full){CaptureListener l=listener;if(l!=null)l.onFailure(index,new IOException("Capture queue is full; frame skipped to protect memory and render performance",full));}
 }
 public boolean isRunning(){return running;}public boolean isPaused(){return paused;}public long count(){return sequence.get();}public int queuedCaptures(){return worker.getQueue().size();}
 public synchronized void shutdown(){if(closed)return;stop();closed=true;worker.shutdown();}
}
