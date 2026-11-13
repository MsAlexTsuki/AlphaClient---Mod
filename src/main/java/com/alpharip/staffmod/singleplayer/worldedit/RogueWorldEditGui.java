// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.singleplayer.worldedit;

import com.alpharip.staffmod.gui.AlphaButton;
import com.alpharip.staffmod.gui.AlphaGuiScreen;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import com.alpharip.staffmod.permissions.PermissionService;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/** Rogue-only WorldEdit controls. The module state is shared with the normal Singleplayer screen. */
public final class RogueWorldEditGui extends AlphaGuiScreen {
    private static final int WORLD_EDIT_TOGGLE = 1;
    private static final int CHEATS_TOGGLE = 2;
    private static final int ACCENT = 0xFFFFD866;

    private final GuiScreen parent;

    public RogueWorldEditGui(GuiScreen parent) {
        this.parent = parent;
    }

    private AlphaModule getWorldEditModule() {
        return AlphaModuleRegistry.find("world-edit");
    }

    private boolean isSingleplayerAvailable() {
        return mc != null && mc.isSingleplayer() && mc.thePlayer != null
                && mc.theWorld != null && mc.getIntegratedServer() != null;
    }

    private boolean hasRogueAccess() {
        return PermissionService.hasRogueOrHigherWorldEditAccess();
    }

    @Override
    public void initGui() {
        buttonList.clear();
        AlphaModule module = getWorldEditModule();
        boolean enabled = module != null && module.isEnabled();
        boolean cheats = WorldEditCheatSettings.areCheatsEnabled();

        buttonList.add(new AlphaButton(WORLD_EDIT_TOGGLE, width / 2 - 90, height / 2 - 28,
                180, 22, "WORLD EDIT: " + (enabled ? "ON" : "OFF"), true, enabled));
        buttonList.add(new AlphaButton(CHEATS_TOGGLE, width / 2 - 90, height / 2 + 2,
                180, 22, "CHEATS: " + (cheats ? "ON" : "OFF"), true, cheats));

        buttonList.get(0).enabled = isSingleplayerAvailable() && module != null && hasRogueAccess();
        buttonList.get(1).enabled = isSingleplayerAvailable() && hasRogueAccess();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == null || !isSingleplayerAvailable() || !hasRogueAccess()) return;

        if (button.id == WORLD_EDIT_TOGGLE) {
            AlphaModule module = getWorldEditModule();
            if (module != null) module.setEnabled(!module.isEnabled());
        } else if (button.id == CHEATS_TOGGLE) {
            WorldEditCheatSettings.setCheatsEnabled(!WorldEditCheatSettings.areCheatsEnabled());
        }
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
        int panelHeight = 142;
        int left = (width - panelWidth) / 2;
        int top = (height - panelHeight) / 2;
        drawRect(left, top, left + panelWidth, top + panelHeight, 0xE00A0C11);
        drawRect(left, top, left + panelWidth, top + 2, ACCENT);
        drawCenteredString(fontRendererObj, "WORLD EDIT", width / 2, top + 20, 0xFFF3F4F6);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
