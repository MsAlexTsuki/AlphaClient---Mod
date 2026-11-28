package com.alpharip.staffmod.staffops.ideas;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Stores caller-computed heat values by grid coordinate; does not render a heatmap. */
public final class InvestigationHeatmap {
 private final Map<String,Double> values=new LinkedHashMap<String,Double>();
 public void setValue(int x,int z,double value){if(Double.isNaN(value)||Double.isInfinite(value))throw new IllegalArgumentException("Value must be finite");values.put(key(x,z),value);}
 public Double getValue(int x,int z){return values.get(key(x,z));}
 public Map<String,Double> getValues(){return Collections.unmodifiableMap(new LinkedHashMap<String,Double>(values));}
 public void clear(){values.clear();}
 private static String key(int x,int z){return x+":"+z;}
}