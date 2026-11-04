package com.alpharip.staffmod.singleplayer.autotool;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.item.Item;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Client command for AutoTool settings, preferences and local item lists. */
public final class AutoToolCommand extends CommandBase {
    @Override
    public String getCommandName() { return "autotool"; }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/autotool [on|off|toggle|settings|profile|prefer <block> <item|clear>|ignore <add|remove> <item>|durability <0-100>|swapback <never|release|broken> [delay]|pausemode <hold|toggle>]";
    }

    @Override
    public int getRequiredPermissionLevel() { return 0; }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (!AutoToolAccess.isAvailable(Minecraft.getMinecraft())) {
            reply(sender, "AutoTool is available only in a loaded singleplayer world.");
            return;
        }
        if (args == null || args.length == 0) {
            openSettings();
            return;
        }
        String action = args[0].toLowerCase(Locale.ROOT);
        if ("settings".equals(action)) {
            openSettings();
        } else if ("on".equals(action) || "off".equals(action) || "toggle".equals(action)) {
            boolean enabled = "toggle".equals(action) ? !AutoToolSettings.isEnabled() : "on".equals(action);
            AutoToolSettings.setEnabled(enabled);
            reply(sender, "AutoTool " + (enabled ? "enabled." : "disabled."));
        } else if ("profile".equals(action)) {
            reply(sender, "Using " + AutoToolSettings.getProfileDescription() + ".");
        } else if ("prefer".equals(action)) {
            setPreference(sender, args);
        } else if ("ignore".equals(action)) {
            setIgnored(sender, args);
        } else if ("durability".equals(action)) {
            setDurability(sender, args);
        } else if ("swapback".equals(action)) {
            setSwapBack(sender, args);
        } else if ("pausemode".equals(action)) {
            setPauseMode(sender, args);
        } else {
            reply(sender, "Usage: " + getCommandUsage(sender));
        }
    }

    private static void openSettings() {
        Minecraft mc = Minecraft.getMinecraft();
        if (AutoToolAccess.isAvailable(mc)) {
            mc.displayGuiScreen(new AutoToolGui(mc.currentScreen instanceof GuiChat ? null : mc.currentScreen));
        }
    }

    private static void setPreference(ICommandSender sender, String[] args) {
        if (args.length != 3) {
            reply(sender, "Usage: /autotool prefer <block-id> <item-id|clear>");
            return;
        }
        ResourceLocation blockLocation = parseId(args[1]);
        if (blockLocation == null || !(Block.blockRegistry.getObject(blockLocation) instanceof Block)) {
            reply(sender, "Unknown block ID: " + args[1]);
            return;
        }
        String blockId = blockLocation.toString();
        if ("clear".equalsIgnoreCase(args[2])) {
            AutoToolSettings.setPreferredItem(blockId, null);
            reply(sender, "Preference cleared for " + blockId + ".");
            return;
        }
        ResourceLocation itemLocation = parseId(args[2]);
        if (itemLocation == null || !(Item.itemRegistry.getObject(itemLocation) instanceof Item)) {
            reply(sender, "Unknown item ID: " + args[2]);
            return;
        }
        AutoToolSettings.setPreferredItem(blockId, itemLocation.toString());
        reply(sender, "Preferred item for " + blockId + " set to " + itemLocation + ".");
    }

    private static void setIgnored(ICommandSender sender, String[] args) {
        if (args.length != 3 || (!"add".equalsIgnoreCase(args[1]) && !"remove".equalsIgnoreCase(args[1]))) {
            reply(sender, "Usage: /autotool ignore <add|remove> <item-id>");
            return;
        }
        ResourceLocation itemLocation = parseId(args[2]);
        if (itemLocation == null || !(Item.itemRegistry.getObject(itemLocation) instanceof Item)) {
            reply(sender, "Unknown item ID: " + args[2]);
            return;
        }
        boolean ignored = "add".equalsIgnoreCase(args[1]);
        AutoToolSettings.setItemIgnored(itemLocation.toString(), ignored);
        reply(sender, (ignored ? "Ignored " : "No longer ignores ") + itemLocation + ".");
    }

    private static void setDurability(ICommandSender sender, String[] args) {
        if (args.length != 2) {
            reply(sender, "Usage: /autotool durability <0-100>");
            return;
        }
        try {
            int percent = Integer.parseInt(args[1]);
            if (percent < 0 || percent > 100) throw new NumberFormatException();
            AutoToolSettings.setMinimumDurability(percent);
            reply(sender, "Minimum remaining durability set to " + percent + "%.");
        } catch (NumberFormatException invalid) {
            reply(sender, "Durability must be a whole number from 0 to 100.");
        }
    }

    private static void setSwapBack(ICommandSender sender, String[] args) {
        if (args.length < 2 || args.length > 3) {
            reply(sender, "Usage: /autotool swapback <never|release|broken> [delayTicks]");
            return;
        }
        String mode = args[1].toUpperCase(Locale.ROOT);
        if (!"NEVER".equals(mode) && !"RELEASE".equals(mode) && !"BROKEN".equals(mode)) {
            reply(sender, "Choose never, release, or broken.");
            return;
        }
        int delay = -1;
        if (args.length == 3) {
            try {
                delay = Integer.parseInt(args[2]);
                if (delay < 0 || delay > 100) throw new NumberFormatException();
            } catch (NumberFormatException invalid) {
                reply(sender, "Delay must be a whole number from 0 to 100 ticks.");
                return;
            }
        }
        AutoToolSettings.setSwapBackMode(mode);
        if (delay >= 0) AutoToolSettings.setSwapBackDelay(delay);
        reply(sender, "Switch-back mode: " + mode.toLowerCase(Locale.ROOT) + ".");
    }

    private static void setPauseMode(ICommandSender sender, String[] args) {
        if (args.length != 2 || (!"hold".equalsIgnoreCase(args[1]) && !"toggle".equalsIgnoreCase(args[1]))) {
            reply(sender, "Usage: /autotool pausemode <hold|toggle>");
            return;
        }
        boolean toggle = "toggle".equalsIgnoreCase(args[1]);
        AutoToolSettings.setPauseToggleMode(toggle);
        reply(sender, "Pause key mode set to " + (toggle ? "toggle" : "hold") + ".");
    }

    private static ResourceLocation parseId(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try { return new ResourceLocation(value.trim().toLowerCase(Locale.ROOT)); }
        catch (RuntimeException invalid) { return null; }
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        List<String> matches = new ArrayList<String>();
        if (args.length == 1) {
            String[] actions = {"on", "off", "toggle", "settings", "profile", "prefer",
                    "ignore", "durability", "swapback", "pausemode"};
            addMatches(matches, actions, args[0]);
        } else if (args.length == 2 && "ignore".equalsIgnoreCase(args[0])) {
            addMatches(matches, new String[]{"add", "remove"}, args[1]);
        } else if (args.length == 2 && "swapback".equalsIgnoreCase(args[0])) {
            addMatches(matches, new String[]{"never", "release", "broken"}, args[1]);
        } else if (args.length == 2 && "pausemode".equalsIgnoreCase(args[0])) {
            addMatches(matches, new String[]{"hold", "toggle"}, args[1]);
        } else if (args.length == 3 && "prefer".equalsIgnoreCase(args[0])
                && "clear".startsWith(args[2].toLowerCase(Locale.ROOT))) {
            matches.add("clear");
        }
        return matches;
    }

    private static void addMatches(List<String> result, String[] candidates, String prefix) {
        String normalized = prefix.toLowerCase(Locale.ROOT);
        for (String candidate : candidates) if (candidate.startsWith(normalized)) result.add(candidate);
    }

    private static void reply(ICommandSender sender, String message) {
        sender.addChatMessage(new ChatComponentText("§6[AutoTool] §f" + message));
    }
}
