package com.alpharip.staffmod.RecordingStudio.export;

import com.alpharip.staffmod.RecordingStudio.timeline.Timeline;
import com.alpharip.staffmod.RecordingStudio.timeline.TimelineClip;
import com.alpharip.staffmod.RecordingStudio.timeline.TimelineTrack;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Standalone FFmpeg exporter for Recording Studio frame sequences and audio timeline clips. */
public final class RecordingStudioExportService {
    public enum Format { MP4, GIF }

    private final File framesDirectory;
    private final File outputDirectory;
    private final Timeline timeline;
    private volatile String status = "Export idle";
    private volatile Process process;
    private volatile boolean cancelled;
    private Thread worker;

    public RecordingStudioExportService(File framesDirectory, File outputDirectory, Timeline timeline) {
        if (framesDirectory == null || outputDirectory == null || timeline == null) {
            throw new IllegalArgumentException("Frame directory, output directory and timeline are required");
        }
        this.framesDirectory = framesDirectory;
        this.outputDirectory = outputDirectory;
        this.timeline = timeline;
    }

    public synchronized void start(final Format format, final double fps, final String executable) throws IOException {
        if (format == null) throw new IllegalArgumentException("Export format is required");
        if (Double.isNaN(fps) || Double.isInfinite(fps) || fps <= 0.0 || fps > 240.0) throw new IllegalArgumentException("FPS must be greater than 0 and at most 240");
        if (worker != null && worker.isAlive()) throw new IllegalStateException("An export is already running");
        File[] frames = listFrames();
        if (frames.length == 0) throw new IOException("No captured PNG frames found");
        if ((!outputDirectory.exists() && !outputDirectory.mkdirs()) || !outputDirectory.isDirectory()) {
            throw new IOException("Cannot create export directory");
        }
        final String command = executable == null || executable.trim().isEmpty() ? "ffmpeg" : executable.trim();
        final File output = new File(outputDirectory,
                "recording-" + System.currentTimeMillis() + (format == Format.MP4 ? ".mp4" : ".gif"));
        cancelled = false;
        status = "Exporting " + format.name() + " (" + frames.length + " frames)";
        worker = new Thread(new Runnable() {
            public void run() {
                export(format, fps, command, output);
            }
        }, "Alpha-RecordingStudio-Export");
        worker.setDaemon(true);
        worker.start();
    }

    public synchronized void cancel() {
        if (worker == null || !worker.isAlive()) return;
        cancelled = true;
        status = "Cancelling export";
        Process active = process;
        if (active != null) active.destroy();
        worker.interrupt();
    }

    public synchronized boolean isRunning() { return worker != null && worker.isAlive(); }
    public String getStatus() { return status; }

    private void export(Format format, double fps, String executable, File output) {
        String extension = format == Format.MP4 ? ".mp4" : ".gif";
        String base = output.getName().substring(0, output.getName().length() - extension.length());
        File partial = new File(outputDirectory, base + ".partial" + extension);
        File palette = null;
        try {
            File[] frames = listFrames();
            if (frames.length == 0) throw new IOException("No captured PNG frames found");
            BufferedImage first = ImageIO.read(frames[0]);
            if (first == null) throw new IOException("Cannot read the first captured PNG frame");
            String input = new File(framesDirectory, "frame-*.png").getAbsolutePath().replace(File.separatorChar, '/');

            List<TimelineClip> videoClips = getVideoClips();
            List<TimelineClip> audioClips = format == Format.MP4 ? getAudioClips() : Collections.<TimelineClip>emptyList();
            List<File> videoSources = new ArrayList<File>();
            List<Integer> videoInputIndexes = new ArrayList<Integer>();
            for (TimelineClip clip : videoClips) {
                if ("@capture".equals(clip.getSource())) {
                    videoInputIndexes.add(0);
                } else {
                    File source = new File(clip.getSource());
                    if (!source.isFile()) throw new FileNotFoundException("Video timeline source not found: " + source);
                    videoSources.add(source);
                    videoInputIndexes.add(videoSources.size());
                }
            }

            long frameDurationMillis = Math.max(1L, (long) Math.ceil(frames.length * 1000.0 / fps));
            long videoDurationMillis = videoClips.isEmpty()
                    ? frameDurationMillis : getTrackEnd(videoClips);
            long outputDurationMillis = Math.max(videoDurationMillis, getTrackEnd(audioClips));
            int outputWidth = first.getWidth() + (first.getWidth() & 1);
            int outputHeight = first.getHeight() + (first.getHeight() & 1);
            String videoFilter = buildVideoFilter(videoClips, videoInputIndexes, outputWidth,
                    outputHeight, fps, outputDurationMillis);

            if (format == Format.MP4) {
                List<String> command = baseCommand(executable, input, fps);
                for (File source : videoSources) {
                    command.add("-i");
                    command.add(source.getAbsolutePath());
                }
                for (TimelineClip clip : audioClips) {
                    File audio = new File(clip.getSource());
                    if (!audio.isFile()) throw new FileNotFoundException("Audio timeline source not found: " + audio);
                    command.add("-i");
                    command.add(audio.getAbsolutePath());
                }

                String audioFilter = buildAudioFilter(audioClips, 1 + videoSources.size());
                String filter = videoFilter;
                if (!audioFilter.isEmpty()) filter = filter.isEmpty() ? audioFilter : filter + ";" + audioFilter;
                if (!filter.isEmpty()) {
                    command.add("-filter_complex");
                    command.add(filter);
                }
                if (videoFilter.isEmpty()) {
                    Collections.addAll(command, "-map", "0:v:0", "-vf",
                            "pad=ceil(iw/2)*2:ceil(ih/2)*2");
                } else {
                    Collections.addAll(command, "-map", "[vout]");
                }
                Collections.addAll(command, "-c:v", "libx264", "-pix_fmt", "yuv420p",
                        "-movflags", "+faststart", "-t", seconds(outputDurationMillis));
                if (audioClips.isEmpty()) {
                    command.add("-an");
                } else {
                    command.add("-map");
                    command.add(audioClips.size() == 1 ? "[a0]" : "[aout]");
                    Collections.addAll(command, "-c:a", "aac", "-b:a", "192k");
                }
                command.add("-y");
                command.add(partial.getAbsolutePath());
                run(command, "MP4 export");
            } else {
                palette = File.createTempFile("alpha-recording-palette-", ".png", outputDirectory);
                List<String> paletteCommand = baseCommand(executable, input, fps);
                for (File source : videoSources) {
                    paletteCommand.add("-i");
                    paletteCommand.add(source.getAbsolutePath());
                }
                if (videoFilter.isEmpty()) {
                    Collections.addAll(paletteCommand, "-vf", "palettegen=stats_mode=diff",
                            "-frames:v", "1", "-y", palette.getAbsolutePath());
                } else {
                    Collections.addAll(paletteCommand, "-filter_complex",
                            videoFilter + ";[vout]palettegen=stats_mode=diff[palette]",
                            "-map", "[palette]", "-frames:v", "1", "-y", palette.getAbsolutePath());
                }
                run(paletteCommand, "GIF palette generation");

                List<String> gifCommand = baseCommand(executable, input, fps);
                for (File source : videoSources) {
                    gifCommand.add("-i");
                    gifCommand.add(source.getAbsolutePath());
                }
                gifCommand.add("-i");
                gifCommand.add(palette.getAbsolutePath());
                if (videoFilter.isEmpty()) {
                    Collections.addAll(gifCommand, "-lavfi",
                            "fps=" + formatRate(fps) + "[x];[x][" + (1 + videoSources.size())
                                    + ":v]paletteuse=dither=sierra2_4a",
                            "-t", seconds(outputDurationMillis), "-y", partial.getAbsolutePath());
                } else {
                    Collections.addAll(gifCommand, "-filter_complex",
                            videoFilter + ";[vout][" + (1 + videoSources.size())
                                    + ":v]paletteuse=dither=sierra2_4a[vfinal]",
                            "-map", "[vfinal]", "-t", seconds(outputDurationMillis),
                            "-y", partial.getAbsolutePath());
                }
                run(gifCommand, "GIF export");
            }

            if (cancelled || Thread.currentThread().isInterrupted()) throw new InterruptedException("Export cancelled");
            if (!partial.isFile() || partial.length() == 0L) throw new IOException("FFmpeg did not create a valid export");
            Files.move(partial.toPath(), output.toPath(), StandardCopyOption.REPLACE_EXISTING);
            status = "Export complete: " + output.getAbsolutePath();
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            status = "Export cancelled";
        } catch (Exception failure) {
            status = "Export failed: " + failure.getMessage();
        } finally {
            process = null;
            if (palette != null && palette.exists() && !palette.delete()) palette.deleteOnExit();
            if (partial.exists() && !partial.delete()) partial.deleteOnExit();
        }
    }

    private List<String> baseCommand(String executable, String input, double fps) {
        List<String> command = new ArrayList<String>();
        Collections.addAll(command, executable, "-hide_banner", "-loglevel", "error",
                "-framerate", formatRate(fps), "-pattern_type", "glob", "-i", input);
        return command;
    }

    private static String formatRate(double fps) {
        return String.format(Locale.ROOT, "%.6f", fps);
    }

    private List<TimelineClip> getAudioClips() {
        for (TimelineTrack track : timeline.getTracks()) {
            if ("audio".equals(track.getId())) {
                List<TimelineClip> valid = new ArrayList<TimelineClip>();
                for (TimelineClip clip : track.getClips()) {
                    if (clip.getDuration() > 0L) valid.add(clip);
                }
                return valid;
            }
        }
        return Collections.emptyList();
    }

    private List<TimelineClip> getVideoClips() {
        for (TimelineTrack track : timeline.getTracks()) {
            if ("video".equals(track.getId())) {
                List<TimelineClip> valid = new ArrayList<TimelineClip>();
                for (TimelineClip clip : track.getClips()) {
                    if (clip.getDuration() > 0L) valid.add(clip);
                }
                Collections.sort(valid, new Comparator<TimelineClip>() {
                    public int compare(TimelineClip left, TimelineClip right) {
                        boolean leftCapture = "@capture".equals(left.getSource());
                        boolean rightCapture = "@capture".equals(right.getSource());
                        if (leftCapture != rightCapture) return leftCapture ? -1 : 1;
                        return Long.compare(left.getStart(), right.getStart());
                    }
                });
                return valid;
            }
        }
        return Collections.emptyList();
    }

    private static long getTrackEnd(List<TimelineClip> clips) {
        long end = 0L;
        for (TimelineClip clip : clips) end = Math.max(end, clip.getEnd());
        return end;
    }

    /**
     * Composes video-track clips over a black timeline background. The reserved @capture
     * source maps to the project's captured PNG sequence; other sources are FFmpeg video inputs.
     */
    private static String buildVideoFilter(List<TimelineClip> clips, List<Integer> inputIndexes,
                                            int width, int height, double fps, long durationMillis) {
        if (clips.isEmpty()) return "";
        String rate = formatRate(fps);
        StringBuilder filter = new StringBuilder();
        filter.append("color=c=black:s=").append(width).append('x').append(height)
                .append(":r=").append(rate).append(":d=").append(seconds(durationMillis)).append("[vbase]");
        String currentBase = "[vbase]";
        for (int i = 0; i < clips.size(); i++) {
            TimelineClip clip = clips.get(i);
            double speed = clip.getSpeed();
            double sourceDuration = clip.getDuration() * speed;
            long start = clip.getStart();
            long end = clip.getEnd();
            String clipLabel = "[vclip" + i + "]";
            String nextBase = "[vbase" + (i + 1) + "]";
            filter.append(";[").append(inputIndexes.get(i)).append(":v:0]")
                    .append("trim=duration=").append(seconds(sourceDuration))
                    .append(",setpts=(PTS-STARTPTS)/")
                    .append(String.format(Locale.ROOT, "%.6f", speed))
                    .append(",fps=").append(rate)
                    .append(",scale=").append(width).append(':').append(height)
                    .append(":force_original_aspect_ratio=decrease")
                    .append(",pad=").append(width).append(':').append(height)
                    .append(":(ow-iw)/2:(oh-ih)/2:black,setsar=1")
                    .append(",setpts=PTS+").append(seconds(start)).append("/TB")
                    .append(clipLabel);
            filter.append(';').append(currentBase).append(clipLabel)
                    .append("overlay=eof_action=pass:shortest=0:enable='between(t,")
                    .append(seconds(start)).append(',').append(seconds(end)).append(")'")
                    .append(nextBase);
            currentBase = nextBase;
        }
        filter.append(';').append(currentBase).append("format=yuv420p[vout]");
        return filter.toString();
    }

    private String buildAudioFilter(List<TimelineClip> clips, int firstInputIndex) {
        if (clips.isEmpty()) return "";
        StringBuilder filter = new StringBuilder();
        for (int i = 0; i < clips.size(); i++) {
            TimelineClip clip = clips.get(i);
            long delay = Math.max(0L, clip.getStart());
            long timelineDuration = Math.max(0L, clip.getDuration());
            double speed = clip.getSpeed();
            double sourceDuration = timelineDuration * speed;

            if (i > 0) filter.append(';');
            filter.append('[').append(firstInputIndex + i).append(":a]")
                    .append("atrim=duration=").append(seconds(sourceDuration))
                    .append(",asetpts=PTS-STARTPTS");

            String tempoFilters = buildAtempoFilters(speed);
            if (!tempoFilters.isEmpty()) filter.append(',').append(tempoFilters);

            // Clip duration is timeline time. Speed changes how much source audio is
            // consumed, while padding/trimming keeps the clip's timeline length stable.
            filter.append(",apad,atrim=duration=").append(seconds(timelineDuration))
                    .append(",adelay=delays=").append(delay).append(":all=1[a").append(i).append(']');
        }
        if (clips.size() > 1) {
            filter.append(';');
            for (int i = 0; i < clips.size(); i++) filter.append("[a").append(i).append(']');
            filter.append("amix=inputs=").append(clips.size())
                    .append(":duration=longest:dropout_transition=0[aout]");
        }
        return filter.toString();
    }

    private static String buildAtempoFilters(double speed) {
        StringBuilder filters = new StringBuilder();
        double remaining = speed;
        while (remaining > 2.0) {
            appendFilter(filters, "atempo=2.0");
            remaining /= 2.0;
        }
        while (remaining < 0.5) {
            appendFilter(filters, "atempo=0.5");
            remaining /= 0.5;
        }
        if (Math.abs(remaining - 1.0) > 0.000001) {
            appendFilter(filters, "atempo=" + String.format(Locale.ROOT, "%.6f", remaining));
        }
        return filters.toString();
    }

    private static void appendFilter(StringBuilder filters, String value) {
        if (filters.length() > 0) filters.append(',');
        filters.append(value);
    }

    private static String seconds(double millis) {
        return String.format(Locale.ROOT, "%.6f", millis / 1000.0);
    }

    private File[] listFrames() throws IOException {
        if (!framesDirectory.isDirectory()) throw new FileNotFoundException("Frame directory not found: " + framesDirectory);
        File[] frames = framesDirectory.listFiles((dir, name) -> name.matches("frame-[0-9]{8,}\\.png"));
        if (frames == null) throw new IOException("Cannot read captured frame directory");
        Arrays.sort(frames, new Comparator<File>() {
            public int compare(File left, File right) {
                long leftIndex = frameIndex(left);
                long rightIndex = frameIndex(right);
                return Long.compare(leftIndex, rightIndex);
            }
        });
        return frames;
    }

    private static long frameIndex(File frame) {
        String name = frame.getName();
        return Long.parseLong(name.substring("frame-".length(), name.length() - ".png".length()));
    }

    private void run(List<String> command, String operation) throws Exception {
        if (cancelled || Thread.currentThread().isInterrupted()) throw new InterruptedException("Export cancelled");
        File logFile = File.createTempFile("alpha-recording-export-", ".log", outputDirectory);
        Process active = null;
        try {
            active = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(logFile).start();
            process = active;
            int exit;
            try {
                exit = active.waitFor();
            } catch (InterruptedException interrupted) {
                terminate(active);
                Thread.currentThread().interrupt();
                throw new InterruptedException(operation + " cancelled");
            }
            if (cancelled) throw new InterruptedException("Export cancelled");
            if (exit != 0) throw new IOException(operation + " failed (exit " + exit + "): " + readTail(logFile, 10000));
        } finally {
            process = null;
            if (active != null && active.isAlive()) terminate(active);
            if (!logFile.delete()) logFile.deleteOnExit();
        }
    }

    private static void terminate(Process process) {
        process.destroy();
        try {
            if (!process.waitFor(250L, java.util.concurrent.TimeUnit.MILLISECONDS)) {
                process.destroyForcibly();
                process.waitFor();
            }
        } catch (InterruptedException interrupted) {
            process.destroyForcibly();
            Thread.currentThread().interrupt();
        }
    }

    private static String readTail(File file, int maxBytes) throws IOException {
        RandomAccessFile input = new RandomAccessFile(file, "r");
        try {
            long start = Math.max(0L, input.length() - maxBytes);
            input.seek(start);
            byte[] bytes = new byte[(int) (input.length() - start)];
            input.readFully(bytes);
            return new String(bytes, "UTF-8");
        } finally {
            input.close();
        }
    }
}
