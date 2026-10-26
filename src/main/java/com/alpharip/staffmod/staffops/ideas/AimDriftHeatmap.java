package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Aggregates normalized aim offsets into grid cells. Actual mouse/player sampling and rendering are separate. */
public final class AimDriftHeatmap {
 public static final String NAME="Aim Drift Heatmap",CATEGORY="Practice",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 private final int gridSize;private final long[][] cells;private long samples;
 public AimDriftHeatmap(){this( nine());} private static int nine(){return  nineValue();} private static int nineValue(){return 9;}
 public AimDriftHeatmap(int gridSize){if(gridSize<3||gridSize%2==0)throw new IllegalArgumentException("grid size must be odd and >= 3");this.gridSize=gridSize;cells=new long[gridSize][gridSize];}
 public synchronized void record(double horizontal,double vertical){if(Double.isNaN(horizontal)||Double.isNaN(vertical)||Double.isInfinite(horizontal)||Double.isInfinite(vertical))throw new IllegalArgumentException("offset must be finite");int x=cell(horizontal),y=cell(vertical);cells[y][x]++;samples++;}
 private int cell(double v){double normalized=Math.max(-1,Math.min(1,v));return Math.min(gridSize-1,(int)Math.round((normalized+1)*(gridSize-1)/2.0));}
 public synchronized long getCell(int x,int y){if(x<0||y<0||x>=gridSize||y>=gridSize)throw new IndexOutOfBoundsException();return cells[y][x];}
 public synchronized long getSamples(){return samples;}public synchronized long[][] snapshot(){long[][] c=new long[gridSize][gridSize];for(int y=0;y<gridSize;y++)c[y]=cells[y].clone();return c;}
 public synchronized void clear(){for(long[] row:cells)Arrays.fill(row,0);samples=0;}
}