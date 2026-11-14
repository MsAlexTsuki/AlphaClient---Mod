package com.alpharip.staffmod.AlphaClans;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ClanService {
    public static final int MAX_MEMBERS = 50;
    public static final long INVITE_LIFETIME_MS = 7L * 24L * 60L * 60L * 1000L;
    private final ClanRepository repository;
    private final Map<UUID, Clan> clans = new LinkedHashMap<UUID, Clan>();
    private final Map<UUID, UUID> clanByPlayer = new HashMap<UUID, UUID>();

    public ClanService(ClanRepository repository) throws IOException {
        if (repository == null) throw new IllegalArgumentException("Clan repository is required");
        this.repository = repository;
        for (Clan clan : repository.loadAll()) {
            if (clan == null) throw new IOException("Clan storage contains an empty clan record");
            if (clans.put(clan.getId(), clan) != null) throw new IOException("Duplicate clan id in storage");
            if (clan.getMembers().isEmpty() || clan.getMembers().size() > MAX_MEMBERS) {
                throw new IOException("Clan storage contains an invalid member count");
            }
            ClanMember owner = clan.getMember(clan.getOwnerId());
            if (owner == null || owner.getRole() != ClanRole.OWNER) {
                throw new IOException("Clan storage has no matching owner member");
            }
            int ownerCount = 0;
            for (ClanMember member : clan.getMembers().values()) {
                if (member.getRole() == ClanRole.OWNER) ownerCount++;
                if (clanByPlayer.put(member.getPlayerId(), clan.getId()) != null) {
                    throw new IOException("A player is assigned to more than one clan");
                }
            }
            if (ownerCount != 1) throw new IOException("Clan storage must contain exactly one owner");
            for (ClanInvite invite : clan.getInvites()) {
                if (!clan.getId().equals(invite.getClanId())) {
                    throw new IOException("Clan storage contains an invite for another clan");
                }
            }
        }
    }

    public synchronized ClanResult createClan(UUID ownerId, String ownerName, String name, String tag) throws IOException {
        if (ownerId == null || ownerName == null || ownerName.trim().isEmpty()) return ClanResult.failure("A valid player is required.");
        if (clanByPlayer.containsKey(ownerId)) return ClanResult.failure("You are already in a clan.");
        try {
            Clan clan = new Clan(UUID.randomUUID(), name, tag, ownerId, System.currentTimeMillis());
            clan.addMember(new ClanMember(ownerId, ownerName, ClanRole.OWNER, System.currentTimeMillis()));
            clans.put(clan.getId(), clan);
            clanByPlayer.put(ownerId, clan.getId());
            persist();
            return ClanResult.success("Clan " + clan.getName() + " created.", clan);
        } catch (IllegalArgumentException ex) {
            return ClanResult.failure(ex.getMessage());
        } catch (IOException ex) {
            clans.remove(clanByPlayer.remove(ownerId));
            throw ex;
        }
    }

    public synchronized ClanResult invite(UUID inviterId, UUID targetId) throws IOException {
        Clan clan = getClanForPlayerInternal(inviterId);
        if (clan == null || !hasPermission(clan, inviterId, ClanPermission.INVITE)) return ClanResult.failure("You cannot invite players.");
        if (targetId == null || targetId.equals(inviterId)) return ClanResult.failure("Choose another player.");
        if (clanByPlayer.containsKey(targetId)) return ClanResult.failure("That player is already in a clan.");
        if (clan.getMembers().size() >= MAX_MEMBERS) return ClanResult.failure("This clan has reached its member limit.");
        long now = System.currentTimeMillis();
        clan.removeExpiredInvites(now);
        if (clan.getInvite(targetId) != null) return ClanResult.failure("That player already has a pending invite.");
        clan.putInvite(new ClanInvite(clan.getId(), inviterId, targetId, now, now + INVITE_LIFETIME_MS));
        persist();
        return ClanResult.success("Clan invite sent.", clan);
    }

    public synchronized ClanResult acceptInvite(UUID playerId, UUID clanId, String playerName) throws IOException {
        if (playerId == null || playerName == null || playerName.trim().isEmpty()) return ClanResult.failure("A valid player is required.");
        if (clanByPlayer.containsKey(playerId)) return ClanResult.failure("You are already in a clan.");
        Clan clan = clans.get(clanId);
        if (clan == null) return ClanResult.failure("Clan not found.");
        ClanInvite invite = clan.getInvite(playerId);
        if (invite == null || invite.isExpired(System.currentTimeMillis())) {
            clan.removeInvite(playerId);
            persist();
            return ClanResult.failure("No valid invite was found.");
        }
        if (clan.getMembers().size() >= MAX_MEMBERS) return ClanResult.failure("This clan has reached its member limit.");
        clan.removeInvite(playerId);
        clan.addMember(new ClanMember(playerId, playerName, ClanRole.MEMBER, System.currentTimeMillis()));
        clanByPlayer.put(playerId, clan.getId());
        persist();
        return ClanResult.success("You joined " + clan.getName() + ".", clan);
    }

    public synchronized ClanResult declineInvite(UUID playerId, UUID clanId) throws IOException {
        Clan clan = clans.get(clanId);
        if (clan == null || clan.getInvite(playerId) == null) return ClanResult.failure("Invite not found.");
        clan.removeInvite(playerId);
        persist();
        return ClanResult.success("Clan invite declined.", clan);
    }

    public synchronized ClanResult leave(UUID playerId) throws IOException {
        Clan clan = getClanForPlayerInternal(playerId);
        if (clan == null) return ClanResult.failure("You are not in a clan.");
        if (playerId.equals(clan.getOwnerId())) {
            if (clan.getMembers().size() > 1) return ClanResult.failure("Transfer ownership or disband the clan before leaving.");
            clans.remove(clan.getId());
            clanByPlayer.remove(playerId);
            persist();
            return ClanResult.success("Your empty clan was disbanded.", null);
        }
        clan.removeMember(playerId);
        clanByPlayer.remove(playerId);
        persist();
        return ClanResult.success("You left " + clan.getName() + ".", clan);
    }

    public synchronized ClanResult kick(UUID actorId, UUID targetId) throws IOException {
        Clan clan = getClanForPlayerInternal(actorId);
        if (clan == null || !hasPermission(clan, actorId, ClanPermission.KICK_MEMBER)) return ClanResult.failure("You cannot remove members.");
        ClanMember target = clan.getMember(targetId);
        ClanMember actor = clan.getMember(actorId);
        if (target == null) return ClanResult.failure("That player is not in your clan.");
        if (targetId.equals(clan.getOwnerId())) return ClanResult.failure("The clan owner cannot be kicked.");
        if (target.getRole() == ClanRole.OFFICER && actor.getRole() != ClanRole.OWNER) {
            return ClanResult.failure("Only the owner can remove an officer.");
        }
        clan.removeMember(targetId);
        clanByPlayer.remove(targetId);
        persist();
        return ClanResult.success(target.getPlayerName() + " was removed from the clan.", clan);
    }

    public synchronized ClanResult setOfficer(UUID actorId, UUID targetId, boolean officer) throws IOException {
        Clan clan = getClanForPlayerInternal(actorId);
        if (clan == null || !hasPermission(clan, actorId, ClanPermission.PROMOTE_MEMBER)) return ClanResult.failure("Only the owner can change member roles.");
        ClanMember target = clan.getMember(targetId);
        if (target == null || targetId.equals(clan.getOwnerId())) return ClanResult.failure("Member not found.");
        target.setRole(officer ? ClanRole.OFFICER : ClanRole.MEMBER);
        persist();
        return ClanResult.success(target.getPlayerName() + (officer ? " promoted to officer." : " changed to member."), clan);
    }

    public synchronized ClanResult transferOwnership(UUID ownerId, UUID targetId) throws IOException {
        Clan clan = getClanForPlayerInternal(ownerId);
        if (clan == null || !hasPermission(clan, ownerId, ClanPermission.TRANSFER_OWNERSHIP)) return ClanResult.failure("Only the owner can transfer ownership.");
        ClanMember target = clan.getMember(targetId);
        ClanMember currentOwner = clan.getMember(ownerId);
        if (target == null || targetId.equals(ownerId)) return ClanResult.failure("Choose another clan member.");
        currentOwner.setRole(ClanRole.OFFICER);
        target.setRole(ClanRole.MEMBER);
        target = new ClanMember(target.getPlayerId(), target.getPlayerName(), ClanRole.OWNER, target.getJoinedAt());
        target.updateLastSeen(System.currentTimeMillis());
        clan.removeMember(targetId);
        clan.addMember(target);
        clan.setOwnerId(targetId);
        persist();
        return ClanResult.success("Clan ownership transferred to " + target.getPlayerName() + ".", clan);
    }

    public synchronized ClanResult rename(UUID actorId, String name) throws IOException {
        Clan clan = getClanForPlayerInternal(actorId);
        if (clan == null || !hasPermission(clan, actorId, ClanPermission.EDIT_SETTINGS)) return ClanResult.failure("Only the owner can edit clan settings.");
        try { clan.setName(name); persist(); return ClanResult.success("Clan name updated.", clan); }
        catch (IllegalArgumentException ex) { return ClanResult.failure(ex.getMessage()); }
    }

    public synchronized ClanResult changeTag(UUID actorId, String tag) throws IOException {
        Clan clan = getClanForPlayerInternal(actorId);
        if (clan == null || !hasPermission(clan, actorId, ClanPermission.EDIT_SETTINGS)) return ClanResult.failure("Only the owner can edit clan settings.");
        try { clan.setTag(tag); persist(); return ClanResult.success("Clan tag updated.", clan); }
        catch (IllegalArgumentException ex) { return ClanResult.failure(ex.getMessage()); }
    }

    public synchronized ClanResult disband(UUID actorId) throws IOException {
        Clan clan = getClanForPlayerInternal(actorId);
        if (clan == null || !hasPermission(clan, actorId, ClanPermission.DISBAND)) return ClanResult.failure("Only the owner can disband the clan.");
        clans.remove(clan.getId());
        for (UUID memberId : clan.getMembers().keySet()) clanByPlayer.remove(memberId);
        persist();
        return ClanResult.success("Clan " + clan.getName() + " disbanded.", null);
    }

    public synchronized ClanResult sendMessage(UUID senderId, String senderName, String message) throws IOException {
        Clan clan = getClanForPlayerInternal(senderId);
        if (clan == null || !hasPermission(clan, senderId, ClanPermission.CHAT)) return ClanResult.failure("You are not in a clan.");
        if (senderName == null || senderName.trim().isEmpty()) return ClanResult.failure("A valid sender name is required.");
        if (message == null || message.trim().isEmpty()) return ClanResult.failure("Message cannot be empty.");
        if (message.trim().length() > 256) return ClanResult.failure("Clan messages are limited to 256 characters.");
        clan.addMessage(new ClanChatMessage(UUID.randomUUID(), senderId, senderName, message, System.currentTimeMillis()));
        persist();
        return ClanResult.success("Message sent.", clan);
    }

    public synchronized Clan getClanForPlayer(UUID playerId) {
        Clan clan = getClanForPlayerInternal(playerId);
        return clan == null ? null : clan.copy();
    }
    public synchronized Clan getClan(UUID clanId) {
        Clan clan = clans.get(clanId);
        return clan == null ? null : clan.copy();
    }
    public synchronized List<Clan> getAllClans() {
        List<Clan> result = new ArrayList<Clan>();
        for (Clan clan : clans.values()) result.add(clan.copy());
        Collections.sort(result, new Comparator<Clan>() {
            @Override public int compare(Clan a, Clan b) { return a.getName().compareToIgnoreCase(b.getName()); }
        });
        return Collections.unmodifiableList(result);
    }
    public synchronized List<ClanInvite> getInvitesFor(UUID playerId) {
        List<ClanInvite> result = new ArrayList<ClanInvite>();
        long now = System.currentTimeMillis();
        for (Clan clan : clans.values()) {
            ClanInvite invite = clan.getInvite(playerId);
            if (invite != null && !invite.isExpired(now)) result.add(invite);
        }
        return Collections.unmodifiableList(result);
    }
    public synchronized boolean hasPermission(UUID clanId, UUID playerId, ClanPermission permission) {
        Clan clan = clans.get(clanId);
        return clan != null && hasPermission(clan, playerId, permission);
    }

    private boolean hasPermission(Clan clan, UUID playerId, ClanPermission permission) {
        ClanMember member = clan.getMember(playerId);
        if (member == null || permission == null) return false;
        if (permission == ClanPermission.VIEW || permission == ClanPermission.CHAT) return true;
        if (member.getRole() == ClanRole.OWNER) return true;
        if (member.getRole() == ClanRole.OFFICER) {
            return permission == ClanPermission.INVITE || permission == ClanPermission.KICK_MEMBER;
        }
        return false;
    }
    private Clan getClanForPlayerInternal(UUID playerId) {
        UUID clanId = clanByPlayer.get(playerId);
        return clanId == null ? null : clans.get(clanId);
    }
    private void persist() throws IOException {
        try {
            repository.saveAll(new ArrayList<Clan>(clans.values()));
        } catch (IOException saveFailure) {
            // Keep the live service consistent with the last successfully saved state.
            try {
                List<Clan> savedClans = repository.loadAll();
                clans.clear();
                clanByPlayer.clear();
                for (Clan savedClan : savedClans) {
                    clans.put(savedClan.getId(), savedClan);
                    for (ClanMember member : savedClan.getMembers().values()) {
                        clanByPlayer.put(member.getPlayerId(), savedClan.getId());
                    }
                }
            } catch (IOException restoreFailure) {
                saveFailure.addSuppressed(restoreFailure);
            }
            throw saveFailure;
        }
    }
}
