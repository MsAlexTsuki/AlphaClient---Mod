package com.alpharip.staffmod.RecordingStudio.integration;
import com.alpharip.staffmod.RecordingStudio.camera.CameraRuntimePort;
/** Client-only camera adapter contract. Must never mutate player position, motion or input. */
public interface MinecraftCameraBridge extends CameraRuntimePort { Object getOriginalRenderViewEntity(); }
