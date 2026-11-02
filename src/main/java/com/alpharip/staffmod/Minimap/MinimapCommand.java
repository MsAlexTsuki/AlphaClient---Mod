package com.alpharip.staffmod.Minimap;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** User-facing command for the main AlphaClient minimap. */
public final class MinimapCommand extends CommandBase {
    @Override public String getCommandName() { return "minimap"; }
    @Override public String getCommandUsage(ICommandSender sender) { return "/minimap <on|off|toggle|settings>"; }
    @Override public int getRequiredPermissionLevel() { return 0; }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        AlphaModule module = AlphaModuleRegistry.find("minimap");
        if (module == null || !module.isImplemented()) {
            sender.addChatMessage(new ChatComponentText("§6[AlphaClient] §fMinimap module is unavailable."));
            return;
        }
        if (args.length == 1 && "settings".equalsIgnoreCase(args[0])) {
            Minecraft.getMinecraft().displayGuiScreen(new MinimapSettingsGui(Minecraft.getMinecraft().currentScreen));
            return;
        }
        if (args.length != 1) {
            sender.addChatMessage(new ChatComponentText("§6[AlphaClient] §fUsage: /minimap <on|off|toggle|settings>"));
            return;
        }
        String action = args[0].toLowerCase(Locale.ROOT);
        boolean enabled;
        if ("on".equals(action)) enabled = true;
        else if ("off".equals(action)) enabled = false;
        else if ("toggle".equals(action)) enabled = !module.isConfiguredEnabled();
        else {
            sender.addChatMessage(new ChatComponentText("§6[AlphaClient] §fUsage: /minimap <on|off|toggle|settings>"));
            return;
        }
        module.setEnabled(enabled);
        sender.addChatMessage(new ChatComponentText("§6[AlphaClient] §fMinimap " + (enabled ? "enabled." : "disabled.")));
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, net.minecraft.util.BlockPos pos) {
        List<String> values = new ArrayList<String>();
        if (args.length == 1) {
            for (String option : new String[]{"on", "off", "toggle", "settings"}) {
                if (option.startsWith(args[0].toLowerCase(Locale.ROOT))) values.add(option);
            }
        }
        return values;
    }
}
