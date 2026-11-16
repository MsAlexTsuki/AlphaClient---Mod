package com.alpharip.staffmod.Viewer.ItemNbtViewer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** One typed entry in the held item's NBT tree. */
public final class NbtTreeNode {
    private final String name, type, value, rawValue;
    private final List<NbtTreeNode> children;
    private boolean expanded;

    public NbtTreeNode(String name, String type, String value, String rawValue,
                       List<NbtTreeNode> children, boolean expanded) {
        this.name = name == null ? "" : name;
        this.type = type == null ? "Unknown" : type;
        this.value = value == null ? "" : value;
        this.rawValue = rawValue == null ? "" : rawValue;
        this.children = Collections.unmodifiableList(new ArrayList<NbtTreeNode>(
                children == null ? Collections.<NbtTreeNode>emptyList() : children));
        this.expanded = expanded;
    }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getValue() { return value; }
    public String getRawValue() { return rawValue; }
    public List<NbtTreeNode> getChildren() { return children; }
    public boolean hasChildren() { return !children.isEmpty(); }
    public boolean isExpanded() { return expanded; }
    public void toggleExpanded() { if (hasChildren()) expanded = !expanded; }
}
