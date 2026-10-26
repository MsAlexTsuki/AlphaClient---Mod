package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores flowchart node and connection data; no visual layout is generated. */
public final class InvestigationFlowchart {
 private final List<Node> nodes=new ArrayList<Node>();
 private final List<Connection> connections=new ArrayList<Connection>();
 public void addNode(String id,String label){if(id==null||id.trim().isEmpty())throw new IllegalArgumentException("Node id must not be blank");nodes.add(new Node(id,label));}
 public void connect(String fromId,String toId,String label){connections.add(new Connection(fromId,toId,label));}
 public List<Node> getNodes(){return Collections.unmodifiableList(new ArrayList<Node>(nodes));}
 public List<Connection> getConnections(){return Collections.unmodifiableList(new ArrayList<Connection>(connections));}
 public static final class Node {private final String id,label;private Node(String id,String label){this.id=id;this.label=label==null?"":label;}public String getId(){return id;}public String getLabel(){return label;}}
 public static final class Connection {private final String fromId,toId,label;private Connection(String fromId,String toId,String label){this.fromId=fromId==null?"":fromId;this.toId=toId==null?"":toId;this.label=label==null?"":label;}public String getFromId(){return fromId;}public String getToId(){return toId;}public String getLabel(){return label;}}
}