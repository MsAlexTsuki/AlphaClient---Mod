package com.alpharip.staffmod.Viewer.ItemNbtViewer;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Converts Minecraft 1.8.9 NBT data into a bounded, display-friendly tree. */
public final class NbtTreeBuilder {
    private static final int MAX_DEPTH = 16, MAX_NODES = 500;
    private int nodeCount;

    public NbtTreeNode build(NBTTagCompound compound) {
        nodeCount = 0;
        return compound == null ? null : buildNode("Item", compound, 0);
    }

    private NbtTreeNode buildNode(String name, NBTBase tag, int depth) {
        if (tag == null) return leaf(name, "Unknown", "", "");
        nodeCount++;
        String type = typeName(tag.getId());
        String raw = tag.toString();
        String value = raw;
        List<NbtTreeNode> children = new ArrayList<NbtTreeNode>();

        if (tag instanceof NBTTagCompound) {
            NBTTagCompound compound = (NBTTagCompound) tag;
            Set<String> keys = compound.getKeySet();
            value = keys.size() + " entries";
            if (depth < MAX_DEPTH) {
                for (String key : keys) {
                    if (nodeCount >= MAX_NODES) {
                        children.add(leaf("…", "Limit", "Additional entries omitted", ""));
                        break;
                    }
                    children.add(buildNode(key, compound.getTag(key), depth + 1));
                }
            } else if (!keys.isEmpty()) {
                children.add(leaf("…", "Limit", "Maximum tree depth reached", ""));
            }
        } else if (tag instanceof NBTTagList) {
            NBTTagList list = (NBTTagList) tag;
            value = list.tagCount() + " entries";
            if (depth < MAX_DEPTH) {
                for (int i = 0; i < list.tagCount(); i++) {
                    if (nodeCount >= MAX_NODES) {
                        children.add(leaf("…", "Limit", "Additional entries omitted", ""));
                        break;
                    }
                    children.add(buildNode("[" + i + "]", list.get(i), depth + 1));
                }
            } else if (list.tagCount() > 0) {
                children.add(leaf("…", "Limit", "Maximum tree depth reached", ""));
            }
        }
        return new NbtTreeNode(name, type, value, raw, children, depth == 0);
    }

    private NbtTreeNode leaf(String name, String type, String value, String raw) {
        return new NbtTreeNode(name, type, value, raw,
                new ArrayList<NbtTreeNode>(), false);
    }

    private String typeName(int id) {
        switch (id) {
            case 1: return "Byte";
            case 2: return "Short";
            case 3: return "Int";
            case 4: return "Long";
            case 5: return "Float";
            case 6: return "Double";
            case 7: return "Byte Array";
            case 8: return "String";
            case 9: return "List";
            case 10: return "Compound";
            case 11: return "Int Array";
            default: return "End";
        }
    }
}
