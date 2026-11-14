package com.alpharip.staffmod.AlphaClans;

import java.util.ArrayList;
import java.util.List;

public final class ClanCommandParser {
    public ClanCommand parse(String input) {
        if (input == null) throw new IllegalArgumentException("Command is empty.");
        String text = input.trim();
        if (text.startsWith("/")) text = text.substring(1);
        List<String> tokens = tokenize(text);
        if (tokens.isEmpty()) throw new IllegalArgumentException("Command is empty.");
        if ("clan".equalsIgnoreCase(tokens.get(0))) tokens.remove(0);
        if (tokens.isEmpty()) throw new IllegalArgumentException("Use /clan help.");
        String name = tokens.remove(0).toLowerCase(java.util.Locale.ROOT);
        return new ClanCommand(name, tokens);
    }

    private List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<String>();
        StringBuilder token = new StringBuilder();
        boolean quoted = false;
        boolean escaping = false;
        boolean started = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (escaping) { token.append(c); escaping = false; started = true; }
            else if (c == '\\' && quoted) { escaping = true; }
            else if (c == '"') { quoted = !quoted; started = true; }
            else if (Character.isWhitespace(c) && !quoted) {
                if (started) { tokens.add(token.toString()); token.setLength(0); started = false; }
            } else { token.append(c); started = true; }
        }
        if (escaping || quoted) throw new IllegalArgumentException("Unclosed quote or escape.");
        if (started) tokens.add(token.toString());
        return tokens;
    }
}
