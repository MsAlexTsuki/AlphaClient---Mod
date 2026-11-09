package com.alpharip.staffmod.singleplayer.privatevault;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ChatComponentText;

import java.io.IOException;

/** Client-local chest screen shared by the vault browser. */
public final class AlphaPrivateVaultScreen extends GuiChest {
    private final int vaultNumber;
    private final AlphaPrivateVaultInventory vaultInventory;
    private final GuiScreen parent;

    public AlphaPrivateVaultScreen(int vaultNumber, AlphaPrivateVaultInventory inventory, GuiScreen parent) {
        super(Minecraft.getMinecraft().thePlayer.inventory, inventory);
        this.vaultNumber = vaultNumber;
        this.vaultInventory = inventory;
        this.parent = parent;
    }

    @Override
    protected void handleMouseClick(Slot slot, int slotId, int clickedButton, int clickType) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!AlphaPrivateVaultManager.isAvailable(mc)) {
            mc.displayGuiScreen(null);
            return;
        }

        if (slot != null) {
            slotId = slot.slotNumber;
        }
        this.inventorySlots.slotClick(slotId, clickedButton, clickType, mc.thePlayer);
        // Inventory mutations are autosaved by the debounced markDirty path.
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1 && parent != null) {
            mc.displayGuiScreen(parent);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void onGuiClosed() {
        Minecraft mc = Minecraft.getMinecraft();
        if (AlphaPrivateVaultManager.isAvailable(mc)
                && !AlphaPrivateVaultManager.getInstance().flushVaultNow(vaultInventory)) {
            mc.thePlayer.addChatMessage(new ChatComponentText(
                    "§c[Alpha Private Vault] Could not save vault " + vaultNumber + "."));
        }
        super.onGuiClosed();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
