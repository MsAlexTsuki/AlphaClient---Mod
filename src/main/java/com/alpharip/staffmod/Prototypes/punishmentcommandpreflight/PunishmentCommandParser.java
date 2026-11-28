package com.alpharip.staffmod.punishmentcommandpreflight;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/** Parses configured command formats without changing the command that will be sent. */
public final class PunishmentCommandParser {
    public PunishmentCommandDraft parse(String outgoingMessage, Collection<PunishmentCommandRule> rules) {
        if (outgoingMessage == null || rules == null || rules.isEmpty()) return null;
        String command = outgoingMessage.trim();
        if (!command.startsWith("/")) return null;
        List<String> tokens = tokenize(command.substring(1));
        if (tokens.isEmpty()) return null;

        String commandName = tokens.get(0).toLowerCase(Locale.ROOT);
        List<String> arguments = tokens.subList(1, tokens.size());
        for (PunishmentCommandRule rule : rules) {
            if (rule == null || !rule.getCommandName().equals(commandName)) continue;
            int targetIndex = rule.getTargetArgument();
            int durationIndex = rule.getDurationArgument();
            int reasonIndex = rule.getReasonStartArgument();
            if (targetIndex >= arguments.size() || durationIndex >= arguments.size() || reasonIndex >= arguments.size()) return null;

            StringBuilder reason = new StringBuilder();
            for (int i = reasonIndex; i < arguments.size(); i++) {
                if (reason.length() > 0) reason.append(' ');
                reason.append(arguments.get(i));
            }
            if (reason.length() == 0) return null;
            return new PunishmentCommandDraft(command, commandName, arguments.get(targetIndex), arguments.get(durationIndex), reason.toString());
        }
        return null;
    }

    /** Supports quoted arguments and escaped quote/backslash characters. */
    private List<String> tokenize(String input) {
        List<String> result = new ArrayList<String>();
        StringBuilder token = new StringBuilder();
        boolean quoted = false, escaping = false, tokenStarted = false;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (escaping) {
                token.append(c);
                escaping = false;
                tokenStarted = true;
            } else if (c == 92) {
                escaping = true;
                tokenStarted = true;
            } else if (c == '"') {
                quoted = !quoted;
                tokenStarted = true;
            } else if (Character.isWhitespace(c) && !quoted) {
                if (tokenStarted) {
                    result.add(token.toString());
                    token.setLength(0);
                    tokenStarted = false;
                }
            } else {
                token.append(c);
                tokenStarted = true;
            }
        }
        if (escaping) token.append((char) 92);
        if (tokenStarted) result.add(token.toString());
        if (quoted) return new ArrayList<String>();
        return result;
    }
}
