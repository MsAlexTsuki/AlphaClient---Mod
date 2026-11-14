package com.alpharip.staffmod.animalbreedingcooldowntimer;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.List;

/** Basic settings and tracked-animal overview for the prototype. */
public final class AnimalBreedingGui extends GuiScreen {
    private final GuiScreen parent;
    private final AnimalBreedingCooldownTimer module;

    public AnimalBreedingGui(GuiScreen parent, AnimalBreedingCooldownTimer module) {
        this.parent = parent;
        this.module = module;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int center = width / 2;
        int top = height / 2 - 70;
        buttonList.add(new GuiButton(0, center - 100, top, 200, 20,
                "Module: " + (module.getSettings().isEnabled() ? "ON" : "OFF")));
        buttonList.add(new GuiButton(1, center - 100, top + 25, 98, 20, "Cooldown - 5s"));
        buttonList.add(new GuiButton(2, center + 2, top + 25, 98, 20, "Cooldown + 5s"));
        buttonList.add(new GuiButton(3, center - 100, top + 50, 200, 20,
                "HUD: " + (module.getSettings().isHudEnabled() ? "ON" : "OFF")));
        buttonList.add(new GuiButton(4, center - 100, top + 76, 200, 20, "Clear tracked animals"));
        buttonList.add(new GuiButton(5, center - 100, top + 102, 200, 20, "Done"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        AnimalBreedingSettings settings = module.getSettings();
        switch (button.id) {
            case 0: settings.setEnabled(!settings.isEnabled()); break;
            case 1: settings.adjustInterval(-5); break;
            case 2: settings.adjustInterval(5); break;
            case 3: settings.setHudEnabled(!settings.isHudEnabled()); break;
            case 4: module.getTracker().clear(); break;
            case 5:
                module.save();
                mc.displayGuiScreen(parent);
                return;
            default: return;
        }
        module.save();
        initGui();
    }

    @Override
    public void onGuiClosed() {
        module.save();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int center = width / 2;
        int top = height / 2 - 94;
        drawCenteredString(fontRendererObj, "Animal Breeding Cooldown Timer",
                center, top, 0xFFFFFF);
        drawCenteredString(fontRendererObj,
                "Configured cooldown: " + module.getSettings().getIntervalSeconds() + " seconds",
                center, top + 13, 0xAAAAAA);
        List<AnimalBreedingEntry> entries = module.getTracker().getAll();
        long now = System.currentTimeMillis();
        int shown = Math.min(5, entries.size());
        for (int i = 0; i < shown; i++) {
            AnimalBreedingEntry entry = entries.get(entries.size() - 1 - i);
            int remaining = entry.getRemainingSeconds(now);
            String value = remaining == 0 ? "Ready" : remaining + "s remaining";
            drawCenteredString(fontRendererObj, entry.getDisplayName() + " - " + value,
                    center, top + 30 + i * 10, 0xFFFFFF);
        }
        if (entries.isEmpty()) {
            drawCenteredString(fontRendererObj, "No animals tracked yet",
                    center, top + 32, 0x777777);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
