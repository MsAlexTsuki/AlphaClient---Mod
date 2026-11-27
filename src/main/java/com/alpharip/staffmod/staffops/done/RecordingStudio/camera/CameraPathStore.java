package com.alpharip.staffmod.RecordingStudio.camera;
import java.io.IOException;
public interface CameraPathStore {void save(String id,CameraPath path)throws IOException;CameraPath load(String id)throws IOException;boolean delete(String id)throws IOException;}
