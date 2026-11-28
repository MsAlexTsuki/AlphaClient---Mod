package com.alpharip.staffmod.Viewer.playerconnectionviewer;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/** Samples locally available connection state once per client tick. */
public final class PlayerConnectionViewerEventHandler {
    private final PlayerConnectionViewer viewer;

    public PlayerConnectionViewerEventHandler(PlayerConnectionViewer viewer) {
        if (viewer == null) throw new IllegalArgumentException("viewer cannot be null");
        this.viewer = viewer;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event == null || event.phase != TickEvent.Phase.END) return;
        viewer.captureCurrentClientState();
    }
}
