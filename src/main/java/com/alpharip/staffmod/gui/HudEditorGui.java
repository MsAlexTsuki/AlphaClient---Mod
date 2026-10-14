package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.hud.BhopHud;
import com.alpharip.staffmod.hud.TargetHud;
import com.alpharip.staffmod.movement.BhopMode;
import com.alpharip.staffmod.movement.BhopProfileManager;
import com.alpharip.staffmod.util.KnockbackTracker;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.opengl.GL11;

/**
 * Alpha's Staffing Mods
 *
 * Author: MsAlexTsuki
 */
public class HudEditorGui extends ModernGuiScreen {

    private static final int HUD_TARGET = 0;
    private static final int HUD_KNOCKBACK = 1;
    private static final int HUD_BHOP = 2;

    private static final int TARGET_WIDTH = 145;
    private static final int TARGET_HEIGHT = 48;
    private static final int KNOCKBACK_WIDTH = 175;
    private static final int KNOCKBACK_HEIGHT = 70;
    private static final int BHOP_WIDTH = 180;
    private static final int BHOP_HEIGHT = 24;

    private static final int TARGET_RESIZE_SIZE = 12;

    private static final int BUTTON_BACK = 100;
    private static final int BUTTON_RESET = 101;

    private GuiScreen parentScreen;
    private int selectedHud = -1;
    private boolean dragging = false;
    private boolean resizing = false;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;
    private float resizeStartScale = 1.0F;
    private int resizeStartMouseX = 0;

    private boolean targetWasEnabled = false;
    private boolean knockbackWasEnabled = false;
    private boolean bhopWasEnabled = false;
    private boolean realHudsHidden = false;

    public HudEditorGui(GuiScreen parent) {
        this.parentScreen = parent;
    }

    @Override
    public void initGui() {
        super.initGui();
        hideRealHuds();

        this.buttonList.add(
                new ModernButton(
                        BUTTON_BACK,
                        getGuiCenter() - 170,
                        getGuiBottom() - 42,
                        160,
                        32,
                        "BACK"
                )
        );

        this.buttonList.add(
                new ModernButton(
                        BUTTON_RESET,
                        getGuiCenter() + 10,
                        getGuiBottom() - 42,
                        160,
                        32,
                        "RESET"
                )
        );
    }

    private void hideRealHuds() {
        if (realHudsHidden) {
            return;
        }

        realHudsHidden = true;

        targetWasEnabled = TargetHud.isEnabled();
        TargetHud.setEnabled(false);

        knockbackWasEnabled = KnockbackTracker.isEnabled();
        KnockbackTracker.setEnabled(false);

        bhopWasEnabled = BhopHud.isEnabled();
        BhopHud.setEditorPreviewMode(true);
    }

    private void restoreRealHuds() {
        if (!realHudsHidden) {
            return;
        }

        realHudsHidden = false;

        TargetHud.setEnabled(targetWasEnabled);
        KnockbackTracker.setEnabled(knockbackWasEnabled);
        BhopHud.setEditorPreviewMode(false);
        BhopHud.setEnabled(bhopWasEnabled);
    }

    @Override
    public void onGuiClosed() {
        restoreRealHuds();
        super.onGuiClosed();
    }

    private int toEditorX(int hudX) {
        float scale = getGuiScale();

        if (scale >= 0.999F) {
            return hudX;
        }

        float centerX = this.width / 2.0F;

        return Math.round(
                centerX
                        + (hudX - centerX) / scale
        );
    }

    private int toEditorY(int hudY) {
        float scale = getGuiScale();

        if (scale >= 0.999F) {
            return hudY;
        }

        float centerY = this.height / 2.0F;

        return Math.round(
                centerY
                        + (hudY - centerY) / scale
        );
    }

    private int toHudX(int editorX) {
        float scale = getGuiScale();

        if (scale >= 0.999F) {
            return editorX;
        }

        float centerX = this.width / 2.0F;

        return Math.round(
                centerX
                        + (editorX - centerX) * scale
        );
    }

    private int toHudY(int editorY) {
        float scale = getGuiScale();

        if (scale >= 0.999F) {
            return editorY;
        }

        float centerY = this.height / 2.0F;

        return Math.round(
                centerY
                        + (editorY - centerY) * scale
        );
    }

    @Override
    public void drawScreen(
            int mouseX,
            int mouseY,
            float partialTicks) {

        drawModernBackground();

        drawModTitle(
                "HUD EDITOR",
                getGuiCenter(),
                getGuiTop() + 18
        );

        drawCenteredString(
                this.fontRendererObj,
                "Click a HUD to select it.",
                getGuiCenter(),
                getGuiTop() + 58,
                COLOR_GRAY
        );

        drawTargetPreview();
        drawKnockbackPreview();
        drawBhopPreview();

        super.drawScreen(
                mouseX,
                mouseY,
                partialTicks
        );
    }

    private void drawTargetPreview() {

        int hudX = TargetHud.getHudX();
        int hudY = TargetHud.getHudY();

        int editorX = toEditorX(hudX);
        int editorY = toEditorY(hudY);

        float scale =
                0.50F
                        + (
                        TargetHud.getHudScale()
                                / 100.0F
                );

        int width =
                Math.round(
                        TARGET_WIDTH * scale
                );

        int height =
                Math.round(
                        TARGET_HEIGHT * scale
                );

        drawRect(
                editorX,
                editorY,
                editorX + width,
                editorY + height,
                selectedHud == HUD_TARGET
                        ? 0xE0202020
                        : 0xC8101010
        );

        drawRect(
                editorX,
                editorY,
                editorX + width,
                editorY + Math.max(
                        1,
                        Math.round(2 * scale)
                ),
                COLOR_GREEN
        );

        drawRect(
                editorX,
                editorY,
                editorX + Math.max(
                        1,
                        Math.round(2 * scale)
                ),
                editorY + height,
                COLOR_GREEN
        );

        drawScaledString(
                "TARGET HUD",
                editorX + Math.round(8 * scale),
                editorY + Math.round(6 * scale),
                scale,
                COLOR_WHITE
        );

        drawScaledString(
                "Target: Player",
                editorX + Math.round(8 * scale),
                editorY + Math.round(21 * scale),
                scale,
                COLOR_GRAY
        );

        drawScaledString(
                "CPS: 12",
                editorX + Math.round(8 * scale),
                editorY + Math.round(34 * scale),
                scale,
                COLOR_GREEN
        );

        if (selectedHud == HUD_TARGET) {
            drawSelectionBorder(
                    editorX,
                    editorY,
                    width,
                    height
            );

            drawResizeHandle(
                    editorX,
                    editorY,
                    width,
                    height
            );
        }
    }

    private void drawResizeHandle(
            int editorX,
            int editorY,
            int width,
            int height) {

        int size = TARGET_RESIZE_SIZE;

        drawRect(
                editorX + width - size,
                editorY + height - size,
                editorX + width,
                editorY + height,
                0xFF101010
        );

        drawRect(
                editorX + width - 7,
                editorY + height - 7,
                editorX + width - 2,
                editorY + height - 2,
                COLOR_GREEN
        );
    }

    private void drawKnockbackPreview() {

        int hudX = KnockbackTracker.getHudX();
        int hudY = KnockbackTracker.getHudY();

        int editorX = toEditorX(hudX);
        int editorY = toEditorY(hudY);

        float scale =
                0.50F
                        + (
                        KnockbackTracker.getHudScale()
                                / 100.0F
                );

        int width =
                Math.round(
                        KNOCKBACK_WIDTH * scale
                );

        int height =
                Math.round(
                        KNOCKBACK_HEIGHT * scale
                );

        drawRect(
                editorX,
                editorY,
                editorX + width,
                editorY + height,
                0xC8101010
        );

        drawScaledString(
                "KNOCKBACK",
                editorX + Math.round(8 * scale),
                editorY + Math.round(7 * scale),
                scale,
                COLOR_WHITE
        );

        drawScaledString(
                "Target: Player",
                editorX + Math.round(8 * scale),
                editorY + Math.round(21 * scale),
                scale,
                COLOR_GRAY
        );

        drawScaledString(
                "Expected: 0.420",
                editorX + Math.round(8 * scale),
                editorY + Math.round(34 * scale),
                scale,
                COLOR_GRAY
        );

        drawScaledString(
                "Actual: 0.380",
                editorX + Math.round(8 * scale),
                editorY + Math.round(47 * scale),
                scale,
                COLOR_GRAY
        );

        drawScaledString(
                "Ratio: 90%",
                editorX + Math.round(8 * scale),
                editorY + Math.round(60 * scale),
                scale,
                COLOR_GREEN
        );

        if (selectedHud == HUD_KNOCKBACK) {
            drawSelectionBorder(
                    editorX,
                    editorY,
                    width,
                    height
            );
        }
    }

    private void drawBhopPreview() {

        int hudX = BhopHud.getHudX();
        int hudY = BhopHud.getHudY();

        int editorX = toEditorX(hudX);
        int editorY = toEditorY(hudY);

        float scale =
                BhopHud.getVisualScale();

        int width =
                Math.round(
                        BHOP_WIDTH * scale
                );

        int height =
                Math.round(
                        BHOP_HEIGHT * scale
                );

        drawRect(
                editorX,
                editorY,
                editorX + width,
                editorY + height,
                0x8C101010
        );

        drawRect(
                editorX,
                editorY,
                editorX + 1,
                editorY + height,
                0x30FFFFFF
        );

        drawRect(
                editorX + width - 1,
                editorY,
                editorX + width,
                editorY + height,
                0x30FFFFFF
        );

        drawRect(
                editorX,
                editorY + height - 1,
                editorX + width,
                editorY + height,
                0x20FFFFFF
        );

        String profileName =
                BhopProfileManager.getSelectedProfileName();

        if (profileName == null
                || profileName.trim().isEmpty()) {

            profileName = "Default";
        }

        String speedName =
                BhopMode.getSpeedLevelName();

        String hudText =
                profileName
                        + " \u2022 "
                        + speedName;

        drawScaledString(
                hudText,
                editorX + Math.round(8 * scale),
                editorY + Math.round(8 * scale),
                scale,
                COLOR_WHITE
        );

        if (selectedHud == HUD_BHOP) {

            drawSelectionBorder(
                    editorX,
                    editorY,
                    width,
                    height
            );
        }
    }

    private void drawScaledString(
            String text,
            int x,
            int y,
            float scale,
            int color) {

        GL11.glPushMatrix();

        GL11.glTranslatef(
                x,
                y,
                0.0F
        );

        GL11.glScalef(
                scale,
                scale,
                1.0F
        );

        this.fontRendererObj.drawStringWithShadow(
                text,
                0,
                0,
                color
        );

        GL11.glPopMatrix();
    }

    private void drawSelectionBorder(
            int x,
            int y,
            int width,
            int height) {

        drawRect(
                x,
                y,
                x + width,
                y + 2,
                COLOR_GREEN
        );

        drawRect(
                x,
                y + height - 2,
                x + width,
                y + height,
                COLOR_GREEN
        );

        drawRect(
                x,
                y,
                x + 2,
                y + height,
                COLOR_GREEN
        );

        drawRect(
                x + width - 2,
                y,
                x + width,
                y + height,
                COLOR_GREEN
        );
    }

    @Override
    protected void mouseClicked(
            int mouseX,
            int mouseY,
            int mouseButton)
            throws java.io.IOException {

        int guiMouseX =
                getScaledMouseX(mouseX);

        int guiMouseY =
                getScaledMouseY(mouseY);

        if (mouseButton == 0) {

            for (Object object : this.buttonList) {

                if (!(object instanceof GuiButton)) {
                    continue;
                }

                GuiButton button =
                        (GuiButton) object;

                if (!button.visible
                        || !button.enabled) {

                    continue;
                }

                if (guiMouseX >= button.xPosition
                        && guiMouseX <=
                        button.xPosition + button.width
                        && guiMouseY >= button.yPosition
                        && guiMouseY <=
                        button.yPosition + button.height) {

                    actionPerformed(button);
                    return;
                }
            }
        }

        if (mouseButton == 0) {

            if (isTargetResizeCorner(
                    guiMouseX,
                    guiMouseY)) {

                selectedHud = HUD_TARGET;
                dragging = true;
                resizing = true;

                resizeStartScale =
                        getTargetVisualScale();

                resizeStartMouseX =
                        guiMouseX;

                return;
            }

            if (isInsideTarget(
                    guiMouseX,
                    guiMouseY)) {

                selectedHud = HUD_TARGET;
                dragging = true;
                resizing = false;

                dragOffsetX =
                        guiMouseX
                                - toEditorX(
                                TargetHud.getHudX()
                        );

                dragOffsetY =
                        guiMouseY
                                - toEditorY(
                                TargetHud.getHudY()
                        );

                return;
            }

            if (isInsideKnockback(
                    guiMouseX,
                    guiMouseY)) {

                selectedHud = HUD_KNOCKBACK;
                dragging = true;

                resizing =
                        isKnockbackResizeCorner(
                                guiMouseX,
                                guiMouseY
                        );

                if (resizing) {

                    resizeStartScale =
                            getKnockbackVisualScale();

                    resizeStartMouseX =
                            guiMouseX;

                } else {

                    dragOffsetX =
                            guiMouseX
                                    - toEditorX(
                                    KnockbackTracker.getHudX()
                            );

                    dragOffsetY =
                            guiMouseY
                                    - toEditorY(
                                    KnockbackTracker.getHudY()
                            );
                }

                return;
            }

            if (isInsideBhop(
                    guiMouseX,
                    guiMouseY)) {

                selectedHud = HUD_BHOP;
                dragging = true;

                resizing =
                        isBhopResizeCorner(
                                guiMouseX,
                                guiMouseY
                        );

                if (resizing) {

                    resizeStartScale =
                            BhopHud.getVisualScale();

                    resizeStartMouseX =
                            guiMouseX;

                } else {

                    dragOffsetX =
                            guiMouseX
                                    - toEditorX(
                                    BhopHud.getHudX()
                            );

                    dragOffsetY =
                            guiMouseY
                                    - toEditorY(
                                    BhopHud.getHudY()
                            );
                }

                return;
            }
        }

        super.mouseClicked(
                mouseX,
                mouseY,
                mouseButton
        );
    }

    @Override
    protected void mouseClickMove(
            int mouseX,
            int mouseY,
            int clickedMouseButton,
            long timeSinceLastClick) {

        if (!dragging
                || clickedMouseButton != 0) {

            super.mouseClickMove(
                    mouseX,
                    mouseY,
                    clickedMouseButton,
                    timeSinceLastClick
            );

            return;
        }

        int guiMouseX =
                getScaledMouseX(mouseX);

        int guiMouseY =
                getScaledMouseY(mouseY);

        if (selectedHud == HUD_TARGET) {

            if (resizing) {

                int delta =
                        guiMouseX
                                - resizeStartMouseX;

                float newVisualScale =
                        resizeStartScale
                                + (
                                delta / 100.0F
                        );

                if (newVisualScale < 0.50F) {
                    newVisualScale = 0.50F;
                }

                if (newVisualScale > 1.50F) {
                    newVisualScale = 1.50F;
                }

                int newHudScale =
                        Math.round(
                                (
                                        newVisualScale
                                                - 0.50F
                                ) * 100.0F
                        );

                if (newHudScale < 0) {
                    newHudScale = 0;
                }

                if (newHudScale > 100) {
                    newHudScale = 100;
                }

                TargetHud.setHudScale(
                        newHudScale
                );

                return;
            }

            int editorX =
                    guiMouseX - dragOffsetX;

            int editorY =
                    guiMouseY - dragOffsetY;

            int newX =
                    toHudX(editorX);

            int newY =
                    toHudY(editorY);

            float scale =
                    getTargetVisualScale();

            int width =
                    Math.round(
                            TARGET_WIDTH * scale
                    );

            int height =
                    Math.round(
                            TARGET_HEIGHT * scale
                    );

            newX =
                    clampX(
                            newX,
                            width
                    );

            newY =
                    clampY(
                            newY,
                            height
                    );

            TargetHud.setHudPosition(
                    newX,
                    newY
            );

            return;
        }

        if (selectedHud == HUD_KNOCKBACK) {

            if (resizing) {

                int delta =
                        guiMouseX
                                - resizeStartMouseX;

                float newVisualScale =
                        resizeStartScale
                                + (
                                delta / 100.0F
                        );

                if (newVisualScale < 0.50F) {
                    newVisualScale = 0.50F;
                }

                if (newVisualScale > 1.50F) {
                    newVisualScale = 1.50F;
                }

                int newHudScale =
                        Math.round(
                                (
                                        newVisualScale
                                                - 0.50F
                                ) * 100.0F
                        );

                if (newHudScale < 0) {
                    newHudScale = 0;
                }

                if (newHudScale > 100) {
                    newHudScale = 100;
                }

                KnockbackTracker.setHudScale(
                        newHudScale
                );

                return;
            }

            int editorX =
                    guiMouseX - dragOffsetX;

            int editorY =
                    guiMouseY - dragOffsetY;

            int newX =
                    toHudX(editorX);

            int newY =
                    toHudY(editorY);

            float scale =
                    getKnockbackVisualScale();

            int width =
                    Math.round(
                            KNOCKBACK_WIDTH * scale
                    );

            int height =
                    Math.round(
                            KNOCKBACK_HEIGHT * scale
                    );

            newX =
                    clampX(
                            newX,
                            width
                    );

            newY =
                    clampY(
                            newY,
                            height
                    );

            KnockbackTracker.setHudPosition(
                    newX,
                    newY
            );

            return;
        }

        if (selectedHud == HUD_BHOP) {

            if (resizing) {

                int delta =
                        guiMouseX
                                - resizeStartMouseX;

                float newVisualScale =
                        resizeStartScale
                                + (
                                delta / 100.0F
                        );

                if (newVisualScale < 0.50F) {
                    newVisualScale = 0.50F;
                }

                if (newVisualScale > 1.50F) {
                    newVisualScale = 1.50F;
                }

                int newHudScale =
                        Math.round(
                                (
                                        newVisualScale
                                                - 0.50F
                                ) * 100.0F
                        );

                if (newHudScale < 0) {
                    newHudScale = 0;
                }

                if (newHudScale > 100) {
                    newHudScale = 100;
                }

                BhopHud.setHudScale(
                        newHudScale
                );

                return;
            }

            int editorX =
                    guiMouseX - dragOffsetX;

            int editorY =
                    guiMouseY - dragOffsetY;

            int newX =
                    toHudX(editorX);

            int newY =
                    toHudY(editorY);

            float scale =
                    BhopHud.getVisualScale();

            int width =
                    Math.round(
                            BHOP_WIDTH * scale
                    );

            int height =
                    Math.round(
                            BHOP_HEIGHT * scale
                    );

            newX =
                    clampX(
                            newX,
                            width
                    );

            newY =
                    clampY(
                            newY,
                            height
                    );

            BhopHud.setHudPosition(
                    newX,
                    newY
            );
        }
    }

    @Override
    protected void mouseReleased(
            int mouseX,
            int mouseY,
            int state) {

        dragging = false;
        resizing = false;

        super.mouseReleased(
                mouseX,
                mouseY,
                state
        );
    }

    /*
     * =========================
     * TARGET SCALE
     * =========================
     */

    private float getTargetVisualScale() {

        return 0.50F
                + (
                TargetHud.getHudScale()
                        / 100.0F
        );
    }

    /*
     * =========================
     * TARGET HITBOX
     * =========================
     */

    private boolean isInsideTarget(
            int mouseX,
            int mouseY) {

        int editorX =
                toEditorX(
                        TargetHud.getHudX()
                );

        int editorY =
                toEditorY(
                        TargetHud.getHudY()
                );

        float scale =
                getTargetVisualScale();

        int width =
                Math.round(
                        TARGET_WIDTH * scale
                );

        int height =
                Math.round(
                        TARGET_HEIGHT * scale
                );

        return mouseX >= editorX
                && mouseX <= editorX + width
                && mouseY >= editorY
                && mouseY <= editorY + height;
    }

    /*
     * =========================
     * TARGET RESIZE CORNER
     * =========================
     */

    private boolean isTargetResizeCorner(
            int mouseX,
            int mouseY) {

        int editorX =
                toEditorX(
                        TargetHud.getHudX()
                );

        int editorY =
                toEditorY(
                        TargetHud.getHudY()
                );

        float scale =
                getTargetVisualScale();

        int width =
                Math.round(
                        TARGET_WIDTH * scale
                );

        int height =
                Math.round(
                        TARGET_HEIGHT * scale
                );

        int size =
                TARGET_RESIZE_SIZE;

        return mouseX >=
                editorX + width - size
                && mouseX <=
                editorX + width + 4
                && mouseY >=
                editorY + height - size
                && mouseY <=
                editorY + height + 4;
    }

    /*
     * =========================
     * KNOCKBACK SCALE
     * =========================
     */

    private float getKnockbackVisualScale() {

        return 0.50F
                + (
                KnockbackTracker.getHudScale()
                        / 100.0F
        );
    }

    /*
     * =========================
     * KNOCKBACK HITBOX
     * =========================
     */

    private boolean isInsideKnockback(
            int mouseX,
            int mouseY) {

        int editorX =
                toEditorX(
                        KnockbackTracker.getHudX()
                );

        int editorY =
                toEditorY(
                        KnockbackTracker.getHudY()
                );

        float scale =
                getKnockbackVisualScale();

        int width =
                Math.round(
                        KNOCKBACK_WIDTH * scale
                );

        int height =
                Math.round(
                        KNOCKBACK_HEIGHT * scale
                );

        return mouseX >= editorX
                && mouseX <= editorX + width
                && mouseY >= editorY
                && mouseY <= editorY + height;
    }

    /*
     * =========================
     * BHOP HITBOX
     * =========================
     */

    private boolean isInsideBhop(
            int mouseX,
            int mouseY) {

        int editorX =
                toEditorX(
                        BhopHud.getHudX()
                );

        int editorY =
                toEditorY(
                        BhopHud.getHudY()
                );

        float scale =
                BhopHud.getVisualScale();

        int width =
                Math.round(
                        BHOP_WIDTH * scale
                );

        int height =
                Math.round(
                        BHOP_HEIGHT * scale
                );

        return mouseX >= editorX
                && mouseX <= editorX + width
                && mouseY >= editorY
                && mouseY <= editorY + height;
    }

    /*
     * =========================
     * KNOCKBACK RESIZE CORNER
     * =========================
     */

    private boolean isKnockbackResizeCorner(
            int mouseX,
            int mouseY) {

        int editorX =
                toEditorX(
                        KnockbackTracker.getHudX()
                );

        int editorY =
                toEditorY(
                        KnockbackTracker.getHudY()
                );

        float scale =
                getKnockbackVisualScale();

        int width =
                Math.round(
                        KNOCKBACK_WIDTH * scale
                );

        int height =
                Math.round(
                        KNOCKBACK_HEIGHT * scale
                );

        int size =
                12;

        return mouseX >=
                editorX + width - size
                && mouseX <=
                editorX + width + 4
                && mouseY >=
                editorY + height - size
                && mouseY <=
                editorY + height + 4;
    }

    /*
     * =========================
     * BHOP RESIZE CORNER
     * =========================
     */

    private boolean isBhopResizeCorner(
            int mouseX,
            int mouseY) {

        int editorX =
                toEditorX(
                        BhopHud.getHudX()
                );

        int editorY =
                toEditorY(
                        BhopHud.getHudY()
                );

        float scale =
                BhopHud.getVisualScale();

        int width =
                Math.round(
                        BHOP_WIDTH * scale
                );

        int height =
                Math.round(
                        BHOP_HEIGHT * scale
                );

        int size =
                10;

        return mouseX >=
                editorX + width - size
                && mouseX <=
                editorX + width + 4
                && mouseY >=
                editorY + height - size
                && mouseY <=
                editorY + height + 4;
    }

    /*
     * =========================
     * CLAMP X
     * =========================
     */

    @Override
    protected int clampX(
            int x,
            int width) {

        int max =
                this.width - width;

        if (max < 0) {
            max = 0;
        }

        if (x < 0) {
            return 0;
        }

        if (x > max) {
            return max;
        }

        return x;
    }

    /*
     * =========================
     * CLAMP Y
     * =========================
     */

    @Override
    protected int clampY(
            int y,
            int height) {

        int max =
                this.height - height;

        if (max < 0) {
            max = 0;
        }

        if (y < 0) {
            return 0;
        }

        if (y > max) {
            return max;
        }

        return y;
    }

    /*
     * =========================
     * BUTTON ACTIONS
     * =========================
     */

    @Override
    protected void actionPerformed(
            GuiButton button)
            throws java.io.IOException {

        /*
         * BACK
         */

        if (button.id ==
                BUTTON_BACK) {

            restoreRealHuds();

            mc.displayGuiScreen(
                    this.parentScreen
            );

            return;
        }

        /*
         * RESET
         */

        if (button.id ==
                BUTTON_RESET) {

            /*
             * Target HUD.
             */

            TargetHud.resetPosition();

            /*
             * Knockback HUD.
             */

            KnockbackTracker.resetHudPosition();

            /*
             * BHOP HUD.
             */

            BhopHud.resetPosition();

            /*
             * Reload Target HUD zodat
             * de nieuwe positie direct
             * actief is.
             */

            TargetHud.loadHudPosition();

            selectedHud = -1;

            dragging = false;

            resizing = false;

            return;
        }

        super.actionPerformed(
                button
        );
    }

    /*
     * =========================
     * GAME PAUSE
     * =========================
     */

    @Override
    public boolean doesGuiPauseGame() {

        return false;
    }
}