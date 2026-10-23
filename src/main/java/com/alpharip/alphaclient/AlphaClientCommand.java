package com.alpharip.alphaclient;

import com.alpharip.staffmod.cps.CpsMod;
import com.alpharip.staffmod.fps.FpsMod;
import com.alpharip.staffmod.gui.AfkTimerSettingsGui;
import com.alpharip.staffmod.gui.BhopSettingsGui;
import com.alpharip.staffmod.gui.CpsSettingsGui;
import com.alpharip.staffmod.gui.FpsSettingsGui;
import com.alpharip.staffmod.gui.ModuleSettingsGui;
import com.alpharip.staffmod.gui.NameViewerSettingsGui;
import com.alpharip.staffmod.gui.PlaytimeTrackerSettingsGui;
import com.alpharip.staffmod.gui.QualityGui;
import com.alpharip.staffmod.gui.SessionTimerSettingsGui;
import com.alpharip.staffmod.gui.WTapSettingsGui;
import com.alpharip.staffmod.hud.AfkTimerHud;
import com.alpharip.staffmod.hud.PlaytimeTrackerHud;
import com.alpharip.staffmod.hud.SessionTimerHud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import com.alpharip.staffmod.movement.BhopMode;
import com.alpharip.staffmod.movement.WTapAssistance;
import com.alpharip.staffmod.permissions.PermissionService;
import com.alpharip.staffmod.profile.ModuleProfileManager;
import com.alpharip.staffmod.pvp.LegacyPvpSettingsGui;
import com.alpharip.staffmod.quality.FullbrightMod;
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
 * Single command implementation shared by the AlphaClient core and its aliases.
 * All actions call the same module APIs and enforce the same module/rank access.
 */
public final class AlphaClientCommand extends CommandBase {
    private final String commandName;

    public AlphaClientCommand(String commandName) {
        this.commandName = commandName;
    }

    @Override public String getCommandName() { return commandName; }
    @Override public String getCommandUsage(ICommandSender sender) { return "/" + commandName + " help"; }
    @Override public int getRequiredPermissionLevel() { return 0; }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        String route = commandName.toLowerCase(Locale.ROOT);

        if ("alphaclient".equals(route)) {
            if (args.length == 0) { sendHelp(sender); return; }
            route = "alphaclient:" + args[0].toLowerCase(Locale.ROOT);
            args = tail(args);
        } else if (isAlias(route)) {
            if (args.length == 0 || "settings".equalsIgnoreCase(args[0])) {
                openSettings(sender, aliasModule(route));
                return;
            }
            if ("nameviewer".equals(route) || "legacypvp".equals(route)) {
                String[] routedArgs = new String[args.length + 1];
                routedArgs[0] = aliasModule(route);
                System.arraycopy(args, 0, routedArgs, 1, args.length);
                args = routedArgs;
                route = "alphaclient:module";
            } else if ("quality".equals(route)) {
                openSettings(sender, "quality");
                return;
            } else {
                route = aliasRoute(route);
            }
        }

        if ("alphaclient:help".equals(route)) {
            sendHelp(sender);
        } else if ("alphaclient:modules".equals(route)) {
            listModules(sender);
        } else if ("alphaclient:settings".equals(route)) {
            if (args.length != 1) {
                reply(sender, "Usage: /alphaclient:settings <module>");
                return;
            }
            openSettings(sender, args[0]);
        } else if ("alphaclient:module".equals(route)) {
            handleModule(sender, args);
        } else if ("alphaclient:fps".equals(route)) {
            handleHud(sender, args, true);
        } else if ("alphaclient:cps".equals(route)) {
            handleHud(sender, args, false);
        } else if ("alphaclient:bhop".equals(route)) {
            handleBhop(sender, args);
        } else if ("alphaclient:wtap".equals(route)) {
            handleWTap(sender, args);
        } else if ("alphaclient:fullbright".equals(route)) {
            handleFullbright(sender, args);
        } else if ("alphaclient:playtime".equals(route)) {
            handleTimer(sender, args, "playtime");
        } else if ("alphaclient:sessiontimer".equals(route)) {
            handleTimer(sender, args, "sessiontimer");
        } else if ("alphaclient:afktimer".equals(route)) {
            handleTimer(sender, args, "afktimer");
        } else {
            sendHelp(sender);
        }
    }

    private boolean isAlias(String name) {
        return "fpshud".equals(name) || "cpshud".equals(name) || "playtime".equals(name)
                || "sessiontimer".equals(name) || "afktimer".equals(name) || "bhop".equals(name)
                || "wtap".equals(name) || "fullbright".equals(name) || "nameviewer".equals(name)
                || "legacypvp".equals(name) || "quality".equals(name);
    }

    private String aliasModule(String alias) {
        if ("fpshud".equals(alias)) return "fps";
        if ("cpshud".equals(alias)) return "cps";
        if ("sessiontimer".equals(alias)) return "sessiontimer";
        if ("afktimer".equals(alias)) return "afktimer";
        if ("bhop".equals(alias)) return "bhop";
        if ("wtap".equals(alias)) return "wtap";
        if ("nameviewer".equals(alias)) return "nameviewer";
        if ("legacypvp".equals(alias)) return "legacypvp";
        if ("quality".equals(alias)) return "quality";
        return alias;
    }

    private String aliasRoute(String alias) {
        if ("fpshud".equals(alias)) return "alphaclient:fps";
        if ("cpshud".equals(alias)) return "alphaclient:cps";
        if ("playtime".equals(alias)) return "alphaclient:playtime";
        if ("sessiontimer".equals(alias)) return "alphaclient:sessiontimer";
        if ("afktimer".equals(alias)) return "alphaclient:afktimer";
        if ("bhop".equals(alias)) return "alphaclient:bhop";
        if ("wtap".equals(alias)) return "alphaclient:wtap";
        if ("fullbright".equals(alias)) return "alphaclient:fullbright";
        return "alphaclient:settings";
    }

    private void handleModule(ICommandSender sender, String[] args) {
        if (args.length != 2) {
            reply(sender, "Usage: /alphaclient module <module> <on|off|toggle>");
            return;
        }
        String rawId = args[0].toLowerCase(Locale.ROOT);
        if ("bhop".equals(rawId) || "staffmods".equals(rawId)) { handleBhop(sender, new String[] { args[1] }); return; }
        if ("wtap".equals(rawId) || "w-tap".equals(rawId) || "w-tap-assistance".equals(rawId)) { handleWTap(sender, new String[] { args[1] }); return; }
        if ("fullbright".equals(rawId)) { handleFullbright(sender, new String[] { args[1] }); return; }
        String id = normalizeModuleId(rawId);
        if ("playtime".equals(id) || "playtime-tracker".equals(id)
                || "sessiontimer".equals(id) || "session-timer".equals(id)
                || "afktimer".equals(id) || "afk-timer".equals(id)) {
            String timer = id.startsWith("playtime") ? "playtime" : (id.startsWith("session") ? "sessiontimer" : "afktimer");
            handleTimer(sender, new String[] { args[1] }, timer);
            return;
        }

        AlphaModule module = AlphaModuleRegistry.find(id);
        if (module == null) {
            reply(sender, "Unknown module: " + args[0] + ". Use /alphaclient modules.");
            return;
        }
        if (!hasModuleAccess(module)) {
            reply(sender, "Access denied: your current rank or module permission does not allow " + module.getName() + ".");
            return;
        }
        if (!module.isImplemented()) {
            reply(sender, module.getName() + " is not implemented yet.");
            return;
        }

        Boolean enabled = parseToggle(args[1], module.isEnabled());
        if (enabled == null) { reply(sender, "Use on, off, or toggle."); return; }
        module.setEnabled(enabled.booleanValue());
        reply(sender, module.getName() + " " + (module.isEnabled() ? "enabled." : "disabled."));
    }

    private boolean hasModuleAccess(AlphaModule module) {
        if (module == null || !module.isUnlocked()) return false;
        if ("staffmods".equals(module.getId())) return hasBhopAccess();
        if ("w-tap-assistance".equals(module.getId())) return hasWTapAccess();
        return true;
    }

    private boolean hasBhopAccess() {
        return PermissionService.hasAccess(PermissionService.Rank.T_MOD)
                && PermissionService.hasModAccess("staff.bhop");
    }

    private boolean hasWTapAccess() {
        return PermissionService.hasAccess(PermissionService.Rank.FOUNDER);
    }

    private void handleBhop(ICommandSender sender, String[] args) {
        if (!hasBhopAccess()) { reply(sender, "Access denied: BHOP requires the correct staff rank and staff.bhop permission."); return; }
        if (args.length == 0 || "settings".equalsIgnoreCase(args[0])) {
            openSettings(sender, "bhop");
            return;
        }
        String action = args[0].toLowerCase(Locale.ROOT);
        if ("on".equals(action) || "off".equals(action) || "toggle".equals(action)) {
            boolean enabled = "toggle".equals(action) ? !BhopMode.isEnabled() : "on".equals(action);
            BhopMode.setEnabled(enabled);
            ModuleProfileManager.saveCurrentModuleStates();
            reply(sender, "BHOP " + (BhopMode.isEnabled() ? "enabled." : "disabled."));
        } else if ("speed".equals(action) && args.length == 2) {
            try {
                int speed = Integer.parseInt(args[1]);
                if (speed < 1 || speed > 20) throw new NumberFormatException();
                BhopMode.setSpeedLevel(speed);
                ModuleProfileManager.saveCurrentModuleStates();
                reply(sender, "BHOP speed set to " + BhopMode.getSpeedLevelName() + ".");
            } catch (NumberFormatException e) {
                reply(sender, "Usage: /bhop speed <1-20>");
            }
        } else if ("shiftup".equals(action) && args.length == 2) {
            Boolean value = parseOnOff(args[1]);
            if (value == null) { reply(sender, "Usage: /bhop shiftup <on|off>"); return; }
            BhopMode.setShiftUpEnabled(value.booleanValue());
            ModuleProfileManager.saveCurrentModuleStates();
            reply(sender, "BHOP Shift-Up " + (value.booleanValue() ? "enabled." : "disabled."));
        } else {
            reply(sender, "Usage: /bhop [settings|on|off|toggle|speed <1-20>|shiftup <on|off>]");
        }
    }

    private void handleWTap(ICommandSender sender, String[] args) {
        if (!hasWTapAccess()) { reply(sender, "Access denied: W-Tap Assistance is Founder-only."); return; }
        if (args.length == 0 || "settings".equalsIgnoreCase(args[0])) { openSettings(sender, "wtap"); return; }
        String action = args[0].toLowerCase(Locale.ROOT);
        if ("on".equals(action) || "off".equals(action) || "toggle".equals(action)) {
            boolean enabled = "toggle".equals(action) ? !WTapAssistance.isEnabled() : "on".equals(action);
            WTapAssistance.setEnabled(enabled);
            reply(sender, "W-Tap Assistance " + (WTapAssistance.isEnabled() ? "enabled." : "disabled."));
        } else if ("delay".equals(action) && args.length == 2) {
            Integer value = parseInteger(args[1], 0, 10);
            if (value == null) { reply(sender, "Usage: /wtap delay <0-10>"); return; }
            WTapAssistance.setDelay(value.intValue());
            reply(sender, "W-Tap delay set to " + WTapAssistance.getDelay() + " ticks.");
        } else if ("cooldown".equals(action) && args.length == 2) {
            Integer value = parseInteger(args[1], 0, 20);
            if (value == null) { reply(sender, "Usage: /wtap cooldown <0-20>"); return; }
            WTapAssistance.setCooldown(value.intValue());
            reply(sender, "W-Tap cooldown set to " + WTapAssistance.getCooldown() + " ticks.");
        } else if ("mode".equals(action) && args.length == 2) {
            String[] modes = { "legit", "standard", "aggressive", "manual" };
            int index = indexOf(modes, args[1].toLowerCase(Locale.ROOT));
            if (index < 0) { reply(sender, "Usage: /wtap mode <legit|standard|aggressive|manual>"); return; }
            WTapAssistance.setMode(index);
            reply(sender, "W-Tap mode set to " + modes[index].toUpperCase(Locale.ROOT) + ".");
        } else if ("trigger".equals(action) && args.length == 2) {
            String value = args[1].toLowerCase(Locale.ROOT);
            if ("hit".equals(value) || "on-hit".equals(value)) WTapAssistance.setTrigger(0);
            else if ("attack".equals(value) || "on-attack".equals(value)) WTapAssistance.setTrigger(1);
            else { reply(sender, "Usage: /wtap trigger <hit|attack>"); return; }
            reply(sender, "W-Tap trigger updated.");
        } else if ("target".equals(action) && args.length == 2) {
            String value = args[1].toLowerCase(Locale.ROOT);
            if ("players".equals(value)) WTapAssistance.setTarget(0);
            else if ("all".equals(value) || "living".equals(value)) WTapAssistance.setTarget(1);
            else { reply(sender, "Usage: /wtap target <players|all>"); return; }
            reply(sender, "W-Tap target updated.");
        } else if ("requirew".equals(action) && args.length == 2) {
            Boolean value = parseOnOff(args[1]);
            if (value == null) { reply(sender, "Usage: /wtap requirew <on|off>"); return; }
            WTapAssistance.setRequireW(value.booleanValue());
            reply(sender, "W-Tap Require W " + (value.booleanValue() ? "enabled." : "disabled."));
        } else {
            reply(sender, "Usage: /wtap [settings|on|off|toggle|delay <0-10>|cooldown <0-20>|mode <legit|standard|aggressive|manual>|trigger <hit|attack>|target <players|all>|requirew <on|off>]");
        }
    }

    private void handleFullbright(ICommandSender sender, String[] args) {
        if (args.length == 0 || "settings".equalsIgnoreCase(args[0])) {
            reply(sender, "Fullbright accepts: /fullbright <on|off|toggle>.");
            return;
        }
        Boolean enabled = parseToggle(args[0], FullbrightMod.isEnabled());
        if (enabled == null) { reply(sender, "Usage: /fullbright <on|off|toggle>"); return; }
        FullbrightMod.setEnabled(enabled.booleanValue());
        reply(sender, "Fullbright " + (FullbrightMod.isEnabled() ? "enabled." : "disabled."));
    }

    private void handleTimer(ICommandSender sender, String[] args, String timer) {
        if (args.length == 0 || "settings".equalsIgnoreCase(args[0])) {
            openSettings(sender, timer);
            return;
        }
        if ("playtime".equals(timer)) {
            if (!"on".equalsIgnoreCase(args[0]) && !"off".equalsIgnoreCase(args[0]) && !"toggle".equalsIgnoreCase(args[0])) {
                reply(sender, "Usage: /playtime <on|off|toggle|settings>");
                return;
            }
            boolean enabled = "toggle".equalsIgnoreCase(args[0]) ? !PlaytimeTrackerHud.isEnabled() : "on".equalsIgnoreCase(args[0]);
            PlaytimeTrackerHud.setEnabled(enabled);
            reply(sender, "Playtime Tracker HUD " + (PlaytimeTrackerHud.isEnabled() ? "enabled." : "disabled."));
        } else if ("sessiontimer".equals(timer)) {
            boolean enabled = parseToggle(args[0], SessionTimerHud.isEnabled()) == null ? SessionTimerHud.isEnabled()
                    : parseToggle(args[0], SessionTimerHud.isEnabled()).booleanValue();
            if (parseToggle(args[0], SessionTimerHud.isEnabled()) == null) { reply(sender, "Usage: /sessiontimer <on|off|toggle|settings>"); return; }
            SessionTimerHud.setEnabled(enabled);
            reply(sender, "Session Timer " + (SessionTimerHud.isEnabled() ? "enabled." : "disabled."));
        } else {
            Boolean value = parseToggle(args[0], AfkTimerHud.isEnabled());
            if (value == null) { reply(sender, "Usage: /afktimer <on|off|toggle|settings>"); return; }
            AfkTimerHud.setEnabled(value.booleanValue());
            reply(sender, "AFK Timer " + (AfkTimerHud.isEnabled() ? "enabled." : "disabled."));
        }
    }

    private void handleHud(ICommandSender sender, String[] args, boolean fps) {
        AlphaModule module = AlphaModuleRegistry.find(fps ? "fps" : "cps");
        if (!hasModuleAccess(module)) { reply(sender, "Access denied: this HUD is not available to your current rank."); return; }
        if (args.length == 0 || "settings".equalsIgnoreCase(args[0])) { openSettings(sender, fps ? "fps" : "cps"); return; }
        String setting = args[0].toLowerCase(Locale.ROOT);
        if ("background".equals(setting) && args.length == 2) {
            Boolean value = parseOnOff(args[1]);
            if (value == null) { reply(sender, "Usage: background <on|off>"); return; }
            if (fps) FpsMod.setShowBackground(value); else CpsMod.setShowBackground(value);
            reply(sender, (fps ? "FPS" : "CPS") + " HUD background " + (value ? "enabled." : "disabled."));
        } else if ("color".equals(setting) && args.length == 2 && "next".equalsIgnoreCase(args[1])) {
            if (fps) FpsMod.cycleColor(); else CpsMod.cycleColor();
            reply(sender, (fps ? "FPS" : "CPS") + " HUD color: " + (fps ? FpsMod.getColorName() : CpsMod.getColorName()) + ".");
        } else if ("position".equals(setting) && args.length == 3) {
            try {
                int x = Integer.parseInt(args[1]), y = Integer.parseInt(args[2]);
                if (x < 0 || y < 0) throw new NumberFormatException();
                if (fps) FpsMod.setHudPosition(x, y); else CpsMod.setHudPosition(x, y);
                reply(sender, (fps ? "FPS" : "CPS") + " HUD position set to " + x + ", " + y + ".");
            } catch (NumberFormatException e) { reply(sender, "Position must be two non-negative numbers: position <x> <y>."); }
        } else if (!fps && "mode".equals(setting) && args.length == 2) {
            String mode = args[1].toLowerCase(Locale.ROOT);
            if ("both".equals(mode)) CpsMod.setDisplayMode(CpsMod.MODE_BOTH);
            else if ("left".equals(mode) || "lmb".equals(mode)) CpsMod.setDisplayMode(CpsMod.MODE_LEFT);
            else if ("right".equals(mode) || "rmb".equals(mode)) CpsMod.setDisplayMode(CpsMod.MODE_RIGHT);
            else { reply(sender, "Usage: /cpshud mode <both|left|right>"); return; }
            reply(sender, "CPS display mode: " + CpsMod.getDisplayModeName() + ".");
        } else {
            reply(sender, fps
                    ? "Usage: /fpshud [settings|background <on|off>|color next|position <x> <y>]"
                    : "Usage: /cpshud [settings|background <on|off>|color next|mode <both|left|right>|position <x> <y>]");
        }
    }

    private void openSettings(ICommandSender sender, String moduleName) {
        String id = normalizeModuleId(moduleName);
        if ("bhop".equals(id) && !hasBhopAccess()) { reply(sender, "Access denied: BHOP requires the correct staff rank and staff.bhop permission."); return; }
        if ("wtap".equals(id) && !hasWTapAccess()) { reply(sender, "Access denied: W-Tap Assistance is Founder-only."); return; }
        AlphaModule module = AlphaModuleRegistry.find(id);
        if (module != null && !hasModuleAccess(module)) { reply(sender, "Access denied: your current rank or module permission does not allow this module."); return; }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null) return;
        GuiScreen parent = mc.currentScreen;
        if ("fps".equals(id)) mc.displayGuiScreen(new FpsSettingsGui(parent));
        else if ("cps".equals(id)) mc.displayGuiScreen(new CpsSettingsGui(parent));
        else if ("playtime".equals(id) || "playtime-tracker".equals(id)) mc.displayGuiScreen(new PlaytimeTrackerSettingsGui(parent));
        else if ("sessiontimer".equals(id) || "session-timer".equals(id)) mc.displayGuiScreen(new SessionTimerSettingsGui(parent));
        else if ("afktimer".equals(id) || "afk-timer".equals(id)) mc.displayGuiScreen(new AfkTimerSettingsGui(parent));
        else if ("bhop".equals(id) || "staffmods".equals(id)) mc.displayGuiScreen(new BhopSettingsGui(parent));
        else if ("wtap".equals(id) || "w-tap-assistance".equals(id)) mc.displayGuiScreen(new WTapSettingsGui(parent));
        else if ("nameviewer".equals(id) || "name-viewer".equals(id)) mc.displayGuiScreen(new NameViewerSettingsGui(parent));
        else if ("legacypvp".equals(id) || "legacy-pvp".equals(id)) mc.displayGuiScreen(new LegacyPvpSettingsGui(parent));
        else if ("quality".equals(id)) mc.displayGuiScreen(new QualityGui(parent));
        else if ("fullbright".equals(id)) reply(sender, "Use /fullbright <on|off|toggle>.");
        else if (module != null) mc.displayGuiScreen(new ModuleSettingsGui(parent, module));
        else reply(sender, "Unknown module/settings: " + moduleName + ". Use /alphaclient modules.");
    }

    private void listModules(ICommandSender sender) {
        reply(sender, "Registered modules (LOCKED modules still require their assigned rank/permission):");
        for (AlphaModule module : AlphaModuleRegistry.getAll()) {
            reply(sender, module.getId() + " - " + (hasModuleAccess(module) ? module.getStatusLabel() : "LOCKED"));
        }
        reply(sender, "Implemented standalone modules: fullbright, playtimeTracker, sessionTimer, afkTimer.");
        reply(sender, "Aliases: /fpshud /cpshud /bhop /wtap /fullbright /nameviewer /legacypvp /playtime /sessiontimer /afktimer /quality.");
        reply(sender, "Planned features remain unavailable until implemented.");
    }

    private void sendHelp(ICommandSender sender) {
        reply(sender, "AlphaClient command core:");
        reply(sender, "/alphaclient help | modules | settings <module>");
        reply(sender, "/alphaclient module <module> <on|off|toggle>");
        reply(sender, "/alphaclient fps|cps <background|color|position|mode> ...");
        reply(sender, "/alphaclient bhop <on|off|toggle|speed <1-20>|shiftup <on|off>|settings>");
        reply(sender, "/alphaclient wtap <on|off|toggle|delay|cooldown|mode|trigger|target|requirew|settings>");
        reply(sender, "/alphaclient fullbright <on|off|toggle>");
        reply(sender, "HUDs: /fpshud /cpshud /playtime /sessiontimer /afktimer.");
        reply(sender, "Other shortcuts: /bhop /wtap /fullbright /nameviewer /legacypvp /quality.");
    }

    private String normalizeModuleId(String id) {
        String value = id == null ? "" : id.toLowerCase(Locale.ROOT);
        if ("fpshud".equals(value)) return "fps";
        if ("cpshud".equals(value)) return "cps";
        if ("playtime".equals(value) || "playtime-tracker".equals(value)) return "playtime";
        if ("session-timer".equals(value) || "sessiontimer".equals(value)) return "sessiontimer";
        if ("afk-timer".equals(value) || "afktimer".equals(value)) return "afktimer";
        if ("bhop".equals(value)) return "staffmods";
        if ("wtap".equals(value) || "w-tap".equals(value)) return "w-tap-assistance";
        if ("nameviewer".equals(value)) return "name-viewer";
        if ("legacypvp".equals(value)) return "legacy-pvp";
        return value;
    }

    private void reply(ICommandSender sender, String message) {
        sender.addChatMessage(new ChatComponentText("§6[AlphaClient] §f" + message));
    }

    private static Boolean parseOnOff(String value) {
        if ("on".equalsIgnoreCase(value)) return Boolean.TRUE;
        if ("off".equalsIgnoreCase(value)) return Boolean.FALSE;
        return null;
    }

    private static Boolean parseToggle(String value, boolean current) {
        if ("on".equalsIgnoreCase(value)) return Boolean.TRUE;
        if ("off".equalsIgnoreCase(value)) return Boolean.FALSE;
        if ("toggle".equalsIgnoreCase(value)) return Boolean.valueOf(!current);
        return null;
    }

    private static Integer parseInteger(String value, int min, int max) {
        try {
            int parsed = Integer.parseInt(value);
            return parsed < min || parsed > max ? null : Integer.valueOf(parsed);
        } catch (NumberFormatException e) { return null; }
    }

    private static int indexOf(String[] values, String value) {
        for (int i = 0; i < values.length; i++) if (values[i].equals(value)) return i;
        return -1;
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
                String[] values = {"help","modules","settings","module","fps","cps","bhop","wtap","fullbright"};
                for (String value : values) options.add(value);
            } else if (args.length == 2 && "module".equalsIgnoreCase(args[0])) {
                for (AlphaModule module : AlphaModuleRegistry.getAll()) options.add(module.getId());
                String[] extra = {"bhop","wtap","fullbright","playtime","sessiontimer","afktimer"};
                for (String value : extra) options.add(value);
            } else if (args.length == 2 && "settings".equalsIgnoreCase(args[0])) {
                String[] values = {"fps","cps","bhop","wtap","fullbright","playtime","sessiontimer","afktimer","nameviewer","legacypvp","quality"};
                for (String value : values) options.add(value);
            }
        } else if ("alphaclient:module".equals(commandName) && args.length == 2) {
            options.add("on"); options.add("off"); options.add("toggle");
        } else if ("alphaclient:settings".equals(commandName) && args.length == 1) {
            String[] values = {"fps","cps","bhop","wtap","fullbright","playtime","sessiontimer","afktimer","nameviewer","legacypvp","quality"};
            for (String value : values) options.add(value);
        }
        return options;
    }
}
