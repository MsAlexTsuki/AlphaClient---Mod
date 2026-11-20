package com.alpharip.staffmod.RecordingStudio.timeline;

/** Non-destructive FFmpeg video effect applied to an imported timeline video clip. */
public enum TimelineVideoEffect {
    NONE(""),
    GRAYSCALE("hue=s=0"),
    SEPIA("colorchannelmixer=.393:.769:.189:0:.349:.686:.168:0:.272:.534:.131"),
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
