package com.alpharip.staffmod.staffops.ideas;

import java.util.LinkedHashMap;
import java.util.Map;

/** Restores snapshot fields into a new local map; it does not overwrite an active case. */
public final class InvestigationRestore {
    public Map<String, String> restore(InvestigationBackup.Snapshot snapshot) {
        if (snapshot == null) return new LinkedHashMap<String, String>();
        return new LinkedHashMap<String, String>(snapshot.getFields());
    }
}