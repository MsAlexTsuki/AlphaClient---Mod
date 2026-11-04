package com.alpharip.staffmod.Minimap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

/** Disk-backed, world- and dimension-specific cache of explored minimap terrain. */
final class PersistentTerrainCache {
    private static final int MAGIC = 0x414C504D;
    private static final int VERSION = 1;
    private final File file;
    private final String identity;
    private final Map<Long, ChunkColors> chunks = new HashMap<Long, ChunkColors>();

    PersistentTerrainCache(File gameDirectory, String identity) {
        this.identity = identity;
        File directory = new File(gameDirectory, "config/alphaclient/minimap-cache");
        if (!directory.isDirectory()) directory.mkdirs();
        this.file = new File(directory, fileKey(identity) + ".dat");
        load();
    }

    boolean hasChunk(int chunkX, int chunkZ) {
        return chunks.containsKey(key(chunkX, chunkZ));
    }

    long getFingerprint(int chunkX, int chunkZ) {
        ChunkColors data = chunks.get(key(chunkX, chunkZ));
        return data == null ? Long.MIN_VALUE : data.fingerprint;
    }

    int getColor(int chunkX, int chunkZ, int localX, int localZ) {
        ChunkColors data = chunks.get(key(chunkX, chunkZ));
        return data == null ? 0 : data.colors[(localZ & 15) * 16 + (localX & 15)];
    }

    void putChunk(int chunkX, int chunkZ, long fingerprint, int[] colors) {
        if (colors == null || colors.length != 256) return;
        ChunkColors data = new ChunkColors();
        data.fingerprint = fingerprint;
        System.arraycopy(colors, 0, data.colors, 0, 256);
        chunks.put(key(chunkX, chunkZ), data);
    }

    synchronized void save() {
        File temporary = new File(file.getParentFile(), file.getName() + ".tmp");
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(temporary)));
            out.writeInt(MAGIC);
            out.writeInt(VERSION);
            out.writeUTF(identity);
            out.writeInt(chunks.size());
            for (Map.Entry<Long, ChunkColors> entry : chunks.entrySet()) {
                out.writeLong(entry.getKey().longValue());
                out.writeLong(entry.getValue().fingerprint);
                for (int color : entry.getValue().colors) out.writeInt(color);
            }
            out.close();
            out = null;
            if (file.exists() && !file.delete()) throw new java.io.IOException("Could not replace old terrain cache");
            if (!temporary.renameTo(file)) throw new java.io.IOException("Could not move terrain cache into place");
        } catch (Exception e) {
            System.err.println("[AlphaClient] Could not save minimap terrain cache: " + e.getMessage());
        } finally {
            if (out != null) try { out.close(); } catch (Exception ignored) { }
        }
    }

    private void load() {
        if (!file.isFile()) return;
        DataInputStream in = null;
        try {
            in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
            if (in.readInt() != MAGIC || in.readInt() != VERSION) return;
            if (!identity.equals(in.readUTF())) return;
            int count = in.readInt();
            if (count < 0 || count > 100000) return;
            for (int i = 0; i < count; i++) {
                long key = in.readLong();
                ChunkColors data = new ChunkColors();
                data.fingerprint = in.readLong();
                for (int color = 0; color < 256; color++) data.colors[color] = in.readInt();
                chunks.put(Long.valueOf(key), data);
            }
        } catch (Exception e) {
            chunks.clear();
            System.err.println("[AlphaClient] Could not load minimap terrain cache: " + e.getMessage());
        } finally {
            if (in != null) try { in.close(); } catch (Exception ignored) { }
        }
    }

    private static long key(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
    }

    private static String fileKey(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes("UTF-8"));
            StringBuilder result = new StringBuilder();
            for (byte b : digest) result.append(String.format("%02x", b & 255));
            return result.toString();
        } catch (Exception e) {
            return Integer.toHexString(value.hashCode());
        }
    }

    private static final class ChunkColors {
        private long fingerprint;
        private final int[] colors = new int[256];
    }
}
