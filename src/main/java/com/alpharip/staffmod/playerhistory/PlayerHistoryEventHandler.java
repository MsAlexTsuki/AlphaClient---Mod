package com.alpharip.staffmod.playerhistory;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/** Forge client-tick adapter. Registration is deliberately left to the owner. */
public final class PlayerHistoryEventHandler {
    private final PlayerHistory history;

    public PlayerHistoryEventHandler(PlayerHistory history) {
        if (history == null) throw new IllegalArgumentException("history cannot be null");
        this.history = history;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event == null || event.phase != TickEvent.Phase.END) return;
        history.onClientTick();
    }
}
