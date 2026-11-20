package com.alpharip.staffmod.RecordingStudio.integration;

import com.alpharip.staffmod.RecordingStudio.timeline.Timeline;
import com.alpharip.staffmod.RecordingStudio.timeline.TimelineClip;
import com.alpharip.staffmod.RecordingStudio.timeline.TimelineTrack;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Streams audio timeline clips in sync with the Studio review playhead. */
public final class RecordingStudioAudioPlayback {
    private static final int BUFFER_SIZE = 16384;
    private final class LoadedClip {
        final String id;
        final File source;
        final long startMillis;
        final long durationMillis;
        final double speed;
        final AudioFormat decodedFormat;
        volatile SourceDataLine line;
        volatile Thread worker;
        volatile boolean started;
        volatile boolean finished;
        volatile long streamGeneration;
        volatile long startedAtTimelineMillis;
        long lastTimelinePosition = -1L;

        LoadedClip(TimelineClip clip, File source, AudioFormat decodedFormat) {
            this.id = clip.getId();
            this.source = source;
            this.startMillis = clip.getStart();
            this.durationMillis = clip.getDuration();
            this.speed = clip.getSpeed();
            this.decodedFormat = decodedFormat;
        }

        void startAt(final long timelinePositionMillis) {
            stopStream();
            started = true;
            finished = false;
            final long generation = ++streamGeneration;
            startedAtTimelineMillis = timelinePositionMillis;
            final long sourceOffsetMillis = Math.max(0L,
                    Math.round((timelinePositionMillis - startMillis) * speed));
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    AudioInputStream sourceStream = null;
                    AudioInputStream pcmStream = null;
                    SourceDataLine output = null;
                    try {
                        sourceStream = AudioSystem.getAudioInputStream(source);
                        AudioFormat pcm = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                                decodedFormat.getSampleRate(), 16, decodedFormat.getChannels(),
                                decodedFormat.getChannels() * 2, decodedFormat.getSampleRate(), false);
                        pcmStream = AudioSystem.getAudioInputStream(pcm, sourceStream);
                        long framesToSkip = Math.round(sourceOffsetMillis * pcm.getFrameRate() / 1000.0D);
                        skipFrames(pcmStream, framesToSkip, pcm.getFrameSize());

                        float outputRate = (float) (pcm.getSampleRate() * speed);
                        if (outputRate < 1000.0F || outputRate > 192000.0F) {
                            throw new IllegalArgumentException("Audio preview speed produces an unsupported sample rate");
                        }
                        AudioFormat outputFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                                outputRate, 16, pcm.getChannels(), pcm.getFrameSize(), outputRate, false);
                        DataLine.Info info = new DataLine.Info(SourceDataLine.class, outputFormat);
                        output = (SourceDataLine) AudioSystem.getLine(info);
                        output.open(outputFormat);
                        if (generation != streamGeneration || Thread.currentThread().isInterrupted()) return;
                        line = output;
                        output.start();

                        byte[] buffer = new byte[BUFFER_SIZE];
                        int count;
                        while (!Thread.currentThread().isInterrupted()
                                && (count = pcmStream.read(buffer, 0, buffer.length)) >= 0) {
                            if (count > 0) output.write(buffer, 0, count);
                        }
                        if (!Thread.currentThread().isInterrupted()) output.drain();
                    } catch (Exception failure) {
                        if (!Thread.currentThread().isInterrupted()) {
                            synchronized (RecordingStudioAudioPlayback.this) {
                                if (generation == streamGeneration) {
                                    status = "Audio preview failed: " + failure.getMessage();
                                }
                            }
                        }
                    } finally {
                        if (output != null) {
                            try { output.stop(); } catch (RuntimeException ignored) { }
                            try { output.close(); } catch (RuntimeException ignored) { }
                        }
                        closeQuietly(pcmStream);
                        closeQuietly(sourceStream);
                        if (generation == streamGeneration) {
                            line = null;
                            started = false;
                            finished = !Thread.currentThread().isInterrupted();
                        }
                    }
                }
            }, "Alpha-RecordingStudio-AudioStream-" + id);
            thread.setDaemon(true);
            worker = thread;
            thread.start();
        }

        void stopStream() {
            streamGeneration++;
            Thread activeWorker = worker;
            worker = null;
            if (activeWorker != null) activeWorker.interrupt();
            SourceDataLine activeLine = line;
            line = null;
            if (activeLine != null) {
                try { activeLine.stop(); } catch (RuntimeException ignored) { }
                try { activeLine.flush(); } catch (RuntimeException ignored) { }
                try { activeLine.close(); } catch (RuntimeException ignored) { }
            }
            started = false;
        }

        void close() { stopStream(); }

        private void skipFrames(AudioInputStream stream, long frames, int frameSize)
                throws java.io.IOException {
            long remaining = frames * (long) frameSize;
            byte[] discard = new byte[8192];
            while (remaining > 0L) {
                long skipped = stream.skip(remaining);
                if (skipped > 0L) {
                    remaining -= skipped;
                } else {
                    int read = stream.read(discard, 0, (int) Math.min(discard.length, remaining));
                    if (read < 0) break;
                    remaining -= read;
                }
            }
        }

        private void closeQuietly(AudioInputStream stream) {
            if (stream == null) return;
            try { stream.close(); } catch (Exception ignored) { }
        }
    }

    private final ExecutorService loader = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "Alpha-RecordingStudio-AudioPreviewLoader");
        thread.setDaemon(true);
        return thread;
    });
    private List<LoadedClip> loaded = Collections.emptyList();
    private String requestedSignature = "";
    private volatile String status = "No audio clips";
    private boolean closed;

    public synchronized void update(Timeline timeline, long positionMillis, boolean playing) {
        if (closed) return;
        List<TimelineClip> clips = getAudioClips(timeline);
        String signature = signature(clips);
        if (!signature.equals(requestedSignature)) {
            requestedSignature = signature;
            closeLoaded();
            status = clips.isEmpty() ? "No audio clips" : "Loading audio clips...";
            if (!clips.isEmpty()) {
                final List<TimelineClip> snapshot = new ArrayList<TimelineClip>(clips);
                final String loadSignature = signature;
                loader.execute(new Runnable() {
                    public void run() {
                        List<LoadedClip> result = new ArrayList<LoadedClip>();
                        String failure = null;
                        for (TimelineClip clip : snapshot) {
                            File source = new File(clip.getSource());
                            if (!source.isFile()) {
                                failure = "Audio file is missing: " + source.getName();
                                continue;
                            }
                            try {
                                javax.sound.sampled.AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(source);
                                AudioFormat sourceFormat = fileFormat.getFormat();
                                float sampleRate = sourceFormat.getSampleRate();
                                int channels = sourceFormat.getChannels();
                                if (sampleRate <= 0.0F || channels <= 0) {
                                    throw new IllegalArgumentException("Unsupported audio format");
                                }
                                AudioFormat decoded = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                                        sampleRate, 16, channels, channels * 2, sampleRate, false);
                                result.add(new LoadedClip(clip, source, decoded));
                            } catch (Exception problem) {
                                failure = "Cannot read audio: " + problem.getMessage();
                            }
                        }
                        synchronized (RecordingStudioAudioPlayback.this) {
                            if (closed || !loadSignature.equals(requestedSignature)) {
                                closeAll(result);
                                return;
                            }
                            loaded = result;
                            status = failure != null ? failure
                                    : result.isEmpty() ? "No playable audio clips"
                                    : "Audio ready (" + result.size() + " clip(s))";
                        }
                    }
                });
            }
        }

        if (!playing) {
            stopLoaded();
            if (status.startsWith("Playing")) status = "Audio paused";
            return;
        }

        boolean active = false;
        for (LoadedClip clip : loaded) {
            long clipEnd = clip.durationMillis > Long.MAX_VALUE - clip.startMillis
                    ? Long.MAX_VALUE : clip.startMillis + clip.durationMillis;
            if (positionMillis < clip.startMillis || positionMillis >= clipEnd) {
                if (clip.started) clip.stopStream();
                clip.lastTimelinePosition = positionMillis;
                continue;
            }
            active = true;
            SourceDataLine line = clip.line;
            long expectedMicros = Math.max(0L, positionMillis - clip.startedAtTimelineMillis) * 1000L;
            boolean seeked = clip.lastTimelinePosition >= 0L
                    && Math.abs(positionMillis - clip.lastTimelinePosition) > 500L;
            if (!clip.started && (!clip.finished || seeked)) {
                clip.startAt(positionMillis);
            } else if (clip.started && line != null
                    && Math.abs(line.getMicrosecondPosition() - expectedMicros) > 250000L) {
                clip.startAt(positionMillis);
            }
            clip.lastTimelinePosition = positionMillis;
        }
        if (active && status.startsWith("Audio ready")) status = "Playing timeline audio";
        else if (!active && !loaded.isEmpty() && status.startsWith("Playing"))
            status = "Audio ready (" + loaded.size() + " clip(s))";
    }

    public synchronized void pause() {
        if (closed) return;
        stopLoaded();
        if (status.startsWith("Playing")) status = "Audio paused";
    }

    public synchronized String getStatus() { return status; }

    public synchronized void close() {
        if (closed) return;
        closed = true;
        loader.shutdownNow();
        closeLoaded();
        requestedSignature = "";
    }

    private void closeLoaded() {
        closeAll(loaded);
        loaded = Collections.emptyList();
    }

    private void stopLoaded() {
        for (LoadedClip clip : loaded) {
            if (clip.started) clip.stopStream();
        }
    }

    private static void closeAll(List<LoadedClip> clips) {
        for (LoadedClip clip : clips) clip.close();
    }

    private static List<TimelineClip> getAudioClips(Timeline timeline) {
        if (timeline == null) return Collections.emptyList();
        for (TimelineTrack track : timeline.getTracks()) {
            if ("audio".equals(track.getId())) return track.getClips();
        }
        return Collections.emptyList();
    }

    private static String signature(List<TimelineClip> clips) {
        StringBuilder value = new StringBuilder();
        for (TimelineClip clip : clips) {
            value.append(clip.getId()).append('|').append(clip.getSource()).append('|')
                    .append(clip.getStart()).append('|').append(clip.getDuration()).append('|')
                    .append(clip.getSpeed()).append(';');
        }
        return value.toString();
    }
}
