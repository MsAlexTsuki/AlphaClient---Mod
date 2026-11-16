package com.alpharip.staffmod.cases.investigationcasemanager;

import com.google.gson.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public final class InvestigationCaseStorage {
    private final File file;

    public InvestigationCaseStorage(File file) { this.file = file; }

    public List<InvestigationCases.CaseEntry> load() {
        List<InvestigationCases.CaseEntry> result = new ArrayList<InvestigationCases.CaseEntry>();
        if (file == null || !file.isFile()) return result;
        try (FileReader reader = new FileReader(file)) {
            JsonObject root = new JsonParser().parse(reader).getAsJsonObject();
            if (!root.has("version") || root.get("version").getAsInt() != 1
                    || !root.has("cases") || !root.get("cases").isJsonArray()) return result;
            for (JsonElement element : root.getAsJsonArray("cases")) {
                try {
                    JsonObject item = element.getAsJsonObject();
                    result.add(new InvestigationCases().restore(
                            item.get("id").getAsLong(),
                            item.get("title").getAsString(),
                            item.get("summary").getAsString(),
                            item.get("createdAt").getAsLong()));
                } catch (Exception malformedCase) {
                    // Keep valid records when one stored record is malformed.
                }
            }
        } catch (Exception exception) {
            System.out.println("[AlphaClient] Investigation case load failed: " + exception.getMessage());
        }
        return result;
    }

    public boolean save(List<InvestigationCases.CaseEntry> cases) {
        if (file == null || cases == null) return false;
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) return false;
        JsonObject root = new JsonObject();
        root.addProperty("version", 1);
        JsonArray array = new JsonArray();
        for (InvestigationCases.CaseEntry entry : cases) {
            JsonObject item = new JsonObject();
            item.addProperty("id", entry.getId());
            item.addProperty("title", entry.getTitle());
            item.addProperty("summary", entry.getSummary());
            item.addProperty("createdAt", entry.getCreatedAtMillis());
            array.add(item);
        }
        root.add("cases", array);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(root.toString());
            return true;
        } catch (Exception exception) {
            return false;
        }
    }
}