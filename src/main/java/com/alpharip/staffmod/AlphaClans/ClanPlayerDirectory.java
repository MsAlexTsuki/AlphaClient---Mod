package com.alpharip.staffmod.AlphaClans;

import java.util.UUID;

/** Adapter to resolve Minecraft player names to UUIDs; implement this in the client integration layer. */
public interface ClanPlayerDirectory {
    UUID findPlayerId(String playerName);
    String findPlayerName(UUID playerId);
}
