package com.alpharip.staffmod.remembervillagers;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.List;

/** Simple browser for locally remembered villager trades. */
final class RememberVillagerTradesGui extends GuiScreen {
    private final GuiScreen parent;
    private final VillagerTradeRegistry registry;
    private final remembervillagertrade module;
    private int scrollOffset;

    RememberVillagerTradesGui(GuiScreen parent, VillagerTradeRegistry registry,
                               remembervillagertrade module) {
        this.parent = parent;
        this.registry = registry;
        this.module = module;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int center = width / 2;
        buttonList.add(new GuiButton(0, center - 100, height - 28, 95, 20, "Done"));
        buttonList.add(new GuiButton(1, center + 5, height - 28, 95, 20, "Clear History"));
        buttonList.add(new GuiButton(2, width - 28, 8, 20, 20, "^"));
        buttonList.add(new GuiButton(3, width - 28, 32, 20, 20, "v"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "Remember Villagers", width / 2, 12, 0xFFFFFF);

        List<VillagerTradeRecord> records = registry.getAll();
        if (records.isEmpty()) {
            drawCenteredString(fontRendererObj, "No villager trades remembered yet.",
                    width / 2, height / 2 - 4, 0xAAAAAA);
        } else {
            int y = 38;
            int visibleRows = Math.max(1, (height - 78) / 42);
            int end = Math.min(records.size(), scrollOffset + visibleRows);
            for (int i = scrollOffset; i < end; i++) {
                VillagerTradeRecord record = records.get(i);
                drawString(fontRendererObj,
                        record.getVillagerName() + "  [" + record.getX() + ", "
                                + record.getY() + ", " + record.getZ() + "]",
                        16, y, 0xFFD36A);
                y += 12;
                for (VillagerTradeEntry trade : record.getTrades()) {
                    if (y > height - 42) break;
                    drawString(fontRendererObj, trade.getDisplayText(), 24, y, 0xFFFFFF);
                    y += 11;
                }
                y += 8;
            }
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            module.save();
            mc.displayGuiScreen(parent);
        } else if (button.id == 1) {
            registry.clear();
            module.save();
            scrollOffset = 0;
        } else if (button.id == 2) {
            scrollOffset = Math.max(0, scrollOffset - 1);
        } else if (button.id == 3) {
            scrollOffset = Math.max(0, Math.min(registry.size() - 1, scrollOffset + 1));
        }
    }

    @Override
    public void onGuiClosed() {
        module.save();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            module.save();
            mc.displayGuiScreen(parent);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }
}
