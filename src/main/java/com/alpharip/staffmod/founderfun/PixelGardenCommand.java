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
    @Override public String getCommandUsage(ICommandSender sender) { return "/pixelgarden [msg <on|off>|open|help|prestige|inventory|upgrades|upgrade <upgrade>|harvesttime <multiplier|finish|normal>|shop|buy|sell|harvest|time|crops|seeds]"; }
    @Override public int getRequiredPermissionLevel() { return 0; }

    @Override public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (!allowed(sender, "commands.pixelgarden")) return;
        String action = args == null || args.length == 0 ? "help" : args[0].toLowerCase(Locale.ROOT);
        PixelGardenGame game = PixelGardenGame.getInstance();
        if ("msg".equals(action)) {
            if (args.length < 2 || (!"on".equalsIgnoreCase(args[1]) && !"off".equalsIgnoreCase(args[1]))) {
                reply(sender, "Usage: /" + commandName + " msg <on|off>");
                return;
            }
            boolean enabled = "on".equalsIgnoreCase(args[1]);
            PixelGardenMessageNotifier.getInstance().setEnabled(enabled);
            reply(sender, enabled ? "Pixel Garden chat reminders enabled." : "Pixel Garden chat reminders disabled.");
        } else if ("prestige".equals(action)) {
            if (!SupabaseCommunityService.isSignedIn()) { reply(sender, "Sign in to your Alpha Account before using Prestige."); return; }
            try {
                com.google.gson.JsonObject state=SupabaseCommunityService.getPixelGardenPrestigeState();
                int count=state.has("prestige_count")?state.get("prestige_count").getAsInt():0;
                int steps=state.has("upgrade_steps")?state.get("upgrade_steps").getAsInt():0;
                if(args.length>=2&&"use".equalsIgnoreCase(args[1])){if(steps<18){reply(sender,"Buy 18 upgrade steps in the Upgrade tab before using Prestige ("+steps+"/18).");return;}open(new PixelGardenPrestigeGUI(null));}
                else if(count<1)reply(sender,"Damn you haven't prestiged yet! You will soon!");
                else if(steps<18)reply(sender,"Prestige upgrades require 18 purchased Upgrade-tab steps ("+steps+"/18).");
                else open(PixelGardenPrestigeUpgradesGUI.forMenu(PixelGardenPrestigeUpgradesGUI.MENU_ID, PixelGardenPrestigeUpgradesGUI.PIXEL_GARDEN_ID, null));
            }catch(Exception e){reply(sender,"Could not load Prestige data: "+safeMessage(e));}
        } else if ("prestigecoingive".equals(action) || "prestigecointake".equals(action)) {
            if(!founderOnly(sender))return;if(args.length<3){reply(sender,"Usage: /pixelgarden "+action+" <Alpha account name> <amount>");return;}
            int amount=number(args[2]);if(amount<1){reply(sender,"Amount must be a positive whole number.");return;}
            runPrestigeFounderAction(sender,"prestigecoingive".equals(action)?"give_prestige_coins":"take_prestige_coins",args[1],amount,0.0D);
        } else if ("prestigecoinreset".equals(action) || "prestigereset".equals(action)) {
            if(!founderOnly(sender))return;if(args.length<2){reply(sender,"Usage: /pixelgarden "+action+" <Alpha account name>");return;}
            runPrestigeFounderAction(sender,"prestigecoinreset".equals(action)?"reset_prestige_coins":"reset_prestige",args[1],0,0.0D);
        } else if ("admin".equals(action)) {
            if(args.length<3||!"fullreset".equalsIgnoreCase(args[1])){reply(sender,"Usage: /pixelgarden admin fullreset <Alpha account name>");return;}
            if(!founderOnly(sender))return;runPrestigeFounderAction(sender,"full_reset",args[2],0,0.0D);
        } else if ("multiplier".equals(action)) {
            if(!founderOnly(sender))return;
            if(args.length>=5&&"prestige".equalsIgnoreCase(args[1])&&"add".equalsIgnoreCase(args[2])){
                double multiplier;try{multiplier=Double.parseDouble(args[4]);}catch(RuntimeException e){reply(sender,"Usage: /pixelgarden multiplier prestige add <Alpha account name> <multiplier>");return;}
                if(multiplier<=0.0D||multiplier>5.0D||Double.isNaN(multiplier)||Double.isInfinite(multiplier)){reply(sender,"Multiplier must be greater than 0 and at most 5.0x.");return;}
                runPrestigeFounderAction(sender,"set_multiplier",args[3],0,multiplier);
            }else if(args.length>=4&&"prestige".equalsIgnoreCase(args[1])&&"reset".equalsIgnoreCase(args[2]))runPrestigeFounderAction(sender,"reset_multiplier",args[3],0,0.0D);
            else reply(sender,"Usage: /pixelgarden multiplier prestige add <Alpha account name> <multiplier> | /pixelgarden multiplier prestige reset <Alpha account name>");
        } else if ("guide".equals(action)) {
            Minecraft mc = Minecraft.getMinecraft();
            open(new PixelGardenHelpGUI(mc == null ? null : mc.currentScreen));
        } else if ("help".equals(action) || "?".equals(action)) {
            Minecraft mc = Minecraft.getMinecraft();
            open(new PixelGardenHelpGUI(mc == null ? null : mc.currentScreen));
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
            if (args.length < 2) {
                reply(sender, "Usage: /pixelgarden upgrade <auto_harvest|growth_boost|expansion|more_coins|more_crops|prestige_yield|prestige_bonus|coin_cache>");
                return;
            }
            String upgrade = args[1].toLowerCase(Locale.ROOT).replace('-', '_');
            if ("auto".equals(upgrade) || "autoharvest".equals(upgrade)) upgrade = "auto_harvest";
            else if ("growth".equals(upgrade) || "growthboost".equals(upgrade)) upgrade = "growth_boost";
            else if ("plot".equals(upgrade)) upgrade = "expansion";

            boolean prestigeUpgrade = "prestige_yield".equals(upgrade)
                    || "prestige_bonus".equals(upgrade) || "coin_cache".equals(upgrade);
            boolean regularUpgrade = "auto_harvest".equals(upgrade) || "growth_boost".equals(upgrade)
                    || "expansion".equals(upgrade) || "more_coins".equals(upgrade) || "more_crops".equals(upgrade);
            if (!regularUpgrade && !prestigeUpgrade) {
                reply(sender, "Unknown upgrade. Choose auto_harvest, growth_boost, expansion, more_coins, more_crops, prestige_yield, prestige_bonus or coin_cache.");
                return;
            }
            try {
                if (prestigeUpgrade) {
                    if (!SupabaseCommunityService.isSignedIn()) {
                        reply(sender, "Sign in to your Alpha Account before buying Prestige upgrades.");
                        return;
                    }
                    com.google.gson.JsonObject state = SupabaseCommunityService.getPixelGardenPrestigeState();
                    int prestigeCount = state.has("prestige_count") ? state.get("prestige_count").getAsInt() : 0;
                    int steps = state.has("upgrade_steps") ? state.get("upgrade_steps").getAsInt() : 0;
                    if (prestigeCount < 1 || steps < 18) {
                        reply(sender, "Prestige upgrades are exclusive to players who have prestiged and completed 18 upgrade steps.");
                        return;
                    }
                    SupabaseCommunityService.purchasePixelGardenPrestigeUpgrade(upgrade);
                    com.google.gson.JsonObject refreshed = SupabaseCommunityService.getPixelGardenPrestigeState();
                    reply(sender, "Purchased Prestige upgrade " + upgrade.replace('_', ' ')
                            + ". Prestige Coins: " + (refreshed.has("prestige_coins")
                            ? refreshed.get("prestige_coins").getAsString() : "unavailable") + ".");
                } else {
                    com.google.gson.JsonObject purchase = SupabaseCommunityService.purchasePixelGardenUpgrade(upgrade);
                    if (purchase.has("save_data") && purchase.get("save_data").isJsonObject()) {
                        game.loadSaveData(purchase.getAsJsonObject("save_data"));
                    }
                    reply(sender, "Purchased " + upgrade.replace('_', ' ') + ". Coins: " + game.getCoins()
                            + ". Upgrade steps: " + purchase.get("upgrade_steps").getAsInt() + "/18.");
                }
            } catch (Exception exception) {
                reply(sender, "Upgrade purchase failed: " + safeMessage(exception));
            }
        } else if ("harvesttime".equals(action)) {
            if (!founderOnly(sender)) return;
            if (args.length < 2) {
                reply(sender, "Usage: /pixelgarden harvesttime <speed multiplier|finish|normal>");
                return;
            }
            long now = System.currentTimeMillis();
            if ("normal".equalsIgnoreCase(args[1])) {
                int restored = game.restoreNormalHarvestTimes(now);
                if (restored > 0) saveCloudAsync(game);
                reply(sender, restored == 0
                        ? "No accelerated crop timers to restore."
                        : "Restored normal harvest timing for " + restored + " crop(s).");
                return;
            }
            boolean finish = "finish".equalsIgnoreCase(args[1]);
            double multiplier = 1.0D;
            if (!finish) {
                String raw = args[1].toLowerCase(Locale.ROOT);
                if (raw.endsWith("x")) raw = raw.substring(0, raw.length() - 1);
                try { multiplier = Double.parseDouble(raw); }
                catch (NumberFormatException exception) {
                    reply(sender, "Use a multiplier such as 2, 10, 100, finish, or normal.");
                    return;
                }
                if (Double.isNaN(multiplier) || Double.isInfinite(multiplier) || multiplier < 1.0D) {
                    reply(sender, "The harvest speed multiplier must be a finite number of at least 1.");
                    return;
                }
            }
            int affected = game.accelerateHarvestTimes(multiplier, finish, now);
            saveCloudAsync(game);
            reply(sender, affected == 0 ? "No growing crops found."
                    : finish ? "Made " + affected + " crop(s) ready to harvest."
                    : "Accelerated harvest timers for " + affected + " crop(s) by " + multiplier + "x.");
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
            reply(sender, "Usage: /pixelgarden [open|inventory|inv|upgrades|upgrade <upgrade>|harvesttime <multiplier|finish>|playtime|shop|buy <crop> [amount]|sell <crop|all> [amount]|harvest <plot|all>|time|crops|seeds]");
        }
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

    private void runPrestigeFounderAction(ICommandSender sender,String action,String target,int amount,double multiplier){
        try{
            com.google.gson.JsonObject r=SupabaseCommunityService.managePixelGardenPrestige(action,target,amount,multiplier);
            String name=r.has("target_alpha_name")?r.get("target_alpha_name").getAsString():target;
            String msg;
            if("give_prestige_coins".equals(action))msg="Gave "+amount+" Prestige Coins to "+name+". New balance: "+r.get("prestige_coins").getAsString()+".";
            else if("take_prestige_coins".equals(action))msg="Took "+amount+" Prestige Coins from "+name+". New balance: "+r.get("prestige_coins").getAsString()+".";
            else if("reset_prestige_coins".equals(action))msg="Reset "+name+"'s Prestige Coins balance to 0.";
            else if("reset_prestige".equals(action))msg="Reset all Prestige progress for "+name+".";
            else if("full_reset".equals(action))msg="Fully reset Pixel Garden and Prestige data for "+name+".";
            else if("set_multiplier".equals(action))msg="Set "+name+"'s personal Prestige multiplier to "+multiplier+"x.";
            else msg="Reset "+name+"'s personal multiplier. Effective multiplier: "+r.get("effective_multiplier").getAsString()+"x.";
            reply(sender,msg);
            if(name.equalsIgnoreCase(SupabaseCommunityService.getSignedInAlphaName())){com.google.gson.JsonObject save=SupabaseCommunityService.loadPixelGardenSave();if(save!=null)PixelGardenGame.getInstance().loadSaveData(save);}
        }catch(Exception e){reply(sender,"Prestige administration failed: "+safeMessage(e));}
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
