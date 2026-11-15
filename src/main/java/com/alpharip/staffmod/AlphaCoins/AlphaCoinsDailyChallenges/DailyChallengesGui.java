package com.alpharip.staffmod.AlphaCoins.AlphaCoinsDailyChallenges;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;
import java.util.List;

/** Minecraft 1.8.9 daily challenge and AlphaCoins screen. */
public final class DailyChallengesGui extends GuiScreen {
    private static final int BACK_BUTTON = 1;
    private static final int CLAIM_BUTTON_BASE = 100;
    private final GuiScreen parent;
    private final DailyChallengeManager manager;
    private String statusMessage = "";

    public DailyChallengesGui(GuiScreen parent, DailyChallengeManager manager) {
        if (manager == null) throw new IllegalArgumentException("Challenge manager is required.");
        this.parent = parent;
        this.manager = manager;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int left = width / 2 - 190;
        int top = Math.max(76, height / 2 - 92);
        List<DailyChallengeProgress> challenges = manager.getChallenges();

        for (int i = 0; i < challenges.size(); i++) {
            DailyChallengeProgress progress = challenges.get(i);
            if (progress.isComplete() && !progress.isClaimed()) {
                buttonList.add(new GuiButton(CLAIM_BUTTON_BASE + i, left + 290,
                        top + i * 43 + 17, 82, 18,
                        "Claim +" + progress.getDefinition().getReward()));
            }
        }
        buttonList.add(new GuiButton(BACK_BUTTON, width / 2 - 45, height - 30, 90, 20, "Done"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == BACK_BUTTON) {
            mc.displayGuiScreen(parent);
            return;
        }
        int index = button.id - CLAIM_BUTTON_BASE;
        List<DailyChallengeProgress> challenges = manager.getChallenges();
        if (index >= 0 && index < challenges.size()) {
            DailyChallengeProgress progress = challenges.get(index);
            if (manager.claim(progress.getDefinition().getId())) {
                statusMessage = "AlphaCoins added to your local balance.";
            } else {
                statusMessage = "This reward has already been claimed or is not ready.";
            }
            initGui();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int center = width / 2;
        int left = center - 190;
        int top = Math.max(76, height / 2 - 92);

        drawCenteredString(fontRendererObj, "Alpha Daily Challenges", center, top - 48, 0xFFFFFF);
        drawCenteredString(fontRendererObj, "AlphaCoins: " + manager.getCoinBalance(),
                center, top - 28, 0xFFD34E);

        List<DailyChallengeProgress> challenges = manager.getChallenges();
        for (int i = 0; i < challenges.size(); i++) {
            DailyChallengeProgress progress = challenges.get(i);
            DailyChallengeDefinition definition = progress.getDefinition();
            int rowY = top + i * 43;
            String state = progress.isClaimed() ? "REWARDED"
                    : progress.isComplete() ? "COMPLETE"
                    : progress.getProgress() + " / " + definition.getTarget();
            drawString(fontRendererObj, definition.getTitle() + "  [" +
                    definition.getDifficulty().getDisplayName() + "]", left, rowY, 0xFFFFFF);
            drawString(fontRendererObj, definition.getDescription(), left, rowY + 12, 0xAAAAAA);
            drawString(fontRendererObj, state + "    Reward: " + definition.getReward() + " AlphaCoins",
                    left, rowY + 25, progress.isClaimed() ? 0x55CC77 : 0xFFD34E);
        }

        if (!statusMessage.isEmpty()) {
            drawCenteredString(fontRendererObj, statusMessage, center, height - 48, 0xAAAAAA);
        }
        if (!manager.wasLastSaveSuccessful()) {
            drawCenteredString(fontRendererObj, "Warning: local challenge data could not be saved.",
                    center, height - 60, 0xFF5555);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
