// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.singleplayer.mobstacker;

import com.alpharip.staffmod.gui.AlphaButton;
import com.alpharip.staffmod.gui.AlphaGuiScreen;
import com.alpharip.staffmod.modules.AlphaModuleFeature;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

/** Settings and status screen for the Scout+ singleplayer Mob Stacker. */
public final class MobStackerGui extends AlphaGuiScreen {
    private static final int BACK = 1;
    private static final int TOGGLE = 2;
    private static final int ACCENT = 0xFFFFC83D;

    private final GuiScreen parent;
    private int left;
    private int top;
    private int panelWidth;
    private int panelHeight;

    public MobStackerGui(GuiScreen parent) {
        this.parent = parent;
    }

    private boolean isAvailable() {
        Minecraft minecraft = Minecraft.getMinecraft();
        AlphaModuleFeature feature = AlphaModuleRegistry.findFeature("mob-stacker");
        return minecraft != null && minecraft.isSingleplayer()
                && minecraft.thePlayer != null && minecraft.theWorld != null
                && minecraft.getIntegratedServer() != null
                && feature != null && feature.isAvailable();
    }

    @Override
    public void initGui() {
        buttonList.clear();
        if (!isAvailable()) {
            if (mc != null) mc.displayGuiScreen(parent);
            return;
        }

        panelWidth = Math.min(420, width - 24);
        panelHeight = 110;
        left = (width - panelWidth) / 2;
        top = (height - panelHeight) / 2;

        AlphaModuleFeature feature = AlphaModuleRegistry.findFeature("mob-stacker");
        boolean enabled = feature != null && feature.isEnabled();

        buttonList.add(new AlphaButton(BACK, left + 16, top + panelHeight - 30,
                90, 20, "BACK"));
        buttonList.add(new AlphaButton(TOGGLE, left + panelWidth - 126,
                top + panelHeight - 30, 110, 20, enabled ? "ON" : "OFF", enabled));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == null) return;
        if (button.id == BACK || !isAvailable()) {
            mc.displayGuiScreen(parent);
        } else if (button.id == TOGGLE) {
            AlphaModuleFeature feature = AlphaModuleRegistry.findFeature("mob-stacker");
            if (feature != null && feature.isAvailable()) {
                feature.setEnabled(!feature.isEnabled());
            }
            initGui();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (!isAvailable()) {
            mc.displayGuiScreen(parent);
            return;
        }

        drawRect(0, 0, width, height, 0x99000000);
        drawRect(left, top, left + panelWidth, top + panelHeight, 0xF00B0D12);
        drawRect(left, top, left + panelWidth, top + 2, ACCENT);

        drawCenteredString(fontRendererObj, "MOB STACKER", width / 2,
                top + 15, 0xFFF2F3F5);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
