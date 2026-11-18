package com.alpharip.staffmod.BuildTimelapse.core;
import com.alpharip.staffmod.BuildTimelapse.timeline.Timeline;import java.io.IOException;
public interface ProjectStore {void save(BuildTimelapseProject project,Timeline timeline)throws IOException;BuildTimelapseProject load(String id)throws IOException;Timeline loadTimeline(String id)throws IOException;}

