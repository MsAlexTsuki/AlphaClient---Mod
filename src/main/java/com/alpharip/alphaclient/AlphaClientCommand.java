package com.alpharip.alphaclient;

import com.alpharip.staffmod.cps.CpsMod;
import com.alpharip.staffmod.fps.FpsMod;
import com.alpharip.staffmod.gui.AfkTimerSettingsGui;
import com.alpharip.staffmod.gui.AlphaModMenu;
import com.alpharip.staffmod.gui.BhopSettingsGui;
import com.alpharip.staffmod.gui.CpsSettingsGui;
import com.alpharip.staffmod.gui.FpsSettingsGui;
import com.alpharip.staffmod.gui.ModuleSettingsGui;
import com.alpharip.staffmod.gui.NameViewerSettingsGui;
import com.alpharip.staffmod.gui.PlaytimeTrackerSettingsGui;
import com.alpharip.staffmod.gui.QualityGui;
import com.alpharip.staffmod.gui.SessionTimerSettingsGui;
import com.alpharip.staffmod.gui.StaffModsMenuGui;
import com.alpharip.staffmod.gui.WTapSettingsGui;
import com.alpharip.staffmod.hud.AfkTimerHud;
import com.alpharip.staffmod.hud.PlaytimeTrackerHud;
import com.alpharip.staffmod.hud.SessionTimerHud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import com.alpharip.staffmod.movement.BhopMode;
import com.alpharip.staffmod.movement.WTapAssistance;
import com.alpharip.staffmod.permissions.PermissionService;
import com.alpharip.staffmod.permissions.SupabaseCommunityService;
import com.alpharip.staffmod.profile.ModuleProfileManager;
import com.alpharip.staffmod.pvp.LegacyPvpSettingsGui;
import com.alpharip.staffmod.pvp.LegacyPvpSettings;
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

        if ("alphahelp".equals(route)) {
            sendHelp(sender);
            return;
        }

        if ("staffmods".equals(route)) {
            if (args.length == 0) {
                openStaffModsMenu(sender);
                return;
            }
            if (args.length == 1 && !"help".equalsIgnoreCase(args[0])) {
                openStaffFolder(sender, args[0]);
                return;
            }
            reply(sender, "Usage: /staffmods [general|beta|tmod|mod|srmod|admin|sradmin|management|platformadmin|manager|owner|founder|donator|scout|rogue|elite|alpha|alphaplus]");
            return;
        }

        if ("general".equals(route)) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc != null) AlphaClient.openGuiFromCommand(new AlphaModMenu(mc.currentScreen));
            return;
        }

        String staffFolder = staffFolderAlias(route);
        if (staffFolder != null) {
            if (args.length != 0) {
                reply(sender, "Usage: /" + route);
                return;
            }
            openStaffFolder(sender, staffFolder);
            return;
        }

        if ("alphaclient".equals(route)) {
            if (args.length == 0) { sendHelp(sender); return; }
            route = "alphaclient:" + args[0].toLowerCase(Locale.ROOT);
            args = tail(args);
        } else if (isAlias(route)) {
            if (args.length == 0 || "settings".equalsIgnoreCase(args[0])) {
                openSettings(sender, aliasModule(route));
                return;
            }
            if ("nameviewer".equals(route)) {
                String[] routedArgs = new String[args.length + 1];
                routedArgs[0] = aliasModule(route);
                System.arraycopy(args, 0, routedArgs, 1, args.length);
                args = routedArgs;
                route = "alphaclient:module";
            } else if ("legacypvp".equals(route)) {
                route = "alphaclient:legacypvp";
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
        } else if ("alphaclient:legacypvp".equals(route)) {
            handleLegacyPvp(sender, args);
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

    private void openStaffModsMenu(ICommandSender sender) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null && PermissionService.hasStaffModsAccess()) {
            mc.displayGuiScreen(new StaffModsMenuGui(mc.currentScreen));
        } else {
            reply(sender, "Access denied: StaffMods requires staff access.");
        }
    }

    private void openStaffFolder(ICommandSender sender, String folder) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null) return;
        if (!PermissionService.hasStaffModsAccess()) {
            reply(sender, "Access denied: StaffMods requires staff access.");
            return;
        }
        StaffModsMenuGui menu = new StaffModsMenuGui(mc.currentScreen);
        if (!menu.openFolderCommand(folder)) {
            reply(sender, "Access denied: your current rank or module permissions do not allow the StaffMods folder '" + folder + "'.");
            return;
        }
        mc.displayGuiScreen(menu);
    }

    private String staffFolderAlias(String name) {
        if ("staffgeneral".equals(name)) return "general";
        if ("beta".equals(name)) return "beta";
        if ("stafftmod".equals(name)) return "tmod";
        if ("staffmod".equals(name)) return "mod";
        if ("staffsrmod".equals(name)) return "srmod";
        if ("staffadmin".equals(name)) return "admin";
        if ("staffsradmin".equals(name)) return "sradmin";
        if ("staffmanagement".equals(name)) return "management";
        if ("staffplatformadmin".equals(name)) return "platformadmin";
        if ("staffmanager".equals(name)) return "manager";
        if ("staffowner".equals(name)) return "owner";
        if ("stafffounder".equals(name)) return "founder";
        if ("donator".equals(name)) return "donator";
        if ("scout".equals(name)) return "scout";
        if ("rogue".equals(name)) return "rogue";
        if ("elite".equals(name)) return "elite";
        if ("alpha".equals(name)) return "alpha";
        if ("alphaplus".equals(name)) return "alphaplus";
        return null;
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
        if ("staffmods".equals(rawId)) {
            reply(sender, "StaffMods is a folder, not a module. Use /staffmods to open it.");
            return;
        }
        if ("bhop".equals(rawId)) { handleBhop(sender, new String[] { args[1] }); return; }
        if ("wtap".equals(rawId) || "w-tap".equals(rawId) || "w-tap-assistance".equals(rawId)) { handleWTap(sender, new String[] { args[1] }); return; }
        if ("fullbright".equals(rawId)) { handleFullbright(sender, new String[] { args[1] }); return; }
        if ("quality".equals(rawId)) {
            reply(sender, "Quality is a feature group, not a single toggle. Use /quality to open its settings.");
            return;
        }
        String id = normalizeModuleId(rawId);
        if ("playtime".equals(id) || "playtime-tracker".equals(id) || "playtimetracker".equals(id)
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
        // Use the authenticated community profile's explicit Founder flag as well as the rank gate.
        return PermissionService.hasAccess(PermissionService.Rank.FOUNDER)
                || SupabaseCommunityService.isSignedInFounder();
    }

    private void handleBhop(ICommandSender sender, String[] args) {
        if (!hasBhopAccess()) { reply(sender, "Access denied: BHOP requires the correct staff rank and staff.bhop permission."); return; }
        if (args.length == 0 || "settings".equalsIgnoreCase(args[0])) {
            openSettings(sender, "bhop");
            return;
        }
        String action = args[0].toLowerCase(Locale.ROOT);
        if (args.length == 1 && ("on".equals(action) || "off".equals(action) || "toggle".equals(action))) {
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
        } else if ("name".equals(action) && args.length >= 2) {
            StringBuilder name = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                if (i > 1) name.append(' ');
                name.append(args[i]);
            }
            BhopMode.setBhopName(name.toString());
            ModuleProfileManager.saveCurrentModuleStates();
            reply(sender, "BHOP name updated.");
        } else if ("color".equals(action) && args.length == 2) {
            if (!args[1].matches("#[0-9a-fA-F]{6}")) {
                reply(sender, "Usage: /bhop color <#RRGGBB>");
                return;
            }
            BhopMode.setColor(args[1].toUpperCase(Locale.ROOT));
            ModuleProfileManager.saveCurrentModuleStates();
            reply(sender, "BHOP color updated.");
        } else {
            reply(sender, "Usage: /bhop [settings|on|off|toggle|speed <1-20>|shiftup <on|off>|name <text>|color <#RRGGBB>]");
        }
    }

    private void handleWTap(ICommandSender sender, String[] args) {
        if (!hasWTapAccess()) { reply(sender, "Access denied: W-Tap Assistance is Founder-only."); return; }
        if (args.length > 0 && "help".equalsIgnoreCase(args[0])) { sendWTapHelp(sender); return; }
        if (args.length == 0 || "settings".equalsIgnoreCase(args[0])) { openSettings(sender, "wtap"); return; }
        String action = args[0].toLowerCase(Locale.ROOT);
        if (args.length == 1 && ("on".equals(action) || "off".equals(action) || "toggle".equals(action))) {
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
            sendWTapHelp(sender);
        }
    }

    private void sendWTapHelp(ICommandSender sender) {
        reply(sender, "§6§lW-TAP ASSISTANCE §8| §7Command Guide");
        reply(sender, "§7Quick actions");
        reply(sender, "§f/wtap on §8- §aEnable W-Tap");
        reply(sender, "§f/wtap off §8- §cDisable W-Tap");
        reply(sender, "§f/wtap toggle §8- §7Switch enabled state");
        reply(sender, "§f/wtap settings §8- §7Open the settings menu");
        reply(sender, "§7Timing");
        reply(sender, "§f/wtap delay <0-10> §8- §7Set delay in ticks");
        reply(sender, "§f/wtap cooldown <0-20> §8- §7Set cooldown in ticks");
        reply(sender, "§7Behaviour");
        reply(sender, "§f/wtap mode <legit|standard|aggressive|manual>");
        reply(sender, "§f/wtap trigger <hit|attack> §8- §7Choose activation trigger");
        reply(sender, "§f/wtap target <players|all> §8- §7Choose valid targets");
        reply(sender, "§f/wtap requirew <on|off> §8- §7Require the W key");
        reply(sender, "§8Tip: use §f/wtap help §8to show this guide again.");
    }

    private void handleLegacyPvp(ICommandSender sender, String[] args) {
        AlphaModule module = AlphaModuleRegistry.find("legacy-pvp");
        if (!hasModuleAccess(module)) { reply(sender, "Access denied: Legacy PvP is not available to your current rank."); return; }
        if (args.length == 0 || "settings".equalsIgnoreCase(args[0])) { openSettings(sender, "legacypvp"); return; }
        if (args.length == 1 && ("on".equalsIgnoreCase(args[0]) || "off".equalsIgnoreCase(args[0]) || "toggle".equalsIgnoreCase(args[0]))) {
            Boolean enabled = parseToggle(args[0], module.isEnabled());
            module.setEnabled(enabled.booleanValue());
            reply(sender, "Legacy PvP " + (module.isEnabled() ? "enabled." : "disabled."));
            return;
        }
        if (args.length != 2) {
            reply(sender, "Usage: /legacypvp [on|off|toggle] or /legacypvp <blockhit|potion-punch|missed-swing-delay|sword-swing|bow-animation|rod-animation|food-drink-animation|disable-view-bobbing> <on|off>");
            return;
        }
        Boolean value = parseOnOff(args[1]);
        if (value == null) { reply(sender, "Use on or off for a Legacy PvP setting."); return; }
        String feature = args[0].toLowerCase(Locale.ROOT);
        if ("blockhit".equals(feature)) LegacyPvpSettings.setBlockhitEnabled(value.booleanValue());
        else if ("potion-punch".equals(feature) || "potionpunch".equals(feature)) LegacyPvpSettings.setPotionPunchAnimationEnabled(value.booleanValue());
        else if ("missed-swing-delay".equals(feature) || "missedswingdelay".equals(feature)) LegacyPvpSettings.setRemoveMissedSwingDelayEnabled(value.booleanValue());
        else if ("sword-swing".equals(feature) || "swordswing".equals(feature)) LegacyPvpSettings.setLegacySwordSwingEnabled(value.booleanValue());
        else if ("bow-animation".equals(feature) || "bowanimation".equals(feature)) LegacyPvpSettings.setLegacyBowAnimationEnabled(value.booleanValue());
        else if ("rod-animation".equals(feature) || "rodanimation".equals(feature)) LegacyPvpSettings.setLegacyRodAnimationEnabled(value.booleanValue());
        else if ("food-drink-animation".equals(feature) || "fooddrinkanimation".equals(feature)) LegacyPvpSettings.setFoodDrinkAnimationEnabled(value.booleanValue());
        else if ("disable-view-bobbing".equals(feature) || "disableviewbobbing".equals(feature)) LegacyPvpSettings.setDisableViewBobbing(value.booleanValue());
        else { reply(sender, "Unknown or non-configurable Legacy PvP feature: " + feature + "."); return; }
        reply(sender, "Legacy PvP setting " + feature + " " + (value.booleanValue() ? "enabled." : "disabled."));
    }

    private void handleFullbright(ICommandSender sender, String[] args) {
        if (args.length == 0 || "settings".equalsIgnoreCase(args[0])) {
            reply(sender, "Fullbright accepts: /fullbright <on|off|toggle>.");
            return;
        }
        if (args.length != 1) { reply(sender, "Usage: /fullbright <on|off|toggle>"); return; }
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
        String option = args[0].toLowerCase(Locale.ROOT);
        if ("background".equals(option) && args.length == 2) {
            Boolean value = parseOnOff(args[1]);
            if (value == null) { reply(sender, "Usage: background <on|off>"); return; }
            if ("playtime".equals(timer)) PlaytimeTrackerHud.setShowBackground(value.booleanValue());
            else if ("sessiontimer".equals(timer)) SessionTimerHud.setShowBackground(value.booleanValue());
            else AfkTimerHud.setShowBackground(value.booleanValue());
            reply(sender, timer + " HUD background " + (value.booleanValue() ? "enabled." : "disabled."));
            return;
        }
        if ("color".equals(option) && args.length == 2 && "next".equalsIgnoreCase(args[1])) {
            if ("playtime".equals(timer)) PlaytimeTrackerHud.cycleColor();
            else if ("sessiontimer".equals(timer)) SessionTimerHud.cycleColor();
            else AfkTimerHud.cycleColor();
            reply(sender, timer + " HUD color advanced.");
            return;
        }
        if ("position".equals(option) && args.length == 3) {
            Integer x = parseInteger(args[1], 0, 100000);
            Integer y = parseInteger(args[2], 0, 100000);
            if (x == null || y == null) { reply(sender, "Usage: position <x> <y> with non-negative coordinates."); return; }
            if ("playtime".equals(timer)) PlaytimeTrackerHud.setHudPosition(x.intValue(), y.intValue());
            else if ("sessiontimer".equals(timer)) SessionTimerHud.setHudPosition(x.intValue(), y.intValue());
            else AfkTimerHud.setHudPosition(x.intValue(), y.intValue());
            reply(sender, timer + " HUD position set to " + x + ", " + y + ".");
            return;
        }
        if ("afktimer".equals(timer) && "mode".equals(option) && args.length == 2) {
            String mode = args[1].toLowerCase(Locale.ROOT);
            if ("always".equals(mode)) AfkTimerHud.setDisplayMode(AfkTimerHud.MODE_ALWAYS_VISIBLE);
            else if ("afk".equals(mode)) AfkTimerHud.setDisplayMode(AfkTimerHud.MODE_ONLY_WHILE_AFK);
            else { reply(sender, "Usage: /afktimer mode <always|afk>"); return; }
            reply(sender, "AFK Timer display mode: " + AfkTimerHud.getDisplayModeName() + ".");
            return;
        }
        if (args.length != 1) {
            reply(sender, "Use one action or one setting at a time.");
            return;
        }
        if ("playtime".equals(timer)) {
            if (!"on".equalsIgnoreCase(args[0]) && !"off".equalsIgnoreCase(args[0]) && !"toggle".equalsIgnoreCase(args[0])) {
                reply(sender, "Usage: /playtime <on|off|toggle|settings|background <on|off>|color next|position <x> <y>");
                return;
            }
            boolean enabled = "toggle".equalsIgnoreCase(args[0]) ? !PlaytimeTrackerHud.isEnabled() : "on".equalsIgnoreCase(args[0]);
            PlaytimeTrackerHud.setEnabled(enabled);
            reply(sender, "Playtime Tracker HUD " + (PlaytimeTrackerHud.isEnabled() ? "enabled." : "disabled."));
        } else if ("sessiontimer".equals(timer)) {
            boolean enabled = parseToggle(args[0], SessionTimerHud.isEnabled()) == null ? SessionTimerHud.isEnabled()
                    : parseToggle(args[0], SessionTimerHud.isEnabled()).booleanValue();
            if (parseToggle(args[0], SessionTimerHud.isEnabled()) == null) { reply(sender, "Usage: /sessiontimer <on|off|toggle|settings|background <on|off>|color next|position <x> <y>"); return; }
            SessionTimerHud.setEnabled(enabled);
            reply(sender, "Session Timer " + (SessionTimerHud.isEnabled() ? "enabled." : "disabled."));
        } else {
            Boolean value = parseToggle(args[0], AfkTimerHud.isEnabled());
            if (value == null) { reply(sender, "Usage: /afktimer <on|off|toggle|settings|mode <always|afk>|background <on|off>|color next|position <x> <y>"); return; }
            AfkTimerHud.setEnabled(value.booleanValue());
            reply(sender, "AFK Timer " + (AfkTimerHud.isEnabled() ? "enabled." : "disabled."));
        }
    }

    private void handleHud(ICommandSender sender, String[] args, boolean fps) {
        AlphaModule module = AlphaModuleRegistry.find(fps ? "fps" : "cps");
        if (!hasModuleAccess(module)) { reply(sender, "Access denied: this HUD is not available to your current rank."); return; }
        if (args.length == 0 || "settings".equalsIgnoreCase(args[0])) { openSettings(sender, fps ? "fps" : "cps"); return; }
        String setting = args[0].toLowerCase(Locale.ROOT);
        if (args.length == 1 && ("on".equals(setting) || "off".equals(setting) || "toggle".equals(setting))) {
            Boolean enabled = parseToggle(setting, module.isEnabled());
            module.setEnabled(enabled.booleanValue());
            reply(sender, (fps ? "FPS" : "CPS") + " module " + (module.isEnabled() ? "enabled." : "disabled."));
        } else if ("background".equals(setting) && args.length == 2) {
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
        GuiScreen parent = mc.currentScreen instanceof net.minecraft.client.gui.GuiChat ? null : mc.currentScreen;
        GuiScreen target = null;
        if ("fps".equals(id)) target = new FpsSettingsGui(parent);
        else if ("cps".equals(id)) target = new CpsSettingsGui(parent);
        else if ("playtime".equals(id) || "playtime-tracker".equals(id)) target = new PlaytimeTrackerSettingsGui(parent);
        else if ("sessiontimer".equals(id) || "session-timer".equals(id)) target = new SessionTimerSettingsGui(parent);
        else if ("afktimer".equals(id) || "afk-timer".equals(id)) target = new AfkTimerSettingsGui(parent);
        else if ("bhop".equals(id) || "staffmods".equals(id)) target = new BhopSettingsGui(parent);
        else if ("wtap".equals(id) || "w-tap-assistance".equals(id)) target = new WTapSettingsGui(parent);
        else if ("nameviewer".equals(id) || "name-viewer".equals(id)) target = new NameViewerSettingsGui(parent);
        else if ("legacypvp".equals(id) || "legacy-pvp".equals(id)) target = new LegacyPvpSettingsGui(parent);
        else if ("quality".equals(id)) target = new QualityGui(parent);
        if (target != null) AlphaClient.openGuiFromCommand(target);
        else if ("fullbright".equals(id)) reply(sender, "Use /fullbright <on|off|toggle>.");
        else if (module != null) mc.displayGuiScreen(new ModuleSettingsGui(parent, module));
        else reply(sender, "Unknown module/settings: " + moduleName + ". Use /alphaclient modules.");
    }

    private void listModules(ICommandSender sender) {
        reply(sender, "Registered modules (LOCKED modules still require their assigned rank/permission):");
        for (AlphaModule module : AlphaModuleRegistry.getAll()) {
            // These are feature groups/folders, not standalone modules.
            if ("staffmods".equals(module.getId()) || "quality".equals(module.getId())) continue;
            reply(sender, module.getId() + " - " + (hasModuleAccess(module) ? module.getStatusLabel() : "LOCKED"));
        }
        reply(sender, "fullbright - " + (FullbrightMod.isEnabled() ? "ON" : "OFF"));
        reply(sender, "playtimeTracker - " + (PlaytimeTrackerHud.isEnabled() ? "ON" : "OFF"));
        reply(sender, "sessionTimer - " + (SessionTimerHud.isEnabled() ? "ON" : "OFF"));
        reply(sender, "afkTimer - " + (AfkTimerHud.isEnabled() ? "ON" : "OFF"));
        reply(sender, "bhop - " + (hasBhopAccess() ? (BhopMode.isEnabled() ? "ON" : "OFF") : "LOCKED"));
        reply(sender, "Aliases: /fpshud /cpshud /bhop /wtap /fullbright /nameviewer /legacypvp /playtime /sessiontimer /afktimer /quality.");
        reply(sender, "Planned features remain unavailable until implemented.");
    }

    private void sendHelp(ICommandSender sender) {
        reply(sender, "AlphaClient command core:");
        reply(sender, "/alphaclient help | modules | settings <module>");
        reply(sender, "/alphaclient module <module> <on|off|toggle>");
        reply(sender, "/alphaclient fps|cps <background|color|position|mode> ...");
        reply(sender, "/alphaclient bhop <on|off|toggle|speed <1-20>|shiftup <on|off>|name <text>|color <#RRGGBB>|settings>");
        reply(sender, "/alphaclient wtap <on|off|toggle|delay|cooldown|mode|trigger|target|requirew|settings>");
        reply(sender, "/alphaclient fullbright <on|off|toggle>");
        reply(sender, "/alphaclient legacypvp <feature> <on|off>");
        reply(sender, "HUDs: /fpshud /cpshud /playtime /sessiontimer /afktimer.");
        reply(sender, "Other shortcuts: /bhop /wtap /fullbright /nameviewer /legacypvp /quality.");
        reply(sender, "StaffMods GUI: /staffmods. Open a StaffMods folder with /staffmods <folder>. /general opens the regular Mods menu on GENERAL.");
        reply(sender, "Staff folder commands: /staffgeneral /stafftmod /staffmod /staffsrmod /staffadmin /staffsradmin /staffmanagement /staffplatformadmin /staffmanager /staffowner /stafffounder.");
        reply(sender, "Other folder commands: /beta /donator /scout /rogue /elite /alpha /alphaplus.");
    }

    private String normalizeModuleId(String id) {
        String value = id == null ? "" : id.toLowerCase(Locale.ROOT);
        if ("fpshud".equals(value)) return "fps";
        if ("cpshud".equals(value)) return "cps";
        if ("playtime".equals(value) || "playtime-tracker".equals(value) || "playtimetracker".equals(value)) return "playtime";
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
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args,
                                                net.minecraft.util.BlockPos pos) {
        List<String> options = new ArrayList<String>();
        if ("staffmods".equals(commandName) && args.length == 1) {
            String[] folders = {"general","beta","tmod","mod","srmod","admin","sradmin","management","platformadmin","manager","owner","founder","donator","scout","rogue","elite","alpha","alphaplus"};
            for (String folder : folders) options.add(folder);
        } else if ("alphaclient".equals(commandName)) {
            if (args.length == 1) {
                String[] values = {"help","modules","settings","module","fps","cps","bhop","wtap","fullbright"};
                for (String value : values) options.add(value);
            } else if (args.length == 2 && "module".equalsIgnoreCase(args[0])) {
                for (AlphaModule module : AlphaModuleRegistry.getAll()) {
                    if ("staffmods".equals(module.getId()) || "quality".equals(module.getId())) continue;
                    options.add(module.getId());
                }
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
