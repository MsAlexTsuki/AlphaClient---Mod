package com.alpharip.staffmod.AlphaClans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ClanCommand {
    private final String name;
    private final List<String> arguments;

    public ClanCommand(String name, List<String> arguments) {
        this.name = name;
        this.arguments = Collections.unmodifiableList(new ArrayList<String>(arguments));
    }
    public String getName() { return name; }
    public List<String> getArguments() { return arguments; }
    public String argument(int index) { return index >= 0 && index < arguments.size() ? arguments.get(index) : null; }
    public String joinedArguments(int start) {
        StringBuilder value = new StringBuilder();
        for (int i = start; i < arguments.size(); i++) {
            if (value.length() > 0) value.append(' ');
            value.append(arguments.get(i));
        }
        return value.toString();
    }
}
