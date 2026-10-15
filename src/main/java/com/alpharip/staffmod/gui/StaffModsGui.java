package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.movement.BhopMode;
import com.alpharip.staffmod.movement.BhopProfileManager;
import com.alpharip.staffmod.movement.BhopProfileManager.BhopProfile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.List;

/**
 * Alpha's Staffing Mods
 *
 * Author: MsAlexTsuki
 */
public class StaffModsGui extends ModernGuiScreen {

    private static final int MAX_WIDTH = 570;
    private static final int SIDE_MARGIN = 12;
    private static final int TOP = 88;
    private static final int BOTTOM_OFFSET = 45;
    private static final int PANEL_GAP = 10;
    private static final int LIST_TOP_OFFSET = 48;
    private static final int LIST_PADDING = 8;
    private static final int ROW_HEIGHT = 30;
    private static final int ROW_GAP = 6;
    private static final int MODULE_COLUMNS = 2;
    private static final int MODULE_GAP = 6;
    private static final int SCROLL_AMOUNT = 24;
    private static final int MAX_PROFILES = 10;

    private final GuiScreen parentScreen;

    private int moduleScroll = 0;
    private int profileScroll = 0;

    private int hoveredModule = -1;
    private int hoveredProfile = -1;
    private boolean maxProfilesPopup = false;

    public StaffModsGui(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
        BhopProfileManager.load();
    }

    @Override
    public void initGui() {
        super.initGui();

        moduleScroll = 0;
        profileScroll = 0;
        hoveredModule = -1;
        hoveredProfile = -1;

        BhopProfileManager.load();
    }

    private int[] getLayout() {
        int availableWidth = this.width - (SIDE_MARGIN * 2);
        int totalWidth = Math.min(MAX_WIDTH, availableWidth);

        if (totalWidth < 300) {
            totalWidth = availableWidth;
        }

        int left = (this.width - totalWidth) / 2;
        int bottom = this.height - BOTTOM_OFFSET;
        int panelHeight = bottom - TOP;

        int moduleWidth = (int) (totalWidth * 0.66D);
        int profileWidth = totalWidth - moduleWidth - PANEL_GAP;

        if (profileWidth < 120) {
            profileWidth = 120;
            moduleWidth = totalWidth - profileWidth - PANEL_GAP;
        }

        int moduleLeft = left;
        int profileLeft = moduleLeft + moduleWidth + PANEL_GAP;

        int listTop = TOP + LIST_TOP_OFFSET;

        int actionY = bottom - 38;
        int settingsY = actionY + 4;
        int createDeleteY = actionY - 40;

        int profileListBottom = createDeleteY - ROW_GAP;
        int moduleListBottom = actionY - 12;

        return new int[] {
                left,
                bottom,
                panelHeight,
                moduleWidth,
                profileWidth,
                moduleLeft,
                profileLeft,
                listTop,
                moduleListBottom,
                profileListBottom,
                actionY,
                settingsY,
                createDeleteY
        };
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawModernBackground();

        int scaledMouseX = getScaledMouseX(mouseX);
        int scaledMouseY = getScaledMouseY(mouseY);

        int[] layout = getLayout();

        int moduleLeft = layout[5];
        int profileLeft = layout[6];
        int listTop = layout[7];
        int moduleListBottom = layout[8];
        int profileListBottom = layout[9];
        int actionY = layout[10];
        int settingsY = layout[11];
        int createDeleteY = layout[12];
        int moduleWidth = layout[3];
        int profileWidth = layout[4];

        int profileContentLeft = profileLeft + LIST_PADDING;
        int profileContentWidth = profileWidth - (LIST_PADDING * 2);

        drawModTitle(
                "CLIENT",
                this.width / 2,
                18
        );

        drawCenteredString(
                this.fontRendererObj,
                "Select a mod category",
                this.width / 2,
                67,
                COLOR_GOLD
        );

        drawTransparentPanel(
                moduleLeft,
                TOP,
                moduleWidth,
                layout[2]
        );

        drawTransparentPanel(
                profileLeft,
                TOP,
                profileWidth,
                layout[2]
        );

        drawString(
                this.fontRendererObj,
                "Select a module",
                moduleLeft + 10,
                TOP + 12,
                COLOR_GOLD
        );

        drawString(
                this.fontRendererObj,
                "Select a profile",
                profileLeft + 10,
                TOP + 12,
                COLOR_GOLD
        );

        drawModules(
                moduleLeft,
                moduleWidth,
                listTop,
                moduleListBottom,
                scaledMouseX,
                scaledMouseY
        );

        drawProfiles(
                profileLeft,
                profileWidth,
                listTop,
                profileListBottom,
                scaledMouseX,
                scaledMouseY
        );

        int actionGap = 6;
        int createWidth = (profileContentWidth - actionGap) / 2;
        int deleteWidth = profileContentWidth - createWidth - actionGap;

        drawActionButton(
                profileContentLeft,
                createDeleteY,
                createWidth,
                34,
                "+ CREATE",
                scaledMouseX,
                scaledMouseY
        );

        drawActionButton(
                profileContentLeft + createWidth + actionGap,
                createDeleteY,
                deleteWidth,
                34,
                "DELETE",
                scaledMouseX,
                scaledMouseY
        );

        drawActionButton(
                profileContentLeft,
                settingsY,
                profileContentWidth,
                34,
                "SETTINGS",
                scaledMouseX,
                scaledMouseY
        );

        drawActionButton(
                moduleLeft,
                actionY,
                moduleWidth,
                34,
                "BACK",
                scaledMouseX,
                scaledMouseY
        );

        super.drawScreen(mouseX, mouseY, partialTicks);

        if (maxProfilesPopup) {
            drawMaxProfilesPopup(scaledMouseX, scaledMouseY);
        }
    }

    private void drawMaxProfilesPopup(int mouseX, int mouseY) {
        int popupWidth = 360;
        int popupHeight = 142;

        int popupX = (this.width - popupWidth) / 2;
        int popupY = (this.height - popupHeight) / 2;

        drawRect(
                0,
                0,
                this.width,
                this.height,
                0x99000000
        );

        drawRect(
                popupX,
                popupY,
                popupX + popupWidth,
                popupY + popupHeight,
                0xEE101010
        );

        drawRect(
                popupX,
                popupY,
                popupX + popupWidth,
                popupY + 1,
                0x70E8E8E8
        );

        drawRect(
                popupX,
                popupY + popupHeight - 1,
                popupX + popupWidth,
                popupY + popupHeight,
                0x70E8E8E8
        );

        drawRect(
                popupX,
                popupY,
                popupX + 1,
                popupY + popupHeight,
                0x70E8E8E8
        );

        drawRect(
                popupX + popupWidth - 1,
                popupY,
                popupX + popupWidth,
                popupY + popupHeight,
                0x70E8E8E8
        );

        drawCenteredString(
                this.fontRendererObj,
                "MAX PROFILES",
                this.width / 2,
                popupY + 16,
                COLOR_GOLD
        );

        drawCenteredString(
                this.fontRendererObj,
                "You cant create more profiles,",
                this.width / 2,
                popupY + 46,
                0xFFFFFFFF
        );

        drawCenteredString(
                this.fontRendererObj,
                "please delete a profile to create one.",
                this.width / 2,
                popupY + 62,
                0xFFFFFFFF
        );

        int buttonWidth = 90;
        int buttonHeight = 28;
        int buttonX = this.width / 2 - buttonWidth / 2;
        int buttonY = popupY + 98;

        boolean hovered = isInside(
                mouseX,
                mouseY,
                buttonX,
                buttonY,
                buttonWidth,
                buttonHeight
        );

        int background = hovered
                ? 0x6040A840
                : 0x50308030;

        int outline = 0xFF55FF55;

        drawRect(
                buttonX,
                buttonY,
                buttonX + buttonWidth,
                buttonY + buttonHeight,
                background
        );

        drawRect(
                buttonX,
                buttonY,
                buttonX + buttonWidth,
                buttonY + 1,
                outline
        );

        drawRect(
                buttonX,
                buttonY + buttonHeight - 1,
                buttonX + buttonWidth,
                buttonY + buttonHeight,
                outline
        );

        drawRect(
                buttonX,
                buttonY,
                buttonX + 1,
                buttonY + buttonHeight,
                outline
        );

        drawRect(
                buttonX + buttonWidth - 1,
                buttonY,
                buttonX + buttonWidth,
                buttonY + buttonHeight,
                outline
        );

        drawCenteredString(
                this.fontRendererObj,
                "OK",
                this.width / 2,
                buttonY + 9,
                0xFF55FF55
        );
    }

    private boolean hasMaximumProfiles() {
        List<BhopProfile> profiles = BhopProfileManager.getProfiles();
        return profiles != null && profiles.size() >= MAX_PROFILES;
    }

    private void drawModules(
            int left,
            int width,
            int listTop,
            int listBottom,
            int mouseX,
            int mouseY) {

        hoveredModule = -1;

        int innerLeft = left + LIST_PADDING;
        int innerWidth = width - (LIST_PADDING * 2);
        int columnWidth = (innerWidth - MODULE_GAP) / MODULE_COLUMNS;

        String[] modules = {
                "BHOP",
                "UTILITIES"
        };

        for (int i = 0; i < modules.length; i++) {
            int row = i / MODULE_COLUMNS;
            int column = i % MODULE_COLUMNS;

            int x = innerLeft + column * (columnWidth + MODULE_GAP);
            int y = listTop + row * (ROW_HEIGHT + ROW_GAP) - moduleScroll;

            if (y + ROW_HEIGHT <= listTop || y + ROW_HEIGHT > listBottom) {
                continue;
            }

            boolean hovered = isInside(
                    mouseX,
                    mouseY,
                    x,
                    y,
                    columnWidth,
                    ROW_HEIGHT
            );

            if (hovered) {
                hoveredModule = i;
            }

            String label = modules[i];

            /*
             * Dubble Jump is always displayed simply as "Dubble Jump".
             * Its ON/OFF status is handled inside DubbleJumpGui instead.
             */
            if ("LONGJUMP".equals(label)) {
                label = "Dubble Jump";
            }

            drawListRow(
                    x,
                    y,
                    columnWidth,
                    ROW_HEIGHT,
                    label,
                    hovered
            );
        }
    }

    private void drawProfiles(
            int left,
            int width,
            int listTop,
            int listBottom,
            int mouseX,
            int mouseY) {

        hoveredProfile = -1;

        List<BhopProfile> profiles = BhopProfileManager.getProfiles();

        if (profiles == null) {
            return;
        }

        int innerLeft = left + LIST_PADDING;
        int rowWidth = width - (LIST_PADDING * 2);
        String selectedProfile = BhopProfileManager.getSelectedProfileName();

        for (int i = 0; i < profiles.size(); i++) {
            BhopProfile profile = profiles.get(i);

            if (profile == null) {
                continue;
            }

            String profileName = profile.getProfileName();

            if (profileName == null || profileName.trim().isEmpty()) {
                profileName = "Default";
            }

            int y = listTop + i * (ROW_HEIGHT + ROW_GAP) - profileScroll;

            if (y + ROW_HEIGHT <= listTop || y + ROW_HEIGHT > listBottom) {
                continue;
            }

            boolean selected = selectedProfile != null
                    && selectedProfile.equals(profileName);

            boolean hovered = isInside(
                    mouseX,
                    mouseY,
                    innerLeft,
                    y,
                    rowWidth,
                    ROW_HEIGHT
            );

            if (hovered) {
                hoveredProfile = i;
            }

            drawProfileRow(
                    innerLeft,
                    y,
                    rowWidth,
                    ROW_HEIGHT,
                    profileName,
                    selected,
                    hovered
            );
        }
    }

    private void drawListRow(
            int x,
            int y,
            int width,
            int height,
            String text,
            boolean hovered) {

        int background = hovered ? 0x55202020 : 0x38101010;
        int outline = hovered ? 0x58E8E8E8 : 0x30E8E8E8;

        drawRect(x, y, x + width, y + height, background);
        drawRect(x, y, x + width, y + 1, outline);
        drawRect(x, y + height - 1, x + width, y + height, outline);
        drawRect(x, y, x + 1, y + height, outline);
        drawRect(x + width - 1, y, x + width, y + height, outline);

        drawString(
                this.fontRendererObj,
                text,
                x + 10,
                y + 10,
                0xFFFFFFFF
        );
    }

    private void drawProfileRow(
            int x,
            int y,
            int width,
            int height,
            String profileName,
            boolean selected,
            boolean hovered) {

        int background;

        if (selected) {
            background = hovered ? 0x60302030 : 0x55201820;
        } else {
            background = hovered ? 0x55202020 : 0x38101010;
        }

        int outline = hovered ? 0x58E8E8E8 : 0x30E8E8E8;

        drawRect(x, y, x + width, y + height, background);
        drawRect(x, y, x + width, y + 1, outline);
        drawRect(x, y + height - 1, x + width, y + height, outline);
        drawRect(x, y, x + 1, y + height, outline);
        drawRect(x + width - 1, y, x + width, y + height, outline);

        if (selected) {
            drawRect(x, y, x + 2, y + height, 0xFF55FF55);
        }

        drawString(
                this.fontRendererObj,
                profileName,
                x + 10,
                y + 10,
                0xFFFFFFFF
        );

        if (selected) {
            drawString(
                    this.fontRendererObj,
                    "✓",
                    x + width - 18,
                    y + 9,
                    0xFF55FF55
            );
        }
    }

    private void drawTransparentPanel(
            int x,
            int y,
            int width,
            int height) {

        drawRect(x, y, x + width, y + height, 0x45101010);

        drawRect(x, y, x + width, y + 1, 0x30E8E8E8);
        drawRect(x, y + height - 1, x + width, y + height, 0x30E8E8E8);
        drawRect(x, y, x + 1, y + height, 0x30E8E8E8);
        drawRect(x + width - 1, y, x + width, y + height, 0x30E8E8E8);
    }

    private void drawActionButton(
            int x,
            int y,
            int width,
            int height,
            String text,
            int mouseX,
            int mouseY) {

        boolean hovered = isInside(
                mouseX,
                mouseY,
                x,
                y,
                width,
                height
        );

        int background = hovered ? 0x55202020 : 0x38101010;
        int outline = hovered ? 0x60E8E8E8 : 0x38E8E8E8;

        drawRect(x, y, x + width, y + height, background);
        drawRect(x, y, x + width, y + 1, outline);
        drawRect(x, y + height - 1, x + width, y + height, outline);
        drawRect(x, y, x + 1, y + height, outline);
        drawRect(x + width - 1, y, x + width, y + height, outline);

        drawCenteredString(
                this.fontRendererObj,
                text,
                x + width / 2,
                y + 10,
                0xFFFFFFFF
        );
    }

    @Override
    protected void mouseClicked(
            int mouseX,
            int mouseY,
            int mouseButton) throws IOException {

        int scaledMouseX = getScaledMouseX(mouseX);
        int scaledMouseY = getScaledMouseY(mouseY);

        if (maxProfilesPopup) {
            int buttonWidth = 90;
            int buttonHeight = 28;
            int popupHeight = 142;
            int popupY = (this.height - popupHeight) / 2;
            int buttonX = this.width / 2 - buttonWidth / 2;
            int buttonY = popupY + 98;

            if (isInside(
                    scaledMouseX,
                    scaledMouseY,
                    buttonX,
                    buttonY,
                    buttonWidth,
                    buttonHeight)) {

                maxProfilesPopup = false;
            }

            return;
        }

        int[] layout = getLayout();

        int left = layout[0];
        int moduleWidth = layout[3];
        int profileWidth = layout[4];
        int profileLeft = layout[6];
        int profileContentLeft = profileLeft + LIST_PADDING;
        int profileContentWidth = profileWidth - (LIST_PADDING * 2);
        int listTop = layout[7];
        int moduleListBottom = layout[8];
        int profileListBottom = layout[9];
        int actionY = layout[10];
        int settingsY = layout[11];
        int createDeleteY = layout[12];

        if (isInside(
                scaledMouseX,
                scaledMouseY,
                left,
                actionY,
                moduleWidth,
                34)) {

            Minecraft.getMinecraft().displayGuiScreen(parentScreen);
            return;
        }

        int actionGap = 6;
        int createWidth = (profileContentWidth - actionGap) / 2;
        int deleteWidth = profileContentWidth - createWidth - actionGap;

        if (isInside(
                scaledMouseX,
                scaledMouseY,
                profileContentLeft,
                createDeleteY,
                createWidth,
                34)) {

            if (hasMaximumProfiles()) {
                maxProfilesPopup = true;
                return;
            }

            Minecraft.getMinecraft().displayGuiScreen(
                    new ProfileCreateGui(this)
            );
            return;
        }

        int deleteX = profileContentLeft + createWidth + actionGap;

        if (isInside(
                scaledMouseX,
                scaledMouseY,
                deleteX,
                createDeleteY,
                deleteWidth,
                34)) {

            deleteSelectedProfile();
            return;
        }

        if (isInside(
                scaledMouseX,
                scaledMouseY,
                profileContentLeft,
                settingsY,
                profileContentWidth,
                34)) {

            Minecraft.getMinecraft().displayGuiScreen(
                    new EditGui(this)
            );
            return;
        }

        int innerLeft = left + LIST_PADDING;
        int innerWidth = moduleWidth - (LIST_PADDING * 2);
        int columnWidth = (innerWidth - MODULE_GAP) / MODULE_COLUMNS;

        String[] modules = {
                "BHOP",
                "UTILITIES"
        };

        for (int i = 0; i < modules.length; i++) {
            int row = i / MODULE_COLUMNS;
            int column = i % MODULE_COLUMNS;

            int x = innerLeft + column * (columnWidth + MODULE_GAP);
            int y = listTop + row * (ROW_HEIGHT + ROW_GAP) - moduleScroll;

            if (y + ROW_HEIGHT <= listTop || y >= moduleListBottom) {
                continue;
            }

            if (isInside(
                    scaledMouseX,
                    scaledMouseY,
                    x,
                    y,
                    columnWidth,
                    ROW_HEIGHT)) {

                openModule(modules[i]);
                return;
            }
        }

        List<BhopProfile> profiles = BhopProfileManager.getProfiles();

        if (profiles != null) {
            int profileInnerLeft = profileLeft + LIST_PADDING;
            int profileRowWidth = profileWidth - (LIST_PADDING * 2);

            for (int i = 0; i < profiles.size(); i++) {
                BhopProfile profile = profiles.get(i);

                if (profile == null) {
                    continue;
                }

                String profileName = profile.getProfileName();

                if (profileName == null || profileName.trim().isEmpty()) {
                    profileName = "Default";
                }

                int y = listTop + i * (ROW_HEIGHT + ROW_GAP) - profileScroll;

                if (y + ROW_HEIGHT <= listTop || y >= profileListBottom) {
                    continue;
                }

                if (isInside(
                        scaledMouseX,
                        scaledMouseY,
                        profileInnerLeft,
                        y,
                        profileRowWidth,
                        ROW_HEIGHT)) {

                    BhopProfileManager.selectProfile(profileName);
                    BhopMode.loadSelectedProfile();
                    return;
                }
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private void openModule(String module) {
        if ("BHOP".equals(module)) {
            Minecraft.getMinecraft().displayGuiScreen(new BhopSettingsGui(this));
            return;
        }
        if ("UTILITIES".equals(module)) {
            Minecraft.getMinecraft().displayGuiScreen(new UtilitiesGui(this));
        }
    }

    private void deleteSelectedProfile() {
        String selectedProfile = BhopProfileManager.getSelectedProfileName();

        if (selectedProfile == null || selectedProfile.trim().isEmpty()) {
            return;
        }

        if ("Default".equalsIgnoreCase(selectedProfile)) {
            return;
        }

        BhopProfileManager.deleteProfile(selectedProfile);
        BhopMode.loadSelectedProfile();
        profileScroll = 0;
        clampProfileScroll();
    }

    @Override
    public void handleMouseInput() throws IOException {
        int wheel = Mouse.getEventDWheel();

        if (wheel != 0) {
            int guiMouseX = Mouse.getEventX()
                    * this.width
                    / this.mc.displayWidth;

            int guiMouseY = this.height
                    - Mouse.getEventY()
                    * this.height
                    / this.mc.displayHeight
                    - 1;

            int scaledMouseX = getScaledMouseX(guiMouseX);
            int scaledMouseY = getScaledMouseY(guiMouseY);

            handleScroll(
                    scaledMouseX,
                    scaledMouseY,
                    wheel
            );
        }

        super.handleMouseInput();
    }

    private void handleScroll(
            int mouseX,
            int mouseY,
            int wheel) {

        int[] layout = getLayout();

        int left = layout[5];
        int moduleWidth = layout[3];
        int profileWidth = layout[4];
        int profileLeft = layout[6];
        int profileContentLeft = profileLeft + LIST_PADDING;
        int profileContentWidth = profileWidth - (LIST_PADDING * 2);
        int listTop = layout[7];
        int moduleListBottom = layout[8];
        int profileListBottom = layout[9];

        if (isInside(
                mouseX,
                mouseY,
                left,
                listTop,
                moduleWidth,
                moduleListBottom - listTop)) {

            if (wheel < 0) {
                moduleScroll += SCROLL_AMOUNT;
            } else {
                moduleScroll -= SCROLL_AMOUNT;
            }

            clampModuleScroll();
            return;
        }

        if (isInside(
                mouseX,
                mouseY,
                profileContentLeft,
                listTop,
                profileContentWidth,
                profileListBottom - listTop)) {

            if (wheel < 0) {
                profileScroll += SCROLL_AMOUNT;
            } else {
                profileScroll -= SCROLL_AMOUNT;
            }

            clampProfileScroll();
        }
    }

    private void clampModuleScroll() {
        int moduleCount = 2;

        int listTop = TOP + LIST_TOP_OFFSET;
        int listBottom = getLayout()[8];
        int listHeight = listBottom - listTop;

        int rows = (moduleCount + MODULE_COLUMNS - 1) / MODULE_COLUMNS;

        int contentHeight = rows * ROW_HEIGHT
                + (rows - 1) * ROW_GAP;

        int maxScroll = contentHeight - listHeight + LIST_PADDING;

        if (maxScroll < 0) {
            maxScroll = 0;
        }

        if (moduleScroll < 0) {
            moduleScroll = 0;
        }

        if (moduleScroll > maxScroll) {
            moduleScroll = maxScroll;
        }
    }

    private void clampProfileScroll() {
        List<BhopProfile> profiles = BhopProfileManager.getProfiles();
        int profileCount = profiles == null ? 0 : profiles.size();

        int listTop = TOP + LIST_TOP_OFFSET;
        int listBottom = getLayout()[9];
        int listHeight = listBottom - listTop;

        int contentHeight;

        if (profileCount <= 0) {
            contentHeight = 0;
        } else {
            contentHeight = profileCount * ROW_HEIGHT
                    + (profileCount - 1) * ROW_GAP;
        }

        int maxScroll = contentHeight - listHeight + LIST_PADDING;

        if (maxScroll < 0) {
            maxScroll = 0;
        }

        if (profileScroll < 0) {
            profileScroll = 0;
        }

        if (profileScroll > maxScroll) {
            profileScroll = maxScroll;
        }
    }

    private boolean isInside(
            int mouseX,
            int mouseY,
            int x,
            int y,
            int width,
            int height) {

        return mouseX >= x
                && mouseX <= x + width
                && mouseY >= y
                && mouseY <= y + height;
    }

    @Override
    protected void keyTyped(
            char typedChar,
            int keyCode) throws IOException {

        if (keyCode == 1) {
            Minecraft.getMinecraft().displayGuiScreen(null);
            Minecraft.getMinecraft().setIngameFocus();
            return;
        }

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}