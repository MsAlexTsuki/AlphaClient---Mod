package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Named visibility layers for a future annotation renderer. */
public final class EvidenceAnnotationLayers {
 private final List<Layer> layers=new ArrayList<Layer>();
 public Layer add(String name,boolean visible){if(name==null||name.trim().isEmpty())throw new IllegalArgumentException("Layer name must not be blank");Layer layer=new Layer(name.trim(),visible);layers.add(layer);return layer;}
 public List<Layer> getLayers(){return Collections.unmodifiableList(new ArrayList<Layer>(layers));}
 public static final class Layer {private final String name;private boolean visible;private Layer(String name,boolean visible){this.name=name;this.visible=visible;}public String getName(){return name;}public boolean isVisible(){return visible;}public void setVisible(boolean visible){this.visible=visible;}}
}