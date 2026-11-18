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

    public synchronized void start(final Format format, final int fps, final String executable) throws IOException {
        if (format == null) throw new IllegalArgumentException("Export format is required");
        if (fps < 1 || fps > 240) throw new IllegalArgumentException("FPS must be between 1 and 240");
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

    private void export(Format format, int fps, String executable, File output) {
        String extension = format == Format.MP4 ? ".mp4" : ".gif";
        String base = output.getName().substring(0, output.getName().length() - extension.length());
        File partial = new File(outputDirectory, base + ".partial" + extension);
        File palette = null;
        try {
            File[] frames = listFrames();
            BufferedImage first = ImageIO.read(frames[0]);
            if (first == null) throw new IOException("Cannot read the first captured PNG frame");
            String input = new File(framesDirectory, "frame-*.png").getAbsolutePath().replace('\\\\', '/');

            if (format == Format.MP4) {
                List<String> command = baseCommand(executable, input, fps);
                List<TimelineClip> audioClips = getAudioClips();
                List<String> audioLabels = new ArrayList<String>();
                for (TimelineClip clip : audioClips) {
                    File audio = new File(clip.getSource());
                    if (!audio.isFile()) throw new FileNotFoundException("Audio timeline source not found: " + audio);
                    command.add("-i");
                    command.add(audio.getAbsolutePath());
                    audioLabels.add("[" + audioLabels.size() + ":a]");
                }

                String filter = buildAudioFilter(audioClips);
                if (!filter.isEmpty()) {
                    command.add("-filter_complex");
                    command.add(filter);
                }
                Collections.addAll(command, "-map", "0:v:0", "-vf",
                        "pad=ceil(iw/2)*2:ceil(ih/2)*2", "-c:v", "libx264",
                        "-pix_fmt", "yuv420p", "-movflags", "+faststart");
                if (audioClips.isEmpty()) {
                    command.add("-an");
                } else {
                    command.add("-map");
                    command.add(audioClips.size() == 1 ? "[a0]" : "[aout]");
                    Collections.addAll(command, "-c:a", "aac", "-b:a", "192k", "-shortest");
                }
                command.add("-y");
                command.add(partial.getAbsolutePath());
                run(command, "MP4 export");
            } else {
                palette = File.createTempFile("alpha-recording-palette-", ".png", outputDirectory);
                List<String> paletteCommand = baseCommand(executable, input, fps);
                Collections.addAll(paletteCommand, "-vf",
                        "palettegen=stats_mode=diff", "-frames:v", "1", "-y", palette.getAbsolutePath());
                run(paletteCommand, "GIF palette generation");

                List<String> gifCommand = new ArrayList<String>();
                Collections.addAll(gifCommand, executable, "-hide_banner", "-loglevel", "error",
                        "-framerate", Integer.toString(fps), "-pattern_type", "glob", "-i", input,
                        "-i", palette.getAbsolutePath(), "-lavfi",
                        "fps=" + fps + ",[0:v]split[a][b];[a]palettegen=stats_mode=diff[p];[b][p]paletteuse=dither=sierra2_4a",
                        "-y", partial.getAbsolutePath());
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

    private List<String> baseCommand(String executable, String input, int fps) {
        List<String> command = new ArrayList<String>();
        Collections.addAll(command, executable, "-hide_banner", "-loglevel", "error",
                "-framerate", Integer.toString(fps), "-pattern_type", "glob", "-i", input);
        return command;
    }

    private List<TimelineClip> getAudioClips() {
        for (TimelineTrack track : timeline.getTracks()) {
            if ("audio".equals(track.getId())) return track.getClips();
        }
        return Collections.emptyList();
    }

    private String buildAudioFilter(List<TimelineClip> clips) {
        if (clips.isEmpty()) return "";
        StringBuilder filter = new StringBuilder();
        for (int i = 0; i < clips.size(); i++) {
            TimelineClip clip = clips.get(i);
            long delay = Math.max(0L, clip.getStart());
            long duration = Math.max(0L, clip.getDuration());
            if (i > 0) filter.append(';');
            filter.append('[').append(i + 1).append(":a]")
                    .append("atrim=duration=").append(String.format(Locale.ROOT, "%.3f", duration / 1000.0))
                    .append(",asetpts=PTS-STARTPTS,adelay=").append(delay).append(":all=1[a").append(i).append(']');
        }
        if (clips.size() > 1) {
            filter.append(';');
            for (int i = 0; i < clips.size(); i++) filter.append("[a").append(i).append(']');
            filter.append("amix=inputs=").append(clips.size())
                    .append(":duration=longest:dropout_transition=0[aout]");
        }
        return filter.toString();
    }

    private File[] listFrames() throws IOException {
        if (!framesDirectory.isDirectory()) throw new FileNotFoundException("Frame directory not found: " + framesDirectory);
        File[] frames = framesDirectory.listFiles((dir, name) -> name.matches("frame-[0-9]{8}\\.png"));
        if (frames == null) throw new IOException("Cannot read captured frame directory");
        Arrays.sort(frames, new Comparator<File>() {
            public int compare(File left, File right) { return left.getName().compareTo(right.getName()); }
        });
        return frames;
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
