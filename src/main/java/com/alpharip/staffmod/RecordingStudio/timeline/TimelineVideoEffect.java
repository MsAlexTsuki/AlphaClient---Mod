package com.alpharip.staffmod.RecordingStudio.timeline;

/** Non-destructive FFmpeg video effect applied to an imported timeline video clip. */
public enum TimelineVideoEffect {
    NONE(""),
    GRAYSCALE("hue=s=0"),
    SEPIA("colorchannelmixer=rr=.393:rg=.769:rb=.189:gr=.349:gg=.686:gb=.168:br=.272:bg=.534:bb=.131"),
    BRIGHTEN("eq=brightness=0.08"),
    DARKEN("eq=brightness=-0.08"),
    CONTRAST("eq=contrast=1.25");

    private final String ffmpegFilter;

    TimelineVideoEffect(String ffmpegFilter) {
        this.ffmpegFilter = ffmpegFilter;
    }

    public String getFfmpegFilter() {
        return ffmpegFilter;
    }
}
