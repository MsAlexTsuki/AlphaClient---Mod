package com.alpharip.staffmod.BuildTimelapse.capture;
import java.io.File;public interface CaptureBackend {CaptureFrame capture(long index,long timestamp,File output,CaptureSettings settings)throws Exception;}
