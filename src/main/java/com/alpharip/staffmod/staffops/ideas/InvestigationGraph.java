package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** In-memory directed graph of investigation entities and relationships. */
public final class InvestigationGraph {
 private final Map<String,String> nodes=new LinkedHashMap<String,String>();
 private final List<Edge> edges=new ArrayList<Edge>();
 public void addNode(String id,String label){if(id==null||id.trim().isEmpty())throw new IllegalArgumentException("Node id must not be blank");nodes.put(id,label==null?"":label);}
 public void addEdge(String from,String to,String relation){if(!nodes.containsKey(from)||!nodes.containsKey(to))throw new IllegalArgumentException("Both nodes must exist");edges.add(new Edge(from,to,relation));}
 public Map<String,String> getNodes(){return Collections.unmodifiableMap(new LinkedHashMap<String,String>(nodes));}
 public List<Edge> getEdges(){return Collections.unmodifiableList(new ArrayList<Edge>(edges));}
 public static final class Edge {private final String from,to,relation;private Edge(String from,String to,String relation){this.from=from;this.to=to;this.relation=relation==null?"":relation;}public String getFrom(){return from;}public String getTo(){return to;}public String getRelation(){return relation;}}
}