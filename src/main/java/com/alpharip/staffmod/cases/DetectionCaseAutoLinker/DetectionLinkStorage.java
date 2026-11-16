package com.alpharip.staffmod.cases.detectioncaseautolinker;
import com.google.gson.*;
import java.io.*;
import java.util.*;

public final class DetectionLinkStorage {
    private final File file;

    public DetectionLinkStorage(File file) {
        this.file = file;
    }

    public List<DetectionRecord> load() {
        List<DetectionRecord> records = new ArrayList<DetectionRecord>();
        if (file == null || !file.isFile()) return records;

        try (FileReader reader = new FileReader(file)) {
            JsonObject root = new JsonParser().parse(reader).getAsJsonObject();
            JsonArray cases = root.getAsJsonArray("cases");
            if (cases == null) return records;

            for (JsonElement element : cases) {
                try {
                    JsonObject object = element.getAsJsonObject();
                    JsonObject attributes = object.getAsJsonObject("attributes");
                    Map<String, String> values = new LinkedHashMap<String, String>();
                    if (attributes != null) {
                        for (Map.Entry<String, JsonElement> entry : attributes.entrySet()) {
                            values.put(entry.getKey(), entry.getValue().getAsString());
                        }
                    }
                    records.add(new DetectionRecord(
                        object.get("id").getAsString(),
                        object.get("label").getAsString(),
                        values
                    ));
                } catch (Exception invalidRecord) {
                    // Skip malformed records while retaining valid records.
                }
            }
        } catch (Exception exception) {
            System.out.println("[AlphaClient] Detection link load failed: " + exception.getMessage());
        }
        return records;
    }

    public Set<String> loadKeys() {
        Set<String> keys = new LinkedHashSet<String>();
        if (file == null || !file.isFile()) return keys;

        try (FileReader reader = new FileReader(file)) {
            JsonObject root = new JsonParser().parse(reader).getAsJsonObject();
            JsonArray storedKeys = root.getAsJsonArray("requiredKeys");
            if (storedKeys != null) {
                for (JsonElement element : storedKeys) {
                    String key = element.getAsString();
                    if (key != null && !key.trim().isEmpty()) keys.add(key.trim());
                }
            }
        } catch (Exception ignored) {
            // Missing or malformed match-key data leaves matching unconfigured.
        }
        return keys;
    }

    public boolean save(List<DetectionRecord> cases, Set<String> keys) {
        if (file == null || cases == null || keys == null) return false;
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) return false;

        JsonObject root = new JsonObject();
        JsonArray caseArray = new JsonArray();
        JsonArray keyArray = new JsonArray();
        for (DetectionRecord record : cases) {
            JsonObject object = new JsonObject();
            JsonObject attributes = new JsonObject();
            object.addProperty("id", record.getId());
            object.addProperty("label", record.getLabel());
            for (Map.Entry<String, String> entry : record.getAttributes().entrySet()) {
                attributes.addProperty(entry.getKey(), entry.getValue());
            }
            object.add("attributes", attributes);
            caseArray.add(object);
        }
        for (String key : keys) keyArray.add(new JsonPrimitive(key));
        root.add("cases", caseArray);
        root.add("requiredKeys", keyArray);

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(root.toString());
            return true;
        } catch (Exception exception) {
            return false;
        }
    }
}