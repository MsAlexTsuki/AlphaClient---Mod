package com.alpharip.staffmod.staffops.ideas;

/**
 * Foundation for DynamicFps; configuration/state only.
 * Reduces client rendering or update work while Minecraft is unfocused or minimized. Runtime performance hooks are not implemented.
 */
public class DynamicFps {
    public boolean enabled = false;
    public String category = "Performance";
    public String style = "default";
}
