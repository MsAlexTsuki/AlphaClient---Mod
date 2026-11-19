package com.alpharip.staffmod.RecordingStudio.ui;

import com.alpharip.staffmod.RecordingStudio.core.RecordingStudioController;
import com.alpharip.staffmod.RecordingStudio.integration.RecordingStudioRuntime;
import com.alpharip.staffmod.RecordingStudio.integration.RecordingStudioPlayback;
import com.alpharip.staffmod.RecordingStudio.scene.RecordingStudioScene;
import com.alpharip.staffmod.RecordingStudio.scene.RecordingStudioSource;
import com.alpharip.staffmod.RecordingStudio.settings.RecordingStudioPreferences;
import com.alpharip.staffmod.RecordingStudio.timeline.TimelineClip;
import com.alpharip.staffmod.RecordingStudio.timeline.TimelineTrack;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

import java.io.IOException;

/** OBS-inspired Recording Studio workspace with an integrated module panel. */
public final class RecordingStudioScreen extends GuiScreen {
    private final RecordingStudioController controller;
    private final RecordingStudioRuntime runtime;
    private final RecordingStudioPlayback playback;
    private boolean playbackMode;
    private boolean navigating;
    private GuiTextField sceneNameField;
    private String selectedSourceId;

    public RecordingStudioScreen(RecordingStudioController controller, RecordingStudioRuntime runtime) {
        if (controller == null || runtime == null) throw new IllegalArgumentException();
        this.controller = controller;
        this.runtime = runtime;
        this.playback = new RecordingStudioPlayback(controller.getFramesDirectory());
        RecordingStudioPreferences preferences = RecordingStudioPreferences.load(
                net.minecraft.client.Minecraft.getMinecraft().mcDataDir);
        controller.getExportSettings().setResolution(preferences.getResolution());
        controller.getExportSettings().setQuality(preferences.getQuality());
    }

    @Override public boolean doesGuiPauseGame() { return false; }

    @Override
    public void initGui() {
        navigating = false;
        buttonList.clear();

        RecordingStudioScene activeScene = controller.getActiveScene();
        if (activeScene == null || activeScene.findSource(selectedSourceId) == null) {
            selectedSourceId = activeScene == null || activeScene.getSources().isEmpty()
                    ? null : activeScene.getSources().get(0).getId();
        }
        sceneNameField = new GuiTextField(30, fontRendererObj, 18, 140, 126, 18);
        sceneNameField.setMaxStringLength(32);
        sceneNameField.setText(activeScene == null ? "" : activeScene.getName());

        int rightX = width - 184;
        int buttonWidth = 78;
        int rightY = Math.max(200, height - 214);
        int gap = 4;
        int row = 23;

        add(0, rightX + 8, rightY, buttonWidth, "REC", true);
        add(1, rightX + 8 + buttonWidth + gap, rightY, buttonWidth, "Pause");
        add(2, rightX + 8, rightY + row, buttonWidth, "Resume");
        add(3, rightX + 8 + buttonWidth + gap, rightY + row, buttonWidth, "Stop");
        add(4, rightX + 8, rightY + row * 2, buttonWidth, "Timeline");
        add(5, rightX + 8 + buttonWidth + gap, rightY + row * 2, buttonWidth, "Save");
        add(6, rightX + 8, rightY + row * 3, buttonWidth, "Camera");
        add(7, rightX + 8 + buttonWidth + gap, rightY + row * 3, buttonWidth, "Interval -");
        add(8, rightX + 8, rightY + row * 4, buttonWidth, "Interval +");
        add(9, rightX + 8 + buttonWidth + gap, rightY + row * 4, buttonWidth, "Mic On");
        add(10, rightX + 8, rightY + row * 5, buttonWidth, "Mic Off");
        add(11, rightX + 8 + buttonWidth + gap, rightY + row * 5, buttonWidth, "MP4", true);
        add(12, rightX + 8, rightY + row * 6, buttonWidth, "GIF");
        add(13, rightX + 8 + buttonWidth + gap, rightY + row * 6, buttonWidth, "Cancel");
        add(14, rightX + 8, rightY + row * 7, buttonWidth, "Storage");
        add(18, rightX + 8 + buttonWidth + gap, rightY + row * 7, buttonWidth, "Next Mic");
        add(27, 18, 450, 126, "Refresh Mic");
        add(28, rightX + 8, 198, buttonWidth, "Source Up");
        add(29, rightX + 8 + buttonWidth + gap, 198, buttonWidth, "Source Down");
        buttonList.add(new AlphaStudioButton(31, width - 94, 10, 78, 17, "Settings"));

        add(19, 18, 84, 126, "Next Scene");
        add(20, 18, 106, 126, "New Scene", true);
        add(23, 18, 162, 126, "Rename Scene");
        add(24, 18, 184, 126, "Remove Scene");
        add(15, 18, 282, 126, "Change Module", true);
        add(16, 18, 306, 126, "Next Camera Path");
        add(17, 18, 330, 126, runtime.isCameraPreviewVisible() ? "HUD Preview: ON" : "HUD Preview: OFF");
        add(21, 18, 354, 126, "Toggle Game Capture");
        add(22, 18, 378, 126, "Toggle Microphone");
        add(25, 18, 402, 126, "Remove Game Source");
        add(26, 18, 426, 126, "Remove Mic Source");

        int playbackX = width - 190 - 184;
        int playbackY = height - 124;
        add(32, playbackX, playbackY, 43, "Review");
        add(33, playbackX + 46, playbackY, 43, "Prev");
        add(34, playbackX + 92, playbackY, 43, "Play");
        add(35, playbackX + 138, playbackY, 43, "Next");

        GuiButton cameraButton = findButton(6);
        if (cameraButton != null) cameraButton.enabled = "normal_gameplay".equals(runtime.getSelectedModuleId());
        GuiButton pathButton = findButton(16);
        if (pathButton != null) pathButton.enabled = !"normal_gameplay".equals(runtime.getSelectedModuleId())
                && !runtime.getSelectedModuleOptions().isEmpty();
    }

    private GuiButton findButton(int id) {
        for (Object value : buttonList) {
            GuiButton button = (GuiButton) value;
            if (button.id == id) return button;
        }
        return null;
    }

    private static String compact(String value, int maximum) {
        if (value == null) return "";
        return value.length() <= maximum ? value : value.substring(0, Math.max(0, maximum - 3)) + "...";
    }

    private void add(int id, int x, int y, int w, String label) {
        add(id, x, y, w, label, false);
    }

    private void add(int id, int x, int y, int w, String label, boolean accent) {
        buttonList.add(new AlphaStudioButton(id, x, y, w, 20, label, accent));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 0:
                playback.pause();
                playbackMode = false;
                runtime.startRecording();
                break;
            case 1: runtime.pauseRecording(); break;
            case 2: runtime.resumeRecording(); break;
            case 3: runtime.stopRecording(); break;
            case 4:
                playback.pause();
                navigating = true;
                mc.displayGuiScreen(new TimelineEditorScreen(this, controller.getTimeline(), runtime));
                break;
            case 5: controller.save(); break;
            case 6:
                playback.pause();
                navigating = true;
                mc.displayGuiScreen(new CameraEditorScreen(this, runtime.getCameraController(), mc.mcDataDir, runtime));
                break;
            case 7: controller.adjustCaptureInterval(-100L); break;
            case 8: controller.adjustCaptureInterval(100L); break;
            case 9: controller.startAudioCapture(); break;
            case 10: controller.stopAudioCapture(); break;
            case 11:
                controller.startExport(com.alpharip.staffmod.RecordingStudio.export.RecordingStudioExportService.Format.MP4);
                break;
            case 12:
                controller.startExport(com.alpharip.staffmod.RecordingStudio.export.RecordingStudioExportService.Format.GIF);
                break;
            case 13: controller.cancelExport(); break;
            case 31:
                playback.pause();
                navigating = true;
                mc.displayGuiScreen(new RecordingStudioSettingsScreen(this, controller));
                break;
            case 14:
                playback.pause();
                navigating = true;
                controller.getStorageUsage().refreshAsync();
                mc.displayGuiScreen(new RecordingStudioStorageScreen(this, controller.getStorageUsage(), runtime));
                break;
            case 32:
                playback.pause();
                if (playbackMode) {
                    playbackMode = false;
                } else {
                    playback.refreshFrames();
                    playbackMode = true;
                }
                break;
            case 33:
                playbackMode = true;
                playback.step(-1);
                break;
            case 34:
                playbackMode = true;
                if (controller.getCapture().getPendingFrameCount() == 0) playback.refreshFrames();
                playback.togglePlayback();
                break;
            case 35:
                playbackMode = true;
                playback.step(1);
                break;
            case 15:
                runtime.selectNextModule();
                initGui();
                break;
            case 16:
                runtime.selectNextModuleOption();
                initGui();
                break;
            case 17:
                runtime.toggleCameraPreview();
                initGui();
                break;
            case 18:
                controller.selectNextMicrophone();
                break;
            case 27:
                controller.refreshMicrophones();
                break;
            case 28:
                if (selectedSourceId != null) controller.moveActiveSceneSource(selectedSourceId, -1);
                initGui();
                break;
            case 29:
                if (selectedSourceId != null) controller.moveActiveSceneSource(selectedSourceId, 1);
                initGui();
                break;
            case 19:
                controller.selectNextScene();
                initGui();
                break;
            case 20:
                controller.createScene();
                initGui();
                break;
            case 21:
                controller.toggleSceneSource(RecordingStudioSource.Type.GAME_CAPTURE);
                initGui();
                break;
            case 22:
                controller.toggleSceneSource(RecordingStudioSource.Type.MICROPHONE);
                initGui();
                break;
            case 23:
                controller.renameActiveScene(sceneNameField.getText());
                initGui();
                break;
            case 24:
                controller.removeActiveScene();
                initGui();
                break;
            case 25:
                controller.removeSceneSource(RecordingStudioSource.Type.GAME_CAPTURE);
                initGui();
                break;
            case 26:
                controller.removeSceneSource(RecordingStudioSource.Type.MICROPHONE);
                initGui();
                break;
            default: break;
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) { mc.displayGuiScreen(null); return; }
        if (sceneNameField != null) sceneNameField.textboxKeyTyped(typedChar, keyCode);
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (sceneNameField != null) sceneNameField.mouseClicked(mouseX, mouseY, mouseButton);
        int sourcePanelLeft = width - 176;
        int sourcePanelRight = width - 18;
        if (mouseX >= sourcePanelLeft && mouseX <= sourcePanelRight && mouseY >= 74) {
            RecordingStudioScene scene = controller.getActiveScene();
            if (scene != null) {
                int index = (mouseY - 74) / 16;
                java.util.List<RecordingStudioSource> sources = scene.getSources();
                if (index >= 0 && index < sources.size()) selectedSourceId = sources.get(index).getId();
            }
        }

        boolean mediaReady = !controller.getCapture().isRecording()
                && !controller.getCapture().isPaused()
                && controller.getCapture().getPendingFrameCount() == 0;
        int timelineBarLeft = 218;
        int timelineBarRight = width - 200;
        int videoTrackTop = height - 99;
        int videoTrackBottom = videoTrackTop + 11;
        if (mouseButton == 0 && mediaReady && mouseY >= videoTrackTop && mouseY <= videoTrackBottom
                && mouseX >= timelineBarLeft && mouseX < timelineBarRight) {
            if (!playbackMode) {
                playback.refreshFrames();
                playbackMode = true;
            }
            double position = (mouseX - timelineBarLeft) / (double) Math.max(1, timelineBarRight - timelineBarLeft);
            int targetFrame = (int) Math.round(position * Math.max(0, playback.getFrameCount() - 1));
            playback.seekToFrame(targetFrame);
        }
    }

    @Override
    public void onGuiClosed() {
        if (!navigating) {
            playback.close(mc);
            runtime.shutdown();
        }
    }

    private static String formatRecordingTime(long millis) {
        long totalSeconds = Math.max(0L, millis) / 1000L;
        long hours = totalSeconds / 3600L;
        long minutes = (totalSeconds / 60L) % 60L;
        long seconds = totalSeconds % 60L;
        return String.format(java.util.Locale.ROOT, "%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void drawTimelineTrack(String trackId, String label, int y, int color,
                                   int barLeft, int barRight, long totalDuration) {
        drawString(fontRendererObj, label, 170, y + 1, 0xFFBBBBBB);
        Gui.drawRect(barLeft, y, barRight, y + 11, 0xFF242424);
        int barWidth = Math.max(1, barRight - barLeft);
        for (TimelineTrack track : controller.getTimeline().getTracks()) {
            if (!trackId.equals(track.getId())) continue;
            for (TimelineClip clip : track.getClips()) {
                if (clip.getDuration() <= 0L) continue;
                double startRatio = Math.max(0.0, Math.min(1.0,
                        clip.getStart() / (double) totalDuration));
                double endRatio = Math.max(startRatio, Math.min(1.0,
                        clip.getEnd() / (double) totalDuration));
                int clipLeft = barLeft + (int) Math.floor(startRatio * barWidth);
                int clipRight = barLeft + (int) Math.ceil(endRatio * barWidth);
                clipLeft = Math.max(barLeft, Math.min(barRight - 1, clipLeft));
                clipRight = Math.max(clipLeft + 1, Math.min(barRight, clipRight));
                Gui.drawRect(clipLeft, y + 1, clipRight, y + 10, color);
                if (clipRight - clipLeft > 42) {
                    String clipLabel = "@capture".equals(clip.getSource()) ? "CAPTURE" : label;
                    drawString(fontRendererObj, clipLabel, clipLeft + 3, y + 2, 0xFF17130A);
                }
            }
        }
        if ("video".equals(trackId)
                && (controller.getCapture().isRecording() || controller.getCapture().isPaused())) {
            long activeDuration = controller.getProject().getElapsedMillis(System.currentTimeMillis());
            int activeRight = barLeft + (int) Math.ceil(
                    Math.max(0L, Math.min(totalDuration, activeDuration)) / (double) totalDuration * barWidth);
            if (activeRight > barLeft) Gui.drawRect(barLeft, y + 1, activeRight, y + 10, color);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        Gui.drawRect(0, 0, width, height, 0x66000000);

        int left = 10;
        int leftRight = 152;
        int right = width - 184;
        int rightRight = width - 10;
        int panelTop = 48;
        int panelBottom = height - 14;
        int previewLeft = 160;
        int previewRight = width - 190;
        int previewTop = 48;
        int previewBottom = height - 140;

        Gui.drawRect(left, panelTop, leftRight, panelBottom, 0xE8101010);
        Gui.drawRect(left, panelTop, leftRight, panelTop + 2, 0xFFFFC83D);
        Gui.drawRect(right, panelTop, rightRight, panelBottom, 0xE8101010);
        Gui.drawRect(right, panelTop, rightRight, panelTop + 2, 0xFFFFC83D);

        Gui.drawRect(previewLeft, previewTop, previewRight, previewBottom, 0x33000000);
        Gui.drawRect(previewLeft, previewTop, previewRight, previewTop + 2, 0xFFFFC83D);
        Gui.drawRect(previewLeft, previewBottom - 1, previewRight, previewBottom, 0xFF777777);
        Gui.drawRect(previewLeft, previewTop, previewLeft + 1, previewBottom, 0xFF777777);
        Gui.drawRect(previewRight - 1, previewTop, previewRight, previewBottom, 0xFF777777);

        drawCenteredString(fontRendererObj, "ALPHA CLIENT  /  RECORDING STUDIO", width / 2, 16, 0xFFAAAAAA);
        drawCenteredString(fontRendererObj, controller.getProject().getName(), width / 2, 31, 0xFFFFC83D);

        RecordingStudioScene activeScene = controller.getActiveScene();
        drawString(fontRendererObj, "SCENES  (" + controller.getTimeline().getScenes().size() + ")", 20, 59, 0xFFFFC83D);
        drawString(fontRendererObj, "> " + compact(activeScene == null ? "None" : activeScene.getName(), 20),
                20, 78, 0xFFFFFFFF);
        drawString(fontRendererObj, "Scene name", 20, 130, 0xFFBBBBBB);
        if (sceneNameField != null) sceneNameField.drawTextBox();
        drawString(fontRendererObj, "MODULE", 20, 210, 0xFFFFC83D);
        drawString(fontRendererObj, compact(runtime.getModuleDisplayName(runtime.getSelectedModuleId()), 19),
                20, 229, 0xFFFFFFFF);
        drawString(fontRendererObj, "Option: " + compact(runtime.getSelectedOptionId().isEmpty()
                ? "Default" : runtime.getSelectedOptionId(), 14), 20, 247, 0xFFBBBBBB);
        drawString(fontRendererObj, compact(runtime.getSelectedModuleStatus(), 22), 20, 265, 0xFFAAAAAA);

        drawCenteredString(fontRendererObj, playbackMode ? "RECORDED PLAYBACK" : "LIVE PREVIEW",
                (previewLeft + previewRight) / 2, previewTop + 10, 0xFFFFC83D);
        if (playbackMode) {
            drawString(fontRendererObj, "PLAYBACK", previewRight - 62, previewTop + 10, 0xFFFFD65A);
        } else {
            boolean recording = controller.getCapture().isRecording();
            boolean paused = controller.getCapture().isPaused();
            long elapsed = controller.getProject().getElapsedMillis(System.currentTimeMillis());
            String state = recording ? "REC" : paused ? "PAUSED" : elapsed > 0L ? "STOPPED" : "READY";
            int stateColor = recording ? 0xFFFF5555 : paused ? 0xFFFFD65A : 0xFFAAAAAA;
            drawString(fontRendererObj, state + "  " + formatRecordingTime(elapsed),
                    previewRight - 130, previewTop + 10, stateColor);
        }
        drawString(fontRendererObj, playbackMode ? "RECORDED GAME VIEW" : "LIVE GAME VIEW",
                previewLeft + 12, previewTop + 28, 0xFFE0E0E0);
        drawString(fontRendererObj, "SESSION  " + controller.getStatus(),
                previewLeft + 12, previewBottom - 42, 0xFFFFFFFF);
        drawString(fontRendererObj, "Frames " + controller.getCapture().getFrameCount()
                + " saved / " + controller.getCapture().getPendingFrameCount()
                + " pending / " + controller.getCapture().getFailedFrameCount() + " failed",
                previewLeft + 12, previewBottom - 28, 0xFFBBBBBB);
        drawString(fontRendererObj, "Timeline " + controller.getTimeline().duration()
                + " ms  |  Interval " + controller.getCapture().getIntervalMillis() + " ms",
                previewLeft + 12, previewBottom - 14, 0xFFBBBBBB);

        drawString(fontRendererObj, "SOURCES", right + 8, 59, 0xFFFFC83D);
        int sourceY = 78;
        if (activeScene != null) {
            for (RecordingStudioSource source : activeScene.getSources()) {
                boolean selected = source.getId().equals(selectedSourceId);
                if (selected) Gui.drawRect(right + 5, sourceY - 2, rightRight - 5, sourceY + 12, 0x884B3A12);
                drawString(fontRendererObj, (selected ? "> " : "  ") + compact(source.getName(), 15) + ": "
                        + (source.isEnabled() ? "ON" : "OFF"), right + 8, sourceY,
                        selected ? 0xFFFFD65A : source.isEnabled() ? 0xFFE0E0E0 : 0xFF888888);
                sourceY += 16;
            }
        }
        drawString(fontRendererObj, "View: " + (runtime.hasExternalCamera() ? "External camera" : "Player"),
                right + 8, 112, 0xFFBBBBBB);
        drawString(fontRendererObj, "Input: " + compact(controller.getMicrophoneSelectionName(), 18),
                right + 8, 128, 0xFFBBBBBB);
        drawString(fontRendererObj, "AUDIO MIXER", right + 8, 145, 0xFFFFC83D);
        drawString(fontRendererObj, controller.getAudioStatus(), right + 8, 164, 0xFFE0E0E0);
        drawString(fontRendererObj, "Game audio: not captured", right + 8, 181, 0xFF888888);
        drawString(fontRendererObj, "CONTROLS", right + 8, 242, 0xFFFFC83D);

        int timelineTop = height - 130;
        int timelineBottom = height - 43;
        Gui.drawRect(previewLeft, timelineTop, previewRight, timelineBottom, 0xE8101010);
        Gui.drawRect(previewLeft, timelineTop, previewRight, timelineTop + 2, 0xFFFFC83D);
        drawString(fontRendererObj, "TIMELINE", previewLeft + 10, height - 120, 0xFFFFC83D);

        long playheadMillis = controller.getProject().getElapsedMillis(System.currentTimeMillis());
        long timelineDuration = Math.max(1L,
                Math.max(controller.getTimeline().duration(), playheadMillis));
        int timelineBarLeft = previewLeft + 58;
        int timelineBarRight = previewRight - 10;
        drawTimelineTrack("video", "VIDEO", height - 99, 0xFFC88F20,
                timelineBarLeft, timelineBarRight, timelineDuration);
        drawTimelineTrack("audio", "AUDIO", height - 81, 0xFF4D9B9B,
                timelineBarLeft, timelineBarRight, timelineDuration);

        if (playheadMillis >= 0L && playheadMillis <= timelineDuration) {
            int playheadX = timelineBarLeft + (int) Math.round(
                    (playheadMillis / (double) timelineDuration) * (timelineBarRight - timelineBarLeft));
            Gui.drawRect(playheadX, height - 100, playheadX + 1, height - 69, 0xFFEFEFEF);
        }
        drawString(fontRendererObj, "Duration: " + controller.getTimeline().duration() + " ms",
                previewLeft + 10, height - 54, 0xFFBBBBBB);
        drawString(fontRendererObj, controller.getExportStatus(), previewLeft + 10, height - 34, 0xFFFFC83D);

        boolean recordingActive = controller.getCapture().isRecording()
                || controller.getCapture().isPaused();
        boolean mediaReady = !recordingActive && controller.getCapture().getPendingFrameCount() == 0;
        GuiButton reviewButton = findButton(32);
        if (reviewButton != null) {
            reviewButton.displayString = playbackMode ? "Live" : "Review";
            reviewButton.enabled = mediaReady;
        }
        GuiButton previousButton = findButton(33);
        if (previousButton != null) previousButton.enabled = mediaReady && playback.getFrameCount() > 0;
        GuiButton playButton = findButton(34);
        if (playButton != null) {
            playButton.displayString = playback.isPlaying() ? "Pause" : "Play";
            playButton.enabled = mediaReady && playback.getFrameCount() > 0;
        }
        GuiButton nextButton = findButton(35);
        if (nextButton != null) nextButton.enabled = mediaReady && playback.getFrameCount() > 0;

        super.drawScreen(mouseX, mouseY, partialTicks);
        if (playbackMode) {
            playback.draw(mc, previewLeft + 6, previewTop + 48,
                    previewRight - previewLeft - 12, previewBottom - previewTop - 96,
                    controller.getCapture().getIntervalMillis(),
                    controller.getCapture().getPendingFrameCount());
        } else {
            runtime.drawLiveViewport(previewLeft + 6, previewTop + 48,
                    previewRight - previewLeft - 12, previewBottom - previewTop - 96);
            runtime.drawCameraPreview();
        }
    }
}
