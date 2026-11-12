package com.alpharip.staffmod.Singleplayer.EasyCraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

/** Small book-icon button intended to be placed inside GuiCrafting by the approved GUI hook. */
public final class EasyCraftTableButton extends GuiButton {
    public EasyCraftTableButton(int id, int x, int y) {
        super(id, x, y, 22, 20, "");
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
        if (!visible) return;
        hovered = mouseX >= xPosition && mouseY >= yPosition
                && mouseX < xPosition + width && mouseY < yPosition + height;
        int background = hovered ? 0xFF777777 : 0xFF3B3B3B;
        drawRect(xPosition, yPosition, xPosition + width, yPosition + height, background);
        drawRect(xPosition, yPosition, xPosition + width, yPosition + 1, 0xFFB8B8B8);
        minecraft.getRenderItem().renderItemAndEffectIntoGUI(new ItemStack(Items.book),
                xPosition + 3, yPosition + 2);
    }
}
