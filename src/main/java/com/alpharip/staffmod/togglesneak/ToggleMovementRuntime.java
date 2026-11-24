package com.alpharip.staffmod.togglesneak;

import net.minecraftforge.common.MinecraftForge;

/** Explicit lifecycle owner; a separately approved integration must start this runtime. */
public final class ToggleMovementRuntime {
    private final ToggleSneak toggleSneak = new ToggleSneak();
    private final ToggleSprint toggleSprint = new ToggleSprint();
    private final ToggleMovementKeyHandler keyHandler =
            new ToggleMovementKeyHandler(toggleSneak, toggleSprint);
    private boolean running;

    public void start() {
        if (running) return;
        ToggleMovementKeyBindings.register();
        MinecraftForge.EVENT_BUS.register(keyHandler);
        running = true;
    }

    public void stop() {
        if (!running) return;
        keyHandler.releaseMovementKeys();
        toggleSneak.reset();
        toggleSprint.reset();
        MinecraftForge.EVENT_BUS.unregister(keyHandler);
        running = false;
    }

    public boolean isRunning() { return running; }
    public ToggleSneak getToggleSneak() { return toggleSneak; }
    public ToggleSprint getToggleSprint() { return toggleSprint; }
}
