package com.alpharip.staffmod.cases.evidencecaselinker;
import java.util.*;

public final class EvidenceLinkService {
    private final LinkedHashMap<String, EvidenceLink> links = new LinkedHashMap<String, EvidenceLink>();

    public synchronized boolean link(EvidenceLink link) {
        if (link == null) return false;
        links.put(link.getEvidenceReference(), link);
        return true;
    }

    public synchronized EvidenceLink get(String reference) {
        if (reference == null) return null;
        return links.get(reference.trim());
    }

    public synchronized boolean unlink(String reference) {
        return reference != null && links.remove(reference.trim()) != null;
    }

    public synchronized List<EvidenceLink> findByCase(String caseId) {
        List<EvidenceLink> matches = new ArrayList<EvidenceLink>();
        if (caseId == null || caseId.trim().isEmpty()) return Collections.unmodifiableList(matches);
        String normalizedCaseId = caseId.trim();
        for (EvidenceLink link : links.values()) {
            if (link.getCaseId().equalsIgnoreCase(normalizedCaseId)) matches.add(link);
        }
        return Collections.unmodifiableList(matches);
    }

    public synchronized List<EvidenceLink> findByReference(String query) {
        List<EvidenceLink> matches = new ArrayList<EvidenceLink>();
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        for (EvidenceLink link : links.values()) {
            if (link.getEvidenceReference().toLowerCase(Locale.ROOT).contains(normalizedQuery)) matches.add(link);
        }
        return Collections.unmodifiableList(matches);
    }

    public synchronized List<EvidenceLink> getLinks() {
        return Collections.unmodifiableList(new ArrayList<EvidenceLink>(links.values()));
    }

    public synchronized boolean replaceAll(List<EvidenceLink> values) {
        links.clear();
        if (values != null) {
            for (EvidenceLink link : values) {
                if (link != null) links.put(link.getEvidenceReference(), link);
            }
        }
        return true;
    }
}