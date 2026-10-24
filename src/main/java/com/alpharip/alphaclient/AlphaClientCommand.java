package com.alpharip.alphaclient;

import com.alpharip.staffmod.cps.CpsMod;
import com.alpharip.staffmod.fps.FpsMod;
import com.alpharip.staffmod.gui.AlphaPermsGui;
import com.alpharip.staffmod.gui.AlphaHelpGui;
import com.alpharip.staffmod.gui.AfkTimerSettingsGui;
import com.alpharip.staffmod.gui.AlphaModMenu;
import com.alpharip.staffmod.gui.BhopSettingsGui;
import com.alpharip.staffmod.gui.CpsSettingsGui;
import com.alpharip.staffmod.gui.FpsSettingsGui;
import com.alpharip.staffmod.gui.HudEditorGui;
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
import com.alpharip.staffmod.quality.AutoGGMod;
import com.alpharip.staffmod.quality.AutoReconnectMod;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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

        if ("autogg".equals(route)) {
            handleAutoGG(sender, args);
            return;
        }
        if ("autoreconnect".equals(route)) {
            handleAutoReconnect(sender, args);
            return;
        }

        if ("alphaperms".equals(route)) {
            if (args != null && args.length > 0 && "help".equalsIgnoreCase(args[0])) {
                showCommandGuide(sender, route);
            } else {
                handleAlphaPerms(sender, args);
            }
            return;
        }

        String permissionKey = commandPermissionForRoute(route, args);
        if (("bhop".equals(route) || "alphaclient:bhop".equals(route)
                || ("alphaclient".equals(route) && args != null && args.length > 0 && "bhop".equalsIgnoreCase(args[0])))
                && !hasBhopAccess()) {
            reply(sender, "Access denied: BHOP requires the correct staff rank and staff.bhop permission.");
            return;
        }
        if (("wtap".equals(route) || "alphaclient:wtap".equals(route)
                || ("alphaclient".equals(route) && args != null && args.length > 0 && "wtap".equalsIgnoreCase(args[0])))
                && !hasWTapAccess()) {
            reply(sender, "Access denied: W-Tap Assistance is Founder-only.");
            return;
        }
        if (permissionKey != null && Boolean.FALSE.equals(PermissionService.getCommandPermissionOverride(permissionKey))) {
            reply(sender, "Access denied: your Alpha Account does not have " + permissionKey + ".");
            return;
        }

        if ("pothud".equals(route)) {
            handlePotionHud(sender, args);
            return;
        }

        if (args != null && args.length == 1 && "help".equalsIgnoreCase(args[0])) {
            showCommandGuide(sender, route);
            return;
        }

        if ("alphahelp".equals(route)) {
            if (args != null && args.length == 1 && "open".equalsIgnoreCase(args[0])) {
                Minecraft mc = Minecraft.getMinecraft();
                if (mc != null) AlphaClient.openGuiFromCommand(new AlphaHelpGui(mc.currentScreen));
                return;
            }
            if (args == null || args.length == 0 || (args.length == 1 && "help".equalsIgnoreCase(args[0]))) {
                sendHelp(sender);
                return;
            }
            reply(sender, "Usage: /alphahelp [open]");
            return;
        }

        if ("hudeditor".equals(route)) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc != null) AlphaClient.openGuiFromCommand(new HudEditorGui(mc.currentScreen));
            return;
        }

        if ("profiles".equals(route)) {
            listProfiles(sender);
            return;
        }

        if ("profile".equals(route)) {
            handleProfileCommand(sender, args);
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

        AlphaModule.Category regularCategory = regularCategoryAlias(route);
        if (regularCategory != null) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc == null) return;
            String categoryPermission = categoryPermissionKey(regularCategory);
            Boolean categoryOverride = PermissionService.getCommandPermissionOverride(categoryPermission);
            boolean categoryAllowed = categoryOverride != null
                    ? categoryOverride.booleanValue()
                    : PermissionService.hasAccess(regularCategory.getRequiredRank());
            if (categoryOverride == null && !categoryAllowed) {
                for (AlphaModule module : AlphaModuleRegistry.getForCategory(regularCategory)) {
                    if (!"staffmods".equals(module.getId()) && module.isUnlocked()) {
                        categoryAllowed = true;
                        break;
                    }
                }
            }
            if (!categoryAllowed) {
                reply(sender, "Access denied: your current rank does not allow the " + regularCategory.getTitle() + " category.");
                return;
            }
            AlphaClient.openGuiFromCommand(new AlphaModMenu(mc.currentScreen, regularCategory));
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

    /**
     * Shared, consistently formatted in-chat command guides.
     * Any registered AlphaClient command can show its guide with the "help" argument.
     */
    private void showCommandGuide(ICommandSender sender, String route) {
        String name = route == null ? "" : route.toLowerCase(Locale.ROOT);
        if ("alphahelp".equals(name)) {
            guideHeader(sender, "ALPHAHELP | COMMAND OVERVIEW");
            guideLine(sender, "/alphahelp", "List commands available to your current rank");
            guideLine(sender, "/alphahelp open", "Open the scrollable command-list GUI");
            guideLine(sender, "/alphahelp help", "Show this AlphaHelp guide");
            guideLine(sender, "/alphaclient help", "Show the main AlphaClient command guide");
            guideLine(sender, "/pothud help", "Show Potion HUD commands and settings");
            guideLine(sender, "/playtime help", "Show Playtime Tracker commands and settings");
            guideLine(sender, "/staffmods help", "Show StaffMods folder commands");
            guideLine(sender, "/alphaperms help", "Show the Founder permission-manager guide");
        } else if ("alphaclient".equals(name) || "alphaclient:help".equals(name)) {
            guideHeader(sender, "ALPHACLIENT");
            guideLine(sender, "/alphaclient help", "Show this guide");
            guideLine(sender, "/alphaclient modules", "List registered modules and their status");
            guideLine(sender, "/alphaclient settings <module>", "Open a module's settings");
            guideLine(sender, "/alphaclient module <module> <on|off|toggle>", "Change a supported module's state");
            guideLine(sender, "/alphaclient fps|cps|bhop|wtap", "Open the corresponding module command");
            guideLine(sender, "/alphaclient fullbright|legacypvp", "Open the corresponding feature command");
            guideLine(sender, "/alphaclient playtime|sessiontimer|afktimer", "Use the corresponding timer command");
            guideLine(sender, "/alphahelp", "Show the rank-filtered command overview");
            guideLine(sender, "/profiles", "List saved profiles");
            guideLine(sender, "/profile <name|create|edit|delete>", "Switch or manage profiles");
            guideLine(sender, "/hudeditor", "Open the HUD editor");
        } else if ("alphaclient:modules".equals(name)) {
            guideHeader(sender, "MODULE LIST");
            guideLine(sender, "/alphaclient:modules", "List registered modules and their current status");
            guideLine(sender, "/alphaclient module <module> <on|off|toggle>", "Change a supported module's state");
            guideLine(sender, "/alphaclient:settings <module>", "Open a module's settings");
        } else if ("alphaclient:settings".equals(name)) {
            guideHeader(sender, "MODULE SETTINGS");
            guideLine(sender, "/alphaclient:settings <module>", "Open settings for the named module");
            guideLine(sender, "/alphaclient settings <module>", "Use the main AlphaClient command route");
            guideLine(sender, "Valid registered module IDs: quality, name-viewer, fps, legacy-pvp, cps, staffmods", "Use the ID as the module argument");
            guideLine(sender, "/fpshud settings", "Open FPS HUD settings");
            guideLine(sender, "/cpshud settings", "Open CPS HUD settings");
            guideLine(sender, "/bhop settings", "Open BHOP settings");
            guideLine(sender, "/wtap settings", "Open W-Tap settings");
            guideLine(sender, "/legacypvp settings", "Open Legacy PvP settings");
        } else if ("alphaclient:module".equals(name)) {
            guideHeader(sender, "MODULE CONTROL");
            guideLine(sender, "/alphaclient:module <module> <on|off|toggle>", "Change a supported module's state");
            guideLine(sender, "/alphaclient module <module> <on|off|toggle>", "Use the main AlphaClient command route");
            guideLine(sender, "Special module IDs: bhop, wtap, fullbright, playtime, sessiontimer, afktimer", "These use dedicated command handlers");
            guideLine(sender, "/alphaclient:modules", "List available modules");
            guideLine(sender, "/staffmods", "Open StaffMods; it is a folder, not a single module");
        } else if ("fpshud".equals(name) || "alphaclient:fps".equals(name)) {
            guideHeader(sender, "FPS HUD");
            guideLine(sender, "/fpshud [on|off|toggle]", "Enable or disable the FPS HUD");
            guideLine(sender, "/fpshud settings", "Open FPS settings");
            guideLine(sender, "/fpshud background <on|off>", "Toggle the HUD background");
            guideLine(sender, "/fpshud color next", "Cycle the HUD color");
            guideLine(sender, "/fpshud position <x> <y>", "Set HUD coordinates");
        } else if ("cpshud".equals(name) || "alphaclient:cps".equals(name)) {
            guideHeader(sender, "CPS HUD");
            guideLine(sender, "/cpshud [on|off|toggle]", "Enable or disable the CPS HUD");
            guideLine(sender, "/cpshud settings", "Open CPS settings");
            guideLine(sender, "/cpshud background <on|off>", "Toggle the HUD background");
            guideLine(sender, "/cpshud color next", "Cycle the HUD color");
            guideLine(sender, "/cpshud mode <both|left|right>", "Choose which clicks are shown");
            guideLine(sender, "/cpshud position <x> <y>", "Set HUD coordinates");
        } else if ("bhop".equals(name) || "alphaclient:bhop".equals(name)) {
            guideHeader(sender, "BHOP");
            guideLine(sender, "/bhop [on|off|toggle]", "Enable or disable BHOP");
            guideLine(sender, "/bhop settings", "Open BHOP settings");
            guideLine(sender, "/bhop speed <1-20>", "Set the speed level");
            guideLine(sender, "/bhop shiftup <on|off>", "Toggle Shift-Up");
            guideLine(sender, "/bhop name <text>", "Set the displayed name");
            guideLine(sender, "/bhop color <#RRGGBB>", "Set the display color");
        } else if ("wtap".equals(name) || "alphaclient:wtap".equals(name)) {
            sendWTapHelp(sender);
        } else if ("fullbright".equals(name) || "alphaclient:fullbright".equals(name)) {
            guideHeader(sender, "FULLBRIGHT");
            guideLine(sender, "/fullbright <on|off|toggle>", "Change Fullbright state");
        } else if ("playtime".equals(name) || "alphaclient:playtime".equals(name)) {
            guideTimer(sender, "PLAYTIME TRACKER", "/playtime", false);
        } else if ("sessiontimer".equals(name) || "alphaclient:sessiontimer".equals(name)) {
            guideTimer(sender, "SESSION TIMER", "/sessiontimer", false);
        } else if ("afktimer".equals(name) || "alphaclient:afktimer".equals(name)) {
            guideTimer(sender, "AFK TIMER", "/afktimer", true);
        } else if ("legacypvp".equals(name) || "alphaclient:legacypvp".equals(name)) {
            guideHeader(sender, "LEGACY PVP");
            guideLine(sender, "/legacypvp [on|off|toggle]", "Enable or disable Legacy PvP");
            guideLine(sender, "/legacypvp settings", "Open Legacy PvP settings");
            guideLine(sender, "/legacypvp <feature> <on|off>", "Toggle blockhit, potion-punch, swing, bow, rod, food/drink animations or view bobbing");
        } else if ("nameviewer".equals(name)) {
            guideHeader(sender, "NAME VIEWER");
            guideLine(sender, "/nameviewer", "Open Name Viewer settings");
            guideLine(sender, "/nameviewer settings", "Open Name Viewer settings");
            guideLine(sender, "/nameviewer help", "Show this guide");
            guideLine(sender, "/alphaclient module name-viewer <on|off|toggle>", "Change the Name Viewer module state");
        } else if ("quality".equals(name)) {
            guideHeader(sender, "QUALITY");
            guideLine(sender, "/quality", "Open the Quality feature-group screen");
            guideLine(sender, "/quality help", "Show this guide");
            guideLine(sender, "/alphaclient settings quality", "Open Quality through the module settings command");
        } else if ("profiles".equals(name)) {
            guideHeader(sender, "PROFILES");
            guideLine(sender, "/profiles", "List available profiles");
            guideLine(sender, "/profile <name>", "Switch to a profile");
            guideLine(sender, "/profile create <name>", "Create and select a profile");
            guideLine(sender, "/profile edit <old name> <new name>", "Rename a profile");
            guideLine(sender, "/profile delete <name>", "Delete a profile");
        } else if ("profile".equals(name)) {
            showCommandGuide(sender, "profiles");
        } else if ("hudeditor".equals(name)) {
            guideHeader(sender, "HUD EDITOR");
            guideLine(sender, "/hudeditor", "Open the HUD editor");
        } else if ("staffmods".equals(name)) {
            guideHeader(sender, "STAFFMODS");
            guideLine(sender, "/staffmods", "Open the StaffMods menu");
            guideLine(sender, "/staffmods <folder>", "Open an accessible StaffMods folder");
            guideLine(sender, "/staffmods general|beta|donator|scout|rogue|elite|alpha|alphaplus", "Open an accessible community or donator folder");
            guideLine(sender, "/staffmods tmod|mod|srmod|admin|sradmin", "Open an accessible staff-rank folder");
            guideLine(sender, "/staffmods management|platformadmin|manager|owner|founder", "Open an accessible senior staff folder");
            guideLine(sender, "/staffmods help", "Show this guide");
            guideLine(sender, "/staffgeneral", "Open StaffMods → General");
            guideLine(sender, "/stafftmod /staffmod /staffsrmod", "Open your permitted staff-rank folder");
            guideLine(sender, "/staffadmin /staffsradmin", "Open your permitted admin folder");
            guideLine(sender, "/staffmanagement", "Open Management (Platform Admin / Manager)");
            guideLine(sender, "/staffplatformadmin /staffmanager", "Open your rank-specific Management folder");
            guideLine(sender, "/staffowner /stafffounder", "Open restricted staff folders");
        } else if ("general".equals(name) || "beta".equals(name) || "donator".equals(name)
                || "scout".equals(name) || "rogue".equals(name) || "elite".equals(name)
                || "alpha".equals(name) || "alphaplus".equals(name)) {
            guideHeader(sender, name.toUpperCase(Locale.ROOT) + " MENU");
            guideLine(sender, "/" + name, "Open the " + name + " module category");
        } else if ("staffgeneral".equals(name) || "stafftmod".equals(name) || "staffmod".equals(name)
                || "staffsrmod".equals(name) || "staffadmin".equals(name) || "staffsradmin".equals(name)
                || "staffmanagement".equals(name) || "staffplatformadmin".equals(name) || "staffplatadmin".equals(name)
                || "staffmanager".equals(name) || "staffowner".equals(name) || "stafffounder".equals(name)) {
            guideHeader(sender, "STAFFMODS FOLDER");
            guideLine(sender, "/" + name, "Open this StaffMods folder if your account has access");
            guideLine(sender, "/staffmods", "Open the StaffMods menu");
            guideLine(sender, "/staffmods <folder>", "Open another folder only if your account has access");
        } else if ("alphaperms".equals(name)) {
            guideHeader(sender, "FOUNDER PERMISSION MANAGER");
            guideLine(sender, "/alphaperms open", "Open the permission manager");
            guideLine(sender, "/alphaperms list", "List permission catalog entries");
            guideLine(sender, "/alphaperms <alpha-name> list", "List account permissions");
            guideLine(sender, "/alphaperms <alpha-name> grant|revoke|clear <permission>", "Manage an account permission");
            guideLine(sender, "/alphaperms rank <rank> list", "List rank permissions");
            guideLine(sender, "/alphaperms rank <rank> grant|revoke|clear <permission>", "Manage a rank permission");
        } else {
            guideHeader(sender, name.toUpperCase(Locale.ROOT) + " COMMAND");
            guideLine(sender, "/" + name, "Run the command's default action");
            guideLine(sender, "/" + name + " help", "Show this guide");
            guideLine(sender, "No additional arguments are documented for this command.", "Use the command overview for supported commands");
        }
        reply(sender, "§8Tip: append §fhelp §8to any AlphaClient command to view its guide.");
    }

    private void guideTimer(ICommandSender sender, String title, String command, boolean afk) {
        guideHeader(sender, title);
        guideLine(sender, command + " <on|off|toggle>", "Enable or disable the timer HUD");
        guideLine(sender, command + " settings", "Open timer settings");
        guideLine(sender, command + " background <on|off>", "Toggle the HUD background");
        guideLine(sender, command + " color next", "Cycle the HUD color");
        guideLine(sender, command + " position <x> <y>", "Set HUD coordinates");
        if ("playtime".equals(command)) {
            guideLine(sender, "/playtime name <name>", "Change the Playtime HUD label");
            guideLine(sender, "/playtime format <mode>", "Choose a Playtime HUD time format");
            guideLine(sender, "/playtime formatlist", "List available Playtime HUD formats");
            guideLine(sender, "/playtime check", "Show your exact tracked playtime");
        }
        if (afk) guideLine(sender, command + " mode <always|afk>", "Choose when the timer is visible");
    }

    private void guideHeader(ICommandSender sender, String title) {
        reply(sender, "§6§l" + title + " §8| §7Command Guide");
    }

    private void guideLine(ICommandSender sender, String command, String description) {
        reply(sender, "§f" + command + " §8- §7" + description);
    }

    private void handleProfileCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            reply(sender, "Usage: /profile <name> | /profile create <name> | /profile edit <profile name> <new name> | /profile delete <name>");
            reply(sender, "Use /profiles to list available profiles.");
            return;
        }

        String action = args[0].toLowerCase(Locale.ROOT);
        if ("create".equals(action)) {
            String name = joinArgs(args, 1);
            if (name.isEmpty()) {
                reply(sender, "Usage: /profile create <name>");
                return;
            }
            if (ModuleProfileManager.createProfile(name)) {
                reply(sender, "Created and switched to profile: " + ModuleProfileManager.getSelectedProfileName());
            } else {
                reply(sender, "Could not create profile. The name may be empty or already in use.");
            }
            return;
        }

        if ("edit".equals(action)) {
            if (args.length < 3) {
                reply(sender, "Usage: /profile edit <profile name> <new name>");
                return;
            }
            String oldName = args[1];
            String newName = joinArgs(args, 2);
            if (newName.isEmpty()) {
                reply(sender, "Usage: /profile edit <profile name> <new name>");
                return;
            }
            if (ModuleProfileManager.renameProfile(oldName, newName)) {
                reply(sender, "Renamed profile '" + oldName + "' to '" + newName + "'.");
            } else {
                reply(sender, "Could not rename profile '" + oldName + "'. It may not exist, Default cannot be renamed, or the new name is already in use.");
            }
            return;
        }

        if ("delete".equals(action)) {
            String name = joinArgs(args, 1);
            if (name.isEmpty()) {
                reply(sender, "Usage: /profile delete <name>");
                return;
            }
            if (ModuleProfileManager.deleteProfile(name)) {
                reply(sender, "Deleted profile: " + name);
                reply(sender, "Active profile: " + ModuleProfileManager.getSelectedProfileName());
            } else {
                reply(sender, "Could not delete profile '" + name + "'. It may not exist, or Default cannot be deleted.");
            }
            return;
        }

        selectProfile(sender, args);
    }

    private String joinArgs(String[] args, int start) {
        if (args == null || start >= args.length) return "";
        StringBuilder value = new StringBuilder();
        for (int i = start; i < args.length; i++) {
            if (i > start) value.append(' ');
            value.append(args[i]);
        }
        return value.toString().trim();
    }

    private String categoryPermissionKey(AlphaModule.Category category) {
        if (category == null) return null;
        switch (category) {
            case PUBLIC: return "menu.general";
            case BETA: return "menu.beta";
            case DONATOR: return "menu.donator";
            case SCOUT: return "menu.scout";
            case ROGUE: return "menu.rogue";
            case ELITE: return "menu.elite";
            case ALPHA: return "menu.alpha";
            case ALPHA_PLUS: return "menu.alphaplus";
            default: return null;
        }
    }

    private String moduleCommandPermission(String moduleId) {
        if (moduleId == null) return null;
        String id = moduleId.toLowerCase(Locale.ROOT);
        if ("fps".equals(id)) return "command.fpshud";
        if ("cps".equals(id)) return "command.cpshud";
        if ("staffmods".equals(id) || "bhop".equals(id)) return "command.bhop";
        if ("w-tap-assistance".equals(id) || "wtap".equals(id)) return "command.wtap";
        if ("fullbright".equals(id)) return "command.fullbright";
        if ("name-viewer".equals(id) || "nameviewer".equals(id)) return "command.nameviewer";
        if ("legacy-pvp".equals(id) || "legacypvp".equals(id)) return "command.legacypvp";
        if ("playtime".equals(id) || "playtime-tracker".equals(id)) return "command.playtime";
        if ("sessiontimer".equals(id) || "session-timer".equals(id)) return "command.sessiontimer";
        if ("afktimer".equals(id) || "afk-timer".equals(id)) return "command.afktimer";
        if ("quality".equals(id)) return "command.quality";
        return null;
    }

    private String commandPermissionForRoute(String route, String[] args) {
        if (route == null) return null;
        if ("alphaclient".equals(route)) {
            if (args == null || args.length == 0 || "help".equalsIgnoreCase(args[0])) return "command.help";
            String subcommand = args[0].toLowerCase(Locale.ROOT);
            if ("modules".equals(subcommand)) return "command.modules";
            if ("settings".equals(subcommand)) return "command.settings";
            if ("module".equals(subcommand) && args.length > 1) return moduleCommandPermission(args[1]);
            if ("fps".equals(subcommand)) return "command.fpshud";
            if ("cps".equals(subcommand)) return "command.cpshud";
            if ("bhop".equals(subcommand)) return "command.bhop";
            if ("wtap".equals(subcommand)) return "command.wtap";
            if ("fullbright".equals(subcommand)) return "command.fullbright";
            if ("legacypvp".equals(subcommand)) return "command.legacypvp";
            if ("playtime".equals(subcommand)) return "command.playtime";
            if ("sessiontimer".equals(subcommand)) return "command.sessiontimer";
            if ("afktimer".equals(subcommand)) return "command.afktimer";
        }
        if ("alphahelp".equals(route) || "alphaclient:help".equals(route)) return "command.help";
        if ("alphaclient:modules".equals(route)) return "command.modules";
        if ("alphaclient:settings".equals(route)) return "command.settings";
        if ("alphaclient:module".equals(route) && args != null && args.length > 0) return moduleCommandPermission(args[0]);
        if ("hudeditor".equals(route)) return "command.hudeditor";
        if ("profiles".equals(route)) return "command.profiles.list";
        if ("profile".equals(route)) {
            String action = args != null && args.length > 0 ? args[0].toLowerCase(Locale.ROOT) : "";
            if ("create".equals(action)) return "command.profiles.create";
            if ("edit".equals(action)) return "command.profiles.edit";
            if ("delete".equals(action)) return "command.profiles.delete";
            return "command.profiles.switch";
        }
        if ("pothud".equals(route)) return "hud.potion";
        if ("fpshud".equals(route) || "alphaclient:fps".equals(route)) return "command.fpshud";
        if ("cpshud".equals(route) || "alphaclient:cps".equals(route)) return "command.cpshud";
        if ("bhop".equals(route) || "alphaclient:bhop".equals(route)) return "command.bhop";
        if ("wtap".equals(route) || "alphaclient:wtap".equals(route)) return "command.wtap";
        if ("fullbright".equals(route) || "alphaclient:fullbright".equals(route)) return "command.fullbright";
        if ("nameviewer".equals(route)) return "command.nameviewer";
        if ("legacypvp".equals(route) || "alphaclient:legacypvp".equals(route)) return "command.legacypvp";
        if ("playtime".equals(route) || "alphaclient:playtime".equals(route)) return args != null && args.length > 0 && "check".equalsIgnoreCase(args[0]) ? "playtime.check" : "command.playtime";
        if ("sessiontimer".equals(route) || "alphaclient:sessiontimer".equals(route)) return "command.sessiontimer";
        if ("afktimer".equals(route) || "alphaclient:afktimer".equals(route)) return "command.afktimer";
        if ("quality".equals(route)) return "command.quality";
        if ("staffmods".equals(route)) {
            String folder = args != null && args.length > 0 ? args[0].toLowerCase(Locale.ROOT) : "";
            return staffFolderPermission(folder);
        }
        if ("staffgeneral".equals(route)) return "staffmods.general";
        if ("stafftmod".equals(route)) return "staffmods.tmod";
        if ("staffmod".equals(route)) return "staffmods.mod";
        if ("staffsrmod".equals(route)) return "staffmods.srmod";
        if ("staffadmin".equals(route)) return "staffmods.admin";
        if ("staffsradmin".equals(route)) return "staffmods.sradmin";
        if ("staffplatformadmin".equals(route) || "staffplatadmin".equals(route)) return "staffmods.platformadmin";
        if ("staffmanagement".equals(route)) return "staffmods.management";
        if ("staffmanager".equals(route)) return "staffmods.manager";
        if ("staffowner".equals(route)) return "staffmods.owner";
        if ("stafffounder".equals(route)) return "staffmods.founder";
        if ("staffdeveloper".equals(route)) return "staffmods.developer";
        if ("general".equals(route)) return "menu.general";
        if ("beta".equals(route)) return "menu.beta";
        if ("donator".equals(route)) return "menu.donator";
        if ("scout".equals(route)) return "menu.scout";
        if ("rogue".equals(route)) return "menu.rogue";
        if ("elite".equals(route)) return "menu.elite";
        if ("alpha".equals(route)) return "menu.alpha";
        if ("alphaplus".equals(route)) return "menu.alphaplus";
        return null;
    }

    private String staffFolderPermission(String folder) {
        if ("general".equals(folder)) return "staffmods.general";
        if ("beta".equals(folder)) return "menu.beta";
        if ("donator".equals(folder)) return "menu.donator";
        if ("scout".equals(folder)) return "menu.scout";
        if ("rogue".equals(folder)) return "menu.rogue";
        if ("elite".equals(folder)) return "menu.elite";
        if ("alpha".equals(folder)) return "menu.alpha";
        if ("alphaplus".equals(folder)) return "menu.alphaplus";
        if ("tmod".equals(folder) || "trial_moderator".equals(folder)) return "staffmods.tmod";
        if ("mod".equals(folder) || "moderator".equals(folder)) return "staffmods.mod";
        if ("srmod".equals(folder) || "senior_moderator".equals(folder)) return "staffmods.srmod";
        if ("admin".equals(folder)) return "staffmods.admin";
        if ("sradmin".equals(folder) || "senior_admin".equals(folder)) return "staffmods.sradmin";
        if ("platformadmin".equals(folder) || "platadmin".equals(folder)) return "staffmods.platformadmin";
        if ("management".equals(folder)) return "staffmods.management";
        if ("manager".equals(folder)) return "staffmods.manager";
        if ("owner".equals(folder)) return "staffmods.owner";
        if ("founder".equals(folder)) return "staffmods.founder";
        if ("developer".equals(folder)) return "staffmods.developer";
        return "staffmods.open";
    }

    private void handleAlphaPerms(ICommandSender sender, String[] args) {
        if (!SupabaseCommunityService.isSignedInFounder()) {
            reply(sender, "FOUNDER-ONLY: sign in with the Founder Alpha Account to manage permissions.");
            return;
        }
        if (args.length > 0 && "open".equalsIgnoreCase(args[0])) {
            if (args.length != 1) {
                reply(sender, "Usage: /alphaperms open");
                return;
            }
            Minecraft mc = Minecraft.getMinecraft();
            if (mc == null) {
                reply(sender, "Could not open the permission manager GUI.");
                return;
            }
            GuiScreen parent = mc.currentScreen instanceof net.minecraft.client.gui.GuiChat ? null : mc.currentScreen;
            AlphaClient.openGuiFromCommand(new AlphaPermsGui(parent));
            return;
        }
        if (args.length == 0 || "help".equalsIgnoreCase(args[0])) {
            reply(sender, "Founder-only command permission manager.");
            reply(sender, "/alphaperms open");
            reply(sender, "/alphaperms list");
            reply(sender, "/alphaperms <alpha-name> list");
            reply(sender, "/alphaperms <alpha-name> grant <permission>");
            reply(sender, "/alphaperms <alpha-name> revoke <permission>");
            reply(sender, "/alphaperms <alpha-name> clear <permission>");
            reply(sender, "/alphaperms rank <rank> list");
            reply(sender, "/alphaperms rank <rank> grant <permission>");
            reply(sender, "/alphaperms rank <rank> revoke <permission>");
            reply(sender, "/alphaperms rank <rank> clear <permission>");
            reply(sender, "/alphaperms createid <id>");
            reply(sender, "/alphaperms editid <old-id> <new-id>");
            reply(sender, "/alphaperms deleteid <id>");
            reply(sender, "/alphaperms setnoteid <id> add <note>");
            reply(sender, "/alphaperms setnoteid <id> list");
            reply(sender, "/alphaperms setnoteid <id> clear <number|all>");
            return;
        }
        try {
            JsonObject payload = new JsonObject();
            if ("createid".equalsIgnoreCase(args[0]) && args.length == 2) {
                payload.addProperty("action", "create_id"); payload.addProperty("permission", args[1]);
            } else if ("editid".equalsIgnoreCase(args[0]) && args.length == 3) {
                payload.addProperty("action", "edit_id"); payload.addProperty("permission", args[1]); payload.addProperty("new_permission", args[2]);
            } else if ("deleteid".equalsIgnoreCase(args[0]) && args.length == 2) {
                payload.addProperty("action", "delete_id"); payload.addProperty("permission", args[1]);
            } else if ("setnoteid".equalsIgnoreCase(args[0]) && args.length >= 3) {
                payload.addProperty("permission", args[1]);
                String noteAction = args[2].toLowerCase(Locale.ROOT);
                if ("add".equals(noteAction) && args.length >= 4) {
                    payload.addProperty("action", "add_note"); payload.addProperty("note", joinArgsRange(args, 3, args.length));
                } else if ("list".equals(noteAction) && args.length == 3) payload.addProperty("action", "list_notes");
                else if ("clear".equals(noteAction) && args.length == 4) {
                    payload.addProperty("action", "clear_note");
                    if ("all".equalsIgnoreCase(args[3])) payload.addProperty("clear_all", true);
                    else { try { payload.addProperty("note_number", Integer.parseInt(args[3])); }
                    catch (NumberFormatException ex) { reply(sender, "Note number must be an integer or all."); return; } }
                } else { reply(sender, "Usage: /alphaperms setnoteid <id> add <note...>|list|clear <number|all>"); return; }
            } else if ("list".equalsIgnoreCase(args[0]) && args.length == 1) {
                payload.addProperty("action", "list");
                payload.addProperty("target", "catalog");
            } else if ("rank".equalsIgnoreCase(args[0])) {
                if (args.length < 3) {
                    reply(sender, "Usage: /alphaperms rank <rank name> <list|grant|revoke|clear> [permission]");
                    return;
                }
                boolean listAction = "list".equalsIgnoreCase(args[args.length - 1]);
                String action = listAction ? "list" : args[args.length - 2].toLowerCase(Locale.ROOT);
                if (!"list".equals(action) && !"grant".equals(action) && !"revoke".equals(action) && !"clear".equals(action)) {
                    reply(sender, "Use list, grant, revoke, or clear.");
                    return;
                }
                int rankEnd = listAction ? args.length - 1 : args.length - 2;
                String rankName = joinArgsRange(args, 1, rankEnd);
                if (rankName.isEmpty() || (!listAction && args.length < 4)) {
                    reply(sender, "Usage: /alphaperms rank <rank name> <grant|revoke|clear> <permission>");
                    return;
                }
                payload.addProperty("target", "rank");
                payload.addProperty("rank", rankName);
                payload.addProperty("action", "list".equals(action) ? "list" : ("grant".equals(action) ? "grant_rank" : ("revoke".equals(action) ? "revoke_rank" : "clear_rank")));
                if (!"list".equals(action)) {
                    payload.addProperty("permission", args[args.length - 1]);
                }
            } else {
                if (args.length < 2) {
                    reply(sender, "Usage: /alphaperms <alpha-name> <list|grant|revoke|clear> [permission]");
                    return;
                }
                String action = args[1].toLowerCase(Locale.ROOT);
                payload.addProperty("username", args[0]);
                if ("list".equals(action) && args.length == 2) payload.addProperty("action", "list");
                else if (("grant".equals(action) || "revoke".equals(action) || "clear".equals(action)) && args.length == 3) {
                    payload.addProperty("action", "grant".equals(action) ? "grant_user" : ("revoke".equals(action) ? "revoke_user" : "clear_user"));
                    payload.addProperty("permission", args[2]);
                } else {
                    reply(sender, "Usage: /alphaperms <alpha-name> <list|grant|revoke|clear> [permission]");
                    return;
                }
            }
            JsonObject response = SupabaseCommunityService.manageCommandPermissions(payload);
            String resultAction = payload.get("action").getAsString();
            if ("create_id".equals(resultAction)) reply(sender, "Created permission ID: " + payload.get("permission").getAsString() + ".");
            else if ("edit_id".equals(resultAction)) reply(sender, "Renamed permission ID: " + payload.get("permission").getAsString() + " -> " + payload.get("new_permission").getAsString() + ".");
            else if ("delete_id".equals(resultAction)) reply(sender, "Deleted permission ID: " + payload.get("permission").getAsString() + ".");
            else if ("add_note".equals(resultAction)) reply(sender, "Added note to " + payload.get("permission").getAsString() + ".");
            else if ("clear_note".equals(resultAction)) reply(sender, payload.has("clear_all") && payload.get("clear_all").getAsBoolean() ? "Cleared all notes for " + payload.get("permission").getAsString() + "." : "Cleared note " + payload.get("note_number").getAsInt() + " for " + payload.get("permission").getAsString() + ".");
            else if ("list_notes".equals(resultAction)) {
                JsonArray notes = response.has("notes") && response.get("notes").isJsonArray() ? response.getAsJsonArray("notes") : new JsonArray();
                reply(sender, "Notes for " + payload.get("permission").getAsString() + " (" + notes.size() + "):");
                for (int i = 0; i < notes.size(); i++) if (notes.get(i).isJsonObject()) reply(sender, (i + 1) + ". " + getJsonString(notes.get(i).getAsJsonObject(), "note"));
            } else if (payload.has("permission")) {
                String targetLabel = payload.has("username") ? payload.get("username").getAsString()
                        : (payload.has("rank") ? "rank " + payload.get("rank").getAsString() : "target");
                reply(sender, "Permission updated for " + targetLabel + ": "
                        + payload.get("action").getAsString() + " " + payload.get("permission").getAsString() + ".");
            } else if (response.has("permissions") && response.get("permissions").isJsonArray()) {
                JsonArray rows = response.getAsJsonArray("permissions");
                reply(sender, "Permissions (" + rows.size() + "):");
                for (JsonElement element : rows) {
                    if (!element.isJsonObject()) continue;
                    JsonObject row = element.getAsJsonObject();
                    String key = getJsonString(row, "permission_key");
                    String display = getJsonString(row, "display_name");
                    String category = getJsonString(row, "category");
                    String status = row.has("configured") && !row.get("configured").isJsonNull()
                            && row.get("configured").getAsBoolean()
                            ? (row.has("allowed") && row.get("allowed").getAsBoolean() ? "ALLOW" : "DENY")
                            : "DEFAULT";
                    String source = getJsonString(row, "source");
                    reply(sender, (category == null ? "" : "[" + category + "] ")
                            + (display == null ? key : display) + " [" + key + "]: " + status
                            + (source == null ? "" : " (" + source + ")"));
                }
            } else {
                reply(sender, "Command permissions updated.");
            }
        } catch (Exception exception) {
            reply(sender, "Permission request failed: " + (exception.getMessage() == null ? "unknown error" : exception.getMessage()));
        }
    }

    private String getJsonString(JsonObject object, String key) {
        try {
            JsonElement value = object.get(key);
            return value == null || value.isJsonNull() ? null : value.getAsString();
        } catch (Exception ignored) {
            return null;
        }
    }

    private String joinArgsRange(String[] args, int start, int endExclusive) {
        if (args == null || start >= args.length || endExclusive <= start) return "";
        StringBuilder value = new StringBuilder();
        int end = Math.min(args.length, endExclusive);
        for (int i = start; i < end; i++) {
            if (i > start) value.append(' ');
            value.append(args[i]);
        }
        return value.toString().trim();
    }

    private void listProfiles(ICommandSender sender) {
        List<ModuleProfileManager.BhopProfile> profiles = ModuleProfileManager.getProfiles();
        String active = ModuleProfileManager.getSelectedProfileName();
        reply(sender, "Available profiles (active profile marked with *):");
        boolean defaultFound = false;
        for (ModuleProfileManager.BhopProfile profile : profiles) {
            String name = profile.getProfileName();
            if ("Default".equalsIgnoreCase(name)) defaultFound = true;
            reply(sender, (name.equalsIgnoreCase(active) ? "* " : "  ") + name);
        }
        if (!defaultFound) {
            reply(sender, ("Default".equalsIgnoreCase(active) ? "* " : "  ") + "Default");
        }
        reply(sender, "Switch profile with /profile <profile name>.");
    }

    private void selectProfile(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            reply(sender, "Usage: /profile <name> | /profile create <name> | /profile edit <profile name> <new name> | /profile delete <name>.");
            return;
        }
        StringBuilder requestedName = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) requestedName.append(' ');
            requestedName.append(args[i]);
        }
        String name = requestedName.toString().trim();
        String activeProfile = ModuleProfileManager.getSelectedProfileName();
        if (activeProfile != null && activeProfile.equalsIgnoreCase(name)) {
            reply(sender, "You are already on the profile: " + activeProfile);
            return;
        }
        if (ModuleProfileManager.selectProfile(name)) {
            reply(sender, "Switched to profile: " + ModuleProfileManager.getSelectedProfileName());
        } else {
            reply(sender, "Profile not found: " + name + ". Use /profiles to list available profiles.");
        }
    }

    private void openStaffModsMenu(ICommandSender sender) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null && PermissionService.hasStaffModsAccess()) {
            AlphaClient.openGuiFromCommand(new StaffModsMenuGui(mc.currentScreen));
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
        AlphaClient.openGuiFromCommand(menu);
    }

    private AlphaModule.Category regularCategoryAlias(String name) {
        if ("general".equals(name)) return AlphaModule.Category.PUBLIC;
        if ("beta".equals(name)) return AlphaModule.Category.BETA;
        if ("donator".equals(name)) return AlphaModule.Category.DONATOR;
        if ("scout".equals(name)) return AlphaModule.Category.SCOUT;
        if ("rogue".equals(name)) return AlphaModule.Category.ROGUE;
        if ("elite".equals(name)) return AlphaModule.Category.ELITE;
        if ("alpha".equals(name)) return AlphaModule.Category.ALPHA;
        if ("alphaplus".equals(name)) return AlphaModule.Category.ALPHA_PLUS;
        return null;
    }

    private String staffFolderAlias(String name) {
        if ("staffgeneral".equals(name)) return "general";
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

    private void handlePotionHud(ICommandSender sender, String[] args) {
        AlphaModule module = AlphaModuleRegistry.find("potion-hud-info");
        if (module == null) {
            reply(sender, "Potion HUD module is unavailable.");
            return;
        }
        if (args == null || args.length == 0) {
            module.setEnabled(!module.isEnabled());
        } else if (args.length == 1) {
            Boolean enabled = parseToggle(args[0], module.isEnabled());
            if (enabled == null) {
                reply(sender, "Usage: /pothud [on|off|toggle]");
                return;
            }
            module.setEnabled(enabled.booleanValue());
        } else {
            reply(sender, "Usage: /pothud [on|off|toggle]");
            return;
        }
        reply(sender, "Potion HUD " + (module.isEnabled() ? "enabled." : "disabled."));
    }

    private boolean hasModuleAccess(AlphaModule module) {
        if (module == null) return false;
        String permissionKey = moduleCommandPermission(module.getId());
        Boolean commandOverride = PermissionService.getCommandPermissionOverride(permissionKey);
        if (commandOverride != null) return commandOverride.booleanValue();
        if (!module.isUnlocked()) return false;
        if ("staffmods".equals(module.getId())) return hasBhopAccess();
        if ("w-tap-assistance".equals(module.getId())) return hasWTapAccess();
        return true;
    }

    private boolean hasBhopAccess() {
        Boolean override = PermissionService.getCommandPermissionOverride("command.bhop");
        if (override != null) return override.booleanValue();
        return PermissionService.hasModAccess("staff.bhop");
    }

    private boolean hasWTapAccess() {
        Boolean override = PermissionService.getCommandPermissionOverride("command.wtap");
        if (override != null) return override.booleanValue();
        // Use the authenticated community profile's explicit Founder flag as well as the rank gate.
        return PermissionService.hasModAccess("w-tap-assistance")
                || PermissionService.hasAccess(PermissionService.Rank.FOUNDER)
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

    private void handleAutoGG(ICommandSender sender, String[] args) {
        if (args == null || args.length == 0) {
            reply(sender, "Usage: /autogg on | off | delay <seconds>");
            return;
        }

        if (args.length == 1 && "on".equalsIgnoreCase(args[0])) {
            AutoGGMod.setEnabled(true);
            reply(sender, "Auto GG is ON.");
            return;
        }
        if (args.length == 1 && "off".equalsIgnoreCase(args[0])) {
            AutoGGMod.setEnabled(false);
            reply(sender, "Auto GG is OFF.");
            return;
        }
        if (args.length == 2 && "delay".equalsIgnoreCase(args[0])) {
            try {
                double seconds = Double.parseDouble(args[1]);
                if (Double.isNaN(seconds) || Double.isInfinite(seconds) || seconds < 0.0D || seconds > 10.0D) {
                    reply(sender, "Auto GG delay must be between 0 and 10 seconds.");
                    return;
                }
                AutoGGMod.setDelaySeconds(seconds);
                reply(sender, "Auto GG delay set to " + String.format(Locale.ROOT, "%.1f", AutoGGMod.getDelaySeconds()) + " seconds.");
            } catch (NumberFormatException e) {
                reply(sender, "Usage: /autogg delay <seconds>");
            }
            return;
        }
        reply(sender, "Usage: /autogg on | off | delay <seconds>");
    }

    private void handleAutoReconnect(ICommandSender sender, String[] args) {
        if (args == null || args.length == 0) {
            reply(sender, "Usage: /autoreconnect on | off | delay <seconds>");
            return;
        }
        if (args.length == 1 && "on".equalsIgnoreCase(args[0])) {
            AutoReconnectMod.setEnabled(true);
            reply(sender, "Auto Reconnect is ON.");
            return;
        }
        if (args.length == 1 && "off".equalsIgnoreCase(args[0])) {
            AutoReconnectMod.setEnabled(false);
            reply(sender, "Auto Reconnect is OFF.");
            return;
        }
        if (args.length == 2 && "delay".equalsIgnoreCase(args[0])) {
            try {
                double seconds = Double.parseDouble(args[1]);
                if (Double.isNaN(seconds) || Double.isInfinite(seconds) || seconds < 0.0D || seconds > 30.0D) {
                    reply(sender, "Auto Reconnect delay must be between 0 and 30 seconds.");
                    return;
                }
                AutoReconnectMod.setDelaySeconds(seconds);
                reply(sender, "Auto Reconnect delay set to " + String.format(Locale.ROOT, "%.1f", AutoReconnectMod.getDelaySeconds()) + " seconds.");
            } catch (NumberFormatException e) {
                reply(sender, "Usage: /autoreconnect delay <seconds>");
            }
            return;
        }
        reply(sender, "Usage: /autoreconnect on | off | delay <seconds>");
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
        if ("playtime".equals(timer) && args.length > 0 && "check".equalsIgnoreCase(args[0])) {
            if (!PermissionService.hasCommandPermission("playtime.check", false)) {
                reply(sender, "Access denied: missing permission playtime.check.");
                return;
            }
            reply(sender, "Playtime: " + PlaytimeTrackerHud.formatExactDuration(PlaytimeTrackerHud.getTotalPlaytimeSeconds() * 1000L));
            return;
        }
        if ("playtime".equals(timer) && args.length > 0 && "name".equalsIgnoreCase(args[0])) {
            String name = joinArgs(args, 1);
            if (name.isEmpty()) { reply(sender, "Usage: /playtime name <name>"); return; }
            PlaytimeTrackerHud.setDisplayName(name);
            reply(sender, "Playtime HUD name set to: " + PlaytimeTrackerHud.getDisplayName());
            return;
        }
        if ("playtime".equals(timer) && args.length > 0 && "formatlist".equalsIgnoreCase(args[0])) {
            reply(sender, "Available Playtime HUD formats:");
            reply(sender, "years_months - Years & months");
            reply(sender, "months_days - Months & days");
            reply(sender, "days_hours - Days & hours");
            reply(sender, "hours_minutes - Hours & minutes");
            reply(sender, "minutes_seconds - Minutes & seconds");
            reply(sender, "seconds_millis - Seconds & milliseconds");
            reply(sender, "Use /playtime format <mode> to select one.");
            return;
        }
        if ("playtime".equals(timer) && args.length > 0 && "format".equalsIgnoreCase(args[0])) {
            if (args.length != 2 || !PlaytimeTrackerHud.isValidFormatMode(args[1].toLowerCase(Locale.ROOT))) {
                reply(sender, "Usage: /playtime format <years_months|months_days|days_hours|hours_minutes|minutes_seconds|seconds_millis>");
                return;
            }
            PlaytimeTrackerHud.setFormatMode(args[1]);
            reply(sender, "Playtime format set to: " + PlaytimeTrackerHud.getFormatMode());
            return;
        }
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
                reply(sender, "Usage: /playtime <on|off|toggle|settings|name <name>|format <mode>|check|background <on|off>|color next|position <x> <y>");
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
        PermissionService.Rank rank = PermissionService.getEffectiveRank();
        reply(sender, "AlphaClient commands available to your current rank (" + rank.name() + "):");
        reply(sender, "/alphaclient help | modules | settings <module>");
        reply(sender, "/alphaclient module <module> <on|off|toggle>");
        reply(sender, "/general opens Mods -> General.");
        if (rank == PermissionService.Rank.BETA) {
            reply(sender, "/beta opens Mods -> Beta.");
        }
        if (rank == PermissionService.Rank.DONATOR) {
            reply(sender, "/donator opens Mods -> Donator.");
        }
        if (rank == PermissionService.Rank.SCOUT) {
            reply(sender, "/scout opens Mods -> Scout.");
        }
        if (rank == PermissionService.Rank.ROGUE) {
            reply(sender, "/rogue opens Mods -> Rogue.");
        }
        if (rank == PermissionService.Rank.ELITE) {
            reply(sender, "/elite opens Mods -> Elite.");
        }
        if (rank == PermissionService.Rank.ALPHA) {
            reply(sender, "/alpha opens Mods -> Alpha.");
        }
        if (rank == PermissionService.Rank.ALPHA_PLUS) {
            reply(sender, "/alphaplus opens Mods -> Alpha+.");
        }
        reply(sender, "Available shortcuts: /fpshud /cpshud /playtime /sessiontimer /afktimer /fullbright /nameviewer /legacypvp /quality /hudeditor /profiles /profile <name|create|edit|delete>.");
        if (hasBhopAccess()) {
            reply(sender, "BHOP: /bhop [settings|on|off|toggle|speed|shiftup|name|color].");
        }
        if (hasWTapAccess()) {
            reply(sender, "W-Tap: /wtap [settings|on|off|toggle|delay|cooldown|mode|trigger|target|requirew].");
        }
        if (SupabaseCommunityService.isSignedInFounder()) {
            reply(sender, "Founder-only permission manager: /alphaperms help.");
        }
        if (PermissionService.hasStaffModsAccess()) {
            reply(sender, "StaffMods: /staffmods opens the StaffMods GUI; /staffgeneral opens StaffMods -> General.");
            if (rank == PermissionService.Rank.T_MOD) reply(sender, "Staff folder: /stafftmod.");
            if (rank == PermissionService.Rank.MOD) reply(sender, "Staff folder: /staffmod.");
            if (rank == PermissionService.Rank.SR_MOD) reply(sender, "Staff folder: /staffsrmod.");
            if (rank == PermissionService.Rank.ADMIN) reply(sender, "Staff folder: /staffadmin.");
            if (rank == PermissionService.Rank.SR_ADMIN) reply(sender, "Staff folder: /staffsradmin.");
            if (rank == PermissionService.Rank.PLAT_ADMIN) reply(sender, "Staff folder: /staffplatformadmin.");
            if (rank == PermissionService.Rank.MANAGER) reply(sender, "Staff folder: /staffmanager.");
            if (rank == PermissionService.Rank.OWNER) reply(sender, "Staff folder: /staffowner.");
            if (rank == PermissionService.Rank.FOUNDER) reply(sender, "Staff folder: /stafffounder.");
            if (rank == PermissionService.Rank.PLAT_ADMIN
                    || rank == PermissionService.Rank.MANAGER) {
                reply(sender, "Staff folder: /staffmanagement.");
            }
        }
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
            if (!PermissionService.hasStaffModsAccess()) return options;
            String[] folders = {"general","beta","tmod","mod","srmod","admin","sradmin","management","platformadmin","manager","owner","founder","donator","scout","rogue","elite","alpha","alphaplus"};
            for (String folder : folders) {
                String key = staffFolderPermission(folder);
                Boolean override = PermissionService.getCommandPermissionOverride(key);
                if (Boolean.FALSE.equals(override)) continue;
                if (isStaffFolderCompletionAllowed(folder)) options.add(folder);
            }
        } else if ("profile".equals(commandName) && args.length == 1) {
            options.add("create");
            options.add("edit");
            options.add("delete");
            for (ModuleProfileManager.BhopProfile profile : ModuleProfileManager.getProfiles()) {
                options.add(profile.getProfileName());
            }
        } else if ("profile".equals(commandName) && args.length == 2
                && ("delete".equalsIgnoreCase(args[0]) || "edit".equalsIgnoreCase(args[0]))) {
            for (ModuleProfileManager.BhopProfile profile : ModuleProfileManager.getProfiles()) {
                if ("delete".equalsIgnoreCase(args[0]) || !"Default".equalsIgnoreCase(profile.getProfileName())) {
                    options.add(profile.getProfileName());
                }
            }
        } else if ("alphaclient".equals(commandName)) {
            if (args.length == 1) {
                String[] values = {"help","modules","settings","module","fps","cps","bhop","wtap","fullbright"};
                for (String value : values) if (isCompletionAllowed(commandPermissionForRoute("alphaclient", new String[]{value}))) options.add(value);
            } else if (args.length == 2 && "module".equalsIgnoreCase(args[0])) {
                for (AlphaModule module : AlphaModuleRegistry.getAll()) {
                    if ("staffmods".equals(module.getId()) || "quality".equals(module.getId())) continue;
                    if (isCompletionAllowed(moduleCommandPermission(module.getId()))) options.add(module.getId());
                }
                String[] extra = {"bhop","wtap","fullbright","playtime","sessiontimer","afktimer"};
                for (String value : extra) if (isCompletionAllowed(moduleCommandPermission(value))) options.add(value);
            } else if (args.length == 2 && "settings".equalsIgnoreCase(args[0])) {
                String[] values = {"fps","cps","bhop","wtap","fullbright","playtime","sessiontimer","afktimer","nameviewer","legacypvp","quality"};
                for (String value : values) if (isCompletionAllowed(moduleCommandPermission(value))) options.add(value);
            }
        } else if ("alphaclient:module".equals(commandName) && args.length == 2) {
            options.add("on"); options.add("off"); options.add("toggle");
        } else if ("alphaclient:settings".equals(commandName) && args.length == 1) {
            String[] values = {"fps","cps","bhop","wtap","fullbright","playtime","sessiontimer","afktimer","nameviewer","legacypvp","quality"};
            for (String value : values) if (isCompletionAllowed(moduleCommandPermission(value))) options.add(value);
        }
        return options;
    }

    private boolean isCompletionAllowed(String permissionKey) {
        if (permissionKey == null) return true;
        if ("command.bhop".equals(permissionKey)) return hasBhopAccess();
        if ("command.wtap".equals(permissionKey)) return hasWTapAccess();
        Boolean override = PermissionService.getCommandPermissionOverride(permissionKey);
        if (Boolean.FALSE.equals(override)) return false;
        if (permissionKey.startsWith("staffmods.")) return PermissionService.hasStaffModsAccess();
        return true;
    }

    private boolean isStaffFolderCompletionAllowed(String folder) {
        if ("general".equals(folder)) return PermissionService.hasStaffModsAccess();
        if ("beta".equals(folder) || "donator".equals(folder) || "scout".equals(folder)
                || "rogue".equals(folder) || "elite".equals(folder) || "alpha".equals(folder)
                || "alphaplus".equals(folder)) {
            String key = staffFolderPermission(folder);
            Boolean override = PermissionService.getCommandPermissionOverride(key);
            if (override != null) return override.booleanValue();
            AlphaModule.Category category = regularCategoryAlias(folder);
            return category != null && PermissionService.hasAccess(category.getRequiredRank());
        }
        PermissionService.Rank rank = null;
        if ("tmod".equals(folder)) rank = PermissionService.Rank.T_MOD;
        else if ("mod".equals(folder)) rank = PermissionService.Rank.MOD;
        else if ("srmod".equals(folder)) rank = PermissionService.Rank.SR_MOD;
        else if ("admin".equals(folder)) rank = PermissionService.Rank.ADMIN;
        else if ("sradmin".equals(folder)) rank = PermissionService.Rank.SR_ADMIN;
        else if ("platformadmin".equals(folder)) rank = PermissionService.Rank.PLAT_ADMIN;
        else if ("management".equals(folder) || "manager".equals(folder)) rank = PermissionService.Rank.MANAGER;
        else if ("owner".equals(folder)) rank = PermissionService.Rank.OWNER;
        else if ("founder".equals(folder)) rank = PermissionService.Rank.FOUNDER;
        if (rank == null) return false;
        Boolean override = PermissionService.getCommandPermissionOverride(staffFolderPermission(folder));
        return override != null ? override.booleanValue() : PermissionService.hasAccess(rank);
    }
}
