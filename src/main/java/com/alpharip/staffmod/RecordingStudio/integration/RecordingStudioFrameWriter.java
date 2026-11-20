package com.alpharip.staffmod.RecordingStudio.integration;

import com.alpharip.staffmod.RecordingStudio.capture.RecordingCaptureService;
import com.alpharip.staffmod.RecordingStudio.scene.RecordingStudioScene;
import com.alpharip.staffmod.RecordingStudio.scene.RecordingStudioSource;
import com.alpharip.staffmod.RecordingStudio.timeline.Timeline;
import net.minecraft.client.Minecraft;
import java.io.File;
import java.io.IOException;

/** Routes the active scene's single video source to Minecraft or full-display capture. */
public final class RecordingStudioFrameWriter implements RecordingCaptureService.FrameWriter {
    private final MinecraftFramebufferFrameWriter minecraftWriter;
    private final Timeline timeline;

    public RecordingStudioFrameWriter(Minecraft minecraft, Timeline timeline) {
        if (minecraft == null || timeline == null) throw new IllegalArgumentException("Minecraft and timeline are required");
        this.minecraftWriter = new MinecraftFramebufferFrameWriter(minecraft);
        this.timeline = timeline;
    }

    public RecordingCaptureService.FrameData captureFrame() throws Exception {
        RecordingStudioScene scene = timeline.getActiveScene();
        if (scene == null) throw new IOException("No active Recording Studio scene");
        RecordingStudioSource desktop = scene.findSource(RecordingStudioSource.Type.DESKTOP);
        if (desktop != null && desktop.isEnabled()) return DesktopScreenFrameWriter.capturePrimary();
        RecordingStudioSource display = scene.findSource(RecordingStudioSource.Type.DISPLAY);
        if (display != null && display.isEnabled()) {
            return DesktopScreenFrameWriter.captureDisplay(display.getScreenDeviceId());
        }
        RecordingStudioSource minecraft = scene.findSource(RecordingStudioSource.Type.GAME_CAPTURE);
        if (minecraft != null && minecraft.isEnabled()) return minecraftWriter.captureFrame();
        throw new IOException("Enable a video source in the active scene before recording");
    }

    public void writeFrame(RecordingCaptureService.FrameData frame, File output) throws Exception {
        minecraftWriter.writeFrame(frame, output);
    }
}
