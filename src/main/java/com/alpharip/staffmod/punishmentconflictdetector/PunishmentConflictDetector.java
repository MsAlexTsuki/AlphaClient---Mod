package com.alpharip.staffmod.punishmentconflictdetector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Local conflict-analysis engine.
 * Records must be supplied by an approved data adapter; this class does not query a server.
 */
public final class PunishmentConflictDetector {
    public static final String NAME = "Punishment Conflict Detector";
    public static final String CATEGORY = "StaffMods";
    public static final String REQUIRED_ACCESS = "Trial Moderator";

    private final List<PunishmentRecord> records = new ArrayList<PunishmentRecord>();

    public synchronized void addRecord(PunishmentRecord record) {
        if (record == null) throw new IllegalArgumentException("Record must not be null.");
        for (PunishmentRecord existing : records) {
            if (existing.getRecordId().equals(record.getRecordId())) {
                throw new IllegalArgumentException("A punishment record with this ID already exists.");
            }
        }
        records.add(record);
    }

    public synchronized boolean removeRecord(String recordId) {
        if (recordId == null) return false;
        for (int i = 0; i < records.size(); i++) {
            if (records.get(i).getRecordId().equals(recordId)) {
                records.remove(i);
                return true;
            }
        }
        return false;
    }

    public synchronized void clearRecords() {
        records.clear();
    }

    public synchronized List<PunishmentRecord> getRecords() {
        return Collections.unmodifiableList(new ArrayList<PunishmentRecord>(records));
    }

    /**
     * Flags two records only when both are active now, belong to the same player,
     * and have the same punishment type. Different types are not assumed to conflict.
     */
    public synchronized List<PunishmentConflict> detectConflicts(long now) {
        List<PunishmentConflict> conflicts = new ArrayList<PunishmentConflict>();
        for (int i = 0; i < records.size(); i++) {
            PunishmentRecord first = records.get(i);
            if (!first.isActiveAt(now)) continue;
            for (int j = i + 1; j < records.size(); j++) {
                PunishmentRecord second = records.get(j);
                if (!second.isActiveAt(now)) continue;
                if (first.normalizedTarget().equals(second.normalizedTarget())
                        && first.getType() == second.getType()) {
                    conflicts.add(new PunishmentConflict(first, second));
                }
            }
        }
        return Collections.unmodifiableList(conflicts);
    }

    public List<PunishmentConflict> detectConflicts() {
        return detectConflicts(System.currentTimeMillis());
    }

    public PunishmentConflictDetectorGui createGui(net.minecraft.client.gui.GuiScreen parent) {
        return new PunishmentConflictDetectorGui(parent, this);
    }
}
