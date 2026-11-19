package com.alpharip.staffmod.RecordingStudio.integration;

import com.alpharip.staffmod.RecordingStudio.timeline.Timeline;
import com.alpharip.staffmod.RecordingStudio.timeline.TimelineClip;
import com.alpharip.staffmod.RecordingStudio.timeline.TimelineTrack;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Plays supported audio timeline clips in sync with the Studio review playhead. */
public final class RecordingStudioAudioPlayback {
    private static final class LoadedClip {
        final String id;
        final long startMillis;
        final long durationMillis;
        final double speed;
        final Clip audio;

        LoadedClip(TimelineClip timelineClip, Clip audio) {
            this.id = timelineClip.getId();
            this.startMillis = timelineClip.getStart();
            this.durationMillis = timelineClip.getDuration();
            this.speed = timelineClip.getSpeed();
            this.audio = audio;
        }

        void close() {
            audio.stop();
            audio.close();
        }
    }

    private final ExecutorService loader = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "Alpha-RecordingStudio-AudioPreview");
        thread.setDaemon(true);
        return thread;
    });
    private List<LoadedClip> loaded = Collections.emptyList();
    private String requestedSignature = "";
    private String status = "No audio clips";
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
                            if (clip.getSpeed() != 1.0D) {
                                failure = "Audio preview requires 1x clip speed";
                                continue;
                            }
                            File source = new File(clip.getSource());
                            if (!source.isFile()) {
                                failure = "Audio file is missing: " + source.getName();
                                continue;
                            }
                            try {
                                AudioInputStream stream = AudioSystem.getAudioInputStream(source);
                                try {
                                    Clip audio = AudioSystem.getClip();
                                    audio.open(stream);
                                    result.add(new LoadedClip(clip, audio));
                                } finally {
                                    stream.close();
                                }
                            } catch (Exception problem) {
                                failure = "Cannot load audio: " + problem.getMessage();
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
            if (status.startsWith("Playing")) status = "Audio ready (" + loaded.size() + " clip(s))";
            return;
        }

        boolean active = false;
        for (LoadedClip clip : loaded) {
            long clipEnd = clip.durationMillis > Long.MAX_VALUE - clip.startMillis
                    ? Long.MAX_VALUE : clip.startMillis + clip.durationMillis;
            if (positionMillis < clip.startMillis || positionMillis >= clipEnd) {
                if (clip.audio.isRunning()) clip.audio.stop();
                continue;
            }
            long offsetMillis = Math.max(0L, positionMillis - clip.startMillis);
            long expectedMicros = Math.min(clip.audio.getMicrosecondLength(), offsetMillis * 1000L);
            long actualMicros = clip.audio.getMicrosecondPosition();
            if (!clip.audio.isRunning() || Math.abs(actualMicros - expectedMicros) > 250000L) {
                clip.audio.stop();
                clip.audio.setMicrosecondPosition(expectedMicros);
                clip.audio.start();
            }
            active = true;
        }
        if (active && status.startsWith("Audio ready")) status = "Playing timeline audio";
        else if (!active && !loaded.isEmpty() && status.startsWith("Playing")) status = "Audio ready (" + loaded.size() + " clip(s))";
    }

    public synchronized void pause() {
        if (closed) return;
        stopLoaded();
        if (status.startsWith("Playing")) status = "Audio paused";
    }

    public synchronized String getStatus() {
        return status;
    }

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
            if (clip.audio.isRunning()) clip.audio.stop();
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
