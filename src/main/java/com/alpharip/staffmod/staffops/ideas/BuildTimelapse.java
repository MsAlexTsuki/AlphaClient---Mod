package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Records timestamped build snapshots. Automatic capture and video encoding are not connected. */
public class BuildTimelapse {
 public static final class Frame {public final long timestamp;public final String imagePath;Frame(long t,String p){timestamp=t;imagePath=p;}}
 private boolean recording;private final List<Frame> frames=new ArrayList<Frame>();
 public void start(){recording=true;}public void stop(){recording=false;}public boolean isRecording(){return recording;}
 public void record(long timestamp,String imagePath){if(!recording) return;if(imagePath==null||imagePath.trim().isEmpty())throw new IllegalArgumentException("Image path required");if(!frames.isEmpty()&&timestamp<frames.get(frames.size()-1).timestamp)throw new IllegalArgumentException("Timestamps must be ordered");frames.add(new Frame(timestamp,imagePath));}
 public List<Frame> getFrames(){return Collections.unmodifiableList(new ArrayList<Frame>(frames));}public void clear(){frames.clear();}
}