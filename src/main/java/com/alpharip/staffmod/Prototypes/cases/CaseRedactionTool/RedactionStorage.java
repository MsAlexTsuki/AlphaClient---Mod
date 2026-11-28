package com.alpharip.staffmod.cases.caseredactiontool;
import com.google.gson.*;
import java.io.*;
import java.util.*;

public final class RedactionStorage {
    private final File file;

    public RedactionStorage(File file) {
        this.file = file;
    }

    public List<RedactionDocument> load() {
        List<RedactionDocument> documents = new ArrayList<RedactionDocument>();
        if (file == null || !file.isFile()) return documents;

        try (FileReader reader = new FileReader(file)) {
            JsonObject root = new JsonParser().parse(reader).getAsJsonObject();
            if (root.get("version").getAsInt() != 1) return documents;

            for (JsonElement element : root.getAsJsonArray("documents")) {
                try {
                    JsonObject object = element.getAsJsonObject();
                    List<RedactionEntry> entries = new ArrayList<RedactionEntry>();
                    for (JsonElement entryElement : object.getAsJsonArray("entries")) {
                        JsonObject entry = entryElement.getAsJsonObject();
                        entries.add(new RedactionEntry(
                            entry.get("start").getAsInt(),
                            entry.get("end").getAsInt(),
                            entry.get("reason").getAsString(),
                            entry.get("enabled").getAsBoolean()
                        ));
                    }
                    documents.add(new RedactionDocument(
                        object.get("id").getAsString(),
                        object.get("title").getAsString(),
                        object.get("source").getAsString(),
                        entries
                    ));
                } catch (Exception invalidDocument) {
                    // Skip malformed documents without discarding valid entries.
                }
            }
        } catch (Exception exception) {
            System.out.println("[AlphaClient] Redaction load failed: " + exception.getMessage());
        }
        return documents;
    }

    public boolean save(List<RedactionDocument> documents) {
        if (file == null || documents == null) return false;
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) return false;

        JsonObject root = new JsonObject();
        root.addProperty("version", 1);
        JsonArray documentArray = new JsonArray();
        for (RedactionDocument document : documents) {
            JsonObject object = new JsonObject();
            object.addProperty("id", document.getId());
            object.addProperty("title", document.getTitle());
            object.addProperty("source", document.getSource());

            JsonArray entries = new JsonArray();
            for (RedactionEntry entry : document.getEntries()) {
                JsonObject entryObject = new JsonObject();
                entryObject.addProperty("start", entry.getStart());
                entryObject.addProperty("end", entry.getEnd());
                entryObject.addProperty("reason", entry.getReason());
                entryObject.addProperty("enabled", entry.isEnabled());
                entries.add(entryObject);
            }
            object.add("entries", entries);
            documentArray.add(object);
        }
        root.add("documents", documentArray);

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(root.toString());
            return true;
        } catch (Exception exception) {
            return false;
        }
    }
}