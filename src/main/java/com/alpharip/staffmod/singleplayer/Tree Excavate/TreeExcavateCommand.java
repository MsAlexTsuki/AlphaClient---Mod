package com.alpharip.staffmod.singleplayer.treeexcavate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Opens and controls the Tree Excavate settings in singleplayer. */
public final class TreeExcavateCommand extends CommandBase {
    @Override
    public String getCommandName() { return "treeexcavate"; }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/treeexcavate [settings|on|off|toggle]";
    }

    @Override
    public int getRequiredPermissionLevel() { return 0; }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        Minecraft mc = Minecraft.getMinecraft();
        if (!TreeExcavateAccess.isAvailable(mc)) {
            sender.addChatMessage(new ChatComponentText("§6[Tree Excavate] §fAvailable only in a loaded singleplayer world."));
            return;
        }
        if (args == null || args.length == 0 || "settings".equalsIgnoreCase(args[0])) {
            mc.displayGuiScreen(new TreeExcavateGui(mc.currentScreen instanceof GuiChat ? null : mc.currentScreen));
            return;
        }

        String action = args[0].toLowerCase(Locale.ROOT);
        if ("on".equals(action) || "off".equals(action) || "toggle".equals(action)) {
            boolean enabled = "toggle".equals(action) ? !TreeExcavateSettings.isEnabled() : "on".equals(action);
            TreeExcavateSettings.setEnabled(enabled);
            sender.addChatMessage(new ChatComponentText("§6[Tree Excavate] §f" + (enabled ? "Enabled." : "Disabled.")));
        } else {
            sender.addChatMessage(new ChatComponentText("§6[Tree Excavate] §fUsage: " + getCommandUsage(sender)));
        }
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        List<String> result = new ArrayList<String>();
        if (args.length == 1) {
            for (String value : new String[]{"settings", "on", "off", "toggle"}) {
                if (value.startsWith(args[0].toLowerCase(Locale.ROOT))) result.add(value);
            }
        }
        return result;
    }
}
