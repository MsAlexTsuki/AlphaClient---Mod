package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Records generic supplied interaction events for investigation review. */
public final class InteractionAnalyzer {
    public static final class Interaction {
        private final long timestampMillis;
        private final String actor;
        private final String target;
        private final String kind;

        public Interaction(long timestampMillis, String actor, String target, String kind) {
            this.timestampMillis = timestampMillis;
            this.actor = actor == null ? "" : actor.trim();
            this.target = target == null ? "" : target.trim();
            this.kind = kind == null ? "" : kind.trim();
        }
        public long getTimestampMillis() { return timestampMillis; }
        public String getActor() { return actor; }
        public String getTarget() { return target; }
        public String getKind() { return kind; }
    }

    private final List<Interaction> interactions = new ArrayList<Interaction>();
    public synchronized void add(Interaction interaction) {
        if (interaction != null) interactions.add(interaction);
    }
    public synchronized List<Interaction> getInteractions() {
        return Collections.unmodifiableList(new ArrayList<Interaction>(interactions));
    }
    public synchronized void clear() { interactions.clear(); }
}