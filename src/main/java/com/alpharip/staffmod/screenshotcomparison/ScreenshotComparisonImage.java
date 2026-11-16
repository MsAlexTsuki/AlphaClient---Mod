package com.alpharip.staffmod.screenshotcomparison;

import java.awt.image.BufferedImage;
import java.io.File;

/** A decoded local screenshot and the file metadata used by the comparison view. */
public final class ScreenshotComparisonImage {
    private final File file;
    private final BufferedImage image;

    ScreenshotComparisonImage(File file, BufferedImage image) {
        if (file == null || image == null) throw new IllegalArgumentException("File and image are required");
        this.file = file;
        this.image = image;
    }

    public File getFile() {
        return file;
    }

    public String getPath() {
        return file.getAbsolutePath();
    }

    public BufferedImage getImage() {
        return image;
    }

    public int getWidth() {
        return image.getWidth();
    }

    public int getHeight() {
        return image.getHeight();
    }

    public long getFileSizeBytes() {
        return file.length();
    }
}
