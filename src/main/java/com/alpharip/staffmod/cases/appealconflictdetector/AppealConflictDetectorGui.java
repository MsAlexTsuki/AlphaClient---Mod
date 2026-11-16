package com.alpharip.staffmod.appealconflictdetector;

import java.io.IOException;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

/** Read-only overview. The host application must open this screen explicitly. */
public final class AppealConflictDetectorGui extends GuiScreen {
    private final AppealConflictService service;
    private List<AppealConflictReport> reports;

    public AppealConflictDetectorGui(AppealConflictService service) {
        if (service == null) throw new IllegalArgumentException("service is required");
        this.service = service;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(0, this.width / 2 - 50, this.height - 30, 100, 20, "Close"));
        refresh();
    }


    private void refresh() {
        reports = service.evaluateAll();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) this.mc.displayGuiScreen(null);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, "Appeal Conflict Detector", this.width / 2, 18, 0xFFFFFF);
        this.drawCenteredString(this.fontRendererObj,
                "Configured policy only - findings require human review",
                this.width / 2, 34, 0xAAAAAA);

        int y = 58;
        if (reports == null || reports.isEmpty()) {
            this.drawCenteredString(this.fontRendererObj, "No appeal cases supplied", this.width / 2, y, 0xCCCCCC);
        } else {
            int visible = Math.min(reports.size(), Math.max(1, (this.height - 105) / 34));
            for (int i = 0; i < visible; i++) {
                AppealConflictReport report = reports.get(i);
                int color = report.hasConflicts() ? 0xFF7777 : 0xDDDDDD;
                this.drawString(this.fontRendererObj,
                        AppealConflictFormatter.format(report).replace("\n", " | "),
                        12, y, color);
                y += 28;
            }
            if (reports.size() > visible) {
                this.drawString(this.fontRendererObj,
                        (reports.size() - visible) + " additional case(s) not shown",
                        12, y, 0xAAAAAA);
            }
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
