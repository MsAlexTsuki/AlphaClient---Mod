package com.alpharip.staffmod.remembervillagers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Properties;

/** Local-only persistence for remembered villager trades. */
final class VillagerTradeStorage {
    void load(File file, VillagerTradeRegistry registry) {
        if (file == null || !file.isFile()) return;
        Properties values = new Properties();
        try (FileInputStream input = new FileInputStream(file)) {
            values.load(input);
            int recordCount = parseInt(values.getProperty("recordCount"), 0);
            List<VillagerTradeRecord> records = new ArrayList<VillagerTradeRecord>();
            for (int i = 0; i < recordCount; i++) {
                String prefix = "record." + i + ".";
                String id = decode(values.getProperty(prefix + "id", ""));
                String name = decode(values.getProperty(prefix + "name", ""));
                int dimension = parseInt(values.getProperty(prefix + "dimension"), 0);
                int x = parseInt(values.getProperty(prefix + "x"), 0);
                int y = parseInt(values.getProperty(prefix + "y"), 0);
                int z = parseInt(values.getProperty(prefix + "z"), 0);
                long capturedAt = parseLong(values.getProperty(prefix + "capturedAt"), 0L);
                int tradeCount = parseInt(values.getProperty(prefix + "tradeCount"), 0);
                List<VillagerTradeEntry> trades = new ArrayList<VillagerTradeEntry>();
                for (int t = 0; t < tradeCount; t++) {
                    String trade = prefix + "trade." + t + ".";
                    trades.add(new VillagerTradeEntry(
                            decode(values.getProperty(trade + "firstInput", "")),
                            parseInt(values.getProperty(trade + "firstInputCount"), 0),
                            decode(values.getProperty(trade + "secondInput", "")),
                            parseInt(values.getProperty(trade + "secondInputCount"), 0),
                            decode(values.getProperty(trade + "output", "")),
                            parseInt(values.getProperty(trade + "outputCount"), 0)));
                }
                if (!id.isEmpty()) {
                    records.add(new VillagerTradeRecord(id, name, dimension, x, y, z, capturedAt, trades));
                }
            }
            registry.replaceAll(records);
        } catch (IOException | IllegalArgumentException ignored) {
            // Preserve the current in-memory state if the local file cannot be read.
        }
    }

    void save(File file, VillagerTradeRegistry registry) {
        if (file == null || registry == null) return;
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) return;

        Properties values = new Properties();
        List<VillagerTradeRecord> records = registry.getAll();
        values.setProperty("recordCount", Integer.toString(records.size()));
        for (int i = 0; i < records.size(); i++) {
            VillagerTradeRecord record = records.get(i);
            String prefix = "record." + i + ".";
            values.setProperty(prefix + "id", encode(record.getVillagerId()));
            values.setProperty(prefix + "name", encode(record.getVillagerName()));
            values.setProperty(prefix + "dimension", Integer.toString(record.getDimensionId()));
            values.setProperty(prefix + "x", Integer.toString(record.getX()));
            values.setProperty(prefix + "y", Integer.toString(record.getY()));
            values.setProperty(prefix + "z", Integer.toString(record.getZ()));
            values.setProperty(prefix + "capturedAt", Long.toString(record.getCapturedAt()));
            values.setProperty(prefix + "tradeCount", Integer.toString(record.getTrades().size()));
            for (int t = 0; t < record.getTrades().size(); t++) {
                VillagerTradeEntry trade = record.getTrades().get(t);
                String item = prefix + "trade." + t + ".";
                values.setProperty(item + "firstInput", encode(trade.getFirstInput()));
                values.setProperty(item + "firstInputCount", Integer.toString(trade.getFirstInputCount()));
                values.setProperty(item + "secondInput", encode(trade.getSecondInput()));
                values.setProperty(item + "secondInputCount", Integer.toString(trade.getSecondInputCount()));
                values.setProperty(item + "output", encode(trade.getOutput()));
                values.setProperty(item + "outputCount", Integer.toString(trade.getOutputCount()));
            }
        }
        try (FileOutputStream output = new FileOutputStream(file)) {
            values.store(output, "Remember Villagers - local trade history");
        } catch (IOException ignored) {
            // A local save failure must not interrupt the game.
        }
    }

    private String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String value) {
        if (value == null || value.isEmpty()) return "";
        return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
    }

    private int parseInt(String value, int fallback) {
        try { return Integer.parseInt(value); }
        catch (NumberFormatException ignored) { return fallback; }
    }

    private long parseLong(String value, long fallback) {
        try { return Long.parseLong(value); }
        catch (NumberFormatException ignored) { return fallback; }
    }
}
