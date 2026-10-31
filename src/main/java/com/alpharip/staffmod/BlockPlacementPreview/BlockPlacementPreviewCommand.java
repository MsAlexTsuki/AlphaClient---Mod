package com.alpharip.staffmod.BlockPlacementPreview;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.BlockPos;
import java.util.ArrayList;
import java.util.List;

/** Client command for the block placement preview and its local settings. */
public final class BlockPlacementPreviewCommand extends CommandBase {
    @Override public String getCommandName() { return "blockpreview"; }
    @Override public String getCommandUsage(ICommandSender sender) {
        return "/blockpreview [on|off|toggle|collision <on|off>|opacity <0-100>|settings]";
    }
    @Override public int getRequiredPermissionLevel() { return 0; }

    @Override public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        Minecraft mc = Minecraft.getMinecraft();
        if (args == null || args.length == 0 || "settings".equalsIgnoreCase(args[0])) {
            if (mc != null) mc.displayGuiScreen(new BlockPlacementPreviewGui(
                    mc.currentScreen instanceof GuiChat ? null : mc.currentScreen));
            return;
        }
        String action = args[0].toLowerCase(java.util.Locale.ROOT);
        if ("on".equals(action) || "off".equals(action) || "toggle".equals(action)) {
            boolean enabled = "toggle".equals(action)
                    ? !BlockPlacementPreviewSettings.isEnabled() : "on".equals(action);
            BlockPlacementPreviewSettings.setEnabled(enabled);
            reply(sender, "Block preview " + (enabled ? "enabled." : "disabled."));
        } else if ("collision".equals(action)) {
            if (args.length != 2 || (!"on".equalsIgnoreCase(args[1]) && !"off".equalsIgnoreCase(args[1]))) {
                reply(sender, "Usage: /blockpreview collision <on|off>");
                return;
            }
            boolean enabled = "on".equalsIgnoreCase(args[1]);
            BlockPlacementPreviewSettings.setShowCollision(enabled);
            reply(sender, "Collision colors " + (enabled ? "enabled." : "disabled."));
        } else if ("opacity".equals(action)) {
            if (args.length != 2) { reply(sender, "Usage: /blockpreview opacity <0-100>"); return; }
            try {
                int percent = Integer.parseInt(args[1]);
                if (percent < 0 || percent > 100) { reply(sender, "Opacity must be between 0 and 100."); return; }
                BlockPlacementPreviewSettings.setOpacity(percent / 100.0F);
                reply(sender, "Preview opacity set to " + percent + "%.");
            } catch (NumberFormatException invalid) {
                reply(sender, "Opacity must be a whole number from 0 to 100.");
            }
        } else {
            reply(sender, "Usage: " + getCommandUsage(sender));
        }
    }

    @Override public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        List<String> values = new ArrayList<String>();
        if (args.length == 1) {
            String[] actions = {"on", "off", "toggle", "collision", "opacity", "settings"};
            for (String value : actions)
                if (value.startsWith(args[0].toLowerCase(java.util.Locale.ROOT))) values.add(value);
        } else if (args.length == 2 && "collision".equalsIgnoreCase(args[0])) {
            if ("on".startsWith(args[1].toLowerCase(java.util.Locale.ROOT))) values.add("on");
            if ("off".startsWith(args[1].toLowerCase(java.util.Locale.ROOT))) values.add("off");
        }
        return values;
    }

    private static void reply(ICommandSender sender, String message) {
        sender.addChatMessage(new ChatComponentText("§6[Block Preview] §f" + message));
    }
}
