package com.alpharip.staffmod.RecordingStudio.ui;

import com.alpharip.staffmod.RecordingStudio.timeline.*;
import com.alpharip.staffmod.RecordingStudio.integration.RecordingStudioRuntime;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import java.util.List;
import java.util.UUID;
import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/** Timeline editor for track and clip operations; the world continues ticking. */
public final class TimelineEditorScreen extends GuiScreen {
    private final GuiScreen parent;
    private final Timeline timeline;
    private final RecordingStudioRuntime runtime;
    private GuiTextField sourceField, startField, durationField, speedField;
    private int trackIndex, clipIndex;
    private String selectedClipId;
    private TimelineMarker selectedMarker;
    private int markerKindIndex;
    private String status = "Select a track or create a clip";
    private volatile boolean mediaProbePending;
    private volatile boolean mediaProbeComplete;
    private volatile long mediaProbeDuration;
    private volatile String mediaProbeFailure;
    private volatile File mediaProbeFile;
    private volatile String mediaProbeTrackId;
    private volatile long mediaProbeStart;
    private volatile boolean screenClosed;
    private static final int DRAG_NONE = 0;
    private static final int DRAG_MOVE = 1;
    private static final int DRAG_TRIM_LEFT = 2;
    private static final int DRAG_TRIM_RIGHT = 3;
    private static final int DRAG_MARKER = 4;
    private int dragMode = DRAG_NONE;
    private TimelineTrack dragTrack;
    private TimelineClip dragClip;
    private long dragOriginalStart;
    private long dragOriginalDuration;
    private long dragScale;
    private long dragMarkerOriginalTime;
    private int dragOriginX;

    public TimelineEditorScreen(GuiScreen parent, Timeline timeline) {
        this(parent, timeline, null);
    }

    public TimelineEditorScreen(GuiScreen parent, Timeline timeline, RecordingStudioRuntime runtime) {
        if (parent == null || timeline == null) throw new IllegalArgumentException();
        this.parent = parent;
        this.timeline = timeline;
        this.runtime = runtime;
    }

    @Override public boolean doesGuiPauseGame() { return false; }

    @Override public void initGui() {
        buttonList.clear();
        int left = width / 2 - 145;
        sourceField = new GuiTextField(0, fontRendererObj, left, height / 2 - 88, 290, 18);
        sourceField.setMaxStringLength(512);
        startField = field(1, left, height / 2 - 48, 82);
        durationField = field(2, left + 104, height / 2 - 48, 82);
        speedField = field(3, left + 208, height / 2 - 48, 82);
        int y = height / 2 - 18;
        add(0, left, y, "Prev Track");
        add(1, left + 98, y, "Next Track");
        add(2, left + 196, y, "Prev Clip");
        y += 22;
        add(3, left, y, "Next Clip");
        add(4, left + 98, y, "Add Clip");
        add(5, left + 196, y, "Move Clip");
        y += 22;
        add(6, left, y, "Trim Clip");
        add(7, left + 98, y, "Set Speed");
        add(8, left + 196, y, "Remove Clip");
        y += 24;
        buttonList.add(new AlphaStudioButton(10, left, y, 290, 20, "Import Media", true));
        y += 22;
        buttonList.add(new AlphaStudioButton(11, left, y, 70, 20, "Add Mark"));
        buttonList.add(new AlphaStudioButton(12, left + 73, y, 70, 20, "Remove"));
        buttonList.add(new AlphaStudioButton(13, left + 146, y, 70, 20, "Kind: MANUAL"));
        buttonList.add(new AlphaStudioButton(14, left + 219, y, 70, 20, "Effect: NONE"));
        y += 24;
        buttonList.add(new AlphaStudioButton(15, left, y, 143, 20, "Fade In: 0 ms"));
        buttonList.add(new AlphaStudioButton(16, left + 147, y, 143, 20, "Fade Out: 0 ms"));
        y += 22;
        buttonList.add(new AlphaStudioButton(9, width / 2 - 50, y, 100, 20, "Back", true));
        syncSelection();
    }

    private GuiTextField field(int id, int x, int y, int w) {
        GuiTextField f = new GuiTextField(id, fontRendererObj, x, y, w, 18);
        f.setMaxStringLength(16);
        return f;
    }
    private void add(int id, int x, int y, String label) {
        buttonList.add(new AlphaStudioButton(id, x, y, 94, 20, label));
    }

    private List<TimelineTrack> tracks() { return timeline.getTracks(); }
    private TimelineTrack selectedTrack() {
        List<TimelineTrack> all = tracks();
        if (all.isEmpty()) return null;
        trackIndex = Math.max(0, Math.min(trackIndex, all.size() - 1));
        return all.get(trackIndex);
    }
    private TimelineClip selectedClip() {
        TimelineTrack track = selectedTrack();
        if (track == null) return null;
        List<TimelineClip> clips = track.getClips();
        if (clips.isEmpty()) return null;
        for (int i = 0; i < clips.size(); i++) {
            if (clips.get(i).getId().equals(selectedClipId)) {
                clipIndex = i;
                return clips.get(i);
            }
        }
        clipIndex = Math.max(0, Math.min(clipIndex, clips.size() - 1));
        TimelineClip fallback = clips.get(clipIndex);
        selectedClipId = fallback.getId();
        return fallback;
    }
    private void syncSelection() {
        TimelineTrack track = selectedTrack();
        TimelineClip clip = selectedClip();
        if (track == null) {
            selectedClipId = null;
            status = "No tracks available; add a track in the studio";
            return;
        }
        if (clip == null) {
            selectedClipId = null;
            if (startField.getText().trim().isEmpty()) startField.setText("0");
            if (durationField.getText().trim().isEmpty()) durationField.setText("1000");
            if (speedField.getText().trim().isEmpty()) speedField.setText("1.0");
            return;
        }
        selectedClipId = clip.getId();
        sourceField.setText(clip.getSource());
        startField.setText(Long.toString(clip.getStart()));
        durationField.setText(Long.toString(clip.getDuration()));
        speedField.setText(Double.toString(clip.getSpeed()));
    }
    private long number(GuiTextField field, String name) {
        try {
            long value = Long.parseLong(field.getText().trim());
            if (value < 0) throw new NumberFormatException();
            return value;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(name + " must be a non-negative whole number");
        }
    }
    private double speed() {
        try {
            double value = Double.parseDouble(speedField.getText().trim());
            if (Double.isNaN(value) || Double.isInfinite(value) || value <= 0 || value > 100)
                throw new NumberFormatException();
            return value;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Speed must be greater than 0 and at most 100");
        }
    }

    @Override protected void actionPerformed(GuiButton button) {
        try {
            TimelineTrack track = selectedTrack();
            switch (button.id) {
                case 0: if (!tracks().isEmpty()) trackIndex = (trackIndex - 1 + tracks().size()) % tracks().size(); syncSelection(); break;
                case 1: if (!tracks().isEmpty()) trackIndex = (trackIndex + 1) % tracks().size(); syncSelection(); break;
                case 2:
                case 3:
                    if (track == null || track.getClips().isEmpty()) throw new IllegalArgumentException("The selected track has no clips");
                    clipIndex = (clipIndex + (button.id == 2 ? -1 : 1) + track.getClips().size()) % track.getClips().size();
                    selectedClipId = track.getClips().get(clipIndex).getId();
                    syncSelection();
                    break;
                case 4:
                    if (track == null) throw new IllegalArgumentException("Create a track first");
                    String source = sourceField.getText().trim();
                    if (source.isEmpty()) throw new IllegalArgumentException("Enter a clip source");
                    TimelineClip created = new TimelineClip("clip-" + UUID.randomUUID(), source,
                            number(startField, "Start"), number(durationField, "Duration"));
                    created.setSpeed(speed());
                    track.add(created);
                    selectedClipId = created.getId();
                    status = "Clip added";
                    syncSelection();
                    break;
                case 5:
                    requireClip(track);
                    if (!track.move(selectedClipId, number(startField, "Start"))) throw new IllegalArgumentException("Clip no longer exists");
                    status = "Clip moved";
                    syncSelection();
                    break;
                case 6:
                    requireClip(track);
                    if (!track.trim(selectedClipId, number(startField, "Start"), number(durationField, "Duration")))
                        throw new IllegalArgumentException("Clip no longer exists");
                    status = "Clip trimmed";
                    syncSelection();
                    break;
                case 7:
                    requireClip(track);
                    if (!track.setSpeed(selectedClipId, speed())) throw new IllegalArgumentException("Clip no longer exists");
                    status = "Playback speed updated";
                    syncSelection();
                    break;
                case 8:
                    requireClip(track);
                    if (!track.remove(selectedClipId)) throw new IllegalArgumentException("Clip no longer exists");
                    selectedClipId = null;
                    status = "Clip removed";
                    syncSelection();
                    break;
                case 9: mc.displayGuiScreen(parent); return;
                case 10: importMedia(); break;
                case 11:
                    long markerTime = number(startField, "Marker time");
                    selectedMarker = new TimelineMarker(markerTime, "Marker", TimelineMarkerKind.values()[markerKindIndex]);
                    timeline.addMarker(selectedMarker);
                    status = "Marker added at " + markerTime + " ms";
                    break;
                case 12:
                    if (selectedMarker == null || !timeline.removeMarker(selectedMarker))
                        throw new IllegalArgumentException("Select a timeline marker first");
                    status = "Marker removed";
                    selectedMarker = null;
                    break;
                case 13:
                    markerKindIndex = (markerKindIndex + 1) % TimelineMarkerKind.values().length;
                    status = "New marker kind: " + TimelineMarkerKind.values()[markerKindIndex].name();
                    break;
                case 14:
                    requireClip(track);
                    if (!"video".equals(track.getId()) || "@capture".equals(selectedClip().getSource()))
                        throw new IllegalArgumentException("Effects are available for imported video clips only");
                    TimelineVideoEffect[] effects = TimelineVideoEffect.values();
                    TimelineVideoEffect currentEffect = selectedClip().getEffect();
                    selectedClip().setEffect(effects[(currentEffect.ordinal() + 1) % effects.length]);
                    status = "Clip effect: " + selectedClip().getEffect().name();
                    break;
                case 15:
                case 16:
                    requireClip(track);
                    if (!"video".equals(track.getId()) || "@capture".equals(selectedClip().getSource()))
                        throw new IllegalArgumentException("Fade controls are available for imported video clips only");
                    TimelineClip fadeClip = selectedClip();
                    long currentFade = button.id == 15 ? fadeClip.getFadeInMillis() : fadeClip.getFadeOutMillis();
                    long otherFade = button.id == 15 ? fadeClip.getFadeOutMillis() : fadeClip.getFadeInMillis();
                    long nextFade = nextFadeDuration(currentFade, Math.max(0L, fadeClip.getDuration() - otherFade));
                    if (button.id == 15) fadeClip.setFadeInMillis(nextFade);
                    else fadeClip.setFadeOutMillis(nextFade);
                    status = (button.id == 15 ? "Fade in: " : "Fade out: ") + nextFade + " ms";
                    break;
                default: break;
            }
        } catch (IllegalArgumentException ex) {
            status = ex.getMessage();
        }
    }
    private static long nextFadeDuration(long current, long maximum) {
        long[] values = new long[] { 0L, 100L, 250L, 500L, 1000L };
        for (int i = 0; i < values.length; i++) {
            if (values[i] == current) {
                for (int step = 1; step <= values.length; step++) {
                    long candidate = values[(i + step) % values.length];
                    if (candidate <= maximum) return candidate;
                }
                return 0L;
            }
        }
        for (long value : values) if (value > current && value <= maximum) return value;
        return 0L;
    }

    private void importMedia() {
        if (mediaProbePending) { status = "A media import is already being processed"; return; }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Import media into Recording Studio");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new FileNameExtensionFilter(
                "Video files (MP4, MOV, MKV, AVI, WEBM, M4V, GIF)",
                "mp4", "mov", "mkv", "avi", "webm", "m4v", "gif", "mpeg", "mpg", "flv", "wmv"));
        chooser.addChoosableFileFilter(new FileNameExtensionFilter(
                "Audio files (WAV, MP3, AAC, M4A, OGG, FLAC)",
                "wav", "mp3", "aac", "m4a", "ogg", "flac", "wma"));
        int result = chooser.showOpenDialog(null);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File selected = chooser.getSelectedFile();
        if (selected == null || !selected.isFile()) {
            status = "Selected media file does not exist";
            return;
        }
        String name = selected.getName().toLowerCase(java.util.Locale.ROOT);
        boolean audio = name.matches(".*\\.(wav|mp3|aac|m4a|ogg|flac|wma)$");
        boolean video = name.matches(".*\\.(mp4|mov|mkv|avi|webm|m4v|gif|mpeg|mpg|flv|wmv)$");
        if (!audio && !video) {
            status = "Unsupported media type";
            return;
        }

        String targetTrackId = audio ? "audio" : "video";
        int targetIndex = -1;
        for (int i = 0; i < tracks().size(); i++) {
            if (targetTrackId.equals(tracks().get(i).getId())) {
                targetIndex = i;
                break;
            }
        }
        if (targetIndex < 0) {
            status = "No " + targetTrackId + " timeline track is available";
            return;
        }

        trackIndex = targetIndex;
        long startMillis;
        try {
            startMillis = timeline.duration();
            startField.setText(Long.toString(startMillis));
        } catch (RuntimeException failure) {
            status = "Cannot determine the timeline insertion position: " + failure.getMessage();
            return;
        }
        sourceField.setText(selected.getAbsolutePath());
        mediaProbeFile = selected;
        mediaProbeTrackId = targetTrackId;
        mediaProbeStart = startMillis;
        mediaProbeFailure = null;
        mediaProbeDuration = 0L;
        mediaProbeComplete = false;
        mediaProbePending = true;
        status = "Reading media duration...";
        Thread worker = new Thread(new Runnable() {
            public void run() {
                try {
                    mediaProbeDuration = probeDurationMillis(selected);
                } catch (Exception failure) {
                    mediaProbeFailure = failure.getMessage();
                } finally {
                    mediaProbeComplete = true;
                }
            }
        }, "Alpha-RecordingStudio-MediaProbe");
        worker.setDaemon(true);
        worker.start();
    }

    private void finishMediaImportIfReady() {
        if (!mediaProbeComplete) return;
        mediaProbeComplete = false;
        mediaProbePending = false;
        File selected = mediaProbeFile;
        String trackId = mediaProbeTrackId;
        long startMillis = mediaProbeStart;
        long durationMillis = mediaProbeDuration;
        String failure = mediaProbeFailure;
        mediaProbeFile = null;
        if (screenClosed || selected == null) return;

        int targetIndex = -1;
        for (int i = 0; i < tracks().size(); i++) {
            if (trackId.equals(tracks().get(i).getId())) { targetIndex = i; break; }
        }
        if (targetIndex < 0) {
            status = "The target timeline track is no longer available";
            return;
        }
        trackIndex = targetIndex;
        if (failure != null || durationMillis <= 0L) {
            selectedClipId = null;
            sourceField.setText(selected.getAbsolutePath());
            durationField.setText("1000");
            speedField.setText("1.0");
            status = "Media selected, but FFprobe could not read its duration. Set Duration (ms) and use Add Clip. "
                    + (failure == null ? "Invalid media duration" : failure);
            return;
        }

        TimelineTrack track = selectedTrack();
        TimelineClip created = new TimelineClip("clip-" + UUID.randomUUID(),
                selected.getAbsolutePath(), startMillis, durationMillis);
        created.setSpeed(1.0D);
        track.add(created);
        selectedClipId = created.getId();
        status = ("audio".equals(trackId) ? "Imported audio: " : "Imported video: ") + selected.getName();
        syncSelection();
    }

    private static long probeDurationMillis(File media) throws Exception {
        Process process = new ProcessBuilder("ffprobe", "-v", "error",
                "-show_entries", "format=duration",
                "-of", "default=noprint_wrappers=1:nokey=1",
                media.getAbsolutePath()).redirectErrorStream(true).start();
        String output;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
            try {
                StringBuilder value = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    if (value.length() > 0) value.append(' ');
                    value.append(line.trim());
                }
                output = value.toString();
            } finally {
                reader.close();
            }
            int exit = process.waitFor();
            if (exit != 0) throw new IOException("FFprobe exited with code " + exit);
        } finally {
            if (process.isAlive()) process.destroyForcibly();
        }
        double seconds = Double.parseDouble(output.trim());
        if (Double.isNaN(seconds) || Double.isInfinite(seconds) || seconds <= 0.0D
                || seconds > Long.MAX_VALUE / 1000.0D) {
            throw new IOException("Invalid media duration");
        }
        return Math.max(1L, Math.round(seconds * 1000.0D));
    }

    private void requireClip(TimelineTrack track) {
        if (track == null || selectedClip() == null) throw new IllegalArgumentException("Select a clip first");
    }

    @Override protected void keyTyped(char c, int key) throws java.io.IOException {
        if (key == 1) { mc.displayGuiScreen(parent); return; }
        sourceField.textboxKeyTyped(c, key);
        startField.textboxKeyTyped(c, key);
        durationField.textboxKeyTyped(c, key);
        speedField.textboxKeyTyped(c, key);
        super.keyTyped(c, key);
    }
    private int timelineLeft() { return width / 2 - 145; }
    private int timelineRight() { return width / 2 + 145; }
    private int timelineTop() { return height / 2 + 136; }
    private int timelineRowHeight() { return 25; }
    private long timelineScale() { return Math.max(10000L, timeline.duration() + 10000L); }
    private long displayTimelineScale() { return dragMode != DRAG_NONE && dragScale > 0L ? dragScale : timelineScale(); }

    private long millisAtTimelineX(int x) {
        int left = timelineLeft();
        int span = Math.max(1, timelineRight() - left);
        double ratio = Math.max(0.0D, Math.min(1.0D, (x - left) / (double) span));
        return Math.max(0L, Math.round(ratio * timelineScale()));
    }

    private void drawTimeline() {
        int left = timelineLeft();
        int right = timelineRight();
        int top = timelineTop();
        int rowHeight = timelineRowHeight();
        int bottom = height - 42;
        if (top >= bottom) return;

        net.minecraft.client.gui.Gui.drawRect(left - 8, top - 22, right + 8, bottom, 0xCC101010);
        net.minecraft.client.gui.Gui.drawRect(left - 8, top - 22, right + 8, top - 20, 0xFFFFC83D);
        drawString(fontRendererObj, "VISUAL TIMELINE  |  Drag clips to move; drag either edge to trim",
                left - 2, top - 16, 0xFFFFC83D);

        long scale = displayTimelineScale();
        int span = Math.max(1, right - left);
        List<TimelineTrack> allTracks = tracks();
        int visibleRows = Math.max(0, (bottom - top) / rowHeight);
        for (int i = 0; i < allTracks.size() && i < visibleRows; i++) {
            TimelineTrack currentTrack = allTracks.get(i);
            int rowY = top + i * rowHeight;
            boolean active = i == trackIndex;
            net.minecraft.client.gui.Gui.drawRect(left, rowY, right, rowY + 19,
                    active ? 0xFF302719 : 0xFF202020);
            drawString(fontRendererObj, currentTrack.getName(), left - 2, rowY + 20, 0xFFBBBBBB);

            for (TimelineClip currentClip : currentTrack.getClips()) {
                if (currentClip.getDuration() <= 0L) continue;
                int clipLeft = left + (int) Math.floor(
                        Math.max(0L, currentClip.getStart()) / (double) scale * span);
                int clipRight = left + (int) Math.ceil(
                        Math.min(scale, currentClip.getEnd()) / (double) scale * span);
                clipLeft = Math.max(left, Math.min(right - 1, clipLeft));
                clipRight = Math.max(clipLeft + 3, Math.min(right, clipRight));
                boolean selected = currentClip.getId().equals(selectedClipId) && active;
                int color = selected ? 0xFFFFC83D
                        : "audio".equals(currentTrack.getId()) ? 0xFF397F83 : 0xFF98701E;
                net.minecraft.client.gui.Gui.drawRect(clipLeft, rowY + 2, clipRight, rowY + 17, color);
                if (selected) {
                    net.minecraft.client.gui.Gui.drawRect(clipLeft, rowY + 1, clipLeft + 2, rowY + 18, 0xFFFFFFFF);
                    net.minecraft.client.gui.Gui.drawRect(clipRight - 2, rowY + 1, clipRight, rowY + 18, 0xFFFFFFFF);
                }
                if (clipRight - clipLeft > 34) {
                    String label = currentClip.getId();
                    if (label.length() > 13) label = label.substring(0, 13);
                    drawString(fontRendererObj, label, clipLeft + 4, rowY + 5, 0xFF17130A);
                }
            }
        }

        for (TimelineMarker marker : timeline.getMarkers()) {
            int markerX = left + (int) Math.round(Math.min(scale, marker.getTimeMillis())
                    / (double) scale * span);
            if (markerX >= left && markerX <= right) {
                net.minecraft.client.gui.Gui.drawRect(markerX, top, markerX + 1,
                        Math.min(bottom, top + visibleRows * rowHeight), 0xFF66D9E8);
                if (marker == selectedMarker) {
                    net.minecraft.client.gui.Gui.drawRect(markerX - 2, top - 3, markerX + 3, top + 3, 0xFFFFFFFF);
                }
                String markerLabel = marker.getLabel();
                if (markerLabel.length() > 10) markerLabel = markerLabel.substring(0, 10);
                drawString(fontRendererObj, markerLabel, Math.min(right - 42, markerX + 3),
                        top - 10, marker == selectedMarker ? 0xFFFFFFFF : 0xFF66D9E8);
            }
        }

    }

    private boolean beginTimelineDrag(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0) return false;
        int left = timelineLeft();
        int right = timelineRight();
        int top = timelineTop();
        int rowHeight = timelineRowHeight();
        int row = (mouseY - top) / rowHeight;
        if (mouseX < left || mouseX > right || mouseY < top
                || mouseY >= top + Math.max(1, tracks().size()) * rowHeight) return false;

        long markerScale = timelineScale();
        int markerSpan = Math.max(1, right - left);
        for (TimelineMarker marker : timeline.getMarkers()) {
            int markerX = left + (int) Math.round(Math.min(markerScale, marker.getTimeMillis())
                    / (double) markerScale * markerSpan);
            if (Math.abs(mouseX - markerX) <= 4) {
                selectedMarker = marker;
                selectedClipId = null;
                dragMode = DRAG_MARKER;
                dragScale = markerScale;
                dragOriginX = mouseX;
                dragMarkerOriginalTime = marker.getTimeMillis();
                startField.setText(Long.toString(marker.getTimeMillis()));
                status = "Dragging marker: " + marker.getLabel();
                return true;
            }
        }
        if (row < 0 || row >= tracks().size()
                || mouseY >= top + (row + 1) * rowHeight - 3) return false;

        selectedMarker = null;
        trackIndex = row;
        TimelineTrack targetTrack = selectedTrack();
        if (targetTrack == null) return true;
        selectedClipId = null;
        int span = Math.max(1, right - left);
        long scale = timelineScale();
        for (TimelineClip candidate : targetTrack.getClips()) {
            if (candidate.getDuration() <= 0L) continue;
            int clipLeft = left + (int) Math.floor(candidate.getStart() / (double) scale * span);
            int clipRight = left + (int) Math.ceil(candidate.getEnd() / (double) scale * span);
            clipLeft = Math.max(left, Math.min(right - 1, clipLeft));
            clipRight = Math.max(clipLeft + 3, Math.min(right, clipRight));
            if (mouseX < clipLeft || mouseX > clipRight) continue;

            selectedClipId = candidate.getId();
            dragTrack = targetTrack;
            dragClip = candidate;
            dragOriginalStart = candidate.getStart();
            dragOriginalDuration = candidate.getDuration();
            dragScale = timelineScale();
            dragOriginX = mouseX;
            if (mouseX - clipLeft <= 6) dragMode = DRAG_TRIM_LEFT;
            else if (clipRight - mouseX <= 6) dragMode = DRAG_TRIM_RIGHT;
            else dragMode = DRAG_MOVE;
            syncSelection();
            status = dragMode == DRAG_MOVE ? "Dragging clip"
                    : dragMode == DRAG_TRIM_LEFT ? "Trimming clip start" : "Trimming clip end";
            return true;
        }

        syncSelection();
        status = "Track selected";
        return true;
    }

    @Override protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton,
                                             long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        if (dragMode == DRAG_NONE) return;
        long delta = Math.round((mouseX - dragOriginX)
                * (dragScale / (double) Math.max(1, timelineRight() - timelineLeft())));
        try {
            if (dragMode == DRAG_MARKER) {
                long targetTime = Math.max(0L, dragMarkerOriginalTime + delta);
                TimelineMarker moved = timeline.moveMarker(selectedMarker, targetTime);
                if (moved != null) {
                    selectedMarker = moved;
                    startField.setText(Long.toString(targetTime));
                }
            } else if (dragTrack == null || dragClip == null) {
                return;
            } else if (dragMode == DRAG_MOVE) {
                dragTrack.move(dragClip.getId(), Math.max(0L, dragOriginalStart + delta));
            } else if (dragMode == DRAG_TRIM_LEFT) {
                long end = dragOriginalStart + dragOriginalDuration;
                long newStart = Math.max(0L, Math.min(end - 1L, dragOriginalStart + delta));
                dragTrack.trim(dragClip.getId(), newStart, end - newStart);
            } else if (dragMode == DRAG_TRIM_RIGHT) {
                dragTrack.trim(dragClip.getId(), dragOriginalStart,
                        Math.max(1L, dragOriginalDuration + delta));
            }
            syncSelection();
        } catch (IllegalArgumentException failure) {
            status = failure.getMessage();
        }
    }

    @Override protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        if (dragMode != DRAG_NONE) {
            if (dragMode == DRAG_MARKER) {
                status = "Marker position updated";
            } else {
                status = dragMode == DRAG_MOVE ? "Clip position updated" : "Clip trim updated";
                syncSelection();
            }
        }
        dragMode = DRAG_NONE;
        dragTrack = null;
        dragClip = null;
    }

    @Override protected void mouseClicked(int x, int y, int button) throws java.io.IOException {
        super.mouseClicked(x, y, button);
        if (beginTimelineDrag(x, y, button)) return;
        sourceField.mouseClicked(x, y, button);
        startField.mouseClicked(x, y, button);
        durationField.mouseClicked(x, y, button);
        speedField.mouseClicked(x, y, button);
    }
    @Override public void onGuiClosed() { screenClosed = true; }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        finishMediaImportIfReady();
        drawDefaultBackground();
        net.minecraft.client.gui.Gui.drawRect(width / 2 - 165, 10, width / 2 + 165, height - 8, 0xE8101010);
        net.minecraft.client.gui.Gui.drawRect(width / 2 - 165, 10, width / 2 + 165, 12, 0xFFFFC83D);
        drawCenteredString(fontRendererObj, "ALPHA CLIENT  /  RECORDING STUDIO", width / 2, 20, 0xFFAAAAAA);
        drawCenteredString(fontRendererObj, "TIMELINE EDITOR", width / 2, 34, 0xFFFFC83D);
        TimelineTrack track = selectedTrack();
        TimelineClip clip = selectedClip();
        drawString(fontRendererObj, "Duration: " + timeline.duration() + " ms", width / 2 - 145, 52, 0xFFFFFF);
        drawString(fontRendererObj, "Track: " + (track == null ? "None" : track.getName()), width / 2 - 145, 66, 0xBBBBBB);
        drawString(fontRendererObj, "Clip: " + (clip == null ? "None" : clip.getId()), width / 2 - 145, 80, 0xBBBBBB);
        drawString(fontRendererObj, "Source", width / 2 - 145, height / 2 - 101, 0xFFFFFF);
        drawString(fontRendererObj, "Start (ms)", width / 2 - 145, height / 2 - 61, 0xFFFFFF);
        drawString(fontRendererObj, "Duration (ms)", width / 2 - 41, height / 2 - 61, 0xFFFFFF);
        drawString(fontRendererObj, "Speed", width / 2 + 63, height / 2 - 61, 0xFFFFFF);
        drawTimeline();
        for (GuiButton button : buttonList) {
            if (button.id == 13) button.displayString = "Kind: " + TimelineMarkerKind.values()[markerKindIndex].name();
            if (button.id == 14 || button.id == 15 || button.id == 16) {
                TimelineTrack activeTrack = selectedTrack();
                TimelineClip activeClip = selectedClip();
                boolean supported = activeTrack != null && "video".equals(activeTrack.getId())
                        && activeClip != null && !"@capture".equals(activeClip.getSource());
                button.enabled = supported;
                if (button.id == 14) button.displayString = "Effect: "
                        + (activeClip == null ? "NONE" : activeClip.getEffect().name());
                if (button.id == 15) button.displayString = "Fade In: "
                        + (activeClip == null ? 0 : activeClip.getFadeInMillis()) + " ms";
                if (button.id == 16) button.displayString = "Fade Out: "
                        + (activeClip == null ? 0 : activeClip.getFadeOutMillis()) + " ms";
            }
        }
        sourceField.drawTextBox();
        startField.drawTextBox();
        durationField.drawTextBox();
        speedField.drawTextBox();
        drawCenteredString(fontRendererObj, status, width / 2, height - 18, 0xFFFFC83D);
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (runtime != null) runtime.drawCameraPreview();
    }
}
