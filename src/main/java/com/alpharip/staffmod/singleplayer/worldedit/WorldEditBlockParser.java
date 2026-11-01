// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.singleplayer.worldedit;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

/** Parses Minecraft block identifiers with optional legacy metadata. */
public final class WorldEditBlockParser {
    private WorldEditBlockParser() { }

    public static IBlockState parse(String input) {
        if (input == null) return null;
        String value = input.trim().toLowerCase(java.util.Locale.ROOT);
        if (!value.matches("[a-z0-9_]+(:[a-z0-9_]+)?(:[0-9]{1,2})?")) return null;
        String[] parts = value.split(":");
        String id = parts.length == 1 ? "minecraft:" + parts[0] : parts[0] + ":" + parts[1];
        Block block = Block.getBlockFromName(id);
        if (block == null) return null;

        int metadata;
        try { metadata = parts.length == 3 ? Integer.parseInt(parts[2]) : 0; }
        catch (NumberFormatException ignored) { return null; }
        if (metadata < 0 || metadata > 15) return null;
        return block.getStateFromMeta(metadata);
    }
}