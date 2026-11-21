package com.alpharip.staffmod.RecordingStudio.integration;

import com.alpharip.staffmod.RecordingStudio.timeline.Timeline;
import com.alpharip.staffmod.RecordingStudio.timeline.TimelineClip;
import com.alpharip.staffmod.RecordingStudio.timeline.TimelineTrack;
import com.alpharip.staffmod.RecordingStudio.timeline.TimelineTransition;
import com.alpharip.staffmod.RecordingStudio.timeline.TimelineTransitionType;
import com.alpharip.staffmod.RecordingStudio.capture.RecordingFrameTimestamp;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Extracts low-resolution preview frames from external video clips with FFmpeg. */
public final class RecordingStudioExternalVideoPreview {
    private static final class ClipRequest {
        final File source;
        final double sourceSeconds;
        final double fadeFactor;
        final double transitionProgress;
        final double transitionOpacity;
        final TimelineTransitionType transitionType;
        final com.alpharip.staffmod.RecordingStudio.timeline.TimelineVideoEffect effect;
        ClipRequest(File source, double sourceSeconds, double fadeFactor,
                    double transitionProgress, double transitionOpacity, TimelineTransitionType transitionType,
                    com.alpharip.staffmod.RecordingStudio.timeline.TimelineVideoEffect effect) {
            this.source = source;
            this.sourceSeconds = sourceSeconds;
            this.fadeFactor = fadeFactor;
            this.transitionProgress = transitionProgress;
            this.transitionOpacity = transitionOpacity;
            this.transitionType = transitionType;
            this.effect = effect;
        }
    }

    private static final class Request {
        final String key;
        final File capturedFrame;
        final java.util.List<ClipRequest> clips;
        Request(String key, File capturedFrame, java.util.List<ClipRequest> clips) {
            this.key = key;
            this.capturedFrame = capturedFrame;
            this.clips = clips;
        }
    }

    private final File framesDirectory;
    private File[] capturedFrames = new File[0];
    private boolean capturedFramesScanned;
    private long capturedFramesDirectoryModified = Long.MIN_VALUE;
    private final ExecutorService decoder = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "Alpha-RecordingStudio-ExternalVideoPreview");
        thread.setDaemon(true);
        return thread;
    });
    private Request pending;
    private String requestedKey = "";
    private boolean workerRunning;
    private boolean closed;
    private BufferedImage readyImage;
    private String status = "No external video at playhead";
    private volatile Process process;
    private RecordingStudioContinuousVideoDecoder continuousDecoder;
    private String continuousDecoderKey = "";
    private long continuousLastPositionMillis = -1L;
    private DynamicTexture texture;
    private ResourceLocation textureLocation;
    private int textureWidth, textureHeight;

    public RecordingStudioExternalVideoPreview(File framesDirectory) {
        if (framesDirectory == null) throw new IllegalArgumentException("Frames directory is required");
        this.framesDirectory = framesDirectory;
    }

    public synchronized boolean update(Timeline timeline, long positionMillis, long frameIntervalMillis, boolean playing) {
        if (closed) return false;
        java.util.List<TimelineClip> activeClips = findActiveExternalClips(timeline, positionMillis);
        File capturedFrame = findCapturedFrame(timeline, positionMillis, frameIntervalMillis);
        if (activeClips.isEmpty()) {
            stopContinuousDecoder();
            requestedKey = "";
            pending = null;
            readyImage = null;
            status = "No external video at playhead";
            return false;
        }

        if (playing && capturedFrame == null && activeClips.size() == 1
                && isContinuousPreviewEligible(timeline, activeClips.get(0))) {
            return updateContinuousPreview(activeClips.get(0), positionMillis);
        }
        stopContinuousDecoder();

        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(capturedFrame == null ? "no-capture" : capturedFrame.getAbsolutePath()).append('|');
        java.util.List<ClipRequest> requests = new java.util.ArrayList<ClipRequest>();
        for (TimelineClip clip : activeClips) {
            long offsetMillis = Math.max(0L, positionMillis - clip.getStart());
            double sourceSeconds = offsetMillis * clip.getSpeed() / 1000.0D;
            long previewBucket = Math.max(0L, (long) Math.floor(sourceSeconds * 10.0D));
            TimelineTransition incoming = findTransitionTo(timeline, clip);
            TimelineTransition outgoing = findTransitionFrom(timeline, clip);
            double transitionProgress = 1.0D;
            double transitionOpacity = 1.0D;
            TimelineTransitionType transitionType = null;
            if (incoming != null && positionMillis < clip.getStart()) {
                long transitionStart = clip.getStart() - incoming.getDurationMillis();
                transitionProgress = Math.max(0.0D, Math.min(1.0D,
                        (positionMillis - transitionStart) / (double) incoming.getDurationMillis()));
                transitionType = incoming.getType();
                if (transitionType == TimelineTransitionType.CROSS_DISSOLVE) {
                    transitionOpacity = transitionProgress;
                } else if (transitionType == TimelineTransitionType.DIP_TO_BLACK) {
                    transitionOpacity = transitionProgress < 0.5D ? 0.0D : 1.0D;
                }
            }
            keyBuilder.append(clip.getId()).append('|').append(clip.getSource())
                    .append('|').append(clip.getEffect().name())
                    .append("|fade=").append(clip.getFadeInMillis()).append(':').append(clip.getFadeOutMillis())
                    .append("|transition=").append(transitionType).append(':')
                    .append(String.format(Locale.ROOT, "%.3f", transitionProgress)).append(':')
                    .append(String.format(Locale.ROOT, "%.3f", transitionOpacity))
                    .append('|').append(previewBucket).append(';');
            File source = new File(clip.getSource());
            if (!source.isFile()) {
                requestedKey = keyBuilder.toString();
                pending = null;
                readyImage = null;
                status = "External video file not found: " + source.getName();
                return true;
            }
            double fadeFactor = 1.0D;
            if (clip.getFadeInMillis() > 0L) {
                fadeFactor = Math.min(fadeFactor, offsetMillis / (double) clip.getFadeInMillis());
            }
            if (clip.getFadeOutMillis() > 0L) {
                fadeFactor = Math.min(fadeFactor,
                        (clip.getDuration() - offsetMillis) / (double) clip.getFadeOutMillis());
            }
            if (incoming != null && transitionType == TimelineTransitionType.DIP_TO_BLACK) {
                fadeFactor *= transitionProgress < 0.5D
                        ? 0.0D : Math.max(0.0D, Math.min(1.0D, (transitionProgress - 0.5D) * 2.0D));
            }
            if (outgoing != null && outgoing.getType() == TimelineTransitionType.DIP_TO_BLACK) {
                long transitionStart = clip.getEnd() - outgoing.getDurationMillis();
                if (positionMillis >= transitionStart && positionMillis < clip.getEnd()) {
                    double progress = (positionMillis - transitionStart) / (double) outgoing.getDurationMillis();
                    fadeFactor *= Math.max(0.0D, 1.0D - Math.min(1.0D, progress * 2.0D));
                }
            }
            fadeFactor = Math.max(0.0D, Math.min(1.0D, fadeFactor));
            requests.add(new ClipRequest(source, sourceSeconds, fadeFactor,
                    transitionProgress, transitionOpacity, transitionType, clip.getEffect()));
        }

        String key = keyBuilder.toString();
        if (!key.equals(requestedKey)) {
            requestedKey = key;
            readyImage = null;
            pending = new Request(key, capturedFrame, requests);
            int layerCount = requests.size() + (capturedFrame == null ? 0 : 1);
            status = "Compositing " + layerCount + " video layer(s)...";
            if (!workerRunning) {
                workerRunning = true;
                decoder.execute(new Runnable() {
                    public void run() { decodePendingRequests(); }
                });
            }
        }
        return true;
    }

    private static boolean isContinuousPreviewEligible(Timeline timeline, TimelineClip clip) {
        if (clip == null || clip.getDuration() <= 0L || clip.getSpeed() != 1.0D
                || clip.getFadeInMillis() > 0L || clip.getFadeOutMillis() > 0L
                || clip.getEffect() == null || !"NONE".equals(clip.getEffect().name())) return false;
        return findTransitionTo(timeline, clip) == null && findTransitionFrom(timeline, clip) == null;
    }

    private boolean updateContinuousPreview(TimelineClip clip, long positionMillis) {
        File source = new File(clip.getSource());
        if (!source.isFile()) {
            stopContinuousDecoder();
            status = "External video file not found: " + source.getName();
            return true;
        }
        long offsetMillis = Math.max(0L, positionMillis - clip.getStart());
        String key = clip.getId() + "|" + source.getAbsolutePath();
        boolean discontinuity = continuousLastPositionMillis >= 0L
                && (positionMillis < continuousLastPositionMillis
                    || positionMillis - continuousLastPositionMillis > 500L);
        if (continuousDecoder == null || !key.equals(continuousDecoderKey) || discontinuity) {
            stopContinuousDecoder();
            try {
                continuousDecoder = new RecordingStudioContinuousVideoDecoder(source, offsetMillis / 1000.0D);
                continuousDecoderKey = key;
                status = "Starting native-rate video preview...";
            } catch (Exception failure) {
                continuousDecoder = null;
                status = "Continuous video preview failed: " + failure.getMessage();
                continuousLastPositionMillis = positionMillis;
                return true;
            }
        }
        BufferedImage nextFrame = continuousDecoder.pollFrame();
        if (nextFrame != null) {
            readyImage = nextFrame;
            status = "External video preview (continuous source-rate playback)";
        } else if (continuousDecoder.getFailure() != null) {
            status = "Continuous video preview failed: " + continuousDecoder.getFailure();
        } else if (!continuousDecoder.isRunning()) {
            status = "External video preview stream ended";
        }
        continuousLastPositionMillis = positionMillis;
        return true;
    }

    private void stopContinuousDecoder() {
        if (continuousDecoder != null) {
            continuousDecoder.close();
            continuousDecoder = null;
        }
        continuousDecoderKey = "";
        continuousLastPositionMillis = -1L;
    }

    private void decodePendingRequests() {
        while (true) {
            final Request request;
            synchronized (this) {
                request = pending;
                pending = null;
                if (request == null || closed) { workerRunning = false; return; }
            }
            BufferedImage image = null;
            String resultStatus;
            try {
                image = decode(request);
                int layerCount = request.clips.size() + (request.capturedFrame == null ? 0 : 1);
                resultStatus = "External video preview (" + layerCount + " layer(s))";
            }
            catch (Exception failure) { resultStatus = "External video preview failed: " + failure.getMessage(); }
            synchronized (this) {
                if (closed) { workerRunning = false; return; }
                if (request.key.equals(requestedKey)) {
                    readyImage = image;
                    status = resultStatus;
                }
            }
        }
    }

    private BufferedImage decode(Request request) throws Exception {
        File log = File.createTempFile("alpha-recording-preview-", ".log");
        Process activeProcess = null;
        try {
            java.util.List<String> command = new java.util.ArrayList<String>();
            java.util.Collections.addAll(command, "ffmpeg", "-hide_banner", "-loglevel", "error");
            if (request.capturedFrame != null) {
                command.add("-i");
                command.add(request.capturedFrame.getAbsolutePath());
            }
            for (ClipRequest clip : request.clips) {
                command.add("-ss");
                command.add(String.format(Locale.ROOT, "%.3f", clip.sourceSeconds));
                command.add("-i");
                command.add(clip.source.getAbsolutePath());
            }

            StringBuilder filter = new StringBuilder();
            if (request.capturedFrame != null) {
                filter.append("[0:v:0]scale=1280:720:force_original_aspect_ratio=decrease")
                        .append(",pad=1280:720:(ow-iw)/2:(oh-ih)/2:black,setsar=1[base]");
            } else {
                filter.append("color=c=black:s=1280x720:r=1:d=1[base]");
            }
            String currentBase = "[base]";
            int firstExternalInput = request.capturedFrame == null ? 0 : 1;
            for (int i = 0; i < request.clips.size(); i++) {
                ClipRequest clip = request.clips.get(i);
                String clipLabel = "[clip" + i + "]";
                String nextBase = "[base" + (i + 1) + "]";
                filter.append(";[").append(firstExternalInput + i).append(":v:0]")
                        .append("trim=duration=1,setpts=PTS-STARTPTS,fps=1")
                        .append(",scale=1280:720:force_original_aspect_ratio=decrease")
                        .append(",pad=1280:720:(ow-iw)/2:(oh-ih)/2:black,setsar=1");
                String effectFilter = clip.effect.getFfmpegFilter();
                if (!effectFilter.isEmpty()) filter.append(',').append(effectFilter);
                if (clip.fadeFactor < 0.999999D) {
                    String factor = String.format(Locale.ROOT, "%.6f", clip.fadeFactor);
                    filter.append(",lutrgb=r='val*").append(factor)
                            .append("':g='val*").append(factor)
                            .append("':b='val*").append(factor).append("'");
                }
                if (clip.transitionType == TimelineTransitionType.CROSS_DISSOLVE) {
                    filter.append(",format=rgba,colorchannelmixer=aa=")
                            .append(String.format(Locale.ROOT, "%.6f", clip.transitionOpacity));
                } else if (clip.transitionType == TimelineTransitionType.DIP_TO_BLACK) {
                    filter.append(",format=rgba,geq=r='r(X,Y)':g='g(X,Y)':b='b(X,Y)':a='")
                            .append(clip.transitionOpacity < 0.5D ? "0" : "255").append("'");
                } else if (clip.transitionType == TimelineTransitionType.WIPE_LEFT) {
                    filter.append(",format=rgba,geq=r='r(X,Y)':g='g(X,Y)':b='b(X,Y)':a='if(lt(X,W*")
                            .append(String.format(Locale.ROOT, "%.6f", clip.transitionProgress)).append("),255,0)'");
                } else if (clip.transitionType == TimelineTransitionType.WIPE_RIGHT) {
                    filter.append(",format=rgba,geq=r='r(X,Y)':g='g(X,Y)':b='b(X,Y)':a='if(gt(X,W*(1-")
                            .append(String.format(Locale.ROOT, "%.6f", clip.transitionProgress)).append(")),255,0)'");
                }
                filter.append(clipLabel);
                filter.append(';').append(currentBase).append(clipLabel);
                if (clip.transitionType == TimelineTransitionType.SLIDE_LEFT) {
                    filter.append("overlay=x='W*(1-")
                            .append(String.format(Locale.ROOT, "%.6f", clip.transitionProgress))
                            .append(")':y=0:shortest=1:format=auto");
                } else if (clip.transitionType == TimelineTransitionType.SLIDE_RIGHT) {
                    filter.append("overlay=x='-W*(1-")
                            .append(String.format(Locale.ROOT, "%.6f", clip.transitionProgress))
                            .append(")':y=0:shortest=1:format=auto");
                } else {
                    filter.append("overlay=shortest=1:format=auto");
                }
                filter.append(nextBase);
                currentBase = nextBase;
            }
            filter.append(';').append(currentBase).append("format=rgb24[out]");
            java.util.Collections.addAll(command, "-filter_complex", filter.toString(),
                    "-map", "[out]", "-frames:v", "1", "-f", "image2pipe",
                    "-vcodec", "png", "pipe:1");

            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectError(log);
            activeProcess = builder.start();
            process = activeProcess;
            BufferedImage image = ImageIO.read(activeProcess.getInputStream());
            int exit = activeProcess.waitFor();
            if (exit != 0) throw new IOException("FFmpeg exited with code " + exit);
            if (image == null) throw new IOException("FFmpeg returned no preview frame");
            return image;
        } finally {
            process = null;
            if (activeProcess != null && activeProcess.isAlive()) {
                activeProcess.destroy();
                if (!activeProcess.waitFor(250L, java.util.concurrent.TimeUnit.MILLISECONDS))
                    activeProcess.destroyForcibly();
            }
            if (!log.delete()) log.deleteOnExit();
        }
    }

    private synchronized File findCapturedFrame(Timeline timeline, long positionMillis, long frameIntervalMillis) {
        if (timeline == null || frameIntervalMillis < 1L) return null;
        TimelineClip captureClip = null;
        for (TimelineTrack track : timeline.getTracks()) {
            if (!"video".equals(track.getId())) continue;
            for (TimelineClip clip : track.getClips()) {
                if ("@capture".equals(clip.getSource()) && clip.getDuration() > 0L
                        && positionMillis >= clip.getStart() && positionMillis < clip.getEnd()) {
                    captureClip = clip;
                    break;
                }
            }
            break;
        }
        if (captureClip == null) return null;
        long directoryModified = framesDirectory.isDirectory()
                ? framesDirectory.lastModified() : -1L;
        if (!capturedFramesScanned || directoryModified != capturedFramesDirectoryModified) {
            File[] found = framesDirectory.listFiles((directory, name) ->
                    name.matches("frame-[0-9]{8}\\.png") && new File(directory, name).isFile());
            if (found == null) found = new File[0];
            java.util.Arrays.sort(found, new java.util.Comparator<File>() {
                public int compare(File left, File right) { return left.getName().compareTo(right.getName()); }
            });
            capturedFrames = found;
            capturedFramesScanned = true;
            capturedFramesDirectoryModified = directoryModified;
        }
        long targetTimestamp = Math.max(0L, positionMillis);
        int low = 0;
        int high = capturedFrames.length - 1;
        int selected = -1;
        while (low <= high) {
            int middle = low + (high - low) / 2;
            long fallback = middle > Long.MAX_VALUE / frameIntervalMillis
                    ? Long.MAX_VALUE : (long) middle * frameIntervalMillis;
            long frameTimestamp = RecordingFrameTimestamp.read(capturedFrames[middle], fallback);
            if (frameTimestamp <= targetTimestamp) {
                selected = middle;
                low = middle + 1;
            } else {
                high = middle - 1;
            }
        }
        return selected < 0 ? null : capturedFrames[selected];
    }

    private static TimelineTransition findTransitionTo(Timeline timeline, TimelineClip clip) {
        for (TimelineTransition transition : timeline.getTransitions()) {
            if (!transition.getToClipId().equals(clip.getId())) continue;
            for (TimelineTrack track : timeline.getTracks()) {
                if (!"video".equals(track.getId())) continue;
                for (TimelineClip previous : track.getClips()) {
                    if (previous.getId().equals(transition.getFromClipId())
                            && previous.getEnd() == clip.getStart()) return transition;
                }
            }
        }
        return null;
    }

    private static TimelineTransition findTransitionFrom(Timeline timeline, TimelineClip clip) {
        for (TimelineTransition transition : timeline.getTransitions()) {
            if (!transition.getFromClipId().equals(clip.getId())) continue;
            for (TimelineTrack track : timeline.getTracks()) {
                if (!"video".equals(track.getId())) continue;
                for (TimelineClip next : track.getClips()) {
                    if (next.getId().equals(transition.getToClipId())
                            && clip.getEnd() == next.getStart()) return transition;
                }
            }
        }
        return null;
    }

    private static java.util.List<TimelineClip> findActiveExternalClips(
            Timeline timeline, long positionMillis) {
        java.util.List<TimelineClip> active = new java.util.ArrayList<TimelineClip>();
        if (timeline == null) return active;
        for (TimelineTrack track : timeline.getTracks()) {
            if (!"video".equals(track.getId())) continue;
            for (TimelineClip clip : track.getClips()) {
                if ("@capture".equals(clip.getSource()) || clip.getDuration() <= 0L) continue;
                TimelineTransition incoming = findTransitionTo(timeline, clip);
                long visibleStart = incoming == null ? clip.getStart()
                        : Math.max(0L, clip.getStart() - incoming.getDurationMillis());
                if (positionMillis >= visibleStart && positionMillis < clip.getEnd()) active.add(clip);
            }
            break;
        }
        java.util.Collections.sort(active, new java.util.Comparator<TimelineClip>() {
            public int compare(TimelineClip left, TimelineClip right) {
                int byStart = Long.compare(left.getStart(), right.getStart());
                return byStart != 0 ? byStart : left.getId().compareTo(right.getId());
            }
        });
        return active;
    }

    public synchronized String getStatus() { return status; }

    public synchronized void draw(Minecraft minecraft, int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) return;
        Gui.drawRect(x, y, x + width, y + height, 0xFF080808);
        if (readyImage != null) { updateTexture(minecraft, readyImage); readyImage = null; }
        else if (!status.startsWith("External video preview") && !status.startsWith("Compositing ") && textureLocation != null) {
            minecraft.getTextureManager().deleteTexture(textureLocation);
            textureLocation = null; texture = null;
        }
        if (textureLocation == null) {
            minecraft.fontRendererObj.drawString(status, x + 8, y + 8, 0xFFFFD65A);
            return;
        }
        float scale = Math.min(width / (float) textureWidth, height / (float) textureHeight);
        int drawWidth = Math.max(1, Math.round(textureWidth * scale));
        int drawHeight = Math.max(1, Math.round(textureHeight * scale));
        int drawX = x + (width - drawWidth) / 2;
        int drawY = y + (height - drawHeight) / 2;
        minecraft.getTextureManager().bindTexture(textureLocation);
        Gui.drawModalRectWithCustomSizedTexture(drawX, drawY, 0.0F, 0.0F,
                drawWidth, drawHeight, textureWidth, textureHeight);
        minecraft.fontRendererObj.drawString(status, x + 8, y + height - 12, 0xFFFFD65A);
    }

    private void updateTexture(Minecraft minecraft, BufferedImage image) {
        if (texture == null || image.getWidth() != textureWidth || image.getHeight() != textureHeight) {
            if (textureLocation != null) minecraft.getTextureManager().deleteTexture(textureLocation);
            textureWidth = image.getWidth(); textureHeight = image.getHeight();
            texture = new DynamicTexture(image);
            textureLocation = minecraft.getTextureManager().getDynamicTextureLocation(
                    "alpha-recording-studio-external-preview", texture);
            return;
        }
        image.getRGB(0, 0, textureWidth, textureHeight, texture.getTextureData(), 0, textureWidth);
        texture.updateDynamicTexture();
    }

    public synchronized void close(Minecraft minecraft) {
        if (closed) return;
        closed = true; pending = null; decoder.shutdownNow();
        stopContinuousDecoder();
        Process activeProcess = process;
        if (activeProcess != null) activeProcess.destroyForcibly();
        readyImage = null;
        if (minecraft != null && textureLocation != null) minecraft.getTextureManager().deleteTexture(textureLocation);
        textureLocation = null; texture = null;
    }
}
