package com.alpharip.staffmod.singleplayer.privatevault;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.io.File;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stores only non-empty vaults inside the active singleplayer world's save folder.
 * Empty vault numbers never create a file or reserve storage.
 */
public final class AlphaPrivateVaultManager {
    private static final AlphaPrivateVaultManager INSTANCE = new AlphaPrivateVaultManager();
    private static final int SLOT_COUNT = 54;

    private AlphaPrivateVaultManager() { }

    public static AlphaPrivateVaultManager getInstance() {
        return INSTANCE;
    }

    public static boolean isAvailable(Minecraft mc) {
        return mc != null && mc.isSingleplayer() && mc.getIntegratedServer() != null
                && mc.thePlayer != null && mc.theWorld != null;
    }

    /**
     * Returns only vault numbers with a saved data file in the active world.
     * Unopened and empty vault numbers are intentionally absent.
     */
    public List<Integer> getSavedVaultNumbers() {
        Minecraft mc = Minecraft.getMinecraft();
        if (!isAvailable(mc)) {
            return Collections.emptyList();
        }

        try {
            File directory = getStorageDirectory(mc);
            File[] files = directory.listFiles();
            List<Integer> vaultNumbers = new ArrayList<Integer>();
            if (files == null) {
                return vaultNumbers;
            }

            for (File file : files) {
                if (!file.isFile()) {
                    continue;
                }
                String name = file.getName();
                if (!name.startsWith("vault-") || !name.endsWith(".dat")) {
                    continue;
                }

                String numberText = name.substring(6, name.length() - 4);
                try {
                    int number = Integer.parseInt(numberText);
                    if (number > 0) {
                        vaultNumbers.add(number);
                    }
                } catch (NumberFormatException ignored) {
                    // Ignore unrelated or malformed filenames.
                }
            }
            Collections.sort(vaultNumbers);
            return vaultNumbers;
        } catch (IOException failure) {
            System.err.println("[AlphaClient] Could not list Alpha Private Vaults: "
                    + failure.getMessage());
            return Collections.emptyList();
        }
    }

    public AlphaPrivateVaultInventory loadVault(int vaultNumber) {
        validateVaultNumber(vaultNumber);
        Minecraft mc = Minecraft.getMinecraft();
        if (!isAvailable(mc)) {
            throw new IllegalStateException("A loaded singleplayer world is required.");
        }

        File file;
        try {
            file = getVaultFile(mc, vaultNumber);
        } catch (IOException failure) {
            throw new IllegalStateException("Could not locate this world's private vault storage: "
                    + failure.getMessage(), failure);
        }
        AlphaPrivateVaultInventory inventory = new AlphaPrivateVaultInventory(vaultNumber);
        if (!file.isFile()) {
            inventory.enablePersistence();
            return inventory;
        }

        try {
            NBTTagCompound root = CompressedStreamTools.read(file);
            if (root == null || !root.hasKey("Items", 9)) {
                throw new IOException("The vault file has an invalid format.");
            }

            NBTTagList items = root.getTagList("Items", 10);
            for (int index = 0; index < items.tagCount(); index++) {
                NBTTagCompound entry = items.getCompoundTagAt(index);
                int slot = entry.getByte("Slot") & 255;
                if (slot >= 0 && slot < SLOT_COUNT) {
                    ItemStack stack = ItemStack.loadItemStackFromNBT(entry);
                    if (stack != null) {
                        inventory.setInventorySlotContents(slot, stack);
                    }
                }
            }
            inventory.enablePersistence();
            return inventory;
        } catch (Exception failure) {
            throw new IllegalStateException("Could not read the existing vault file safely: "
                    + failure.getMessage(), failure);
        }
    }

    public boolean saveVault(int vaultNumber, AlphaPrivateVaultInventory inventory) {
        validateVaultNumber(vaultNumber);
        if (inventory == null || inventory.getVaultNumber() != vaultNumber) {
            return false;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (!isAvailable(mc)) {
            return false;
        }

        File file;
        try {
            file = getVaultFile(mc, vaultNumber);
            boolean hasItems = false;
            for (int slot = 0; slot < SLOT_COUNT; slot++) {
                if (inventory.getStackInSlot(slot) != null) {
                    hasItems = true;
                    break;
                }
            }

            if (!hasItems) {
                Files.deleteIfExists(file.toPath());
                File directory = file.getParentFile();
                if (directory != null && directory.isDirectory()) {
                    String[] remaining = directory.list();
                    if (remaining != null && remaining.length == 0) {
                        Files.deleteIfExists(directory.toPath());
                    }
                }
                return true;
            }

            File directory = file.getParentFile();
            if (directory == null) {
                return false;
            }
            Files.createDirectories(directory.toPath());

            NBTTagCompound root = new NBTTagCompound();
            root.setInteger("VaultNumber", vaultNumber);
            NBTTagList items = new NBTTagList();
            for (int slot = 0; slot < SLOT_COUNT; slot++) {
                ItemStack stack = inventory.getStackInSlot(slot);
                if (stack == null) {
                    continue;
                }
                NBTTagCompound entry = new NBTTagCompound();
                entry.setByte("Slot", (byte) slot);
                stack.writeToNBT(entry);
                items.appendTag(entry);
            }
            root.setTag("Items", items);

            File temporary = new File(directory, file.getName() + ".tmp");
            try {
                CompressedStreamTools.write(root, temporary);
                try {
                    Files.move(temporary.toPath(), file.toPath(),
                            StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                } catch (AtomicMoveNotSupportedException unsupported) {
                    Files.move(temporary.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            } finally {
                Files.deleteIfExists(temporary.toPath());
            }
            return true;
        } catch (Exception failure) {
            System.err.println("[AlphaClient] Could not save Alpha Private Vault " + vaultNumber
                    + ": " + failure.getMessage());
            return false;
        }
    }

    private static File getVaultFile(Minecraft mc, int vaultNumber) throws IOException {
        File storageDirectory = getStorageDirectory(mc);
        return new File(storageDirectory, "vault-" + vaultNumber + ".dat");
    }

    private static File getStorageDirectory(Minecraft mc) throws IOException {
        String folderName = mc.getIntegratedServer().getFolderName();
        if (folderName == null || folderName.trim().isEmpty()) {
            throw new IOException("The active singleplayer world folder is unavailable.");
        }

        File savesDirectory = new File(mc.mcDataDir, "saves");
        File worldDirectory = new File(savesDirectory, folderName);
        String savesRoot = savesDirectory.getCanonicalPath() + File.separator;
        String worldPath = worldDirectory.getCanonicalPath();
        if (!worldPath.startsWith(savesRoot)) {
            throw new IOException("The active world path is invalid.");
        }

        return new File(new File(worldDirectory, "alphaclient"), "privatevaults");
    }

    private static void validateVaultNumber(int vaultNumber) {
        if (vaultNumber < 1) {
            throw new IllegalArgumentException("Vault numbers must be 1 or higher.");
        }
    }
}
