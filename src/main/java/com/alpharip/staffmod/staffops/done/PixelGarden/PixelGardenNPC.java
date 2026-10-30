// --- Made by MsAlexTsuki, MsBallNibbler and AlphaClient ---
package com.alpharip.staffmod.founderfun;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry of the existing Pixel Garden NPC identifiers supplied for the project.
 * These are references only: this class does not invent or spawn NPC entities.
 */
public final class PixelGardenNPC {
    public static final String JASMINE = "pixelgarden.npc.jasmine";
    public static final String HANK = "pixelgarden.npc.hank";
    public static final String EMILY = "pixelgarden.npc.emily";
    public static final String SEM = "pixelgarden.npc.sem";
    public static final String CARSON = "pixelgarden.npc.carson";
    public static final String JIMMY = "pixelgarden.npc.jimmy";
    public static final String MACK = "pixelgarden.npc.mack";
    public static final String LIZ = "pixelgarden.npc.liz";
    public static final String TUTORIAL = "pixelgarden.npc.tutorial";

    private static final Map<String, String> NAMES;
    private static final List<String> QUEST_NPCS = Collections.unmodifiableList(Arrays.asList(
            JASMINE, HANK, EMILY, SEM, CARSON, JIMMY, MACK, LIZ));

    static {
        Map<String, String> names = new LinkedHashMap<String, String>();
        names.put(JASMINE, "Jasmine");
        names.put(HANK, "Hank");
        names.put(EMILY, "Emily");
        names.put(SEM, "Sem");
        names.put(CARSON, "Carson");
        names.put(JIMMY, "Jimmy");
        names.put(MACK, "Mack");
        names.put(LIZ, "Liz");
        names.put(TUTORIAL, "Vincent");
        NAMES = Collections.unmodifiableMap(names);
    }

    private PixelGardenNPC() { }

    public static String displayName(String id) {
        if (id == null || id.trim().isEmpty()) return "Not assigned";
        String name = NAMES.get(id.trim());
        return name == null ? id.trim() : name;
    }

    public static List<String> getQuestNpcIds() { return QUEST_NPCS; }

    public static Map<String, String> getAllNpcNames() { return NAMES; }

    /** NPC assigned to each step of the eight-step starter tutorial. */
    public static String tutorialStepNpc(int step) {
        if (step < 0 || step >= QUEST_NPCS.size()) return TUTORIAL;
        return QUEST_NPCS.get(step);
    }
}
