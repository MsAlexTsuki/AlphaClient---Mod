package com.alpharip.staffmod.caseescalationtimer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/** Local JSON persistence only. No network or Supabase access. */
public final class EscalationStorage {
    private final File file;
    public EscalationStorage(File file) { this.file = file; }

    public List<EscalationCase> load() {
        List<EscalationCase> result = new ArrayList<EscalationCase>();
        if (file == null || !file.isFile()) return result;
        FileReader reader = null;
        try {
            reader = new FileReader(file);
            JsonElement root = new JsonParser().parse(reader);
            if (root == null || !root.isJsonObject()) return result;
            JsonElement values = root.getAsJsonObject().get("cases");
            if (values == null || !values.isJsonArray()) return result;
            for (JsonElement value : values.getAsJsonArray()) {
                if (!value.isJsonObject()) continue;
                JsonObject o = value.getAsJsonObject();
                try {
                    result.add(new EscalationCase(str(o, "id"), str(o, "title"), str(o, "reference"),
                            num(o, "openedAt"), num(o, "deadlineAt"),
                            EscalationCaseState.valueOf(str(o, "state")),
                            num(o, "pausedAt"), num(o, "totalPausedMillis")));
                } catch (RuntimeException ignored) { }
            }
        } catch (Exception exception) {
            System.out.println("[AlphaClient] Could not load Case Escalation Timer: " + exception.getMessage());
        } finally {
            if (reader != null) try { reader.close(); } catch (Exception ignored) { }
        }
        return result;
    }

    public boolean save(List<EscalationCase> cases) {
        if (file == null || cases == null) return false;
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) return false;
        JsonArray values = new JsonArray();
        for (EscalationCase item : cases) {
            JsonObject o = new JsonObject();
            o.addProperty("id", item.getId());
            o.addProperty("title", item.getTitle());
            o.addProperty("reference", item.getReference());
            o.addProperty("openedAt", item.getOpenedAt());
            o.addProperty("deadlineAt", item.getDeadlineAt());
            o.addProperty("state", item.getState().name());
            o.addProperty("pausedAt", item.getPausedAt());
            o.addProperty("totalPausedMillis", item.getTotalPausedMillis());
            values.add(o);
        }
        JsonObject root = new JsonObject();
        root.add("cases", values);
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            writer.write(root.toString());
            writer.flush();
            return true;
        } catch (Exception exception) {
            System.out.println("[AlphaClient] Could not save Case Escalation Timer: " + exception.getMessage());
            return false;
        } finally {
            if (writer != null) try { writer.close(); } catch (Exception ignored) { }
        }
    }

    private static String str(JsonObject o, String key) {
        JsonElement value = o.get(key);
        return value != null && value.isJsonPrimitive() ? value.getAsString() : "";
    }
    private static long num(JsonObject o, String key) {
        JsonElement value = o.get(key);
        return value != null && value.isJsonPrimitive() ? value.getAsLong() : 0L;
    }
}
