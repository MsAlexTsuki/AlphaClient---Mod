// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.keystrokes;

import com.alpharip.staffmod.cps.CpsMod;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Mouse;

/** Reads the current local keyboard and mouse state; CPS is read without modifying its source module. */
public final class KeystrokesInput {
    private KeystrokesInput() { }
    public static boolean isForward() { Minecraft mc = Minecraft.getMinecraft(); return mc != null && mc.gameSettings.keyBindForward.isKeyDown(); }
    public static boolean isLeft() { Minecraft mc = Minecraft.getMinecraft(); return mc != null && mc.gameSettings.keyBindLeft.isKeyDown(); }
    public static boolean isBack() { Minecraft mc = Minecraft.getMinecraft(); return mc != null && mc.gameSettings.keyBindBack.isKeyDown(); }
    public static boolean isRight() { Minecraft mc = Minecraft.getMinecraft(); return mc != null && mc.gameSettings.keyBindRight.isKeyDown(); }
    public static boolean isJump() { Minecraft mc = Minecraft.getMinecraft(); return mc != null && mc.gameSettings.keyBindJump.isKeyDown(); }
    public static boolean isLeftMouseDown() { return Mouse.isButtonDown(0); }
    public static boolean isRightMouseDown() { return Mouse.isButtonDown(1); }
    public static int getLeftCps() { return CpsMod.getLeftCps(); }
    public static int getRightCps() { return CpsMod.getRightCps(); }
}
