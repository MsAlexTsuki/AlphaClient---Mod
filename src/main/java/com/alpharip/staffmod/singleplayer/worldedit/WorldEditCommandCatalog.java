// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.singleplayer.worldedit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * AlphaClient's independent command inventory for Singleplayer World Edit.
 * This is a command registry, not a claim that every operation is implemented.
 */
public final class WorldEditCommandCatalog {
    private static final List<Category> CATEGORIES = new ArrayList<Category>();

    static {
        add("Biome",
                "biomelist|biomels", "biomeinfo", "setbiome");
        add("Chunk", "chunkinfo", "listchunks", "delchunks");
        add("General", "limit", "fast", "gmask", "toggleplace", "searchitem");
        add("Navigation",
                "unstuck|!", "ascend|asc", "descend|desc", "ceil", "thru",
                "jumpto|j", "up");
        add("Selection",
                "pos1", "pos2", "hpos1", "hpos2", "chunk", "wand",
                "toggleeditwand", "expand", "contract", "shift", "outset",
                "inset", "size", "count", "distr", "sel|desel|deselect");
        add("Region",
                "set", "line", "curve", "replace", "overlay", "center",
                "naturalize", "walls", "faces|outline", "smooth", "move",
                "stack", "regen", "deform", "hollow", "forest", "flora");
        add("Generation",
                "hcyl", "cyl", "hsphere", "sphere", "forestgen", "pumpkins",
                "hpyramid", "pyramid", "generate|gen|g",
                "generatebiome|genbiome|gb");
        add("Clipboard",
                "copy", "cut", "paste", "rotate", "flip", "load", "save",
                "clearclipboard");
        add("History", "undo", "redo", "clearhistory");
        add("Schematic",
                "schematic:load", "schematic:save", "schematic:delete",
                "schematic:formats", "schematic:list");
        add("Snapshot",
                "snapshot:list", "snapshot:use", "snapshot:sel",
                "snapshot:before", "snapshot:after", "restore");
        add("Scripting", "cs", ".s");
        add("Brush",
                "brush:sphere", "brush:cylinder", "brush:clipboard",
                "brush:smooth", "brush:ex", "brush:gravity",
                "brush:butcher", "brush:deform", "brush:set",
                "brush:paint", "brush:apply", "brush:forest",
                "brush:raise", "brush:lower");
        add("Super Pickaxe",
                "superpickaxe:single", "superpickaxe:area",
                "superpickaxe:recursive|recur");
        add("Tool",
                "tool:none", "tool:info", "tool:tree", "tool:repl",
                "tool:cycler", "tool:floodfill", "tool:deltree",
                "tool:farwand", "tool:lrbuild");
        add("Tool utilities",
                ",", "/", "mask", "mat|material", "range", "size");
        add("Utility",
                "fill", "fillr", "drain", "fixlava", "fixwater",
                "removeabove", "removebelow", "removenear", "replacenear",
                "snow", "thaw", "green", "extinguish", "butcher",
                "remove", "calc");
    }

    private WorldEditCommandCatalog() { }

    private static void add(String name, String... commands) {
        CATEGORIES.add(new Category(name, commands));
    }

    public static List<Category> getCategories() {
        return Collections.unmodifiableList(CATEGORIES);
    }

    public static final class Category {
        public final String name;
        private final String[] commands;

        private Category(String name, String[] commands) {
            this.name = name;
            this.commands = commands.clone();
        }

        public String[] getCommands() {
            return commands.clone();
        }
    }
}
