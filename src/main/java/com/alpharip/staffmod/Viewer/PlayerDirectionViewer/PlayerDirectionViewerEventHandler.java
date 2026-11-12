package com.alpharip.staffmod.Viewer.PlayerDirectionViewer;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/** Updates player direction samples at the end of each client tick. */
public final class PlayerDirectionViewerEventHandler {
    private final PlayerDirectionViewer viewer;

    public PlayerDirectionViewerEventHandler(PlayerDirectionViewer viewer) {
        if (viewer == null) throw new IllegalArgumentException("viewer cannot be null");
        this.viewer = viewer;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event == null || event.phase != TickEvent.Phase.END) return;
        viewer.captureVisiblePlayers();
    }
}
