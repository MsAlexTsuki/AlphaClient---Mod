package com.alpharip.staffmod.cases.evidencecaselinker;
import com.google.gson.*;
import java.io.*;
import java.util.*;

public final class EvidenceLinkStorage {
    private final File file;

    public EvidenceLinkStorage(File file) {
        this.file = file;
    }

    public List<EvidenceLink> load() {
        List<EvidenceLink> links = new ArrayList<EvidenceLink>();
        if (file == null || !file.isFile()) return links;

        try (FileReader reader = new FileReader(file)) {
            JsonArray storedLinks = new JsonParser().parse(reader).getAsJsonArray();
            for (JsonElement element : storedLinks) {
                try {
                    JsonObject object = element.getAsJsonObject();
                    links.add(new EvidenceLink(
                        object.get("reference").getAsString(),
                        object.get("caseId").getAsString(),
                        object.get("note").getAsString(),
                        object.get("createdAt").getAsLong()
                    ));
                } catch (Exception invalidLink) {
                    // Skip malformed links while retaining valid links.
                }
            }
        } catch (Exception exception) {
            System.out.println("[AlphaClient] Evidence link load failed: " + exception.getMessage());
        }
        return links;
    }

    public boolean save(List<EvidenceLink> links) {
        if (file == null || links == null) return false;
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) return false;

        JsonArray array = new JsonArray();
        for (EvidenceLink link : links) {
            JsonObject object = new JsonObject();
            object.addProperty("reference", link.getEvidenceReference());
            object.addProperty("caseId", link.getCaseId());
            object.addProperty("note", link.getNote());
            object.addProperty("createdAt", link.getCreatedAt());
            array.add(object);
        }

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(array.toString());
            return true;
        } catch (Exception exception) {
            return false;
        }
    }
}