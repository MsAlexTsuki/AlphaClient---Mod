package com.alpharip.staffmod.BuildTimelapse.ui;

import com.alpharip.staffmod.BuildTimelapse.camera.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import java.io.File;
import java.io.IOException;

public final class CameraEditorScreen extends GuiScreen {
    private final GuiScreen parent;
    private final CameraController camera;
    private final CameraPathStore pathStore;
    private CameraPath path = new CameraPath("Studio Camera Path");
    private GuiTextField pathId;
    private String status = "Adjust the independent camera";

    public CameraEditorScreen(GuiScreen parent, CameraController camera) {
        if (parent == null || camera == null) throw new IllegalArgumentException();
        this.parent = parent;
        this.camera = camera;
        this.pathStore = new FileCameraPathStore(
            new File(Minecraft.getMinecraft().mcDataDir, "buildtimelapse/camera-paths")
        );
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }

    @Override
    public void initGui() {
        String previousPathId = pathId == null ? "studio-camera" : pathId.getText();
        buttonList.clear();
        int x = width / 2 - 150;
        int y = height / 2 - 80;
        buttonList.add(new GuiButton(0, x, y, 90, 20, "X -"));
        buttonList.add(new GuiButton(1, x + 95, y, 90, 20, "X +"));
        buttonList.add(new GuiButton(2, x + 190, y, 110, 20, "Mode: " + camera.getMode()));
        buttonList.add(new GuiButton(3, x, y + 24, 90, 20, "Y -"));
        buttonList.add(new GuiButton(4, x + 95, y + 24, 90, 20, "Y +"));
        buttonList.add(new GuiButton(5, x + 190, y + 24, 110, 20, "FOV +"));
        buttonList.add(new GuiButton(6, x, y + 48, 90, 20, "Z -"));
        buttonList.add(new GuiButton(7, x + 95, y + 48, 90, 20, "Z +"));
        buttonList.add(new GuiButton(8, x + 190, y + 48, 110, 20, "FOV -"));
        buttonList.add(new GuiButton(9, x, y + 78, 90, 20, "Yaw -"));
        buttonList.add(new GuiButton(10, x + 95, y + 78, 90, 20, "Yaw +"));
        buttonList.add(new GuiButton(11, x + 190, y + 78, 110, 20, "Pitch +"));
        buttonList.add(new GuiButton(12, x, y + 108, 90, 20, "Add Point"));
        buttonList.add(new GuiButton(13, x + 95, y + 108, 90, 20, "Play Path"));
        buttonList.add(new GuiButton(14, x + 190, y + 108, 110, 20, "Stop Path"));
        buttonList.add(new GuiButton(15, width / 2 - 50, y + 140, 100, 20, "Back"));

        pathId = new GuiTextField(16, fontRendererObj, width / 2 - 150, y + 170, 145, 20);
        pathId.setMaxStringLength(80);
        pathId.setText(previousPathId == null || previousPathId.trim().isEmpty() ? "studio-camera" : previousPathId);
        buttonList.add(new GuiButton(16, width / 2 + 5, y + 170, 70, 20, "Save"));
        buttonList.add(new GuiButton(17, width / 2 + 80, y + 170, 70, 20, "Load"));
    }

    private void adjust(double dx, double dy, double dz, float yaw, float pitch, float fov) {
        CameraPose p = camera.getPose();
        try {
            camera.setPose(new CameraPose(
                p.x + dx, p.y + dy, p.z + dz,
                p.yaw + yaw, p.pitch + pitch, p.roll,
                Math.max(1, Math.min(179, p.fov + fov))
            ));
            status = "Camera pose updated";
        } catch (Exception e) {
            status = "Camera update failed: " + e.getMessage();
        }
    }

    private void savePath() {
        String id = pathId == null ? "" : pathId.getText().trim();
        if (id.isEmpty()) {
            status = "Enter a path ID before saving";
            return;
        }
        try {
            pathStore.save(id, path);
            status = "Saved camera path: " + id;
        } catch (IOException | IllegalArgumentException e) {
            status = "Could not save camera path: " + e.getMessage();
        }
    }

    private void loadPath() {
        String id = pathId == null ? "" : pathId.getText().trim();
        if (id.isEmpty()) {
            status = "Enter a path ID before loading";
            return;
        }
        try {
            path = pathStore.load(id);
            camera.stopPath();
            status = "Loaded " + path.getName() + " (" + path.getWaypoints().size() + " points)";
        } catch (IOException | IllegalArgumentException e) {
            status = "Could not load camera path: " + e.getMessage();
        }
    }

    @Override
    protected void actionPerformed(GuiButton b) {
        if (b.id == 0) adjust(-1, 0, 0, 0, 0, 0);
        else if (b.id == 1) adjust(1, 0, 0, 0, 0, 0);
        else if (b.id == 2) {
            CameraMode[] modes = CameraMode.values();
            camera.setMode(modes[(camera.getMode().ordinal() + 1) % modes.length]);
            initGui();
        } else if (b.id == 3) adjust(0, -1, 0, 0, 0, 0);
        else if (b.id == 4) adjust(0, 1, 0, 0, 0, 0);
        else if (b.id == 5) adjust(0, 0, 0, 0, 0, 5);
        else if (b.id == 6) adjust(0, 0, -1, 0, 0, 0);
        else if (b.id == 7) adjust(0, 0, 1, 0, 0, 0);
        else if (b.id == 8) adjust(0, 0, 0, 0, 0, -5);
        else if (b.id == 9) adjust(0, 0, 0, -15, 0, 0);
        else if (b.id == 10) adjust(0, 0, 0, 15, 0, 0);
        else if (b.id == 11) adjust(0, 0, 0, 0, 10, 0);
        else if (b.id == 12) {
            path.add(new CameraWaypoint(
                "point-" + java.util.UUID.randomUUID().toString(), camera.getPose(), 2000, 500, "SMOOTH"
            ));
            status = "Waypoint added: " + path.getWaypoints().size();
        } else if (b.id == 13) {
            if (path.getWaypoints().size() < 2) {
                status = "Add at least two waypoints first";
                return;
            }
            try {
                camera.activate();
                camera.playPath(path, System.currentTimeMillis());
                status = "Camera path playing";
            } catch (Exception e) {
                status = "Could not play camera path: " + e.getMessage();
            }
        } else if (b.id == 14) {
            camera.stopPath();
            status = "Camera path stopped";
        } else if (b.id == 15) {
            mc.displayGuiScreen(parent);
        } else if (b.id == 16) {
            savePath();
        } else if (b.id == 17) {
            loadPath();
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (pathId != null) pathId.textboxKeyTyped(typedChar, keyCode);
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (pathId != null) pathId.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "CINEMATIC CAMERA", width / 2, 24, 0xFFC83D);
        CameraPose p = camera.getPose();
        drawCenteredString(fontRendererObj,
            String.format(java.util.Locale.ROOT, "X %.2f  Y %.2f  Z %.2f", p.x, p.y, p.z),
            width / 2, 45, 0xFFFFFF);
        drawCenteredString(fontRendererObj,
            String.format(java.util.Locale.ROOT, "Yaw %.1f  Pitch %.1f  FOV %.1f", p.yaw, p.pitch, p.fov),
            width / 2, 59, 0xFFFFFF);
        drawString(fontRendererObj, "Path ID", width / 2 - 150, height / 2 + 75, 0xBBBBBB);
        if (pathId != null) pathId.drawTextBox();
        drawCenteredString(fontRendererObj,
            "Waypoints: " + path.getWaypoints().size() + " | " + path.getName(),
            width / 2, height / 2 + 196, 0xFFFFFF);
        drawCenteredString(fontRendererObj, status, width / 2, height - 12, 0xBBBBBB);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
