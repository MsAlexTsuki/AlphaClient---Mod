package com.alpharip.staffmod.BuildTimelapse.capture;
public interface CaptureListener {void onCaptured(CaptureFrame frame);void onFailure(long index,Throwable error);}
