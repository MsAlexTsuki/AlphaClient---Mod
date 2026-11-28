package com.alpharip.staffmod.blindcasereview;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

import java.io.IOException;
import java.util.Map;

/** Local review screen. The host application must open it with an existing session. */
public final class BlindCaseReviewGui extends GuiScreen {
    private static final int APPROVE = 1;
    private static final int REJECT = 2;
    private static final int ESCALATE = 3;
    private static final int REVEAL = 4;
    private static final int CLOSE = 5;

    private final GuiScreen parent;
    private final BlindReviewSession session;
    private final String reviewerReference;
    private GuiTextField rationaleField;

    public BlindCaseReviewGui(GuiScreen parent, BlindReviewSession session, String reviewerReference) {
        if (session == null) throw new IllegalArgumentException("session is required");
        if (reviewerReference == null || reviewerReference.trim().isEmpty()) {
            throw new IllegalArgumentException("reviewerReference is required");
        }
        this.parent = parent;
        this.session = session;
        this.reviewerReference = reviewerReference.trim();
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int center = width / 2;
        rationaleField = new GuiTextField(0, fontRendererObj, center - 145, height - 88, 290, 18);
        rationaleField.setMaxStringLength(180);
        rationaleField.setFocused(true);

        buttonList.add(new GuiButton(APPROVE, center - 145, height - 62, 92, 20, "APPROVE"));
        buttonList.add(new GuiButton(REJECT, center - 46, height - 62, 92, 20, "REJECT"));
        buttonList.add(new GuiButton(ESCALATE, center + 53, height - 62, 92, 20, "ESCALATE"));
        buttonList.add(new GuiButton(REVEAL, center - 145, height - 34, 140, 20, "REVEAL CONTEXT"));
        buttonList.add(new GuiButton(CLOSE, center + 5, height - 34, 140, 20, "CLOSE"));
        updateButtonState();
    }

    private void updateButtonState() {
        boolean open = !session.hasDecision();
        for (Object item : buttonList) {
            GuiButton button = (GuiButton) item;
            if (button.id == APPROVE || button.id == REJECT || button.id == ESCALATE) {
                button.enabled = open;
            } else if (button.id == REVEAL) {
                button.enabled = session.hasDecision() && !session.isContextRevealed();
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button.id == CLOSE) {
            mc.displayGuiScreen(parent);
            return;
        }
        if (button.id == REVEAL) {
            if (session.hasDecision()) session.revealRestrictedContext();
            updateButtonState();
            return;
        }
        if (session.hasDecision()) return;

        BlindReviewOutcome outcome = null;
        if (button.id == APPROVE) outcome = BlindReviewOutcome.APPROVE;
        if (button.id == REJECT) outcome = BlindReviewOutcome.REJECT;
        if (button.id == ESCALATE) outcome = BlindReviewOutcome.ESCALATE;
        if (outcome != null) {
            String rationale = rationaleField.getText();
            if (rationale == null || rationale.trim().isEmpty()) return;
            session.submitDecision(new BlindReviewDecision(reviewerReference, outcome, rationale));
            updateButtonState();
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (rationaleField != null && rationaleField.textboxKeyTyped(typedChar, keyCode)) return;
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (rationaleField != null) rationaleField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        BlindCaseView view = session.getReviewerView();
        drawCenteredString(fontRendererObj, "BLIND CASE REVIEW", width / 2, 16, 0xFFFFC83D);
        drawCenteredString(fontRendererObj, "Case " + view.getCaseReference(), width / 2, 32, 0xFFE5E7EB);
        drawString(fontRendererObj, "REVIEW SUMMARY", 24, 54, 0xFFFFC83D);
        drawString(fontRendererObj, trim(view.getReviewSummary(), 92), 24, 68, 0xFFE5E7EB);

        int y = 88;
        drawString(fontRendererObj, "EVIDENCE", 24, y, 0xFFFFC83D);
        y += 14;
        int shown = Math.min(view.getEvidence().size(), Math.max(0, (height - 220) / 25));
        for (int i = 0; i < shown; i++) {
            BlindCaseEvidence evidence = view.getEvidence().get(i);
            drawString(fontRendererObj, trim(evidence.getLabel() + ": " + evidence.getContent(), 100),
                    24, y, 0xFFE5E7EB);
            y += 25;
        }
        if (view.getEvidence().size() > shown) {
            drawString(fontRendererObj, (view.getEvidence().size() - shown) + " more evidence item(s)",
                    24, y, 0xFF9AA3B2);
        }

        if (session.hasDecision()) {
            drawString(fontRendererObj, "DECISION: " +
                    BlindCaseReviewFormatter.formatDecision(session.getDecision()), 24, height - 116, 0xFFB8E986);
        } else {
            drawString(fontRendererObj, "DECISION RATIONALE", 24, height - 108, 0xFFFFC83D);
            rationaleField.drawTextBox();
        }

        if (session.isContextRevealed()) {
            drawString(fontRendererObj, "RESTRICTED CONTEXT", 24, height - 142, 0xFFFFC83D);
            Map<String, String> context = session.getRevealedContext();
            drawString(fontRendererObj, trim(BlindCaseReviewFormatter.formatContext(context), 100),
                    24, height - 130, 0xFFE5E7EB);
        } else {
            drawString(fontRendererObj, "Restricted context stays hidden until a decision is submitted.",
                    24, height - 142, 0xFF9AA3B2);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private String trim(String value, int maxLength) {
        if (value == null) return "";
        return value.length() <= maxLength ? value : value.substring(0, maxLength - 3) + "...";
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
