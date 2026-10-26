package com.alpharip.staffmod.staffops.ideas;

import java.util.LinkedHashMap;
import java.util.Map;

/** Copies fields from an in-memory investigation backup into a new map. */
public final class InvestigationRestore {
    public Map<String, String> restore(InvestigationBackup.Snapshot snapshot) {
        if (snapshot == null) return new LinkedHashMap<String, String>();
        return new LinkedHashMap<String, String>(snapshot.getFields());
    }
}