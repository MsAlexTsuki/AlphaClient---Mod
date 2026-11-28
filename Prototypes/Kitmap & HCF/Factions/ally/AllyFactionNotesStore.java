package com.alpharip.staffmod.kitmaphcf.factions.ally;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Local JSON persistence for Ally Faction Notes.
 * A failed load disables saving so an unreadable file is not overwritten.
 */
public final class AllyFactionNotesStore {
    private static final AllyFactionNotesStore INSTANCE = new AllyFactionNotesStore();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type DATA_TYPE =
            new TypeToken<LinkedHashMap<String, List<String>>>() { }.getType();

    private final AllyFactionNotes notes = new AllyFactionNotes();
    private boolean loaded;
    private boolean loadFailed;

    private AllyFactionNotesStore() { }

    public static AllyFactionNotesStore getInstance() {
        return INSTANCE;
    }

    public synchronized void load() {
        if (loaded) {
            return;
        }
        loaded = true;

        File file = getFile();
        if (!file.isFile()) {
            return;
        }

        Reader reader = null;
        try {
            reader = new InputStreamReader(new FileInputStream(file), "UTF-8");
            Map<String, List<String>> saved = GSON.fromJson(reader, DATA_TYPE);
            if (saved == null) {
                throw new IOException("Ally faction notes file did not contain a JSON object.");
            }
            notes.replaceAll(saved);
        } catch (Exception exception) {
            loadFailed = true;
            System.err.println("[AlphaClient] Could not load Ally Faction Notes. "
                    + "Saving is disabled to protect the existing file: "
                    + exception.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                    // Nothing else can be done when closing a read-only stream.
                }
            }
        }
    }

    public synchronized void addNote(String faction, String note) {
        load();
        notes.add(faction, note);
        save();
    }

    public synchronized List<String> getNotes(String faction) {
        load();
        return notes.get(faction);
    }

    public synchronized List<String> getFactionNames() {
        load();
        return notes.getFactionNames();
    }

    public synchronized void clearFaction(String faction) {
        load();
        notes.clear(faction);
        save();
    }

    public synchronized File getFile() {
        Minecraft minecraft = Minecraft.getMinecraft();
        File gameDirectory = minecraft == null ? new File(".") : minecraft.mcDataDir;
        return new File(new File(gameDirectory, "config/alphaclient"),
                "ally-faction-notes.json");
    }

    private synchronized void save() {
        if (loadFailed) {
            return;
        }

        File file = getFile();
        File parent = file.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) {
            System.err.println("[AlphaClient] Could not create Ally Faction Notes directory.");
            return;
        }

        File temporary = new File(parent, file.getName() + ".tmp");
        Writer writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(temporary), "UTF-8");
            GSON.toJson(notes.snapshot(), DATA_TYPE, writer);
            writer.close();
            writer = null;

            try {
                Files.move(temporary.toPath(), file.toPath(),
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicFailure) {
                try {
                    Files.move(temporary.toPath(), file.toPath(),
                            StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException fallbackFailure) {
                    fallbackFailure.addSuppressed(atomicFailure);
                    throw fallbackFailure;
                }
            }
        } catch (Exception exception) {
            System.err.println("[AlphaClient] Could not save Ally Faction Notes: "
                    + exception.getMessage());
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ignored) {
                    // Preserve the original save error.
                }
            }
            if (temporary.exists() && !temporary.delete()) {
                temporary.deleteOnExit();
            }
        }
    }
}
