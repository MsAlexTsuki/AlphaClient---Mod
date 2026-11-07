package com.alpharip.staffmod.singleplayer.alphahomes;

import com.alpharip.alphaclient.AlphaClient;
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

/** Singleplayer-only command entry points for AlphaHomes. */
public final class AlphaHomesCommand extends CommandBase {
    private final String commandName;

    public AlphaHomesCommand(String commandName) {
        this.commandName = commandName;
    }

    @Override
    public String getCommandName() { return commandName; }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/alphahome sethome [name] | deletehome <name> | <name> | info | list";
    }

    @Override
    public int getRequiredPermissionLevel() { return 0; }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        Minecraft mc = Minecraft.getMinecraft();
        if (!AlphaHomesManager.isAvailable(mc)) {
            reply(sender, "AlphaHomes is available only in a loaded singleplayer world.");
            return;
        }

        if (args == null || args.length == 0) {
            reply(sender, "Usage: " + getCommandUsage(sender));
            return;
        }

        String action = args[0].toLowerCase(Locale.ROOT);
        AlphaHomesManager manager = AlphaHomesManager.getInstance();

        try {
            if ("sethome".equals(action)) {
                String name = args.length > 1 ? join(args, 1) : "home";
                AlphaHome home = manager.setHome(name);
                reply(sender, "Home '" + home.getName() + "' saved at "
                        + coordinates(home) + " in dimension " + home.getDimension() + ".");
            } else if ("deletehome".equals(action)) {
                if (args.length < 2) {
                    reply(sender, "Usage: /alphahome deletehome <name>");
                    return;
                }
                String name = join(args, 1);
                if (manager.deleteHome(name)) {
                    reply(sender, "Home '" + name + "' was deleted. Linked waypoint cleanup was attempted.");
                } else {
                    reply(sender, "Home not found or could not be saved.");
                }
            } else if ("info".equals(action)) {
                mc.displayGuiScreen(new AlphaHomesGui(mc.currentScreen instanceof GuiChat ? null : mc.currentScreen));
            } else if ("list".equals(action)) {
                List<AlphaHome> homes = manager.getHomes();
                if (homes.isEmpty()) {
                    reply(sender, "No homes have been set in this world.");
                    return;
                }
                reply(sender, "Homes in this world (" + homes.size() + "):");
                for (AlphaHome home : homes) {
                    reply(sender, home.getName() + " — " + coordinates(home)
                            + " (dimension " + home.getDimension() + ")");
                }
            } else {
                String name = join(args, 0);
                AlphaHome home = manager.find(name);
                if (home == null) {
                    reply(sender, "Home not found: " + name + ". Use /alphahome list to see saved homes.");
                    return;
                }
                if (manager.teleportTo(name)) {
                    reply(sender, "Teleported to home '" + home.getName() + "'.");
                } else {
                    reply(sender, "Could not teleport to home '" + home.getName() + "'.");
                }
            }
        } catch (IllegalArgumentException | IllegalStateException failure) {
            reply(sender, failure.getMessage() == null ? "AlphaHomes action failed." : failure.getMessage());
        } catch (Exception failure) {
            reply(sender, "AlphaHomes action failed: "
                    + (failure.getMessage() == null ? "unknown error" : failure.getMessage()));
        }
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        List<String> result = new ArrayList<String>();
        if (!AlphaHomesManager.isAvailable(Minecraft.getMinecraft())) return result;

        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            String[] actions = {"sethome", "deletehome", "info", "list"};
            for (String action : actions) if (action.startsWith(prefix)) result.add(action);
            for (AlphaHome home : AlphaHomesManager.getInstance().getHomes()) {
                if (home.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) result.add(home.getName());
            }
        } else if (args.length >= 2 && "deletehome".equalsIgnoreCase(args[0])) {
            String prefix = args[args.length - 1].toLowerCase(Locale.ROOT);
            for (AlphaHome home : AlphaHomesManager.getInstance().getHomes()) {
                if (home.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) result.add(home.getName());
            }
        }
        return result;
    }

    private static String join(String[] values, int start) {
        StringBuilder result = new StringBuilder();
        for (int i = start; i < values.length; i++) {
            if (i > start) result.append(' ');
            result.append(values[i]);
        }
        String value = result.toString().trim();
        if (value.length() >= 2 && value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"') {
            value = value.substring(1, value.length() - 1);
        }
        return value;
    }

    private static String coordinates(AlphaHome home) {
        return (int) Math.floor(home.getX()) + ", " + (int) Math.floor(home.getY())
                + ", " + (int) Math.floor(home.getZ());
    }

    private static void reply(ICommandSender sender, String message) {
        sender.addChatMessage(new ChatComponentText("§6[AlphaHomes] §f" + message));
    }
}
