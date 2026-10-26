package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Parses simple @name mentions from text; it does not send messages or notifications. */
public final class InvestigationMentions {
    public List<String> find(String text) {
        List<String> mentions = new ArrayList<String>();
        if (text == null || text.isEmpty()) return mentions;
        for (String token : text.split("\\s+")) {
            if (token.length() > 1 && token.charAt(0) == '@') {
                String name = token.substring(1).replaceAll("[^A-Za-z0-9_\\-]", "");
                if (!name.isEmpty() && !mentions.contains(name)) mentions.add(name);
            }
        }
        return Collections.unmodifiableList(mentions);
    }
}