package com.alpharip.staffmod.staffops.ideas;

/** Records a detection label and confidence value supplied by an existing detector. */
public final class EvidenceDetectionMarker {
 private String detector="",description=""; private double confidence; private long timestampMillis;
 public void setDetection(String detector,String description,double confidence,long timestampMillis){this.detector=detector==null?"":detector;this.description=description==null?"":description;this.confidence=Math.max(0.0,Math.min(1.0,confidence));this.timestampMillis=timestampMillis;}
 public String getDetector(){return detector;} public String getDescription(){return description;} public double getConfidence(){return confidence;} public long getTimestampMillis(){return timestampMillis;}
}