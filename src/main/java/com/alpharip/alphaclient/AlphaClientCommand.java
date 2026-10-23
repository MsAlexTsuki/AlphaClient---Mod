package com.alpharip.alphaclient;

import com.alpharip.staffmod.cps.CpsMod;
import com.alpharip.staffmod.fps.FpsMod;
import com.alpharip.staffmod.gui.AfkTimerSettingsGui;
import com.alpharip.staffmod.gui.CpsSettingsGui;
import com.alpharip.staffmod.gui.FpsSettingsGui;
import com.alpharip.staffmod.gui.PlaytimeTrackerSettingsGui;
import com.alpharip.staffmod.gui.SessionTimerSettingsGui;
import com.alpharip.staffmod.hud.AfkTimerHud;
import com.alpharip.staffmod.hud.PlaytimeTrackerHud;
import com.alpharip.staffmod.hud.SessionTimerHud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Shared client command implementation. The /alphaclient command is the core;
 * namespaced commands and short aliases delegate to the same settings and module APIs.
 */
public final class AlphaClientCommand extends CommandBase {
    private final String commandName;

    public AlphaClientCommand(String commandName) {
        this.commandName = commandName;
    }

    @Override
    public String getCommandName() {
        return commandName;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/" + commandName + " help";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        String route = commandName;
        if ("alphaclient".equals(route)) {
            if (args.length == 0) {
                sendHelp(sender);
                return;
            }
            route = "alphaclient:" + args[0].toLowerCase(Locale.ROOT);
            args = tail(args);
        } else if ("fpshud".equals(route) || "cpshud".equals(route)) {
            if (args.length == 0) {
                openSettings(route.equals("fpshud") ? "fps" : "cps");
                return;
            }
            route = route.equals("fpshud") ? "alphaclient:fps" : "alphaclient:cps";
        } else if ("playtime".equals(route)) {
            openSettings("playtime");
            return;
        } else if ("sessiontimer".equals(route)) {
            openSettings("sessiontimer");
            return;
        } else if ("afktimer".equals(route)) {
            openSettings("afktimer");
            return;
        }

        if ("alphaclient:help".equals(route)) {
            sendHelp(sender);
        } else if ("alphaclient:modules".equals(route)) {
            listModules(sender);
        } else if ("alphaclient:settings".equals(route)) {
            if (args.length != 1) {
                reply(sender, "Usage: /alphaclient:settings <fps|cps|playtime|sessiontimer|afktimer>");
                return;
            }
            openSettings(args[0]);
        } else if ("alphaclient:module".equals(route)) {
            handleModule(sender, args);
        } else if ("alphaclient:fps".equals(route)) {
            handleHud(sender, args, true);
        } else if ("alphaclient:cps".equals(route)) {
            handleHud(sender, args, false);
        } else {
            sendHelp(sender);
        }
    }

    private void handleModule(ICommandSender sender, String[] args) {
        if (args.length != 2) {
            reply(sender, "Usage: /alphaclient:module <module> <on|off|toggle>");
            return;
        }
        AlphaModule module = AlphaModuleRegistry.find(args[0].toLowerCase(Locale.ROOT));
        if (module == null) {
            reply(sender, "Unknown module: " + args[0] + ". Use /alphaclient:modules.");
            return;
        }
        if (!module.isUnlocked()) {
            reply(sender, "Access denied: your current rank cannot use " + module.getName() + ".");
            return;
        }
        if (!module.isImplemented()) {
            reply(sender, module.getName() + " is not implemented yet.");
            return;
        }

        String action = args[1].toLowerCase(Locale.ROOT);
        boolean enabled;
        if ("on".equals(action)) enabled = true;
        else if ("off".equals(action)) enabled = false;
        else if ("toggle".equals(action)) enabled = !module.isEnabled();
        else {
            reply(sender, "Use on, off, or toggle.");
            return;
        }
        module.setEnabled(enabled);
        reply(sender, module.getName() + " " + (module.isEnabled() ? "enabled." : "disabled."));
    }

    private void handleHud(ICommandSender sender, String[] args, boolean fps) {
        AlphaModule module = AlphaModuleRegistry.find(fps ? "fps" : "cps");
        if (module == null || !module.isUnlocked()) {
            reply(sender, "Access denied: this HUD is not available to your current rank.");
            return;
        }
        if (args.length == 0 || "settings".equalsIgnoreCase(args[0])) {
            openSettings(fps ? "fps" : "cps");
            return;
        }
        String setting = args[0].toLowerCase(Locale.ROOT);
        if ("background".equals(setting) && args.length == 2) {
            Boolean value = parseOnOff(args[1]);
            if (value == null) {
                reply(sender, "Usage: background <on|off>");
                return;
            }
            if (fps) FpsMod.setShowBackground(value); else CpsMod.setShowBackground(value);
            reply(sender, (fps ? "FPS" : "CPS") + " HUD background " + (value ? "enabled." : "disabled."));
        } else if ("color".equals(setting) && args.length == 2 && "next".equalsIgnoreCase(args[1])) {
            if (fps) FpsMod.cycleColor(); else CpsMod.cycleColor();
            reply(sender, (fps ? "FPS" : "CPS") + " HUD color: " + (fps ? FpsMod.getColorName() : CpsMod.getColorName()) + ".");
        } else if ("position".equals(setting) && args.length == 3) {
            try {
                int x = Integer.parseInt(args[1]);
                int y = Integer.parseInt(args[2]);
                if (x < 0 || y < 0) throw new NumberFormatException();
                if (fps) FpsMod.setHudPosition(x, y); else CpsMod.setHudPosition(x, y);
                reply(sender, (fps ? "FPS" : "CPS") + " HUD position set to " + x + ", " + y + ".");
            } catch (NumberFormatException e) {
                reply(sender, "Position must use two non-negative numbers: position <x> <y>.");
            }
        } else if (!fps && "mode".equals(setting) && args.length == 2) {
            String mode = args[1].toLowerCase(Locale.ROOT);
            if ("both".equals(mode)) CpsMod.setDisplayMode(CpsMod.MODE_BOTH);
            else if ("left".equals(mode) || "lmb".equals(mode)) CpsMod.setDisplayMode(CpsMod.MODE_LEFT);
            else if ("right".equals(mode) || "rmb".equals(mode)) CpsMod.setDisplayMode(CpsMod.MODE_RIGHT);
            else {
                reply(sender, "Usage: mode <both|left|right>");
                return;
            }
            reply(sender, "CPS display mode: " + CpsMod.getDisplayModeName() + ".");
        } else {
            reply(sender, fps
                    ? "Usage: /fpshud [settings|background <on|off>|color next|position <x> <y>]"
                    : "Usage: /cpshud [settings|background <on|off>|color next|mode <both|left|right>|position <x> <y>]");
        }
    }

    private void openSettings(String module) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null) return;
        GuiScreen parent = mc.currentScreen;
        String id = module.toLowerCase(Locale.ROOT);
        if ("fps".equals(id)) mc.displayGuiScreen(new FpsSettingsGui(parent));
        else if ("cps".equals(id)) mc.displayGuiScreen(new CpsSettingsGui(parent));
        else if ("playtime".equals(id) || "playtime-tracker".equals(id)) mc.displayGuiScreen(new PlaytimeTrackerSettingsGui(parent));
        else if ("sessiontimer".equals(id) || "session-timer".equals(id)) mc.displayGuiScreen(new SessionTimerSettingsGui(parent));
        else if ("afktimer".equals(id) || "afk-timer".equals(id)) mc.displayGuiScreen(new AfkTimerSettingsGui(parent));
        else {
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("§6[AlphaClient] §cNo settings screen exists for " + module + "."));
        }
    }

    private void listModules(ICommandSender sender) {
        reply(sender, "Available modules:");
        for (AlphaModule module : AlphaModuleRegistry.getAll()) {
            reply(sender, module.getId() + " - " + (module.isUnlocked() ? module.getStatusLabel() : "LOCKED"));
        }
        reply(sender, "HUD settings: fps, cps, playtime, sessiontimer, afktimer.");
    }

    private void sendHelp(ICommandSender sender) {
        reply(sender, "AlphaClient command core:");
        reply(sender, "/alphaclient:help | /alphaclient:modules");
        reply(sender, "/alphaclient:settings <fps|cps|playtime|sessiontimer|afktimer>");
        reply(sender, "/alphaclient:module <module> <on|off|toggle>");
        reply(sender, "/alphaclient:fps|cps <background|color|position|mode> ...");
        reply(sender, "Shortcuts: /fpshud, /cpshud, /playtime, /sessiontimer, /afktimer.");
    }

    private void reply(ICommandSender sender, String message) {
        sender.addChatMessage(new ChatComponentText("§6[AlphaClient] §f" + message));
    }

    private static Boolean parseOnOff(String value) {
        if ("on".equalsIgnoreCase(value)) return Boolean.TRUE;
        if ("off".equalsIgnoreCase(value)) return Boolean.FALSE;
        return null;
    }

    private static String[] tail(String[] values) {
        String[] result = new String[Math.max(0, values.length - 1)];
        if (result.length > 0) System.arraycopy(values, 1, result, 0, result.length);
        return result;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args) {
        List<String> options = new ArrayList<String>();
        if ("alphaclient".equals(commandName)) {
            if (args.length == 1) {
                options.add("help"); options.add("modules"); options.add("settings"); options.add("module");
                options.add("fps"); options.add("cps");
            }
        } else if ("alphaclient:module".equals(commandName) && args.length == 2) {
            options.add("on"); options.add("off"); options.add("toggle");
        } else if ("alphaclient:settings".equals(commandName) && args.length == 1) {
            options.add("fps"); options.add("cps"); options.add("playtime"); options.add("sessiontimer"); options.add("afktimer");
        }
        return options;
    }
}
