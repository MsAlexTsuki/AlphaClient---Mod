package com.alpharip.staffmod.Viewer.ItemNbtViewer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/** Standalone viewer for NBT serialized from the item held by the local player. */
public final class ItemNbtViewer {
    public static final String NAME = "Item NBT Viewer";
    public static final String CATEGORY = "Viewer";
    private static final ItemNbtViewer INSTANCE = new ItemNbtViewer();
    private final NbtTreeBuilder treeBuilder = new NbtTreeBuilder();
    private NbtTreeNode root;
    private String serializedNbt = "";
    private String itemDisplayName = "No item held";

    private ItemNbtViewer() { }
    public static ItemNbtViewer getInstance() { return INSTANCE; }

    /** Reads the currently held stack and rebuilds the tree only when its NBT changes. */
    public synchronized void captureHeldItem() {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.thePlayer == null) {
            clear("Player unavailable");
            return;
        }
        ItemStack stack = minecraft.thePlayer.getHeldItem();
        if (stack == null) {
            clear("No item held");
            return;
        }
        NBTTagCompound compound = new NBTTagCompound();
        stack.writeToNBT(compound);
        String currentNbt = compound.toString();
        itemDisplayName = stack.getDisplayName();
        if (root != null && currentNbt.equals(serializedNbt)) return;
        serializedNbt = currentNbt;
        root = treeBuilder.build(compound);
    }

    private void clear(String status) {
        root = null;
        serializedNbt = "";
        itemDisplayName = status;
    }
    public synchronized NbtTreeNode getRoot() { return root; }
    public synchronized String getSerializedNbt() { return serializedNbt; }
    public synchronized String getItemDisplayName() { return itemDisplayName; }
    public ItemNbtViewerGui createGui(GuiScreen parent) {
        return new ItemNbtViewerGui(parent, this);
    }
    /** Opens the standalone screen; callers decide where the entry point belongs. */
    public void openGui(GuiScreen parent) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft != null) minecraft.displayGuiScreen(createGui(parent));
    }
}
