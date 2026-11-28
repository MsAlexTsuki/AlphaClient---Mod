package com.alpharip.staffmod.AlphaClans;

import java.util.UUID;

public final class ClanChatMessage {
    private final UUID id;
    private final UUID senderId;
    private final String senderName;
    private final String message;
    private final long timestamp;

    public ClanChatMessage(UUID id, UUID senderId, String senderName, String message, long timestamp) {
        if (id == null || senderId == null || senderName == null || senderName.trim().isEmpty()
                || message == null || message.trim().isEmpty() || message.trim().length() > 256) {
            throw new IllegalArgumentException("Invalid clan message");
        }
        this.id = id;
        this.senderId = senderId;
        this.senderName = senderName.trim();
        this.message = message.trim();
        this.timestamp = timestamp;
    }

    public UUID getId() { return id; }
    public UUID getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }
}
