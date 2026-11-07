package com.alpharip.staffmod.singleplayer.privatevault;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.BlockPos;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** Opens numbered, world-local Alpha Private Vault inventories in singleplayer. */
public final class AlphaPrivateVaultCommand extends CommandBase {
    @Override
    public String getCommandName() {
        return "apv";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/apv <number>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(final ICommandSender sender, String[] args) throws CommandException {
        final Minecraft mc = Minecraft.getMinecraft();
        if (!AlphaPrivateVaultManager.isAvailable(mc)) {
            reply(sender, "Alpha Private Vault is available only in a loaded singleplayer world.");
            return;
        }

        if (args == null || args.length != 1) {
            reply(sender, "Usage: /apv <number>");
            return;
        }

        final int vaultNumber;
        try {
            vaultNumber = Integer.parseInt(args[0]);
        } catch (NumberFormatException invalidNumber) {
            reply(sender, "Enter a positive whole number for the vault.");
            return;
        }

        if (vaultNumber < 1) {
            reply(sender, "Vault numbers must be 1 or higher.");
            return;
        }

        try {
            final AlphaPrivateVaultInventory inventory =
                    AlphaPrivateVaultManager.getInstance().loadVault(vaultNumber);
            final IntegratedServer server = mc.getIntegratedServer();
            final UUID playerId = mc.thePlayer.getUniqueID();

            // Open the inventory through the integrated server so vanilla
            // ContainerChest handles clicks and synchronizes both inventories.
            server.addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    EntityPlayerMP serverPlayer =
                            server.getConfigurationManager().getPlayerByUUID(playerId);
                    if (serverPlayer == null) {
                        return;
                    }
                    serverPlayer.displayGUIChest(inventory);
                }
            });
        } catch (Exception failure) {
            reply(sender, "Could not open vault " + vaultNumber + ": "
                    + (failure.getMessage() == null ? "unknown storage error" : failure.getMessage()));
        }
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return Collections.emptyList();
    }

    private static void reply(ICommandSender sender, String message) {
        sender.addChatMessage(new ChatComponentText("§6[Alpha Private Vault] §f" + message));
    }
}
