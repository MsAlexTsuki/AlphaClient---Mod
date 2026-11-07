package com.alpharip.staffmod.singleplayer.privatevault;

import com.alpharip.staffmod.gui.AlphaButton;
import com.alpharip.staffmod.gui.AlphaGuiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatComponentText;

import java.util.ArrayList;
import java.util.List;

/** Browses the non-empty private vaults saved in the active singleplayer world. */
public final class AlphaPrivateVaultsGui extends AlphaGuiScreen {
    private static final int BACK = 90;
    private static final int PREVIOUS = 91;
    private static final int NEXT = 92;
    private static final int FIRST_VAULT_BUTTON = 100;
    private static final int PAGE_SIZE = 7;

    private final GuiScreen parent;
    private List<Integer> vaultNumbers = new ArrayList<Integer>();
    private int page;
    private int panelLeft;
    private int panelTop;
    private int panelWidth;
    private int panelHeight;

    public AlphaPrivateVaultsGui(GuiScreen parent) {
        this.parent = parent;
    }

    private boolean isAvailable() {
        return AlphaPrivateVaultManager.isAvailable(Minecraft.getMinecraft());
    }

    @Override
    public void initGui() {
        buttonList.clear();
        if (!isAvailable()) {
            mc.displayGuiScreen(parent);
            return;
        }

        panelWidth = Math.min(420, width - 24);
        panelHeight = Math.min(310, height - 24);
        panelLeft = (width - panelWidth) / 2;
        panelTop = (height - panelHeight) / 2;

        vaultNumbers = AlphaPrivateVaultManager.getInstance().getSavedVaultNumbers();
        int pageCount = Math.max(1, (vaultNumbers.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        page = Math.max(0, Math.min(page, pageCount - 1));

        int start = page * PAGE_SIZE;
        int end = Math.min(vaultNumbers.size(), start + PAGE_SIZE);
        for (int index = start; index < end; index++) {
            int vaultNumber = vaultNumbers.get(index);
            int row = index - start;
            buttonList.add(new AlphaButton(FIRST_VAULT_BUTTON + row,
                    panelLeft + 18, panelTop + 67 + row * 28,
                    panelWidth - 36, 23, "OPEN  •  Private Vault #" + vaultNumber));
        }

        int footerY = panelTop + panelHeight - 30;
        buttonList.add(new AlphaButton(BACK, panelLeft + 18, footerY, 76, 20, "BACK"));
        if (page > 0) {
            buttonList.add(new AlphaButton(PREVIOUS, panelLeft + panelWidth - 160,
                    footerY, 64, 20, "PREVIOUS"));
        }
        if (page + 1 < pageCount) {
            buttonList.add(new AlphaButton(NEXT, panelLeft + panelWidth - 90,
                    footerY, 72, 20, "NEXT"));
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (!isAvailable()) {
            mc.displayGuiScreen(parent);
            return;
        }

        List<Integer> current = AlphaPrivateVaultManager.getInstance().getSavedVaultNumbers();
        if (!current.equals(vaultNumbers)) {
            vaultNumbers = current;
            initGui();
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == null) return;
        if (!isAvailable()) {
            mc.displayGuiScreen(parent);
            return;
        }

        if (button.id == BACK) {
            mc.displayGuiScreen(parent);
            return;
        }
        if (button.id == PREVIOUS) {
            page = Math.max(0, page - 1);
            initGui();
            return;
        }
        if (button.id == NEXT) {
            page++;
            initGui();
            return;
        }
        if (button.id >= FIRST_VAULT_BUTTON && button.id < FIRST_VAULT_BUTTON + PAGE_SIZE) {
            int index = page * PAGE_SIZE + button.id - FIRST_VAULT_BUTTON;
            if (index < 0 || index >= vaultNumbers.size()) return;

            int vaultNumber = vaultNumbers.get(index);
            try {
                AlphaPrivateVaultInventory inventory =
                        AlphaPrivateVaultManager.getInstance().loadVault(vaultNumber);
                mc.displayGuiScreen(new AlphaPrivateVaultScreen(vaultNumber, inventory, this));
            } catch (Exception failure) {
                mc.thePlayer.addChatMessage(new ChatComponentText(
                        "§c[Alpha Private Vault] Could not open vault " + vaultNumber + ": "
                                + (failure.getMessage() == null ? "unknown storage error" : failure.getMessage())));
                initGui();
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (!isAvailable()) {
            mc.displayGuiScreen(parent);
            return;
        }

        int pageCount = Math.max(1, (vaultNumbers.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        drawRect(0, 0, width, height, 0x99000000);
        drawRect(panelLeft, panelTop, panelLeft + panelWidth, panelTop + panelHeight, 0xF00B0D12);
        drawRect(panelLeft, panelTop, panelLeft + panelWidth, panelTop + 2, 0xFFFFC83D);
        drawCenteredString(fontRendererObj, "ALPHA PRIVATE VAULTS", width / 2,
                panelTop + 14, 0xFFF3F4F6);
        drawCenteredString(fontRendererObj, "SINGLEPLAYER STORAGE", width / 2,
                panelTop + 32, 0xFFFFD866);
        drawString(fontRendererObj, vaultNumbers.size() + " saved vault(s)",
                panelLeft + 18, panelTop + 49, 0xFFB8BDC8);

        if (vaultNumbers.isEmpty()) {
            drawCenteredString(fontRendererObj, "No private vaults saved in this world yet.",
                    width / 2, panelTop + 112, 0xFFB8BDC8);
            drawCenteredString(fontRendererObj, "Use /apv <number> and place an item inside to save one.",
                    width / 2, panelTop + 130, 0xFFB8BDC8);
        } else {
            drawString(fontRendererObj, "Page " + (page + 1) + " / " + pageCount,
                    panelLeft + 18, panelTop + panelHeight - 17, 0xFFB8BDC8);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}