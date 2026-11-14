package com.alpharip.staffmod.AlphaClans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class Clan {
    private final UUID id;
    private String name;
    private String tag;
    private UUID ownerId;
    private final long createdAt;
    private final Map<UUID, ClanMember> members = new LinkedHashMap<UUID, ClanMember>();
    private final Map<UUID, ClanInvite> invites = new LinkedHashMap<UUID, ClanInvite>();
    private final List<ClanChatMessage> messages = new ArrayList<ClanChatMessage>();

    public Clan(UUID id, String name, String tag, UUID ownerId, long createdAt) {
        if (id == null || ownerId == null) throw new IllegalArgumentException("Clan identifiers are required");
        this.id = id;
        setName(name);
        setTag(tag);
        this.ownerId = ownerId;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getTag() { return tag; }
    public UUID getOwnerId() { return ownerId; }
    public long getCreatedAt() { return createdAt; }
    public Map<UUID, ClanMember> getMembers() {
        return Collections.unmodifiableMap(new LinkedHashMap<UUID, ClanMember>(members));
    }
    public List<ClanInvite> getInvites() {
        return Collections.unmodifiableList(new ArrayList<ClanInvite>(invites.values()));
    }
    public List<ClanChatMessage> getMessages() {
        return Collections.unmodifiableList(new ArrayList<ClanChatMessage>(messages));
    }
    public ClanMember getMember(UUID playerId) { return members.get(playerId); }
    public ClanInvite getInvite(UUID targetId) { return invites.get(targetId); }
    public boolean isMember(UUID playerId) { return members.containsKey(playerId); }

    /** Returns a detached copy so callers cannot mutate service-owned clan state. */
    Clan copy() {
        Clan copy = new Clan(id, name, tag, ownerId, createdAt);
        for (ClanMember member : members.values()) {
            ClanMember memberCopy = new ClanMember(member.getPlayerId(), member.getPlayerName(),
                    member.getRole(), member.getJoinedAt());
            memberCopy.updateLastSeen(member.getLastSeenAt());
            copy.restoreMember(memberCopy);
        }
        for (ClanInvite invite : invites.values()) {
            copy.restoreInvite(new ClanInvite(invite.getClanId(), invite.getInviterId(),
                    invite.getTargetId(), invite.getCreatedAt(), invite.getExpiresAt()));
        }
        for (ClanChatMessage message : messages) {
            copy.restoreMessage(new ClanChatMessage(message.getId(), message.getSenderId(),
                    message.getSenderName(), message.getMessage(), message.getTimestamp()));
        }
        return copy;
    }

    void setName(String value) {
        if (value == null || value.trim().length() < 3 || value.trim().length() > 24) {
            throw new IllegalArgumentException("Clan names must contain 3 to 24 characters");
        }
        name = value.trim();
    }
    void setTag(String value) {
        if (value == null || !value.trim().matches("[A-Za-z0-9]{2,5}")) {
            throw new IllegalArgumentException("Clan tags must contain 2 to 5 letters or numbers");
        }
        tag = value.trim().toUpperCase(java.util.Locale.ROOT);
    }
    void setOwnerId(UUID value) { ownerId = value; }
    void addMember(ClanMember member) {
        if (member == null || members.containsKey(member.getPlayerId())) throw new IllegalArgumentException("Member already exists");
        members.put(member.getPlayerId(), member);
    }
    ClanMember removeMember(UUID playerId) { return members.remove(playerId); }
    void putInvite(ClanInvite invite) { invites.put(invite.getTargetId(), invite); }
    ClanInvite removeInvite(UUID targetId) { return invites.remove(targetId); }
    void removeExpiredInvites(long now) {
        java.util.Iterator<ClanInvite> it = invites.values().iterator();
        while (it.hasNext()) if (it.next().isExpired(now)) it.remove();
    }
    void addMessage(ClanChatMessage message) {
        messages.add(message);
        while (messages.size() > 100) messages.remove(0);
    }
    void restoreMember(ClanMember member) { members.put(member.getPlayerId(), member); }
    void restoreInvite(ClanInvite invite) { invites.put(invite.getTargetId(), invite); }
    void restoreMessage(ClanChatMessage message) { addMessage(message); }
}
