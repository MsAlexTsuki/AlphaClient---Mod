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
        } else if ("quest".equals(action) || "quests".equals(action)) {
            handleQuestCommand(sender, args, game);
        } else if ("createquest".equals(action)) {
            if (!founderOnly(sender)) return;
            if (args.length < 7 || !"admin".equalsIgnoreCase(args[1])) {
                reply(sender, "Usage: /pixelgarden createquest admin <quest id> <description> <NPC> <amount> <reward type>");
                return;
            }
            String questId = args[2].toLowerCase(Locale.ROOT);
            String npc = args[args.length - 3];
            int amount = number(args[args.length - 2]);
            String rewardType = args[args.length - 1].toLowerCase(Locale.ROOT);
            String description = joinArgs(args, 3, args.length - 3);
            if (amount < 1 || description.trim().isEmpty()) {
                reply(sender, "Quest amount must be positive and description cannot be empty.");
                return;
            }
            com.google.gson.JsonObject payload = new com.google.gson.JsonObject();
            payload.addProperty("action", "create");
            payload.addProperty("quest_id", questId);
            payload.addProperty("description", description);
            payload.addProperty("npc", npc);
            payload.addProperty("amount", amount);
            payload.addProperty("reward_type", rewardType);
            runQuestAdminRequest(sender, payload, "Created shared quest " + questId + ".");
        } else if ("givequest".equals(action) || "takequest".equals(action)) {
            if (!founderOnly(sender)) return;
            if (args.length < 4 || !"admin".equalsIgnoreCase(args[1])) {
                reply(sender, "Usage: /pixelgarden " + action + " admin <Alpha account name> <quest id>");
                return;
            }
            com.google.gson.JsonObject payload = new com.google.gson.JsonObject();
            payload.addProperty("action", "givequest".equals(action) ? "give" : "take");
            payload.addProperty("target_alpha_name", args[2]);
            payload.addProperty("quest_id", args[3]);
            runQuestAdminRequest(sender, payload, ("givequest".equals(action) ? "Assigned quest " : "Removed quest ") + args[3] + " for " + args[2] + ".");
        } else if ("completequest".equals(action)) {
            if (!founderOnly(sender)) return;
            if (args.length < 3 || !"admin".equalsIgnoreCase(args[1])) {
                reply(sender, "Usage: /pixelgarden completequest admin <quest id>");
                return;
            }
            com.google.gson.JsonObject payload = new com.google.gson.JsonObject();
            payload.addProperty("action", "complete");
            payload.addProperty("target_alpha_name", SupabaseCommunityService.getSignedInAlphaName());
            payload.addProperty("quest_id", args[2]);
            runQuestAdminRequest(sender, payload, "Completed quest " + args[2] + " on your Founder account.");
        } else if ("rewards".equals(action)) {
            if (!founderOnly(sender)) return;
            if (args.length < 5 || (!"add".equalsIgnoreCase(args[1]) && !"remove".equalsIgnoreCase(args[1]))) {
                reply(sender, "Usage: /pixelgarden rewards <add|remove> <quest id> <type> <amount|all>");
                return;
            }
            boolean remove = "remove".equalsIgnoreCase(args[1]);
            com.google.gson.JsonObject payload = new com.google.gson.JsonObject();
            payload.addProperty("action", remove ? "reward_remove" : "reward_add");
            payload.addProperty("quest_id", args[2]);
            payload.addProperty("reward_type", args[3]);
            if (remove && "all".equalsIgnoreCase(args[4])) payload.addProperty("all", true);
            else {
                int amount = number(args[4]);
                if (amount < 1) { reply(sender, "Reward amount must be a positive whole number or 'all' when removing."); return; }
                payload.addProperty("amount", amount);
            }
            runQuestAdminRequest(sender, payload, (remove ? "Updated rewards for " : "Added rewards to ") + args[2] + ".");
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




    private void handleQuestCommand(ICommandSender sender, String[] args, PixelGardenGame game) {
        if (!SupabaseCommunityService.isSignedIn()) {
            reply(sender, "Sign in to your Alpha Account to use quests.");
            return;
        }
        String sub = args.length >= 2 ? args[1].toLowerCase(Locale.ROOT) : "open";
        PixelGardenProgressionData data = game.getProgressionData();
        data.ensureDefaultCatalog();
        try {
            data.mergeSharedDefinitions(SupabaseCommunityService.getSharedPixelGardenQuests());
        } catch (Exception exception) {
            reply(sender, "Could not sync shared quests: " + safeMessage(exception));
            if (!"open".equals(sub)) return;
        }
        if ("open".equals(sub)) {
            open(new PixelGardenQuestBookGUI(null));
            return;
        }
        List<PixelGardenProgressionData.QuestInstance> instances = new java.util.ArrayList<PixelGardenProgressionData.QuestInstance>(data.getInstances());
        if ("list".equals(sub) || "active".equals(sub)) {
            int shown = 0;
            for (int i = 0; i < instances.size(); i++) {
                PixelGardenProgressionData.QuestInstance instance = instances.get(i);
                if ("active".equals(sub) && instance.getStatus() != PixelGardenProgressionData.QuestStatus.ACTIVE) continue;
                PixelGardenProgressionData.QuestDefinition definition = data.getDefinition(instance.definitionId);
                if (definition == null) continue;
                shown++;
                reply(sender, "#" + (i + 1) + " " + definition.name + " [" + instance.getStatus().name() + "]");
            }
            if (shown == 0) reply(sender, "No " + ("active".equals(sub) ? "active " : "") + "quests found. Open /pixelgarden Quest open to view the Quest Book.");
        } else if ("info".equals(sub)) {
            if (args.length < 3) { reply(sender, "Usage: /pixelgarden Quest info <number>"); return; }
            int index = number(args[2]) - 1;
            if (index < 0 || index >= instances.size()) { reply(sender, "Quest number not found. Use /pixelgarden Quest list."); return; }
            PixelGardenProgressionData.QuestInstance instance = instances.get(index);
            PixelGardenProgressionData.QuestDefinition definition = data.getDefinition(instance.definitionId);
            if (definition == null) { reply(sender, "Quest definition not found."); return; }
            reply(sender, definition.name + " [" + instance.getStatus().name() + "]: " + definition.description);
            if (!definition.npc.isEmpty()) reply(sender, "NPC: " + definition.npc);
            for (PixelGardenProgressionData.Objective objective : definition.objectives)
                reply(sender, objective.type + " " + objective.target + ": " + instance.getObjectiveProgress(objective.id) + "/" + objective.requiredAmount);
            if (!definition.rewards.isEmpty()) reply(sender, "Rewards: " + definition.rewards.toString());
        } else if ("accept".equals(sub)) {
            int index = args.length >= 3 ? number(args[2]) - 1 : firstAvailableQuest(instances);
            if (index < 0 || index >= instances.size()) { reply(sender, "No available quest found. Use /pixelgarden Quest list."); return; }
            PixelGardenProgressionData.QuestInstance instance = instances.get(index);
            final int playerPrestige;
            try {
                com.google.gson.JsonObject prestigeState = SupabaseCommunityService.getPixelGardenPrestigeState();
                playerPrestige = prestigeState != null && prestigeState.has("prestige_count")
                        ? Math.max(0, prestigeState.get("prestige_count").getAsInt()) : 0;
            } catch (Exception exception) {
                reply(sender, "Could not verify your Prestige level, so the quest was not accepted. " + safeMessage(exception));
                return;
            }
            if (data.acceptQuest(instance.instanceId, data.getLevel(), playerPrestige)) {
                saveCloudAsync(game);
                reply(sender, "Accepted quest: " + questName(data, instance) + ".");
            } else {
                reply(sender, "Could not accept that quest. Check its level/prestige requirements and make sure you have fewer than 5 active quests.");
            }
        } else if ("remove".equals(sub)) {
            if (args.length < 3) { reply(sender, "Usage: /pixelgarden Quest Remove <number>"); return; }
            final int index = number(args[2]) - 1;
            if (index < 0 || index >= instances.size()) { reply(sender, "Quest number not found."); return; }
            final PixelGardenProgressionData.QuestInstance target = instances.get(index);
            if (target.getStatus() != PixelGardenProgressionData.QuestStatus.ACTIVE
                    && target.getStatus() != PixelGardenProgressionData.QuestStatus.AVAILABLE) {
                reply(sender, "Only available or active quests can be removed.");
                return;
            }
            Minecraft mc = Minecraft.getMinecraft();
            open(new PixelGardenQuestRemoveConfirmGUI(mc == null ? null : mc.currentScreen, new Runnable() {
                @Override public void run() {
                    if (data.removeQuest(target.instanceId)) {
                        saveCloudAsync(game);
                        reply(sender, "Quest removed. Its rewards will be lost.");
                    } else reply(sender, "Quest could not be removed.");
                }
            }));
        } else if ("givequest".equals(sub)) {
            if (args.length < 5) { reply(sender, "Usage: /pixelgarden Quest givequest <quest number> <crop> <amount>"); return; }
            int index = number(args[2]) - 1;
            PixelGardenGame.Crop crop = crop(args[3]);
            int amount = number(args[4]);
            if (index < 0 || index >= instances.size() || crop == null || amount < 1) {
                reply(sender, "Use a valid quest number, crop and positive amount.");
                return;
            }
            PixelGardenProgressionData.QuestInstance instance = instances.get(index);
            if (instance.getStatus() != PixelGardenProgressionData.QuestStatus.ACTIVE) {
                reply(sender, "Accept this quest before delivering crops.");
                return;
            }
            PixelGardenProgressionData.QuestDefinition definition = data.getDefinition(instance.definitionId);
            String objectiveId = null;
            int nextProgress = 0;
            int deliveryAmount = amount;
            if (definition != null) for (PixelGardenProgressionData.Objective objective : definition.objectives) {
                if ("DELIVER_CROP".equalsIgnoreCase(objective.type)
                        && ("*".equals(objective.target) || crop.name().equalsIgnoreCase(objective.target))) {
                    int currentProgress = instance.getObjectiveProgress(objective.id);
                    deliveryAmount = Math.min(amount, Math.max(0, objective.requiredAmount - currentProgress));
                    objectiveId = objective.id;
                    nextProgress = currentProgress + deliveryAmount;
                    break;
                }
            }
            if (objectiveId == null) { reply(sender, "That quest does not accept delivered crops."); return; }
            if (deliveryAmount < 1) { reply(sender, "This quest's crop objective is already complete."); return; }
            if (game.getInventory(crop) < deliveryAmount) { reply(sender, "You do not have " + deliveryAmount + " harvested " + crop.displayName + "."); return; }
            if (!game.consumeHarvestedCrop(crop, deliveryAmount)) { reply(sender, "Could not consume the crop delivery."); return; }
            if (!data.setObjectiveProgress(instance.instanceId, objectiveId, nextProgress)) {
                game.grantQuestRewards(java.util.Collections.singletonMap("crop_" + crop.name().toLowerCase(Locale.ROOT), deliveryAmount));
                reply(sender, "Could not update quest progress.");
                return;
            }
            PixelGardenProgressionData.QuestDefinition refreshedDefinition = data.getDefinition(instance.definitionId);
            boolean complete = true;
            for (PixelGardenProgressionData.Objective objective : refreshedDefinition.objectives)
                if (data.getInstance(instance.instanceId).getObjectiveProgress(objective.id) < objective.requiredAmount) complete = false;
            if (complete) data.completeQuest(instance.instanceId);
            saveCloudAsync(game);
            reply(sender, "Delivered " + deliveryAmount + " " + crop.displayName + " to " + refreshedDefinition.name + ".");
        } else if ("complete".equals(sub)) {
            if (args.length < 3) { reply(sender, "Usage: /pixelgarden Quest complete <quest number>"); return; }
            int index = number(args[2]) - 1;
            if (index < 0 || index >= instances.size()) { reply(sender, "Quest number not found."); return; }
            PixelGardenProgressionData.QuestInstance instance = instances.get(index);
            PixelGardenProgressionData.QuestDefinition definition = data.getDefinition(instance.definitionId);
            if (definition == null) { reply(sender, "Quest definition not found."); return; }
            if (instance.getStatus() == PixelGardenProgressionData.QuestStatus.ACTIVE) data.completeQuest(instance.instanceId);
            PixelGardenProgressionData.QuestInstance current = data.getInstance(instance.instanceId);
            if (current == null || current.getStatus() != PixelGardenProgressionData.QuestStatus.COMPLETED) {
                reply(sender, "Quest objectives are not complete yet.");
                return;
            }
            if (current.areRewardsClaimed()) { reply(sender, "Quest is already completed and its rewards were claimed."); return; }
            if (!definition.rewards.isEmpty() && !game.grantQuestRewards(definition.rewards)) {
                reply(sender, "Quest completed, but rewards could not be granted. They remain unclaimed.");
                saveCloudAsync(game);
                return;
            }
            data.markRewardsClaimed(instance.instanceId);
            saveCloudAsync(game);
            reply(sender, "Completed " + definition.name + " and claimed its rewards.");
        } else if ("history".equals(sub)) {
            List<PixelGardenProgressionData.HistoryEntry> history = data.getHistory();
            if (history.isEmpty()) reply(sender, "Your quest history is empty.");
            for (PixelGardenProgressionData.HistoryEntry entry : history) {
                PixelGardenProgressionData.QuestDefinition definition = data.getDefinition(entry.definitionId);
                reply(sender, (definition == null ? entry.definitionId : definition.name) + " — " + entry.outcome.name());
            }
        } else if ("progress".equals(sub)) {
            int found = 0;
            for (PixelGardenProgressionData.QuestInstance instance : instances) {
                if (instance.getStatus() != PixelGardenProgressionData.QuestStatus.ACTIVE) continue;
                PixelGardenProgressionData.QuestDefinition definition = data.getDefinition(instance.definitionId);
                if (definition == null) continue;
                for (PixelGardenProgressionData.Objective objective : definition.objectives) {
                    reply(sender, definition.name + ": " + objective.type + " " + objective.target + " "
                            + instance.getObjectiveProgress(objective.id) + "/" + objective.requiredAmount);
                    found++;
                }
            }
            if (found == 0) reply(sender, "You have no active quest progress.");
        } else {
            reply(sender, "Quest commands: open, active, list, info <number>, accept [number], Remove <number>, givequest <number> <crop> <amount>, complete <number>, history, progress.");
        }
    }

    private static int firstAvailableQuest(List<PixelGardenProgressionData.QuestInstance> instances) {
        for (int i = 0; i < instances.size(); i++)
            if (instances.get(i).getStatus() == PixelGardenProgressionData.QuestStatus.AVAILABLE) return i;
        return -1;
    }

    private static String questName(PixelGardenProgressionData data, PixelGardenProgressionData.QuestInstance instance) {
        PixelGardenProgressionData.QuestDefinition definition = data.getDefinition(instance.definitionId);
        return definition == null ? instance.definitionId : definition.name;
    }

    private void runQuestAdminRequest(ICommandSender sender, com.google.gson.JsonObject payload, String successMessage) {
        try {
            com.google.gson.JsonObject result = SupabaseCommunityService.managePixelGardenQuests(payload);
            reply(sender, result.has("error") ? result.get("error").getAsString() : successMessage);
        } catch (Exception exception) {
            reply(sender, "Quest administration failed: " + safeMessage(exception));
        }
    }

    private static String joinArgs(String[] args, int start, int endExclusive) {
        StringBuilder value = new StringBuilder();
        for (int i = start; i < endExclusive; i++) {
            if (value.length() > 0) value.append(' ');
            value.append(args[i]);
        }
        return value.toString();
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
