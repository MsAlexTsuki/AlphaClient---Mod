package com.alpharip.staffmod.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.common.Loader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/** Optional, reflection-only bridge to the Alpha Recording Studio sister addon. */
public final class RecordingStudioAddonBridge {
    private static final String MOD_ID = "alpharecordingstudio";
    private static final String ENTRY_POINT =
            "com.alpharip.staffmod.RecordingStudio.command.RecordingStudioCommand";

    private RecordingStudioAddonBridge() { }

    public static boolean isAvailable() {
        return Loader.isModLoaded(MOD_ID);
    }

    public static void open(Minecraft minecraft) {
        if (minecraft == null || minecraft.thePlayer == null || minecraft.theWorld == null) return;
        if (!isAvailable()) return;

        try {
            Class<?> commandClass = Class.forName(ENTRY_POINT, true,
                    RecordingStudioAddonBridge.class.getClassLoader());
            Method openMethod = commandClass.getMethod("openFromClient");
            openMethod.invoke(null);
        } catch (InvocationTargetException failure) {
            Throwable cause = failure.getCause() == null ? failure : failure.getCause();
            minecraft.thePlayer.addChatMessage(new ChatComponentText(
                    "§6[Recording Studio] §fCould not open: " + cause.getMessage()));
        } catch (ReflectiveOperationException | LinkageError failure) {
            minecraft.thePlayer.addChatMessage(new ChatComponentText(
                    "§6[Recording Studio] §fThe installed sister addon could not be loaded."));
        }
    }
}
