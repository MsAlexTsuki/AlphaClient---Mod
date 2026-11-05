package com.alpharip.staffmod.Palette;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

/** Opens the AlphaClient Command Palette with /palette. */
public final class PaletteCommand extends CommandBase {
    @Override
    public String getCommandName() {
        return "palette";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/palette";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null) {
            return;
        }

        if (minecraft.currentScreen instanceof GuiChat) {
            minecraft.displayGuiScreen(new PaletteGui(null));
        } else {
            minecraft.displayGuiScreen(new PaletteGui(minecraft.currentScreen));
        }
    }
}
