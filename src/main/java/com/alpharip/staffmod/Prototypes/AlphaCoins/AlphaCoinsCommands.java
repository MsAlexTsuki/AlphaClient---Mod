package com.alpharip.staffmod.AlphaCoins;

import com.alpharip.staffmod.AlphaCoins.CORE.AlphaCoinsCore;
import com.alpharip.staffmod.AlphaCoins.AlphaCoinsDailyChallenges.ChallengeMetric;
import com.alpharip.staffmod.AlphaCoins.AlphaCoinsDailyChallenges.DailyChallengeManager;
import com.alpharip.staffmod.AlphaCoins.AlphaCoinsDailyChallenges.DailyChallengeProgress;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Prototype command handler for AlphaCoins.
 *
 * This class deliberately has no Minecraft command registration. The command framework
 * can adapt its sender and account services when the feature is integrated into the client.
 */
public final class AlphaCoinsCommands {
    private static final long NORMAL_GIVE_LIMIT = 200L;
    private static final long OWNER_GIVE_TAKE_LIMIT = 1200L;
    private static final long NORMAL_GIVE_WINDOW_MS = 48L * 60L * 60L * 1000L;

    private final AccountGateway accounts;
    private final DailyChallengeManager challenges;
    private final Map<String, Deque<GiveRecord>> recentGives =
            new HashMap<String, Deque<GiveRecord>>();

    public AlphaCoinsCommands(AccountGateway accounts, DailyChallengeManager challenges) {
        if (accounts == null) throw new IllegalArgumentException("Account gateway is required.");
        this.accounts = accounts;
        this.challenges = challenges;
    }

    /**
     * Execute a command after the command framework has removed the leading command label.
     * Supported labels: acoins, alphacoins, and alphaclient:alphacoins.
     */
    public void execute(String commandLabel, String[] args, CommandSender sender) {
        if (sender == null) return;
        String label = commandLabel == null ? "" : commandLabel.toLowerCase();
        if (!isSupportedLabel(label)) {
            sender.sendMessage("Unknown AlphaCoins command.");
            return;
        }

        String[] input = args == null ? new String[0] : args;
        if (input.length == 0) {
            showOwnBalance(sender);
            return;
        }

        String subcommand = input[0].toLowerCase();
        if ("challenges".equals(subcommand) || "challanges".equals(subcommand)) {
            showChallenges(sender);
        } else if ("check".equals(subcommand)) {
            checkBalance(input, sender);
        } else if ("give".equals(subcommand)) {
            giveCoins(input, sender);
        } else if ("take".equals(subcommand)) {
            takeCoins(input, sender);
        } else if ("challengesc".equals(subcommand)) {
            testChallenge(input, sender);
        } else {
            showHelp(sender);
        }
    }

    private void showOwnBalance(CommandSender sender) {
        AlphaCoinsCore core = AlphaCoinsCore.getInstance();
        if (!core.isLoaded()) {
            sender.sendMessage("Your AlphaCoins wallet is not loaded.");
            return;
        }
        sender.sendMessage("Your AlphaCoins balance: " + core.getService().getBalance());
    }

    private void showChallenges(CommandSender sender) {
        if (challenges == null || !challenges.isLoaded()) {
            sender.sendMessage("Daily Challenges are not loaded for the active profile.");
            return;
        }

        List<DailyChallengeProgress> current = challenges.getChallenges();
        sender.sendMessage("Daily Challenges:");
        for (DailyChallengeProgress progress : current) {
            sender.sendMessage("- " + progress.getDefinition().getTitle()
                    + " [" + progress.getDefinition().getId() + "] "
                    + progress.getProgress() + "/" + progress.getDefinition().getTarget()
                    + " | Reward: " + progress.getDefinition().getReward()
                    + " AlphaCoins | "
                    + (progress.isClaimed() ? "Claimed"
                    : progress.isComplete() ? "Complete - not claimed" : "In progress"));
        }
    }

    private void checkBalance(String[] args, CommandSender sender) {
        if (args.length != 2 || isBlank(args[1])) {
            sender.sendMessage("Usage: /alphacoins check <alpha account>");
            return;
        }

        String account = args[1].trim();
        if (!accounts.accountExists(account)) {
            sender.sendMessage("Alpha account not found: " + account);
            return;
        }
        sender.sendMessage(account + "'s AlphaCoins balance: " + accounts.getBalance(account));
    }

    private void giveCoins(String[] args, CommandSender sender) {
        boolean ownerMode = args.length == 4 && "OW".equalsIgnoreCase(args[1]);
        if (ownerMode) {
            if (!sender.isOwnerOrFounder()) {
                sender.sendMessage("Only Owner and Founder can use the OW giveaway command.");
                return;
            }
            transferCoins(args[2], args[3], sender, true, false);
            return;
        }

        if (args.length != 3) {
            sender.sendMessage("Usage: /alphacoins give <alpha account> <amount>");
            sender.sendMessage("Usage: /alphacoins give OW <alpha account> <amount>");
            return;
        }
        transferCoins(args[1], args[2], sender, false, false);
    }

    private void takeCoins(String[] args, CommandSender sender) {
        if (args.length != 4 || !"OW".equalsIgnoreCase(args[1])) {
            sender.sendMessage("Usage: /alphacoins take OW <alpha account> <amount>");
            return;
        }
        if (!sender.isOwnerOrFounder()) {
            sender.sendMessage("Only Owner and Founder can use the OW take command.");
            return;
        }
        transferCoins(args[2], args[3], sender, true, true);
    }

    private void transferCoins(String account, String rawAmount, CommandSender sender,
                               boolean ownerMode, boolean remove) {
        if (isBlank(account)) {
            sender.sendMessage("An Alpha account is required.");
            return;
        }

        long amount;
        try {
            amount = Long.parseLong(rawAmount);
        } catch (Exception ignored) {
            sender.sendMessage("Enter a valid AlphaCoins amount.");
            return;
        }
        if (amount <= 0L) {
            sender.sendMessage("The amount must be higher than zero.");
            return;
        }
        if (ownerMode && amount > OWNER_GIVE_TAKE_LIMIT) {
            sender.sendMessage("The amount must be lower.");
            return;
        }
        if (!accounts.accountExists(account)) {
            sender.sendMessage("Alpha account not found: " + account);
            return;
        }

        if (!ownerMode && !remove) {
            long remaining = getNormalGiveRemaining(sender.getAlphaAccount());
            if (amount > remaining) {
                sender.sendMessage("You can give " + remaining
                        + " more AlphaCoins during the current 48-hour period.");
                return;
            }
        }

        boolean success;
        if (remove) {
            success = accounts.removeCoins(account, amount, "Owner/Founder AlphaCoins adjustment");
        } else {
            String reason = ownerMode
                    ? "Owner/Founder AlphaCoins giveaway"
                    : "AlphaCoins community transfer";
            success = accounts.addCoins(account, amount, reason);
        }

        if (!success) {
            sender.sendMessage(remove
                    ? "Could not remove AlphaCoins. Check the account balance and storage."
                    : "Could not add AlphaCoins to that account.");
            return;
        }

        if (!ownerMode && !remove) recordNormalGive(sender.getAlphaAccount(), amount);
        sender.sendMessage((remove ? "Removed " : "Gave ") + amount
                + " AlphaCoins " + (remove ? "from " : "to ") + account + ".");
    }

    private synchronized long getNormalGiveRemaining(String issuer) {
        if (isBlank(issuer)) return 0L;
        Deque<GiveRecord> records = getGiveRecords(issuer);
        pruneExpired(records, System.currentTimeMillis());
        long used = 0L;
        for (GiveRecord record : records) {
            if (record.amount > Long.MAX_VALUE - used) return 0L;
            used += record.amount;
        }
        return Math.max(0L, NORMAL_GIVE_LIMIT - used);
    }

    private synchronized void recordNormalGive(String issuer, long amount) {
        if (isBlank(issuer)) return;
        Deque<GiveRecord> records = getGiveRecords(issuer);
        long now = System.currentTimeMillis();
        pruneExpired(records, now);
        records.addLast(new GiveRecord(amount, now));
    }

    private Deque<GiveRecord> getGiveRecords(String issuer) {
        Deque<GiveRecord> records = recentGives.get(issuer.toLowerCase());
        if (records == null) {
            records = new ArrayDeque<GiveRecord>();
            recentGives.put(issuer.toLowerCase(), records);
        }
        return records;
    }

    private static void pruneExpired(Deque<GiveRecord> records, long now) {
        while (!records.isEmpty()
                && now - records.peekFirst().timestamp >= NORMAL_GIVE_WINDOW_MS) {
            records.removeFirst();
        }
    }

    /**
     * Test-only command: simulates the remaining progress for one active daily challenge,
     * then checks whether the normal challenge flow also marks it as claimed automatically.
     * It intentionally does not call DailyChallengeManager.claim(), because that would hide
     * whether automatic claiming is actually implemented.
     */
    private void testChallenge(String[] args, CommandSender sender) {
        if (args.length != 2 || isBlank(args[1])) {
            sender.sendMessage("Usage: /alphacoins challengesc <task-id>");
            return;
        }
        if (challenges == null || !challenges.isLoaded()) {
            sender.sendMessage("Daily Challenges are not loaded for the active profile.");
            return;
        }

        String taskId = args[1].trim();
        DailyChallengeProgress selected = findChallenge(taskId);
        if (selected == null) {
            sender.sendMessage("Daily challenge not found: " + taskId);
            return;
        }
        if (selected.isClaimed()) {
            sender.sendMessage("Challenge is already complete and claimed. No extra reward was issued.");
            return;
        }

        if (!selected.isComplete()) {
            int remaining = selected.getDefinition().getTarget() - selected.getProgress();
            challenges.record(selected.getDefinition().getMetric(), remaining);
        }

        DailyChallengeProgress afterTest = findChallenge(taskId);
        if (afterTest == null || !afterTest.isComplete()) {
            sender.sendMessage("Challenge test failed: progress did not reach the target.");
            return;
        }
        if (afterTest.isClaimed()) {
            sender.sendMessage("Challenge test passed: completed and automatically claimed.");
        } else {
            sender.sendMessage("Challenge completed, but it was NOT automatically claimed.");
            sender.sendMessage("The current DailyChallengeManager.record() flow does not claim rewards automatically.");
        }
    }

    private DailyChallengeProgress findChallenge(String taskId) {
        List<DailyChallengeProgress> current = challenges.getChallenges();
        for (DailyChallengeProgress progress : current) {
            if (progress.getDefinition().getId().equalsIgnoreCase(taskId)) return progress;
        }
        return null;
    }

    private static void showHelp(CommandSender sender) {
        sender.sendMessage("AlphaCoins commands:");
        sender.sendMessage("/acoins");
        sender.sendMessage("/alphacoins");
        sender.sendMessage("/alphaclient:Alphacoins");
        sender.sendMessage("/acoins challenges");
        sender.sendMessage("/alphacoins challanges");
        sender.sendMessage("/acoins check <alpha account>");
        sender.sendMessage("/alphacoins check <alpha account>");
        sender.sendMessage("/acoins give <alpha account> <amount>");
        sender.sendMessage("/alphacoins give <alpha account> <amount>");
        sender.sendMessage("/alphacoins give OW <alpha account> <amount>");
        sender.sendMessage("/alphacoins take OW <alpha account> <amount>");
        sender.sendMessage("/alphacoins challengesc <task-id>");
    }

    private static boolean isSupportedLabel(String label) {
        return "acoins".equals(label)
                || "alphacoins".equals(label)
                || "alphaclient:alphacoins".equals(label);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /** Implemented by the eventual command adapter; no Supabase dependency is assumed here. */
    public interface CommandSender {
        String getAlphaAccount();
        boolean isOwnerOrFounder();
        void sendMessage(String message);
    }

    /**
     * Account lookup and remote-account mutations are intentionally abstract.
     * Implementations must return false for unknown accounts or failed persistence.
     */
    public interface AccountGateway {
        boolean accountExists(String alphaAccount);
        long getBalance(String alphaAccount);
        boolean addCoins(String alphaAccount, long amount, String reason);
        boolean removeCoins(String alphaAccount, long amount, String reason);
    }

    private static final class GiveRecord {
        private final long amount;
        private final long timestamp;

        private GiveRecord(long amount, long timestamp) {
            this.amount = amount;
            this.timestamp = timestamp;
        }
    }
}
