// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

import com.alpharip.alphaclient.AlphaClient;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import com.alpharip.staffmod.permissions.PermissionService;
import com.alpharip.staffmod.permissions.AlphaCommandVisibility;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Read-only help GUI. Entries are filtered against the current effective permissions. */
public final class AlphaHelpGui extends GuiScreen {
    private final GuiScreen parent;
    private final List<Entry> entries = new ArrayList<Entry>();
    private int scrollOffset;
    private int listTop;
    private int listBottom;

    public AlphaHelpGui(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        buttonList.add(new GuiButton(1, 8, height - 30, 70, 20, "Back"));
        rebuildEntries();
    }

    private void rebuildEntries() {
        entries.clear();
        addCommand("Help", "/alphahelp", "command.help", true);
        addCommand("List modules", "/alphaclient modules", "command.modules", true);
        addCommand("Open settings", "/alphaclient settings <module>", "command.settings", true);
        addCommand("Toggle module", "/alphaclient module <module> <on|off|toggle>", "command.modules", true);
        addCommand("Profiles list", "/profiles", "command.profiles.list", true);
        addCommand("Switch profile", "/profile <name>", "command.profiles.switch", true);
        addCommand("Create profile", "/profile create <name>", "command.profiles.create", true);
        addCommand("Edit profile", "/profile edit <old name> <new name>", "command.profiles.edit", true);
        addCommand("Delete profile", "/profile delete <name>", "command.profiles.delete", true);
        addCommand("HUD editor", "/hudeditor", "command.hudeditor", true);
        addCommand("Potion HUD", "/pothud", "command.pothud", true);
        addCommand("AutoGG", "/autogg", "command.autogg", true);
        addCommand("Auto Reconnect", "/autoreconnect", "command.autoreconnect", true);
        addCommand("Open help GUI", "/alphahelp open", "command.alphahelp.open", true);
        addCommand("Pixel Garden chat guide", "/pixelgarden help", "commands.pixelgarden", true);
        addCommand("Open Command Guide GUI", "/pixelgarden guide", "commands.pixelgarden", true);
        addCommand("Open Pixel Garden", "/pixelgarden open", "commands.pixelgarden", true);
        addCommand("Pixel Garden inventory", "/pixelgarden inventory", "commands.pixelgarden", true);
        addCommand("Show crop and seed counts", "/pixelgarden inv", "commands.pixelgarden", true);
        addCommand("Plant a crop", "/pixelgarden plant <plot> <crop>", "commands.pixelgarden", true);
        addCommand("Open garden upgrades", "/pixelgarden upgrades", "pixelgarden.upgrades", true);
        addCommand("Buy a garden upgrade", "/pixelgarden upgrade <upgrade>", "pixelgarden.upgrades", true);
        addCommand("Open garden shop", "/pixelgarden shop", "pixelgarden.shop", true);
        addCommand("Buy seeds", "/pixelgarden buy <crop> [amount]", "pixelgarden.shop", true);
        addCommand("Sell harvested crops", "/pixelgarden sell <crop|all> [amount]", "commands.pixelgarden", true);
        addCommand("Harvest crops", "/pixelgarden harvest <plot|all>", "commands.pixelgarden", true);
        addCommand("Show crop timers", "/pixelgarden time", "commands.pixelgarden", true);
        addCommand("Show harvested crops", "/pixelgarden crops", "commands.pixelgarden", true);
        addCommand("Show seed counts", "/pixelgarden seeds", "commands.pixelgarden", true);
        addCommand("Check garden coins", "/pixelgarden coins [Alpha account]", "commands.pixelgarden", true);
        addCommand("Show reset tokens", "/pixelgarden resettokens", "commands.pixelgarden", true);
        addCommand("Use a reset token", "/pixelgarden resettoken use", "commands.pixelgarden", true);
        addCommand("Show Pixel Garden playtime", "/pixelgarden playtime", "commands.pixelgarden", true);
        addCommand("Prestige Coins and upgrades", "/pixelgarden prestige", "commands.pixelgarden", true);
        addCommand("Perform a Prestige", "/pixelgarden prestige use", "commands.pixelgarden", true);
        addCommand("Pixel Garden alias", "/garden help", "commands.pixelgarden", true);



        if (com.alpharip.staffmod.permissions.SupabaseCommunityService.isSignedInFounder()) {
            addCommand("Give Prestige Coins", "/pixelgarden prestigecoingive <Alpha name> <amount>", "commands.pixelgarden", true);
            addCommand("Take Prestige Coins", "/pixelgarden prestigecointake <Alpha name> <amount>", "commands.pixelgarden", true);
            addCommand("Reset Prestige Coins balance", "/pixelgarden prestigecoinreset <Alpha name>", "commands.pixelgarden", true);
            addCommand("Reset Prestige progress", "/pixelgarden prestigereset <Alpha name>", "commands.pixelgarden", true);
            addCommand("Full Pixel Garden reset", "/pixelgarden admin fullreset <Alpha name>", "commands.pixelgarden", true);
            addCommand("Set Prestige multiplier", "/pixelgarden multiplier prestige add <Alpha name> <multiplier>", "commands.pixelgarden", true);
            addCommand("Reset Prestige multiplier", "/pixelgarden multiplier prestige reset <Alpha name>", "commands.pixelgarden", true);
            addCommand("Give garden coins", "/pixelgarden coinsgive <Alpha account> <amount>", "commands.pixelgarden", true);
            addCommand("Take garden coins", "/pixelgarden takecoins <Alpha account> <amount>", "commands.pixelgarden", true);
            addCommand("Reset garden coins", "/pixelgarden coinsreset <Alpha account>", "commands.pixelgarden", true);
            addCommand("Give a reset token", "/pixelgarden give resettoken <Alpha account>", "commands.pixelgarden", true);
            addCommand("Give a reset token (legacy route)", "/pixelgarden resettoken give <Alpha account>", "commands.pixelgarden", true);
            addCommand("Force-reset a garden", "/pixelgarden resettoken admin <Alpha account>", "commands.pixelgarden", true);
        }

        if (com.alpharip.staffmod.permissions.SupabaseCommunityService.hasStaffOperationsAccess()) {
            addCommand("Staff Operations", "/staffoperations", "Menu.StaffOperations", true);
        }
        if (com.alpharip.staffmod.permissions.SupabaseCommunityService.canUseAlphaNoteCommand()) {
            addCommand("Create private staff note", "/alphanote <player> <note>", "command.alphanote", true);
        }

        addCommand("General menu", "/general", "menu.general", PermissionService.hasAccess(PermissionService.Rank.PUBLIC));
        addCommand("Beta menu", "/beta", "menu.beta", PermissionService.hasAccess(PermissionService.Rank.BETA));
        addCommand("Donator menu", "/donator", "menu.donator", PermissionService.hasAccess(PermissionService.Rank.DONATOR));
        addCommand("Scout menu", "/scout", "menu.scout", PermissionService.hasAccess(PermissionService.Rank.SCOUT));
        addCommand("Rogue menu", "/rogue", "menu.rogue", PermissionService.hasAccess(PermissionService.Rank.ROGUE));
        addCommand("Elite menu", "/elite", "menu.elite", PermissionService.hasAccess(PermissionService.Rank.ELITE));
        addCommand("Alpha menu", "/alpha", "menu.alpha", PermissionService.hasAccess(PermissionService.Rank.ALPHA));
        addCommand("Alpha+ menu", "/alphaplus", "menu.alphaplus", PermissionService.hasAccess(PermissionService.Rank.ALPHA_PLUS));

        addCommand("FPS HUD", "/fpshud", "command.fpshud", true);
        addCommand("CPS HUD", "/cpshud", "command.cpshud", true);
        addCommand("Playtime", "/playtime", "command.playtime", true);
        addCommand("Session timer", "/sessiontimer", "command.sessiontimer", true);
        addCommand("AFK timer", "/afktimer", "command.afktimer", true);
        addCommand("Fullbright", "/fullbright", "command.fullbright", true);
        addCommand("Name Viewer", "/nameviewer", "command.nameviewer", true);
        addCommand("Legacy PvP", "/legacypvp", "command.legacypvp", true);
        addCommand("Quality settings", "/quality", "command.quality", true);
        addCommand("BHOP", "/bhop", "command.bhop", PermissionService.hasAccess(PermissionService.Rank.T_MOD));
        addCommand("W-Tap", "/wtap", "command.wtap", PermissionService.hasAccess(PermissionService.Rank.T_MOD));

        if (PermissionService.hasStaffModsAccess()) {
            addCommand("StaffMods menu", "/staffmods", "staffmods.open", true);
            addCommand("StaffMods General", "/staffgeneral", "staffmods.general", true);
            addCommand("StaffMods T-Mod", "/stafftmod", "staffmods.tmod", PermissionService.hasAccess(PermissionService.Rank.T_MOD));
            addCommand("StaffMods Mod", "/staffmod", "staffmods.mod", PermissionService.hasAccess(PermissionService.Rank.MOD));
            addCommand("StaffMods Sr-Mod", "/staffsrmod", "staffmods.srmod", PermissionService.hasAccess(PermissionService.Rank.SR_MOD));
            addCommand("StaffMods Admin", "/staffadmin", "staffmods.admin", PermissionService.hasAccess(PermissionService.Rank.ADMIN));
            addCommand("StaffMods Sr-Admin", "/staffsradmin", "staffmods.sradmin", PermissionService.hasAccess(PermissionService.Rank.SR_ADMIN));
            addCommand("StaffMods Management", "/staffmanagement", "staffmods.management", PermissionService.hasAccess(PermissionService.Rank.PLAT_ADMIN));
            addCommand("StaffMods Platform Admin", "/staffplatformadmin", "staffmods.platformadmin", PermissionService.hasAccess(PermissionService.Rank.PLAT_ADMIN));
            addCommand("StaffMods Manager", "/staffmanager", "staffmods.manager", PermissionService.hasAccess(PermissionService.Rank.MANAGER));
            addCommand("StaffMods Owner", "/staffowner", "staffmods.owner", PermissionService.hasAccess(PermissionService.Rank.OWNER));
            if (PermissionService.hasAccess(PermissionService.Rank.FOUNDER)) {
                addCommand("Founder permission manager", "/alphaperms open", "staffmods.founder", true);
            }
        }

        for (AlphaModule module : AlphaModuleRegistry.getAll()) {
            if (module != null && module.isUnlocked()) {
                addCommand(module.getName(), "/alphaclient module " + module.getId() + " <on|off|toggle>",
                        "command.modules", true);
            }
        }
        scrollOffset = 0;
    }

    private void addCommand(String title, String command, String key, boolean fallback) {
        if (AlphaCommandVisibility.canShow(command) && PermissionService.hasCommandPermission(key, fallback)) {
            entries.add(new Entry(title, command));
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button != null && button.id == 1) mc.displayGuiScreen(parent);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            mc.displayGuiScreen(parent);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            scrollOffset += wheel > 0 ? -3 : 3;
            int visible = Math.max(1, (listBottom - listTop) / 22);
            scrollOffset = Math.max(0, Math.min(scrollOffset, Math.max(0, entries.size() - visible)));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "AlphaClient Help", width / 2, 12, 0xFFFFFF);
        drawCenteredString(fontRendererObj, "Commands currently allowed for your account", width / 2, 27, 0xC8C8C8);
        int left = Math.max(8, width / 2 - 210);
        int right = Math.min(width - 8, width / 2 + 210);
        listTop = 52;
        listBottom = height - 40;
        drawRect(left, listTop - 2, right, listBottom, 0xAA111111);
        int visible = Math.max(0, (listBottom - listTop) / 22);
        for (int i = 0; i < visible && i + scrollOffset < entries.size(); i++) {
            Entry entry = entries.get(i + scrollOffset);
            int y = listTop + i * 22;
            if ((i & 1) == 1) drawRect(left + 1, y, right - 1, y + 21, 0x33222222);
            drawString(fontRendererObj, trim(entry.title, 145), left + 7, y + 3, 0xFFD45A);
            drawString(fontRendererObj, trim(entry.command, right - left - 165), left + 158, y + 3, 0xEEEEEE);
        }
        if (entries.isEmpty()) drawCenteredString(fontRendererObj, "No commands are currently available.", width / 2, listTop + 10, 0xAAAAAA);
        drawString(fontRendererObj, "Scroll to view more. Press ESC or Back to close.", 88, height - 24, 0xAAAAAA);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private String trim(String value, int maxPixels) {
        String result = value == null ? "" : value;
        while (!result.isEmpty() && fontRendererObj.getStringWidth(result) > maxPixels) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    private static final class Entry {
        private final String title;
        private final String command;
        private Entry(String title, String command) {
            this.title = title;
            this.command = command;
        }
    }
}
