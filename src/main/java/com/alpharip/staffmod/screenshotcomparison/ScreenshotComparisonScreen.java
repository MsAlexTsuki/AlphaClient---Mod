package com.alpharip.staffmod.screenshotcomparison;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;
import java.io.IOException;

/** Local screenshot comparison UI. Open manually through ScreenshotComparison.open(parent). */
public final class ScreenshotComparisonScreen extends GuiScreen {
    private final GuiScreen parent;
    private final ScreenshotComparisonImageLoader loader = new ScreenshotComparisonImageLoader();
    private final ScreenshotComparisonConfig config = new ScreenshotComparisonConfig();
    private final ScreenshotComparisonComposer composer = new ScreenshotComparisonComposer();
    private final ScreenshotComparisonTexture texture = new ScreenshotComparisonTexture();

    private GuiTextField beforePath;
    private GuiTextField afterPath;
    private ScreenshotComparisonImage beforeImage;
    private ScreenshotComparisonImage afterImage;
    private ScreenshotComparisonPair pair;
    private BufferedImage preview;
    private String status = "Enter two local screenshot paths.";
    private String metrics = "";

    public ScreenshotComparisonScreen(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int fieldWidth = Math.max(100, width - 32);
        beforePath = new GuiTextField(10, fontRendererObj, 16, 42, fieldWidth, 18);
        afterPath = new GuiTextField(11, fontRendererObj, 16, 82, fieldWidth, 18);
        beforePath.setMaxStringLength(512);
        afterPath.setMaxStringLength(512);

        int buttonY = 108;
        int buttonWidth = Math.max(72, Math.min(110, (width - 48) / 4));
        int gap = 6;
        int total = buttonWidth * 4 + gap * 3;
        int startX = (width - total) / 2;
        buttonList.add(new GuiButton(1, startX, buttonY, buttonWidth, 20, "Load Before"));
        buttonList.add(new GuiButton(2, startX + buttonWidth + gap, buttonY, buttonWidth, 20, "Load After"));
        buttonList.add(new GuiButton(3, startX + (buttonWidth + gap) * 2, buttonY, buttonWidth, 20, "Compare"));
        buttonList.add(new GuiButton(4, startX + (buttonWidth + gap) * 3, buttonY, buttonWidth, 20,
                config.getMode().getDisplayName()));
        buttonList.add(new GuiButton(5, width - 82, height - 28, 66, 18, "Done"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == null) return;
        if (button.id == 1) {
            loadBefore();
        } else if (button.id == 2) {
            loadAfter();
        } else if (button.id == 3) {
            buildComparison();
        } else if (button.id == 4) {
            config.setMode(config.getMode().next());
            button.displayString = config.getMode().getDisplayName();
            buildComparison();
        } else if (button.id == 5) {
            closeScreen();
        }
    }

    private void loadBefore() {
        try {
            beforeImage = loader.load(beforePath.getText());
            status = "Before screenshot loaded: " + beforeImage.getWidth() + " x " + beforeImage.getHeight();
            refreshPair();
        } catch (IOException exception) {
            status = "Before: " + exception.getMessage();
        }
    }

    private void loadAfter() {
        try {
            afterImage = loader.load(afterPath.getText());
            status = "After screenshot loaded: " + afterImage.getWidth() + " x " + afterImage.getHeight();
            refreshPair();
        } catch (IOException exception) {
            status = "After: " + exception.getMessage();
        }
    }

    private void refreshPair() {
        if (beforeImage == null || afterImage == null) {
            pair = null;
            status += " Load the other screenshot to continue.";
            return;
        }
        pair = new ScreenshotComparisonPair(beforeImage, afterImage);
        buildComparison();
    }

    private void buildComparison() {
        if (pair == null) {
            status = "Load both screenshots before comparing.";
            return;
        }
        try {
            preview = composer.compose(pair, config);
            texture.update(preview);
            ScreenshotDifferenceResult result = composer.analyze(pair, config);
            metrics = String.format(java.util.Locale.ROOT,
                    "Changed pixels: %.2f%%  |  Mean RGB difference: %.1f",
                    result.getChangedPercentage(), result.getMeanChannelDifference());
            status = "Comparison ready · " + config.getMode().getDisplayName();
        } catch (IllegalArgumentException exception) {
            preview = null;
            texture.clear();
            metrics = "";
            status = "Comparison unavailable: " + exception.getMessage();
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (beforePath != null && beforePath.textboxKeyTyped(typedChar, keyCode)) return;
        if (afterPath != null && afterPath.textboxKeyTyped(typedChar, keyCode)) return;
        if (keyCode == 1) {
            closeScreen();
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (beforePath != null) beforePath.mouseClicked(mouseX, mouseY, mouseButton);
        if (afterPath != null) afterPath.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawRect(0, 0, width, 30, 0xE50B0D12);
        drawRect(0, 30, width, 32, 0xFFFFC83D);
        drawCenteredString(fontRendererObj, "SCREENSHOT COMPARISON", width / 2, 11, 0xFFF2F3F5);

        drawString(fontRendererObj, "BEFORE IMAGE PATH", 16, 31, 0xFFB8BDC8);
        drawString(fontRendererObj, "AFTER IMAGE PATH", 16, 71, 0xFFB8BDC8);
        if (beforePath != null) beforePath.drawTextBox();
        if (afterPath != null) afterPath.drawTextBox();

        drawString(fontRendererObj, status, 16, 134, 0xFFE0E3E8);
        if (!metrics.isEmpty()) drawString(fontRendererObj, metrics, 16, 147, 0xFFFFC83D);

        drawPreview();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawPreview() {
        ResourceLocation location = texture.getLocation();
        if (location == null || preview == null) return;
        int availableWidth = Math.max(1, width - 32);
        int availableHeight = Math.max(1, height - 190);
        float scale = Math.min((float) availableWidth / preview.getWidth(),
                (float) availableHeight / preview.getHeight());
        scale = Math.min(1.0F, scale);
        int drawWidth = Math.max(1, (int) (preview.getWidth() * scale));
        int drawHeight = Math.max(1, (int) (preview.getHeight() * scale));
        int x = (width - drawWidth) / 2;
        int y = 166 + Math.max(0, (availableHeight - drawHeight) / 2);
        Minecraft.getMinecraft().getTextureManager().bindTexture(location);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 0.0F,
                drawWidth, drawHeight, preview.getWidth(), preview.getHeight());
    }

    private void closeScreen() {
        texture.clear();
        Minecraft.getMinecraft().displayGuiScreen(parent);
    }

    @Override
    public void onGuiClosed() {
        texture.clear();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
