package com.alpharip.staffmod.RecordingStudio.export;

/** YouTube-oriented MP4 export settings. Resolution limits never upscale the source. */
public final class RecordingStudioExportSettings {
    public enum Resolution {
        ORIGINAL("Original", 0, 0),
        P1080("1080p", 1920, 1080),
        P720("720p", 1280, 720);

        private final String label;
        private final int maxWidth;
        private final int maxHeight;

        Resolution(String label, int maxWidth, int maxHeight) {
            this.label = label;
            this.maxWidth = maxWidth;
            this.maxHeight = maxHeight;
        }

        public String getLabel() { return label; }

        public int[] fit(int sourceWidth, int sourceHeight) {
            if (sourceWidth <= 0 || sourceHeight <= 0) {
                throw new IllegalArgumentException("Source dimensions must be positive");
            }
            double scale = 1.0;
            if (maxWidth > 0 && maxHeight > 0) {
                scale = Math.min(1.0, Math.min(maxWidth / (double) sourceWidth,
                        maxHeight / (double) sourceHeight));
            }
            int width = Math.max(2, ((int) Math.floor(sourceWidth * scale)) & ~1);
            int height = Math.max(2, ((int) Math.floor(sourceHeight * scale)) & ~1);
            return new int[] { width, height };
        }

        public Resolution next() {
            Resolution[] values = values();
            return values[(ordinal() + 1) % values.length];
        }
    }

    public enum Quality {
        HIGH("High", 18),
        BALANCED("Balanced", 21),
        SMALL_FILE("Smaller file", 25);

        private final String label;
        private final int crf;

        Quality(String label, int crf) {
            this.label = label;
            this.crf = crf;
        }

        public String getLabel() { return label; }
        public int getCrf() { return crf; }

        public Quality next() {
            Quality[] values = values();
            return values[(ordinal() + 1) % values.length];
        }
    }

    private Resolution resolution = Resolution.P1080;
    private Quality quality = Quality.HIGH;

    public Resolution getResolution() { return resolution; }
    public Quality getQuality() { return quality; }
    public void setResolution(Resolution value) { if (value != null) resolution = value; }
    public void setQuality(Quality value) { if (value != null) quality = value; }
    public void cycleResolution() { resolution = resolution.next(); }
    public void cycleQuality() { quality = quality.next(); }
}
