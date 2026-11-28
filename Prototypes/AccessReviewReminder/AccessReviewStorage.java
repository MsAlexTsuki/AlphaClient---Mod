package com.alpharip.staffmod.AccessReviewReminder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/** Reads and writes Access Review Reminder data in the local Minecraft config folder. */
final class AccessReviewStorage {
    private AccessReviewStorage() { }

    static List<AccessReviewEntry> load(File file) {
        List<AccessReviewEntry> result = new ArrayList<AccessReviewEntry>();
        if (file == null || !file.isFile()) return result;

        FileReader reader = null;
        try {
            reader = new FileReader(file);
            JsonElement root = new JsonParser().parse(reader);
            if (root == null || !root.isJsonObject()) return result;

            JsonElement recordsValue = root.getAsJsonObject().get("staff");
            if (recordsValue == null || !recordsValue.isJsonArray()) return result;

            JsonArray records = recordsValue.getAsJsonArray();
            for (JsonElement element : records) {
                if (!element.isJsonObject()) continue;
                JsonObject object = element.getAsJsonObject();
                String id = readString(object, "id");
                String name = readString(object, "staffName");
                String rank = readString(object, "rank");
                long lastReviewed = readLong(object, "lastReviewedAt");
                int intervalDays = (int) readLong(object, "intervalDays");
                if (id.isEmpty() || name.trim().isEmpty()
                        || intervalDays < 1 || intervalDays > 3650 || lastReviewed < 0L) {
                    continue;
                }
                result.add(new AccessReviewEntry(id, name, rank, lastReviewed, intervalDays));
            }
        } catch (Exception exception) {
            System.out.println("[AlphaClient] Could not load Access Review Reminder: "
                    + exception.getMessage());
        } finally {
            if (reader != null) {
                try { reader.close(); } catch (Exception ignored) { }
            }
        }
        return result;
    }

    static boolean save(File file, List<AccessReviewEntry> entries) {
        if (file == null) return false;
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) return false;

        JsonObject root = new JsonObject();
        JsonArray records = new JsonArray();
        for (AccessReviewEntry entry : entries) {
            JsonObject object = new JsonObject();
            object.addProperty("id", entry.getId());
            object.addProperty("staffName", entry.getStaffName());
            object.addProperty("rank", entry.getRank());
            object.addProperty("lastReviewedAt", entry.getLastReviewedAt());
            object.addProperty("intervalDays", entry.getIntervalDays());
            records.add(object);
        }
        root.add("staff", records);

        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            writer.write(root.toString());
            writer.flush();
            return true;
        } catch (Exception exception) {
            System.out.println("[AlphaClient] Could not save Access Review Reminder: "
                    + exception.getMessage());
            return false;
        } finally {
            if (writer != null) {
                try { writer.close(); } catch (Exception ignored) { }
            }
        }
    }

    private static String readString(JsonObject object, String key) {
        JsonElement value = object.get(key);
        return value != null && value.isJsonPrimitive() ? value.getAsString() : "";
    }

    private static long readLong(JsonObject object, String key) {
        JsonElement value = object.get(key);
        try {
            return value != null && value.isJsonPrimitive() ? value.getAsLong() : 0L;
        } catch (Exception ignored) {
            return 0L;
        }
    }
}