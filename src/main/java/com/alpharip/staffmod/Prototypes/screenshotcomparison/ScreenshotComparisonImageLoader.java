package com.alpharip.staffmod.screenshotcomparison;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

/** Loads local PNG/JPEG screenshots while rejecting unsupported or oversized files. */
public final class ScreenshotComparisonImageLoader {
    private static final long MAX_PIXELS = 40000000L;

    public ScreenshotComparisonImage load(String path) throws IOException {
        if (path == null || path.trim().isEmpty()) {
            throw new IOException("Enter a screenshot file path.");
        }

        File file = new File(path.trim()).getCanonicalFile();
        if (!file.isFile()) throw new IOException("Screenshot file does not exist.");
        if (!file.canRead()) throw new IOException("Screenshot file cannot be read.");

        String name = file.getName().toLowerCase(Locale.ROOT);
        if (!(name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg"))) {
            throw new IOException("Only PNG and JPEG screenshots are supported.");
        }

        BufferedImage image = ImageIO.read(file);
        if (image == null) throw new IOException("The selected file is not a supported image.");
        long pixels = (long) image.getWidth() * (long) image.getHeight();
        if (pixels <= 0L || pixels > MAX_PIXELS) {
            throw new IOException("Image dimensions exceed the 40-megapixel safety limit.");
        }

        return new ScreenshotComparisonImage(file, image);
    }
}
