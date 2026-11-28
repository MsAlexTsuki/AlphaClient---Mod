package com.alpharip.staffmod.BuildTimelapse.export;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/** External FFmpeg adapter; FFmpeg must be installed separately. */
public final class FfmpegVideoEncoder implements VideoEncoder {
    private final String executable;

    public FfmpegVideoEncoder(String value) {
        executable = value == null || value.trim().isEmpty() ? "ffmpeg" : value.trim();
    }

    @Override
    public void encode(ExportJob job) throws Exception {
        if (job == null) throw new IllegalArgumentException("job");
        File frames = job.getFrames();
        if (!frames.isDirectory()) throw new FileNotFoundException(frames.toString());
        ExportSettings settings = job.getSettings();
        File output = job.getOutput();

        if (settings.getFormat() == ExportSettings.Format.IMAGE_SEQUENCE) {
            copySequence(frames, output);
            return;
        }
        if (settings.getFormat() == ExportSettings.Format.MP4
                && ((settings.getWidth() & 1) != 0 || (settings.getHeight() & 1) != 0)) {
            throw new IllegalArgumentException("MP4 dimensions must be even for yuv420p output");
        }

        File parent = output.getAbsoluteFile().getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("Cannot create export directory: " + parent);
        }

        String input = new File(frames, "frame-%08d.png").getAbsolutePath();
        if (settings.getFormat() == ExportSettings.Format.GIF) {
            encodeOptimizedGif(input, output, parent, settings);
        } else if (settings.getFormat() == ExportSettings.Format.MP4) {
            List<String> command = baseCommand(input, settings);
            Collections.addAll(command, "-vf",
                    "scale=" + settings.getWidth() + ":" + settings.getHeight()
                            + ":force_original_aspect_ratio=decrease,pad="
                            + settings.getWidth() + ":" + settings.getHeight() + ":(ow-iw)/2:(oh-ih)/2",
                    "-an", "-c:v", "libx264", "-pix_fmt", "yuv420p", output.getAbsolutePath());
            run(command, "MP4 export");
        } else {
            throw new UnsupportedOperationException("Unsupported export format");
        }

        if (!output.isFile() || output.length() == 0) {
            throw new IOException("FFmpeg did not create a valid output file");
        }
    }

    private void encodeOptimizedGif(String input, File output, File parent, ExportSettings settings)
            throws Exception {
        File palette = File.createTempFile("alpha-timelapse-palette-", ".png", parent);
        try {
            List<String> paletteCommand = baseCommand(input, settings);
            Collections.addAll(paletteCommand, "-vf",
                    "scale=" + settings.getWidth() + ":" + settings.getHeight()
                            + ":flags=lanczos,palettegen=stats_mode=diff",
                    "-frames:v", "1", "-y", palette.getAbsolutePath());
            run(paletteCommand, "GIF palette generation");

            List<String> gifCommand = new ArrayList<String>();
            Collections.addAll(gifCommand, executable, "-hide_banner", "-loglevel", "error", "-y",
                    "-framerate", Integer.toString(settings.getFps()), "-i", input,
                    "-i", palette.getAbsolutePath(), "-lavfi",
                    "fps=" + settings.getFps() + ",scale=" + settings.getWidth() + ":"
                            + settings.getHeight() + ":flags=lanczos[x];[x][1:v]paletteuse=dither=sierra2_4a",
                    output.getAbsolutePath());
            run(gifCommand, "GIF encoding");
        } finally {
            if (palette.exists() && !palette.delete()) palette.deleteOnExit();
        }
    }

    private List<String> baseCommand(String input, ExportSettings settings) {
        List<String> command = new ArrayList<String>();
        Collections.addAll(command, executable, "-hide_banner", "-loglevel", "error",
                "-framerate", Integer.toString(settings.getFps()), "-i", input);
        return command;
    }

    private void run(List<String> command, String operation) throws Exception {
        File logFile = File.createTempFile("alpha-timelapse-ffmpeg-", ".log");
        Process process = null;
        try {
            process = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(logFile).start();
            final int exit;
            try {
                exit = process.waitFor();
            } catch (InterruptedException interrupted) {
                terminate(process);
                Thread.currentThread().interrupt();
                throw new InterruptedException(operation + " cancelled");
            }
            if (exit != 0) {
                throw new IOException(operation + " failed with exit code " + exit + ": "
                        + readTail(logFile, 12000));
            }
        } finally {
            if (process != null && process.isAlive()) terminate(process);
            if (!logFile.delete()) logFile.deleteOnExit();
        }
    }

    private static void terminate(Process process) {
        process.destroy();
        try {
            if (!process.waitFor(250, java.util.concurrent.TimeUnit.MILLISECONDS)) {
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
            long start = Math.max(0, input.length() - maxBytes);
            input.seek(start);
            byte[] bytes = new byte[(int) (input.length() - start)];
            input.readFully(bytes);
            return new String(bytes, "UTF-8");
        } finally {
            input.close();
        }
    }

    private void copySequence(File source, File destination) throws IOException, InterruptedException {
        if (!destination.exists() && !destination.mkdirs()) {
            throw new IOException("Cannot create image sequence output directory");
        }
        if (!destination.isDirectory()) throw new IOException("Image sequence output is not a directory");
        File[] png = source.listFiles(new FilenameFilter() {
            @Override public boolean accept(File dir, String name) {
                return name.matches("frame-[0-9]{8}\\.png");
            }
        });
        if (png == null || png.length == 0) throw new IOException("No captured PNG frames found");
        Arrays.sort(png, new Comparator<File>() {
            @Override public int compare(File left, File right) {
                return left.getName().compareTo(right.getName());
            }
        });
        for (File frame : png) {
            if (Thread.currentThread().isInterrupted()) throw new InterruptedException("Export cancelled");
            File target = new File(destination, frame.getName());
            if (!frame.getCanonicalFile().equals(target.getCanonicalFile())) {
                Files.copy(frame.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
}
