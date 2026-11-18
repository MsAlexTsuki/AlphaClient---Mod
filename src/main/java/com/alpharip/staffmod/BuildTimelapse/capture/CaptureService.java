package com.alpharip.staffmod.BuildTimelapse.capture;

import java.io.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/** Tick-driven interval scheduler with a bounded single-thread capture queue. Call tick on the client tick. */
public final class CaptureService {
    private final CaptureBackend backend;
    private final CaptureSettings settings;
    private final File dir;
    private final ThreadPoolExecutor worker;
    private final AtomicLong sequence = new AtomicLong();
    private volatile boolean running, paused, closed;
    private long nextAt;
    private volatile CaptureListener listener;

    public CaptureService(CaptureBackend backend, CaptureSettings settings, File directory) {
        if (backend == null || settings == null || directory == null) {
            throw new IllegalArgumentException("Capture backend, settings and directory are required");
        }
        this.backend = backend;
        this.settings = settings;
        this.dir = directory;
        worker = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(2), new ThreadFactory() {
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable, "Alpha-BuildTimelapse-Capture");
                thread.setDaemon(true);
                return thread;
            }
        }, new ThreadPoolExecutor.AbortPolicy());
    }

    public void setListener(CaptureListener value) { listener = value; }
    public CaptureSettings getSettings() { return settings; }

    public synchronized void start() {
        if (closed) throw new IllegalStateException("Capture service is shut down");
        if (running) return;
        if ((!dir.exists() && !dir.mkdirs()) || !dir.isDirectory()) {
            throw new IllegalStateException("Cannot create frame directory: " + dir);
        }
        try {
            initializeSequence();
        } catch (IOException failure) {
            throw new IllegalStateException("Cannot safely resume the existing frame sequence", failure);
        }
        running = true;
        paused = false;
        nextAt = System.currentTimeMillis();
    }

    public synchronized void pause() {
        if (running) paused = true;
    }

    public synchronized void resume() {
        if (running && !closed) {
            paused = false;
            nextAt = System.currentTimeMillis();
        }
    }

    public synchronized void stop() {
        running = false;
        paused = false;
    }

    public void tick(final long now) {
        final long index;
        final CaptureSettings snapshot;
        final boolean indexLimitReached;
        synchronized (this) {
            if (closed || !running || paused || now < nextAt) return;
            if (sequence.get() > 99999999L) {
                running = false;
                index = sequence.get();
                snapshot = null;
                indexLimitReached = true;
            } else {
                long interval = settings.getInterval();
                nextAt = now > Long.MAX_VALUE - interval ? Long.MAX_VALUE : now + interval;
                index = sequence.getAndIncrement();
                snapshot = settings.copy();
                indexLimitReached = false;
            }
        }
        if (indexLimitReached) {
            notifyFailure(index, new IOException("Build Timelapse frame index limit reached"));
            return;
        }
        try {
            worker.execute(new Runnable() {
                public void run() {
                    try {
                        File output = new File(dir, String.format(java.util.Locale.ROOT, "frame-%08d.png", index));
                        CaptureFrame frame = backend.capture(index, now, output, snapshot);
                        if (frame == null) throw new IOException("Capture backend returned no frame");
                        CaptureListener current = listener;
                        if (current != null) current.onCaptured(frame);
                    } catch (Exception failure) {
                        notifyFailure(index, failure);
                    }
                }
            });
        } catch (RejectedExecutionException full) {
            notifyFailure(index, new IOException(
                    "Capture queue is full; frame skipped to protect memory and render performance", full));
        }
    }

    private void notifyFailure(long index, Throwable error) {
        CaptureListener current = listener;
        if (current != null) {
            try { current.onFailure(index, error); }
            catch (RuntimeException ignored) { }
        }
    }

    public boolean isRunning() { return running; }
    public boolean isPaused() { return paused; }
    public long count() { return sequence.get(); }
    public int queuedCaptures() { return worker.getQueue().size(); }

    public synchronized void shutdown() {
        if (closed) return;
        stop();
        closed = true;
        worker.shutdownNow();
    }

    private void initializeSequence() throws IOException {
        if (sequence.get() != 0L) return;
        File[] existing = dir.listFiles();
        if (existing == null) {
            throw new IOException("Cannot inspect existing frame files; refusing to overwrite them");
        }
        long next = 0L;
        for (File file : existing) {
            String name = file.getName();
            if (!file.isFile() || !name.matches("frame-[0-9]{8}\\.png")) continue;
            try {
                long index = Long.parseLong(name.substring(6, name.length() - 4));
                if (index >= 99999999L) {
                    throw new IOException("Build Timelapse frame index limit reached");
                }
                next = Math.max(next, index + 1L);
            } catch (NumberFormatException ignored) {
                // Ignore malformed filenames; they are not part of the numbered frame sequence.
            }
        }
        sequence.set(next);
    }
}
