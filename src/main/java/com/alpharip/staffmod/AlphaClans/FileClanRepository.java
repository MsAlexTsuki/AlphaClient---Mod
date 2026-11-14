package com.alpharip.staffmod.AlphaClans;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Local-only persistence. No Supabase, account credentials, or network calls are used. */
public final class FileClanRepository implements ClanRepository {
    private static final int MAGIC = 0x41434C4E;
    private static final int VERSION = 1;
    private static final int MAX_CLANS = 1000;
    private static final int MAX_MEMBERS = 100;
    private static final int MAX_INVITES = 1000;
    private static final int MAX_MESSAGES = 100;
    private final Path file;

    public FileClanRepository(Path file) {
        if (file == null) throw new IllegalArgumentException("Storage path is required");
        this.file = file;
    }

    @Override
    public synchronized List<Clan> loadAll() throws IOException {
        List<Clan> result = new ArrayList<Clan>();
        if (!Files.exists(file)) return result;
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(Files.newInputStream(file)))) {
            if (in.readInt() != MAGIC) throw new IOException("Invalid AlphaClans data file");
            int version = in.readInt();
            if (version != VERSION) throw new IOException("Unsupported AlphaClans data version: " + version);
            int clanCount = readCount(in, MAX_CLANS);
            for (int i = 0; i < clanCount; i++) {
                Clan clan = new Clan(readUuid(in), in.readUTF(), in.readUTF(), readUuid(in), in.readLong());
                int memberCount = readCount(in, MAX_MEMBERS);
                for (int m = 0; m < memberCount; m++) {
                    UUID playerId = readUuid(in);
                    String playerName = in.readUTF();
                    ClanRole role;
                    try { role = ClanRole.valueOf(in.readUTF()); }
                    catch (IllegalArgumentException ex) { throw new IOException("Unknown clan role", ex); }
                    long joinedAt = in.readLong();
                    long lastSeenAt = in.readLong();
                    ClanMember member = new ClanMember(playerId, playerName, role, joinedAt);
                    member.updateLastSeen(lastSeenAt);
                    clan.restoreMember(member);
                }
                int inviteCount = readCount(in, MAX_INVITES);
                for (int n = 0; n < inviteCount; n++) {
                    clan.restoreInvite(new ClanInvite(readUuid(in), readUuid(in), readUuid(in), in.readLong(), in.readLong()));
                }
                int messageCount = readCount(in, MAX_MESSAGES);
                for (int n = 0; n < messageCount; n++) {
                    clan.restoreMessage(new ClanChatMessage(readUuid(in), readUuid(in), in.readUTF(), in.readUTF(), in.readLong()));
                }
                result.add(clan);
            }
            if (in.read() != -1) throw new IOException("Unexpected trailing data in AlphaClans file");
        } catch (EOFException ex) {
            throw new IOException("AlphaClans data file is incomplete", ex);
        } catch (IllegalArgumentException ex) {
            throw new IOException("AlphaClans data file contains invalid records", ex);
        }
        return result;
    }

    @Override
    public synchronized void saveAll(List<Clan> clans) throws IOException {
        if (clans == null) throw new IllegalArgumentException("Clan list is required");
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) Files.createDirectories(parent);
        Path temp = file.resolveSibling(file.getFileName().toString() + ".tmp");
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(temp)))) {
            out.writeInt(MAGIC);
            out.writeInt(VERSION);
            out.writeInt(clans.size());
            for (Clan clan : clans) {
                writeUuid(out, clan.getId());
                out.writeUTF(clan.getName());
                out.writeUTF(clan.getTag());
                writeUuid(out, clan.getOwnerId());
                out.writeLong(clan.getCreatedAt());
                out.writeInt(clan.getMembers().size());
                for (ClanMember member : clan.getMembers().values()) {
                    writeUuid(out, member.getPlayerId());
                    out.writeUTF(member.getPlayerName());
                    out.writeUTF(member.getRole().name());
                    out.writeLong(member.getJoinedAt());
                    out.writeLong(member.getLastSeenAt());
                }
                out.writeInt(clan.getInvites().size());
                for (ClanInvite invite : clan.getInvites()) {
                    writeUuid(out, invite.getClanId());
                    writeUuid(out, invite.getInviterId());
                    writeUuid(out, invite.getTargetId());
                    out.writeLong(invite.getCreatedAt());
                    out.writeLong(invite.getExpiresAt());
                }
                out.writeInt(clan.getMessages().size());
                for (ClanChatMessage message : clan.getMessages()) {
                    writeUuid(out, message.getId());
                    writeUuid(out, message.getSenderId());
                    out.writeUTF(message.getSenderName());
                    out.writeUTF(message.getMessage());
                    out.writeLong(message.getTimestamp());
                }
            }
            out.flush();
        }
        try {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static int readCount(DataInputStream in, int max) throws IOException {
        int value = in.readInt();
        if (value < 0 || value > max) throw new IOException("Invalid record count: " + value);
        return value;
    }
    private static UUID readUuid(DataInputStream in) throws IOException {
        return new UUID(in.readLong(), in.readLong());
    }
    private static void writeUuid(DataOutputStream out, UUID value) throws IOException {
        out.writeLong(value.getMostSignificantBits());
        out.writeLong(value.getLeastSignificantBits());
    }
}
