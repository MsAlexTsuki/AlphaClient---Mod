package com.alpharip.staffmod.AutoTool;

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

/** Client command for AutoTool and its local settings. */
public final class AutoToolCommand extends CommandBase {
    @Override
    public String getCommandName() {
        return "autotool";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/autotool [on|off|toggle|revert <on|off>|settings]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        Minecraft mc = Minecraft.getMinecraft();
        if (args == null || args.length == 0 || "settings".equalsIgnoreCase(args[0])) {
            if (mc != null) {
                mc.displayGuiScreen(new AutoToolGui(mc.currentScreen instanceof GuiChat ? null : mc.currentScreen));
            }
            return;
        }

        String action = args[0].toLowerCase(Locale.ROOT);
        if ("on".equals(action) || "off".equals(action) || "toggle".equals(action)) {
            boolean enabled = "toggle".equals(action)
                    ? !AutoToolSettings.isEnabled() : "on".equals(action);
            AutoToolSettings.setEnabled(enabled);
            reply(sender, "AutoTool " + (enabled ? "enabled." : "disabled."));
        } else if ("revert".equals(action)) {
            if (args.length != 2 || (!"on".equalsIgnoreCase(args[1])
                    && !"off".equalsIgnoreCase(args[1]))) {
                reply(sender, "Usage: /autotool revert <on|off>");
                return;
            }
            boolean enabled = "on".equalsIgnoreCase(args[1]);
            AutoToolSettings.setSwapBackEnabled(enabled);
            reply(sender, "Switch back after mining " + (enabled ? "enabled." : "disabled."));
        } else {
            reply(sender, "Usage: " + getCommandUsage(sender));
        }
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        List<String> values = new ArrayList<String>();
        if (args.length == 1) {
            String[] actions = {"on", "off", "toggle", "revert", "settings"};
            String prefix = args[0].toLowerCase(Locale.ROOT);
            for (String value : actions) {
                if (value.startsWith(prefix)) values.add(value);
            }
        } else if (args.length == 2 && "revert".equalsIgnoreCase(args[0])) {
            String prefix = args[1].toLowerCase(Locale.ROOT);
            if ("on".startsWith(prefix)) values.add("on");
            if ("off".startsWith(prefix)) values.add("off");
        }
        return values;
    }

    private static void reply(ICommandSender sender, String message) {
        sender.addChatMessage(new ChatComponentText("§6[AutoTool] §f" + message));
    }
}
