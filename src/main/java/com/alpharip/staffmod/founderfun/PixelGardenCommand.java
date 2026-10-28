// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.founderfun;

import com.alpharip.alphaclient.AlphaClient;
import com.alpharip.staffmod.hud.PlaytimeTrackerHud;
import com.alpharip.staffmod.permissions.PermissionService;
import com.alpharip.staffmod.permissions.SupabaseCommunityService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import java.util.List;
import java.util.Locale;

/** Dedicated Pixel Garden command handler; AlphaClient remains the core mod. */
public final class PixelGardenCommand extends CommandBase {
    private final String commandName;
    public PixelGardenCommand(String commandName) { this.commandName = commandName; }
    @Override public String getCommandName() { return commandName; }
    @Override public String getCommandUsage(ICommandSender sender) { return "/pixelgarden [open|help|inventory|inv|plant|upgrades|upgrade|playtime|shop|buy|sell|harvest|time|crops|seeds]"; }
    @Override public int getRequiredPermissionLevel() { return 0; }

    @Override public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (!allowed(sender, "commands.pixelgarden")) return;
        String action = args == null || args.length == 0 ? "help" : args[0].toLowerCase(Locale.ROOT);
        PixelGardenGame game = PixelGardenGame.getInstance();
        if ("help".equals(action) || "?".equals(action)) {
            showCommandGuide(sender);
        } else if ("open".equals(action)) {
            open(new PixelGardenGUI(null));
        } else if ("inventory".equals(action)) {
            open(new PixelGardenInventoryGUI(null));
        } else if ("inv".equals(action)) {
            showInventory(sender, game);
        } else if ("upgrades".equals(action)) {
            if (allowed(sender, "pixelgarden.upgrades")) open(new PixelGardenUpgradeGUI(null));
        } else if ("shop".equals(action)) {
            if (allowed(sender, "pixelgarden.shop")) open(new PixelGardenShopGUI(null));
        } else if ("resettokens".equals(action)) {
            try { reply(sender, "You have " + SupabaseCommunityService.getPixelGardenResetTokens() + " reset token(s). Use /pixelgarden resettoken use to reset your garden."); }
            catch (Exception exception) { reply(sender, "Could not read reset tokens: " + safeMessage(exception)); }
        } else if ("resettoken".equals(action)) {
            if (args.length >= 2 && "use".equalsIgnoreCase(args[1])) {
                if (!SupabaseCommunityService.isSignedIn()) { reply(sender, "Sign in to your Alpha Account before using a reset token."); return; }
                try {
                    if (SupabaseCommunityService.getPixelGardenResetTokens() < 1) { reply(sender, "You do not have a reset token."); return; }
                    open(new PixelGardenResetGUI(null));
                } catch (Exception exception) { reply(sender, "Could not verify reset tokens: " + safeMessage(exception)); }
            } else if (args.length >= 3 && "admin".equalsIgnoreCase(args[1])) {
                if (!founderOnly(sender)) return;
                runFounderAction(sender, "force_reset", args[2], 0);
            } else if (args.length >= 4 && "give".equalsIgnoreCase(args[1]) && "resettoken".equalsIgnoreCase(args[2])) {
                if (!founderOnly(sender)) return;
                runFounderAction(sender, "give_token", args[3], 0);
            } else {
                reply(sender, "Usage: /pixelgarden resettokens | /pixelgarden resettoken use | /pixelgarden resettoken admin <Alpha account> | /pixelgarden give resettoken <Alpha account>");
            }
        } else if ("give".equals(action)) {
            if (args.length >= 3 && "resettoken".equalsIgnoreCase(args[1])) {
                if (!founderOnly(sender)) return;
                runFounderAction(sender, "give_token", args[2], 0);
            } else {
                reply(sender, "Usage: /pixelgarden give resettoken <Alpha account>");
            }
        } else if ("coins".equals(action)) {
            String target = args.length >= 2 ? args[1] : null;
            try {
                com.google.gson.JsonObject result = SupabaseCommunityService.getPixelGardenCoins(target);
                reply(sender, result.get("alpha_name").getAsString() + " has " + result.get("coins").getAsInt() + " Pixel Garden coins.");
            } catch (Exception exception) { reply(sender, "Could not read coins: " + safeMessage(exception)); }
        } else if ("coinsgive".equals(action)) {
            if (!founderOnly(sender)) return;
            if (args.length < 3) { reply(sender, "Usage: /pixelgarden coinsgive <Alpha account> <amount>"); return; }
            int amount = number(args[2]);
            if (amount < 1) { reply(sender, "Amount must be a positive whole number."); return; }
            runFounderAction(sender, "give_coins", args[1], amount);
        } else if ("takecoins".equals(action)) {
            if (!founderOnly(sender)) return;
            if (args.length < 3) { reply(sender, "Usage: /pixelgarden takecoins <Alpha account> <amount>"); return; }
            int amount = number(args[2]);
            if (amount < 1) { reply(sender, "Amount must be a positive whole number."); return; }
            runFounderAction(sender, "take_coins", args[1], amount);
        } else if ("coinsreset".equals(action)) {
            if (!founderOnly(sender)) return;
            if (args.length < 2) { reply(sender, "Usage: /pixelgarden coinsreset <Alpha account>"); return; }
            runFounderAction(sender, "reset_coins", args[1], 0);
        } else if ("playtime".equals(action)) {
            reply(sender, "Your AlphaClient playtime: " + duration(PlaytimeTrackerHud.getTotalPlaytimeSeconds()) + ".");
        } else if ("upgrade".equals(action)) {
            if (!allowed(sender, "pixelgarden.upgrades")) return;
            if (args.length < 2) { reply(sender, "Usage: /pixelgarden upgrade <auto_harvest|growth_boost|expansion>"); return; }
            String upgrade = args[1].toLowerCase(Locale.ROOT).replace('-', '_');
            boolean ok;
            if ("auto".equals(upgrade) || "autoharvest".equals(upgrade) || "auto_harvest".equals(upgrade)) {
                ok = game.buyAutoHarvest(); upgrade = "auto harvest";
            } else if ("growth".equals(upgrade) || "growthboost".equals(upgrade) || "growth_boost".equals(upgrade)) {
                ok = game.buyGrowthBoost(); upgrade = "growth boost";
            } else if ("plot".equals(upgrade) || "expansion".equals(upgrade)) {
                ok = game.expandGarden(); upgrade = "plot expansion";
            } else { reply(sender, "Unknown upgrade. Choose auto_harvest, growth_boost or expansion."); return; }
            reply(sender, ok ? "Purchased " + upgrade + ". Coins: " + game.getCoins() + "." : "Upgrade failed: already owned, max level reached, or insufficient coins.");
        } else if ("buy".equals(action)) {
            if (!allowed(sender, "pixelgarden.shop")) return;
            if (args.length < 2) { reply(sender, "Usage: /pixelgarden buy <wheat|carrot|potato|pumpkin> [amount]"); return; }
            PixelGardenGame.Crop crop = crop(args[1]);
            int amount = args.length > 2 ? number(args[2]) : 1;
            if (crop == null || amount < 1) { reply(sender, "Choose wheat, carrot, potato or pumpkin and a positive amount."); return; }
            reply(sender, game.buySeeds(crop, amount) ? "Bought " + amount + " " + crop.displayName + " seed(s)." : "Purchase failed: insufficient coins or invalid amount.");
        } else if ("sell".equals(action)) {
            if (!allowed(sender, "pixelgarden.shop")) return;
            if (args.length < 2) { reply(sender, "Usage: /pixelgarden sell <crop|all> [amount]"); return; }
            if ("all".equalsIgnoreCase(args[1])) {
                int sold = 0;
                for (PixelGardenGame.Crop crop : PixelGardenGame.Crop.values()) {
                    int count = game.getInventory(crop);
                    if (count > 0 && game.sell(crop, count)) sold += count;
                }
                reply(sender, sold == 0 ? "You have no harvested crops to sell." : "Sold " + sold + " crop(s). Coins: " + game.getCoins() + ".");
            } else {
                PixelGardenGame.Crop crop = crop(args[1]);
                if (crop == null) { reply(sender, "Unknown crop. Choose wheat, carrot, potato or pumpkin."); return; }
                int amount = args.length > 2 ? number(args[2]) : game.getInventory(crop);
                reply(sender, amount > 0 && game.sell(crop, amount) ? "Sold " + amount + " " + crop.displayName + ". Coins: " + game.getCoins() + "." : "Nothing sold: check your harvested inventory and amount.");
            }
        } else if ("plant".equals(action)) {
            if (args.length < 3) { reply(sender, "Usage: /pixelgarden plant <plot> <wheat|carrot|potato|pumpkin>"); return; }
            int plotNumber = number(args[1]);
            PixelGardenGame.Crop crop = crop(args[2]);
            if (plotNumber < 1) { reply(sender, "Plot numbers start at 1."); return; }
            if (crop == null) { reply(sender, "Choose wheat, carrot, potato or pumpkin."); return; }
            boolean planted = game.plant(plotNumber - 1, crop, System.currentTimeMillis());
            if (planted) {
                reply(sender, "Planted " + crop.displayName + " on plot " + plotNumber + ". Seeds left: " + game.getSeeds(crop) + ".");
                saveCloudAsync(game);
            } else {
                reply(sender, "Could not plant on plot " + plotNumber + ": it may be locked, occupied, invalid, or you may have no seeds.");
            }
        } else if ("harvest".equals(action)) {
            if (args.length < 2) { reply(sender, "Usage: /pixelgarden harvest <plot number|all>"); return; }
            long now = System.currentTimeMillis();
            if ("all".equalsIgnoreCase(args[1])) {
                int count = 0; List<PixelGardenGame.Plot> plots = game.getPlots();
                for (int i = 0; i < plots.size(); i++) if (game.harvest(i, now)) count++;
                reply(sender, count == 0 ? "No crops are ready to harvest." : "Harvested " + count + " plot(s).");
            } else {
                int plot = number(args[1]);
                if (plot < 1) { reply(sender, "Plot numbers start at 1."); return; }
                reply(sender, game.harvest(plot - 1, now) ? "Harvested plot " + plot + "." : "Plot " + plot + " is locked, empty, growing, or does not exist.");
            }
        } else if ("time".equals(action)) {
            List<PixelGardenGame.Plot> plots = game.getPlots(); long now = System.currentTimeMillis(); int found = 0;
            for (int i = 0; i < plots.size(); i++) {
                PixelGardenGame.Plot plot = plots.get(i);
                if (plot.getState(now) == PixelGardenGame.PlotState.GROWING) {
                    reply(sender, "Plot " + (i + 1) + ": " + duration((plot.getRemainingMillis(now) + 999L) / 1000L) + " until harvest."); found++;
                } else if (plot.getState(now) == PixelGardenGame.PlotState.READY) {
                    reply(sender, "Plot " + (i + 1) + ": ready to harvest."); found++;
                }
            }
            if (found == 0) reply(sender, "No crops are growing or ready.");
        } else if ("crops".equals(action)) {
            for (PixelGardenGame.Crop crop : PixelGardenGame.Crop.values()) reply(sender, crop.displayName + ": " + game.getInventory(crop) + " harvested.");
        } else if ("seeds".equals(action)) {
            for (PixelGardenGame.Crop crop : PixelGardenGame.Crop.values()) reply(sender, crop.displayName + " seeds: " + game.getSeeds(crop) + ".");
        } else {
            reply(sender, "Usage: /pixelgarden [open|inventory|inv|upgrades|upgrade <upgrade>|playtime|shop|buy <crop> [amount]|sell <crop|all> [amount]|harvest <plot|all>|time|crops|seeds]");
        }
    }


    private void showCommandGuide(ICommandSender sender) {
        reply(sender, "§ePixel Garden Command Guide");
        reply(sender, "/pixelgarden open §7- Open Pixel Garden.");
        reply(sender, "/pixelgarden inventory §7- Open the inventory screen.");
        reply(sender, "/pixelgarden inv §7- Show crop and planting-seed counts in chat.");
        reply(sender, "/pixelgarden plant <plot> <crop> §7- Plant a crop on an unlocked empty plot.");
        reply(sender, "/pixelgarden upgrades §7- Open the upgrades GUI.");
        reply(sender, "/pixelgarden upgrade <auto_harvest|growth_boost|expansion> §7- Buy an upgrade.");
        reply(sender, "/pixelgarden playtime §7- Show your AlphaClient playtime.");
        reply(sender, "/pixelgarden shop §7- Open the shop.");
        reply(sender, "/pixelgarden buy <wheat|carrot|potato|pumpkin> [amount] §7- Buy seeds.");
        reply(sender, "/pixelgarden sell <crop> [amount] §7- Sell harvested crops.");
        reply(sender, "/pixelgarden sell all §7- Sell all harvested crops.");
        reply(sender, "/pixelgarden harvest <plot number|all> §7- Harvest a ready plot or all ready plots.");
        reply(sender, "/pixelgarden time §7- Show crop growth timers.");
        reply(sender, "/pixelgarden crops §7- Show harvested crop counts.");
        reply(sender, "/pixelgarden seeds §7- Show seed counts by crop.");
        reply(sender, "/pixelgarden coins [Alpha account] §7- Show a coin balance.");
        reply(sender, "/pixelgarden resettokens §7- Show your reset token count.");
        reply(sender, "/pixelgarden resettoken use §7- Open the reset confirmation popup.");
        if (SupabaseCommunityService.isSignedInFounder()) {
            reply(sender, "/pixelgarden coinsgive <Alpha account> <amount> §7- Give coins (Founder only).");
            reply(sender, "/pixelgarden takecoins <Alpha account> <amount> §7- Remove coins (Founder only).");
            reply(sender, "/pixelgarden coinsreset <Alpha account> §7- Reset coins (Founder only).");
            reply(sender, "/pixelgarden give resettoken <Alpha account> §7- Give a reset token (Founder only).");
            reply(sender, "/pixelgarden resettoken admin <Alpha account> §7- Force a full reset (Founder only).");
        }
        reply(sender, "/garden §7- Alias for /pixelgarden; use /garden open or /garden help.");
    }


    private static void showInventory(ICommandSender sender, PixelGardenGame game) {
        reply(sender, "§ePixel Garden Inventory");
        reply(sender, "§6Planting seeds:");
        for (PixelGardenGame.Crop crop : PixelGardenGame.Crop.values()) {
            reply(sender, crop.displayName + " seeds: " + game.getSeeds(crop) + ".");
        }
        reply(sender, "§6Harvested crops:");
        for (PixelGardenGame.Crop crop : PixelGardenGame.Crop.values()) {
            reply(sender, crop.displayName + ": " + game.getInventory(crop) + ".");
        }
        reply(sender, "Use /pixelgarden plant <plot> <crop> to plant a seed.");
    }

    private static void saveCloudAsync(final PixelGardenGame game) {
        if (!SupabaseCommunityService.isSignedIn()) return;
        final String account = SupabaseCommunityService.getSignedInEmail();
        final com.google.gson.JsonObject snapshot = game.toSaveData();
        Thread thread = new Thread(new Runnable() {
            @Override public void run() {
                try {
                    if (SupabaseCommunityService.isSignedIn()
                            && (account == null || account.equals(SupabaseCommunityService.getSignedInEmail()))) {
                        SupabaseCommunityService.savePixelGardenSave(snapshot);
                    }
                } catch (Exception exception) {
                    System.err.println("[AlphaClient] Pixel Garden command save failed: " + exception.getMessage());
                }
            }
        }, "AlphaClient-PixelGardenCommandSave");
        thread.setDaemon(true);
        thread.start();
    }

    private boolean founderOnly(ICommandSender sender) {
        if (SupabaseCommunityService.isSignedInFounder()) return true;
        reply(sender, "Access denied: this command is restricted to Founder.");
        return false;
    }

    private void runFounderAction(ICommandSender sender, String action, String target, int amount) {
        try {
            com.google.gson.JsonObject result = SupabaseCommunityService.managePixelGarden(action, target, amount);
            String name = result.has("target_alpha_name") ? result.get("target_alpha_name").getAsString() : target;
            if (name.equalsIgnoreCase(SupabaseCommunityService.getSignedInAlphaName())) {
                com.google.gson.JsonObject refreshed = SupabaseCommunityService.loadPixelGardenSave();
                if (refreshed != null) PixelGardenGame.getInstance().loadSaveData(refreshed);
            }
            if ("force_reset".equals(action)) reply(sender, "Force-reset the full Pixel Garden for " + name + ".");
            else if ("give_token".equals(action)) reply(sender, "Gave 1 reset token to " + name + ".");
            else if ("reset_coins".equals(action)) reply(sender, "Reset " + name + "'s Pixel Garden coins to 0.");
            else if ("take_coins".equals(action)) reply(sender, "Removed coins from " + name + ". New balance: " + result.get("coins").getAsInt() + ".");
            else if ("give_coins".equals(action)) reply(sender, "Gave " + amount + " coins to " + name + ". New balance: " + result.get("coins").getAsInt() + ".");
        } catch (Exception exception) { reply(sender, "Pixel Garden action failed: " + safeMessage(exception)); }
    }

    private static String safeMessage(Exception exception) {
        return exception.getMessage() == null ? "unknown error" : exception.getMessage();
    }

    private boolean allowed(ICommandSender sender, String permission) {
        if (PermissionService.hasCommandPermission(permission, true)) return true;
        reply(sender, "Access denied: missing " + permission + " permission.");
        return false;
    }
    private static void open(GuiScreen screen) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null) AlphaClient.openGuiFromCommand(screen);
    }
    private static PixelGardenGame.Crop crop(String name) {
        for (PixelGardenGame.Crop c : PixelGardenGame.Crop.values())
            if (c.name().equalsIgnoreCase(name) || c.displayName.equalsIgnoreCase(name)) return c;
        return null;
    }
    private static int number(String value) {
        try { return Integer.parseInt(value); } catch (NumberFormatException ignored) { return -1; }
    }
    private static String duration(long s) {
        long d=s/86400L, h=(s%86400L)/3600L, m=(s%3600L)/60L, sec=s%60L;
        if (d>0) return d+"d "+h+"h "+m+"m";
        if (h>0) return h+"h "+m+"m "+sec+"s";
        if (m>0) return m+"m "+sec+"s";
        return sec+"s";
    }
    private static void reply(ICommandSender sender, String message) {
        if (sender != null) sender.addChatMessage(new ChatComponentText("§6[Pixel Garden] §f" + message));
    }
}
