// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.founderfun;

import com.alpharip.staffmod.permissions.SupabaseCommunityService;
import com.google.gson.JsonObject;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

/** Founder-only command for awarding Pixel Garden coins to an Alpha Account. */
public final class FounderGivePixelGardenCoinsCommand extends CommandBase {
    @Override public String getCommandName() { return "pgivecoins"; }
    @Override public String getCommandUsage(ICommandSender sender) { return "/pgivecoins <alphaAccountName> <amount>"; }
    @Override public int getRequiredPermissionLevel() { return 0; }

    @Override public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (!PixelGardenModuleSettings.getInstance().isEnabled()) {
            reply(sender, "Pixel Garden is disabled in AlphaClient Settings.");
            return;
        }
        if (!SupabaseCommunityService.isSignedIn()) {
            reply(sender, "Sign in to your Founder Alpha Account first.");
            return;
        }
        if (!SupabaseCommunityService.isSignedInFounder()) {
            reply(sender, "Access denied: this command is Founder-only.");
            return;
        }
        if (args == null || args.length != 2) {
            reply(sender, "Usage: /pgivecoins <alphaAccountName> <amount>");
            return;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException exception) {
            reply(sender, "Amount must be a whole number between 1 and 1,000,000.");
            return;
        }
        if (amount < 1 || amount > 1000000) {
            reply(sender, "Amount must be between 1 and 1,000,000 coins.");
            return;
        }
        try {
            JsonObject result = SupabaseCommunityService.grantPixelGardenCoins(args[0], amount);
            String target = result.has("alpha_name") ? result.get("alpha_name").getAsString() : args[0];
            long balance = result.has("new_coins") ? result.get("new_coins").getAsLong() : -1L;
            reply(sender, "Granted " + amount + " Pixel Garden coins to " + target
                    + (balance >= 0L ? ". New balance: " + balance + " coins." : "."));
        } catch (Exception exception) {
            String message = exception.getMessage();
            reply(sender, "Coin grant failed: " + (message == null || message.trim().isEmpty()
                    ? "unknown error" : message));
        }
    }

    private static void reply(ICommandSender sender, String message) {
        if (sender != null) sender.addChatMessage(new ChatComponentText("§6[AlphaClient] §f" + message));
    }
}
