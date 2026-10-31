package com.alpharip.staffmod.staffops.ideas;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
/** Chat lane model; message classification, kill collapsing and chat UI are pending. */
public final class ChatTriage {
    public enum Lane { FACTION, SYSTEM, KILLS, STAFF, OTHER }
    public static final class Message {
        public final Lane lane;
        public final String text;
        public Message(Lane lane, String text) { this.lane = lane == null ? Lane.OTHER : lane; this.text = text == null ? "" : text; }
    }
    private final EnumMap<Lane, List<Message>> lanes = new EnumMap<Lane, List<Message>>(Lane.class);
    public ChatTriage() { for (Lane lane : Lane.values()) lanes.put(lane, new ArrayList<Message>()); }
    public void add(Message message) { if (message != null) lanes.get(message.lane).add(message); }
    public List<Message> getMessages(Lane lane) {
        return Collections.unmodifiableList(new ArrayList<Message>(lanes.get(lane == null ? Lane.OTHER : lane)));
    }
    public void clear() { for (List<Message> lane : lanes.values()) lane.clear(); }
}
