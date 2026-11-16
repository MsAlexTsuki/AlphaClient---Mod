package com.alpharip.staffmod.cases.investigationcasemanager;

import java.io.File;

/** Facade for the local investigation case catalogue prototype. */
public final class InvestigationCaseManager {
    private final InvestigationCases cases = new InvestigationCases();
    private final InvestigationCaseStorage storage;

    public InvestigationCaseManager(File storageFile) {
        storage = new InvestigationCaseStorage(storageFile);
    }

    public InvestigationCases getCases() { return cases; }

    public InvestigationCases.CaseEntry create(String title, String summary, long createdAtMillis) {
        return cases.create(title, summary, createdAtMillis);
    }

    public boolean remove(long id) { return cases.remove(id); }

    public boolean load() {
        cases.clear();
        for (InvestigationCases.CaseEntry entry : storage.load()) {
            cases.restore(entry.getId(), entry.getTitle(), entry.getSummary(), entry.getCreatedAtMillis());
        }
        return true;
    }

    public boolean save() { return storage.save(cases.getCases()); }

    public InvestigationCaseManagerGui createGui(net.minecraft.client.gui.GuiScreen parent) {
        return new InvestigationCaseManagerGui(parent, this);
    }
}