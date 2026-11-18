package com.alpharip.staffmod.RecordingStudio.ui;

import com.alpharip.staffmod.RecordingStudio.timeline.*;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import java.util.List;
import java.util.UUID;

/** Timeline editor for track and clip operations; the world continues ticking. */
public final class TimelineEditorScreen extends GuiScreen {
    private final GuiScreen parent;
    private final Timeline timeline;
    private GuiTextField sourceField, startField, durationField, speedField;
    private int trackIndex, clipIndex;
    private String selectedClipId;
    private String status = "Select a track or create a clip";

    public TimelineEditorScreen(GuiScreen parent, Timeline timeline) {
        if (parent == null || timeline == null) throw new IllegalArgumentException();
        this.parent = parent;
        this.timeline = timeline;
    }

    @Override public boolean doesGuiPauseGame() { return false; }

    @Override public void initGui() {
        buttonList.clear();
        int left = width / 2 - 145;
        sourceField = new GuiTextField(0, fontRendererObj, left, height / 2 - 88, 290, 18);
        sourceField.setMaxStringLength(180);
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
        return clips.get(clipIndex);
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
            throw new IllegalArgumentException("Speed must be between 0 and 100");
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
                default: break;
            }
        } catch (IllegalArgumentException ex) {
            status = ex.getMessage();
        }
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
    @Override protected void mouseClicked(int x, int y, int button) throws java.io.IOException {
        super.mouseClicked(x, y, button);
        sourceField.mouseClicked(x, y, button);
        startField.mouseClicked(x, y, button);
        durationField.mouseClicked(x, y, button);
        speedField.mouseClicked(x, y, button);
    }
    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
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
        sourceField.drawTextBox();
        startField.drawTextBox();
        durationField.drawTextBox();
        speedField.drawTextBox();
        drawCenteredString(fontRendererObj, status, width / 2, height - 18, 0xFFFFC83D);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
