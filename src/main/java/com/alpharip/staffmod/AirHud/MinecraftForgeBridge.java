package com.alpharip.staffmod.AirHud;

import net.minecraftforge.common.MinecraftForge;

/** Small isolated bridge for the Forge event bus. */
final class MinecraftForgeBridge {
    private MinecraftForgeBridge() { }
    static void register(Object listener) { MinecraftForge.EVENT_BUS.register(listener); }
    static void unregister(Object listener) { MinecraftForge.EVENT_BUS.unregister(listener); }
}
