package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** In-memory directed relationship graph for evidence references. */
public final class EvidenceRelationshipGraph {
 private final List<Edge> edges=new ArrayList<Edge>();
 public void connect(String from,String to,String relationship){if(blank(from)||blank(to))throw new IllegalArgumentException("Both references are required");edges.add(new Edge(from,to,relationship));}
 public List<Edge> getEdges(){return Collections.unmodifiableList(new ArrayList<Edge>(edges));}
 private static boolean blank(String s){return s==null||s.trim().isEmpty();}
 public static final class Edge {private final String from,to,relationship;private Edge(String from,String to,String relationship){this.from=from;this.to=to;this.relationship=relationship==null?"related":relationship;}public String getFrom(){return from;}public String getTo(){return to;}public String getRelationship(){return relationship;}}
}