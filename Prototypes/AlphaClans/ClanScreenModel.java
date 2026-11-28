package com.alpharip.staffmod.AlphaClans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** UI-ready snapshot/controller. A Minecraft GuiScreen renderer can consume this without owning clan rules. */
public final class ClanScreenModel {
    private final ClanService service;

    public ClanScreenModel(ClanService service) {
        if (service == null) throw new IllegalArgumentException("Clan service is required");
        this.service = service;
    }

    public Snapshot createSnapshot(UUID playerId) {
        Clan clan = service.getClanForPlayer(playerId);
        if (clan == null) {
            List<InviteView> invites = new ArrayList<InviteView>();
            for (ClanInvite invite : service.getInvitesFor(playerId)) {
                Clan invitedClan = service.getClan(invite.getClanId());
                if (invitedClan != null) invites.add(new InviteView(invitedClan.getId(), invitedClan.getName(), invitedClan.getTag(), invite.getExpiresAt()));
            }
            return new Snapshot(null, null, Collections.<MemberView>emptyList(),
                    Collections.<MessageView>emptyList(), invites, false, false);
        }

        List<MemberView> members = new ArrayList<MemberView>();
        for (ClanMember member : clan.getMembers().values()) {
            members.add(new MemberView(member.getPlayerId(), member.getPlayerName(), member.getRole(), member.getJoinedAt()));
        }
        List<MessageView> messages = new ArrayList<MessageView>();
        for (ClanChatMessage message : clan.getMessages()) {
            messages.add(new MessageView(message.getSenderName(), message.getMessage(), message.getTimestamp()));
        }
        return new Snapshot(clan.getId(), clan.getName() + " [" + clan.getTag() + "]",
                members, messages, Collections.<InviteView>emptyList(),
                service.hasPermission(clan.getId(), playerId, ClanPermission.INVITE),
                service.hasPermission(clan.getId(), playerId, ClanPermission.EDIT_SETTINGS));
    }

    public static final class Snapshot {
        private final UUID clanId;
        private final String title;
        private final List<MemberView> members;
        private final List<MessageView> messages;
        private final List<InviteView> invites;
        private final boolean canInvite;
        private final boolean canEdit;
        private Snapshot(UUID clanId, String title, List<MemberView> members, List<MessageView> messages,
                         List<InviteView> invites, boolean canInvite, boolean canEdit) {
            this.clanId = clanId; this.title = title;
            this.members = Collections.unmodifiableList(new ArrayList<MemberView>(members));
            this.messages = Collections.unmodifiableList(new ArrayList<MessageView>(messages));
            this.invites = Collections.unmodifiableList(new ArrayList<InviteView>(invites));
            this.canInvite = canInvite; this.canEdit = canEdit;
        }
        public UUID getClanId() { return clanId; }
        public String getTitle() { return title; }
        public List<MemberView> getMembers() { return members; }
        public List<MessageView> getMessages() { return messages; }
        public List<InviteView> getInvites() { return invites; }
        public boolean canInvite() { return canInvite; }
        public boolean canEdit() { return canEdit; }
        public boolean hasClan() { return clanId != null; }
    }

    public static final class MemberView {
        private final UUID playerId; private final String name; private final ClanRole role; private final long joinedAt;
        MemberView(UUID playerId, String name, ClanRole role, long joinedAt) {
            this.playerId = playerId; this.name = name; this.role = role; this.joinedAt = joinedAt;
        }
        public UUID getPlayerId() { return playerId; }
        public String getName() { return name; }
        public ClanRole getRole() { return role; }
        public long getJoinedAt() { return joinedAt; }
    }
    public static final class MessageView {
        private final String sender; private final String message; private final long timestamp;
        MessageView(String sender, String message, long timestamp) {
            this.sender = sender; this.message = message; this.timestamp = timestamp;
        }
        public String getSender() { return sender; }
        public String getMessage() { return message; }
        public long getTimestamp() { return timestamp; }
    }
    public static final class InviteView {
        private final UUID clanId; private final String name; private final String tag; private final long expiresAt;
        InviteView(UUID clanId, String name, String tag, long expiresAt) {
            this.clanId = clanId; this.name = name; this.tag = tag; this.expiresAt = expiresAt;
        }
        public UUID getClanId() { return clanId; }
        public String getName() { return name; }
        public String getTag() { return tag; }
        public long getExpiresAt() { return expiresAt; }
    }
}
