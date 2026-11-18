package com.alpharip.staffmod.BuildTimelapse.command;

import com.alpharip.staffmod.BuildTimelapse.integration.BuildTimelapseAddon;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

import java.io.IOException;

/** Opens the Build Timelapse workspace bundled with Alpha Recording Studio. */
public final class BuildTimelapseCommand extends CommandBase {
    @Override
    public String getCommandName() {
        return "buildtimelapse";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/buildtimelapse";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.theWorld == null || minecraft.thePlayer == null) {
            sender.addChatMessage(new ChatComponentText(
                    "§6[Build Timelapse] §fJoin a world before opening Build Timelapse."));
            return;
        }
        try {
            BuildTimelapseAddon.openStudio(minecraft);
        } catch (IOException | RuntimeException failure) {
            sender.addChatMessage(new ChatComponentText(
                    "§6[Build Timelapse] §fCould not open: " + failure.getMessage()));
        }
    }
}
