package com.alpharip.staffmod.CommandPalette;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

/** Opens the searchable AlphaClient action palette with /palette. */
public final class CommandPaletteCommand extends CommandBase {
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
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null) mc.displayGuiScreen(new CommandPaletteGui(mc.currentScreen));
    }
}
