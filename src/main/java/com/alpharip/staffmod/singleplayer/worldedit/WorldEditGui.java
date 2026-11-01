// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.singleplayer.worldedit;

import com.alpharip.staffmod.gui.AlphaButton;
import com.alpharip.staffmod.gui.AlphaGuiScreen;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/** Singleplayer World Edit module switch. Editing is performed through //acwe commands. */
public final class WorldEditGui extends AlphaGuiScreen {
    private static final int TOGGLE = 1;
    private static final int ACCENT = 0xFFFFD866;

    private final GuiScreen parent;

    public WorldEditGui(GuiScreen parent) {
        this.parent = parent;
    }

    private AlphaModule getWorldEditModule() {
        return AlphaModuleRegistry.find("world-edit");
    }

    private boolean isSingleplayerAvailable() {
        return mc != null && mc.isSingleplayer() && mc.thePlayer != null
                && mc.theWorld != null && mc.getIntegratedServer() != null;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        AlphaModule module = getWorldEditModule();
        boolean enabled = module != null && module.isEnabled();
        buttonList.add(new AlphaButton(TOGGLE, width / 2 - 90, height / 2 - 10,
                180, 22, "WORLD EDIT: " + (enabled ? "ON" : "OFF"), true, enabled));
        buttonList.get(0).enabled = isSingleplayerAvailable() && module != null;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == null || button.id != TOGGLE || !isSingleplayerAvailable()) return;
        AlphaModule module = getWorldEditModule();
        if (module == null) return;
        module.setEnabled(!module.isEnabled());
        initGui();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == org.lwjgl.input.Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(parent);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x22000000);
        int panelWidth = Math.min(360, width - 24);
        int panelHeight = 112;
        int left = (width - panelWidth) / 2;
        int top = (height - panelHeight) / 2;
        drawRect(left, top, left + panelWidth, top + panelHeight, 0xE00A0C11);
        drawRect(left, top, left + panelWidth, top + 2, ACCENT);
        drawCenteredString(fontRendererObj, "WORLD EDIT", width / 2, top + 22, 0xFFF3F4F6);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
