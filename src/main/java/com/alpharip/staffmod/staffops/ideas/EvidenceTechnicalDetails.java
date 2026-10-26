package com.alpharip.staffmod.staffops.ideas;

/** Holds technical detail fields supplied by a caller; performs no system inspection. */
public final class EvidenceTechnicalDetails {
 private String format="",dimensions="",checksum="",source="",notes="";
 public void setDetails(String format,String dimensions,String checksum,String source,String notes){this.format=clean(format);this.dimensions=clean(dimensions);this.checksum=clean(checksum);this.source=clean(source);this.notes=clean(notes);}
 private static String clean(String value){return value==null?"":value;}
 public String getFormat(){return format;} public String getDimensions(){return dimensions;} public String getChecksum(){return checksum;} public String getSource(){return source;} public String getNotes(){return notes;}
}