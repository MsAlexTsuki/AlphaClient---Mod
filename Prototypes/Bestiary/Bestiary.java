package com.alpharip.staffmod.Bestiary;

/**
 * Standalone Bestiary prototype bootstrap.
 * Call register() explicitly from a future integration point; this class does not
 * register itself with AlphaClient Core.
 */
public final class Bestiary {
    private static final Bestiary INSTANCE = new Bestiary();
    private final BestiaryBook book = new BestiaryBook();
    private final BestiaryTracker tracker = new BestiaryTracker(book);
    private final BestiaryKillEventHandler eventHandler = new BestiaryKillEventHandler(tracker);
    private boolean registered;

    private Bestiary() { }
    public static Bestiary getInstance() { return INSTANCE; }
    public BestiaryBook getBook() { return book; }

    public synchronized void register() {
        if (registered) return;
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(eventHandler);
        registered = true;
    }

    public synchronized void unregister() {
        if (!registered) return;
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(eventHandler);
        registered = false;
    }

    public synchronized boolean isRegistered() { return registered; }
}
